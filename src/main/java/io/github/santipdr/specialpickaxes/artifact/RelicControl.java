package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.server.level.ServerPlayer;

/** Semantic intents shared by keyboard input, packets and tests. No client coordinates are trusted. */
public final class RelicControl {
    public enum Action { ACTIVATE, MODE, SELECT, CONFIRM, CANCEL, PAUSE, ALT_SKILL }
    private RelicControl(){}
    public static boolean execute(ServerPlayer p,ArtifactKind expected,Action action){
        if(!expected.playable()||p.isRemoved()||!p.isAlive()||p.isSpectator()||!(p.getMainHandItem().getItem() instanceof ArtifactItem item)||item.kind!=expected)return false;
        WorkQueue.discardStale(p);
        var k=item.kind;var tool=p.getMainHandItem();
        boolean copiedWorldbreaker=k==ArtifactKind.WORLDBREAKER&&tool.hasTag()&&tool.getTag().contains("copiedSkill");
        if(action==Action.CANCEL)return cancel(p,k);
        // Bound packet spam without preventing an emergency cancellation.
        var data=ArtifactState.of(p,k);long now=ArtifactState.now(p);
        if(data.contains("inputTick")&&data.getLong("inputTick")<=now&&now-data.getLong("inputTick")<2)return false;data.putLong("inputTick",now);
        switch(action){
            case PAUSE:
                if(!WorkQueue.busy(p)||WorkQueue.status(p).equals("ready"))return false;
                WorkQueue.togglePause(p);p.displayClientMessage(net.minecraft.network.chat.Component.translatable("status.specialpickaxes."+WorkQueue.status(p)),true);ArtifactFeedback.cue(p,"select");return true;
            case CONFIRM:
                return confirm(p);
            case SELECT:
                if(copiedWorldbreaker){
                    try{var copied=ArtifactKind.byId(tool.getTag().getString("copiedSkill"));if(ArtifactInteraction.regional(copied,ArtifactState.mode(p,copied)))return ArtifactInteraction.use(p,tool,copied,false);}
                    catch(IllegalArgumentException invalid){return false;}
                }
                if(!ArtifactInteraction.regional(k,ArtifactState.mode(p,k))||WorkQueue.busy(p))return false;
                if(ArtifactInteraction.selecting(p)){ArtifactInteraction.clear(p);ArtifactFeedback.message(p,"cancelled");ArtifactFeedback.cue(p,"cancel");return true;}
                return ArtifactInteraction.use(p,tool,k,false);
            case MODE:
                if(copiedWorldbreaker)return false;
                if(ArtifactInteraction.modeCount(k)<2)return false;
                WorkQueue.cancel(p);ArtifactInteraction.clear(p);DomainFields.stop(p);
                ArtifactState.rotate(p,k);tool.getOrCreateTag().putInt("artifactMode",ArtifactState.mode(p,k));ArtifactInteraction.display(p,tool,k);ArtifactFeedback.message(p,"named_mode",net.minecraft.network.chat.Component.translatable("mode.specialpickaxes."+ArtifactInteraction.modeKey(k,ArtifactState.mode(p,k))));ArtifactFeedback.cue(p,"select");return true;
            case ACTIVATE:
                if(WorkQueue.busy(p)){
                    var status=WorkQueue.status(p);
                    if(status.equals("paused")){
                        if(!WorkQueue.togglePause(p))return false;
                        ArtifactFeedback.message(p,"resumed");ArtifactFeedback.cue(p,"select");return true;
                    }
                    if(status.equals("ready"))return confirm(p);
                    if(!WorkQueue.togglePause(p))return false;
                    p.displayClientMessage(net.minecraft.network.chat.Component.translatable("status.specialpickaxes."+WorkQueue.status(p)),true);ArtifactFeedback.cue(p,"select");return true;
                }
                if(DomainFields.active(p)||CompanionActions.active(p))return cancel(p,k);
                if(!copiedWorldbreaker&&ArtifactInteraction.regional(k,ArtifactState.mode(p,k)))return ArtifactInteraction.use(p,tool,k,false);
                return ArtifactActions.use(p,tool,k,false);
            case ALT_SKILL:
                if(WorkQueue.busy(p)||ArtifactInteraction.selecting(p))return cancel(p,k);
                return ArtifactActions.useAlternate(p,tool,k);
            default:return false;
        }
    }
    private static boolean confirm(ServerPlayer p){
        if(!WorkQueue.status(p).equals("ready")||!WorkQueue.togglePause(p))return false;
        ArtifactFeedback.cue(p,"confirm");return true;
    }
    private static boolean cancel(ServerPlayer p,ArtifactKind kind){
        boolean active=WorkQueue.busy(p)||DomainFields.active(p)||CompanionActions.active(p)||ArtifactInteraction.selecting(p);
        if(kind==ArtifactKind.MERIDIAN||kind==ArtifactKind.LODESTAR){active|=ArtifactState.anchor(p,kind,"a").isPresent();ArtifactState.clearAnchors(p,kind);ArtifactState.of(p,kind).remove("trail");}
        WorkQueue.cancel(p);DomainFields.stop(p);CompanionActions.stop(p);ArtifactInteraction.clear(p);
        if(!active)return true;
        ArtifactFeedback.cue(p,"cancel");ArtifactFeedback.message(p,"cancelled");return true;
    }
    public static boolean corner(ServerPlayer p,net.minecraft.core.BlockPos pos){
        if(!ArtifactInteraction.left(p,pos,false))return false;
        ArtifactInteraction.analyzeComplete(p);return true;
    }
}
