package io.github.santipdr.specialpickaxes.client;

import io.github.santipdr.specialpickaxes.SpecialPickaxes;
import io.github.santipdr.specialpickaxes.artifact.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Continuous depth-tested wireframes. Display-only NBT; the server never reads these coordinates back. */
@Mod.EventBusSubscriber(modid=SpecialPickaxes.ID,value=Dist.CLIENT)
public final class SelectionRenderer {
    @SubscribeEvent public static void render(RenderLevelStageEvent e){
        if(e.getStage()!=RenderLevelStageEvent.Stage.AFTER_PARTICLES)return;
        var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null)return;
        var tool=mc.player.getMainHandItem();if(!(tool.getItem() instanceof ArtifactItem)||!tool.hasTag())return;
        var tag=tool.getTag();if(!tag.getString("selectionDimension").equals(mc.level.dimension().location().toString()))return;
        var pose=e.getPoseStack();var camera=e.getCamera().getPosition();pose.pushPose();pose.translate(-camera.x,-camera.y,-camera.z);
        var buffers=mc.renderBuffers().bufferSource();var lines=buffers.getBuffer(RenderType.lines());
        try{
            for(int i=0;i<4;i++)if(tag.contains("selection"+i)){
                var p=BlockPos.of(tag.getLong("selection"+i));LevelRenderer.renderLineBox(pose,lines,new AABB(p).inflate(0.005),i%2==0?0.3F:1F,i%2==0?0.95F:0.75F,0.8F,1F);
            }
            for(int i=0;i<4;i+=2)if(tag.contains("selection"+i)&&tag.contains("selection"+(i+1))){
                var v=new SelectionVolume(BlockPos.of(tag.getLong("selection"+i)),BlockPos.of(tag.getLong("selection"+(i+1))));
                if(v.size()>1048576)continue; // defensive client rendering bound, not world-edit authority
                var box=new AABB(v.min().getX(),v.min().getY(),v.min().getZ(),v.max().getX()+1D,v.max().getY()+1D,v.max().getZ()+1D);
                LevelRenderer.renderLineBox(pose,lines,box,i==0?0.25F:1F,i==0?0.9F:0.72F,i==0?1F:0.25F,0.8F);
            }
            buffers.endBatch(RenderType.lines());
        }finally{pose.popPose();}
    }
}
