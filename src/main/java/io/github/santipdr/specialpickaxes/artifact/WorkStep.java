package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public interface WorkStep {
    BlockPos pos();
    boolean apply(ServerPlayer player,ItemStack tool,ArtifactKind kind);
    default boolean stopOnFailure() { return false; }
    record Mine(BlockPos pos,BlockState expected) implements WorkStep {
        public boolean apply(ServerPlayer p,ItemStack t,ArtifactKind k) { return WorldSafety.mine(p,t,k,pos,expected); }
    }
    record Place(BlockPos pos,BlockState state) implements WorkStep {
        public boolean apply(ServerPlayer p,ItemStack t,ArtifactKind k) { return WorldSafety.placePaid(p,t,k,pos,state); }
    }
    record Rephase(BlockPos pos,BlockState expected,BlockState target) implements WorkStep {
        public boolean apply(ServerPlayer p,ItemStack t,ArtifactKind k) { return WorldSafety.transmute(p,t,k,pos,expected,target); }
    }
    record Exchange(BlockPos pos,BlockPos other,BlockState first,BlockState second) implements WorkStep {
        public boolean apply(ServerPlayer p,ItemStack t,ArtifactKind k) { return WorldSafety.exchange(p,t,pos,other,first,second); }
    }
    record Move(BlockPos pos) implements WorkStep {
        public boolean apply(ServerPlayer p,ItemStack t,ArtifactKind k) { return WorldSafety.move(p,Vec3.atBottomCenterOf(pos).add(0,0.05,0),k); }
        public boolean stopOnFailure() { return true; }
    }
}
