package io.github.santipdr.specialpickaxes.artifact;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import java.util.*;
import java.util.function.Supplier;

/** Explicit equipped mining command. Never swaps the equipped stack into the player's hand. */
public final class CuriosMiningActive {
    private static final Set<UUID> STARTING=new HashSet<>();
    private CuriosMiningActive(){}
    public static boolean activate(ServerPlayer p,ItemStack source,Supplier<ItemStack> current){
        if(!STARTING.add(p.getUUID()))return false;
        try{
            if(!p.isAlive()||p.isRemoved()||p.isSpectator()||!p.mayBuild()||source.isEmpty()||current.get()!=source||WorkQueue.busy(p)||IcarianFlight.active(p))return false;
            var kind=WorldbreakerCuriosPassives.sourceKind(source);
            if(kind==null||!kind.playable()){
                ArtifactFeedback.message(p,"curios_active_unavailable");return false;
            }
            var held=p.getMainHandItem();if(held.isEmpty())return false;
            var state=ArtifactState.of(p,kind);long now=ArtifactState.now(p);
            if(PersistedCooldown.active(now,state.getLong("curiosActiveReady"),160)){ArtifactFeedback.message(p,"alternate_cooldown",Math.max(1,(state.getLong("curiosActiveReady")-now+19)/20));return false;}
            var skill=HeldSkillLease.capture(source);
            var lease=new EquipmentLease<>(source,()->current.get()==source&&skill.valid()&&WorldbreakerCuriosPassives.sourceKind(source)==kind?source:ItemStack.EMPTY);
            if(!ArtifactActions.canSpendDurability(p,source,8))return false;
            if(kind==ArtifactKind.ICARUS||kind==ArtifactKind.PALIMPSEST||kind==ArtifactKind.IRIDIUM){
                boolean started=kind==ArtifactKind.ICARUS?IcarianFlight.startEquipped(p,held,lease):EquippedLootPulse.activate(p,held,kind,lease);
                if(!started)return false;
                state.putLong("curiosActiveReady",now+160);source.hurtAndBreak(8,p,who->{});AbilityVfx.cast(p,kind,true);return true;
            }
            var context=HeldUtilityContext.capture(p,held);var aim=p.getLookAngle();var target=ConnectedExcavation.closeTarget(p);if(target.isEmpty())return false;var at=target.get();var level=p.serverLevel();
            if(!level.hasChunkAt(at)||!NaturalExcavationProgram.eligible(level.getBlockState(at))||PlayerPlacedBlocks.get(level).contains(at)||!WorldSafety.harvestable(p,held,at))return false;
            if(!WorldSafety.allowed(p,kind,at)||!context.valid()||!p.getLookAngle().equals(aim)||!lease.active()||!level.hasChunkAt(at)
                    ||!NaturalExcavationProgram.eligible(level.getBlockState(at))||PlayerPlacedBlocks.get(level).contains(at)||!WorldSafety.harvestable(p,held,at)||!ArtifactActions.canSpendDurability(p,source,8))return false;
            WorkProgram program=kind==ArtifactKind.SEAM_RIPPER?new EquippedSeamSlice(at,net.minecraft.core.Direction.getNearest(p.getLookAngle().x,p.getLookAngle().y,p.getLookAngle().z),lease):new ConnectedExcavation(p,at,kind,true,lease);
            if(!WorkQueue.startRegion(p,held,kind,program))return false;
            state.putLong("curiosActiveReady",now+160);
            source.hurtAndBreak(8,p,who->{});AbilityVfx.cast(p,kind,true);return true;
        }finally{STARTING.remove(p.getUUID());}
    }
}
