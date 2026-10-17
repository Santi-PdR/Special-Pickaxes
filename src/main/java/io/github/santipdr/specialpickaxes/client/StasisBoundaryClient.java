package io.github.santipdr.specialpickaxes.client;

import io.github.santipdr.specialpickaxes.SpecialPickaxes;
import io.github.santipdr.specialpickaxes.artifact.ArtifactFeedback;
import io.github.santipdr.specialpickaxes.network.StasisBoundaryMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Locally rendered Stasis domains: one small sync packet replaces recurring boundary particle packets. */
@Mod.EventBusSubscriber(modid=SpecialPickaxes.ID,value=Dist.CLIENT)
public final class StasisBoundaryClient {
    private record Boundary(ResourceLocation dimension,BlockPos center,int radius,int halfHeight,long lastSeen){}
    private static final Map<UUID,Boundary> BOUNDARIES=new HashMap<>();
    private static long clientTicks;

    private StasisBoundaryClient(){}

    public static void receive(StasisBoundaryMessage update){
        if(!update.visible()){BOUNDARIES.remove(update.owner());return;}
        var dimension=ResourceLocation.tryParse(update.dimension());
        if(dimension==null||update.radius()<1||update.radius()>16||update.halfHeight()<1||update.halfHeight()>16)return;
        BOUNDARIES.put(update.owner(),new Boundary(dimension,BlockPos.of(update.center()),update.radius(),update.halfHeight(),clientTicks));
        if(BOUNDARIES.size()>32){var oldest=BOUNDARIES.entrySet().stream().min(java.util.Comparator.comparingLong(entry->entry.getValue().lastSeen())).orElse(null);if(oldest!=null)BOUNDARIES.remove(oldest.getKey());}
    }

    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event){
        if(event.phase!=TickEvent.Phase.END)return;
        clientTicks++;
        var mc=Minecraft.getInstance();
        if(mc.player==null||mc.level==null){BOUNDARIES.clear();return;}
        BOUNDARIES.entrySet().removeIf(entry->clientTicks-entry.getValue().lastSeen()>45);
        if(mc.isPaused()||clientTicks%4!=0)return;
        for(var boundary:BOUNDARIES.values()){
            if(!boundary.dimension().equals(mc.level.dimension().location()))continue;
            var center=boundary.center();int radius=boundary.radius();
            if(mc.player.distanceToSqr(center.getX()+.5D,center.getY()+.5D,center.getZ()+.5D)>(48D+radius)*(48D+radius))continue;
            double phase=clientTicks*.15D;
            var head=ArtifactFeedback.stasisBoundaryPoint(center,radius,phase);
            double bob=Math.sin(phase*.7D)*.12D;
            var glow=new net.minecraft.core.particles.DustParticleOptions(new Vector3f(.95F,.78F,.42F),.9F);
            mc.level.addParticle(net.minecraft.core.particles.ParticleTypes.END_ROD,head.x,head.y+.72D+bob,head.z,0,.015D,0);
            mc.level.addParticle(glow,head.x,head.y+.28D+bob,head.z,0,.005D,0);
        }
    }

    public static void render(RenderLevelStageEvent event,Minecraft mc){
        if(BOUNDARIES.isEmpty()||mc.player==null||mc.level==null)return;
        var pose=event.getPoseStack();var camera=event.getCamera().getPosition();pose.pushPose();pose.translate(-camera.x,-camera.y,-camera.z);
        var buffers=mc.renderBuffers().bufferSource();var lines=buffers.getBuffer(RenderType.lines());
        try{
            for(var boundary:BOUNDARIES.values()){
                if(!boundary.dimension().equals(mc.level.dimension().location()))continue;
                var center=boundary.center();int radius=boundary.radius(),height=boundary.halfHeight();
                if(mc.player.distanceToSqr(center.getX()+.5D,center.getY()+.5D,center.getZ()+.5D)>(48D+radius)*(48D+radius))continue;
                var box=new AABB(center.getX()-radius,center.getY()-height,center.getZ()-radius,
                        center.getX()+radius+1D,center.getY()+height+1D,center.getZ()+radius+1D);
                // Cool cyan edges mark the actual buff volume; a moving gold bead shows the field is alive.
                LevelRenderer.renderLineBox(pose,lines,box,.42F,.78F,1F,.62F);
                double phase=(clientTicks+event.getPartialTick())*.15D;
                var head=ArtifactFeedback.stasisBoundaryPoint(center,radius,phase);
                double markerY=center.getY()-height+.08D+Math.sin(phase*.7D)*.12D;
                LevelRenderer.renderLineBox(pose,lines,new AABB(head.x-.07D,markerY,head.z-.07D,
                        head.x+.07D,markerY+.16D,head.z+.07D),.98F,.8F,.36F,.95F);
            }
            buffers.endBatch(RenderType.lines());
        }finally{pose.popPose();}
    }
}
