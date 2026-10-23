package io.github.santipdr.specialpickaxes.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

/** Blockbench-animated four-petal ember flare for Hellspec's custom Magma Brand pulse. */
public final class HellspecBrandFlareParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    private HellspecBrandFlareParticle(ClientLevel level,double x,double y,double z,
                                       double xd,double yd,double zd,SpriteSet sprites){
        super(level,x,y,z,xd,yd,zd);
        this.sprites=sprites;
        this.lifetime=16;
        this.quadSize=.42F;
        this.gravity=-.015F;
        this.friction=.86F;
        this.hasPhysics=false;
        this.yd+=.018D;
        this.roll=(float)(level.random.nextFloat()*Math.PI*2F);
        this.setSpriteFromAge(sprites);
    }

    @Override public void tick(){
        super.tick();
        if(this.removed)return;
        this.setSpriteFromAge(this.sprites);
        int remaining=this.lifetime-this.age;
        this.alpha=this.age<2?this.age/2F:remaining<4?remaining/4F:1F;
    }

    @Override public int getLightColor(float partialTick){return 15728880;}
    @Override public ParticleRenderType getRenderType(){return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;}

    public static final class Provider implements ParticleProvider<SimpleParticleType>{
        private final SpriteSet sprites;
        public Provider(SpriteSet sprites){this.sprites=sprites;}
        @Override public HellspecBrandFlareParticle createParticle(SimpleParticleType type,ClientLevel level,
                double x,double y,double z,double xd,double yd,double zd){
            return new HellspecBrandFlareParticle(level,x,y,z,xd,yd,zd,this.sprites);
        }
    }
}
