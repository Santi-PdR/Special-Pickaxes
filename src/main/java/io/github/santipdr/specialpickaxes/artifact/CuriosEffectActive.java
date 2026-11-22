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
    private static MobEffectInstance buff(net.minecraft.world.effect.MobEffect effect,int amplifier){
        return new MobEffectInstance(effect,200,amplifier,false,false,true);
    }
    public static boolean activate(ServerPlayer p,ItemStack source,Supplier<ItemStack> current){
        if(!STARTING.add(p.getUUID()))return false;
        try{
            if(!p.isAlive()||p.isRemoved()||p.isSpectator()||source.isEmpty()||current.get()!=source)return false;
            var kind=WorldbreakerCuriosPassives.sourceKind(source);if(kind==null||!kind.playable())return false;
            var skill=HeldSkillLease.capture(source);var state=ArtifactState.of(p,kind);long now=ArtifactState.now(p);
            if(PersistedCooldown.active(now,state.getLong("curiosEffectReady"),200))return false;
            var effects=switch(kind){
                // Protected originals retain their previous activation profile.
                case SEAM_RIPPER,IRIDIUM -> java.util.List.of(buff(MobEffects.DIG_SPEED,0));
                case PALIMPSEST -> java.util.List.of(buff(MobEffects.DIG_SPEED,1));
                case CHOIR -> java.util.List.of(buff(MobEffects.DAMAGE_RESISTANCE,2));
                case EVENTIDE -> java.util.List.of(buff(MobEffects.NIGHT_VISION,0),buff(MobEffects.WATER_BREATHING,0));
                case CRUCIBLE -> java.util.List.of(buff(MobEffects.DIG_SPEED,3),buff(MobEffects.MOVEMENT_SPEED,1));
                case INTERREGNUM -> java.util.List.of(buff(MobEffects.ABSORPTION,3));
                case WORLDLOOM -> java.util.List.of(buff(MobEffects.REGENERATION,2));
                case ICARUS -> java.util.List.of(buff(MobEffects.JUMP,2),buff(MobEffects.SLOW_FALLING,0));
                case AXIOM -> java.util.List.of(buff(MobEffects.INVISIBILITY,0));
                case EXODIUM -> java.util.List.of(buff(MobEffects.MOVEMENT_SPEED,2));
                case HELLSPEC -> java.util.List.of(buff(MobEffects.FIRE_RESISTANCE,0),buff(MobEffects.DAMAGE_RESISTANCE,0));
                case WORLDBREAKER -> java.util.List.of(buff(MobEffects.ABSORPTION,1),buff(MobEffects.DAMAGE_RESISTANCE,1));
                default -> java.util.List.<MobEffectInstance>of();
            };
            if(effects.isEmpty()||current.get()!=source||!skill.valid())return false;
            for(var effect:effects){
                // Forge effect callbacks may replace equipment between bonuses.
                if(current.get()!=source||!skill.valid()||!p.isAlive()||p.isRemoved())return false;
                p.addEffect(effect);
            }
            if(current.get()!=source||!skill.valid()||!p.isAlive()||p.isRemoved())return false;
            state.putLong("curiosEffectReady",now+200);return true;
        }finally{STARTING.remove(p.getUUID());}
    }
}
