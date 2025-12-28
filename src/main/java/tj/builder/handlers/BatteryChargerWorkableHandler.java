package tj.builder.handlers;

import gregtech.api.capability.IElectricItem;
import gregtech.api.metatileentity.MetaTileEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import tj.builder.multicontrollers.TJMultiblockDisplayBase;
import tj.capability.impl.AbstractWorkableHandler;
import tj.util.ItemStackHelper;
import tj.util.PlayerWorldIDData;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

import static gregtech.api.capability.GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM;
import static gregtech.api.unification.material.Materials.Nitrogen;
import static net.minecraftforge.energy.CapabilityEnergy.ENERGY;
import static tj.builder.handlers.BatteryChargerWorkableHandler.TransferMode.INPUT;

public class BatteryChargerWorkableHandler extends AbstractWorkableHandler<BatteryChargerWorkableHandler> {

    private boolean transferToOutput;
    private TransferMode transferMode = INPUT;
    private EntityPlayer[] linkedPlayers;
    private UUID[] linkedPlayersID;
    private String[] entityLinkName;
    private long totalEnergyPerTick;
    private int[] entityLinkWorld;
    private int fluidConsumption;
    private NBTTagCompound linkData;

    public BatteryChargerWorkableHandler(MetaTileEntity metaTileEntity) {
        super(metaTileEntity);
        this.maxProgress = 1;
    }

    @Override
    public BatteryChargerWorkableHandler initialize(int busCount) {
        this.linkedPlayers = this.linkedPlayers != null ? Arrays.copyOf(this.linkedPlayers, this.tierSupplier.getAsInt()) : new EntityPlayer[this.tierSupplier.getAsInt()];
        this.linkedPlayersID = this.linkedPlayersID != null ? Arrays.copyOf(this.linkedPlayersID, this.tierSupplier.getAsInt()) : new UUID[this.tierSupplier.getAsInt()];
        this.entityLinkName = this.entityLinkName != null ? Arrays.copyOf(this.entityLinkName, this.tierSupplier.getAsInt()) : new String[this.tierSupplier.getAsInt()];
        this.entityLinkWorld = this.entityLinkWorld != null ? Arrays.copyOf(this.entityLinkWorld, this.tierSupplier.getAsInt()) : new int[this.tierSupplier.getAsInt()];
        this.energyPerTick = (long) (Math.pow(4, this.tierSupplier.getAsInt()) * 8);
        this.updateTotalEnergyPerTick();
        return super.initialize(busCount);
    }

    @Override
    protected boolean startRecipe() {
        if (this.metaTileEntity.getOffsetTimer() % 200 == 0)
            this.playerLinkUpdate();
        return true;
    }

    @Override
    protected void progressRecipe(int progress) {
        this.progress++;
    }

    @Override
    protected boolean completeRecipe() {
        for (int i = 0; i < this.linkedPlayers.length; i++) {
            EntityPlayer linkedPlayer = this.linkedPlayers[i];
            if (linkedPlayer == null)
                continue;

            if (this.metaTileEntity.getWorld().provider.getDimension() != linkedPlayer.world.provider.getDimension()) {
                FluidStack fluidStack = Nitrogen.getPlasma(this.fluidConsumption);
                if (fluidStack.isFluidStackIdentical(this.importFluidsSupplier.get().drain(fluidStack, false)))
                    this.importFluidsSupplier.get().drain(fluidStack, true);
                else continue;
            }

            for (int j = 0; j < linkedPlayer.inventory.armorInventory.size(); j++) {
                ItemStack stack = linkedPlayer.inventory.armorInventory.get(j);
                if (stack.isEmpty())
                    continue;
                IEnergyStorage RFContainer = stack.getCapability(ENERGY, null);
                this.transferRF((int) this.energyPerTick, RFContainer, this.transferMode, stack, false);

                IElectricItem EUContainer = stack.getCapability(CAPABILITY_ELECTRIC_ITEM, null);
                this.transferEU(this.energyPerTick, EUContainer, this.transferMode, stack, false);
            }

            for (int j = 0; j < linkedPlayer.inventory.mainInventory.size(); j++) {
                ItemStack stack = linkedPlayer.inventory.mainInventory.get(j);
                if (stack.isEmpty())
                    continue;
                IEnergyStorage RFContainer = stack.getCapability(ENERGY, null);
                this.transferRF((int) this.energyPerTick, RFContainer, this.transferMode, stack, false);

                IElectricItem EUContainer = stack.getCapability(CAPABILITY_ELECTRIC_ITEM, null);
                this.transferEU(this.energyPerTick, EUContainer, this.transferMode, stack, false);
            }

            for (int j = 0; j < linkedPlayer.inventory.offHandInventory.size(); j++) {
                ItemStack stack = linkedPlayer.inventory.offHandInventory.get(j);
                if (stack.isEmpty())
                    continue;
                IEnergyStorage RFContainer = stack.getCapability(ENERGY, null);
                this.transferRF((int) this.energyPerTick, RFContainer, this.transferMode, stack, false);

                IElectricItem EUContainer = stack.getCapability(CAPABILITY_ELECTRIC_ITEM, null);
                this.transferEU(this.energyPerTick, EUContainer, this.transferMode, stack, false);
            }
        }
        for (int i = 0; i < this.importItemsSupplier.get().getSlots(); i++) {
            ItemStack stack = this.importItemsSupplier.get().getStackInSlot(i);
            if (stack.isEmpty())
                continue;

            IEnergyStorage RFContainer = stack.getCapability(ENERGY, null);
            this.transferRF((int) this.energyPerTick, RFContainer, this.transferMode, stack, this.transferToOutput);

            IElectricItem EUContainer = stack.getCapability(CAPABILITY_ELECTRIC_ITEM, null);
            this.transferEU(this.energyPerTick, EUContainer, this.transferMode, stack, this.transferToOutput);
        }
        if (this.metaTileEntity instanceof TJMultiblockDisplayBase)
            ((TJMultiblockDisplayBase) this.metaTileEntity).calculateMaintenance(this.maxProgress);
        return true;
    }

    private void playerLinkUpdate() {
        for (int i = 0; i < this.linkedPlayersID.length; i++) {
            if (this.linkedPlayersID[i] == null)
                continue;

            int worldID = PlayerWorldIDData.getINSTANCE().getPlayerWorldIdMap().get(this.linkedPlayersID[i]);
            this.linkedPlayers[i] = DimensionManager.getWorld(worldID).getPlayerEntityByUUID(this.linkedPlayersID[i]);
            this.entityLinkWorld[i] = worldID;
        }
        this.updateTotalEnergyPerTick();
    }

    private void transferRF(int energyToAdd, IEnergyStorage RFContainer, TransferMode transferMode, ItemStack stack, boolean transferToOutput) {
        if (RFContainer == null)
            return;
        if (transferMode == INPUT) {
            int energyRemainingToFill = RFContainer.getMaxEnergyStored() - RFContainer.getEnergyStored();
            if (RFContainer.getEnergyStored() < 1 || energyRemainingToFill != 0) {
                int energyInserted = Math.min(energyToAdd, energyRemainingToFill);
                RFContainer.receiveEnergy((int) Math.abs(this.importEnergySupplier.get().removeEnergy(energyInserted / 4) * 4), false);
            }
        } else {
            long energyRemainingToFill = (this.exportEnergySupplier.get().getEnergyCapacity() - this.exportEnergySupplier.get().getEnergyStored());
            if (this.exportEnergySupplier.get().getEnergyStored() < 1 || energyRemainingToFill != 0) {
                int energyExtracted = RFContainer.extractEnergy((int) Math.min(Integer.MAX_VALUE, Math.min(energyToAdd * 4L, energyRemainingToFill)), false);
                this.exportEnergySupplier.get().addEnergy(energyExtracted / 4);
            }
            if (transferToOutput && RFContainer.getEnergyStored() < 1 && ItemStackHelper.insertIntoItemHandler(this.exportItemsSupplier.get(), stack, true).isEmpty())
                ItemStackHelper.insertIntoItemHandler(this.exportItemsSupplier.get(), stack, false);
        }
    }

    private void transferEU(long energyToAdd, IElectricItem EUContainer, TransferMode transferMode, ItemStack stack, boolean transferToOutput) {
        if (EUContainer == null)
            return;
        if (transferMode == INPUT) {
            long energyRemainingToFill = EUContainer.getMaxCharge() - EUContainer.getCharge();
            if (EUContainer.getCharge() < 1 || energyRemainingToFill != 0) {
                long energyInserted = Math.min(energyRemainingToFill, energyToAdd);
                EUContainer.charge(Math.abs(this.importEnergySupplier.get().removeEnergy(energyInserted)), this.tierSupplier.getAsInt(), true, false);
            }
        } else {
            long energyRemainingToFill = this.exportEnergySupplier.get().getEnergyCapacity() - this.exportEnergySupplier.get().getEnergyStored();
            if (this.exportEnergySupplier.get().getEnergyStored() < 1 || energyRemainingToFill != 0) {
                long energyExtracted = EUContainer.discharge(Math.min(energyRemainingToFill, energyToAdd), this.tierSupplier.getAsInt(), true, true,false);
                this.exportEnergySupplier.get().addEnergy(energyExtracted);
            }
            if (transferToOutput && EUContainer.getCharge() < 1 && ItemStackHelper.insertIntoItemHandler(this.exportItemsSupplier.get(), stack, true).isEmpty())
                ItemStackHelper.insertIntoItemHandler(this.exportItemsSupplier.get(), stack, false);
        }
    }

    public void renameLink(String name, String id) {
        int index = id.lastIndexOf(";");
        index = Integer.parseInt(id.substring(index + 1));
        this.entityLinkName[index] = name;
        this.metaTileEntity.markDirty();
    }

    public void updateTotalEnergyPerTick() {
        int dimensionID = this.metaTileEntity.getWorld().provider.getDimension();
        int linkedWorldsCount = (int) Arrays.stream(this.entityLinkWorld).filter(id -> id != dimensionID && id != Integer.MIN_VALUE).count();
        this.fluidConsumption = 10 * linkedWorldsCount;
        int slots = this.importItemsSupplier.get().getSlots();
        long amps = slots + Arrays.stream(this.linkedPlayers).filter(Objects::nonNull).count();
        this.totalEnergyPerTick = (long) (Math.pow(4, this.tierSupplier.getAsInt()) * 8) * amps;
    }

    public void setReset(boolean reset) {
        Arrays.fill(this.linkedPlayers, null);
        Arrays.fill(this.linkedPlayersID, null);
        Arrays.fill(this.entityLinkName, null);
        Arrays.fill(this.entityLinkWorld, Integer.MIN_VALUE);
        this.linkData.setInteger("I", this.linkedPlayers.length);
        this.updateTotalEnergyPerTick();
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound data = super.serializeNBT();
        NBTTagList linkList = new NBTTagList();
        for (int i = 0; i < this.linkedPlayersID.length; i++) {
            if (this.linkedPlayersID[i] != null) {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setInteger("index", i);
                tag.setUniqueId("playerID", this.linkedPlayersID[i]);
                tag.setString("name", this.entityLinkName[i]);
                tag.setInteger("world", this.entityLinkWorld[i]);
                linkList.appendTag(tag);
            }
        }
        data.setTag("links", linkList);
        data.setInteger("transferMode", this.transferMode.ordinal());
        data.setBoolean("transferToOutput", this.transferToOutput);
        data.setInteger("linkPlayersSize", this.linkedPlayers.length);
        data.setLong("totalEnergyPerTick", this.totalEnergyPerTick);
        if (this.linkData != null)
            data.setTag("link.XYZ", this.linkData);
        return data;
    }

    @Override
    public void deserializeNBT(NBTTagCompound data) {
        super.deserializeNBT(data);
        this.totalEnergyPerTick = data.getLong("totalEnergyPerTick");
        this.transferMode = TransferMode.values()[data.getInteger("transferMode")];
        this.transferToOutput = data.getBoolean("transferToOutput");
        this.linkedPlayers = new EntityPlayer[data.getInteger("linkPlayersSize")];
        this.linkedPlayersID = new UUID[data.getInteger("linkPlayersSize")];
        this.entityLinkName = new String[data.getInteger("linkPlayersSize")];
        this.entityLinkWorld = new int[data.getInteger("linkPlayersSize")];
        NBTTagList linkList = data.getTagList("links", Constants.NBT.TAG_COMPOUND);
        for (NBTBase nbtBase : linkList) {
            NBTTagCompound tag = (NBTTagCompound) nbtBase;
            int i = tag.getInteger("index");
            this.linkedPlayersID[i] = tag.getUniqueId("playerID");
            this.entityLinkName[i] = tag.getString("name");
            this.entityLinkWorld[i] = tag.getInteger("world");
        }
        if (data.hasKey("link.XYZ"))
            this.linkData = data.getCompoundTag("link.XYZ");
    }

    public void setLinkData(NBTTagCompound linkData) {
        this.linkData = linkData;
        this.metaTileEntity.markDirty();
    }

    public NBTTagCompound getLinkData() {
        return this.linkData;
    }

    public long getTotalEnergyPerTick() {
        return this.totalEnergyPerTick;
    }

    public String[] getEntityLinkName() {
        return this.entityLinkName;
    }

    public int[] getEntityLinkWorld() {
        return this.entityLinkWorld;
    }

    public EntityPlayer[] getLinkedPlayers() {
        return this.linkedPlayers;
    }

    public UUID[] getLinkedPlayersID() {
        return this.linkedPlayersID;
    }

    public int getFluidConsumption() {
        return this.fluidConsumption;
    }

    public boolean isTransferToOutput() {
        return this.transferToOutput;
    }

    public void setTransferToOutput(boolean transferToOutput) {
        this.transferToOutput = transferToOutput;
    }

    public void setTransferMode(TransferMode transferMode) {
        this.transferMode = transferMode;
    }

    public TransferMode getTransferMode() {
        return this.transferMode;
    }

    public enum TransferMode {
        INPUT,
        OUTPUT
    }
}
