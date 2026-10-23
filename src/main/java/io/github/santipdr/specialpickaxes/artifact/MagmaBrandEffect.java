package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/** Hellspec's eruption and Cinder Retort leave a visible brand with custom magma damage. */
public final class MagmaBrandEffect extends MobEffect {
    private static final String CASTER="specialpickaxesMagmaBrandCaster";
    private static final DustParticleOptions BRAND_EMBER=new DustParticleOptions(new Vector3f(1F,.24F,.035F),.78F);
    public MagmaBrandEffect(){super(MobEffectCategory.HARMFUL,0xff5428);}

    /** Keeps the owner so each later damage pulse can recheck PvP, teams, range and protected regions. */
    public static boolean applyFrom(ServerPlayer caster,LivingEntity target,int duration,int amplifier){
        if(!canDamage(caster,target))return false;
        if(!ArtifactPassives.grantCombatEffect(target,io.github.santipdr.specialpickaxes.SpecialPickaxes.MAGMA_BRAND.get(),amplifier,duration))return false;
        target.getPersistentData().putUUID(CASTER,caster.getUUID());
        return true;
    }

    private static boolean canDamage(ServerPlayer caster,LivingEntity target){
        if(caster==null||target==null||!caster.isAlive()||caster.isRemoved()||target==caster||!target.isAlive()
                ||caster.serverLevel()!=target.level()||caster.isAlliedTo(target))return false;
        if(target instanceof Player other&&!caster.canHarmPlayer(other))return false;
        return WorldSafety.allowed(caster,ArtifactKind.HELLSPEC,target.blockPosition());
    }

    @Override public void applyEffectTick(LivingEntity entity,int amplifier){
        if(entity.level().isClientSide||!(entity.level() instanceof ServerLevel level))return;
        var data=entity.getPersistentData();
        if(!data.hasUUID(CASTER))return; // An effect supplied outside the relic system never becomes owned damage.
        var id=data.getUUID(CASTER);
        net.minecraft.world.entity.Entity owner=level.getEntity(id);
        ServerPlayer caster=owner instanceof ServerPlayer player?player:level.getServer().getPlayerList().getPlayer(id);
        if(!canDamage(caster,entity)){
            entity.removeEffect(this);
            ArtifactPassives.clearCombatEffectLease(entity,this);
            data.remove(CASTER);
            return;
        }
        // Keep the custom pulse distinct from vanilla fire ticks so already-burning targets still feel the mark.
        entity.invulnerableTime=0;
        if(!entity.hurt(level.damageSources().magic(),1.5F+.5F*Math.max(0,amplifier)))return;
        entity.setSecondsOnFire(2);
        ArtifactFeedback.entityAuraPulse(level,entity,brandParticles(entity,level.getGameTime()));
    }

    /** Three ember motes orbit a compact Blockbench flare while smoke marks the burn. */
    public static List<ArtifactFeedback.AuraParticle> brandParticles(LivingEntity entity,long worldTime){
        double phase=worldTime*.16D,radius=Math.max(.28D,entity.getBbWidth()*.5D);
        double x=entity.getX(),y=entity.getY()+entity.getBbHeight()*.44D,z=entity.getZ();
        var particles=new ArrayList<ArtifactFeedback.AuraParticle>(6);
        for(int i=0;i<3;i++){
            double angle=phase+i*Math.PI*2D/3D;
            particles.add(new ArtifactFeedback.AuraParticle(BRAND_EMBER,
                    new Vec3(x+Math.cos(angle)*radius,y+Math.sin(angle*2D)*.07D,z+Math.sin(angle)*radius),1,0,0,0,0));
        }
        particles.add(new ArtifactFeedback.AuraParticle(io.github.santipdr.specialpickaxes.SpecialPickaxes.HELLSPEC_BRAND_FLARE.get(),new Vec3(x,y-.12D,z),1,0,0,0,0));
        particles.add(new ArtifactFeedback.AuraParticle(ParticleTypes.SMOKE,new Vec3(x,y+entity.getBbHeight()*.24D,z),1,.025D,.035D,.025D,.002D));
        if(Math.floorMod(worldTime,40L)==0L)
            particles.add(new ArtifactFeedback.AuraParticle(ParticleTypes.LAVA,new Vec3(x,y-.2D,z),1,.06D,.04D,.06D,.003D));
        return List.copyOf(particles);
    }

    @Override public boolean isDurationEffectTick(int duration,int amplifier){return duration%20==0;}
}
