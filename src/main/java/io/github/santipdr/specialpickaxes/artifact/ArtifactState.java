package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import java.util.*;

/** Bounded player-owned records. Never stores block-entity contents or generated loot. */
public final class ArtifactState {
    public static final String ROOT = "siege_artifacts_v2";
    public record Memory(BlockPos pos, BlockState state) {}
    private ArtifactState() {}
    public static long now(ServerPlayer p) { return p.server.overworld().getGameTime(); }
    public static CompoundTag root(ServerPlayer p) {
        if (!p.getPersistentData().contains(ROOT)) p.getPersistentData().put(ROOT,new CompoundTag());
        return p.getPersistentData().getCompound(ROOT);
    }
    public static CompoundTag of(ServerPlayer p, ArtifactKind kind) {
        var root=root(p);
        if (!root.contains(kind.id)) root.put(kind.id,new CompoundTag());
        return root.getCompound(kind.id);
    }
    public static String dimension(ServerPlayer p) { return p.level().dimension().location().toString(); }
    public static int charge(ServerPlayer p, ArtifactKind kind) { return Math.max(0,Math.min(256,of(p,kind).getInt("charge"))); }
    public static void charge(ServerPlayer p, ArtifactKind kind, int amount) { of(p,kind).putInt("charge",Math.min(256,charge(p,kind)+amount)); }
    public static int mode(ServerPlayer p, ArtifactKind kind) { return Math.floorMod(of(p,kind).getInt("mode"),4); }
    public static int rotate(ServerPlayer p, ArtifactKind kind) { int m=(mode(p,kind)+1)%4;of(p,kind).putInt("mode",m);return m; }
    public static void record(ServerPlayer p, ArtifactKind kind, BlockPos pos, BlockState state) {
        // Both memories use only default inert states; no NBT/powered/fluid/machine reconstruction.
        if (!WorldSafety.inert(state)) return;
        var data=of(p,kind);
        ListTag list=data.getList("memory",Tag.TAG_COMPOUND);
        for (int i=list.size()-1;i>=0;i--) {
            var old=list.getCompound(i);
            if (old.getLong("pos")==pos.asLong() || !old.getString("dim").equals(dimension(p))
                    || now(p)-old.getLong("time")>ArtifactConfig.MEMORY_TTL.get()) list.remove(i);
        }
        while(list.size()>=ArtifactConfig.MEMORY.get()) list.remove(0);
        var entry=new CompoundTag();entry.putLong("pos",pos.asLong());
        entry.putString("block",Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(state.getBlock())).toString());
        entry.putString("dim",dimension(p));entry.putLong("time",now(p));list.add(entry);data.put("memory",list);
    }
    public static List<Memory> memories(ServerPlayer p, ArtifactKind kind) {
        var list=of(p,kind).getList("memory",Tag.TAG_COMPOUND);var result=new ArrayList<Memory>();
        for (int i=Math.max(0,list.size()-ArtifactConfig.MEMORY.get());i<list.size();i++) {
            var tag=list.getCompound(i);
            if(!tag.getString("dim").equals(dimension(p)) || now(p)-tag.getLong("time")>ArtifactConfig.MEMORY_TTL.get()) continue;
            var id=ResourceLocation.tryParse(tag.getString("block"));
            var block=id==null?null:ForgeRegistries.BLOCKS.getValue(id);
            if(block!=null && WorldSafety.inert(block.defaultBlockState())) result.add(new Memory(BlockPos.of(tag.getLong("pos")),block.defaultBlockState()));
        }
        return List.copyOf(result);
    }
    public static void anchor(ServerPlayer p,ArtifactKind kind,String key,BlockPos pos) {
        var data=of(p,kind);data.putLong(key,pos.asLong());data.putString("dimension",dimension(p));data.putLong("anchorTime",now(p));
    }
    public static Optional<BlockPos> anchor(ServerPlayer p,ArtifactKind kind,String key) {
        var data=of(p,kind);
        return data.contains(key) && data.getString("dimension").equals(dimension(p))
            && now(p)-data.getLong("anchorTime")<=ArtifactConfig.MEMORY_TTL.get()
            ?Optional.of(BlockPos.of(data.getLong(key))):Optional.empty();
    }
    public static void clearAnchors(ServerPlayer p,ArtifactKind kind) { var d=of(p,kind);d.remove("a");d.remove("b"); }
}
