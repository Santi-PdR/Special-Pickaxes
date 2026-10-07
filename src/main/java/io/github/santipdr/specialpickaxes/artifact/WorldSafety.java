package io.github.santipdr.specialpickaxes.artifact;

import io.github.santipdr.specialpickaxes.ability.AbilityUseEvent;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.minecraftforge.common.*;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;
import java.util.Set;

/** Conservative mutation boundary. No inventories, falling blocks, machinery, arbitrary mod callbacks or ores in swaps. */
public final class WorldSafety {
    private static final Set<Block> MATTER=Set.of(Blocks.STONE,Blocks.COBBLESTONE,Blocks.DEEPSLATE,
        Blocks.COBBLED_DEEPSLATE,Blocks.GRANITE,Blocks.DIORITE,Blocks.ANDESITE,Blocks.TUFF,
        Blocks.STONE_BRICKS,Blocks.BRICKS,Blocks.DEEPSLATE_BRICKS,Blocks.DEEPSLATE_TILES,Blocks.BLACKSTONE,Blocks.POLISHED_BLACKSTONE,
        Blocks.QUARTZ_BLOCK,Blocks.SMOOTH_QUARTZ,Blocks.TERRACOTTA,Blocks.GLASS,Blocks.OAK_PLANKS,Blocks.SPRUCE_PLANKS,Blocks.BIRCH_PLANKS,
        Blocks.JUNGLE_PLANKS,Blocks.ACACIA_PLANKS,Blocks.DARK_OAK_PLANKS,Blocks.MANGROVE_PLANKS,Blocks.CHERRY_PLANKS,
        Blocks.CALCITE,Blocks.BASALT,Blocks.SMOOTH_BASALT,Blocks.OBSIDIAN,Blocks.END_STONE,Blocks.NETHERRACK);
    private WorldSafety() {}
    private record NativeBreak(ServerPlayer player,ItemStack tool,BlockPos pos,BlockState expected){}
    private static final ThreadLocal<NativeBreak> NATIVE_BREAK=new ThreadLocal<>();
    public static boolean changedDuringBreakEvent(ServerPlayer p,BlockPos pos){
        var context=NATIVE_BREAK.get();return context!=null&&context.player()==p&&context.pos().equals(pos)
            &&(p.getMainHandItem()!=context.tool()||p.serverLevel().getBlockState(pos)!=context.expected()||barrier(p,pos));
    }
    public static boolean inert(BlockState state) { return !state.is(Blocks.BEDROCK)&&state.getFluidState().isEmpty()&&MATTER.contains(state.getBlock()) && state==state.getBlock().defaultBlockState(); }
    public static boolean allowed(ServerPlayer p, ArtifactKind kind, BlockPos pos) {
        var level=p.serverLevel();
        return p.isAlive() && !p.isSpectator() && p.mayBuild() && !level.isOutsideBuildHeight(pos)
            && p.distanceToSqr(Vec3.atCenterOf(pos))<=Math.pow(ArtifactConfig.RANGE.get(),2)
            && level.hasChunkAt(pos) && level.getWorldBorder().isWithinBounds(pos) && level.mayInteract(p,pos)
            && !MinecraftForge.EVENT_BUS.post(new AbilityUseEvent(p,kind.id,pos));
    }
    /** Physical barriers: query the level Fluid API as well as the block state. */
    public static boolean barrier(ServerPlayer p,BlockPos pos){
        var level=p.serverLevel();if(!level.hasChunkAt(pos)||level.isOutsideBuildHeight(pos))return true;
        var state=level.getBlockState(pos);
        return state.is(Blocks.BEDROCK)||!level.getFluidState(pos).isEmpty()||!state.getFluidState().isEmpty()||state.hasBlockEntity()||state.getDestroySpeed(level,pos)<0;
    }
    public static boolean directionClear(ServerPlayer p,ItemStack tool,ArtifactKind kind,BlockPos pos){
        return allowed(p,kind,pos)&&!barrier(p,pos)&&(p.serverLevel().getBlockState(pos).isAir()||harvestable(p,tool,pos));
    }
    public static boolean harvestable(ServerPlayer p, ItemStack tool, BlockPos pos) {
        if(barrier(p,pos))return false;
        var level=p.serverLevel();var s=level.getBlockState(pos);
        return !barrier(p,pos)&&!tool.isEmpty() && p.getMainHandItem()==tool && !s.isAir() && !s.hasBlockEntity()
            && s.getFluidState().isEmpty() && s.getDestroySpeed(level,pos)>=0
            && ArtifactTools.effective(s) && tool.isCorrectToolForDrops(s);
    }
    public static boolean mine(ServerPlayer p,ItemStack tool,ArtifactKind kind,BlockPos pos,BlockState expected) {
        if(!allowed(p,kind,pos) || p.serverLevel().getBlockState(pos)!=expected || !harvestable(p,tool,pos)) return false;
        // Backpressure: do not destroy another block into a dense pile of uncollected drops.
        if(dropPressure(p,pos))return false;
        var previous=NATIVE_BREAK.get();NATIVE_BREAK.set(new NativeBreak(p,tool,pos.immutable(),expected));
        try{return p.gameMode.destroyBlock(pos);}finally{if(previous==null)NATIVE_BREAK.remove();else NATIVE_BREAK.set(previous);}
    }
    public static boolean dropPressure(ServerPlayer p,BlockPos pos){return p.serverLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new AABB(pos).inflate(8)).size()>=256;}
    private static boolean breakPermission(ServerPlayer p,BlockPos pos) {
        return ForgeHooks.onBlockBreakEvent(p.serverLevel(),p.gameMode.getGameModeForPlayer(),p,pos)>=0;
    }
    private static boolean emptyForPlacement(ServerPlayer p,BlockPos pos,BlockState state) {
        return p.serverLevel().getBlockState(pos).isAir() && state.canSurvive(p.serverLevel(),pos)
            && p.serverLevel().isUnobstructed(state,pos,net.minecraft.world.phys.shapes.CollisionContext.empty());
    }
    private static void restoreOwned(net.minecraft.server.level.ServerLevel level,BlockSnapshot snapshot,BlockPos pos,BlockState placed){
        // Roll back our tentative write, never a fluid/bedrock/machine installed by
        // another listener. Unconditional rollback would destructively overwrite it.
        if(level.hasChunkAt(pos)&&level.getBlockState(pos)==placed&&level.getFluidState(pos).isEmpty()&&placed.getFluidState().isEmpty())snapshot.restore(true,false);
    }
    public static boolean placePaid(ServerPlayer p,ItemStack tool,ArtifactKind kind,BlockPos pos,BlockState state) {
        if(!allowed(p,kind,pos) || barrier(p,pos) || !inert(state) || !emptyForPlacement(p,pos,state) || tool.isEmpty()) return false;
        int paymentSlot=-1;
        if(!p.isCreative()) {
            for(int i=0;i<p.getInventory().getContainerSize();i++) {
                var stack=p.getInventory().getItem(i);
                if(stack.is(state.getBlock().asItem()) && !stack.hasTag()) { paymentSlot=i;break; }
            }
            if(paymentSlot<0) return false;
        }
        var payment=paymentSlot<0?ItemStack.EMPTY:p.getInventory().getItem(paymentSlot);
        var level=p.serverLevel();var snapshot=BlockSnapshot.create(level.dimension(),level,pos);
        boolean accepted=false;
        try {
            if(!level.setBlock(pos,state,2) || ForgeEventFactory.onBlockPlace(p,snapshot,Direction.UP)
                    || level.getBlockState(pos)!=state || p.getMainHandItem()!=tool || tool.isEmpty()) return false;
            if(!p.isCreative()) {
                // A listener may replace/remove the payment stack. Never place for free after such a change.
                if(p.getInventory().getItem(paymentSlot)!=payment || payment.isEmpty()
                        || !payment.is(state.getBlock().asItem()) || payment.hasTag()) return false;
                payment.shrink(1);
            }
            accepted=true;return true;
        } finally {
            if(!accepted) restoreOwned(level,snapshot,pos,state);
            else level.blockUpdated(pos,state.getBlock());
        }
    }
    public static boolean transmute(ServerPlayer p,ItemStack tool,ArtifactKind kind,BlockPos pos,BlockState expected,BlockState next) {
        if(expected==next || kind==ArtifactKind.CRUCIBLE&&!MiningDesigns.matrix(expected) || !inert(expected) || !inert(next) || !allowed(p,kind,pos)
                || p.serverLevel().getBlockState(pos)!=expected || !harvestable(p,tool,pos) || !breakPermission(p,pos)) return false;
        var level=p.serverLevel();
        if(level.getBlockState(pos)!=expected) return false;
        var snapshot=BlockSnapshot.create(level.dimension(),level,pos);boolean accepted=false;
        try {
            if(!level.setBlock(pos,next,2)) return false;
            accepted=!ForgeEventFactory.onBlockPlace(p,snapshot,Direction.UP) && level.getBlockState(pos)==next
                && p.getMainHandItem()==tool && !tool.isEmpty();
            return accepted;
        } finally {
            if(!accepted) restoreOwned(level,snapshot,pos,next);
            else level.blockUpdated(pos,next.getBlock());
        }
    }
    public static boolean vacant(BlockState s) { return s.is(Blocks.AIR) || s.is(Blocks.CAVE_AIR) || s.is(Blocks.VOID_AIR); }
    public static boolean exchange(ServerPlayer p,ItemStack tool,BlockPos a,BlockPos b,BlockState sa,BlockState sb) {
        var kind=tool.getItem() instanceof ArtifactItem item?item.kind:ArtifactKind.ATLAS;
        if(barrier(p,a)||barrier(p,b)||a.equals(b) || sa==sb || vacant(sa) && vacant(sb) || !(inert(sa) || vacant(sa))
                || !(inert(sb) || vacant(sb)) || !allowed(p,kind,a)
                || !allowed(p,kind,b)) return false;
        var level=p.serverLevel();
        if(level.getBlockState(a)!=sa || level.getBlockState(b)!=sb || tool.isEmpty() || p.getMainHandItem()!=tool) return false;
        if(!vacant(sa) && (!harvestable(p,tool,a) || !breakPermission(p,a))) return false;
        if(!vacant(sb) && (!harvestable(p,tool,b) || !breakPermission(p,b))) return false;
        if(vacant(sa) && !emptyForPlacement(p,a,sb) || vacant(sb) && !emptyForPlacement(p,b,sa)) return false;
        if(level.getBlockState(a)!=sa || level.getBlockState(b)!=sb) return false;
        var oldA=BlockSnapshot.create(level.dimension(),level,a);var oldB=BlockSnapshot.create(level.dimension(),level,b);
        boolean accepted=false;
        try {
            if(!level.setBlock(a,sb,2) || !level.setBlock(b,sa,2)) return false;
            accepted=(vacant(sb) || !ForgeEventFactory.onBlockPlace(p,oldA,Direction.UP))
                && (vacant(sa) || !ForgeEventFactory.onBlockPlace(p,oldB,Direction.UP))
                && level.getBlockState(a)==sb && level.getBlockState(b)==sa && p.getMainHandItem()==tool && !tool.isEmpty();
            return accepted;
        } finally {
            if(!accepted) { restoreOwned(level,oldA,a,sb);restoreOwned(level,oldB,b,sa); }
            else { level.blockUpdated(a,sb.getBlock());level.blockUpdated(b,sa.getBlock()); }
        }
    }
    public static boolean freeBody(ServerPlayer p,Vec3 feet) {
        var level=p.serverLevel();var box=p.getBoundingBox().move(feet.subtract(p.position()));
        if(box.minY<level.getMinBuildHeight() || box.maxY>=level.getMaxBuildHeight() || !level.getWorldBorder().isWithinBounds(box)) return false;
        for(BlockPos pos:BlockPos.betweenClosed(BlockPos.containing(box.minX,box.minY,box.minZ),BlockPos.containing(box.maxX,box.maxY,box.maxZ)))
            if(!level.hasChunkAt(pos) || !level.getFluidState(pos).isEmpty() || !level.mayInteract(p,pos)) return false;
        return level.noCollision(p,box);
    }
    public static boolean move(ServerPlayer p,Vec3 target) { return move(p,target,ArtifactKind.ICARUS); }
    public static boolean move(ServerPlayer p,Vec3 target,ArtifactKind kind) {
        var delta=target.subtract(p.position());double distance=delta.length();
        if(!Double.isFinite(distance) || distance>8 || p.isPassenger() || p.isSleeping()) return false;
        int steps=Math.max(1,(int)Math.ceil(distance/0.2));
        for(int i=1;i<=steps;i++) {
            var point=p.position().add(delta.scale(i/(double)steps));
            if(!freeBody(p,point) || !allowed(p,kind,BlockPos.containing(point))) return false;
        }
        p.connection.teleport(target.x,target.y,target.z,p.getYRot(),p.getXRot());return true;
    }
}
