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
            if(!p.isAlive()||p.isRemoved()||p.isSpectator()||!p.mayBuild()||source.isEmpty()||current.get()!=source||WorkQueue.busy(p))return false;
            var kind=WorldbreakerCuriosPassives.sourceKind(source);
            if(kind==null||!switch(kind){case WORLDBREAKER,AXIOM,WORLDLOOM,EXODIUM,HELLSPEC->true;default->false;}){
                ArtifactFeedback.message(p,"curios_active_unavailable");return false;
            }
            var held=p.getMainHandItem();if(held.isEmpty())return false;
            var state=ArtifactState.of(p,kind);long now=ArtifactState.now(p);
            if(PersistedCooldown.active(now,state.getLong("curiosActiveReady"),160)){ArtifactFeedback.message(p,"alternate_cooldown",Math.max(1,(state.getLong("curiosActiveReady")-now+19)/20));return false;}
            var lease=new EquipmentLease<>(source,()->current.get()==source&&WorldbreakerCuriosPassives.sourceKind(source)==kind?source:ItemStack.EMPTY);
            var context=HeldUtilityContext.capture(p,held);var target=ArtifactActions.target(p);if(target.isEmpty())return false;var at=target.get();var level=p.serverLevel();
            if(!level.hasChunkAt(at)||!NaturalExcavationProgram.eligible(level.getBlockState(at))||PlayerPlacedBlocks.get(level).contains(at))return false;
            if(!WorldSafety.allowed(p,kind,at)||!context.valid()||!lease.active()||!level.hasChunkAt(at)
                    ||!NaturalExcavationProgram.eligible(level.getBlockState(at))||PlayerPlacedBlocks.get(level).contains(at)||!ArtifactActions.canSpendDurability(p,source,8))return false;
            WorkProgram program=switch(kind){
                case WORLDBREAKER -> new WorldbreakerCoreFracture(at,lease);
                case AXIOM -> new AxiomAftershock(at,lease);
                case WORLDLOOM -> new WorldloomRoots(at,lease);
                case EXODIUM -> new ExodiumFallingStar(at,lease);
                case HELLSPEC -> new HellspecCinderRing(at,lease);
                default -> throw new IllegalStateException("unsupported Curios mining source");
            };
            if(!WorkQueue.startRegion(p,held,kind,program))return false;
            state.putLong("curiosActiveReady",now+160);
            source.hurtAndBreak(8,p,who->{});AbilityVfx.cast(p,kind,true);return true;
        }finally{STARTING.remove(p.getUUID());}
    }
}
