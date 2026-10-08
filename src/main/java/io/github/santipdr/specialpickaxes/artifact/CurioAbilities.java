package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;

/** Two support abilities for each artifact while equipped in its Curios slot. */
public final class CurioAbilities {
    private CurioAbilities() {}
    public static boolean execute(ServerPlayer player, ItemStack tool, ArtifactKind kind, RelicControl.Action action) {
        int skill = action == RelicControl.Action.CURIO_TWO ? 2 : 1;
        String key = "curioSkill" + skill + "Ready";
        var data = ArtifactState.of(player, kind);long now = ArtifactState.now(player);
        if (data.getLong(key) > now) { ArtifactFeedback.message(player,"curio_cooldown");return false; }
        int duration = 240;
        switch (kind) {
            case PALIMPSEST -> { if(skill==1){grant(player,MobEffects.REGENERATION,2,duration);grant(player,MobEffects.SATURATION,0,40);}else{grant(player,MobEffects.ABSORPTION,3,duration);grant(player,MobEffects.DAMAGE_RESISTANCE,1,duration);} }
            case CHOIR -> { if(skill==1)grant(player,MobEffects.DIG_SPEED,3,duration);else{grant(player,MobEffects.DAMAGE_RESISTANCE,2,duration);grant(player,MobEffects.NIGHT_VISION,0,duration);} }
            case EVENTIDE -> { if(skill==1)markHostiles(player);grant(player,MobEffects.NIGHT_VISION,0,duration);grant(player,MobEffects.SLOW_FALLING,0,duration); }
            case CRUCIBLE -> { grant(player,MobEffects.FIRE_RESISTANCE,0,duration);grant(player,MobEffects.ABSORPTION,skill==1?1:3,duration); }
            case INTERREGNUM -> { if(skill==1){grant(player,MobEffects.REGENERATION,3,duration);grant(player,MobEffects.NIGHT_VISION,0,duration);}else{grant(player,MobEffects.DAMAGE_RESISTANCE,3,duration);grant(player,MobEffects.ABSORPTION,3,duration);} }
            case WORLDLOOM -> { if(skill==1)grant(player,MobEffects.REGENERATION,3,duration);else{grant(player,MobEffects.SATURATION,0,80);grant(player,MobEffects.JUMP,1,duration);} }
            case ICARUS -> { grant(player,MobEffects.SLOW_FALLING,0,duration);if(skill==1)grant(player,MobEffects.JUMP,3,duration);else{player.setDeltaMovement(player.getDeltaMovement().add(0,0.9,0));player.hasImpulse=true;grant(player,MobEffects.MOVEMENT_SPEED,2,duration);} }
            case AXIOM -> { if(skill==1){grant(player,MobEffects.INVISIBILITY,0,160);grant(player,MobEffects.MOVEMENT_SPEED,2,duration);}else if(!ArtifactSkills.use(player,tool,kind))return false; }
            case WORLDBREAKER -> { if(skill==2){if(!ArtifactSkills.use(player,tool,kind))return false;}else{grant(player,MobEffects.DAMAGE_RESISTANCE,2,duration);grant(player,MobEffects.ABSORPTION,2,duration);} }
            case EXODIUM -> { if(skill==1)grant(player,MobEffects.MOVEMENT_SPEED,3,duration);else{grant(player,MobEffects.DIG_SPEED,3,duration);grant(player,MobEffects.SLOW_FALLING,0,duration);} }
            case IRIDIUM -> { if(skill==1){MiningDesigns.survey(player,player.blockPosition());grant(player,MobEffects.DIG_SPEED,2,duration);}else{magnetDrops(player);grant(player,MobEffects.MOVEMENT_SPEED,1,duration);} }
            case HELLSPEC -> { if(skill==1){grant(player,MobEffects.FIRE_RESISTANCE,0,duration*2);grant(player,MobEffects.REGENERATION,1,duration);}else if(!ArtifactSkills.use(player,tool,kind))return false; }
            default -> { return false; }
        }
        data.putLong(key, now + 600);
        ArtifactFeedback.burst(player,kind,player.blockPosition(),8);
        ArtifactFeedback.message(player,skill==1?"curio_one":"curio_two");
        return true;
    }
    private static void markHostiles(ServerPlayer player) {
        int left=32;
        for(var mob:player.serverLevel().getEntitiesOfClass(Monster.class,player.getBoundingBox().inflate(24),m->m.isAlive()&&!m.isAlliedTo(player))) {
            mob.addEffect(new MobEffectInstance(MobEffects.GLOWING,240,0,true,false,true));
            if(--left<=0)break;
        }
    }
    private static void magnetDrops(ServerPlayer player){
        var center=player.getEyePosition();int pulled=0;
        for(var drop:player.serverLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,player.getBoundingBox().inflate(12),e->e.isAlive()&&(e.getOwner()==null||player.getUUID().equals(e.getOwner())))){
            var delta=center.subtract(drop.position());if(delta.lengthSqr()<0.25)continue;drop.setDeltaMovement(delta.normalize().scale(0.65));drop.hasImpulse=true;if(++pulled>=64)break;
        }
    }
    private static void grant(ServerPlayer player,MobEffect effect,int amplifier,int duration) {
        var current=player.getEffect(effect);
        if(current==null||current.getAmplifier()<amplifier||current.getAmplifier()==amplifier&&current.getDuration()<duration/2)
            player.addEffect(new MobEffectInstance(effect,duration,amplifier,true,false,true));
    }
}
