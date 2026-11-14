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
        var level=player.serverLevel();var actorOrigin=player.position();
        var aimed=kind==ArtifactKind.WORLDLOOM?java.util.Optional.of(player.blockPosition().below()):ArtifactActions.target(player);
        if(aimed.isEmpty())return false;
        var origin=aimed.get();
        if(!WorldSafety.allowed(player,kind,origin)||!context(player,tool,level,actorOrigin))return false;
        if(kind==ArtifactKind.PALIMPSEST)return survey(player,tool,origin);
        var steps=new ArrayList<WorkStep>();var forward=player.getDirection();var side=forward.getClockWise();
        if(kind==ArtifactKind.WORLDLOOM){
            if(player.getOffhandItem().hasTag()||!(player.getOffhandItem().getItem() instanceof BlockItem block)||!WorldSafety.inert(block.getBlock().defaultBlockState()))return false;
            var floor=player.blockPosition().below();
            for(int depth=1;depth<=9;depth++){
                var at=floor.relative(forward,depth);
                if(!level.hasChunkAt(at)||level.isOutsideBuildHeight(at)||!level.getBlockState(at).isAir())break;
                steps.add(new WorkStep.Place(at,block.getBlock().defaultBlockState(),true));
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
        if(MiningDesigns.seamGeology(state)||(ores&&ArtifactOres.isOre(state)))steps.add(new WorkStep.Mine(at.immutable(),state,true));
    }

    private static boolean context(ServerPlayer player,ItemStack tool,net.minecraft.server.level.ServerLevel level,net.minecraft.world.phys.Vec3 origin){
        return player.isAlive()&&!player.isRemoved()&&!player.isSpectator()&&player.serverLevel()==level
                &&player.position().equals(origin)&&player.getMainHandItem()==tool&&!tool.isEmpty();
    }

    /** Loaded, natural, nearest-first cells; no permission callbacks inside the collector. */
    public static java.util.List<BlockPos> surveyCandidates(ServerPlayer player,BlockPos origin){
        var level=player.serverLevel();
        var best=new NearestCandidates<BlockPos>(32,java.util.Comparator.comparingDouble((BlockPos p)->p.distSqr(origin)).thenComparingLong(BlockPos::asLong));
        for(var cursor:BlockPos.betweenClosed(origin.offset(-6,-6,-6),origin.offset(6,6,6))){
            if(level.isOutsideBuildHeight(cursor)||!level.hasChunkAt(cursor)||PlayerPlacedBlocks.get(level).contains(cursor)
                    ||!ArtifactOres.isOre(level.getBlockState(cursor)))continue;
            best.offer(cursor.immutable());
        }
        return best.sorted();
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
                level.sendParticles(player,net.minecraft.core.particles.ParticleTypes.END_ROD,true,at.getX()+.5,at.getY()+.5,at.getZ()+.5,1,0,0,0,0);
            found++;
        }
        if(nearest!=null)player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.specialpickaxes.survey_nearest",nearest.getX(),nearest.getY(),nearest.getZ()),false);
        ArtifactFeedback.message(player,"survey_found",found);return true;
    }
}
