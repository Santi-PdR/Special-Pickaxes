package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Vector3f;

/** Iridium's successive ore breaks resonate into a short, capped mining-speed surge. */
public final class LodeSurgeEffect extends MobEffect {
    private static final String NEXT_PEAK_MEAL="specialpickaxesLodePeakMealAt";
    private static final int PEAK_MEAL_COOLDOWN_TICKS=40;
    private static final DustParticleOptions MINT=new DustParticleOptions(new Vector3f(.34F,.96F,.78F),.78F);
    private static final DustParticleOptions AMBER=new DustParticleOptions(new Vector3f(1F,.72F,.32F),.68F);

    public LodeSurgeEffect(){super(MobEffectCategory.BENEFICIAL,0x68f2cb);}

    public static int orePulseCost(int stacks){
        int charge=Math.max(0,Math.min(3,stacks));return 3+charge+(charge==3?1:0);
    }
    /** Four motes to two viewers, plus the peak accent sent only to the recipient. */
    public static int sharedPulseCost(int stacks){return 8+(stacks>=3?1:0);}

    /** Local sparkle confirms each charge without broadcasting a mining trail to the whole server. */
    public static void oreBreakPulse(ServerPlayer player,BlockPos pos,int stacks){
        if(!(player.level() instanceof ServerLevel level))return;
        stacks=Math.max(0,Math.min(3,stacks));
        if(!ArtifactFeedback.reserveAuraParticles(level.getServer().getTickCount(),orePulseCost(stacks),1))return;
        double x=pos.getX()+.5D,y=pos.getY()+.58D,z=pos.getZ()+.5D;
        level.sendParticles(player,MINT,false,x,y,z,3+stacks, .18D,.18D,.18D,.012D);
        if(stacks==3)level.sendParticles(player,ParticleTypes.GLOW,false,x,y,z,1,.05D,.08D,.05D,.008D);
    }

    /** The bounded Curios handoff is drawn only for the wearer and the teammate receiving it. */
    public static void sharedOrePulse(ServerPlayer owner,ServerPlayer ally,BlockPos ore,int stacks){
        if(owner.level()!=ally.level()||!(owner.level() instanceof ServerLevel level))return;
        if(!ArtifactFeedback.reserveAuraParticles(level.getServer().getTickCount(),sharedPulseCost(stacks),1))return;
        var start=new net.minecraft.world.phys.Vec3(ore.getX()+.5D,ore.getY()+.58D,ore.getZ()+.5D);
        var end=ally.position().add(0,ally.getBbHeight()*.58D,0);
        for(int i=1;i<=4;i++){
            var point=start.lerp(end,i/5D);
            level.sendParticles(owner,MINT,false,point.x,point.y,point.z,1,0,0,0,0);
            level.sendParticles(ally,MINT,false,point.x,point.y,point.z,1,0,0,0,0);
        }
        if(stacks>=3)level.sendParticles(ally,ParticleTypes.GLOW,false,end.x,end.y,end.z,1,.04D,.04D,.04D,.005D);
    }

    @Override public void applyEffectTick(LivingEntity entity,int amplifier){
        if(entity.level().isClientSide||!(entity instanceof ServerPlayer player)||!(entity.level() instanceof ServerLevel level))return;
        int charge=Math.max(0,Math.min(2,amplifier));
        if(charge==2)feedAtPeak(player,level,player.getY()+player.getBbHeight()*.55D);
        if(player.tickCount%12!=0)return;
        int motes=2+charge;
        if(!ArtifactFeedback.reserveAuraParticles(level.getServer().getTickCount(),motes,1))return;
        double phase=player.tickCount*.12D,radius=.3D+charge*.055D;
        double y=player.getY()+player.getBbHeight()*.55D;
        for(int i=0;i<motes;i++){
            double angle=phase+i*(Math.PI*2D/motes);
            double progress=i/(double)Math.max(1,motes-1);
            // Each added ore charge lifts the orbit into a short helix; the final bright
            // mote marks the full surge without increasing its particle count or cadence.
            double lift=(progress-.5D)*charge*.12D;
            double helix=Math.sin(angle*2D+phase*.35D)*(.025D+charge*.018D);
            var particle=charge==2&&i==motes-1?ParticleTypes.END_ROD:((i+charge)&1)==0?MINT:AMBER;
            level.sendParticles(player,particle,false,
                    player.getX()+Math.cos(angle)*radius,y+lift+helix,
                    player.getZ()+Math.sin(angle)*radius,1,0,0,0,0);
        }
    }

    /** A full ore streak restores one food point at most every two seconds. */
    private static void feedAtPeak(ServerPlayer player,ServerLevel level,double y){
        var food=player.getFoodData();if(food.getFoodLevel()>=20)return;
        long now=ArtifactState.now(player);var data=player.getPersistentData();
        if(PersistedCooldown.active(now,data.getLong(NEXT_PEAK_MEAL),PEAK_MEAL_COOLDOWN_TICKS))return;
        food.eat(1,.2F);data.putLong(NEXT_PEAK_MEAL,now+PEAK_MEAL_COOLDOWN_TICKS);
        if(ArtifactFeedback.reserveAuraParticles(level.getServer().getTickCount(),2,1))
            level.sendParticles(player,AMBER,false,player.getX(),y+.12D,player.getZ(),2,.08D,.1D,.08D,.008D);
    }

    @Override public boolean isDurationEffectTick(int duration,int amplifier){return duration%4==0;}
}
