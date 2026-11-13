package io.github.santipdr.specialpickaxes.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

/** Four-frame dawn sun that unfolds once per Palimpsest Dawn Mending pulse. */
public final class DawnBloomParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    private DawnBloomParticle(ClientLevel level,double x,double y,double z,double xd,double yd,double zd,SpriteSet sprites){
        super(level,x,y,z,xd,yd,zd);
        this.sprites=sprites;
        // 4 frames x 3 ticks keeps the sprite and the particle lifetime identical.
        this.lifetime=12;
        this.quadSize=.52F;
        this.gravity=-.004F;
        this.friction=.97F;
        this.hasPhysics=false;
        this.yd+=.01D;
        this.roll=(float)(level.random.nextFloat()*Math.PI*2F);
        this.setSpriteFromAge(sprites);
    }

    @Override public void tick(){
        super.tick();
        if(this.removed)return;
        this.setSpriteFromAge(this.sprites);
        this.oRoll=this.roll;
        this.roll+=.012F;
        int remaining=this.lifetime-this.age;
        this.quadSize=.46F+Math.min(this.age,6)*.016F;
        this.alpha=this.age<2?this.age/2F:remaining<3?remaining/3F:1F;
    }

    @Override public int getLightColor(float partialTick){return 15728880;}
    @Override public ParticleRenderType getRenderType(){return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;}

    public static final class Provider implements ParticleProvider<SimpleParticleType>{
        private final SpriteSet sprites;
        public Provider(SpriteSet sprites){this.sprites=sprites;}
        @Override public Particle createParticle(SimpleParticleType type,ClientLevel level,double x,double y,double z,
                                                double xd,double yd,double zd){
            return new DawnBloomParticle(level,x,y,z,xd,yd,zd,this.sprites);
        }
    }
}
