package io.github.santipdr.specialpickaxes.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

/** A Blockbench-built, eight-point star that turns over each Starfall mining impact. */
public final class ExodiumStarfallParticle extends TextureSheetParticle {
    private final SpriteSet sprites;
    private final float spin;

    private ExodiumStarfallParticle(ClientLevel level,double x,double y,double z,
                                    double xd,double yd,double zd,SpriteSet sprites){
        super(level,x,y,z,xd,yd,zd);
        this.sprites=sprites;
        this.alpha=0F;
        this.lifetime=10;
        this.quadSize=.42F;
        this.gravity=0F;
        this.friction=.94F;
        this.hasPhysics=false;
        this.roll=level.random.nextFloat()*(float)(Math.PI*2D);
        this.spin=(level.random.nextBoolean()?1F:-1F)*(.09F+level.random.nextFloat()*.035F);
        this.setSpriteFromAge(sprites);
    }

    @Override public void tick(){
        super.tick();
        if(this.removed)return;
        this.setSpriteFromAge(this.sprites);
        this.oRoll=this.roll;
        this.roll+=this.spin;
        this.quadSize=.38F+Math.min(this.age,3)*.018F;
        this.alpha=ParticleEnvelope.alpha(this.age,this.lifetime,2,3);
    }

    @Override public int getLightColor(float partialTick){return 15728880;}
    @Override public ParticleRenderType getRenderType(){return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;}

    public static final class Provider implements ParticleProvider<SimpleParticleType>{
        private final SpriteSet sprites;
        public Provider(SpriteSet sprites){this.sprites=sprites;}
        @Override public Particle createParticle(SimpleParticleType type,ClientLevel level,double x,double y,double z,
                                                double xd,double yd,double zd){
            return new ExodiumStarfallParticle(level,x,y,z,xd,yd,zd,this.sprites);
        }
    }
}
