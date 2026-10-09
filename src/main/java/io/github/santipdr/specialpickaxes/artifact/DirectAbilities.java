package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import java.util.*;

/** Small direct programs. They share the existing mutation boundary and scheduler, not a second editor. */
public final class DirectAbilities {
    private DirectAbilities() {}
    public static boolean handles(ArtifactKind k){return k==ArtifactKind.KEYSTONE||k==ArtifactKind.WORLDBREAKER||k==ArtifactKind.AEGIS||k==ArtifactKind.LODESTAR||k==ArtifactKind.SEAM_RIPPER||CompanionActions.handles(k);}
    public static boolean activate(ServerPlayer p,ItemStack tool,ArtifactKind k){
        if(CompanionActions.handles(k))return CompanionActions.start(p,tool,k);
        if(k==ArtifactKind.LODESTAR)return returnPath(p,tool);
        var center=ArtifactActions.target(p).orElse(p.blockPosition());
        if(!WorldSafety.allowed(p,k,center))return false;
        if(k==ArtifactKind.AEGIS){int radius=Math.min(8,ArtifactConfig.MAX_RADIUS.get());DomainFields.start(p,tool,k,center,radius);ArtifactFeedback.ring(p,k,center,radius);return true;}
        List<WorkStep> steps=switch(k){
            case KEYSTONE->vault(p,center.above());
            case SEAM_RIPPER->seam(p,center);
            case WORLDBREAKER->ArtifactState.mode(p,k)==6?convergence(p,center):ArtifactState.mode(p,k)==1?cleave(p,center):ArtifactState.mode(p,k)==4?restore(p,k,center):List.of();
            default->List.of();
        };
        if(WorkQueue.start(p,tool,k,steps)){ArtifactFeedback.preview(p,k,steps);return true;}return false;
    }
    /** Supreme program: fracture an annular quarry without touching its core, then erect a paid sanctuary. */
    public static List<WorkStep> convergence(ServerPlayer p,BlockPos center){
        var result=new ArrayList<WorkStep>();int radius=Math.min(12,ArtifactConfig.MAX_RADIUS.get()+4);
        for(int y=4;y>=0;y--)for(int x=-radius;x<=radius;x++)for(int z=-radius;z<=radius;z++){
            int r=x*x+z*z;if(r<25||r>radius*radius)continue;var at=center.offset(x,y,z);
            if(WorldSafety.allowed(p,ArtifactKind.WORLDBREAKER,at)&&WorldSafety.inert(p.serverLevel().getBlockState(at)))result.add(new WorkStep.Mine(at,p.serverLevel().getBlockState(at)));
        }
        result.addAll(vault(p,center.above(),true));return result;
    }
    public static BlockState material(ServerPlayer p){
        var off=p.getOffhandItem();return !off.hasTag()&&off.getItem() instanceof BlockItem b&&WorldSafety.inert(b.getBlock().defaultBlockState())?b.getBlock().defaultBlockState():Blocks.STONE.defaultBlockState();
    }
    private static List<WorkStep> restore(ServerPlayer p,ArtifactKind k,BlockPos center){
        var result=new ArrayList<WorkStep>();for(var m:ArtifactState.memories(p,k))if(m.pos().distSqr(center)<=24*24)result.add(new WorkStep.Place(m.pos(),m.state()));return result;
    }
    public static List<WorkStep> vault(ServerPlayer p,BlockPos origin){return vault(p,origin,ArtifactState.mode(p,ArtifactKind.KEYSTONE)==1);}
    private static List<WorkStep> vault(ServerPlayer p,BlockPos origin,boolean supports){
        var result=new ArrayList<WorkStep>();var forward=p.getDirection();var side=forward.getClockWise();var material=material(p);
        for(int z=0;z<5;z++)for(int x=-4;x<=4;x++){
            int y=(int)Math.round(4*(1-Math.pow(x/4.5,2)));
            result.add(new WorkStep.Place(origin.relative(forward,z).relative(side,x).above(y),material));
            if(supports&&Math.abs(x)==4&&z%2==0)
                for(int support=0;support<y;support++)result.add(new WorkStep.Place(origin.relative(forward,z).relative(side,x).above(support),material));
        }return result;
    }
    /** Stationary directional cleavage; unlike Icarus this never moves its owner. */
    public static List<WorkStep> cleave(ServerPlayer p,BlockPos origin){
        var result=new ArrayList<WorkStep>();var look=p.getLookAngle();var forward=Direction.getNearest(look.x,look.y,look.z);
        int length=Math.min(64,ArtifactConfig.BORE_LENGTH.get()*2);
        for(int n=0;n<length;n++)for(int u=-2;u<=2;u++)for(int v=-2;v<=2;v++){
            if(Math.abs(u)+Math.abs(v)>2)continue;var at=origin.relative(forward,n);
            at=switch(forward.getAxis()){case X->at.offset(0,u,v);case Y->at.offset(u,0,v);case Z->at.offset(u,v,0);};
            if(p.serverLevel().hasChunkAt(at))result.add(new WorkStep.Mine(at,p.serverLevel().getBlockState(at)));
        }return result;
    }
    /** Bounded flood of the interface only, not a vein or arbitrary cuboid. */
    public static List<WorkStep> seam(ServerPlayer p,BlockPos seed){
        var level=p.serverLevel();if(!level.hasChunkAt(seed))return List.of();var source=level.getBlockState(seed);
        if(!WorldSafety.inert(source))return List.of();
        boolean exposed=ArtifactState.mode(p,ArtifactKind.SEAM_RIPPER)==1;
        var neighbor=p.getOffhandItem().getItem() instanceof BlockItem b?b.getBlock():Blocks.AIR;
        if(!exposed&&(neighbor==Blocks.AIR||source.is(neighbor)))return List.of();
        var pending=new ArrayDeque<BlockPos>();var visited=new HashSet<BlockPos>();var result=new ArrayList<WorkStep>();pending.add(seed);visited.add(seed);
        while(!pending.isEmpty()&&visited.size()<=512&&result.size()<256){
            var at=pending.removeFirst();if(!WorldSafety.allowed(p,ArtifactKind.SEAM_RIPPER,at)||level.getBlockState(at)!=source)continue;
            boolean border=false;
            for(var d:Direction.values()){var q=at.relative(d);if(level.hasChunkAt(q)&&(exposed?level.getBlockState(q).isAir():level.getBlockState(q).is(neighbor))){border=true;break;}}
            if(!border)continue;result.add(new WorkStep.Mine(at,source));
            for(var d:Direction.values()){var q=at.relative(d);if(visited.size()<512&&visited.add(q))pending.addLast(q);}
        }return result;
    }
    /** Counter-seam cuts the offhand-selected material from the opposite side of the same interface. */
    public static boolean counterSeam(ServerPlayer p,ItemStack tool){
        var target=ArtifactActions.target(p);if(target.isEmpty())return false;
        var level=p.serverLevel();var seed=target.get();if(!level.hasChunkAt(seed))return false;
        var exposed=level.getBlockState(seed);if(!WorldSafety.inert(exposed))return false;
        var offhand=p.getOffhandItem();
        if(!(offhand.getItem() instanceof BlockItem item))return false;
        var seamBlock=item.getBlock();var seamState=seamBlock.defaultBlockState();
        if(seamBlock==exposed.getBlock()||!WorldSafety.inert(seamState))return false;
        var frontier=new ArrayDeque<BlockPos>();var visited=new HashSet<BlockPos>();
        for(var direction:Direction.values()){
            var adjacent=seed.relative(direction);
            if(level.hasChunkAt(adjacent)&&level.getBlockState(adjacent)==seamState&&visited.add(adjacent))frontier.addLast(adjacent);
        }
        var steps=new ArrayList<WorkStep>(64);int examined=0;
        while(!frontier.isEmpty()&&examined<512&&steps.size()<256){
            var at=frontier.removeFirst();examined++;
            if(!WorldSafety.allowed(p,ArtifactKind.SEAM_RIPPER,at)||level.getBlockState(at)!=seamState)continue;
            boolean touchesTarget=false;
            for(var direction:Direction.values()){
                var adjacent=at.relative(direction);
                if(level.hasChunkAt(adjacent)&&level.getBlockState(adjacent)==exposed){touchesTarget=true;break;}
            }
            if(touchesTarget)steps.add(new WorkStep.Mine(at,seamState));
            for(var direction:Direction.values()){
                var adjacent=at.relative(direction);
                if(visited.size()<512&&level.hasChunkAt(adjacent)&&level.getBlockState(adjacent)==seamState&&visited.add(adjacent))frontier.addLast(adjacent);
            }
        }
        boolean started=WorkQueue.start(p,tool,ArtifactKind.SEAM_RIPPER,steps);
        if(started)ArtifactFeedback.preview(p,ArtifactKind.SEAM_RIPPER,steps);
        return started;
    }
    private static boolean returnPath(ServerPlayer p,ItemStack tool){
        var k=ArtifactKind.LODESTAR;var data=ArtifactState.of(p,k);
        if(ArtifactState.anchor(p,k,"a").isEmpty()){
            ArtifactState.anchor(p,k,"a",p.blockPosition());data.put("trail",new ListTag());recordFootstep(p);ArtifactFeedback.message(p,"trail_marked");return true;
        }
        recordFootstep(p);var trail=data.getList("trail",Tag.TAG_LONG);var steps=new ArrayList<WorkStep>();
        for(int i=trail.size()-1;i>=0;i--)steps.add(new WorkStep.Move(BlockPos.of(((LongTag)trail.get(i)).getAsLong())));
        boolean started=WorkQueue.start(p,tool,k,steps);
        if(started){ArtifactState.clearAnchors(p,k);data.remove("trail");}return started;
    }
    public static void recordFootstep(ServerPlayer p){
        if(WorkQueue.busy(p)||ArtifactState.anchor(p,ArtifactKind.LODESTAR,"a").isEmpty())return;
        var data=ArtifactState.of(p,ArtifactKind.LODESTAR);var trail=data.getList("trail",Tag.TAG_LONG);long pos=p.blockPosition().asLong();
        if(trail.isEmpty()||((LongTag)trail.get(trail.size()-1)).getAsLong()!=pos){
            // Keep the most recent route. No jump over an omitted section is ever allowed by Move.
            if(trail.size()>=128){trail.remove(0);ArtifactState.anchor(p,ArtifactKind.LODESTAR,"a",BlockPos.of(((LongTag)trail.get(0)).getAsLong()));}trail.add(LongTag.valueOf(pos));data.put("trail",trail);
        }
    }
    public static void preview(ServerPlayer p,ArtifactKind k){
        var target=ArtifactActions.target(p).orElse(p.blockPosition());
        if(k==ArtifactKind.PALIMPSEST||k==ArtifactKind.WORLDBREAKER&&ArtifactState.mode(p,k)==4)
            ArtifactFeedback.shape(p,k,new SelectionVolume(target.offset(-16,-16,-16),target.offset(16,16,16)),k==ArtifactKind.WORLDBREAKER?4:0);
        else if(k==ArtifactKind.KEYSTONE)ArtifactFeedback.preview(p,k,vault(p,target.above()));
        else if(k==ArtifactKind.CHOIR)ArtifactFeedback.preview(p,k,ArtifactActions.echo(p,target));
        else if(k==ArtifactKind.LODESTAR){recordFootstep(p);ArtifactState.anchor(p,k,"a").ifPresent(a->ArtifactFeedback.trace(p,k,p.blockPosition(),a));}
        else if(k==ArtifactKind.SEAM_RIPPER)ArtifactFeedback.burst(p,k,target,2);
    }
}
