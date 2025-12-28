package tj.machines;

import tj.TJConfig;
import gregicadditions.GAConfig;
import gregicadditions.machines.multi.miner.Miner;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static gregicadditions.machines.multi.miner.Miner.isOre;

public interface TJMiner {

    enum Type {

        ELITE(1, TJConfig.eliteLargeMiner.eliteMinerChunkDiamater, TJConfig.eliteLargeMiner.eliteMinerFortune, Miner.fortuneString(TJConfig.eliteLargeMiner.eliteMinerFortune), TJConfig.eliteLargeMiner.eliteMinerDrillingFluid),
        ULTIMATE(1, TJConfig.ultimateLargeMiner.ultimateMinerChunkDiamater, TJConfig.ultimateLargeMiner.ultimateMinerFortune, Miner.fortuneString(TJConfig.ultimateLargeMiner.ultimateMinerFortune), TJConfig.ultimateLargeMiner.ultimateMinerDrillingFluid),
        DESTROYER(1, 1, TJConfig.worldDestroyerMiner.worldDestroyerFortune, Miner.fortuneString(TJConfig.worldDestroyerMiner.worldDestroyerFortune), TJConfig.worldDestroyerMiner.worldDestroyerDrillingFluid),
        CREATIVE(1, GAConfig.multis.largeMiner.voidLargeMinerDiameter, 6, Miner.fortuneString(6), 5);

        public final int tick;
        public final int chunk;
        public final int fortune;
        public final int drillingFluidConsumePerTick;
        public final String fortuneString;

        Type(int tick, int chunk, int fortune, String fortuneString, int drillingFluidConsumePerTick) {
            this.tick = tick;
            this.chunk = chunk;
            this.fortune = fortune;
            this.drillingFluidConsumePerTick = drillingFluidConsumePerTick;
            this.fortuneString = fortuneString;
        }
    }

    Type getType();

    World getWorld();

    long getTimer();

    default long getNbBlock() {
        return 3L;
    }

    static List<BlockPos> getBlockToMinePerChunk(TJMiner miner, AtomicLong x, AtomicLong y, AtomicLong z, ChunkPos chunkPos) {
        List<BlockPos> blocks = new ArrayList<>();
        for (int i = 0; i < miner.getNbBlock(); i++) {
            if (y.get() >= 0 && miner.getTimer() % miner.getType().tick == 0) {
                if (z.get() <= chunkPos.getZEnd()) {
                    if (x.get() <= chunkPos.getXEnd()) {
                        BlockPos blockPos = new BlockPos(x.get(), y.get(), z.get());
                        Block block = miner.getWorld().getBlockState(blockPos).getBlock();
                        if (miner.getWorld().getTileEntity(blockPos) == null) {
                            if (isOre(block) || miner.getType() == Type.DESTROYER) {
                                blocks.add(blockPos);
                            }
                        }
                        x.incrementAndGet();
                    } else {
                        x.set(chunkPos.getXStart());
                        z.incrementAndGet();
                    }
                } else {
                    z.set(chunkPos.getZStart());
                    y.decrementAndGet();
                }
            }
        }
        return blocks;
    }
}
