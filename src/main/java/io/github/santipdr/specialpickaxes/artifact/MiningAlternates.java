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
        if(kind==ArtifactKind.PALIMPSEST)return survey(player,tool,origin);
        if(kind==ArtifactKind.CHOIR||kind==ArtifactKind.HELLSPEC){
            var state=level.getBlockState(origin);
            if(!NaturalExcavationProgram.eligible(state)||PlayerPlacedBlocks.get(level).contains(origin)
                    ||!WorldSafety.harvestable(player,tool,origin))return false;
            var shape=kind==ArtifactKind.CHOIR?ExcavationVolume.Shape.VAULT:ExcavationVolume.Shape.CHIMNEY;
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
            var floor=player.blockPosition().below();
            for(int depth=1;depth<=BRIDGE_LENGTH;depth++){
                var at=BridgeDesign.cell(floor,forward,depth,slope);
                if(!level.hasChunkAt(at)||level.isOutsideBuildHeight(at)||!level.getBlockState(at).isAir()||stairs&&!BridgeDesign.clearance(level,at))break;
                steps.add(new WorkStep.Place(at,material,true));
            }
        }else if(kind==ArtifactKind.AXIOM){
            if(!ArtifactOres.isOre(level.getBlockState(origin)))return false;
            return WorkQueue.startRegion(player,tool,kind,new TemporalRiftProgram(origin,kind,false));
        }else if(kind==ArtifactKind.CRUCIBLE){
            var state=level.getBlockState(origin);
            var materials=new net.minecraft.world.level.block.Block[]{Blocks.STONE,Blocks.DEEPSLATE,Blocks.GRANITE,Blocks.DIORITE,Blocks.ANDESITE,Blocks.DIRT,Blocks.BASALT,Blocks.OBSIDIAN,Blocks.CALCITE,Blocks.TUFF,Blocks.DRIPSTONE_BLOCK,Blocks.GRAVEL};
            var next=materials[Math.floorMod(ArtifactState.mode(player,kind),materials.length)].defaultBlockState();
            if(!WorldSafety.inert(state)||PlayerPlacedBlocks.get(level).contains(origin))return false;
            return WorkQueue.startRegion(player,tool,kind,new RadialMiningProgram(origin,12,kind,ArtifactState.mode(player,kind),next));
        }else if(kind==ArtifactKind.INTERREGNUM){
            return WorkQueue.startRegion(player,tool,kind,new TemporalRiftProgram(origin,kind,true));
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

    /** Loaded, natural, nearest-first cells; no permission callbacks inside the collector. */
    public static java.util.List<BlockPos> surveyCandidates(ServerPlayer player,BlockPos origin){
        var level=player.serverLevel();
        return SurveyVolume.select(origin,cursor->!level.isOutsideBuildHeight(cursor)&&level.hasChunkAt(cursor)
                &&!PlayerPlacedBlocks.get(level).contains(cursor)&&ArtifactOres.isOre(level.getBlockState(cursor)));
    }

    /** Information is emitted only after validating all claim callbacks and the captured miner. */
    private static boolean survey(ServerPlayer player,ItemStack tool,BlockPos origin){
        var level=player.serverLevel();var actorOrigin=player.position();var accepted=new java.util.LinkedHashMap<BlockPos,net.minecraft.world.level.block.state.BlockState>();
        for(var at:surveyCandidates(player,origin)){
            var expected=level.getBlockState(at);
            boolean allowed=WorldSafety.allowed(player,ArtifactKind.PALIMPSEST,at);
            if(!context(player,tool,level,actorOrigin))return false;
            if(allowed&&level.hasChunkAt(at)&&level.getBlockState(at)==expected&&!PlayerPlacedBlocks.get(level).contains(at))accepted.put(at,expected);
        }
        int found=0;BlockPos nearest=null;
        for(var hit:accepted.entrySet()){
            var at=hit.getKey();
            if(!level.hasChunkAt(at)||level.getBlockState(at)!=hit.getValue()||PlayerPlacedBlocks.get(level).contains(at))continue;
            if(nearest==null)nearest=at;
            if(ArtifactFeedback.reserveAuraParticles(level.getServer().getTickCount(),1,1))
                AbilityVfx.retired(level,player,net.minecraft.core.particles.ParticleTypes.END_ROD,true,at.getX()+.5,at.getY()+.5,at.getZ()+.5,1,0,0,0,0);
            found++;
        }
        if(nearest!=null)player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.specialpickaxes.survey_nearest",nearest.getX(),nearest.getY(),nearest.getZ()),false);
        ArtifactFeedback.message(player,"survey_found",found);return true;
    }
}
