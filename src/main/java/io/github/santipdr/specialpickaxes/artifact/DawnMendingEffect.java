package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Vector3f;

/** Palimpsest's passive gives controlled healing pulses instead of vanilla Regeneration. */
public final class DawnMendingEffect extends MobEffect {
    private static final String NEXT_PULSE="specialpickaxesDawnMendingAtWorldTime";
    private static final DustParticleOptions DAWN_DUST=new DustParticleOptions(new Vector3f(1F,.72F,.28F),.82F);

    public DawnMendingEffect(){super(MobEffectCategory.BENEFICIAL,0xf3b950);}

    @Override public void applyEffectTick(LivingEntity entity,int amplifier){
        if(entity.level().isClientSide)return;
        // Persist cadence against the world clock; player tickCount resets on respawn.
        long now=entity.level().getGameTime();
        var data=entity.getPersistentData();
        if(data.getLong(NEXT_PULSE)>now)return;
        if(entity.getHealth()>=entity.getMaxHealth())return;
        float before=entity.getHealth();
        entity.heal(1F+.5F*Math.max(0,amplifier));
        // Other relics (for example Null Mark) or server rules may veto the heal.
        // Do not spend the pulse or show a false healing bloom unless health changed.
        if(entity.getHealth()<=before)return;
        data.putLong(NEXT_PULSE,now+40L);
        if(entity.level() instanceof ServerLevel level){
            double x=entity.getX(),y=entity.getY()+entity.getBbHeight()*.48D,z=entity.getZ();
            for(int i=0;i<8;i++){
                double angle=Math.PI*2D*i/8D;
                level.sendParticles(DAWN_DUST,x+Math.cos(angle)*.42D,y+(i%2)*.12D,z+Math.sin(angle)*.42D,1,.025D,.04D,.025D,0);
                if((i&1)==0)level.sendParticles(ParticleTypes.WAX_ON,x+Math.cos(angle)*.32D,y+.25D,z+Math.sin(angle)*.32D,1,.02D,.035D,.02D,.004D);
            }
        }
    }

    @Override public boolean isDurationEffectTick(int duration,int amplifier){return duration%40==0;}
}
