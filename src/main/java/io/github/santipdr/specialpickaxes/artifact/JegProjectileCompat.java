package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.Projectile;
import java.lang.reflect.Method;

/** Optional Just Enough Guns adapter; the mod remains usable when JEG is absent. */
final class JegProjectileCompat {
    private static final String PROJECTILE_CLASS="ttv.migami.jeg.entity.projectile.ProjectileEntity";
    private static final Class<? extends Entity> TYPE=resolveType();
    private static final Method SHOOTER=resolveShooter();
    private JegProjectileCompat(){}

    @SuppressWarnings("unchecked")
    private static Class<? extends Entity> resolveType(){
        try{return (Class<? extends Entity>)Class.forName(PROJECTILE_CLASS,false,JegProjectileCompat.class.getClassLoader()).asSubclass(Entity.class);}
        catch(ReflectiveOperationException|LinkageError unavailable){return null;}
    }
    private static Method resolveShooter(){
        if(TYPE==null)return null;
        try{return TYPE.getMethod("getShooter");}
        catch(ReflectiveOperationException unavailable){return null;}
    }
    @SuppressWarnings("unchecked")
    static Class<Entity> entityType(){return TYPE==null?null:(Class<Entity>)(Class<?>)TYPE;}
    static boolean isProjectile(Entity entity){return entity instanceof Projectile||TYPE!=null&&TYPE.isInstance(entity);}
    static Entity owner(Entity entity){
        if(entity instanceof Projectile projectile)return projectile.getOwner();
        if(SHOOTER==null||TYPE==null||!TYPE.isInstance(entity))return null;
        try{Object shooter=SHOOTER.invoke(entity);return shooter instanceof Entity owner?owner:null;}
        catch(ReflectiveOperationException|RuntimeException unavailable){return null;}
    }
    static boolean hostileTo(Entity entity,ServerPlayer player,boolean rejectPlayerOwned){
        if(!isProjectile(entity))return false;
        Entity owner=owner(entity);
        return owner==null||owner!=player&&!owner.isAlliedTo(player)
                &&!(rejectPlayerOwned&&owner instanceof net.minecraft.world.entity.player.Player);
    }
}
