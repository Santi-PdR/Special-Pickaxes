package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.server.level.ServerPlayer;

/** Semantic intents shared by keyboard input, packets and tests. No client coordinates are trusted. */
public final class RelicControl {
    public enum Action { ACTIVATE, SECONDARY, MODE, SELECT, CONFIRM, CANCEL, PAUSE }
    private RelicControl(){}
    public static boolean execute(ServerPlayer p,ArtifactKind expected,Action action){
        if(!p.isAlive()||p.isSpectator()||!(p.getMainHandItem().getItem() instanceof ArtifactItem item)||item.kind!=expected)return false;
        var k=item.kind;var tool=p.getMainHandItem();
        if(action==Action.CANCEL){
            WorkQueue.cancel(p);DomainFields.stop(p);CompanionActions.stop(p);ArtifactInteraction.clear(p);
            ArtifactFeedback.cue(p,"cancel");ArtifactFeedback.message(p,"cancelled");return true;
        }
        // Bound packet spam without preventing an emergency cancellation.
        var data=ArtifactState.of(p,k);long now=ArtifactState.now(p);
        if(data.contains("inputTick")&&now-data.getLong("inputTick")<2)return false;data.putLong("inputTick",now);
        switch(action){
            case PAUSE: return WorkQueue.busy(p)&&!WorkQueue.status(p).equals("ready")&&WorkQueue.togglePause(p);
            case CONFIRM: return WorkQueue.status(p).equals("ready")&&WorkQueue.togglePause(p);
            case SELECT:
                if(!ArtifactInteraction.regional(k,ArtifactState.mode(p,k))||WorkQueue.busy(p))return false;
                if(ArtifactInteraction.selecting(p)){ArtifactInteraction.clear(p);return true;}
                return ArtifactInteraction.use(p,tool,k,false);
            case MODE:
                if(WorkQueue.busy(p)||ArtifactInteraction.modeCount(k)<2)return false;
                if(ArtifactInteraction.selecting(p))return ArtifactInteraction.left(p,p.blockPosition(),true);
                ArtifactState.rotate(p,k);ArtifactFeedback.message(p,"named_mode",net.minecraft.network.chat.Component.translatable("mode.specialpickaxes."+ArtifactInteraction.modeKey(k,ArtifactState.mode(p,k))));ArtifactFeedback.cue(p,"select");return true;
            case SECONDARY:
                if(k==ArtifactKind.MERIDIAN||k==ArtifactKind.LODESTAR){ArtifactState.clearAnchors(p,k);data.remove("trail");ArtifactFeedback.message(p,"unlinked");return true;}
                if(DomainFields.active(p)||CompanionActions.active(p)){DomainFields.stop(p);CompanionActions.stop(p);WorkQueue.cancel(p);ArtifactFeedback.message(p,"released");return true;}
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
