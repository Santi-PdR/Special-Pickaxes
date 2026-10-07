package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import java.util.*;

/** Server-owned UX state. Vanilla interaction packets carry intent, never trusted coordinates. */
public final class ArtifactInteraction {
    private static final class Selection {
        final ItemStack tool;final ArtifactKind kind;final String dimension;final long expires;
        final List<BlockPos> points=new ArrayList<>();int transform;
        Selection(ServerPlayer p,ItemStack t,ArtifactKind k){tool=t;kind=k;dimension=ArtifactState.dimension(p);expires=ArtifactState.now(p)+12000;}
    }
    private static final Map<UUID,Selection> SELECTED=new HashMap<>();
    private ArtifactInteraction(){}
    public static boolean regional(ArtifactKind k){return k==ArtifactKind.ATLAS||k==ArtifactKind.WORLDBREAKER||k==ArtifactKind.CHRONICLE||k==ArtifactKind.CRUCIBLE||k==ArtifactKind.TESSELLATOR;}
    public static boolean regional(ArtifactKind k,int mode){return regional(k)&&(k!=ArtifactKind.WORLDBREAKER||mode!=1&&mode!=4&&mode!=6);}
    public static String[] modes(ArtifactKind k){return switch(k){
        case PALIMPSEST->new String[]{"restore"};case CHOIR->new String[]{"rotate0","rotate90","rotate180","rotate270"};
        case EVENTIDE->new String[]{"attract","repel"};case MERIDIAN->new String[]{"link"};
        case CRUCIBLE->new String[]{"stone","deepslate","basalt","obsidian"};case INTERREGNUM->new String[]{"stasis"};
        case WORLDLOOM->new String[]{"shelter","bridge"};case ICARUS->new String[]{"forward","reverse"};case AXIOM->new String[]{"ribs","subtract"};
        case ATLAS,TESSELLATOR->new String[]{"identity","mirror_x","mirror_z","rotate90","rotate180","rotate270"};
        case WORLDBREAKER->new String[]{"world_break","world_carve","world_rephase","world_transpose","world_restore","world_record","world_convergence"};
        case CHRONICLE->new String[]{"record","restore"};case KEYSTONE->new String[]{"vault","supported_vault"};
        case AEGIS->new String[]{"reflect","shear"};case LODESTAR->new String[]{"return_path"};
        case SEAM_RIPPER->new String[]{"interface","exposed_surface"};case CAUSEWAY->new String[]{"footbridge"};
        case COUNTERSEAL->new String[]{"blast_ward"};case COVENANT->new String[]{"seal_wake"};};}
    public static int modeCount(ArtifactKind k){return modes(k).length;}
    public static String modeKey(ArtifactKind k,int m){return modes(k)[Math.floorMod(m,modeCount(k))];}
    private static int corners(ServerPlayer p,ArtifactKind k){return k==ArtifactKind.ATLAS||k==ArtifactKind.TESSELLATOR||k==ArtifactKind.WORLDBREAKER&&ArtifactState.mode(p,k)==3?4:2;}
    public static void clear(ServerPlayer p){SELECTED.remove(p.getUUID());}
    public static void clear(){SELECTED.clear();}
    public static boolean selecting(ServerPlayer p){var s=SELECTED.get(p.getUUID());if(s!=null&&!valid(p,s)){clear(p);return false;}return s!=null;}
    public static boolean left(ServerPlayer p,BlockPos pos,boolean shift){
        var s=SELECTED.get(p.getUUID());
        if(s==null)return false;
        if(!valid(p,s)){clear(p);return false;}
        if(shift){
            if(corners(p,s.kind)==4){s.transform=(s.transform+1)%6;if(s.kind==ArtifactKind.ATLAS||s.kind==ArtifactKind.TESSELLATOR)ArtifactState.of(p,s.kind).putInt("mode",s.transform);ArtifactFeedback.message(p,"transform",Component.translatable("mode.specialpickaxes."+modes(ArtifactKind.ATLAS)[s.transform]));}
            else {ArtifactState.rotate(p,s.kind);if(!regional(s.kind,ArtifactState.mode(p,s.kind)))clear(p);ArtifactFeedback.message(p,"named_mode",Component.translatable("mode.specialpickaxes."+modeKey(s.kind,ArtifactState.mode(p,s.kind))));}
            ArtifactFeedback.cue(p,"select");return true;
        }
        if(!WorldSafety.allowed(p,s.kind,pos)||s.points.size()>=corners(p,s.kind)){ArtifactFeedback.cue(p,"error");ArtifactFeedback.message(p,"selection_invalid");return true;}
        s.points.add(pos.immutable());ArtifactFeedback.cue(p,"select");ArtifactFeedback.burst(p,s.kind,pos,6);
        ArtifactFeedback.message(p,s.points.size()==corners(p,s.kind)?"selection_ready":"corner",s.points.size());return true;
    }
    public static void analyzeComplete(ServerPlayer p){
        var s=SELECTED.get(p.getUUID());if(s!=null&&valid(p,s)&&s.points.size()==corners(p,s.kind)&&!WorkQueue.busy(p))use(p,s.tool,s.kind,false);
    }
    private static boolean valid(ServerPlayer p,Selection s){return p.isAlive()&&!p.isRemoved()&&p.getMainHandItem()==s.tool&&ArtifactState.dimension(p).equals(s.dimension)&&(ArtifactState.now(p)<=s.expires||WorkQueue.busy(p));}
    public static boolean use(ServerPlayer p,ItemStack tool,ArtifactKind k,boolean shift){
        if(!WorldSafety.allowed(p,k,p.blockPosition()))return false;
        if(CompanionActions.handles(k)&&CompanionActions.active(p)){CompanionActions.stop(p);WorkQueue.cancel(p);ArtifactFeedback.message(p,"cancelled");ArtifactFeedback.cue(p,"cancel");return true;}
        if(k==ArtifactKind.AEGIS&&!shift&&DomainFields.active(p)){DomainFields.stop(p);ArtifactFeedback.message(p,"released");return true;}
        if(WorkQueue.busy(p)){
            if(shift){WorkQueue.cancel(p);DomainFields.stop(p);if(k==ArtifactKind.MERIDIAN)ArtifactState.clearAnchors(p,k);clear(p);ArtifactFeedback.message(p,"cancelled");ArtifactFeedback.cue(p,"cancel");}
            else {WorkQueue.togglePause(p);ArtifactFeedback.cue(p,"confirm");}return true;
        }
        var s=SELECTED.get(p.getUUID());
        if(s!=null&&!valid(p,s)){clear(p);s=null;}
        if(shift){
            if(s!=null){clear(p);ArtifactFeedback.message(p,"cancelled");ArtifactFeedback.cue(p,"cancel");return true;}
            if(k==ArtifactKind.MERIDIAN){ArtifactState.clearAnchors(p,k);ArtifactFeedback.message(p,"unlinked");}
            else if(k==ArtifactKind.LODESTAR){ArtifactState.clearAnchors(p,k);ArtifactState.of(p,k).remove("trail");ArtifactFeedback.message(p,"unlinked");}
            else if(CompanionActions.handles(k)){CompanionActions.stop(p);ArtifactFeedback.message(p,"cancelled");}
            else if(k==ArtifactKind.INTERREGNUM){DomainFields.stop(p);ArtifactFeedback.message(p,"released");}
            else {ArtifactState.rotate(p,k);ArtifactFeedback.message(p,"named_mode",Component.translatable("mode.specialpickaxes."+modeKey(k,ArtifactState.mode(p,k))));}
            ArtifactFeedback.cue(p,"select");return true;
        }
        if(regional(k,ArtifactState.mode(p,k))){
            if(s==null){s=new Selection(p,tool,k);if(k==ArtifactKind.ATLAS||k==ArtifactKind.TESSELLATOR)s.transform=ArtifactState.mode(p,k);SELECTED.put(p.getUUID(),s);ArtifactFeedback.message(p,"armed");ArtifactFeedback.cue(p,"select");return true;}
            if(s.points.size()!=corners(p,k)){ArtifactFeedback.message(p,"need_corners",s.points.size(),corners(p,k));ArtifactFeedback.cue(p,"error");return false;}
            try {
                var a=new SelectionVolume(s.points.get(0),s.points.get(1));var b=s.points.size()==4?new SelectionVolume(s.points.get(2),s.points.get(3)):null;
                if(a.size()>ArtifactConfig.REGION_LIMIT.get()){ArtifactFeedback.message(p,"volume_limit",a.size(),ArtifactConfig.REGION_LIMIT.get());ArtifactFeedback.cue(p,"error");return false;}
                if(b!=null&&a.overlaps(b)){ArtifactFeedback.message(p,"overlap");ArtifactFeedback.cue(p,"error");return false;}
                if(b!=null&&!SelectionVolume.Transform.values()[s.transform].compatible(a,b)){ArtifactFeedback.message(p,"incompatible",a.dimensions(),b.dimensions());ArtifactFeedback.cue(p,"error");return false;}
                var material=p.getOffhandItem().getItem() instanceof BlockItem item?item.getBlock().defaultBlockState():Blocks.STONE.defaultBlockState();
                if(k==ArtifactKind.CRUCIBLE)material=switch(ArtifactState.mode(p,k)){case 1->Blocks.DEEPSLATE.defaultBlockState();case 2->Blocks.BASALT.defaultBlockState();case 3->Blocks.OBSIDIAN.defaultBlockState();default->Blocks.STONE.defaultBlockState();};
                var region=new RegionWork(a,b,SelectionVolume.Transform.values()[s.transform],k,ArtifactState.mode(p,k),material);
                if(!WorkQueue.startRegion(p,tool,k,region)){ArtifactFeedback.message(p,"backpressure");return false;}
                // Keep selection visible while analyzing/executing. No world edits until second confirmation after analysis.
                ArtifactFeedback.cue(p,"confirm");return true;
            }catch(IllegalArgumentException invalid){ArtifactFeedback.message(p,"selection_invalid");ArtifactFeedback.cue(p,"error");return false;}
        }
        return ArtifactActions.use(p,tool,k,false);
    }

    public static void display(ServerPlayer p,ItemStack tool,ArtifactKind k){
        var s=SELECTED.get(p.getUUID());if(s!=null&&!valid(p,s)){clear(p);s=null;}
        String status=WorkQueue.busy(p)?WorkQueue.status(p):s!=null?(s.points.size()==corners(p,k)?"selected":"selecting"):"idle";
        if(!WorkQueue.busy(p)&&(DomainFields.active(p)||CompanionActions.active(p)))status="domain";
        var tag=tool.getOrCreateTag();if(!status.equals(tag.getString("artifactStatus"))&&status.equals("ready")){p.displayClientMessage(Component.translatable("status.specialpickaxes.ready"),true);ArtifactFeedback.cue(p,"select");}tag.putString("artifactStatus",status);tag.putInt("artifactProgress",WorkQueue.completed(p));
        tag.putString("artifactModeName",modeKey(k,ArtifactState.mode(p,k)));
        tag.remove("artifactSource");tag.remove("artifactTarget");tag.remove("artifactTransform");
        if(s!=null&&corners(p,k)==4){tag.putString("artifactTransform",modes(ArtifactKind.ATLAS)[s.transform]);if(k==ArtifactKind.ATLAS||k==ArtifactKind.TESSELLATOR)tag.putInt("artifactMode",s.transform);}
        for(int i=0;i<4;i++)tag.remove("selection"+i);tag.remove("selectionDimension");
        if(s!=null){
            tag.putString("selectionDimension",s.dimension);for(int i=0;i<s.points.size();i++)tag.putLong("selection"+i,s.points.get(i).asLong());
            for(var point:s.points)ArtifactFeedback.burst(p,k,point,2);
            if(s.points.size()>=2){var a=new SelectionVolume(s.points.get(0),s.points.get(1));ArtifactFeedback.box(p,k,a,false);ArtifactFeedback.shape(p,k,a,ArtifactState.mode(p,k));tag.putString("artifactSource",a.dimensions());
                if(s.points.size()==4){var b=new SelectionVolume(s.points.get(2),s.points.get(3));ArtifactFeedback.box(p,k,b,true);tag.putString("artifactTarget",b.dimensions());if(SelectionVolume.Transform.values()[s.transform].compatible(a,b))ArtifactFeedback.trace(p,k,a.min(),SelectionVolume.Transform.values()[s.transform].map(a,b,a.min()));}}
        }
        if(k==ArtifactKind.MERIDIAN){var a=ArtifactState.anchor(p,k,"a");var b=ArtifactState.anchor(p,k,"b");if(a.isPresent()){
            tag.putString("artifactStatus",b.isPresent()?"linked":"anchor_b");tag.putString("artifactSource",a.get().toShortString());ArtifactFeedback.burst(p,k,a.get(),3);
            if(b.isPresent()){tag.putString("artifactTarget",b.get().toShortString());tag.putInt("artifactRotation",ArtifactState.of(p,k).getInt("rotation")*90);tag.putInt("artifactDistance",(int)Math.round(Math.sqrt(a.get().distSqr(b.get()))));ArtifactFeedback.trace(p,k,a.get(),b.get());ArtifactFeedback.burst(p,ArtifactKind.PALIMPSEST,b.get(),3);}
        }}
        var memory=ArtifactState.of(p,k).getList("memory",net.minecraft.nbt.Tag.TAG_COMPOUND);
        tag.putLong("artifactOldest",memory.isEmpty()?0:ArtifactState.age(p,memory.getCompound(0))/1200);tag.putLong("artifactLatest",memory.isEmpty()?0:ArtifactState.age(p,memory.getCompound(memory.size()-1))/1200);
    }
}
