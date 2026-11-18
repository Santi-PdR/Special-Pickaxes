package io.github.santipdr.specialpickaxes.client;

import io.github.santipdr.specialpickaxes.artifact.RelicParticleStyle;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;

/** Sprite animation and motion are both specific to the casting pickaxe; lifetime is under two seconds. */
public final class RelicSignatureParticle extends TextureSheetParticle {
    private final SpriteSet sprites;
    private final RelicParticleStyle style;
    private final double originX,originY,originZ,phase;
    private RelicSignatureParticle(ClientLevel level,double x,double y,double z,SpriteSet sprites,RelicParticleStyle style){
        super(level,x,y,z,0,0,0);
        this.sprites=sprites;this.style=style;originX=x;originY=y;originZ=z;
        phase=level.random.nextDouble()*Math.PI*2;
        lifetime=style.lifetime;quadSize=style.size;hasPhysics=false;alpha=0;
        xd=0;yd=0;zd=0;gravity=0;roll=(float)phase;oRoll=roll;
        setSpriteFromAge(sprites);
    }
    @Override public void tick(){
        xo=x;yo=y;zo=z;oRoll=roll;
        if(++age>=lifetime){remove();return;}
        double t=age/(double)lifetime;var offset=style.displacement(t,phase);
        setPos(originX+offset.x,originY+offset.y,originZ+offset.z);
        roll+=style.spin;quadSize=style.size*(float)(.65+.5*Math.sin(t*Math.PI));
        alpha=ParticleEnvelope.alpha(age,lifetime,3,8);setSpriteFromAge(sprites);
    }
    @Override public int getLightColor(float partialTick){return 15728880;}
    @Override public ParticleRenderType getRenderType(){return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;}
    public static final class Provider implements ParticleProvider<SimpleParticleType>{
        private final SpriteSet sprites;private final RelicParticleStyle style;
        public Provider(SpriteSet sprites,RelicParticleStyle style){this.sprites=sprites;this.style=style;}
        @Override public RelicSignatureParticle createParticle(SimpleParticleType type,ClientLevel level,double x,double y,double z,double xd,double yd,double zd){
            return new RelicSignatureParticle(level,x,y,z,sprites,style);
        }
    }
}
