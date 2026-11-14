package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Blocks;
import java.util.ArrayList;

/** Block-only alternate techniques. Never queries, damages or controls living entities/projectiles. */
public final class MiningAlternates {
    private MiningAlternates(){}

    public static boolean use(ServerPlayer player,ItemStack tool,ArtifactKind kind){
        var aimed=ArtifactActions.target(player);if(aimed.isEmpty())return false;
        var origin=aimed.get();var level=player.serverLevel();
        if(!WorldSafety.allowed(player,kind,origin)||player.serverLevel()!=level||player.getMainHandItem()!=tool||tool.isEmpty())return false;
        if(kind==ArtifactKind.PALIMPSEST)return survey(player,tool,origin);
        var steps=new ArrayList<WorkStep>();var forward=player.getDirection();var side=forward.getClockWise();
        if(kind==ArtifactKind.WORLDLOOM){
            if(!(player.getOffhandItem().getItem() instanceof BlockItem block)||!WorldSafety.inert(block.getBlock().defaultBlockState()))return false;
            var floor=player.blockPosition().below();
            for(int depth=1;depth<=9;depth++){
                var at=floor.relative(forward,depth);
                if(level.hasChunkAt(at)&&!level.isOutsideBuildHeight(at)&&level.getBlockState(at).isAir())
                    steps.add(new WorkStep.Place(at,block.getBlock().defaultBlockState()));
            }
        }else if(kind==ArtifactKind.AXIOM){
            if(!ArtifactOres.isOre(level.getBlockState(origin)))return false;
            for(var direction:Direction.values())addMine(player,steps,origin.relative(direction),false);
        }else if(kind==ArtifactKind.CRUCIBLE){
            var state=level.getBlockState(origin);
            var materials=new net.minecraft.world.level.block.Block[]{Blocks.STONE,Blocks.DEEPSLATE,Blocks.GRANITE,Blocks.DIORITE,Blocks.ANDESITE,Blocks.DIRT,Blocks.BASALT,Blocks.OBSIDIAN,Blocks.CALCITE,Blocks.TUFF,Blocks.DRIPSTONE_BLOCK,Blocks.GRAVEL};
            var next=materials[Math.floorMod(ArtifactState.mode(player,kind),materials.length)].defaultBlockState();
            if(state!=next&&WorldSafety.inert(state)&&!PlayerPlacedBlocks.get(level).contains(origin))steps.add(new WorkStep.Rephase(origin,state,next));
        }else if(kind==ArtifactKind.INTERREGNUM){
            addMine(player,steps,origin,true);
        }else if(kind==ArtifactKind.CHOIR){
            for(int depth=0;depth<8;depth++)for(int width=-1;width<=1;width++)for(int height=0;height<3;height++){
                if(height==2&&width!=0)continue;
                addMine(player,steps,origin.relative(forward,depth).relative(side,width).above(height),true);
            }
        }else if(kind==ArtifactKind.HELLSPEC){
            for(int height=0;height<12;height++)for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)
                addMine(player,steps,origin.offset(x,height,z),true);
        }else return false;
        if(steps.isEmpty()||!WorkQueue.start(player,tool,kind,steps))return false;
        ArtifactFeedback.preview(player,kind,steps);
        ArtifactFeedback.message(player,"mining_alternate_started",steps.size());
        return true;
    }

    private static void addMine(ServerPlayer player,ArrayList<WorkStep> steps,BlockPos at,boolean ores){
        var level=player.serverLevel();
        if(level.isOutsideBuildHeight(at)||!level.hasChunkAt(at)||PlayerPlacedBlocks.get(level).contains(at))return;
        var state=level.getBlockState(at);
        if(MiningDesigns.seamGeology(state)||(ores&&ArtifactOres.isOre(state)))steps.add(new WorkStep.Mine(at.immutable(),state));
    }

    /** Reveals at most 32 loaded ore cells to this miner; no drops or world mutation. */
    private static boolean survey(ServerPlayer player,ItemStack tool,BlockPos origin){
        var level=player.serverLevel();int found=0;
        for(var cursor:BlockPos.betweenClosed(origin.offset(-6,-6,-6),origin.offset(6,6,6))){
            if(level.isOutsideBuildHeight(cursor)||!level.hasChunkAt(cursor)||!ArtifactOres.isOre(level.getBlockState(cursor)))continue;
            var at=cursor.immutable();
            if(!WorldSafety.allowed(player,ArtifactKind.PALIMPSEST,at)||player.serverLevel()!=level||player.getMainHandItem()!=tool||tool.isEmpty())return false;
            if(!ArtifactOres.isOre(level.getBlockState(at)))continue;
            if(ArtifactFeedback.reserveAuraParticles(level.getServer().getTickCount(),1,1))
                level.sendParticles(player,net.minecraft.core.particles.ParticleTypes.END_ROD,true,at.getX()+.5,at.getY()+.5,at.getZ()+.5,1,0,0,0,0);
            if(++found==32)break;
        }
        ArtifactFeedback.message(player,"survey_found",found);return true;
    }
}
