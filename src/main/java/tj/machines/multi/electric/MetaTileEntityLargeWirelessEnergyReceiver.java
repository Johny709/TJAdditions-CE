package tj.machines.multi.electric;

import gregicadditions.client.ClientHandler;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.multiblock.PatternMatchContext;
import gregtech.api.render.ICubeRenderer;
import net.minecraft.util.ResourceLocation;

import java.util.List;
import java.util.Map;

import static gregtech.api.metatileentity.multiblock.MultiblockAbility.OUTPUT_ENERGY;

public class MetaTileEntityLargeWirelessEnergyReceiver extends MetaTileEntityLargeWirelessEnergyEmitter {

    private IEnergyContainer outputEnergyContainer;

    public MetaTileEntityLargeWirelessEnergyReceiver(ResourceLocation metaTileEntityId, TransferType transferType) {
        super(metaTileEntityId, transferType);
        this.transferType = transferType;
        this.workableHandler.setExportEnergySupplier(this::getOutputEnergyContainer);
        this.reinitializeStructurePattern();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityLargeWirelessEnergyReceiver(this.metaTileEntityId, this.transferType);
    }

    @Override
    protected boolean checkStructureComponents(List<IMultiblockPart> parts, Map<MultiblockAbility<Object>, List<Object>> abilities) {
        return abilities.containsKey(OUTPUT_ENERGY) && super.checkStructureComponents(parts, abilities);
    }

    @Override
    protected boolean hasEnoughEnergy(long amount) {
        return true;
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        this.outputEnergyContainer = new EnergyContainerList(getAbilities(OUTPUT_ENERGY));
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return ClientHandler.RED_STEEL_CASING;
    }

    @Override
    public long getEnergyStored() {
        return this.outputEnergyContainer.getEnergyStored();
    }

    @Override
    public long getEnergyCapacity() {
        return this.outputEnergyContainer.getEnergyCapacity();
    }

    private IEnergyContainer getOutputEnergyContainer() {
        return this.outputEnergyContainer;
    }
}
