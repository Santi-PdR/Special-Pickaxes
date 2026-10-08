package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.*;
import java.util.*;

/** Pure bounded geometry, independently testable and shared by previews/work plans. */
public final class Geometry {
    private Geometry() {}
    public static List<BlockPos> cube(BlockPos center,int radius,int limit) {
        if(radius<0 || radius>12 || limit<1) return List.of();
        var all=new ArrayList<BlockPos>();
        for(int x=-radius;x<=radius;x++) for(int y=-radius;y<=radius;y++) for(int z=-radius;z<=radius;z++) all.add(center.offset(x,y,z));
        all.sort(Comparator.comparingDouble(center::distSqr));
        return List.copyOf(all.subList(0,Math.min(limit,all.size())));
    }
    public static BlockPos rotate(BlockPos offset,int turns) {
        return switch(Math.floorMod(turns,4)) {
            case 1 -> new BlockPos(-offset.getZ(),offset.getY(),offset.getX());
            case 2 -> new BlockPos(-offset.getX(),offset.getY(),-offset.getZ());
            case 3 -> new BlockPos(offset.getZ(),offset.getY(),-offset.getX());
            default -> offset;
        };
    }
    public static List<BlockPos> section(BlockPos center,Direction direction) {
        var out=new ArrayList<BlockPos>();
        for(int a=-1;a<=1;a++) for(int b=-1;b<=1;b++) out.add(switch(direction.getAxis()) {
            case X -> center.offset(0,a,b); case Y -> center.offset(a,0,b); case Z -> center.offset(a,b,0);
        });
        return out;
    }
}
