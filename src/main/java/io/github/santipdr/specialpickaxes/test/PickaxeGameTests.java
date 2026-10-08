package io.github.santipdr.specialpickaxes.test;

import com.mojang.authlib.GameProfile;
import io.github.santipdr.specialpickaxes.SpecialPickaxes;
import io.github.santipdr.specialpickaxes.artifact.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
        return player(h,kind,"ArtifactTest");
    }
    private static ServerPlayer player(GameTestHelper h,ArtifactKind kind,String name) {
        var p=new FakePlayer(h.getLevel(),new GameProfile(UUID.randomUUID(),name));
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
        h.assertTrue(SpecialPickaxes.PICKS.size()==12,"twelve artifacts");var p=player(h,ArtifactKind.PALIMPSEST);p.getInventory().clearContent();
        var source=h.getLevel().getServer().createCommandSourceStack().withEntity(p).withPermission(2);
        for(var kind:ArtifactKind.playableValues()) {
            var item=SpecialPickaxes.PICKS.get(kind).get();h.assertTrue(item instanceof ArtifactItem,"artifact item class");
            var tool=new ItemStack(item);h.assertTrue(tool.is(net.minecraft.tags.ItemTags.PICKAXES),"recognized as a pickaxe: "+kind);
            h.assertTrue(tool.is(net.minecraft.tags.ItemTags.AXES)&&tool.is(net.minecraft.tags.ItemTags.SHOVELS),"multi-tool tags stay complete: "+kind);
            h.assertTrue(h.getLevel().getRecipeManager().getRecipes().stream().noneMatch(r -> r.getResultItem(h.getLevel().registryAccess()).is(item)),"no recipe outputs artifact");
            int result=h.getLevel().getServer().getCommands().performPrefixedCommand(source,"specialpickaxes grant @s "+kind.id);
            h.assertTrue(result==1 && p.getInventory().countItem(item)==1,"admin command actually grants "+kind.id);
        }
        boolean denied=!h.getLevel().getServer().getCommands().getDispatcher().getRoot().getChild("specialpickaxes").canUse(source.withPermission(0));
        h.assertTrue(denied && p.getInventory().countItem(SpecialPickaxes.PICKS.get(ArtifactKind.PALIMPSEST).get())==1,"non-admin cannot grant");
        finish(h,p);
    }
    @GameTest(template="empty") public static void placedBlockProvenanceIsPrunedSafely(GameTestHelper h) {
        var removed=h.absolutePos(new BlockPos(5,3,7));h.getLevel().setBlockAndUpdate(removed,Blocks.STONE.defaultBlockState());
        var data=PlayerPlacedBlocks.get(h.getLevel());data.mark(removed);PlayerPlacedBlocks.scheduleCleanup(h.getLevel(),removed);
        h.getLevel().setBlockAndUpdate(removed,Blocks.AIR.defaultBlockState());PlayerPlacedBlocks.tickCleanup();
        h.assertTrue(!data.contains(removed),"removed placed-block provenance is released");
        var preserved=removed.east();h.getLevel().setBlockAndUpdate(preserved,Blocks.GRANITE.defaultBlockState());
        data.mark(preserved);PlayerPlacedBlocks.scheduleCleanup(h.getLevel(),preserved);PlayerPlacedBlocks.tickCleanup();
        h.assertTrue(data.contains(preserved),"unchanged/cancelled block keeps its protection");finish(h,player(h,ArtifactKind.PALIMPSEST));
    }
    @GameTest(template="empty") public static void palimpsestMinesOnlyItsConnectedOreVein(GameTestHelper h) {
        var p=player(h,ArtifactKind.PALIMPSEST);var pos=target(h);
        for(var at:List.of(pos,pos.east(),pos.above()))h.getLevel().setBlockAndUpdate(at,Blocks.DIAMOND_ORE.defaultBlockState());
        h.getLevel().setBlockAndUpdate(pos.east(2),Blocks.DEEPSLATE_DIAMOND_ORE.defaultBlockState());
        var adjacentGold=pos.east(3);h.getLevel().setBlockAndUpdate(adjacentGold,Blocks.GOLD_ORE.defaultBlockState());
        var stone=pos.south();h.getLevel().setBlockAndUpdate(stone,Blocks.STONE.defaultBlockState());
        var distant=pos.east(8);h.getLevel().setBlockAndUpdate(distant,Blocks.DIAMOND_ORE.defaultBlockState());
        h.assertTrue(ArtifactActions.use(p,p.getMainHandItem(),ArtifactKind.PALIMPSEST,false),"vein echo activates");drain(p);
        for(var at:List.of(pos,pos.east(),pos.above(),pos.east(2)))h.assertTrue(h.getLevel().getBlockState(at).isAir(),"connected ore-family vein mined");
        h.assertTrue(h.getLevel().getBlockState(stone).is(Blocks.STONE)&&h.getLevel().getBlockState(adjacentGold).is(Blocks.GOLD_ORE)&&h.getLevel().getBlockState(distant).is(Blocks.DIAMOND_ORE),"stone, adjacent different ore family, and disconnected ore preserved");finish(h,p);
    }
    @GameTest(template="empty") public static void choirReplayMatchesState(GameTestHelper h) {
        var p=player(h,ArtifactKind.CHOIR);p.setYRot(0);var origin=p.blockPosition().above().relative(Direction.SOUTH);
        for(int depth=0;depth<20;depth++)for(var pos:DirectionalProgram.section(origin,Direction.SOUTH,DirectionalProgram.Shape.RESONANT_TUNNEL,depth))h.getLevel().setBlockAndUpdate(pos,Blocks.STONE.defaultBlockState());
        var behind=p.blockPosition().relative(Direction.NORTH,2);h.getLevel().setBlockAndUpdate(behind,Blocks.STONE.defaultBlockState());
        h.assertTrue(ArtifactActions.use(p,p.getMainHandItem(),ArtifactKind.CHOIR,false),"forward resonance tunnel starts");drain(p);
        h.assertTrue(h.getLevel().getBlockState(origin).isAir()&&h.getLevel().getBlockState(origin.relative(Direction.SOUTH,19)).isAir(),"2x2 tunnel advances twenty blocks forward");
        h.assertTrue(h.getLevel().getBlockState(behind).is(Blocks.STONE),"tunnel leaves blocks behind untouched");finish(h,p);
    }
    @GameTest(template="empty") public static void eventideMiningNotCombat(GameTestHelper h) {
        var p=player(h,ArtifactKind.EVENTIDE);var center=target(h);
        h.getLevel().setBlockAndUpdate(center.above(),Blocks.DIAMOND_ORE.defaultBlockState());
        var mob=EntityType.ZOMBIE.create(h.getLevel());mob.setPos(center.getX()+2,center.getY(),center.getZ());h.getLevel().addFreshEntity(mob);
        mob.setDeltaMovement(Vec3.ZERO);float health=mob.getHealth();
        h.assertTrue(ArtifactActions.use(p,p.getMainHandItem(),ArtifactKind.EVENTIDE,false),"gravity field activates");DomainFields.tick();
        h.assertTrue(mob.getDeltaMovement().equals(Vec3.ZERO),"mining does not become entity combat");
        h.assertTrue(mob.getHealth()==health,"not a damage explosion");
        h.assertTrue(DomainFields.contains(p,center)&&p.hasEffect(net.minecraft.world.effect.MobEffects.DIG_SPEED),"field grants its mining effect");
        h.assertTrue(h.getLevel().getBlockState(center).is(Blocks.STONE)&&h.getLevel().getBlockState(center.above()).is(Blocks.DIAMOND_ORE),"activation does not perform a large quarry");finish(h,p);
    }

    @GameTest(template="empty") public static void crucibleConservesBlocksAndLoot(GameTestHelper h) {
        var p=player(h,ArtifactKind.CRUCIBLE);var pos=target(h);PlayerPlacedBlocks.get(h.getLevel()).unmark(pos);ArtifactState.of(p,ArtifactKind.CRUCIBLE).putInt("mode",7);
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
        h.assertTrue(p.hasEffect(net.minecraft.world.effect.MobEffects.REGENERATION)&&p.getEffect(net.minecraft.world.effect.MobEffects.REGENERATION).getAmplifier()==4&&p.hasEffect(net.minecraft.world.effect.MobEffects.NIGHT_VISION),"domain grants Regeneration V and Night Vision");
        DomainFields.stop(p);h.assertTrue(!DomainFields.frozen(mob) && Math.abs(mob.getDeltaMovement().x-.15)<.00001,"original momentum restored without weaponizing release");finish(h,p);
    }
    @GameTest(template="empty") public static void stasisAuraFollowsOnlyItsOwner(GameTestHelper h){
        var p=player(h,ArtifactKind.INTERREGNUM);ArtifactState.of(p,ArtifactKind.INTERREGNUM).putInt("mode",1);
        h.assertTrue(ArtifactActions.use(p,p.getMainHandItem(),ArtifactKind.INTERREGNUM,false),"personal aura activates without a target block");DomainFields.tick();
        h.assertTrue(DomainFields.contains(p,p.blockPosition())&&p.hasEffect(SpecialPickaxes.DOMINION.get())&&p.hasEffect(net.minecraft.world.effect.MobEffects.NIGHT_VISION),"aura follows owner with personal effects");
        var mob=EntityType.ZOMBIE.create(h.getLevel());mob.setPos(p.getX()+1,p.getY(),p.getZ());h.getLevel().addFreshEntity(mob);mob.setDeltaMovement(.1,0,0);DomainFields.tick();
        h.assertTrue(!DomainFields.frozen(mob),"personal aura does not freeze nearby mobs");finish(h,p);
    }
    @GameTest(template="empty") public static void stasisDomainSupportsAlliedMiners(GameTestHelper h){
        var owner=player(h,ArtifactKind.INTERREGNUM);var ally=player(h,ArtifactKind.INTERREGNUM,"ArtifactAlly");var center=target(h);
        var team=h.getLevel().getScoreboard().addPlayerTeam("stasis_"+owner.getUUID().toString().substring(0,8));
        h.getLevel().getScoreboard().addPlayerToTeam(owner.getScoreboardName(),team);
        h.getLevel().getScoreboard().addPlayerToTeam(ally.getScoreboardName(),team);
        ally.setPos(center.getX()+1.5,center.getY(),center.getZ()+0.5);ally.getFoodData().setFoodLevel(0);
        h.getLevel().addFreshEntity(ally);
        h.assertTrue(DomainFields.start(owner,owner.getMainHandItem(),ArtifactKind.INTERREGNUM,center,8),"team stasis starts");
        DomainFields.tick();
        h.assertTrue(ally.hasEffect(SpecialPickaxes.DOMINION.get())&&ally.hasEffect(net.minecraft.world.effect.MobEffects.NIGHT_VISION),"ally receives the domain identity effects");
        h.assertTrue(ally.getEffect(net.minecraft.world.effect.MobEffects.DIG_SPEED).getAmplifier()==2
                &&ally.getEffect(net.minecraft.world.effect.MobEffects.REGENERATION).getAmplifier()==1,"ally receives Haste III and Regeneration II");
        h.assertTrue(ally.getFoodData().getFoodLevel()>0,"ally receives gradual food restoration");
        DomainFields.stop(owner);finish(h,owner);
    }
    @GameTest(template="empty") public static void worldloomBoundedQuarryPreservesOres(GameTestHelper h) {
        var p=player(h,ArtifactKind.WORLDLOOM);var center=target(h);var stone=center.east();var ore=center.above();
        h.getLevel().setBlockAndUpdate(stone,Blocks.GRANITE.defaultBlockState());h.getLevel().setBlockAndUpdate(ore,Blocks.DIAMOND_ORE.defaultBlockState());
        h.assertTrue(ArtifactActions.use(p,p.getMainHandItem(),ArtifactKind.WORLDLOOM,false),"quarry activation");drain(p);
        h.assertTrue(h.getLevel().getBlockState(center).isAir()&&h.getLevel().getBlockState(stone).isAir(),"scheduled quarry mines the stone sphere");
        h.assertTrue(h.getLevel().getBlockState(ore).is(Blocks.DIAMOND_ORE),"quarry preserves ores");finish(h,p);
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
    @GameTest(template="empty") public static void axiomPeelsConnectedMatrix(GameTestHelper h) {
        var p=player(h,ArtifactKind.AXIOM);var center=target(h);var matrix=center.offset(1,0,0);var ore=center.above();
        h.getLevel().setBlockAndUpdate(matrix,Blocks.STONE.defaultBlockState());h.getLevel().setBlockAndUpdate(ore,Blocks.DIAMOND_ORE.defaultBlockState());
        h.assertTrue(ArtifactActions.use(p,p.getMainHandItem(),ArtifactKind.AXIOM,false),"axiom activation");for(int tick=0;tick<2000&&WorkQueue.busy(p);tick++)WorkQueue.tick();
        h.assertTrue(h.getLevel().getBlockState(matrix).isAir(),"matrix removed");
        h.assertTrue(h.getLevel().getBlockState(center).is(Blocks.STONE) && h.getLevel().getBlockState(ore).is(Blocks.DIAMOND_ORE),"hollow core and ore preserved");finish(h,p);
    }

    @GameTest(template="empty") public static void protectionRollbackAndPayment(GameTestHelper h) {
        var p=player(h,ArtifactKind.WORLDLOOM);var a=target(h);var b=a.offset(3,0,0);h.getLevel().setBlockAndUpdate(b,Blocks.DEEPSLATE.defaultBlockState());
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
        var p=player(h,ArtifactKind.WORLDLOOM);var pos=new BlockPos(1000000,100,1000000);p.setPos(pos.getX()+0.5,pos.getY(),pos.getZ()+0.5);
        h.assertTrue(!h.getLevel().hasChunkAt(pos),"fixture chunk is unloaded");
        h.assertTrue(!WorldSafety.mine(p,p.getMainHandItem(),ArtifactKind.WORLDLOOM,pos,Blocks.STONE.defaultBlockState()),"unloaded denied");
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



    @GameTest(template="empty") public static void multiToolAndHighEnchantments(GameTestHelper h){
        var p=player(h,ArtifactKind.WORLDBREAKER);var tool=p.getMainHandItem();
        for(var block:List.of(Blocks.STONE,Blocks.DEEPSLATE,Blocks.OAK_LOG,Blocks.OAK_PLANKS,Blocks.DIRT,Blocks.SAND,Blocks.GRAVEL,Blocks.CLAY,Blocks.SNOW_BLOCK)){
            h.assertTrue(tool.isCorrectToolForDrops(block.defaultBlockState()),"combined harvest tool: "+block);h.assertTrue(tool.getDestroySpeed(block.defaultBlockState())>=64,"combined mining speed");
        }
        for(int level:new int[]{100,255,1000,100000}){
            var levels=new net.minecraft.nbt.ListTag();var entry=new net.minecraft.nbt.CompoundTag();entry.putString("id","minecraft:efficiency");entry.putInt("lvl",level);levels.add(entry);tool.getOrCreateTag().put("Enchantments",levels);
            h.assertTrue(EnchantmentScaling.level(tool,Enchantments.BLOCK_EFFICIENCY)==level,"raw enchantment survives exactly");
            h.assertTrue(EnchantmentScaling.budget(tool)<=ArtifactConfig.ENCHANT_BUDGET.get(),"bounded throughput");
        }
        var fortune=new net.minecraft.nbt.CompoundTag();fortune.putString("id","minecraft:fortune");fortune.putInt("lvl",1000);tool.getEnchantmentTags().add(fortune);h.assertTrue(tool.getEnchantmentLevel(Enchantments.BLOCK_FORTUNE)==1000,"Fortune 1000 not truncated to 255");finish(h,p);
    }
    @GameTest(template="empty") public static void regionSelectionAnalysisAndPause(GameTestHelper h){
        var p=player(h,ArtifactKind.CRUCIBLE);ArtifactState.of(p,ArtifactKind.CRUCIBLE).putInt("mode",6);var a=target(h);var b=a.offset(2,0,0);PlayerPlacedBlocks.get(h.getLevel()).unmark(a);PlayerPlacedBlocks.get(h.getLevel()).unmark(b);h.getLevel().setBlockAndUpdate(b,Blocks.STONE.defaultBlockState());
        inputReady(p,ArtifactKind.CRUCIBLE);h.assertTrue(RelicControl.execute(p,ArtifactKind.CRUCIBLE,RelicControl.Action.ACTIVATE),"R arms selection");
        h.assertTrue(p.getMainHandItem().getDestroySpeed(Blocks.STONE.defaultBlockState())<=1F,"mining speed drops while selecting corners");
        h.assertTrue(ArtifactInteraction.left(p,a,false)&&ArtifactInteraction.left(p,b,false),"select corners");
        inputReady(p,ArtifactKind.CRUCIBLE);h.assertTrue(RelicControl.execute(p,ArtifactKind.CRUCIBLE,RelicControl.Action.ACTIVATE),"R analyzes selection");
        inputReady(p,ArtifactKind.CRUCIBLE);h.assertTrue(RelicControl.execute(p,ArtifactKind.CRUCIBLE,RelicControl.Action.ACTIVATE)&&WorkQueue.status(p).equals("paused"),"R pauses analysis");
        WorkQueue.tick();h.assertTrue(h.getLevel().getBlockState(a).is(Blocks.STONE),"paused operation untouched");
        inputReady(p,ArtifactKind.CRUCIBLE);h.assertTrue(RelicControl.execute(p,ArtifactKind.CRUCIBLE,RelicControl.Action.ACTIVATE),"R resumes analysis");
        for(int tick=0;tick<10&&!WorkQueue.status(p).equals("ready");tick++)WorkQueue.tick();
        h.assertTrue(h.getLevel().getBlockState(a).is(Blocks.STONE)&&WorkQueue.status(p).equals("ready"),"analysis never mutates and requires confirmation");
        inputReady(p,ArtifactKind.CRUCIBLE);h.assertTrue(RelicControl.execute(p,ArtifactKind.CRUCIBLE,RelicControl.Action.ACTIVATE),"R confirms analyzed operation");drain(p);
        h.assertTrue(h.getLevel().getBlockState(a).is(Blocks.BASALT),"confirmed operation transforms");ArtifactInteraction.clear(p);
        h.assertTrue(p.getMainHandItem().getDestroySpeed(Blocks.STONE.defaultBlockState())>1F,"normal mining speed returns after selection");finish(h,p);
    }


    @GameTest(template="empty") public static void simultaneousLargeRegionsAreBounded(GameTestHelper h){
        var p=player(h,ArtifactKind.CRUCIBLE);var q=player(h,ArtifactKind.CRUCIBLE);var a=target(h);var v=new SelectionVolume(a,a.offset(49,19,49));
        h.assertTrue(v.size()==50000,"large selection");
        for(var owner:List.of(p,q))h.assertTrue(WorkQueue.startRegion(owner,owner.getMainHandItem(),ArtifactKind.CRUCIBLE,new RegionWork(v,null,SelectionVolume.Transform.IDENTITY,ArtifactKind.CRUCIBLE,0,Blocks.BASALT.defaultBlockState())),"stream does not materialize/truncate large volume");
        WorkQueue.tick();h.assertTrue(WorkQueue.lastAttempts()<=ArtifactConfig.GLOBAL.get(),"global fair budget");h.assertTrue(WorkQueue.remaining(p)>49000&&WorkQueue.remaining(q)>49000,"both cursors retained");
        WorkQueue.cancel(q);h.assertTrue(!WorkQueue.busy(q)&&WorkQueue.busy(p),"independent cancellation");p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);WorkQueue.tick();h.assertTrue(!WorkQueue.busy(p),"tool switch cancels large work");finish(h,p);
    }

    @GameTest(template="empty") public static void regionLifecycleCancellation(GameTestHelper h){
        for(int reason=0;reason<3;reason++){
            var p=player(h,ArtifactKind.CRUCIBLE);var a=target(h);var v=new SelectionVolume(a,a.offset(10,10,10));
            WorkQueue.startRegion(p,p.getMainHandItem(),ArtifactKind.CRUCIBLE,new RegionWork(v,null,SelectionVolume.Transform.IDENTITY,ArtifactKind.CRUCIBLE,0,Blocks.BASALT.defaultBlockState()));
            if(reason==0)MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(p));
            if(reason==1){p.setHealth(0);WorkQueue.tick();}
            if(reason==2)MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent(p,net.minecraft.world.level.Level.OVERWORLD,net.minecraft.world.level.Level.NETHER));
            h.assertTrue(!WorkQueue.busy(p),"logout/death/dimension cancellation "+reason);
        }h.succeed();
    }
    @GameTest(template="empty") public static void unloadedRegionPausesWithoutLoading(GameTestHelper h){
        var p=player(h,ArtifactKind.CRUCIBLE);var a=new BlockPos(1000000,60,1000000);var v=new SelectionVolume(a,a);
        h.assertTrue(!h.getLevel().hasChunkAt(a),"fixture chunk absent");WorkQueue.startRegion(p,p.getMainHandItem(),ArtifactKind.CRUCIBLE,new RegionWork(v,null,SelectionVolume.Transform.IDENTITY,ArtifactKind.CRUCIBLE,0,Blocks.BASALT.defaultBlockState()));
        WorkQueue.tick();h.assertTrue(WorkQueue.status(p).equals("paused")&&WorkQueue.remaining(p)==1&&!h.getLevel().hasChunkAt(a),"no implicit chunk load, cursor retained");finish(h,p);
    }
    @GameTest(template="empty") public static void analyzedRegionDoesNotDestroyChangedBlocksOrMachines(GameTestHelper h){
        var p=player(h,ArtifactKind.CRUCIBLE);var a=target(h);var b=a.offset(1,0,0);h.getLevel().setBlockAndUpdate(b,Blocks.CHEST.defaultBlockState());
        var v=new SelectionVolume(a,b);WorkQueue.startRegion(p,p.getMainHandItem(),ArtifactKind.CRUCIBLE,new RegionWork(v,null,SelectionVolume.Transform.IDENTITY,ArtifactKind.CRUCIBLE,0,Blocks.BASALT.defaultBlockState()));
        WorkQueue.tick();h.getLevel().setBlockAndUpdate(a,Blocks.OBSIDIAN.defaultBlockState());WorkQueue.togglePause(p);drain(p);
        h.assertTrue(h.getLevel().getBlockState(a).is(Blocks.OBSIDIAN)&&h.getLevel().getBlockState(b).is(Blocks.CHEST),"analyzed state changed: skipped; machine untouched");finish(h,p);
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
        for(var k:ArtifactKind.playableValues())for(int m=0;m<ArtifactInteraction.modeCount(k);m++){
            boolean expected=k==ArtifactKind.CRUCIBLE||k==ArtifactKind.WORLDBREAKER&&m==5;
            h.assertTrue(ArtifactInteraction.regional(k,m)==expected,"regional contract "+k+" mode "+m);
        }h.succeed();
    }
    @GameTest(template="empty") public static void worldbreakerRegionBreakSkipsFluidsAndMachines(GameTestHelper h){
        var p=player(h,ArtifactKind.WORLDBREAKER);ArtifactState.of(p,ArtifactKind.WORLDBREAKER).putInt("mode",5);
        var a=target(h);var granite=a.east();var fluid=a.east(2);var chest=a.east(3);var b=a.east(4);
        h.getLevel().setBlockAndUpdate(granite,Blocks.GRANITE.defaultBlockState());h.getLevel().setBlockAndUpdate(fluid,Blocks.LAVA.defaultBlockState());
        h.getLevel().setBlockAndUpdate(chest,Blocks.CHEST.defaultBlockState());h.getLevel().setBlockAndUpdate(b,Blocks.STONE.defaultBlockState());
        h.assertTrue(ArtifactInteraction.regional(ArtifactKind.WORLDBREAKER,5),"region mode uses corner selection");
        RelicControl.execute(p,ArtifactKind.WORLDBREAKER,RelicControl.Action.SELECT);RelicControl.corner(p,a);RelicControl.corner(p,b);WorkQueue.tick();
        h.assertTrue(WorkQueue.status(p).equals("ready"),"selection is analyzed before execution");
        inputReady(p,ArtifactKind.WORLDBREAKER);h.assertTrue(RelicControl.execute(p,ArtifactKind.WORLDBREAKER,RelicControl.Action.ACTIVATE),"R confirms analyzed region");drain(p);
        h.assertTrue(h.getLevel().getBlockState(a).isAir()&&h.getLevel().getBlockState(granite).isAir()&&h.getLevel().getBlockState(b).isAir(),"minable region geology is harvested");
        h.assertTrue(h.getLevel().getBlockState(fluid).is(Blocks.LAVA)&&h.getLevel().getBlockState(chest).is(Blocks.CHEST),"fluid and container are preserved");finish(h,p);
    }
    @GameTest(template="empty") public static void compactAndExpandedTooltipContracts(GameTestHelper h){
        for(var k:ArtifactKind.playableValues()){
            var tool=new ItemStack(SpecialPickaxes.PICKS.get(k).get());var compact=new ArrayList<net.minecraft.network.chat.Component>();var expanded=new ArrayList<net.minecraft.network.chat.Component>();
            ArtifactTooltips.compact(tool,k,compact);ArtifactTooltips.expanded(tool,k,expanded);
            h.assertTrue(compact.size()==3&&expanded.size()>=9,"compact R/X/passive tooltip and complete manual "+k);
            if(k==ArtifactKind.CRUCIBLE){var actions=ArtifactTooltips.actions(tool,k,7);h.assertTrue(!actions.contains(RelicControl.Action.SELECT)&&actions.contains(RelicControl.Action.ACTIVATE)&&!actions.contains(RelicControl.Action.CONFIRM)&&!actions.contains(RelicControl.Action.PAUSE),"R replaces separate selection, confirm, and pause keys");}
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








    @GameTest(template="empty") public static void allRelicsKeepTripleToolAndHighLevels(GameTestHelper h){
        for(var k:ArtifactKind.playableValues()){
            var tool=new ItemStack(SpecialPickaxes.PICKS.get(k).get());
            for(var block:List.of(Blocks.STONE,Blocks.OAK_LOG,Blocks.DIRT))h.assertTrue(tool.isCorrectToolForDrops(block.defaultBlockState()),"triple tool "+k);
            for(int level:new int[]{100,255,1000,100000,Integer.MAX_VALUE}){
                var tags=new net.minecraft.nbt.ListTag();
                for(String id:List.of("efficiency","fortune","silk_touch","unbreaking","mending")){var t=new net.minecraft.nbt.CompoundTag();t.putString("id","minecraft:"+id);t.putInt("lvl",level);tags.add(t);}tool.getOrCreateTag().put("Enchantments",tags);
                var restored=ItemStack.of(tool.save(new net.minecraft.nbt.CompoundTag()));
                for(var enchant:List.of(Enchantments.BLOCK_EFFICIENCY,Enchantments.BLOCK_FORTUNE,Enchantments.SILK_TOUCH,Enchantments.UNBREAKING,Enchantments.MENDING)){
                    h.assertTrue(EnchantmentScaling.level(restored,enchant)==level,"raw NBT roundtrip "+level+" on "+k);
                    if(level<=1000)h.assertTrue(restored.getEnchantmentLevel(enchant)==level,"actual hook preserves "+level+" on "+k);
                }
            }
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
        var p=player(h,ArtifactKind.CRUCIBLE);var at=target(h);PlayerPlacedBlocks.get(h.getLevel()).unmark(at);PlayerPlacedBlocks.get(h.getLevel()).unmark(at.east());ArtifactState.of(p,ArtifactKind.CRUCIBLE).putInt("mode",7);p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(Items.OBSIDIAN));
        h.assertTrue(RelicControl.execute(p,ArtifactKind.CRUCIBLE,RelicControl.Action.SELECT),"selection key arms");
        RelicControl.corner(p,at);RelicControl.corner(p,at.east());for(int tick=0;tick<10&&!WorkQueue.status(p).equals("ready");tick++)WorkQueue.tick();
        h.assertTrue(WorkQueue.status(p).equals("ready")&&h.getLevel().getBlockState(at).is(Blocks.STONE),"last corner analyzes without mutation");
        inputReady(p,ArtifactKind.CRUCIBLE);h.assertTrue(RelicControl.execute(p,ArtifactKind.CRUCIBLE,RelicControl.Action.ACTIVATE),"R confirms analyzed region");for(int tick=0;tick<10&&WorkQueue.busy(p);tick++)WorkQueue.tick();
        h.assertTrue(h.getLevel().getBlockState(at).is(Blocks.OBSIDIAN),"confirmed region executes");finish(h,p);
    }
    @GameTest(template="empty") public static void modeIsIndependentOfSecondaryAndIncompleteSelection(GameTestHelper h){
        var p=player(h,ArtifactKind.CRUCIBLE);var at=target(h);
        RelicControl.execute(p,ArtifactKind.CRUCIBLE,RelicControl.Action.SELECT);RelicControl.corner(p,at);
        inputReady(p,ArtifactKind.CRUCIBLE);h.assertTrue(!RelicControl.execute(p,ArtifactKind.CRUCIBLE,RelicControl.Action.CONFIRM),"incomplete state cannot confirm");
        inputReady(p,ArtifactKind.CRUCIBLE);h.assertTrue(RelicControl.execute(p,ArtifactKind.CRUCIBLE,RelicControl.Action.MODE)&&!ArtifactInteraction.selecting(p),"mode cancels incompatible selection");h.assertTrue(ArtifactState.mode(p,ArtifactKind.CRUCIBLE)==1,"selected transform persists after leaving selection");
        RelicControl.execute(p,ArtifactKind.CRUCIBLE,RelicControl.Action.CANCEL);h.assertTrue(!ArtifactInteraction.selecting(p),"explicit cancel exits");finish(h,p);
    }


    @GameTest(template="empty") public static void changingToDirectModeLeavesNoSelector(GameTestHelper h){
        var p=player(h,ArtifactKind.CRUCIBLE);var at=target(h);
        RelicControl.execute(p,ArtifactKind.CRUCIBLE,RelicControl.Action.SELECT);RelicControl.corner(p,at);
        inputReady(p,ArtifactKind.CRUCIBLE);RelicControl.execute(p,ArtifactKind.CRUCIBLE,RelicControl.Action.MODE);
        h.assertTrue(ArtifactState.mode(p,ArtifactKind.CRUCIBLE)==1&&!ArtifactInteraction.selecting(p),"direct carve cannot inherit an active corner selector");finish(h,p);
    }
}
