package io.github.santipdr.specialpickaxes.test;

import com.mojang.authlib.GameProfile;
import io.github.santipdr.specialpickaxes.SpecialPickaxes;
import io.github.santipdr.specialpickaxes.artifact.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.gametest.*;
import java.util.*;
import java.util.function.Consumer;

@GameTestHolder(SpecialPickaxes.ID)
@PrefixGameTestTemplate(false)
public final class PickaxeGameTests {
    private static ServerPlayer player(GameTestHelper h,ArtifactKind kind) {
        var p=new FakePlayer(h.getLevel(),new GameProfile(UUID.randomUUID(),"ArtifactTest"));
        p.connection=new ServerGamePacketListenerImpl(h.getLevel().getServer(),new Connection(PacketFlow.SERVERBOUND),p);
        p.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        var at=h.absolutePos(new BlockPos(5,2,3));p.setPos(at.getX()+0.5,at.getY(),at.getZ()+0.5);
        p.setYRot(0);p.setXRot(0);p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(SpecialPickaxes.PICKS.get(kind).get()));return p;
    }
    private static BlockPos target(GameTestHelper h) {
        var pos=h.absolutePos(new BlockPos(5,3,7));h.getLevel().setBlockAndUpdate(pos,Blocks.STONE.defaultBlockState());return pos;
    }
    private static void finish(GameTestHelper h,ServerPlayer p) { WorkQueue.cancel(p);DomainFields.stop(p);h.succeed(); }
    private static void drain(ServerPlayer p) { for(int i=0;i<400 && WorkQueue.busy(p);i++) WorkQueue.tick(); }
    @GameTest(template="empty") public static void registryNoRecipesAndAdmin(GameTestHelper h) {
        h.assertTrue(SpecialPickaxes.PICKS.size()==10,"ten artifacts only");var p=player(h,ArtifactKind.PALIMPSEST);p.getInventory().clearContent();
        var source=h.getLevel().getServer().createCommandSourceStack().withEntity(p).withPermission(2);
        for(var kind:ArtifactKind.values()) {
            var item=SpecialPickaxes.PICKS.get(kind).get();h.assertTrue(item instanceof ArtifactItem,"artifact item class");
            h.assertTrue(h.getLevel().getRecipeManager().getRecipes().stream().noneMatch(r -> r.getResultItem(h.getLevel().registryAccess()).is(item)),"no recipe outputs artifact");
            int result=h.getLevel().getServer().getCommands().performPrefixedCommand(source,"specialpickaxes grant @s "+kind.id);
            h.assertTrue(result==1 && p.getInventory().countItem(item)==1,"admin command actually grants "+kind.id);
        }
        int denied=h.getLevel().getServer().getCommands().performPrefixedCommand(source.withPermission(0),"specialpickaxes grant @s palimpsest");
        h.assertTrue(denied==0 && p.getInventory().countItem(SpecialPickaxes.PICKS.get(ArtifactKind.PALIMPSEST).get())==1,"non-admin cannot grant");
        finish(h,p);
    }
    @GameTest(template="empty") public static void palimpsestPaidReconstruction(GameTestHelper h) {
        var p=player(h,ArtifactKind.PALIMPSEST);var pos=target(h);var tool=p.getMainHandItem();
        h.assertTrue(p.gameMode.destroyBlock(pos),"manual mining succeeds");
        h.assertTrue(ArtifactState.memories(p,ArtifactKind.PALIMPSEST).size()==1,"actual mining recorded");
        h.assertTrue(ArtifactActions.use(p,tool,ArtifactKind.PALIMPSEST,false),"reconstruction queued");drain(p);
        h.assertTrue(h.getLevel().getBlockState(pos).isAir(),"no free restoration without material");
        p.getInventory().add(new ItemStack(Items.STONE,2));
        h.assertTrue(ArtifactActions.primary(p,tool,ArtifactKind.PALIMPSEST),"retry with payment");drain(p);
        h.assertTrue(h.getLevel().getBlockState(pos).is(Blocks.STONE),"restored original state");
        h.assertTrue(p.getInventory().countItem(Items.STONE)==1,"exactly one material consumed");
        finish(h,p);
    }
    @GameTest(template="empty") public static void choirReplayMatchesState(GameTestHelper h) {
        var p=player(h,ArtifactKind.CHOIR);var pos=target(h);var source=pos.offset(-2,0,0);
        ArtifactState.record(p,ArtifactKind.CHOIR,source,Blocks.STONE.defaultBlockState());
        ArtifactState.record(p,ArtifactKind.CHOIR,source.above(),Blocks.STONE.defaultBlockState());
        h.getLevel().setBlockAndUpdate(pos.above(),Blocks.DIAMOND_ORE.defaultBlockState());
        h.assertTrue(ArtifactActions.use(p,p.getMainHandItem(),ArtifactKind.CHOIR,false),"replay activates");drain(p);
        h.assertTrue(h.getLevel().getBlockState(pos).isAir(),"translated stroke mined");
        h.assertTrue(h.getLevel().getBlockState(pos.above()).is(Blocks.DIAMOND_ORE),"mismatched ore not mined");finish(h,p);
    }
    @GameTest(template="empty") public static void singularityAndPreservation(GameTestHelper h) {
        var p=player(h,ArtifactKind.EVENTIDE);var center=target(h);
        h.getLevel().setBlockAndUpdate(center.above(),Blocks.DIAMOND_ORE.defaultBlockState());
        var mob=EntityType.ZOMBIE.create(h.getLevel());mob.setPos(center.getX()+2,center.getY(),center.getZ());h.getLevel().addFreshEntity(mob);
        mob.setDeltaMovement(Vec3.ZERO);float health=mob.getHealth();
        h.assertTrue(ArtifactActions.use(p,p.getMainHandItem(),ArtifactKind.EVENTIDE,false),"domain activates");DomainFields.tick();drain(p);
        h.assertTrue(mob.getDeltaMovement().x<0,"hostile trajectory bends inward");
        h.assertTrue(mob.getHealth()==health,"not a damage explosion");
        h.assertTrue(h.getLevel().getBlockState(center).isAir() && h.getLevel().getBlockState(center.above()).is(Blocks.DIAMOND_ORE),"geology removed, ore preserved");finish(h,p);
    }
    @GameTest(template="empty") public static void meridianEntangledMining(GameTestHelper h) {
        var p=player(h,ArtifactKind.MERIDIAN);var a=target(h);var b=a.offset(3,0,0);h.getLevel().setBlockAndUpdate(b,Blocks.STONE.defaultBlockState());
        ArtifactState.anchor(p,ArtifactKind.MERIDIAN,"a",a);ArtifactState.anchor(p,ArtifactKind.MERIDIAN,"b",b);
        p.gameMode.destroyBlock(a);h.assertTrue(WorkQueue.busy(p),"manual mining queued consequence");drain(p);
        h.assertTrue(h.getLevel().getBlockState(b).isAir(),"paired anchor consequence mined");
        h.assertTrue(p.getMainHandItem().getDamageValue()==2,"both physical blocks cost durability");finish(h,p);
    }
    @GameTest(template="empty") public static void crucibleConservesBlocksAndLoot(GameTestHelper h) {
        var p=player(h,ArtifactKind.CRUCIBLE);var pos=target(h);ArtifactState.of(p,ArtifactKind.CRUCIBLE).putInt("mode",3);
        h.assertTrue(ArtifactActions.use(p,p.getMainHandItem(),ArtifactKind.CRUCIBLE,false),"transmutation activates");drain(p);
        h.assertTrue(h.getLevel().getBlockState(pos).is(Blocks.OBSIDIAN),"curated rephase applied");
        h.assertTrue(h.getLevel().getEntitiesOfClass(ItemEntity.class,new AABB(pos).inflate(0.8)).isEmpty(),"no extra drops from replacement");finish(h,p);
    }
    @GameTest(template="empty") public static void interregnumStasisAndRelease(GameTestHelper h) {
        var p=player(h,ArtifactKind.INTERREGNUM);var center=target(h);
        var mob=EntityType.ZOMBIE.create(h.getLevel());mob.setPos(center.getX()+1.5,center.getY(),center.getZ()+0.5);h.getLevel().addFreshEntity(mob);
        mob.setDeltaMovement(0.15,0,0);Vec3 start=mob.position();
        h.assertTrue(ArtifactActions.use(p,p.getMainHandItem(),ArtifactKind.INTERREGNUM,false),"stasis activation");DomainFields.tick();
        h.assertTrue(DomainFields.frozen(mob),"hostile enrolled in stasis");mob.setPos(start.add(0.3,0,0));DomainFields.tick();
        h.assertTrue(mob.position().distanceToSqr(start)<0.001 && mob.getDeltaMovement().lengthSqr()==0,"position and momentum held");
        DomainFields.stop(p);h.assertTrue(!DomainFields.frozen(mob) && mob.getDeltaMovement().x>0,"momentum restored on release");finish(h,p);
    }
    @GameTest(template="empty") public static void worldloomPaidBridge(GameTestHelper h) {
        var p=player(h,ArtifactKind.WORLDLOOM);target(h);p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(Items.STONE,32));
        ArtifactState.of(p,ArtifactKind.WORLDLOOM).putInt("mode",1);
        h.assertTrue(ArtifactActions.use(p,p.getMainHandItem(),ArtifactKind.WORLDLOOM,false),"loom activation");drain(p);
        h.assertTrue(p.getInventory().countItem(Items.STONE)<32,"construction paid from inventory");
        h.assertTrue(h.getLevel().getBlockState(h.absolutePos(new BlockPos(5,4,7))).is(Blocks.STONE),"bridge built above target");finish(h,p);
    }
    @GameTest(template="empty") public static void icarusBoreAndSolidStop(GameTestHelper h) {
        var p=player(h,ArtifactKind.ICARUS);var wall=h.absolutePos(new BlockPos(5,2,5));
        for(int x=-1;x<=1;x++) for(int y=0;y<3;y++) h.getLevel().setBlockAndUpdate(wall.offset(x,y,0),Blocks.STONE.defaultBlockState());
        var barrier=wall.offset(0,0,2);h.getLevel().setBlockAndUpdate(barrier,Blocks.BEDROCK.defaultBlockState());
        h.getLevel().setBlockAndUpdate(barrier.above(),Blocks.BEDROCK.defaultBlockState());
        double old=p.getZ();h.assertTrue(ArtifactActions.use(p,p.getMainHandItem(),ArtifactKind.ICARUS,false),"kinetic bore activates");drain(p);
        h.assertTrue(p.getZ()>old && p.getZ()<barrier.getZ(),"advanced through excavation but not bedrock");
        h.assertTrue(h.getLevel().getBlockState(wall).isAir(),"bore excavated corridor");finish(h,p);
    }
    @GameTest(template="empty") public static void axiomPreservesOreAndRibs(GameTestHelper h) {
        var p=player(h,ArtifactKind.AXIOM);var center=target(h);var matrix=center.offset(1,0,0);var ore=center.above();
        h.getLevel().setBlockAndUpdate(matrix,Blocks.STONE.defaultBlockState());h.getLevel().setBlockAndUpdate(ore,Blocks.DIAMOND_ORE.defaultBlockState());
        h.assertTrue(ArtifactActions.use(p,p.getMainHandItem(),ArtifactKind.AXIOM,false),"axiom activation");drain(p);
        h.assertTrue(h.getLevel().getBlockState(matrix).isAir(),"matrix removed");
        h.assertTrue(h.getLevel().getBlockState(center).is(Blocks.STONE) && h.getLevel().getBlockState(ore).is(Blocks.DIAMOND_ORE),"structural rib and ore preserved");finish(h,p);
    }
    @GameTest(template="empty") public static void atlasAtomicSwap(GameTestHelper h) {
        var p=player(h,ArtifactKind.ATLAS);var a=target(h);var b=a.offset(3,0,0);h.getLevel().setBlockAndUpdate(b,Blocks.DEEPSLATE.defaultBlockState());
        h.assertTrue(WorldSafety.exchange(p,p.getMainHandItem(),a,b,Blocks.STONE.defaultBlockState(),Blocks.DEEPSLATE.defaultBlockState()),"atomic geology swap");
        h.assertTrue(h.getLevel().getBlockState(a).is(Blocks.DEEPSLATE) && h.getLevel().getBlockState(b).is(Blocks.STONE),"both sides exchanged");
        h.assertTrue(h.getLevel().getEntitiesOfClass(ItemEntity.class,new AABB(a,b).inflate(1)).isEmpty(),"exchange has no drops");finish(h,p);
    }
    @GameTest(template="empty") public static void protectionRollbackAndPayment(GameTestHelper h) {
        var p=player(h,ArtifactKind.ATLAS);var a=target(h);var b=a.offset(3,0,0);h.getLevel().setBlockAndUpdate(b,Blocks.DEEPSLATE.defaultBlockState());
        Consumer<BlockEvent.EntityPlaceEvent> deny=e -> { if(e.getPos().equals(b)) e.setCanceled(true); };
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL,false,BlockEvent.EntityPlaceEvent.class,deny);
        try {
            h.assertTrue(!WorldSafety.exchange(p,p.getMainHandItem(),a,b,Blocks.STONE.defaultBlockState(),Blocks.DEEPSLATE.defaultBlockState()),"denied second placement rejects pair");
            h.assertTrue(h.getLevel().getBlockState(a).is(Blocks.STONE) && h.getLevel().getBlockState(b).is(Blocks.DEEPSLATE),"both states rolled back");
            h.getLevel().setBlockAndUpdate(b,Blocks.AIR.defaultBlockState());p.getInventory().add(new ItemStack(Items.STONE,2));
            h.assertTrue(!WorldSafety.placePaid(p,p.getMainHandItem(),ArtifactKind.WORLDLOOM,b,Blocks.STONE.defaultBlockState()),"placement cancellation honoured");
            h.assertTrue(p.getInventory().countItem(Items.STONE)==2 && h.getLevel().getBlockState(b).isAir(),"denial costs nothing and leaves air");
        } finally { MinecraftForge.EVENT_BUS.unregister(deny); }
        finish(h,p);
    }
    @GameTest(template="empty") public static void breakProtectionAndStalePlans(GameTestHelper h) {
        var p=player(h,ArtifactKind.CHOIR);var pos=target(h);var tool=p.getMainHandItem();
        Consumer<BlockEvent.BreakEvent> deny=e -> { if(e.getPos().equals(pos)) e.setCanceled(true); };
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL,false,BlockEvent.BreakEvent.class,deny);
        try { h.assertTrue(!WorldSafety.mine(p,tool,ArtifactKind.CHOIR,pos,Blocks.STONE.defaultBlockState()),"Forge break protection honoured"); }
        finally { MinecraftForge.EVENT_BUS.unregister(deny); }
        h.assertTrue(tool.getDamageValue()==0 && h.getLevel().getBlockState(pos).is(Blocks.STONE),"no denied drops/durability");
        var step=new WorkStep.Mine(pos,Blocks.STONE.defaultBlockState());h.getLevel().setBlockAndUpdate(pos,Blocks.DIAMOND_ORE.defaultBlockState());
        h.assertTrue(!step.apply(p,tool,ArtifactKind.CHOIR),"stale plan cannot mine changed state");finish(h,p);
    }
    @GameTest(template="empty") public static void cooldownPersistenceAndMemoryBound(GameTestHelper h) {
        var p=player(h,ArtifactKind.INTERREGNUM);target(h);var tool=p.getMainHandItem();
        h.assertTrue(ArtifactActions.use(p,tool,ArtifactKind.INTERREGNUM,false),"first activation");
        h.assertTrue(!ArtifactActions.use(p,tool,ArtifactKind.INTERREGNUM,false),"replay blocked");
        h.assertTrue(tool.getDamageValue()==ArtifactConfig.COST.get(),"activation cost exact");
        var serialized=ArtifactState.root(p).copy();var other=player(h,ArtifactKind.INTERREGNUM);
        other.getPersistentData().put(ArtifactState.ROOT,serialized);
        h.assertTrue(!ArtifactActions.use(other,other.getMainHandItem(),ArtifactKind.INTERREGNUM,false),"serialized cooldown retained");
        for(int i=0;i<ArtifactConfig.MEMORY.get()+10;i++) ArtifactState.record(p,ArtifactKind.PALIMPSEST,new BlockPos(i,40,0),Blocks.STONE.defaultBlockState());
        h.assertTrue(ArtifactState.memories(p,ArtifactKind.PALIMPSEST).size()==ArtifactConfig.MEMORY.get(),"bounded memory");
        var snapshot=ArtifactState.root(p).copy();other.getPersistentData().put(ArtifactState.ROOT,snapshot);
        h.assertTrue(ArtifactState.memories(other,ArtifactKind.PALIMPSEST).size()==ArtifactConfig.MEMORY.get(),"memory roundtrip");
        WorkQueue.cancel(other);DomainFields.stop(other);finish(h,p);
    }
    @GameTest(template="empty") public static void schedulerBudgetsAndCancellation(GameTestHelper h) {
        var p=player(h,ArtifactKind.AXIOM);var steps=new ArrayList<WorkStep>();var count=new int[]{0};
        for(int i=0;i<100;i++) steps.add(new WorkStep() {
            public BlockPos pos(){return p.blockPosition();}
            public boolean apply(ServerPlayer actor,ItemStack stack,ArtifactKind kind){count[0]++;return false;}
        });
        h.assertTrue(WorkQueue.start(p,p.getMainHandItem(),ArtifactKind.AXIOM,steps),"job queued");WorkQueue.tick();
        h.assertTrue(count[0]>0 && count[0]<=ArtifactConfig.PER_PLAYER.get(),"attempts, including failures, are budgeted");
        h.assertTrue(WorkQueue.lastAttempts()<=ArtifactConfig.GLOBAL.get(),"global budget");
        int previous=count[0];p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);WorkQueue.tick();
        h.assertTrue(count[0]==previous && !WorkQueue.busy(p),"switching tool cancels pending work");finish(h,p);
    }
    @GameTest(template="empty") public static void unloadedChunkAndRestrictedBlocks(GameTestHelper h) {
        var p=player(h,ArtifactKind.ATLAS);var pos=new BlockPos(1000000,100,1000000);p.setPos(pos.getX()+0.5,pos.getY(),pos.getZ()+0.5);
        h.assertTrue(!h.getLevel().hasChunkAt(pos),"fixture chunk is unloaded");
        h.assertTrue(!WorldSafety.mine(p,p.getMainHandItem(),ArtifactKind.ATLAS,pos,Blocks.STONE.defaultBlockState()),"unloaded denied");
        h.assertTrue(!h.getLevel().hasChunkAt(pos),"operation did not load chunk");
        h.assertTrue(!WorldSafety.inert(Blocks.CHEST.defaultBlockState()) && !WorldSafety.inert(Blocks.DIAMOND_ORE.defaultBlockState())
            && !WorldSafety.inert(Blocks.BEDROCK.defaultBlockState()),"protected matter categories excluded");finish(h,p);
    }
    @GameTest(template="empty") public static void silkFortuneLootAndDurability(GameTestHelper h) {
        var p=player(h,ArtifactKind.CHOIR);var pos=target(h);var tool=p.getMainHandItem();tool.enchant(Enchantments.SILK_TOUCH,1);
        WorkQueue.start(p,tool,ArtifactKind.CHOIR,List.of(new WorkStep.Mine(pos,Blocks.STONE.defaultBlockState())));drain(p);
        var drops=h.getLevel().getEntitiesOfClass(ItemEntity.class,new AABB(pos).inflate(0.8));
        h.assertTrue(drops.stream().mapToInt(e -> e.getItem().is(Items.STONE)?e.getItem().getCount():0).sum()==1,"exact silk drop, not duplicated");
        h.assertTrue(tool.getDamageValue()==1,"normal per-block durability");
        tool=new ItemStack(tool.getItem());tool.enchant(Enchantments.BLOCK_FORTUNE,3);p.setItemInHand(InteractionHand.MAIN_HAND,tool);
        var ore=pos.offset(2,0,0);h.getLevel().setBlockAndUpdate(ore,Blocks.DIAMOND_ORE.defaultBlockState());
        WorkQueue.start(p,tool,ArtifactKind.CHOIR,List.of(new WorkStep.Mine(ore,Blocks.DIAMOND_ORE.defaultBlockState())));drain(p);
        int diamonds=h.getLevel().getEntitiesOfClass(ItemEntity.class,new AABB(ore).inflate(0.8)).stream().mapToInt(e -> e.getItem().is(Items.DIAMOND)?e.getItem().getCount():0).sum();
        h.assertTrue(diamonds>=1 && diamonds<=4,"Fortune loot within vanilla range");finish(h,p);
    }
    @GameTest(template="empty") public static void noUnpaidPlacementOrWallTeleport(GameTestHelper h) {
        var p=player(h,ArtifactKind.ICARUS);var pos=target(h);
        h.assertTrue(!WorldSafety.move(p,Vec3.atBottomCenterOf(pos)),"occupied destination rejected");
        h.assertTrue(!WorldSafety.placePaid(p,p.getMainHandItem(),ArtifactKind.WORLDLOOM,pos,Blocks.STONE.defaultBlockState()),"occupied construction rejected");
        var air=pos.above();h.assertTrue(!WorldSafety.placePaid(p,p.getMainHandItem(),ArtifactKind.WORLDLOOM,air,Blocks.STONE.defaultBlockState()),"no free block without payment");finish(h,p);
    }
}
