package io.github.santipdr.specialpickaxes.test;

import com.mojang.authlib.GameProfile;
import io.github.santipdr.specialpickaxes.*;
import io.github.santipdr.specialpickaxes.ability.*;
import io.github.santipdr.specialpickaxes.mining.MiningSafety;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import java.util.UUID;
import java.util.function.Consumer;

@GameTestHolder(SpecialPickaxes.ID)
@PrefixGameTestTemplate(false)
public final class PickaxeGameTests {
    private static ServerPlayer player(GameTestHelper h, String id) {
        var player = new FakePlayer(h.getLevel(), new GameProfile(UUID.randomUUID(), "PickaxeTest"));
        // FakePlayer's default handler deliberately makes teleport a no-op. Exercise the real handler.
        player.connection = new net.minecraft.server.network.ServerGamePacketListenerImpl(h.getLevel().getServer(),
            new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND), player);
        player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        var pos = h.absolutePos(new BlockPos(5, 2, 5));
        player.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(SpecialPickaxes.PICKS.get(id).get()));
        return player;
    }
    @GameTest(template = "empty") public static void registryAndRecipes(GameTestHelper h) {
        h.assertTrue(SpecialPickaxes.PICKS.size() == 10, "ten pickaxes");
        SpecialPickaxes.PICKS.forEach((id, object) -> {
            h.assertTrue(object.get() instanceof SpecialPickaxeItem, id + " registered");
            h.assertTrue(h.getLevel().getRecipeManager().byKey(new ResourceLocation(SpecialPickaxes.ID, id)).isPresent(), id + " recipe");
        });
        h.succeed();
    }
    @GameTest(template = "empty") public static void overdriveAndCooldown(GameTestHelper h) {
        var p = player(h, "overdrive"); var tool = p.getMainHandItem();
        h.assertTrue(AbilityRuntime.activate(p, tool, new SpeedAbility()), "first activation");
        h.assertTrue(p.hasEffect(SpecialPickaxes.OVERDRIVE.get()), "synced effect installed");
        var speed = new net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed(p,
            Blocks.STONE.defaultBlockState(), 8, p.blockPosition());
        MinecraftForge.EVENT_BUS.post(speed);
        h.assertTrue(speed.getNewSpeed() == 80, "actual BreakSpeed multiplier is ten");
        h.assertTrue(!AbilityRuntime.activate(p, tool, new SpeedAbility()), "cooldown blocks replay");
        h.assertTrue(tool.getDamageValue() == PickaxeConfig.TIMINGS.get("overdrive").cost().get(), "activation durability");
        h.succeed();
    }
    @GameTest(template = "empty") public static void claimsAndUnbreakable(GameTestHelper h) {
        var p = player(h, "excavator"); var tool = p.getMainHandItem();
        BlockPos pos = h.absolutePos(new BlockPos(5, 2, 7));
        h.getLevel().setBlockAndUpdate(pos, Blocks.STONE.defaultBlockState());
        Consumer<BlockEvent.BreakEvent> deny = e -> { if (e.getPos().equals(pos)) e.setCanceled(true); };
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, BlockEvent.BreakEvent.class, deny);
        try {
            h.assertTrue(MiningSafety.guarded(() -> MiningSafety.breakOne(p, tool, "excavator", pos, pos, 2, 2) ? 1 : 0) == 0, "claim denies");
            h.assertTrue(h.getLevel().getBlockState(pos).is(Blocks.STONE), "protected block stays");
            h.assertTrue(tool.getDamageValue() == 0, "denied break does not damage tool");
        } finally { MinecraftForge.EVENT_BUS.unregister(deny); }
        h.getLevel().setBlockAndUpdate(pos, Blocks.BEDROCK.defaultBlockState());
        h.assertTrue(!MiningSafety.eligible(p, tool, pos, 100), "bedrock denied");
        h.succeed();
    }
    @GameTest(template = "empty") public static void areaDurability(GameTestHelper h) {
        var p = player(h, "excavator"); p.setYRot(0); p.setXRot(0);
        BlockPos center = h.absolutePos(new BlockPos(5, 3, 7));
        for (int x = -1; x <= 1; x++) for (int y = -1; y <= 1; y++)
            h.getLevel().setBlockAndUpdate(center.offset(x, y, 0), Blocks.STONE.defaultBlockState());
        h.assertTrue(p.gameMode.destroyBlock(center), "root break");
        for (int x = -1; x <= 1; x++) for (int y = -1; y <= 1; y++)
            h.assertTrue(h.getLevel().getBlockState(center.offset(x,y,0)).isAir(), "3x3 mined");
        h.assertTrue(p.getMainHandItem().getDamageValue() == 9, "one durability per block");
        h.succeed();
    }
    @GameTest(template = "empty") public static void veinTerminates(GameTestHelper h) {
        var p = player(h, "vein_miner");
        BlockPos root = h.absolutePos(new BlockPos(3, 3, 7));
        for (int x = 0; x < 4; x++) h.getLevel().setBlockAndUpdate(root.offset(x, 0, 0), Blocks.DIAMOND_ORE.defaultBlockState());
        h.getLevel().setBlockAndUpdate(root.offset(4, 0, 0), Blocks.IRON_ORE.defaultBlockState());
        p.gameMode.destroyBlock(root);
        for (int x = 0; x < 4; x++) h.assertTrue(h.getLevel().getBlockState(root.offset(x,0,0)).isAir(), "connected ore mined");
        h.assertTrue(h.getLevel().getBlockState(root.offset(4,0,0)).is(Blocks.IRON_ORE), "different type retained");
        h.assertTrue(p.getMainHandItem().getDamageValue() == 4, "vein durability");
        h.succeed();
    }
    @GameTest(template = "empty") public static void smeltingSilkAndFortune(GameTestHelper h) {
        var p = player(h, "inferno"); var tool = p.getMainHandItem();
        BlockPos pos = h.absolutePos(new BlockPos(4, 3, 7));
        var state = Blocks.IRON_ORE.defaultBlockState();
        tool.enchant(Enchantments.BLOCK_FORTUNE, 3);
        var drops = Block.getDrops(state, h.getLevel(), pos, null, p, tool);
        h.assertTrue(!drops.isEmpty() && drops.stream().allMatch(s -> s.is(Items.IRON_INGOT)), "fortune loot smelted via recipes");
        var silk = new ItemStack(tool.getItem()); silk.enchant(Enchantments.SILK_TOUCH, 1);
        drops = Block.getDrops(state, h.getLevel(), pos, null, p, silk);
        h.assertTrue(drops.size() == 1 && drops.get(0).is(Items.IRON_ORE), "silk has priority");
        drops = Block.getDrops(Blocks.DIAMOND_ORE.defaultBlockState(), h.getLevel(), pos, null, p, tool);
        h.assertTrue(drops.stream().allMatch(s -> s.is(Items.DIAMOND)), "no recipe retains drops");
        h.succeed();
    }
    @GameTest(template = "empty") public static void magnetAndPickupDelay(GameTestHelper h) {
        var p = player(h, "magnetic");
        ItemEntity drop = new ItemEntity(h.getLevel(), p.getX()+2, p.getY()+0.5, p.getZ(), new ItemStack(Items.DIAMOND));
        drop.setNoPickUpDelay(); drop.setDeltaMovement(Vec3.ZERO); h.getLevel().addFreshEntity(drop);
        new MagnetAbility().tick(p, p.getMainHandItem());
        h.assertTrue(drop.getDeltaMovement().x < 0, "magnet pulls toward player");
        drop.setDefaultPickUpDelay(); drop.setDeltaMovement(Vec3.ZERO);
        new MagnetAbility().tick(p, p.getMainHandItem());
        h.assertTrue(drop.getDeltaMovement().lengthSqr() == 0, "pickup delay respected");
        h.succeed();
    }
    @GameTest(template = "empty") public static void scannerAndStorm(GameTestHelper h) {
        var p = player(h, "scanner");
        h.setBlock(new BlockPos(7,2,5), Blocks.DIAMOND_ORE);
        h.assertTrue(AbilityRuntime.activate(p, p.getMainHandItem(), new OreScannerAbility()), "scanner server activation");
        var mob = EntityType.ZOMBIE.create(h.getLevel());
        h.assertTrue(mob != null, "zombie created");
        mob.setPos(p.getX(), p.getY(), p.getZ()+2); h.getLevel().addFreshEntity(mob);
        float health = mob.getHealth();
        h.assertTrue(new StormAbility().activate(p, p.getMainHandItem()), "storm finds visible monster");
        h.assertTrue(mob.getHealth() < health, "storm does actual damage");
        h.succeed();
    }
    @GameTest(template = "empty") public static void explosiveBoundedAndProtected(GameTestHelper h) {
        var p = player(h, "explosive"); p.setYRot(0); p.setXRot(0);
        BlockPos center = h.absolutePos(new BlockPos(5, 3, 7));
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-2,-2,0), center.offset(2,2,3)))
            h.getLevel().setBlockAndUpdate(pos, Blocks.STONE.defaultBlockState());
        BlockPos denied = center.above();
        Consumer<BlockEvent.BreakEvent> deny = e -> { if (e.getPos().equals(denied)) e.setCanceled(true); };
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, BlockEvent.BreakEvent.class, deny);
        try {
            h.assertTrue(AbilityRuntime.activate(p, p.getMainHandItem(), new ExplosiveMiningAbility()), "controlled blast activates");
            h.assertTrue(h.getLevel().getBlockState(denied).is(Blocks.STONE), "protected blast neighbour survives");
            int damage = p.getMainHandItem().getDamageValue() - PickaxeConfig.TIMINGS.get("explosive").cost().get();
            h.assertTrue(damage > 0 && damage <= PickaxeConfig.BLAST_LIMIT.get(), "blast limit and per-block durability");
            h.assertTrue(h.getLevel().getBlockState(center.offset(0,0,3)).is(Blocks.STONE), "outside radius survives");
        } finally { MinecraftForge.EVENT_BUS.unregister(deny); }
        h.succeed();
    }
    @GameTest(template = "empty") public static void teleportWallsAndAnchor(GameTestHelper h) {
        var p = player(h, "ender");
        Vec3 destination = p.position().add(3, 0, 0);
        h.assertTrue(SafeTeleport.pathClear(p, destination, 8, "ender"), "open path");
        BlockPos wall = BlockPos.containing(p.position().add(1.5,0,0));
        h.getLevel().setBlockAndUpdate(wall, Blocks.STONE.defaultBlockState());
        h.getLevel().setBlockAndUpdate(wall.above(), Blocks.STONE.defaultBlockState());
        h.assertTrue(!SafeTeleport.pathClear(p, destination, 8, "ender"), "wall blocks swept volume");
        h.assertTrue(!SafeTeleport.free(p, Vec3.atBottomCenterOf(wall)), "solid destination denied");
        h.assertTrue(new VoidAnchorAbility().activate(p, p.getMainHandItem()), "anchor set");
        h.assertTrue(AbilityRuntime.data(p).contains("anchor"), "anchor recorded server-side");
        Vec3 origin = p.position();
        p.setPos(origin.x, origin.y, origin.z + 1);
        h.assertTrue(new VoidAnchorAbility().activate(p, p.getMainHandItem()), "anchor return");
        h.assertTrue(p.position().distanceToSqr(origin) < 0.01, "returned to exact anchor");
        p.setYRot(180); p.setXRot(0);
        h.assertTrue(new TeleportAbility().activate(p, p.getMainHandItem()), "ender open-path movement");
        h.succeed();
    }
}
