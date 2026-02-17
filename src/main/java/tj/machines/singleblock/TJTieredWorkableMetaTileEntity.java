package tj.machines.singleblock;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import gregicadditions.GAValues;
import gregicadditions.machines.overrides.GATieredMetaTileEntity;
import gregtech.api.capability.*;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.CoverDefinition;
import gregtech.api.cover.ICoverable;
import gregtech.api.metatileentity.MTETrait;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import tj.TJValues;
import tj.capability.IMachineHandler;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Serves as a basis to implement your own recipe or workable handler for single blocks.
 */
public abstract class TJTieredWorkableMetaTileEntity extends GATieredMetaTileEntity implements IActiveOutputSide, IMachineHandler {

    protected final ItemStackHandler chargerInventory;
    private EnumFacing outputFacing = this.getInitialFacing();
    private boolean allowInputFromOutputSide;
    private boolean isItemAutoOutput;
    private boolean isFluidAutoOutput;

    public TJTieredWorkableMetaTileEntity(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
        this.chargerInventory = new ItemStackHandler(1) {
            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }
        };
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        String color = TJValues.VCC[this.getTier()];
        tooltip.add(I18n.format("machine.universal.tooltip.voltage_in", this.energyContainer.getInputVoltage(), color, GAValues.VN[this.getTier()]));
        tooltip.add(I18n.format("gregtech.universal.tooltip.energy_storage_capacity", this.energyContainer.getEnergyCapacity()));
    }

    @Override
    protected boolean shouldUpdate(MTETrait trait) {
        return false;
    }

    @Override
    public void update() {
        super.update();
        if (!this.getWorld().isRemote) {
            IElectricItem chargerContainer = this.chargerInventory.getStackInSlot(0).getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
            if (chargerContainer != null && this.energyContainer.getEnergyStored() >= this.getMaxVoltage()) {
                this.energyContainer.removeEnergy(chargerContainer.charge(this.getMaxVoltage(), 1, true, false));
            }
            if (this.getOffsetTimer() % 5 == 0) {
                if (this.isItemAutoOutput)
                    this.pushItemsIntoNearbyHandlers(this.outputFacing);
                if (this.isFluidAutoOutput)
                    this.pushFluidsIntoNearbyHandlers(this.outputFacing);
            }
        }
    }

    @Override
    public boolean onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        EnumFacing hitFacing = ICoverable.determineGridSideHit(hitResult);
        if (facing == this.getOutputFacing() || (hitFacing == this.getOutputFacing() && playerIn.isSneaking())) {
            if (!this.getWorld().isRemote) {
                if (isAllowInputFromOutputSide()) {
                    this.setAllowInputFromOutputSide(false);
                    playerIn.sendMessage(new TextComponentTranslation("gregtech.machine.basic.input_from_output_side.disallow"));
                } else {
                    this.setAllowInputFromOutputSide(true);
                    playerIn.sendMessage(new TextComponentTranslation("gregtech.machine.basic.input_from_output_side.allow"));
                }
            }
            return true;
        }
        return super.onScrewdriverClick(playerIn, hand, facing, hitResult);
    }

    @Override
    public boolean onWrenchClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        if (!playerIn.isSneaking()) {
            EnumFacing currentOutputSide = this.getOutputFacing();
            if (currentOutputSide == facing || this.getFrontFacing() == facing) {
                return false;
            }
            if (!this.getWorld().isRemote) {
                this.setOutputFacing(facing);
            }
            return true;
        }
        return super.onWrenchClick(playerIn, hand, facing, hitResult);
    }

    @Override
    public boolean placeCoverOnSide(EnumFacing side, ItemStack itemStack, CoverDefinition coverDefinition) {
        boolean coverPlaced = super.placeCoverOnSide(side, itemStack, coverDefinition);
        if (coverPlaced && this.getOutputFacing() == side) {
            CoverBehavior cover = getCoverAtSide(side);
            if (cover != null && cover.shouldCoverInteractWithOutputside()) {
                this.setAllowInputFromOutputSide(true);
            }
        }
        return coverPlaced;
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        switch (dataId) {
            case 100: this.outputFacing = EnumFacing.VALUES[buf.readByte()]; break;
            case 101: this.isItemAutoOutput = buf.readBoolean(); break;
            case 102: this.isFluidAutoOutput = buf.readBoolean(); break;
        }
        this.scheduleRenderUpdate();
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(this.isItemAutoOutput);
        buf.writeBoolean(this.isFluidAutoOutput);
        buf.writeByte(this.outputFacing.getIndex());
        buf.writeBoolean(this.allowInputFromOutputSide);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.isItemAutoOutput = buf.readBoolean();
        this.isFluidAutoOutput = buf.readBoolean();
        this.outputFacing = EnumFacing.VALUES[buf.readByte()];
        this.allowInputFromOutputSide = buf.readBoolean();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean("isItemAutoOutput", this.isItemAutoOutput);
        data.setBoolean("isFluidAutoOutput", this.isFluidAutoOutput);
        data.setBoolean("allowInputFromOutputSide", this.allowInputFromOutputSide);
        data.setInteger("outputFacing", this.outputFacing.getIndex());
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.isItemAutoOutput = data.getBoolean("isItemAutoOutput");
        this.isFluidAutoOutput = data.getBoolean("isFluidAutoOutput");
        this.allowInputFromOutputSide = data.getBoolean("allowInputFromOutputSide");
        if (data.hasKey("outputFacing"))
            this.outputFacing = EnumFacing.VALUES[data.getInteger("outputFacing")];
    }

    protected void setOutputFacing(EnumFacing outputFacing) {
        this.outputFacing = outputFacing;
        if (!this.getWorld().isRemote) {
            this.getHolder().notifyBlockUpdate();
            this.writeCustomData(100, buf -> buf.writeByte(outputFacing.getIndex()));
            this.markDirty();
        }
    }

    protected void setItemAutoOutput(boolean isItemAutoOutput) {
        this.isItemAutoOutput = isItemAutoOutput;
        if (!this.getWorld().isRemote) {
            this.writeCustomData(101, buffer -> buffer.writeBoolean(isItemAutoOutput));
            this.markDirty();
        }
    }

    protected void setFluidAutoOutput(boolean isFluidAutoOutput) {
        this.isFluidAutoOutput = isFluidAutoOutput;
        if (!this.getWorld().isRemote) {
            this.writeCustomData(102, buffer -> buffer.writeBoolean(isFluidAutoOutput));
            this.markDirty();
        }
    }

    public void setAllowInputFromOutputSide(boolean allowInputFromOutputSide) {
        this.allowInputFromOutputSide = allowInputFromOutputSide;
        if (!this.getWorld().isRemote) {
            this.getHolder().notifyBlockUpdate();
            this.writeCustomData(103, buffer -> buffer.writeBoolean(allowInputFromOutputSide));
            this.markDirty();
        }
    }

    public EnumFacing getOutputFacing() {
        return this.outputFacing;
    }

    public EnumFacing getInitialFacing() {
        return this.frontFacing.getOpposite();
    }

    @Override
    public boolean isAutoOutputItems() {
        return this.isItemAutoOutput;
    }

    @Override
    public boolean isAutoOutputFluids() {
        return isFluidAutoOutput;
    }

    @Override
    public boolean isAllowInputFromOutputSide() {
        return this.allowInputFromOutputSide;
    }

    @Override
    public IItemHandlerModifiable getInputBus(int index) {
        return this.getImportItems();
    }

    @Override
    public IItemHandlerModifiable getImportItemInventory() {
        return this.getImportItems();
    }

    @Override
    public IItemHandlerModifiable getExportItemInventory() {
        return this.getExportItems();
    }

    @Override
    public IMultipleTankHandler getImportFluidTank() {
        return this.getImportFluids();
    }

    @Override
    public IMultipleTankHandler getExportFluidTank() {
        return this.getExportFluids();
    }

    @Override
    public IEnergyContainer getInputEnergyContainer() {
        return this.energyContainer;
    }

    @Override
    public long getMaxVoltage() {
        return GAValues.V[this.getTier()];
    }

    @Override
    public int getTier() {
        return super.getTier();
    }
}
