package io.github.santipdr.specialpickaxes.client;

import io.github.santipdr.specialpickaxes.SpecialPickaxes;
import io.github.santipdr.specialpickaxes.artifact.StasisBoundaryGeometry;
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
    private static final net.minecraft.core.particles.DustParticleOptions BOUNDARY_GLOW=
            new net.minecraft.core.particles.DustParticleOptions(new Vector3f(.95F,.78F,.42F),.9F);
    private record Boundary(ResourceLocation dimension,BlockPos center,int radius,int halfHeight,long lastSeen,int remaining,int total){}
    private static final Map<UUID,Boundary> BOUNDARIES=new HashMap<>();
    private static long clientTicks;

    private StasisBoundaryClient(){}

    public static void receive(StasisBoundaryMessage update){
        if(!update.visible()){BOUNDARIES.remove(update.owner());return;}
        var dimension=ResourceLocation.tryParse(update.dimension());
        if(dimension==null||update.radius()<1||update.radius()>16||update.halfHeight()<1||update.halfHeight()>16)return;
        BOUNDARIES.put(update.owner(),new Boundary(dimension,BlockPos.of(update.center()),update.radius(),update.halfHeight(),clientTicks,Math.max(0,update.remainingTicks()),Math.max(1,update.totalTicks())));
        if(BOUNDARIES.size()>32){var oldest=BOUNDARIES.entrySet().stream().min(java.util.Comparator.comparingLong(entry->entry.getValue().lastSeen())).orElse(null);if(oldest!=null)BOUNDARIES.remove(oldest.getKey());}
    }

    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event){
        if(event.phase!=TickEvent.Phase.END)return;
        clientTicks++;
        var mc=Minecraft.getInstance();
        if(mc.player==null||mc.level==null){BOUNDARIES.clear();return;}
        BOUNDARIES.entrySet().removeIf(entry->clientTicks-entry.getValue().lastSeen()>45);
        // No particle emission from persistent fields. The optional volume is a line-only selection aid.
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
                // Four world-space hourglass rails shrink with server-owned remaining lifetime.
                // This is part of the boundary, not an equipment HUD or screen overlay.
                double fraction=io.github.santipdr.specialpickaxes.artifact.StasisLifetime.fraction(boundary.remaining(),boundary.total(),clientTicks-boundary.lastSeen());
                float red=fraction<.2?.98F:.42F,green=fraction<.2?.55F:.85F,blue=fraction<.2?.2F:1F;
                for(int corner=0;corner<4;corner++){
                    var top=StasisBoundaryGeometry.upperCorner(center,radius,height,corner);
                    double base=center.getY()-height,fill=base+(2*height+1)*fraction;
                    LevelRenderer.renderLineBox(pose,lines,new AABB(top.x-.09,base,top.z-.09,top.x+.09,fill,top.z+.09),red,green,blue,.95F);
                }
                // Cool cyan edges mark the actual buff volume; a small alpha wave keeps long edges legible.
                double phase=(clientTicks+event.getPartialTick())*.15D;
                float edgeAlpha=.72F+(float)(.06D*(.5D+.5D*Math.sin(phase*.35D)));
                LevelRenderer.renderLineBox(pose,lines,box,.42F,.78F,1F,edgeAlpha);
                for(int corner=0;corner<4;corner++){
                    var point=StasisBoundaryGeometry.upperCorner(center,radius,height,corner);
                    LevelRenderer.renderLineBox(pose,lines,new AABB(point.x-.055D,point.y-.08D,point.z-.055D,
                            point.x+.055D,point.y+.08D,point.z+.055D),.98F,.8F,.36F,.8F);
                }
                for(int edge=0;edge<4;edge++){
                    var point=StasisBoundaryGeometry.upperEdgeMidpoint(center,radius,height,edge);
                    LevelRenderer.renderLineBox(pose,lines,new AABB(point.x-.045D,point.y-.015D,point.z-.045D,
                            point.x+.045D,point.y+.11D,point.z+.045D),.72F,.9F,1F,.82F);
                }
                var head=StasisBoundaryGeometry.upperPerimeterPoint(center,radius,height,phase);
                double markerY=head.y+Math.sin(phase*.7D)*.045D;
                LevelRenderer.renderLineBox(pose,lines,new AABB(head.x-.07D,markerY-.08D,head.z-.07D,
                        head.x+.07D,markerY+.08D,head.z+.07D),.98F,.8F,.36F,.95F);
            }
            buffers.endBatch(RenderType.lines());
        }finally{pose.popPose();}
    }
}
