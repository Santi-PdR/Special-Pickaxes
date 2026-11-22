package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import java.util.HashSet;
import java.util.UUID;
import java.util.function.Supplier;

/** Equipped activation grants only an effect. No block plans, loot scans or motion. */
public final class CuriosEffectActive {
    private static final HashSet<UUID> STARTING=new HashSet<>();
    private CuriosEffectActive(){}
    public static boolean activate(ServerPlayer p,ItemStack source,Supplier<ItemStack> current){
        if(!STARTING.add(p.getUUID()))return false;
        try{
            if(!p.isAlive()||p.isRemoved()||p.isSpectator()||source.isEmpty()||current.get()!=source)return false;
            var kind=WorldbreakerCuriosPassives.sourceKind(source);if(kind==null||!kind.playable())return false;
            var skill=HeldSkillLease.capture(source);var state=ArtifactState.of(p,kind);long now=ArtifactState.now(p);
            if(PersistedCooldown.active(now,state.getLong("curiosEffectReady"),200))return false;
            var effect=switch(kind){
                case PALIMPSEST,SEAM_RIPPER,IRIDIUM -> MobEffects.DIG_SPEED;
                case CHOIR,WORLDBREAKER -> MobEffects.DAMAGE_RESISTANCE;
                case EVENTIDE,AXIOM -> MobEffects.NIGHT_VISION;
                case CRUCIBLE,HELLSPEC -> MobEffects.FIRE_RESISTANCE;
                case INTERREGNUM -> MobEffects.ABSORPTION;
                case WORLDLOOM -> MobEffects.REGENERATION;
                case ICARUS -> MobEffects.SLOW_FALLING;
                case EXODIUM -> MobEffects.MOVEMENT_SPEED;
                default -> MobEffects.DIG_SPEED;
            };
            if(current.get()!=source||!skill.valid())return false;
            p.addEffect(new MobEffectInstance(effect,200,0,false,false,true));
            if(current.get()!=source||!skill.valid()||!p.isAlive()||p.isRemoved())return false;
            state.putLong("curiosEffectReady",now+200);return true;
        }finally{STARTING.remove(p.getUUID());}
    }
}
