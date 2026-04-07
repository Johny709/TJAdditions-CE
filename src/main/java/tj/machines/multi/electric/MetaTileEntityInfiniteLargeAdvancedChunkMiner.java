package tj.machines.multi.electric;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.render.ICubeRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import tj.builder.multicontrollers.TJMultiblockControllerBase;
import tj.capability.IProgressBar;
import tj.capability.ProgressBar;
import tj.capability.impl.handler.IMinerHandler;

import java.util.Queue;
import java.util.function.UnaryOperator;

public class MetaTileEntityInfiniteLargeAdvancedChunkMiner extends TJMultiblockControllerBase implements IMinerHandler, IProgressBar {

    public MetaTileEntityInfiniteLargeAdvancedChunkMiner(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    protected void updateFormedValid() {

    }

    @Override
    protected BlockPattern createStructurePattern() {
        return null;
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return null;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return null;
    }

    @Override
    public int getFortuneLvl() {
        return 0;
    }

    @Override
    public int getDiameter() {
        return 0;
    }

    @Override
    public FluidStack getDrillingFluid() {
        return null;
    }

    @Override
    public int[][] getBarMatrix() {
        return new int[1][1];
    }

    @Override
    public void getProgressBars(Queue<UnaryOperator<ProgressBar.ProgressBarBuilder>> bars) {

    }
}
