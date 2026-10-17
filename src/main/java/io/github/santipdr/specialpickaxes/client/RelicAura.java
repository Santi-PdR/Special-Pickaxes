package io.github.santipdr.specialpickaxes.client;

import io.github.santipdr.specialpickaxes.SpecialPickaxes;
import io.github.santipdr.specialpickaxes.artifact.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;

/** Cosmetic-only hand and Curios signatures. Never sends gameplay packets. */
@Mod.EventBusSubscriber(modid=SpecialPickaxes.ID,value=Dist.CLIENT)
public final class RelicAura {
    private static final int MAX_VISIBLE_CURIO_AURAS=6;
    private static final int MAX_CURIO_CANDIDATES=12;
    private static final int MAX_INSPECTED_NEARBY_PLAYERS=128;
    private static final double VISIBLE_CURIO_RADIUS_SQR=24D*24D;
    private static ArtifactKind heldKind;
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent e){
        if(e.phase!=TickEvent.Phase.END)return;var mc=Minecraft.getInstance();
        if(mc.player==null||mc.level==null){heldKind=null;return;}
        if(mc.isPaused()||mc.options.hideGui||mc.player.tickCount%4!=0)return;
        ArtifactItem held=mc.player.getMainHandItem().getItem() instanceof ArtifactItem item?item:null;
        if(held!=null){
            if(heldKind!=held.kind){heldKind=held.kind;
                var action=RelicControl.Action.ACTIVATE;
                mc.player.displayClientMessage(mc.player.getMainHandItem().getHoverName().copy().append(" · ").append(RelicKeys.name(action)).append(" → ").append(net.minecraft.network.chat.Component.translatable("key.specialpickaxes."+action.name().toLowerCase(java.util.Locale.ROOT))),true);
            }
            emitHeld(mc,mc.player,held,mc.player.getMainHandItem());
        } else heldKind=null;
        var curio=CuriosCompat.find(mc.player);
        if(curio!=null&&curio.stack().getItem() instanceof ArtifactItem item
                &&(held==null||held.kind!=item.kind))emitCurios(mc,mc.player,item);

        // Render nearby wearers on this client too, so a Curios passive has a readable
        // team-facing signature without introducing server packets or an unbounded scan.
        var farthestFirst=java.util.Comparator.<Player>comparingDouble(viewer->viewer.distanceToSqr(mc.player)).reversed();
        var nearest=new java.util.PriorityQueue<Player>(MAX_CURIO_CANDIDATES,farthestFirst);
        var nearbyPlayers=new java.util.ArrayList<Player>(MAX_INSPECTED_NEARBY_PLAYERS);
        mc.level.getEntities(net.minecraft.world.level.entity.EntityTypeTest.forClass(Player.class),
                mc.player.getBoundingBox().inflate(24D),viewer->viewer!=mc.player&&!viewer.isSpectator()
                        &&!viewer.isInvisibleTo(mc.player)&&viewer.distanceToSqr(mc.player)<=VISIBLE_CURIO_RADIUS_SQR,
                nearbyPlayers,MAX_INSPECTED_NEARBY_PLAYERS);
        for(var viewer:nearbyPlayers){
            if(nearest.size()<MAX_CURIO_CANDIDATES)nearest.add(viewer);
            else if(viewer.distanceToSqr(mc.player)<nearest.peek().distanceToSqr(mc.player)){
                nearest.poll();nearest.add(viewer);
            }
        }
        var candidates=new java.util.ArrayList<Player>(nearest);
        candidates.sort(java.util.Comparator.comparingDouble(viewer->viewer.distanceToSqr(mc.player)));
        var nearby=new java.util.ArrayList<java.util.Map.Entry<Player,ArtifactItem>>();
        int visibleHeld=0;
        for(int i=0;i<Math.min(MAX_CURIO_CANDIDATES,candidates.size());i++){
            var viewer=candidates.get(i);
            ArtifactItem heldRelic=viewer.getMainHandItem().getItem() instanceof ArtifactItem item?item:null;
            if(heldRelic!=null&&visibleHeld<MAX_VISIBLE_CURIO_AURAS){
                emitHeld(mc,viewer,heldRelic,viewer.getMainHandItem());visibleHeld++;
            }
            var equipped=CuriosCompat.find(viewer);
            if(equipped==null||!(equipped.stack().getItem() instanceof ArtifactItem item))continue;
            if(heldRelic!=null&&heldRelic.kind==item.kind)continue;
            nearby.add(java.util.Map.entry(viewer,item));
        }
        nearby.sort(java.util.Comparator.comparingDouble(entry->entry.getKey().distanceToSqr(mc.player)));
        for(int i=0;i<Math.min(MAX_VISIBLE_CURIO_AURAS,nearby.size());i++)
            emitCurios(mc,nearby.get(i).getKey(),nearby.get(i).getValue());
    }
    private static void emitHeld(Minecraft mc,Player player,ArtifactItem item,ItemStack tool){
        var look=player.getLookAngle();var side=look.cross(new Vec3(0,1,0)).normalize();
        var hand=player.getEyePosition().add(look.scale(0.65)).add(side.scale(0.3)).add(0,-0.3,0);
        double angle=player.tickCount*0.12+item.kind.ordinal()+ArtifactTooltips.mode(tool)*0.22;
        var dust=dust(item.kind,0.3F);
        ParticleOptions accent=accent(item.kind,dust);
        for(int i=0;i<3;i++){
            double orbit=angle+i*Math.PI*2/3,radius=0.055+i*0.022;
            var at=hand.add(side.scale(Math.cos(orbit)*radius)).add(0,Math.sin(orbit)*0.075,0).add(look.scale(Math.sin(orbit)*radius));
            mc.level.addParticle(dust,at.x,at.y,at.z,0,0.008,0);
            if(i==0)mc.level.addParticle(accent,at.x,at.y,at.z,0,0.006,0);
        }
    }
    /** A restrained rotating belt halo makes the Curios passive visible without suggesting an active skill. */
    private static void emitCurios(Minecraft mc,Player player,ArtifactItem item){
        double phase=player.tickCount*0.075D+item.kind.ordinal()*0.61D;
        var dust=dust(item.kind,item.kind==ArtifactKind.ICARUS?0.34F:0.27F);
        int lodeCharges=0;
        if(item.kind==ArtifactKind.IRIDIUM){
            var surge=player.getEffect(SpecialPickaxes.LODE_SURGE.get());
            if(surge!=null)lodeCharges=Math.min(3,surge.getAmplifier()+1);
        }
        ParticleOptions accent=accent(item.kind,dust);
        double y=player.getY()+Math.min(player.getBbHeight()*.58D,1.05D);
        double yaw=Math.toRadians(player.getYRot());
        var back=new Vec3(Math.sin(yaw),0,-Math.cos(yaw));
        var side=new Vec3(Math.cos(yaw),0,Math.sin(yaw));
        for(int i=0;i<4;i++){
            double angle=phase+i*Math.PI/2D,radius=.42D+Math.sin(phase+i*.7D)*.035D;
            double x=player.getX()+Math.cos(angle)*radius,z=player.getZ()+Math.sin(angle)*radius;
            double particleY=y+Math.sin(angle*2D)*.06D;
            ParticleOptions particle=i==0?accent:dust;
            switch(item.kind){
                case PALIMPSEST -> {
                    angle=phase*.72D+i*Math.PI/2D;radius=.25D+i*.035D;
                    x=player.getX()+Math.cos(angle)*radius;z=player.getZ()+Math.sin(angle)*radius;
                    particleY=y-.2D+i*.13D;particle=i==2?ParticleTypes.ENCHANT:dust;
                }
                case CHOIR -> {
                    radius=.28D+(.12D+.03D*Math.sin(phase))*(i/2);
                    angle=phase*.18D+i*Math.PI/2D;x=player.getX()+Math.cos(angle)*radius;z=player.getZ()+Math.sin(angle)*radius;
                    particleY=y-.08D+(i%2)*.18D;particle=i==0?ParticleTypes.SCULK_CHARGE_POP:dust;
                }
                case EVENTIDE -> {
                    angle=-phase*.55D+i*Math.PI/2D;radius=.34D+(i%2)*.13D;
                    x=player.getX()+Math.cos(angle)*radius;z=player.getZ()+Math.sin(angle)*radius;
                    particleY=y-.22D+i*.12D;particle=i==0?ParticleTypes.REVERSE_PORTAL:dust;
                }
                case CRUCIBLE -> {
                    angle=phase*.28D+i*Math.PI/2D;radius=.31D;
                    x=player.getX()+Math.cos(angle)*radius;z=player.getZ()+Math.sin(angle)*radius;
                    particleY=y-.12D+(i%2)*.28D;particle=i%2==0?ParticleTypes.SMALL_FLAME:dust;
                }
                case INTERREGNUM -> {
                    double spin=phase*.12D,corner=(i%2==0?-.42D:.42D);
                    double cx=corner,cz=(i<2?-.42D:.42D);
                    x=player.getX()+cx*Math.cos(spin)-cz*Math.sin(spin);
                    z=player.getZ()+cx*Math.sin(spin)+cz*Math.cos(spin);
                    particleY=y+.08D+Math.sin(phase*.3D+i)*.055D;particle=i==Math.floorMod(player.tickCount/4,4)?ParticleTypes.END_ROD:dust;
                }
                case WORLDLOOM -> {
                    angle=phase*.38D+i*Math.PI/2D;radius=.38D;
                    x=player.getX()+Math.cos(angle)*radius;z=player.getZ()+Math.sin(angle)*radius;
                    particleY=player.getY()+.12D+(i%2)*.12D;particle=i==0?ParticleTypes.HAPPY_VILLAGER:dust;
                }
                case ICARUS -> {
                    int wing=i<2?-1:1;double feather=(i%2)*.22D+.24D;
                    double spread=.20D+(i%2)*.22D;
                    x=player.getX()+back.x*.24D+side.x*wing*spread;
                    z=player.getZ()+back.z*.24D+side.z*wing*spread;
                    particleY=y-.04D+feather+Math.sin(phase+i*.8D)*.035D;particle=i%2==0?ParticleTypes.ENCHANT:dust;
                }
                case AXIOM -> {
                    angle=-phase*.62D+i*Math.PI/2D;radius=.26D+(i%2)*.15D;
                    x=player.getX()+Math.cos(angle)*radius;z=player.getZ()+Math.sin(angle)*radius;
                    particleY=y-.12D+(i%2)*.24D;particle=i==0?ParticleTypes.SCULK_SOUL:dust;
                }
                case EXODIUM -> {
                    angle=phase*.46D+i*Math.PI/2D;radius=.18D+(i%2)*.25D;
                    x=player.getX()+Math.cos(angle)*radius;z=player.getZ()+Math.sin(angle)*radius;
                    particleY=y-.18D+(i%2)*.34D;particle=i==0?ParticleTypes.END_ROD:dust;
                }
                case IRIDIUM -> {
                    angle=phase*.3D+i*Math.PI/2D;radius=.4D+(i<lodeCharges ? .08D : 0D);
                    x=player.getX()+Math.cos(angle)*radius;z=player.getZ()+Math.sin(angle)*radius;
                    particleY=y-.1D+(i%2)*.2D;particle=i==3?ParticleTypes.END_ROD:(i<lodeCharges?ParticleTypes.GLOW:dust);
                }
                case WORLDBREAKER -> {
                    angle=phase*.36D+i*Math.PI/2D;radius=.31D+.05D*Math.sin(phase+i);
                    x=player.getX()+Math.cos(angle)*radius;z=player.getZ()+Math.sin(angle)*radius;
                    particleY=y-.16D+(i%2)*.32D;particle=i==0?ParticleTypes.FIREWORK:dust;
                }
                case HELLSPEC -> {
                    angle=phase*.5D+i*Math.PI/2D;radius=.28D+(i%2)*.12D;
                    x=player.getX()+Math.cos(angle)*radius;z=player.getZ()+Math.sin(angle)*radius;
                    particleY=y-.16D+(i%2)*.28D;particle=i%2==0?ParticleTypes.FLAME:dust;
                }
                case SEAM_RIPPER -> {
                    angle=phase*.22D;radius=.13D+i*.105D;
                    x=player.getX()+Math.cos(angle)*radius;z=player.getZ()+Math.sin(angle)*radius;
                    particleY=y-.14D+i*.09D;particle=i==3?ParticleTypes.GLOW:dust;
                }
                default -> { }
            }
            mc.level.addParticle(particle,x,particleY,z,0,.004D,0);
        }
    }
    private static DustParticleOptions dust(ArtifactKind kind,float size){
        int color=kind==ArtifactKind.ICARUS?0xffe8c6:kind.color;
        return new DustParticleOptions(new Vector3f(((color>>16)&255)/255F,((color>>8)&255)/255F,(color&255)/255F),size);
    }
    private static ParticleOptions accent(ArtifactKind kind,ParticleOptions fallback){
        return switch(kind){
            case PALIMPSEST->ParticleTypes.ENCHANT;case CHOIR->ParticleTypes.SCULK_CHARGE_POP;
            case EVENTIDE->ParticleTypes.REVERSE_PORTAL;case CRUCIBLE->ParticleTypes.SMALL_FLAME;
            case INTERREGNUM->ParticleTypes.END_ROD;case WORLDLOOM->ParticleTypes.HAPPY_VILLAGER;
            case ICARUS->ParticleTypes.ENCHANT;case AXIOM->ParticleTypes.SCULK_SOUL;
            case WORLDBREAKER->ParticleTypes.FIREWORK;
            case EXODIUM->ParticleTypes.END_ROD;case IRIDIUM->ParticleTypes.GLOW;case HELLSPEC->ParticleTypes.FLAME;
            default->fallback;
        };
    }
}
