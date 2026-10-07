package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.server.level.ServerPlayer;

/** Semantic intents shared by keyboard input, packets and tests. No client coordinates are trusted. */
public final class RelicControl {
    public enum Action { ACTIVATE, SECONDARY, MODE, SELECT, CONFIRM, CANCEL, PAUSE }
    private RelicControl(){}
    public static boolean execute(ServerPlayer p,ArtifactKind expected,Action action){
        if(!expected.playable()||!p.isAlive()||p.isSpectator()||!(p.getMainHandItem().getItem() instanceof ArtifactItem item)||item.kind!=expected)return false;
        WorkQueue.discardStale(p);
        var k=item.kind;var tool=p.getMainHandItem();
        if(action==Action.CANCEL){
            boolean active=WorkQueue.busy(p)||DomainFields.active(p)||CompanionActions.active(p)||ArtifactInteraction.selecting(p);
            if(k==ArtifactKind.MERIDIAN||k==ArtifactKind.LODESTAR){active|=ArtifactState.anchor(p,k,"a").isPresent();ArtifactState.clearAnchors(p,k);ArtifactState.of(p,k).remove("trail");}
            WorkQueue.cancel(p);DomainFields.stop(p);CompanionActions.stop(p);ArtifactInteraction.clear(p);
            if(!active)return true;
            ArtifactFeedback.cue(p,"cancel");ArtifactFeedback.message(p,"cancelled");return true;
        }
        // Bound packet spam without preventing an emergency cancellation.
        var data=ArtifactState.of(p,k);long now=ArtifactState.now(p);
        if(data.contains("inputTick")&&data.getLong("inputTick")<=now&&now-data.getLong("inputTick")<2)return false;data.putLong("inputTick",now);
        switch(action){
            case PAUSE:
                if(!WorkQueue.busy(p)||WorkQueue.status(p).equals("ready"))return false;
                WorkQueue.togglePause(p);p.displayClientMessage(net.minecraft.network.chat.Component.translatable("status.specialpickaxes."+WorkQueue.status(p)),true);ArtifactFeedback.cue(p,"select");return true;
            case CONFIRM:
                if(!WorkQueue.status(p).equals("ready")){ArtifactFeedback.message(p,"not_ready");ArtifactFeedback.cue(p,"error");return false;}
                if(!WorkQueue.togglePause(p))return false;ArtifactFeedback.cue(p,"confirm");return true;
            case SELECT:
                if(!ArtifactInteraction.regional(k,ArtifactState.mode(p,k))||WorkQueue.busy(p))return false;
                if(ArtifactInteraction.selecting(p)){ArtifactInteraction.clear(p);ArtifactFeedback.message(p,"cancelled");ArtifactFeedback.cue(p,"cancel");return true;}
                return ArtifactInteraction.use(p,tool,k,false);
            case MODE:
                if(ArtifactInteraction.modeCount(k)<2)return false;
                WorkQueue.cancel(p);ArtifactInteraction.clear(p);DomainFields.stop(p);
                ArtifactState.rotate(p,k);tool.getOrCreateTag().putInt("artifactMode",ArtifactState.mode(p,k));ArtifactInteraction.display(p,tool,k);ArtifactFeedback.message(p,"named_mode",net.minecraft.network.chat.Component.translatable("mode.specialpickaxes."+ArtifactInteraction.modeKey(k,ArtifactState.mode(p,k))));ArtifactFeedback.cue(p,"select");return true;
            case SECONDARY:
                if(k==ArtifactKind.MERIDIAN||k==ArtifactKind.LODESTAR){ArtifactState.clearAnchors(p,k);data.remove("trail");ArtifactFeedback.message(p,"unlinked");ArtifactFeedback.cue(p,"cancel");return true;}
                if(DomainFields.active(p)||CompanionActions.active(p)){DomainFields.stop(p);CompanionActions.stop(p);WorkQueue.cancel(p);ArtifactFeedback.message(p,"released");ArtifactFeedback.cue(p,"complete");return true;}
                return false;
            case ACTIVATE:
                if(ArtifactInteraction.regional(k,ArtifactState.mode(p,k))){ArtifactFeedback.message(p,"select_key");return false;}
                if(WorkQueue.busy(p))return false;
                return ArtifactActions.use(p,tool,k,false);
            default:return false;
        }
    }
    public static boolean corner(ServerPlayer p,net.minecraft.core.BlockPos pos){
        if(!ArtifactInteraction.left(p,pos,false))return false;
        ArtifactInteraction.analyzeComplete(p);return true;
    }
}
