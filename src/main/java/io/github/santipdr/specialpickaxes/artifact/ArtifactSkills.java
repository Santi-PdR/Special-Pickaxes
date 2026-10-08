package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/** Non-mining active skill for each pick, keeping the primary excavation ability distinct. */
public final class ArtifactSkills {
    private ArtifactSkills() {}
    public static boolean use(ServerPlayer p, ItemStack tool, ArtifactKind kind) {
        var data=ArtifactState.of(p,kind);long now=ArtifactState.now(p);
        if(data.getLong("alternateReady")>now){ArtifactFeedback.message(p,"cooldown");return false;}
        boolean cast=cast(p,tool,kind);
        if(!cast)return false;
        int cooldown=Math.max(100,ArtifactConfig.COOLDOWN.get()*5);
        data.putLong("alternateReady",now+cooldown);
        ArtifactFeedback.sound(p,kind);ArtifactFeedback.burst(p,kind,p.blockPosition(),10);
        ArtifactFeedback.message(p,"alternate_used");return true;
    }
    private static boolean cast(ServerPlayer p,ItemStack tool,ArtifactKind kind){
        return switch(kind){
            case PALIMPSEST -> palimpsest(p);
            case CHOIR -> choir(p);
            case EVENTIDE -> eventide(p);
            case CRUCIBLE -> crucible(p);
            case INTERREGNUM -> stasis(p);
            case WORLDLOOM -> worldloom(p);
            case ICARUS -> icarus(p);
            case AXIOM -> axiom(p);
            case WORLDBREAKER -> worldbreaker(p,tool);
            case EXODIUM -> exodium(p);
            case IRIDIUM -> iridium(p);
            case HELLSPEC -> hellspec(p);
            default -> false;
        };
    }
    private static boolean palimpsest(ServerPlayer p){
        grant(p,MobEffects.DIG_SPEED,4,240);grant(p,MobEffects.REGENERATION,2,160);return true;
    }
    private static boolean choir(ServerPlayer p){
        allies(p,10,a->{grant(a,MobEffects.DAMAGE_RESISTANCE,1,160);grant(a,MobEffects.NIGHT_VISION,0,240);});return true;
    }
    private static boolean eventide(ServerPlayer p){
        int marked=0;for(var mob:p.serverLevel().getEntitiesOfClass(Monster.class,p.getBoundingBox().inflate(32),m->m.isAlive()&&!m.isAlliedTo(p))){
            mob.addEffect(new MobEffectInstance(MobEffects.GLOWING,300,0,true,false,true));if(++marked>=48)break;
        }
        grant(p,MobEffects.NIGHT_VISION,0,400);return true;
    }
    private static boolean crucible(ServerPlayer p){
        int shown=0;var level=p.serverLevel();var center=p.blockPosition();
        for(int x=-10;x<=10&&shown<40;x++)for(int y=-6;y<=6&&shown<40;y++)for(int z=-10;z<=10&&shown<40;z++){
            var pos=center.offset(x,y,z);if(!level.hasChunkAt(pos))continue;var state=level.getBlockState(pos);
            if(MiningDesigns.crucibleGeology(state)&&!PlayerPlacedBlocks.get(level).contains(pos)){
                level.sendParticles(p,net.minecraft.core.particles.ParticleTypes.END_ROD,false,pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5,2,.12,.12,.12,.01);shown++;
            }
        }
        grant(p,MobEffects.DIG_SPEED,3,240);grant(p,MobEffects.ABSORPTION,2,240);return true;
    }
    private static boolean stasis(ServerPlayer p){
        grant(p,MobEffects.DAMAGE_RESISTANCE,2,240);grant(p,MobEffects.REGENERATION,2,240);grant(p,MobEffects.NIGHT_VISION,0,400);return true;
    }
    private static boolean worldloom(ServerPlayer p){
        allies(p,12,a->{grant(a,MobEffects.REGENERATION,1,240);grant(a,MobEffects.SATURATION,0,40);});return true;
    }
    private static boolean icarus(ServerPlayer p){
        var look=p.getLookAngle().normalize();p.setDeltaMovement(p.getDeltaMovement().add(look.scale(1.1).add(0,.45,0)));p.hasImpulse=true;
        grant(p,MobEffects.SLOW_FALLING,0,500);grant(p,MobEffects.JUMP,2,300);grant(p,MobEffects.MOVEMENT_SPEED,1,300);return true;
    }
    private static boolean axiom(ServerPlayer p){
        Vec3 start=p.position(),direction=p.getLookAngle().normalize(),last=start;var box=p.getBoundingBox();
        for(int step=1;step<=18;step++){
            Vec3 next=start.add(direction.scale(step));BlockPos at=BlockPos.containing(next);
            if(!p.serverLevel().hasChunkAt(at)||!p.serverLevel().getWorldBorder().isWithinBounds(at)||!p.serverLevel().noCollision(p,box.move(next.subtract(start))))break;
            last=next;
        }
        if(last.distanceToSqr(start)<9)return false;
        p.connection.teleport(last.x,last.y,last.z,p.getYRot(),p.getXRot());grant(p,MobEffects.NIGHT_VISION,0,400);return true;
    }
    private static boolean worldbreaker(ServerPlayer p,ItemStack tool){
        if(!tool.hasTag()||!tool.getTag().contains("copiedSkill")){ArtifactFeedback.message(p,"copy_pick_first");return false;}
        ArtifactKind source;try{source=ArtifactKind.byId(tool.getTag().getString("copiedSkill"));}catch(IllegalArgumentException invalid){return false;}
        if(source==ArtifactKind.WORLDBREAKER)return false;
        return source!=null&&cast(p,tool,source);
    }
    private static boolean exodium(ServerPlayer p){
        grant(p,MobEffects.MOVEMENT_SPEED,3,240);grant(p,MobEffects.DIG_SPEED,2,240);grant(p,MobEffects.SLOW_FALLING,0,240);return true;
    }
    private static boolean iridium(ServerPlayer p){
        int found=MiningDesigns.survey(p,p.blockPosition());grant(p,MobEffects.DIG_SPEED,3,240);grant(p,MobEffects.NIGHT_VISION,0,400);return found>=0;
    }
    private static boolean hellspec(ServerPlayer p){
        var from=p.getEyePosition();var to=from.add(p.getLookAngle().scale(24));
        var hit=p.serverLevel().clip(new net.minecraft.world.level.ClipContext(from,to,net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.ANY,p));
        if(hit.getType()!=net.minecraft.world.phys.HitResult.Type.BLOCK||!p.serverLevel().getFluidState(hit.getBlockPos()).is(net.minecraft.tags.FluidTags.LAVA))return false;
        grant(p,MobEffects.FIRE_RESISTANCE,0,600);grant(p,MobEffects.REGENERATION,2,240);grant(p,MobEffects.DAMAGE_RESISTANCE,1,240);return true;
    }
    private static void allies(ServerPlayer p,double radius,java.util.function.Consumer<ServerPlayer> action){
        for(var ally:p.serverLevel().getEntitiesOfClass(ServerPlayer.class,p.getBoundingBox().inflate(radius),q->q.isAlive()&&(q==p||q.isAlliedTo(p))))action.accept(ally);
    }
    private static void grant(net.minecraft.world.entity.LivingEntity e,MobEffect effect,int amp,int ticks){
        var current=e.getEffect(effect);if(current==null||current.getAmplifier()<amp||current.getAmplifier()==amp&&current.getDuration()<ticks/2)
            e.addEffect(new MobEffectInstance(effect,ticks,amp,true,false,true));
    }
}
