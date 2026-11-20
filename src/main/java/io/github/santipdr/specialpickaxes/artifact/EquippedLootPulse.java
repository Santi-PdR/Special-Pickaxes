package io.github.santipdr.specialpickaxes.artifact;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
/** Explicit bounded loot pulse: Dawn lights drops only; Iridium nudges only owned ore loot. */
final class EquippedLootPulse {
    private EquippedLootPulse(){}
    static boolean activate(ServerPlayer p,ItemStack held,ArtifactKind kind,EquipmentLease<ItemStack> source){
        var context=HeldUtilityContext.capture(p,held);var center=p.getEyePosition();int changed=0;
        var drops=EntitySelection.nearest(p.serverLevel(),ItemEntity.class,p.getBoundingBox().inflate(12),
                drop->drop.isAlive()&&(kind==ArtifactKind.PALIMPSEST||ArtifactTechniques.isIridiumRecallTarget(p,drop,center,12)),center,32,256);
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
