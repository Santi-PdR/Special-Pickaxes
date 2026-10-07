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
        if(kind==ArtifactKind.WORLDBREAKER)ArtifactState.of(p,kind).putInt("mode",0);
        p.setYRot(0);p.setXRot(0);p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(SpecialPickaxes.PICKS.get(kind).get()));return p;
    }
    private static BlockPos target(GameTestHelper h) {
        var pos=h.absolutePos(new BlockPos(5,3,7));h.getLevel().setBlockAndUpdate(pos,Blocks.STONE.defaultBlockState());return pos;
    }
    private static void finish(GameTestHelper h,ServerPlayer p) { CompanionActions.stop(p);ArtifactInteraction.clear(p);WorkQueue.cancel(p);DomainFields.stop(p);h.succeed(); }
    private static void drain(ServerPlayer p) { for(int i=0;i<400 && WorkQueue.busy(p);i++) WorkQueue.tick(); }
    @GameTest(template="empty") public static void registryNoRecipesAndAdmin(GameTestHelper h) {
        h.assertTrue(SpecialPickaxes.PICKS.size()==20,"twenty artifacts");var p=player(h,ArtifactKind.PALIMPSEST);p.getInventory().clearContent();
        var source=h.getLevel().getServer().createCommandSourceStack().withEntity(p).withPermission(2);
        for(var kind:ArtifactKind.values()) {
            var item=SpecialPickaxes.PICKS.get(kind).get();h.assertTrue(item instanceof ArtifactItem,"artifact item class");
            h.assertTrue(h.getLevel().getRecipeManager().getRecipes().stream().noneMatch(r -> r.getResultItem(h.getLevel().registryAccess()).is(item)),"no recipe outputs artifact");
            int result=h.getLevel().getServer().getCommands().performPrefixedCommand(source,"specialpickaxes grant @s "+kind.id);
            h.assertTrue(result==1 && p.getInventory().countItem(item)==1,"admin command actually grants "+kind.id);
        }
        boolean denied=!h.getLevel().getServer().getCommands().getDispatcher().getRoot().getChild("specialpickaxes").canUse(source.withPermission(0));
        h.assertTrue(denied && p.getInventory().countItem(SpecialPickaxes.PICKS.get(ArtifactKind.PALIMPSEST).get())==1,"non-admin cannot grant");
        finish(h,p);
    }
    @GameTest(template="empty") public static void palimpsestPaidReconstruction(GameTestHelper h) {
        var p=player(h,ArtifactKind.PALIMPSEST);var pos=target(h);var tool=p.getMainHandItem();
        h.assertTrue(p.gameMode.destroyBlock(pos),"manual mining succeeds");MiningObservations.flush();
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
        p.gameMode.destroyBlock(a);MiningObservations.flush();h.assertTrue(WorkQueue.busy(p),"manual mining queued consequence");drain(p);
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
    @GameTest(template="empty") public static void vanillaTiersRemainValid(GameTestHelper h) {
        h.assertTrue(new ItemStack(Items.DIAMOND_PICKAXE).isCorrectToolForDrops(Blocks.OBSIDIAN.defaultBlockState()),"artifact tier does not revoke vanilla diamond harvesting");
        h.assertTrue(new ItemStack(Items.NETHERITE_PICKAXE).isCorrectToolForDrops(Blocks.DIAMOND_ORE.defaultBlockState()),"netherite remains valid");
        h.succeed();
    }
    @GameTest(template="empty") public static void changedPaymentCannotDuplicateMatter(GameTestHelper h) {
        var p=player(h,ArtifactKind.WORLDLOOM);var pos=target(h).above();
        p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(Items.STONE,1));
        Consumer<BlockEvent.EntityPlaceEvent> change=e -> {
            if(e.getPos().equals(pos)) p.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);
        };
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL,false,BlockEvent.EntityPlaceEvent.class,change);
        try {
            h.assertTrue(!WorldSafety.placePaid(p,p.getMainHandItem(),ArtifactKind.WORLDLOOM,pos,Blocks.STONE.defaultBlockState()),"invalidated payment rejects placement");
            h.assertTrue(h.getLevel().getBlockState(pos).isAir(),"unpaid matter rolled back");
        } finally { MinecraftForge.EVENT_BUS.unregister(change); }
        finish(h,p);
    }

    @GameTest(template="empty") public static void anchorsDoNotCrossDimensions(GameTestHelper h) {
        var p=player(h,ArtifactKind.MERIDIAN);var a=target(h);
        ArtifactState.anchor(p,ArtifactKind.MERIDIAN,"a",a);ArtifactState.anchor(p,ArtifactKind.MERIDIAN,"b",a.offset(2,0,0));
        ArtifactState.of(p,ArtifactKind.MERIDIAN).putString("dimension","minecraft:the_nether");
        h.assertTrue(ArtifactState.anchor(p,ArtifactKind.MERIDIAN,"a").isEmpty(),"foreign anchor rejected");
        ArtifactState.anchor(p,ArtifactKind.MERIDIAN,"a",a);
        h.assertTrue(ArtifactState.anchor(p,ArtifactKind.MERIDIAN,"b").isEmpty(),"new dimension cannot inherit old second anchor");finish(h,p);
    }
    @GameTest(template="empty") public static void dominionSpeedIsLocal(GameTestHelper h) {
        var p=player(h,ArtifactKind.INTERREGNUM);var center=target(h);
        DomainFields.start(p,p.getMainHandItem(),ArtifactKind.INTERREGNUM,center,4);
        p.setPos(center.getX()+0.5,center.getY(),center.getZ()-2);DomainFields.tick();
        var speed=new net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed(p,Blocks.STONE.defaultBlockState(),64,center);
        MinecraftForge.EVENT_BUS.post(speed);h.assertTrue(speed.getNewSpeed()==512,"domain grants actual eightfold speed");
        p.setPos(center.getX()+12,center.getY(),center.getZ());
        speed=new net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed(p,Blocks.STONE.defaultBlockState(),64,center);
        MinecraftForge.EVENT_BUS.post(speed);h.assertTrue(speed.getNewSpeed()==64,"no speed outside domain");finish(h,p);
    }

    @GameTest(template="empty") public static void atlasRelocatesIntoAirWithoutDuplication(GameTestHelper h) {
        var p=player(h,ArtifactKind.ATLAS);var a=target(h);var b=a.offset(3,1,0);
        h.getLevel().setBlockAndUpdate(b,Blocks.AIR.defaultBlockState());
        h.assertTrue(WorldSafety.exchange(p,p.getMainHandItem(),a,b,Blocks.STONE.defaultBlockState(),Blocks.AIR.defaultBlockState()),"solid transported into empty space");
        h.assertTrue(h.getLevel().getBlockState(a).isAir() && h.getLevel().getBlockState(b).is(Blocks.STONE),"one block removed, exactly one block placed");
        h.assertTrue(h.getLevel().getEntitiesOfClass(ItemEntity.class,new AABB(a,b).inflate(1)).isEmpty(),"relocation emits no duplicated material");finish(h,p);
    }

    @GameTest(template="empty") public static void multiToolAndHighEnchantments(GameTestHelper h){
        var p=player(h,ArtifactKind.WORLDBREAKER);var tool=p.getMainHandItem();
        for(var block:List.of(Blocks.STONE,Blocks.DEEPSLATE,Blocks.OAK_LOG,Blocks.OAK_PLANKS,Blocks.DIRT,Blocks.SAND,Blocks.GRAVEL,Blocks.CLAY,Blocks.SNOW_BLOCK)){
            h.assertTrue(tool.isCorrectToolForDrops(block.defaultBlockState()),"combined harvest tool: "+block);h.assertTrue(tool.getDestroySpeed(block.defaultBlockState())==64,"combined mining speed");
        }
        for(int level:new int[]{100,255,1000,100000}){
            var levels=new net.minecraft.nbt.ListTag();var entry=new net.minecraft.nbt.CompoundTag();entry.putString("id","minecraft:efficiency");entry.putInt("lvl",level);levels.add(entry);tool.getOrCreateTag().put("Enchantments",levels);
            h.assertTrue(EnchantmentScaling.level(tool,Enchantments.BLOCK_EFFICIENCY)==level,"raw enchantment survives exactly");
            h.assertTrue(EnchantmentScaling.budget(tool)<=ArtifactConfig.ENCHANT_BUDGET.get(),"bounded throughput");
        }
        var fortune=new net.minecraft.nbt.CompoundTag();fortune.putString("id","minecraft:fortune");fortune.putInt("lvl",1000);tool.getEnchantmentTags().add(fortune);h.assertTrue(tool.getEnchantmentLevel(Enchantments.BLOCK_FORTUNE)==1000,"Fortune 1000 not truncated to 255");finish(h,p);
    }
    @GameTest(template="empty") public static void regionSelectionAnalysisAndPause(GameTestHelper h){
        var p=player(h,ArtifactKind.WORLDBREAKER);var a=target(h);var b=a.offset(2,0,0);h.getLevel().setBlockAndUpdate(b,Blocks.STONE.defaultBlockState());
        h.assertTrue(ArtifactInteraction.use(p,p.getMainHandItem(),ArtifactKind.WORLDBREAKER,false),"arm");
        h.assertTrue(ArtifactInteraction.left(p,a,false)&&ArtifactInteraction.left(p,b,false),"select corners");
        h.assertTrue(ArtifactInteraction.use(p,p.getMainHandItem(),ArtifactKind.WORLDBREAKER,false),"analyze");WorkQueue.tick();
        h.assertTrue(h.getLevel().getBlockState(a).is(Blocks.STONE),"analysis never mutates");h.assertTrue(WorkQueue.status(p).equals("ready"),"ready requires confirmation");
        WorkQueue.togglePause(p);WorkQueue.togglePause(p);WorkQueue.tick();h.assertTrue(h.getLevel().getBlockState(a).is(Blocks.STONE),"paused operation untouched");
        WorkQueue.togglePause(p);drain(p);h.assertTrue(h.getLevel().getBlockState(a).isAir(),"confirmed operation mines");ArtifactInteraction.clear(p);finish(h,p);
    }
    @GameTest(template="empty") public static void transformedRegionExchange(GameTestHelper h){
        var p=player(h,ArtifactKind.ATLAS);var a=target(h);var b=a.offset(4,0,0);
        h.getLevel().setBlockAndUpdate(a.offset(1,0,0),Blocks.OBSIDIAN.defaultBlockState());h.getLevel().setBlockAndUpdate(b,Blocks.DEEPSLATE.defaultBlockState());h.getLevel().setBlockAndUpdate(b.offset(0,0,1),Blocks.BASALT.defaultBlockState());
        var region=new RegionWork(new SelectionVolume(a,a.offset(1,0,0)),new SelectionVolume(b,b.offset(0,0,1)),SelectionVolume.Transform.ROTATE_90,ArtifactKind.ATLAS,0,Blocks.STONE.defaultBlockState());
        h.assertTrue(WorkQueue.startRegion(p,p.getMainHandItem(),ArtifactKind.ATLAS,region),"queue transformed volume");WorkQueue.tick();WorkQueue.togglePause(p);drain(p);
        h.assertTrue(h.getLevel().getBlockState(b).is(Blocks.STONE)&&h.getLevel().getBlockState(b.offset(0,0,1)).is(Blocks.OBSIDIAN),"rotation maps every cell");finish(h,p);
    }
    @GameTest(template="empty") public static void checkpointPaidRestore(GameTestHelper h){
        var p=player(h,ArtifactKind.CHRONICLE);var a=target(h);var volume=new SelectionVolume(a,a);
        var record=new RegionWork(volume,null,SelectionVolume.Transform.IDENTITY,ArtifactKind.CHRONICLE,0,Blocks.STONE.defaultBlockState());
        WorkQueue.startRegion(p,p.getMainHandItem(),ArtifactKind.CHRONICLE,record);WorkQueue.tick();WorkQueue.togglePause(p);drain(p);
        h.assertTrue(ArtifactState.memories(p,ArtifactKind.CHRONICLE).size()==1,"checkpoint recorded");h.getLevel().setBlockAndUpdate(a,Blocks.AIR.defaultBlockState());p.getInventory().add(new ItemStack(Items.STONE));
        var restore=new RegionWork(volume,null,SelectionVolume.Transform.IDENTITY,ArtifactKind.CHRONICLE,1,Blocks.STONE.defaultBlockState());WorkQueue.startRegion(p,p.getMainHandItem(),ArtifactKind.CHRONICLE,restore);WorkQueue.tick();
        h.assertTrue(!WorkQueue.togglePause(p),"regional cooldown respected");
        // Advance only this fake owner's readiness, without changing the shared test world's clock.
        ArtifactState.of(p,ArtifactKind.CHRONICLE).putLong("ready",0);WorkQueue.togglePause(p);drain(p);
        h.assertTrue(h.getLevel().getBlockState(a).is(Blocks.STONE)&&p.getInventory().countItem(Items.STONE)==0,"paid restoration conserves material");finish(h,p);
    }
    @GameTest(template="empty") public static void simultaneousLargeRegionsAreBounded(GameTestHelper h){
        var p=player(h,ArtifactKind.WORLDBREAKER);var q=player(h,ArtifactKind.WORLDBREAKER);var a=target(h);var v=new SelectionVolume(a,a.offset(49,19,49));
        h.assertTrue(v.size()==50000,"large selection");
        for(var owner:List.of(p,q))h.assertTrue(WorkQueue.startRegion(owner,owner.getMainHandItem(),ArtifactKind.WORLDBREAKER,new RegionWork(v,null,SelectionVolume.Transform.IDENTITY,ArtifactKind.WORLDBREAKER,0,Blocks.STONE.defaultBlockState())),"stream does not materialize/truncate large volume");
        WorkQueue.tick();h.assertTrue(WorkQueue.lastAttempts()<=ArtifactConfig.GLOBAL.get(),"global fair budget");h.assertTrue(WorkQueue.remaining(p)>49000&&WorkQueue.remaining(q)>49000,"both cursors retained");
        WorkQueue.cancel(q);h.assertTrue(!WorkQueue.busy(q)&&WorkQueue.busy(p),"independent cancellation");p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);WorkQueue.tick();h.assertTrue(!WorkQueue.busy(p),"tool switch cancels large work");finish(h,p);
    }

    @GameTest(template="empty") public static void regionLifecycleCancellation(GameTestHelper h){
        for(int reason=0;reason<3;reason++){
            var p=player(h,ArtifactKind.WORLDBREAKER);var a=target(h);var v=new SelectionVolume(a,a.offset(10,10,10));
            WorkQueue.startRegion(p,p.getMainHandItem(),ArtifactKind.WORLDBREAKER,new RegionWork(v,null,SelectionVolume.Transform.IDENTITY,ArtifactKind.WORLDBREAKER,0,Blocks.STONE.defaultBlockState()));
            if(reason==0)MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(p));
            if(reason==1){p.setHealth(0);WorkQueue.tick();}
            if(reason==2)MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent(p,net.minecraft.world.level.Level.OVERWORLD,net.minecraft.world.level.Level.NETHER));
            h.assertTrue(!WorkQueue.busy(p),"logout/death/dimension cancellation "+reason);
        }h.succeed();
    }
    @GameTest(template="empty") public static void unloadedRegionPausesWithoutLoading(GameTestHelper h){
        var p=player(h,ArtifactKind.WORLDBREAKER);var a=new BlockPos(1000000,60,1000000);var v=new SelectionVolume(a,a);
        h.assertTrue(!h.getLevel().hasChunkAt(a),"fixture chunk absent");WorkQueue.startRegion(p,p.getMainHandItem(),ArtifactKind.WORLDBREAKER,new RegionWork(v,null,SelectionVolume.Transform.IDENTITY,ArtifactKind.WORLDBREAKER,0,Blocks.STONE.defaultBlockState()));
        WorkQueue.tick();h.assertTrue(WorkQueue.status(p).equals("paused")&&WorkQueue.remaining(p)==1&&!h.getLevel().hasChunkAt(a),"no implicit chunk load, cursor retained");finish(h,p);
    }
    @GameTest(template="empty") public static void analyzedRegionDoesNotDestroyChangedBlocksOrMachines(GameTestHelper h){
        var p=player(h,ArtifactKind.WORLDBREAKER);var a=target(h);var b=a.offset(1,0,0);h.getLevel().setBlockAndUpdate(b,Blocks.CHEST.defaultBlockState());
        var v=new SelectionVolume(a,b);WorkQueue.startRegion(p,p.getMainHandItem(),ArtifactKind.WORLDBREAKER,new RegionWork(v,null,SelectionVolume.Transform.IDENTITY,ArtifactKind.WORLDBREAKER,0,Blocks.STONE.defaultBlockState()));
        WorkQueue.tick();h.getLevel().setBlockAndUpdate(a,Blocks.OBSIDIAN.defaultBlockState());WorkQueue.togglePause(p);drain(p);
        h.assertTrue(h.getLevel().getBlockState(a).is(Blocks.OBSIDIAN)&&h.getLevel().getBlockState(b).is(Blocks.CHEST),"analyzed state changed: skipped; machine untouched");finish(h,p);
    }
    @GameTest(template="empty") public static void tessellatorPaysAndLeavesBlueprint(GameTestHelper h){
        var p=player(h,ArtifactKind.TESSELLATOR);var a=target(h);var b=a.offset(3,0,0);h.getLevel().setBlockAndUpdate(b,Blocks.AIR.defaultBlockState());p.getInventory().add(new ItemStack(Items.STONE));
        var r=new RegionWork(new SelectionVolume(a,a),new SelectionVolume(b,b),SelectionVolume.Transform.IDENTITY,ArtifactKind.TESSELLATOR,0,Blocks.STONE.defaultBlockState());WorkQueue.startRegion(p,p.getMainHandItem(),ArtifactKind.TESSELLATOR,r);WorkQueue.tick();WorkQueue.togglePause(p);drain(p);
        h.assertTrue(h.getLevel().getBlockState(a).is(Blocks.STONE)&&h.getLevel().getBlockState(b).is(Blocks.STONE)&&p.getInventory().countItem(Items.STONE)==0,"copy is paid, not free matter");finish(h,p);
    }
    @GameTest(template="empty") public static void keystoneBuildsPaidArch(GameTestHelper h){
        var p=player(h,ArtifactKind.KEYSTONE);var a=target(h).above();var v=new SelectionVolume(a,a.offset(4,3,0));for(long n=0;n<v.size();n++)h.getLevel().setBlockAndUpdate(v.at(n),Blocks.AIR.defaultBlockState());p.getInventory().add(new ItemStack(Items.STONE,32));
        WorkQueue.startRegion(p,p.getMainHandItem(),ArtifactKind.KEYSTONE,new RegionWork(v,null,SelectionVolume.Transform.IDENTITY,ArtifactKind.KEYSTONE,0,Blocks.STONE.defaultBlockState()));WorkQueue.tick();WorkQueue.togglePause(p);drain(p);
        h.assertTrue(h.getLevel().getBlockState(a.offset(2,3,0)).is(Blocks.STONE),"vault apex");h.assertTrue(h.getLevel().getBlockState(a.offset(2,0,0)).isAir(),"walkable void under vault");h.assertTrue(p.getInventory().countItem(Items.STONE)==27,"five paid arch voxels");finish(h,p);
    }
    @GameTest(template="empty") public static void regionProtectionStillAppliesAfterAnalysis(GameTestHelper h){
        var p=player(h,ArtifactKind.WORLDBREAKER);var a=target(h);var v=new SelectionVolume(a,a);
        WorkQueue.startRegion(p,p.getMainHandItem(),ArtifactKind.WORLDBREAKER,new RegionWork(v,null,SelectionVolume.Transform.IDENTITY,ArtifactKind.WORLDBREAKER,0,Blocks.STONE.defaultBlockState()));WorkQueue.tick();
        Consumer<BlockEvent.BreakEvent> deny=e->{if(e.getPlayer()==p)e.setCanceled(true);};MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST,deny);
        try{WorkQueue.togglePause(p);drain(p);h.assertTrue(h.getLevel().getBlockState(a).is(Blocks.STONE),"new protection still cancels after preparing");}finally{MinecraftForge.EVENT_BUS.unregister(deny);}finish(h,p);
    }

    @GameTest(template="empty") public static void extremeEnchantmentGameplay(GameTestHelper h){
        var p=player(h,ArtifactKind.WORLDBREAKER);var pos=target(h);var tool=p.getMainHandItem();var tags=new net.minecraft.nbt.ListTag();
        for(String id:List.of("efficiency","fortune","unbreaking","mending")){var t=new net.minecraft.nbt.CompoundTag();t.putString("id","minecraft:"+id);t.putInt("lvl",id.equals("efficiency")?Integer.MAX_VALUE:1000);tags.add(t);}tool.getOrCreateTag().put("Enchantments",tags);
        float speed=p.getDigSpeed(Blocks.STONE.defaultBlockState(),pos);h.assertTrue(Float.isFinite(speed)&&speed>0,"actual vanilla/Forge extreme Efficiency is finite and positive");
        h.getLevel().setBlockAndUpdate(pos,Blocks.DIAMOND_ORE.defaultBlockState());h.assertTrue(WorldSafety.mine(p,tool,ArtifactKind.WORLDBREAKER,pos,Blocks.DIAMOND_ORE.defaultBlockState()),"real Fortune 1000 harvesting");
        int diamonds=h.getLevel().getEntitiesOfClass(ItemEntity.class,new AABB(pos).inflate(1)).stream().filter(e->e.getItem().is(Items.DIAMOND)).mapToInt(e->e.getItem().getCount()).sum();
        h.assertTrue(diamonds>0&&diamonds<=1002,"native high-Fortune loot remains bounded and nonnegative");
        tool.setDamageValue(100);new net.minecraft.world.entity.ExperienceOrb(h.getLevel(),p.getX(),p.getY(),p.getZ(),10).playerTouch(p);
        h.assertTrue(tool.getDamageValue()<100,"Mending 1000 repairs with actual XP pickup");finish(h,p);
    }

    @GameTest(template="empty") public static void memoriesKeepDimensionsAndExpire(GameTestHelper h){
        var p=player(h,ArtifactKind.PALIMPSEST);var a=target(h);ArtifactState.record(p,ArtifactKind.PALIMPSEST,a,Blocks.STONE.defaultBlockState());
        var list=ArtifactState.of(p,ArtifactKind.PALIMPSEST).getList("memory",net.minecraft.nbt.Tag.TAG_COMPOUND);list.getCompound(0).putString("dim","minecraft:the_nether");
        ArtifactState.record(p,ArtifactKind.PALIMPSEST,a.offset(1,0,0),Blocks.STONE.defaultBlockState());
        h.assertTrue(list.size()==2&&ArtifactState.memories(p,ArtifactKind.PALIMPSEST).size()==1,"other dimension history preserved but not restored here");
        list.getCompound(0).putLong("epoch",System.currentTimeMillis()-(ArtifactConfig.MEMORY_TTL.get()+1L)*50);ArtifactState.prune(p,ArtifactKind.PALIMPSEST);
        h.assertTrue(list.size()==1,"expired memory pruned from player-visible count");finish(h,p);
    }


    @GameTest(template="empty") public static void onlyNecessaryModesUseCorners(GameTestHelper h){
        for(var k:ArtifactKind.values())for(int m=0;m<ArtifactInteraction.modeCount(k);m++){
            boolean expected=k==ArtifactKind.ATLAS||k==ArtifactKind.CRUCIBLE||k==ArtifactKind.CHRONICLE||k==ArtifactKind.TESSELLATOR||k==ArtifactKind.WORLDBREAKER&&m!=1&&m!=4&&m!=6;
            h.assertTrue(ArtifactInteraction.regional(k,m)==expected,"regional contract "+k+" mode "+m);
        }h.succeed();
    }
    @GameTest(template="empty") public static void compactAndExpandedTooltipContracts(GameTestHelper h){
        for(var k:ArtifactKind.values()){
            var tool=new ItemStack(SpecialPickaxes.PICKS.get(k).get());var compact=new ArrayList<net.minecraft.network.chat.Component>();var expanded=new ArrayList<net.minecraft.network.chat.Component>();
            ArtifactTooltips.compact(tool,k,compact);ArtifactTooltips.expanded(tool,k,expanded);
            h.assertTrue(compact.size()==1&&expanded.size()>=9,"compact identity/mode/hint and advanced manual "+k);
        }h.succeed();
    }
    @GameTest(template="empty") public static void legacyEnergyDoesNotGateAbilities(GameTestHelper h){
        var p=player(h,ArtifactKind.INTERREGNUM);target(h);var tool=p.getMainHandItem();var data=ArtifactState.of(p,ArtifactKind.INTERREGNUM);
        data.putInt("charge",0);tool.getOrCreateTag().putInt("artifactCharge",999);
        ((ArtifactItem)tool.getItem()).inventoryTick(tool,h.getLevel(),p,0,true);
        h.assertTrue(!data.contains("charge")&&!tool.getTag().contains("artifactCharge"),"legacy values removed");
        ArtifactInteraction.use(p,tool,ArtifactKind.INTERREGNUM,false);
        h.assertTrue(DomainFields.active(p)&&!ArtifactInteraction.selecting(p),"first use activates directly without stored resource");finish(h,p);
    }
    @GameTest(template="empty") public static void aegisReflectsWithoutTakingOwnership(GameTestHelper h){
        var p=player(h,ArtifactKind.AEGIS);var center=target(h);
        var shot=new net.minecraft.world.entity.projectile.Snowball(h.getLevel(),center.getX()+2.5,center.getY()+0.5,center.getZ()+0.5);
        shot.setDeltaMovement(-0.3,0,0);h.getLevel().addFreshEntity(shot);
        DomainFields.start(p,p.getMainHandItem(),ArtifactKind.AEGIS,center,8);DomainFields.tick();
        h.assertTrue(shot.getDeltaMovement().x>0&&shot.getOwner()==null,"outward reflection, unchanged owner");shot.discard();finish(h,p);
    }
    @GameTest(template="empty") public static void lodestarRetracesAndConsumesRoute(GameTestHelper h){
        var p=player(h,ArtifactKind.LODESTAR);var start=p.blockPosition();var tool=p.getMainHandItem();
        h.assertTrue(DirectAbilities.activate(p,tool,ArtifactKind.LODESTAR),"anchor marked");
        p.setPos(start.getX()+1.5,start.getY(),start.getZ()+0.5);DirectAbilities.recordFootstep(p);
        p.setPos(start.getX()+2.5,start.getY(),start.getZ()+0.5);DirectAbilities.recordFootstep(p);
        h.assertTrue(DirectAbilities.activate(p,tool,ArtifactKind.LODESTAR),"return queued");drain(p);
        h.assertTrue(p.blockPosition().equals(start)&&ArtifactState.anchor(p,ArtifactKind.LODESTAR,"a").isEmpty(),"returned physically; route consumed");finish(h,p);
    }
    @GameTest(template="empty") public static void seamRipperExtractsOnlyContactSkin(GameTestHelper h){
        var p=player(h,ArtifactKind.SEAM_RIPPER);var at=target(h);
        h.getLevel().setBlockAndUpdate(at.east(),Blocks.STONE.defaultBlockState());h.getLevel().setBlockAndUpdate(at.above(),Blocks.DIRT.defaultBlockState());
        p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(Items.STONE));h.assertTrue(DirectAbilities.seam(p,at).isEmpty(),"same material is not an interface");
        p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(Items.DIRT));var plan=DirectAbilities.seam(p,at);
        h.assertTrue(plan.size()==1,"not whole connected vein");WorkQueue.start(p,p.getMainHandItem(),ArtifactKind.SEAM_RIPPER,plan);drain(p);
        h.assertTrue(h.getLevel().getBlockState(at).isAir()&&h.getLevel().getBlockState(at.east()).is(Blocks.STONE)&&h.getLevel().getBlockState(at.above()).is(Blocks.DIRT),"only A touching B mined");finish(h,p);
    }
    @GameTest(template="empty") public static void causewayPaysForFootingAndCancels(GameTestHelper h){
        var p=player(h,ArtifactKind.CAUSEWAY);var below=p.blockPosition().below();
        h.getLevel().setBlockAndUpdate(below,Blocks.AIR.defaultBlockState());h.getLevel().setBlockAndUpdate(below.south(),Blocks.AIR.defaultBlockState());
        p.getInventory().add(new ItemStack(Items.STONE,2));CompanionActions.start(p,p.getMainHandItem(),ArtifactKind.CAUSEWAY);CompanionActions.tick();drain(p);
        h.assertTrue(h.getLevel().getBlockState(below).is(Blocks.STONE)&&p.getInventory().countItem(Items.STONE)==0,"two paid footings");
        p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);CompanionActions.tick();h.assertTrue(!CompanionActions.active(p),"tool change cancels service");finish(h,p);
    }
    @GameTest(template="empty") public static void countersealFiltersTerrainButNotEntities(GameTestHelper h){
        var p=player(h,ArtifactKind.COUNTERSEAL);var near=p.blockPosition();var far=near.east(12);
        CompanionActions.start(p,p.getMainHandItem(),ArtifactKind.COUNTERSEAL);
        var explosion=new net.minecraft.world.level.Explosion(h.getLevel(),null,near.getX(),near.getY(),near.getZ(),4,false,net.minecraft.world.level.Explosion.BlockInteraction.DESTROY);
        explosion.getToBlow().addAll(List.of(near,far));var entities=new ArrayList<Entity>();entities.add(p);
        MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.level.ExplosionEvent.Detonate(h.getLevel(),explosion,entities));
        h.assertTrue(explosion.getToBlow().equals(List.of(far))&&entities.equals(List.of(p)),"terrain ward leaves distant blocks and entity damage list unchanged");finish(h,p);
    }
    @GameTest(template="empty") public static void covenantSealsConfirmedManualScars(GameTestHelper h){
        var p=player(h,ArtifactKind.COVENANT);var at=target(h);p.getInventory().add(new ItemStack(Items.STONE));
        CompanionActions.start(p,p.getMainHandItem(),ArtifactKind.COVENANT);
        h.assertTrue(p.gameMode.destroyBlock(at),"manual break");MiningObservations.flush();CompanionActions.tick();drain(p);
        h.assertTrue(h.getLevel().getBlockState(at).isAir(),"not sealed before passing it");
        p.setPos(at.getX()+0.5,at.getY(),at.getZ()+4.5);CompanionActions.tick();drain(p);
        h.assertTrue(h.getLevel().getBlockState(at).is(Blocks.STONE)&&p.getInventory().countItem(Items.STONE)==0,"paid wake sealed behind miner");finish(h,p);
    }

    @GameTest(template="empty") public static void lodestarUsesItsOwnPermissionEvent(GameTestHelper h){
        var p=player(h,ArtifactKind.LODESTAR);var before=p.position();
        Consumer<io.github.santipdr.specialpickaxes.ability.AbilityUseEvent> deny=e->{if(e.player==p&&e.ability.equals("lodestar"))e.setCanceled(true);};
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST,deny);
        try{h.assertTrue(!new WorkStep.Move(p.blockPosition().east()).apply(p,p.getMainHandItem(),ArtifactKind.LODESTAR)&&p.position().equals(before),"route respects Lodestar permission, not Icarus permission");}
        finally{MinecraftForge.EVENT_BUS.unregister(deny);}finish(h,p);
    }
    @GameTest(template="empty") public static void allRelicsKeepTripleToolAndHighLevels(GameTestHelper h){
        for(var k:ArtifactKind.values()){
            var tool=new ItemStack(SpecialPickaxes.PICKS.get(k).get());
            for(var block:List.of(Blocks.STONE,Blocks.OAK_LOG,Blocks.DIRT))h.assertTrue(tool.isCorrectToolForDrops(block.defaultBlockState()),"triple tool "+k);
            var tags=new net.minecraft.nbt.ListTag();
            for(String id:List.of("efficiency","fortune","silk_touch","unbreaking","mending")){var t=new net.minecraft.nbt.CompoundTag();t.putString("id","minecraft:"+id);t.putInt("lvl",1000);tags.add(t);}tool.getOrCreateTag().put("Enchantments",tags);
            for(var enchant:List.of(Enchantments.BLOCK_EFFICIENCY,Enchantments.BLOCK_FORTUNE,Enchantments.SILK_TOUCH,Enchantments.UNBREAKING,Enchantments.MENDING))h.assertTrue(tool.getEnchantmentLevel(enchant)==1000,"level 1000 preserved on "+k);
        }h.succeed();
    }

    private static void inputReady(ServerPlayer p,ArtifactKind k){ArtifactState.of(p,k).remove("inputTick");}
    @GameTest(template="empty") public static void semanticControlsAreServerAuthoritative(GameTestHelper h){
        var p=player(h,ArtifactKind.INTERREGNUM);target(h);
        h.assertTrue(!RelicControl.execute(p,ArtifactKind.ICARUS,RelicControl.Action.ACTIVATE),"stale tool packet rejected");
        h.assertTrue(RelicControl.execute(p,ArtifactKind.INTERREGNUM,RelicControl.Action.ACTIVATE)&&DomainFields.active(p),"one intent activates without selection");
        h.assertTrue(RelicControl.execute(p,ArtifactKind.INTERREGNUM,RelicControl.Action.CANCEL)&&!DomainFields.active(p),"cancel bypasses activation cooldown");finish(h,p);
    }
    @GameTest(template="empty") public static void selectionAutomaticallyAnalyzesThenExplicitlyConfirms(GameTestHelper h){
        var p=player(h,ArtifactKind.CRUCIBLE);var at=target(h);ArtifactState.of(p,ArtifactKind.CRUCIBLE).putInt("mode",3);
        h.assertTrue(RelicControl.execute(p,ArtifactKind.CRUCIBLE,RelicControl.Action.SELECT),"selection key arms");
        RelicControl.corner(p,at);RelicControl.corner(p,at);WorkQueue.tick();
        h.assertTrue(WorkQueue.status(p).equals("ready")&&h.getLevel().getBlockState(at).is(Blocks.STONE),"last corner analyzes without mutation");
        inputReady(p,ArtifactKind.CRUCIBLE);h.assertTrue(RelicControl.execute(p,ArtifactKind.CRUCIBLE,RelicControl.Action.CONFIRM),"explicit confirm");drain(p);
        h.assertTrue(h.getLevel().getBlockState(at).is(Blocks.OBSIDIAN),"confirmed region executes");finish(h,p);
    }
    @GameTest(template="empty") public static void modeIsIndependentOfSecondaryAndIncompleteSelection(GameTestHelper h){
        var p=player(h,ArtifactKind.ATLAS);var at=target(h);
        RelicControl.execute(p,ArtifactKind.ATLAS,RelicControl.Action.SELECT);RelicControl.corner(p,at);
        inputReady(p,ArtifactKind.ATLAS);h.assertTrue(!RelicControl.execute(p,ArtifactKind.ATLAS,RelicControl.Action.CONFIRM),"incomplete state cannot confirm");
        inputReady(p,ArtifactKind.ATLAS);h.assertTrue(RelicControl.execute(p,ArtifactKind.ATLAS,RelicControl.Action.MODE)&&ArtifactInteraction.selecting(p),"transform does not cancel selection");
        RelicControl.execute(p,ArtifactKind.ATLAS,RelicControl.Action.CANCEL);h.assertTrue(!ArtifactInteraction.selecting(p),"explicit cancel exits");finish(h,p);
    }
    @GameTest(template="empty") public static void supremeProgramPreservesCoreAndCombinesPaidArchitecture(GameTestHelper h){
        var p=player(h,ArtifactKind.WORLDBREAKER);var center=target(h);ArtifactState.of(p,ArtifactKind.WORLDBREAKER).remove("mode");
        h.assertTrue(ArtifactState.mode(p,ArtifactKind.WORLDBREAKER)==6&&!ArtifactInteraction.regional(ArtifactKind.WORLDBREAKER,6),"new supreme mode defaults to direct convergence");
        var outer=center.east(6);h.getLevel().setBlockAndUpdate(outer,Blocks.STONE.defaultBlockState());var plan=DirectAbilities.convergence(p,center);
        h.assertTrue(plan.stream().anyMatch(s->s instanceof WorkStep.Mine)&&plan.stream().anyMatch(s->s instanceof WorkStep.Place),"two distinct operations in one bounded program");
        h.assertTrue(plan.stream().noneMatch(s->s instanceof WorkStep.Mine&&s.pos().equals(center)),"central core preserved");finish(h,p);
    }
}
