package io.github.santipdr.specialpickaxes.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

/** Tiny animated crystal used by the Stasis Curios ward. */
public final class StasisShardParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    private StasisShardParticle(ClientLevel level,double x,double y,double z,double xd,double yd,double zd,SpriteSet sprites){
        super(level,x,y,z,xd,yd,zd);
        this.sprites=sprites;
        this.lifetime=20;
        this.quadSize=.16F;
        this.gravity=0F;
        this.friction=.92F;
        this.hasPhysics=false;
        this.yd=.004D;
        this.setSpriteFromAge(sprites);
    }

    @Override public void tick(){
        super.tick();
        if(this.removed)return;
        this.setSpriteFromAge(this.sprites);
        this.oRoll=this.roll;
        this.roll+=.035F;
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
            return new StasisShardParticle(level,x,y,z,xd,yd,zd,this.sprites);
        }
    }
}
