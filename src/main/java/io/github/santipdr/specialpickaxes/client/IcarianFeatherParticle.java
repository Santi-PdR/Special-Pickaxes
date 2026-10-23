package io.github.santipdr.specialpickaxes.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

/** A small animated feather that rides the cyan current of Icarian Lift. */
public final class IcarianFeatherParticle extends TextureSheetParticle {
    private final SpriteSet sprites;
    private final float flutter;

    private IcarianFeatherParticle(ClientLevel level,double x,double y,double z,double xd,double yd,double zd,SpriteSet sprites){
        super(level,x,y,z,xd,yd,zd);
        this.sprites=sprites;
        this.lifetime=16;
        this.quadSize=.28F;
        this.gravity=0F;
        this.friction=.94F;
        this.hasPhysics=false;
        this.yd+=.012D;
        this.roll=(float)(level.random.nextFloat()*Math.PI*2F);
        this.flutter=(level.random.nextBoolean()?1F:-1F)*(.045F+level.random.nextFloat()*.025F);
        this.setSpriteFromAge(sprites);
    }

    @Override public void tick(){
        super.tick();
        if(this.removed)return;
        this.setSpriteFromAge(this.sprites);
        this.oRoll=this.roll;
        this.roll+=this.flutter;
        int remaining=this.lifetime-this.age;
        this.alpha=this.age<2?this.age/2F:remaining<4?remaining/4F:1F;
    }

    @Override public int getLightColor(float partialTick){return 15728880;}
    @Override public ParticleRenderType getRenderType(){return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;}

    public static final class Provider implements ParticleProvider<SimpleParticleType>{
        private final SpriteSet sprites;
        public Provider(SpriteSet sprites){this.sprites=sprites;}
        @Override public Particle createParticle(SimpleParticleType type,ClientLevel level,double x,double y,double z,
                                                double xd,double yd,double zd){
            return new IcarianFeatherParticle(level,x,y,z,xd,yd,zd,this.sprites);
        }
    }
}
