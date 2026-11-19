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
        if(kind==ArtifactKind.WORLDLOOM)return WorldloomHarvest.start(player,tool);
        var level=player.serverLevel();var actorOrigin=player.position();
        var aimed=ArtifactActions.target(player);
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
        if(kind==ArtifactKind.AXIOM){
            if(!ArtifactOres.isOre(level.getBlockState(origin)))return false;
            return WorkQueue.startRegion(player,tool,kind,new TemporalRiftProgram(origin,kind,false));
        }else if(kind==ArtifactKind.CRUCIBLE){
            return ParadoxLattice.start(player,tool);
        }else if(kind==ArtifactKind.INTERREGNUM){
            return WorkQueue.startRegion(player,tool,kind,new StasisHourglassProgram(origin));
        }else return false;

    }

    private static boolean context(ServerPlayer player,ItemStack tool,net.minecraft.server.level.ServerLevel level,net.minecraft.world.phys.Vec3 origin){
        return player.isAlive()&&!player.isRemoved()&&!player.isSpectator()&&player.mayBuild()&&player.serverLevel()==level
                &&player.position().equals(origin)&&player.getMainHandItem()==tool&&!tool.isEmpty();
    }

}
