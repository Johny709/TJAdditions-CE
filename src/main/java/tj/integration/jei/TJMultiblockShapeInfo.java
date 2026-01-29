package tj.integration.jei;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.util.BlockInfo;
import gregtech.common.blocks.MetaBlocks;
import gregtech.integration.jei.multiblock.MultiblockShapeInfo;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import tj.util.TJBlockInfo;
import tj.util.triple.IntTriple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;
import static gregtech.api.multiblock.BlockPattern.RelativeDirection.FRONT;
import static gregtech.api.multiblock.BlockPattern.RelativeDirection.RIGHT;
import static gregtech.api.multiblock.BlockPattern.RelativeDirection.UP;

public class TJMultiblockShapeInfo extends MultiblockShapeInfo {

    private final BlockInfo[][][] blocks; //[z][y][x]

    public TJMultiblockShapeInfo(BlockInfo[][][] blocks) {
        super(blocks);
        this.blocks = blocks;
    }

    @Override
    public BlockInfo[][][] getBlocks() {
        return blocks;
    }

    public static Builder builder() {
        return builder(RIGHT, UP, FRONT);
    }

    public static Builder builder(BlockPattern.RelativeDirection charDir, BlockPattern.RelativeDirection stringDir, BlockPattern.RelativeDirection aisleDir) {
        return new Builder(charDir, stringDir, aisleDir);
    }

    public static class Builder extends MultiblockShapeInfo.Builder {

        private final List<String[]> shape = new ArrayList<>();
        private final Char2ObjectMap<BlockInfo> symbolMap = new Char2ObjectOpenHashMap<>();
        private final BlockPattern.RelativeDirection[] structureDir = new BlockPattern.RelativeDirection[3];
        private final BlockPattern.RelativeDirection[] idealDir = {RIGHT, UP, FRONT};

        public Builder(BlockPattern.RelativeDirection charDir, BlockPattern.RelativeDirection stringDir, BlockPattern.RelativeDirection aisleDir) {
            this.structureDir[0] = charDir;
            this.structureDir[1] = stringDir;
            this.structureDir[2] = aisleDir;

            int flags = 0;
            for (int i = 0; i < 3; ++i) {
                switch (this.structureDir[i]) {
                    case UP:
                    case DOWN:
                        flags |= 1;
                        break;
                    case LEFT:
                    case RIGHT:
                        flags |= 2;
                        break;
                    case FRONT:
                    case BACK:
                        flags |= 4;
                }
            }
            if (flags != 7) {
                throw new IllegalArgumentException("Must have 3 different axes!");
            }
        }

        @Override
        public Builder aisle(String... data) {
            this.shape.add(data);
            return this;
        }

        @Override
        public Builder where(char symbol, BlockInfo value) {
            this.symbolMap.put(symbol, value);
            return this;
        }

        @Override
        public Builder where(char symbol, IBlockState blockState) {
            return where(symbol, new BlockInfo(blockState));
        }

        @Override
        public Builder where(char symbol, MetaTileEntity tileEntity, EnumFacing frontSide) {
            MetaTileEntityHolder holder = new MetaTileEntityHolder();
            holder.setMetaTileEntity(tileEntity);
            holder.getMetaTileEntity().setFrontFacing(frontSide);
            return where(symbol, new BlockInfo(MetaBlocks.MACHINE.getDefaultState(), holder));
        }

        public Builder where(char symbol, EnumFacing frontSide, MetaTileEntity... mte) {
            List<MetaTileEntityHolder> holders = new ArrayList<>();
            for (MetaTileEntity metaTileEntity : mte) {
                MetaTileEntityHolder holder = new MetaTileEntityHolder();
                holder.setMetaTileEntity(metaTileEntity);
                holder.getMetaTileEntity().setFrontFacing(frontSide);
                holders.add(holder);
            }
            IBlockState[] states = Arrays.copyOf(new IBlockState[0], holders.size());
            Arrays.fill(states, MetaBlocks.MACHINE.getDefaultState());
            this.symbolMap.put(symbol, new TJBlockInfo(states, holders.toArray(new TileEntity[0])));
            return this;
        }

        private BlockInfo[][][] bakeArray() {
            IntTriple maximumBounds = transformPos(this.shape.size(), this.shape.get(0).length, this.shape.get(0)[0].length(), 0, 0, 0, false); // Find the bounds of the transformed array by transforming the final position
            BlockInfo[][][] blockInfos = new BlockInfo[maximumBounds.getLeft()][maximumBounds.getMiddle()][maximumBounds.getRight()];
            for (int i = 0; i < this.shape.size(); i++) {
                String[] aisleEntry = this.shape.get(i);
                for (int j = 0; j < aisleEntry.length; j++) {
                    String rowEntry = aisleEntry[j];
                    for (int k = 0; k < rowEntry.length(); k++) {
                        BlockInfo positionData = this.symbolMap.getOrDefault(rowEntry.charAt(k), BlockInfo.EMPTY);
                        if (positionData.getTileEntity() != null && positionData.getTileEntity() instanceof MetaTileEntityHolder) {

                            MetaTileEntityHolder holder = (MetaTileEntityHolder) positionData.getTileEntity();

                            MetaTileEntityHolder newHolder = new MetaTileEntityHolder();
                            newHolder.setMetaTileEntity(holder.getMetaTileEntity().createMetaTileEntity(newHolder));
                            newHolder.getMetaTileEntity().setFrontFacing(holder.getMetaTileEntity().getFrontFacing());

                            positionData = new BlockInfo(positionData.getBlockState(), newHolder);
                        }
                        if (this.idealDir != this.structureDir) {
                            IntTriple blockInfoPosition = transformPos(i, j, k, shape.size(), aisleEntry.length, rowEntry.length(), true);
                            blockInfos[blockInfoPosition.getLeft()][blockInfoPosition.getMiddle()][blockInfoPosition.getRight()] = positionData;
                        } else {
                            blockInfos[i][j][k] = positionData;
                        }
                    }
                }
            }
            return blockInfos;
        }

        // Transforms from the builder's structureDir to the default (RIGHT, UP, FRONT) at a position in the 3D array. If
        private IntTriple transformPos(int posZ, int posY, int posX, int maxZ, int maxY, int maxX, boolean canReverseLines) { // This reversal is required because the array structure does not fit easily with the structureDir formatting.
            BlockPattern.RelativeDirection[] currentDir = this.structureDir.clone();
            int[] position = {posX, posY, posZ};
            int[] bounds = {maxX, maxY, maxZ};

            // First pass: swap all lefts, downs, and backs to their corresponding opposite sides.
            for (int i = 0; i < 3; i++) {
                if (currentDir[i] == LEFT || currentDir[i] == DOWN || currentDir[i] == BACK) {
                    if (canReverseLines) {
                        position[i] = bounds[i] - position[i] - 1;
                    }
                    switch (currentDir[i]) {
                        case LEFT:
                            currentDir[i] = RIGHT;
                            break;
                        case DOWN:
                            currentDir[i] = UP;
                            break;
                        case BACK:
                            currentDir[i] = FRONT;
                            break;
                    }

                }
            }

            // Second pass: check the first and second elements to see if they're the correct directions for their particular position, and if not, swap them.
            for (int i = 0; i < 2; i++) {
                if (currentDir[i] != idealDir[i]) {
                    for (int j = i; j < 3; j++) {
                        if (currentDir[j] == idealDir[i]) {
                            currentDir[j] = currentDir[i];
                            currentDir[i] = idealDir[i];
                            int tempPos = position[i];
                            position[i] = position[j];
                            position[j] = tempPos;
                        }
                    }
                }
            }

            return new IntTriple(position[2], position[1], position[0]);
        }

        @Override
        public TJMultiblockShapeInfo build() {
            return new TJMultiblockShapeInfo(this.bakeArray());
        }
    }
}
