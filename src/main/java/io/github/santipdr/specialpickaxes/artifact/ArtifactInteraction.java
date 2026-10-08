package io.github.santipdr.specialpickaxes.artifact;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import java.util.*;

/** Region selections are owned by one exact stack and mode. */
public final class ArtifactInteraction {
    private static final class Selection {
        final ItemStack tool;final ArtifactKind kind;final String dimension;final int mode;final long expires;
        final List<BlockPos> points=new ArrayList<>(2);boolean submitted;
        Selection(ServerPlayer p,ItemStack t,ArtifactKind k){tool=t;kind=k;mode=ArtifactState.mode(p,k);dimension=ArtifactState.dimension(p);expires=ArtifactState.now(p)+12000;}
    }
    private static final Map<UUID,Selection> SELECTED=new HashMap<>();
    private ArtifactInteraction(){}
    public static boolean regional(ArtifactKind k){return k==ArtifactKind.CRUCIBLE;}
    public static boolean regional(ArtifactKind k,int mode){return k==ArtifactKind.CRUCIBLE||k==ArtifactKind.WORLDBREAKER&&mode==5;}
    public static String[] modes(ArtifactKind k){return switch(k){
        case PALIMPSEST->new String[]{"vein"};case CHOIR->new String[]{"forward"};
        case EVENTIDE->new String[]{"gravity_in","gravity_out"};case CRUCIBLE->new String[]{"stone","deepslate","granite","diorite","andesite","dirt","basalt","obsidian","calcite","tuff","dripstone","gravel"};
        case INTERREGNUM->new String[]{"domain","aura"};case WORLDLOOM->new String[]{"quarry"};
        case ICARUS->new String[]{"forward","wide"};case AXIOM->new String[]{"hollow_pulse"};
        case EXODIUM->new String[]{"starfall"};case IRIDIUM->new String[]{"orefall"};case HELLSPEC->new String[]{"hellforge"};
        case WORLDBREAKER->new String[]{"carve","fracture","cleave","core_drill","world_shatter","region_break"};
        default->new String[]{"retired"};};}
    public static int modeCount(ArtifactKind k){return modes(k).length;}
    public static String modeKey(ArtifactKind k,int mode){return modes(k)[Math.floorMod(mode,modeCount(k))];}
    public static void clear(ServerPlayer p){SELECTED.remove(p.getUUID());}
    public static void clear(){SELECTED.clear();}
    private static boolean valid(ServerPlayer p,Selection s){return p.isAlive()&&!p.isRemoved()&&p.getMainHandItem()==s.tool&&ArtifactState.mode(p,s.kind)==s.mode&&ArtifactState.dimension(p).equals(s.dimension)&&(ArtifactState.now(p)<=s.expires||WorkQueue.busy(p));}
    private static Selection selection(ServerPlayer p){var s=SELECTED.get(p.getUUID());if(s!=null&&!valid(p,s)){clear(p);return null;}return s;}
    public static boolean selecting(ServerPlayer p){return selection(p)!=null;}
    public static int cornerCount(ServerPlayer p){var s=selection(p);return s==null?0:s.points.size();}
    public static boolean left(ServerPlayer p,BlockPos pos,boolean ignoredShift){
        var s=selection(p);if(s==null)return false;
        // Consume even invalid/duplicate clicks: never fall through to normal mining while selecting.
        if(s.submitted||s.points.size()==2)return true;
        if(s.points.contains(pos)){ArtifactFeedback.message(p,"duplicate_corner");return true;}
        if(!WorldSafety.allowed(p,s.kind,pos)||p.distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(pos))>32*32){ArtifactFeedback.message(p,"selection_invalid");return true;}
        s.points.add(pos.immutable());ArtifactFeedback.cue(p,"select");ArtifactFeedback.message(p,"corner",s.points.size());return true;
    }
    public static void analyzeComplete(ServerPlayer p){
        var s=selection(p);if(s==null||s.submitted||s.points.size()!=2||WorkQueue.busy(p))return;
        try{
            var volume=new SelectionVolume(s.points.get(0),s.points.get(1));
            var material=s.kind==ArtifactKind.CRUCIBLE?ArtifactActions.geologyMaterial(p,s.mode):net.minecraft.world.level.block.Blocks.STONE.defaultBlockState();
            var program=new RegionWork(volume,null,SelectionVolume.Transform.IDENTITY,s.kind,s.mode,material);
            if(WorkQueue.startRegion(p,s.tool,s.kind,program)){s.submitted=true;ArtifactFeedback.message(p,"selection_ready");}
        }catch(IllegalArgumentException invalid){ArtifactFeedback.message(p,"selection_invalid");}
    }
    public static boolean use(ServerPlayer p,ItemStack tool,ArtifactKind kind,boolean secondary){
        if(!kind.playable()||!WorldSafety.allowed(p,kind,p.blockPosition()))return false;
        if(secondary){WorkQueue.cancel(p);DomainFields.stop(p);clear(p);return true;}
        if(!regional(kind,ArtifactState.mode(p,kind)))return ArtifactActions.use(p,tool,kind,false);
        if(WorkQueue.busy(p))return false;
        if(selection(p)==null){SELECTED.put(p.getUUID(),new Selection(p,tool,kind));ArtifactFeedback.message(p,"armed");return true;}
        analyzeComplete(p);return true;
    }
    public static void display(ServerPlayer p,ItemStack tool,ArtifactKind kind){
        var s=selection(p);String status=WorkQueue.busy(p)?WorkQueue.status(p):s!=null?"selecting":DomainFields.active(p)?"domain":"idle";
        var tag=tool.getOrCreateTag();if(!status.equals(tag.getString("artifactStatus"))&&status.equals("ready")){p.displayClientMessage(Component.translatable("status.specialpickaxes.ready"),true);ArtifactFeedback.cue(p,"select");}
        tag.putString("artifactStatus",status);tag.putInt("artifactProgress",WorkQueue.completed(p));tag.putString("artifactModeName",modeKey(kind,ArtifactState.mode(p,kind)));
        for(int i=0;i<4;i++)tag.remove("selection"+i);tag.remove("selectionDimension");tag.remove("artifactSource");tag.remove("artifactTarget");tag.remove("artifactTransform");
        if(s!=null){tag.putString("selectionDimension",s.dimension);for(int i=0;i<s.points.size();i++)tag.putLong("selection"+i,s.points.get(i).asLong());
            if(s.points.size()==2)ArtifactFeedback.box(p,kind,new SelectionVolume(s.points.get(0),s.points.get(1)),false);
        }
    }
}
