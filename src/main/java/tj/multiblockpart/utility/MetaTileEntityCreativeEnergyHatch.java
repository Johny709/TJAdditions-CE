package tj.multiblockpart.utility;

import gregicadditions.GAValues;
import gregicadditions.machines.multi.multiblockpart.GAMetaTileEntityMultiblockPart;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.impl.EnergyContainerHandler;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import java.util.List;

public class MetaTileEntityCreativeEnergyHatch extends GAMetaTileEntityMultiblockPart implements IMultiblockAbilityPart<IEnergyContainer> {

    private final EnergyContainerHandler energyContainer = new EnergyContainerHandler(this, Long.MAX_VALUE, 0, 0, 0, 0) {

        @Override
        public long changeEnergy(long energyToAdd) {
            return energyToAdd;
        }

        @Override
        public long getEnergyStored() {
            return energyStored;
        }

        @Override
        public long getInputVoltage() {
            return inputVoltage;
        }

        @Override
        public long getInputAmperage() {
            return inputAmps;
        }
    };

    private long energyStored;
    private long inputVoltage;
    private long inputAmps;

    public MetaTileEntityCreativeEnergyHatch(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, GAValues.MAX);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityCreativeEnergyHatch(this.metaTileEntityId);
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return null;
    }

    @Override
    public MultiblockAbility<IEnergyContainer> getAbility() {
        return MultiblockAbility.INPUT_ENERGY;
    }

    @Override
    public void registerAbilities(List<IEnergyContainer> list) {
        list.add(this.energyContainer);
    }
}
