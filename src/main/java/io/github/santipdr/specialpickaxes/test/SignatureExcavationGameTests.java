package io.github.santipdr.specialpickaxes.test;

import io.github.santipdr.specialpickaxes.artifact.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraftforge.gametest.*;
import java.util.*;

@GameTestHolder("specialpickaxes")
@PrefixGameTestTemplate(false)
public final class SignatureExcavationGameTests {
    @GameTest(template="empty",batch="signature_geometry_r7") public static void everySignatureHasUniqueBoundedForwardFootprint(GameTestHelper h){
        for(var direction:Direction.values())for(boolean alternate:new boolean[]{false,true}){
            var unique=new HashSet<Set<BlockPos>>();
            for(var kind:ArtifactKind.playableValues()){
                if(!SignatureExcavation.handles(kind))continue;
                var seed=new BlockPos(5,100,5);var cells=SignatureExcavation.footprint(seed,direction,kind,alternate);
                h.assertTrue(cells.get(0).equals(seed)&&cells.size()<=(alternate?384:256),"seed first and fixed volume cap "+kind.id);
                h.assertTrue(new HashSet<>(cells).size()==cells.size(),"no duplicate inspections "+kind.id);
                for(var at:cells){var delta=at.subtract(seed);h.assertTrue(delta.getX()*direction.getStepX()+delta.getY()*direction.getStepY()+delta.getZ()*direction.getStepZ()>=0,"all cells on target face or forward "+kind.id);}
                h.assertTrue(unique.add(new HashSet<>(cells)),"distinct footprint for each held signature "+kind.id);
            }
        }h.succeed();
    }
}
