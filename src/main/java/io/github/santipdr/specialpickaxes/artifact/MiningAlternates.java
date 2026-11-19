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
    public static final int BRIDGE_LENGTH=64;
    private MiningAlternates(){}

    public static boolean use(ServerPlayer player,ItemStack tool,ArtifactKind kind){
        var level=player.serverLevel();var actorOrigin=player.position();
        var aimed=kind==ArtifactKind.WORLDLOOM?java.util.Optional.of(player.blockPosition().below()):ArtifactActions.target(player);
        if(aimed.isEmpty())return false;
        var origin=aimed.get();
        if(!WorldSafety.allowed(player,kind,origin)||!context(player,tool,level,actorOrigin))return false;
        if(kind==ArtifactKind.HELLSPEC){
            var state=level.getBlockState(origin);
            if(!NaturalExcavationProgram.eligible(state)||PlayerPlacedBlocks.get(level).contains(origin)
                    ||!WorldSafety.harvestable(player,tool,origin))return false;
            var shape=ExcavationVolume.Shape.CHIMNEY;
            var program=new NaturalExcavationProgram(origin,player.getDirection(),shape);
            if(!WorkQueue.startRegion(player,tool,kind,program))return false;
            ArtifactFeedback.message(player,"mining_alternate_started",program.remaining());
            return true;
        }
        var steps=new ArrayList<WorkStep>();var forward=player.getDirection();
        if(kind==ArtifactKind.WORLDLOOM){
            if(player.getOffhandItem().hasTag()||!(player.getOffhandItem().getItem() instanceof BlockItem block))return false;
            var material=block.getBlock().defaultBlockState();boolean stairs=BridgeDesign.stairMaterial(material);
            if(!WorldSafety.inert(material)&&!stairs)return false;
            int slope=BridgeDesign.slope(player.getXRot(),stairs);
            if(stairs)material=material.setValue(net.minecraft.world.level.block.StairBlock.FACING,slope>0?forward:forward.getOpposite());
            var floor=player.blockPosition().below();boolean wide=player.isShiftKeyDown();
            for(int depth=1;depth<=BRIDGE_LENGTH;depth++){
                var row=BridgeDesign.row(floor,forward,depth,slope,wide);boolean clear=true;
                for(var at:row)if(!level.hasChunkAt(at)||level.isOutsideBuildHeight(at)||!level.getBlockState(at).isAir()
                        ||(stairs||wide)&&!BridgeDesign.clearance(level,at)){clear=false;break;}
                if(!clear)break;
                for(var at:row)steps.add(wide?new WorkStep.BridgePlace(at,material):new WorkStep.Place(at,material,true));
            }
        }else if(kind==ArtifactKind.AXIOM){
            if(!ArtifactOres.isOre(level.getBlockState(origin)))return false;
            return WorkQueue.startRegion(player,tool,kind,new TemporalRiftProgram(origin,kind,false));
        }else if(kind==ArtifactKind.CRUCIBLE){
            return ParadoxLattice.start(player,tool);
        }else if(kind==ArtifactKind.INTERREGNUM){
            return WorkQueue.startRegion(player,tool,kind,new StasisHourglassProgram(origin));
        }else return false;
        if(steps.isEmpty()||!WorkQueue.start(player,tool,kind,steps))return false;
        ArtifactFeedback.preview(player,kind,steps);
        ArtifactFeedback.message(player,"mining_alternate_started",steps.size());
        return true;
    }

    private static boolean context(ServerPlayer player,ItemStack tool,net.minecraft.server.level.ServerLevel level,net.minecraft.world.phys.Vec3 origin){
        return player.isAlive()&&!player.isRemoved()&&!player.isSpectator()&&player.mayBuild()&&player.serverLevel()==level
                &&player.position().equals(origin)&&player.getMainHandItem()==tool&&!tool.isEmpty();
    }

}
