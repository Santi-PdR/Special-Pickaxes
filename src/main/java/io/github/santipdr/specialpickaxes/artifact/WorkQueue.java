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
    private static final Map<UUID,FallGuard> CORE_DRILL_GUARDS=new HashMap<>();
    private static final ArrayDeque<UUID> ORDER=new ArrayDeque<>();
    private static boolean running;
    private static boolean ticking;
    private static int lastAttempts;
    private static final long CORE_DRILL_LANDING_GRACE=600;
    private static final class FallGuard {
        final ServerPlayer player;final ResourceKey<Level> dimension;long expires;
        FallGuard(ServerPlayer player,long expires){this.player=player;this.dimension=player.level().dimension();this.expires=expires;}
    }
    private static final class Job {
        final ServerPlayer player; final ItemStack tool; final ArtifactKind kind;final HeldSkillLease skill;
        final ResourceKey<Level> dimension; final long deadline; final ArrayDeque<WorkStep> steps;final long reservedBytes;
        int accepted,completed,succeeded; boolean paused,aborted; WorkProgram region;
        BlockPos lastImpact;int impactTick=Integer.MIN_VALUE;String pauseReason="";
        final ArrayDeque<BlockPos> seamTrailPoints=new ArrayDeque<>(5);
        Job(ServerPlayer p,ItemStack t,ArtifactKind k,List<WorkStep> work,long snapshotBytes) {
            player=p;tool=t;kind=k;skill=HeldSkillLease.capture(t);dimension=p.level().dimension();deadline=ArtifactState.now(p)+ArtifactConfig.JOB_TTL.get();
            steps=new ArrayDeque<>(work);accepted=work.size();this.reservedBytes=snapshotBytes;
        }
    }
    private WorkQueue() {}
    private static boolean descentProgram(WorkProgram program){
        return program instanceof WorldbreakerMantleDescent||program instanceof DirectionalProgram directional&&directional.isCoreDrill();
    }
    public static boolean running() { return running; }
    public static boolean protectsFall(ServerPlayer p){
        var guard=CORE_DRILL_GUARDS.get(p.getUUID());
        if(guard!=null&&guard.player==p&&p.isAlive()&&!p.isRemoved()&&p.level().dimension()==guard.dimension&&ArtifactState.now(p)<=guard.expires)return true;
        var job=JOBS.get(p.getUUID());return job!=null&&job.tool==p.getMainHandItem()&&(job.kind==ArtifactKind.ICARUS||job.kind==ArtifactKind.WORLDBREAKER&&ArtifactState.mode(p,job.kind)==3);
    }
    /** Remove player-bound descent state on logout, death replacement, or dimension travel. */
    public static void forget(ServerPlayer p){CORE_DRILL_GUARDS.remove(p.getUUID());}
    private static boolean validActor(Job job,ServerPlayer player){
        return job.skill.valid()&&job.player==player&&player.isAlive()&&!player.isRemoved()&&!player.isSpectator()&&player.mayBuild()
                &&player.getMainHandItem()==job.tool&&!job.tool.isEmpty()&&player.level().dimension()==job.dimension;
    }
    public static void discardStale(ServerPlayer p){var j=JOBS.get(p.getUUID());if(j!=null&&!validActor(j,p))cancel(p);}
    public static boolean busy(ServerPlayer p) { return JOBS.containsKey(p.getUUID()); }
    public static int remaining(ServerPlayer p) { var job=JOBS.get(p.getUUID());return job==null?0:job.region==null?job.steps.size():job.region.remaining(); }
    public static String status(ServerPlayer p){var j=JOBS.get(p.getUUID());return j==null?"idle":j.region!=null&&j.region.awaiting()?"ready":j.paused?"paused":j.region!=null&&!j.region.executing()?"preparing":"executing";}
    public static String pauseReason(ServerPlayer p){var job=JOBS.get(p.getUUID());return job==null||!job.paused?"":job.pauseReason;}
    public static int completed(ServerPlayer p){var j=JOBS.get(p.getUUID());return j==null?0:j.completed;}
    public static int succeeded(ServerPlayer p){var j=JOBS.get(p.getUUID());return j==null?0:j.succeeded;}
    public static boolean regionSnapshotBudgetAllows(ServerPlayer p,long snapshotBytes){
        long used=0;for(var job:JOBS.values())used+=job.reservedBytes;
        long limit=(long)ArtifactConfig.REGION_MEMORY_MIB.get()*1024*1024;
        if(snapshotBytes>limit-used){ArtifactFeedback.message(p,"region_memory_limit");return false;}
        return true;
    }
    public static boolean togglePause(ServerPlayer p){var j=JOBS.get(p.getUUID());if(j==null)return false;
        if(!validActor(j,p)){cancel(p);return false;}
        if(j.region!=null&&j.region.awaiting()){
        int cost=EnchantmentScaling.activationCost(j.tool,j.kind);if(!ArtifactActions.canSpendDurability(p,j.tool,cost))return false;
        j.tool.hurtAndBreak(cost,p,who->who.broadcastBreakEvent(net.minecraft.world.InteractionHand.MAIN_HAND));if(j.region instanceof RegionWork r&&(j.kind==ArtifactKind.CHRONICLE&&r.mode==0))ArtifactState.of(p,j.kind).remove("memory");
        ArtifactState.of(p,j.kind).putLong("ready",ArtifactState.now(p)+ArtifactConfig.COOLDOWN.get());p.getCooldowns().addCooldown(j.tool.getItem(),ArtifactConfig.COOLDOWN.get());
        j.region.confirm();j.completed=0;j.succeeded=0;j.paused=false;j.pauseReason="";
    }else if(j.paused){
        if(j.region!=null&&!j.region.loaded(p)){j.pauseReason="chunk";ArtifactFeedback.message(p,"chunk_pause");return false;}
        if(dropBackpressured(j,p)){j.pauseReason="drops";ArtifactFeedback.message(p,"drop_pause");return false;}
        if(JOBS.get(p.getUUID())!=j)return false;
        if(!validActor(j,p)){cancel(p);return false;}
        j.paused=false;j.pauseReason="";
    }else {j.paused=true;j.pauseReason="manual";}return true;}
    public static boolean startRegion(ServerPlayer p,ItemStack tool,ArtifactKind kind,WorkProgram region){
        if(busy(p)||tool.isEmpty()||JOBS.size()>=ArtifactConfig.ACTIVE_JOBS.get())return false;
        var memories=region instanceof RegionWork snapshot&&snapshot.needsRestoration()?ArtifactState.memories(p,kind):List.<ArtifactState.Memory>of();
        long snapshotBytes=region instanceof RegionWork snapshot?snapshot.reservedMemoryBytes(memories):0;
        if(!regionSnapshotBudgetAllows(p,snapshotBytes))return false;
        if(region instanceof RegionWork snapshot){snapshot.allocateSnapshots();snapshot.loadMemories(memories);}
        var j=new Job(p,tool,kind,List.of(),snapshotBytes);j.region=region;if(!(region instanceof RegionWork))region.loadMemories(p);JOBS.put(p.getUUID(),j);ORDER.addLast(p.getUUID());
        if(kind==ArtifactKind.WORLDBREAKER&&descentProgram(region))CORE_DRILL_GUARDS.put(p.getUUID(),new FallGuard(p,ArtifactState.now(p)+CORE_DRILL_LANDING_GRACE));
        return true;
    }
    public static int lastAttempts() { return lastAttempts; }
    public static boolean start(ServerPlayer p,ItemStack tool,ArtifactKind kind,List<WorkStep> steps) {
        if(busy(p) || steps.isEmpty() || tool.isEmpty() || JOBS.size()>=ArtifactConfig.ACTIVE_JOBS.get()) return false;
        var bounded=List.copyOf(steps.subList(0,Math.min(steps.size(),ArtifactConfig.JOB_LIMIT.get())));
        JOBS.put(p.getUUID(),new Job(p,tool,kind,bounded,0));ORDER.addLast(p.getUUID());return true;
    }
    public static boolean append(ServerPlayer p,ItemStack tool,ArtifactKind kind,WorkStep step) {
        var job=JOBS.get(p.getUUID());
        if(job==null) return start(p,tool,kind,List.of(step));
        if(job.kind!=kind || job.tool!=tool || job.accepted>=ArtifactConfig.JOB_LIMIT.get()) return false;
        job.steps.add(step);job.accepted++;return true;
    }
    public static void cancel(ServerPlayer p) { JOBS.remove(p.getUUID());ORDER.remove(p.getUUID()); }
    public static void clear() { JOBS.clear();CORE_DRILL_GUARDS.clear();ORDER.clear();lastAttempts=0; }
    public static void tick() {
        if(ticking)return;
        ticking=true;
        try{tickJobs();}finally{ticking=false;}
    }
    private static void tickJobs(){
        int budget=ArtifactConfig.GLOBAL.get();lastAttempts=0;int turns=ORDER.size(),initial=turns,visited=0;
        while(turns-->0 && budget>0 && !ORDER.isEmpty()) {
            visited++;UUID id=ORDER.removeFirst();var job=JOBS.get(id);if(job==null) continue;
            var p=job.player;
            if(!validActor(job,p)||ArtifactState.now(p)>job.deadline) { JOBS.remove(id);if(p.isAlive()&&!p.isRemoved()){ArtifactFeedback.message(p,"cancelled");ArtifactFeedback.cue(p,"cancel");}continue; }
            // Keep aerial mining safe even while the same active job is paused for confirmation, chunks, or drops.
            if(protectsFall(p))p.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING,40,0,false,false,true));
            if(JOBS.get(id)!=job)continue;
            if(job.paused || job.region!=null&&job.region.awaiting()){ORDER.addLast(id);continue;}
            if(job.region!=null&&!job.region.loaded(p)){
                if(JOBS.get(id)==job){job.paused=true;job.pauseReason="chunk";ArtifactFeedback.message(p,"chunk_pause");ORDER.addLast(id);}
                continue;
            }
            if(JOBS.get(id)!=job)continue;
            int playerBudget=job.region==null?EnchantmentScaling.budget(job.tool):job.region.attemptsPerTick(job.tool);
            int turn=Math.min(budget,playerBudget);BlockPos feedback=null;net.minecraft.world.level.block.state.BlockState feedbackMaterial=null;
            while(turn-->0 && JOBS.get(id)==job&&!job.paused&&(job.region!=null?!job.region.done()&&!job.region.awaiting():!job.steps.isEmpty())) {
                if(job.region!=null&&!job.region.loaded(p)){job.paused=true;job.pauseReason="chunk";break;}
                if(dropBackpressured(job,p)){
                    job.paused=true;job.pauseReason="drops";ArtifactFeedback.message(p,"drop_pause");break;
                }
                if(JOBS.get(id)!=job||job.paused)break;
                if(!validActor(job,p)){job.aborted=true;break;}
                // Backpressure permissions invoke external callbacks. A leased program may
                // become done there (e.g. its Curios source was removed), before next().
                if(job.region!=null&&job.region.done())break;
                budget--;lastAttempts++;
                WorkStep step=job.region==null?job.steps.removeFirst():job.region.next(p);
                if(JOBS.get(id)!=job)break;
                if(!validActor(job,p)){job.aborted=true;break;}
                job.completed++;
                try {
                    running=true;boolean ok=step.apply(p,job.tool,job.kind);
                    if(ok) {
                        feedback=step.pos();
                        if(job.kind==ArtifactKind.CRUCIBLE&&step instanceof WorkStep.Rephase rephase)feedbackMaterial=rephase.target();
                        if(job.kind==ArtifactKind.SEAM_RIPPER){job.seamTrailPoints.addLast(feedback.immutable());while(job.seamTrailPoints.size()>5)job.seamTrailPoints.removeFirst();}
                        job.succeeded++;
                    }
                    else if(step.stopOnFailure()) {job.steps.clear();job.aborted=true;break;}
                } catch(RuntimeException error) {
                    LogUtils.getLogger().error("Artifact job {} cancelled after exception",job.kind.id,error);job.steps.clear();job.aborted=true;break;
                } finally { running=false; }
                if(JOBS.get(id)!=job||job.paused)break;
                if(!validActor(job,p)){job.steps.clear();job.aborted=true;break;}
            }
            // A native break/claim callback may cancel or replace this exact job.
            // Never requeue it, emit stale completion, or remove the replacement.
            if(JOBS.get(id)!=job)continue;
            if(feedback!=null){
                job.lastImpact=feedback.immutable();
                int now=p.server.getTickCount();
                if(job.succeeded>0&&(job.impactTick==Integer.MIN_VALUE||now-job.impactTick>=10)){
                    job.impactTick=now;AbilityVfx.excavation(p,job.kind,feedback,false);
                }
            }
            // New signatures already emitted their authored impact above. The legacy
            // branch only feeds retired no-op emitters, but still spends CPU/budget.
            // Keep protected originals and other legacy programs on their old path.
            if(feedback!=null && !(job.region instanceof SignatureExcavation) && p.tickCount%4==0){
                if(job.kind==ArtifactKind.EXODIUM)ArtifactFeedback.starfallWork(p,feedback);
                else if(job.kind==ArtifactKind.IRIDIUM)ArtifactFeedback.iridiumLodePulse(p,feedback);
                else if(job.kind==ArtifactKind.AXIOM&&job.region instanceof RadialMiningProgram radial)ArtifactFeedback.axiomNullcutPulse(p,feedback,radial.center());
                else if(job.kind==ArtifactKind.CRUCIBLE&&feedbackMaterial!=null)ArtifactFeedback.crucibleRephase(p,feedback,feedbackMaterial);
                else {
                    if(job.kind==ArtifactKind.SEAM_RIPPER)ArtifactFeedback.seamTrail(p,List.copyOf(job.seamTrailPoints));
                    ArtifactFeedback.burst(p,job.kind,feedback,4);
                }
            }
            if(job.region!=null)job.region.reportProgress(p);
            if(JOBS.get(id)!=job)continue;
            if(job.aborted||(job.region==null?job.steps.isEmpty():job.region.done())) {
                if(!job.aborted&&job.succeeded>0&&job.lastImpact!=null)
                    AbilityVfx.excavation(p,job.kind,job.lastImpact,true);
                if(job.aborted){ArtifactFeedback.message(p,"cancelled");ArtifactFeedback.cue(p,"cancel");}
                else {boolean partial=job.region==null||job.region.reportPartial();String result=job.succeeded==0?(job.kind==ArtifactKind.CRUCIBLE?"crucible_no_change":"nothing_changed"):partial&&job.succeeded<job.completed?"partial":"complete";ArtifactFeedback.message(p,result);ArtifactFeedback.cue(p,"complete");}ArtifactInteraction.clear(p);JOBS.remove(id,job);
            } else ORDER.addLast(id);
        }
        if(visited==initial&&ORDER.size()>1)ORDER.addLast(ORDER.removeFirst());
        tickCoreDrillGuards();
    }

    private static void tickCoreDrillGuards(){
        for(var entry:new ArrayList<>(CORE_DRILL_GUARDS.entrySet())){
            var guard=entry.getValue();var p=guard.player;
            if(CORE_DRILL_GUARDS.get(entry.getKey())!=guard)continue;
            if(!p.isAlive()||p.isRemoved()||p.level().dimension()!=guard.dimension||ArtifactState.now(p)>guard.expires||p.onGround()) {CORE_DRILL_GUARDS.remove(entry.getKey(),guard);continue;}
            var job=JOBS.get(entry.getKey());
            if(job!=null&&job.kind==ArtifactKind.WORLDBREAKER&&descentProgram(job.region))guard.expires=ArtifactState.now(p)+CORE_DRILL_LANDING_GRACE;
            p.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING,40,0,false,false,true));
            if(CORE_DRILL_GUARDS.get(entry.getKey())==guard&&job==null&&p.tickCount%10==0)RelicEffects.emit(p,ArtifactKind.WORLDBREAKER,"fall_guard",p.position());
        }
    }

    private static boolean dropBackpressured(Job job,ServerPlayer player){
        if(job.region!=null)return job.region.backpressured(player);
        var next=job.steps.peekFirst();
        return next instanceof WorkStep.Mine mine
                &&(!mine.naturalOnly()||!PlayerPlacedBlocks.get(player.serverLevel()).contains(mine.pos()))
                &&WorldSafety.backpressuredMine(player,job.tool,job.kind,mine.pos(),mine.expected());
    }
}
