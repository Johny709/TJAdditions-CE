package tj.machines.multi.electric;

import gregicadditions.client.ClientHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.render.ICubeRenderer;
import net.minecraft.util.ResourceLocation;

import java.util.List;
import java.util.Map;

import static gregtech.api.metatileentity.multiblock.MultiblockAbility.OUTPUT_ENERGY;

public class MetaTileEntityLargeWirelessEnergyReceiver extends MetaTileEntityLargeWirelessEnergyEmitter {

    public MetaTileEntityLargeWirelessEnergyReceiver(ResourceLocation metaTileEntityId, TransferType transferType) {
        super(metaTileEntityId, transferType);
        this.transferType = transferType;
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
}
