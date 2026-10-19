package io.github.santipdr.specialpickaxes.client;

import io.github.santipdr.specialpickaxes.SpecialPickaxes;
import io.github.santipdr.specialpickaxes.artifact.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.*;
import net.minecraft.world.level.Level;
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
    private static final java.util.Map<ArtifactKind,DustParticleOptions> HELD_DUST=dustPalette(.3F,.3F);
    private static final java.util.Map<ArtifactKind,DustParticleOptions> CURIOS_DUST=dustPalette(.27F,.34F);
    private record NearbyCurio(Player player,ArtifactItem relic){}
    private static ArtifactKind heldKind;
    private static Player scanOwner;
    private static Level scanLevel;
    private static int lastNearbyScanTick=Integer.MIN_VALUE;
    private static java.util.List<Player> nearbyCandidates=java.util.List.of();
    private static java.util.List<NearbyCurio> nearbyCurios=java.util.List.of();
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent e){
        if(e.phase!=TickEvent.Phase.END)return;var mc=Minecraft.getInstance();
        if(mc.player==null||mc.level==null){heldKind=null;clearNearbyCache();return;}
        if(scanOwner!=mc.player||scanLevel!=mc.level){clearNearbyCache();scanOwner=mc.player;scanLevel=mc.level;}
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

        // Aura animation still updates every four ticks; costly nearby-player and Curios
        // inventory lookups refresh every eight ticks and remain strictly client-side.
        if(CurioAuraSchedule.due(mc.player.tickCount,lastNearbyScanTick))
            refreshNearbyWearers(mc);
        int visibleHeld=0;
        for(var viewer:nearbyCandidates){
            if(!visibleToLocal(mc,viewer))continue;
            ArtifactItem heldRelic=viewer.getMainHandItem().getItem() instanceof ArtifactItem item?item:null;
            if(heldRelic!=null&&visibleHeld<MAX_VISIBLE_CURIO_AURAS){
                emitHeld(mc,viewer,heldRelic,viewer.getMainHandItem());visibleHeld++;
            }
        }
        int visibleCurios=0;
        for(var entry:nearbyCurios){
            if(!visibleToLocal(mc,entry.player()))continue;
            ArtifactItem heldRelic=entry.player().getMainHandItem().getItem() instanceof ArtifactItem item?item:null;
            if(heldRelic!=null&&heldRelic.kind==entry.relic().kind)continue;
            emitCurios(mc,entry.player(),entry.relic());
            if(++visibleCurios>=MAX_VISIBLE_CURIO_AURAS)break;
        }
    }
    private static void refreshNearbyWearers(Minecraft mc){
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
        var wearers=new java.util.ArrayList<NearbyCurio>(MAX_CURIO_CANDIDATES);
        for(var viewer:candidates){
            var equipped=CuriosCompat.find(viewer);
            if(equipped!=null&&equipped.stack().getItem() instanceof ArtifactItem item)wearers.add(new NearbyCurio(viewer,item));
        }
        nearbyCandidates=java.util.List.copyOf(candidates);
        nearbyCurios=java.util.List.copyOf(wearers);
        lastNearbyScanTick=mc.player.tickCount;
    }
    private static boolean visibleToLocal(Minecraft mc,Player viewer){
        return !viewer.isRemoved()&&!viewer.isSpectator()&&!viewer.isInvisibleTo(mc.player)
                &&viewer.distanceToSqr(mc.player)<=VISIBLE_CURIO_RADIUS_SQR;
    }
    private static void clearNearbyCache(){
        scanOwner=null;scanLevel=null;lastNearbyScanTick=Integer.MIN_VALUE;
        nearbyCandidates=java.util.List.of();nearbyCurios=java.util.List.of();
    }
    private static void emitHeld(Minecraft mc,Player player,ArtifactItem item,ItemStack tool){
        var look=player.getLookAngle();var side=look.cross(new Vec3(0,1,0)).normalize();
        var hand=player.getEyePosition().add(look.scale(0.65)).add(side.scale(0.3)).add(0,-0.3,0);
        double angle=player.tickCount*0.12+item.kind.ordinal()+ArtifactTooltips.mode(tool)*0.22;
        var dust=HELD_DUST.get(item.kind);
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
        var dust=CURIOS_DUST.get(item.kind);
        int lodeCharges=0;
        if(item.kind==ArtifactKind.IRIDIUM){
            var surge=player.getEffect(SpecialPickaxes.LODE_SURGE.get());
            if(surge!=null)lodeCharges=Math.min(3,surge.getAmplifier()+1);
        }
        ParticleOptions accent=accent(item.kind,dust);
        double y=player.getY()+Math.min(player.getBbHeight()*.58D,1.05D);
        double yaw=Math.toRadians(player.getYRot());
        int auraPoints=item.kind==ArtifactKind.ICARUS?8:4;
        for(int i=0;i<auraPoints;i++){
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
                    var point=CurioAuraGeometry.worldloomVinePoint(new Vec3(player.getX(),player.getY(),player.getZ()),phase*.38D,i);
                    x=point.x;particleY=point.y;z=point.z;
                    particle=i==Math.floorMod(player.tickCount/8,4)?ParticleTypes.CHERRY_LEAVES:dust;
                }
                case ICARUS -> {
                    var point=CurioAuraGeometry.icarusFeatherPoint(new Vec3(player.getX(),y,player.getZ()),yaw,phase,i);
                    x=point.x;particleY=point.y;z=point.z;
                    int feather=i%4;
                    particle=feather==3?ParticleTypes.END_ROD:feather==0?ParticleTypes.ENCHANT:dust;
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
    private static java.util.Map<ArtifactKind,DustParticleOptions> dustPalette(float size,float icarusSize){
        var palette=new java.util.EnumMap<ArtifactKind,DustParticleOptions>(ArtifactKind.class);
        for(var kind:ArtifactKind.values()){
            int color=kind==ArtifactKind.ICARUS?0xffe8c6:kind.color;float particleSize=kind==ArtifactKind.ICARUS?icarusSize:size;
            palette.put(kind,new DustParticleOptions(
                    new Vector3f(((color>>16)&255)/255F,((color>>8)&255)/255F,(color&255)/255F),particleSize));
        }
        return java.util.Map.copyOf(palette);
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
