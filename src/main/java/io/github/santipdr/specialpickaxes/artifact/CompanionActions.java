package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import java.util.*;

/** Reactive, player-owned companions: paid footing, paid wake sealing and non-destructive blast protection. */
public final class CompanionActions {
    private static final class Active {
        final ServerPlayer player;final ItemStack tool;final ArtifactKind kind;final String dimension;final long expires;final int footingY;
        final ArrayDeque<BlockPos> scars=new ArrayDeque<>();
        Active(ServerPlayer p,ItemStack tool,ArtifactKind kind){player=p;footingY=p.blockPosition().getY()-1;this.tool=tool;this.kind=kind;dimension=ArtifactState.dimension(p);expires=ArtifactState.now(p)+ArtifactConfig.FIELD_TIME.get();}
        boolean valid(){return player.isAlive()&&!player.isRemoved()&&player.getMainHandItem()==tool&&!tool.isEmpty()&&dimension.equals(ArtifactState.dimension(player))&&ArtifactState.now(player)<=expires;}
    }
    private static final Map<UUID,Active> ACTIVE=new HashMap<>();
    private CompanionActions(){}
    public static boolean handles(ArtifactKind k){return k==ArtifactKind.CAUSEWAY||k==ArtifactKind.COUNTERSEAL||k==ArtifactKind.COVENANT;}
    public static boolean active(ServerPlayer p){return ACTIVE.containsKey(p.getUUID());}
    public static boolean start(ServerPlayer p,ItemStack tool,ArtifactKind k){
        if(!handles(k)||ACTIVE.size()>=ArtifactConfig.ACTIVE_JOBS.get())return false;
        ACTIVE.put(p.getUUID(),new Active(p,tool,k));ArtifactFeedback.message(p,"companion_active");return true;
    }
    public static void stop(ServerPlayer p){ACTIVE.remove(p.getUUID());}
    public static void clear(){ACTIVE.clear();}
    public static void mined(ServerPlayer p,ArtifactKind k,BlockPos pos){
        var a=ACTIVE.get(p.getUUID());if(a!=null&&a.kind==ArtifactKind.COVENANT&&k==a.kind&&a.valid()&&a.scars.size()<128)a.scars.addLast(pos.immutable());
    }
    public static void tick(){
        var it=ACTIVE.values().iterator();
        while(it.hasNext()){
            var a=it.next();var p=a.player;
            if(!a.valid()||!WorldSafety.allowed(p,a.kind,p.blockPosition())){it.remove();continue;}
            if(p.tickCount%5!=0)continue;
            if(a.kind==ArtifactKind.CAUSEWAY&&p.getDeltaMovement().y>-.4&&p.blockPosition().getY()-1==a.footingY){
                var feet=p.blockPosition().below();var material=DirectAbilities.material(p);
                for(int n=0;n<2;n++){var at=feet.relative(p.getDirection(),n);if(p.serverLevel().hasChunkAt(at)&&p.serverLevel().getBlockState(at).isAir())WorkQueue.append(p,a.tool,a.kind,new WorkStep.Place(at,material));}
            }
            if(a.kind==ArtifactKind.COVENANT){
                for(int n=0,limit=Math.min(4,a.scars.size());n<limit;n++){
                    var at=a.scars.removeFirst();var delta=net.minecraft.world.phys.Vec3.atCenterOf(at).subtract(p.position());
                    if(delta.lengthSqr()>4&&delta.dot(p.getLookAngle())<0){
                        if(!WorkQueue.append(p,a.tool,a.kind,new WorkStep.Place(at,DirectAbilities.material(p))))a.scars.addLast(at);
                    }else a.scars.addLast(at);
                }
            }
            if(p.tickCount%10==0)ArtifactFeedback.ring(p,a.kind,p.blockPosition(),a.kind==ArtifactKind.COUNTERSEAL?Math.min(8,ArtifactConfig.MAX_RADIUS.get()):2);
        }
    }
    public static void protect(Level level,List<BlockPos> affected){
        // Bounded event work. This changes only terrain eligibility, never damage or entity ownership.
        var wards=ACTIVE.values().stream().filter(a->a.kind==ArtifactKind.COUNTERSEAL&&a.player.level()==level&&a.valid()).toList();
        if(wards.isEmpty())return;
        int checked=0,pairs=16384,permissions=1024,radius=Math.min(8,ArtifactConfig.MAX_RADIUS.get());var it=affected.iterator();
        while(it.hasNext()&&checked++<4096){
            var pos=it.next();for(var a:wards){
                if(--pairs<0)return;
                if(a.player.blockPosition().distSqr(pos)<=radius*radius){
                    if(--permissions<0)return;
                    if(WorldSafety.allowed(a.player,a.kind,pos)){it.remove();break;}
                }
            }
        }
    }
}
