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
        int accepted;
        Job(ServerPlayer p,ItemStack t,ArtifactKind k,List<WorkStep> work) {
            player=p;tool=t;kind=k;dimension=p.level().dimension();deadline=ArtifactState.now(p)+ArtifactConfig.JOB_TTL.get();
            steps=new ArrayDeque<>(work);accepted=work.size();
        }
    }
    private WorkQueue() {}
    public static boolean running() { return running; }
    public static boolean busy(ServerPlayer p) { return JOBS.containsKey(p.getUUID()); }
    public static int remaining(ServerPlayer p) { var job=JOBS.get(p.getUUID());return job==null?0:job.steps.size(); }
    public static int lastAttempts() { return lastAttempts; }
    public static boolean start(ServerPlayer p,ItemStack tool,ArtifactKind kind,List<WorkStep> steps) {
        if(busy(p) || steps.isEmpty() || tool.isEmpty()) return false;
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
        int budget=ArtifactConfig.GLOBAL.get();lastAttempts=0;int turns=ORDER.size();
        while(turns-->0 && budget>0 && !ORDER.isEmpty()) {
            UUID id=ORDER.removeFirst();var job=JOBS.get(id);if(job==null) continue;
            var p=job.player;
            if(!p.isAlive() || p.isRemoved() || p.getMainHandItem()!=job.tool || job.tool.isEmpty()
                    || p.level().dimension()!=job.dimension || ArtifactState.now(p)>job.deadline) { JOBS.remove(id);continue; }
            if(job.kind==ArtifactKind.ICARUS) p.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING,10,0,false,false,true));
            int turn=Math.min(budget,ArtifactConfig.PER_PLAYER.get());BlockPos feedback=null;
            while(turn-->0 && !job.steps.isEmpty()) {
                WorkStep step=job.steps.removeFirst();budget--;lastAttempts++;
                try {
                    running=true;boolean ok=step.apply(p,job.tool,job.kind);
                    if(ok) feedback=step.pos();
                    else if(step.stopOnFailure()) job.steps.clear();
                } catch(RuntimeException error) {
                    LogUtils.getLogger().error("Artifact job {} cancelled after exception",job.kind.id,error);job.steps.clear();
                } finally { running=false; }
                if(job.tool.isEmpty() || p.getMainHandItem()!=job.tool) job.steps.clear();
            }
            if(feedback!=null && p.tickCount%4==0) ArtifactFeedback.burst(p,job.kind,feedback,4);
            if(job.steps.isEmpty()) JOBS.remove(id);else ORDER.addLast(id);
        }
    }
}
