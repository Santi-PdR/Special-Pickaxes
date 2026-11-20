package io.github.santipdr.specialpickaxes.artifact;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
/** Explicit bounded loot pulse: Dawn lights drops only; Iridium nudges only owned ore loot. */
final class EquippedLootPulse {
    private EquippedLootPulse(){}
    static boolean activate(ServerPlayer p,ItemStack held,ArtifactKind kind,EquipmentLease<ItemStack> source){
        if(kind==ArtifactKind.PALIMPSEST){
            var guard=HeldUtilityContext.capture(p,held);
            if(!source.active()||!guard.valid())return false;
            p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DIG_SPEED,160,3,false,false,true));
            if(!source.active()||!guard.valid())return false;
            p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.REGENERATION,100,1,false,false,true));
            return source.active()&&guard.valid();
        }
        var context=HeldUtilityContext.capture(p,held);var center=p.getEyePosition();int changed=0;
        var drops=EntitySelection.nearest(p.serverLevel(),ItemEntity.class,p.getBoundingBox().inflate(12),
                drop->drop.isAlive()&&ArtifactTechniques.isIridiumRecallTarget(p,drop,center,12),center,32,256);
        for(var drop:drops){
            var pos=drop.position();var stack=drop.getItem();boolean allowed=WorldSafety.allowed(p,kind,drop.blockPosition());
            if(!context.valid()||!source.active()||source.expected().isEmpty())break;
            if(!allowed||!drop.isAlive()||!drop.position().equals(pos)||drop.getItem()!=stack)continue;
            if(kind==ArtifactKind.IRIDIUM&&!ArtifactTechniques.isIridiumRecallTarget(p,drop,center,12))continue;
            drop.setGlowingTag(true);
            if(kind==ArtifactKind.IRIDIUM){drop.setDeltaMovement(UtilityPullMotion.velocity(center.subtract(pos),drop.getDeltaMovement(),.2,.85,1.05));drop.hasImpulse=true;}
            changed++;
        }return changed>0;
    }
}
