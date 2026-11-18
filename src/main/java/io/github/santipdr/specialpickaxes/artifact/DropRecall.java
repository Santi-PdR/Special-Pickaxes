package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import java.util.*;

/** Finite active spell, never a passive magnet. Snapshot targets; no repeated world entity scans. */
public final class DropRecall {
    public static final int DURATION=160,PER_PLAYER=32,GLOBAL=192,MAX_TARGETS=256,MAX_JOBS=32;
    private static final Map<UUID,Recall> JOBS=new HashMap<>();
    private static final ArrayDeque<UUID> ORDER=new ArrayDeque<>();
    private static boolean ticking;
    private static final class Recall {
        final ServerPlayer player;final ServerLevel level;final ItemStack tool;final ArtifactKind kind;
        final Vec3 origin;final int radius;final long deadline;final ArrayDeque<ItemEntity> drops=new ArrayDeque<>();
        Recall(ServerPlayer player,ItemStack tool,ArtifactKind kind,List<ItemEntity> selected,int radius){
            this.player=player;this.tool=tool;this.kind=kind;this.radius=radius;level=player.serverLevel();origin=player.position();
            deadline=ArtifactState.now(player)+DURATION;
            var seen=new HashSet<UUID>();
            int inspected=0;
            for(var drop:selected){if(++inspected>MAX_TARGETS)break;if(seen.add(drop.getUUID()))drops.addLast(drop);}
        }
        boolean valid(){
            return JOBS.get(player.getUUID())==this&&player.isAlive()&&!player.isRemoved()&&!player.isSpectator()
                    &&player.mayBuild()&&player.serverLevel()==level&&player.getMainHandItem()==tool&&!tool.isEmpty()
                    &&player.position().distanceToSqr(origin)<=(double)radius*radius
                    &&ArtifactState.now(player)<=deadline;
        }
    }
    private DropRecall(){}
    public static boolean start(ServerPlayer player,ItemStack tool,ArtifactKind kind,List<ItemEntity> targets,int radius){
        if((kind!=ArtifactKind.EVENTIDE&&kind!=ArtifactKind.IRIDIUM)||radius<1||radius>64||targets.isEmpty()
                ||!player.isAlive()||player.isRemoved()||player.isSpectator()||!player.mayBuild()
                ||tool!=player.getMainHandItem()||tool.isEmpty()||!(tool.getItem() instanceof ArtifactItem item)
                ||ArtifactSkills.feedbackKind(tool,item.kind)!=kind)return false;
        if(!JOBS.containsKey(player.getUUID())&&JOBS.size()>=MAX_JOBS)return false;
        stop(player);var recall=new Recall(player,tool,kind,targets,radius);
        JOBS.put(player.getUUID(),recall);ORDER.addLast(player.getUUID());return true;
    }
    public static boolean active(ServerPlayer player){return JOBS.containsKey(player.getUUID());}
    public static int remaining(ServerPlayer player){var job=JOBS.get(player.getUUID());return job==null?0:job.drops.size();}
    public static void stop(ServerPlayer player){JOBS.remove(player.getUUID());ORDER.removeIf(player.getUUID()::equals);}
    public static void clear(){JOBS.clear();ORDER.clear();}
    public static void tick(){
        if(ticking)return;ticking=true;
        try{
            int global=GLOBAL,turns=ORDER.size();
            while(turns-->0&&global>0&&!ORDER.isEmpty()){
                var id=ORDER.removeFirst();var job=JOBS.get(id);if(job==null)continue;
                if(!job.valid()){JOBS.remove(id,job);continue;}
                int attempts=Math.min(PER_PLAYER,job.drops.size());
                while(attempts-->0&&global-->0&&job.valid()&&!job.drops.isEmpty()){
                    var drop=job.drops.removeFirst();
                    if(steer(job,drop)&&job.valid())job.drops.addLast(drop);
                }
                if(JOBS.get(id)!=job)continue;
                if(!job.valid()||job.drops.isEmpty())JOBS.remove(id,job);else ORDER.addLast(id);
            }
            // Rotate priority under saturation instead of always serving the same first players.
            if(ORDER.size()>1)ORDER.addLast(ORDER.removeFirst());
        }finally{ticking=false;}
    }
    private static boolean eligible(Recall job,ItemEntity drop){
        return drop.isAlive()&&!drop.isRemoved()&&drop.level()==job.level&&!drop.getItem().isEmpty()
                &&job.level.hasChunkAt(drop.blockPosition())&&DropOwnership.allows(job.player,drop)
                &&drop.position().distanceToSqr(job.player.position())<=(double)job.radius*job.radius
                &&(job.kind!=ArtifactKind.IRIDIUM||ArtifactTechniques.isIridiumRecallTarget(job.player,drop,job.player.position(),job.radius));
    }
    private static boolean steer(Recall job,ItemEntity drop){
        if(!eligible(job,drop))return false;
        var position=drop.position();var stack=drop.getItem();
        var context=HeldUtilityContext.capture(job.player,job.tool);
        boolean allowed=WorldSafety.allowed(job.player,job.kind,drop.blockPosition());
        if(!allowed||!context.valid()||!job.valid()||!drop.position().equals(position)||drop.getItem()!=stack||!eligible(job,drop))return false;
        var destination=job.player.position().add(0,.45,0);var delta=destination.subtract(position);
        if(delta.lengthSqr()<.45)return false; // normal pickup/collision rules keep authority
        drop.setDeltaMovement(UtilityPullMotion.velocity(delta,drop.getDeltaMovement(),.2,job.kind==ArtifactKind.IRIDIUM?.85:.65,job.kind==ArtifactKind.IRIDIUM?1.05:.8));
        drop.hasImpulse=true;return true;
    }
}
