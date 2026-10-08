package io.github.santipdr.specialpickaxes.artifact;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import java.util.*;

/** Round-robin global budget. Failed/skipped candidates also consume budget; no unbounded work on one tick. */
public final class WorkQueue {
    private static final Map<UUID,Job> JOBS=new HashMap<>();
    private static final ArrayDeque<UUID> ORDER=new ArrayDeque<>();
    private static boolean running;
    private static int lastAttempts;
    private static final class Job {
        final ServerPlayer player; final ItemStack tool; final ArtifactKind kind;
        final ResourceKey<Level> dimension; final long deadline; final ArrayDeque<WorkStep> steps;
        int accepted,completed,succeeded; boolean paused,aborted; WorkProgram region;
        Job(ServerPlayer p,ItemStack t,ArtifactKind k,List<WorkStep> work) {
            player=p;tool=t;kind=k;dimension=p.level().dimension();deadline=ArtifactState.now(p)+ArtifactConfig.JOB_TTL.get();
            steps=new ArrayDeque<>(work);accepted=work.size();
        }
    }
    private WorkQueue() {}
    public static boolean running() { return running; }
    public static boolean protectsFall(ServerPlayer p){var job=JOBS.get(p.getUUID());return job!=null&&job.tool==p.getMainHandItem()&&(job.kind==ArtifactKind.ICARUS||job.kind==ArtifactKind.WORLDBREAKER&&ArtifactState.mode(p,job.kind)==3);}
    public static void discardStale(ServerPlayer p){var j=JOBS.get(p.getUUID());if(j!=null&&(p.getMainHandItem()!=j.tool||p.level().dimension()!=j.dimension||!p.isAlive()))cancel(p);}
    public static boolean busy(ServerPlayer p) { return JOBS.containsKey(p.getUUID()); }
    public static int remaining(ServerPlayer p) { var job=JOBS.get(p.getUUID());return job==null?0:job.region==null?job.steps.size():job.region.remaining(); }
    public static String status(ServerPlayer p){var j=JOBS.get(p.getUUID());return j==null?"idle":j.region!=null&&j.region.awaiting()?"ready":j.paused?"paused":j.region!=null&&!j.region.executing()?"preparing":"executing";}
    public static int completed(ServerPlayer p){var j=JOBS.get(p.getUUID());return j==null?0:j.completed;}
    public static int succeeded(ServerPlayer p){var j=JOBS.get(p.getUUID());return j==null?0:j.succeeded;}
    public static boolean togglePause(ServerPlayer p){var j=JOBS.get(p.getUUID());if(j==null)return false;if(j.region!=null&&j.region.awaiting()){
        if(ArtifactState.of(p,j.kind).getLong("ready")>ArtifactState.now(p))return false;
        int cost=EnchantmentScaling.activationCost(j.tool,j.kind);if(!p.isCreative()&&j.tool.getMaxDamage()-j.tool.getDamageValue()<=cost)return false;
        j.tool.hurtAndBreak(cost,p,who->who.broadcastBreakEvent(net.minecraft.world.InteractionHand.MAIN_HAND));if(j.region instanceof RegionWork r&&(j.kind==ArtifactKind.CHRONICLE&&r.mode==0))ArtifactState.of(p,j.kind).remove("memory");
        ArtifactState.of(p,j.kind).putLong("ready",ArtifactState.now(p)+ArtifactConfig.COOLDOWN.get());p.getCooldowns().addCooldown(j.tool.getItem(),ArtifactConfig.COOLDOWN.get());
        j.region.confirm();j.completed=0;j.succeeded=0;j.paused=false;}else j.paused=!j.paused;return true;}
    public static boolean startRegion(ServerPlayer p,ItemStack tool,ArtifactKind kind,WorkProgram region){
        if(busy(p)||tool.isEmpty()||JOBS.size()>=ArtifactConfig.ACTIVE_JOBS.get())return false;
        var j=new Job(p,tool,kind,List.of());j.region=region;region.loadMemories(p);JOBS.put(p.getUUID(),j);ORDER.addLast(p.getUUID());return true;
    }
    public static int lastAttempts() { return lastAttempts; }
    public static boolean start(ServerPlayer p,ItemStack tool,ArtifactKind kind,List<WorkStep> steps) {
        if(busy(p) || steps.isEmpty() || tool.isEmpty() || JOBS.size()>=ArtifactConfig.ACTIVE_JOBS.get()) return false;
        var bounded=List.copyOf(steps.subList(0,Math.min(steps.size(),ArtifactConfig.JOB_LIMIT.get())));
        JOBS.put(p.getUUID(),new Job(p,tool,kind,bounded));ORDER.addLast(p.getUUID());return true;
    }
    public static boolean append(ServerPlayer p,ItemStack tool,ArtifactKind kind,WorkStep step) {
        var job=JOBS.get(p.getUUID());
        if(job==null) return start(p,tool,kind,List.of(step));
        if(job.kind!=kind || job.tool!=tool || job.accepted>=ArtifactConfig.JOB_LIMIT.get()) return false;
        job.steps.add(step);job.accepted++;return true;
    }
    public static void cancel(ServerPlayer p) { JOBS.remove(p.getUUID());ORDER.remove(p.getUUID()); }
    public static void clear() { JOBS.clear();ORDER.clear();running=false;lastAttempts=0; }
    public static void tick() {
        if(running)return; // Forge callbacks cannot recursively consume this or another job.
        int budget=ArtifactConfig.GLOBAL.get();lastAttempts=0;int turns=ORDER.size(),initial=turns,visited=0;
        while(turns-->0 && budget>0 && !ORDER.isEmpty()) {
            visited++;UUID id=ORDER.removeFirst();var job=JOBS.get(id);if(job==null) continue;
            var p=job.player;
            if(!p.isAlive() || p.isRemoved() || p.getMainHandItem()!=job.tool || job.tool.isEmpty()
                    || p.level().dimension()!=job.dimension || ArtifactState.now(p)>job.deadline) { JOBS.remove(id);if(p.isAlive()&&!p.isRemoved()){ArtifactFeedback.message(p,"cancelled");ArtifactFeedback.cue(p,"cancel");}continue; }
            if(job.paused || job.region!=null&&job.region.awaiting()){ORDER.addLast(id);continue;}
            if(job.region!=null&&!job.region.loaded(p)){job.paused=true;ArtifactFeedback.message(p,"chunk_pause");ORDER.addLast(id);continue;}
            if(job.kind==ArtifactKind.ICARUS||job.kind==ArtifactKind.WORLDBREAKER&&ArtifactState.mode(p,job.kind)==3)p.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING,40,0,false,false,true));
            int turn=Math.min(budget,EnchantmentScaling.budget(job.tool));BlockPos feedback=null;
            while(turn-->0 && (job.region!=null?!job.region.done()&&!job.region.awaiting():!job.steps.isEmpty())) {
                if(job.region!=null&&!job.region.loaded(p)){job.paused=true;break;}
                if(job.region!=null?job.region.backpressured(p):job.steps.peekFirst() instanceof WorkStep.Mine mine&&WorldSafety.dropPressure(p,mine.pos())){
                    job.paused=true;ArtifactFeedback.message(p,"drop_pause");break;
                }
                budget--;lastAttempts++;
                WorkStep step=job.region==null?job.steps.removeFirst():job.region.next(p);job.completed++;
                try {
                    running=true;boolean ok=step.apply(p,job.tool,job.kind);
                    if(ok) { feedback=step.pos();job.succeeded++; }
                    else if(step.stopOnFailure()) {job.steps.clear();job.aborted=true;break;}
                } catch(RuntimeException error) {
                    LogUtils.getLogger().error("Artifact job {} cancelled after exception",job.kind.id,error);job.steps.clear();job.aborted=true;break;
                } finally { running=false; }
                if(job.tool.isEmpty() || p.getMainHandItem()!=job.tool) { job.steps.clear();job.paused=true;break; }
            }
            if(feedback!=null&&p.tickCount%10==0)RelicEffects.emit(p,job.kind,"work",net.minecraft.world.phys.Vec3.atCenterOf(feedback));
            if(feedback!=null && p.tickCount%4==0) ArtifactFeedback.burst(p,job.kind,feedback,4);
            if(job.aborted||(job.region==null?job.steps.isEmpty():job.region.done())) {
                if(job.aborted){ArtifactFeedback.message(p,"cancelled");ArtifactFeedback.cue(p,"cancel");}
                else {ArtifactFeedback.message(p,job.succeeded==0?"nothing_changed":job.succeeded<job.completed?"partial":"complete");ArtifactFeedback.cue(p,"complete");}ArtifactInteraction.clear(p);JOBS.remove(id);
            } else ORDER.addLast(id);
        }
        if(visited==initial&&ORDER.size()>1)ORDER.addLast(ORDER.removeFirst());
    }
}
