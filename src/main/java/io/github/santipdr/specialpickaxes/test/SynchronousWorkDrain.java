package io.github.santipdr.specialpickaxes.test;

import io.github.santipdr.specialpickaxes.artifact.*;
import net.minecraft.server.level.ServerPlayer;

/** Synchronous legacy fixtures do not tick the world clock. Advance it only for the new
 * time-based settling program, restoring it afterward. Actual falling physics has a separate
 * asynchronous GameTest and never uses this helper. No program state or outcome is injected. */
final class SynchronousWorkDrain {
    private SynchronousWorkDrain(){}
    private static boolean connected(ServerPlayer player){
        try{
            var field=WorkQueue.class.getDeclaredField("JOBS");field.setAccessible(true);
            var jobs=(java.util.Map<?,?>)field.get(null);var job=jobs.get(player.getUUID());if(job==null)return false;
            var region=job.getClass().getDeclaredField("region");region.setAccessible(true);return region.get(job) instanceof ConnectedExcavation||region.get(job) instanceof SignatureExcavation;
        }catch(ReflectiveOperationException e){throw new IllegalStateException(e);}
    }
    static void drain(ServerPlayer player,int turns){
        long time=player.serverLevel().getGameTime();boolean timed=connected(player);
        try{
            for(int i=0;i<turns&&WorkQueue.busy(player);i++){
                if(timed)player.serverLevel().getServer().getWorldData().overworldData().setGameTime(time+i+1);
                WorkQueue.tick();
            }
        }finally{if(timed)player.serverLevel().getServer().getWorldData().overworldData().setGameTime(time);}
    }
}
