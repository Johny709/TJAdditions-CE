package tj.builder.handlers;

import gregtech.api.block.machines.BlockMachine;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.metatileentity.MetaTileEntity;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import tj.builder.multicontrollers.TJMultiblockDisplayBase;
import tj.capability.impl.AbstractWorkableHandler;

import java.util.Arrays;

import static gregtech.api.capability.GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER;
import static gregtech.api.unification.material.Materials.Nitrogen;
import static net.minecraftforge.energy.CapabilityEnergy.ENERGY;

public class LargeWirelessEnergyWorkableHandler extends AbstractWorkableHandler<LargeWirelessEnergyWorkableHandler> {

    private String[] entityLinkName;
    private BlockPos[] entityLinkBlockPos;
    private int[] entityLinkWorld;
    private int[] entityEnergyAmps;
    private int linkedWorldsCount;
    private int fluidConsumption;
    private byte mode;
    private NBTTagCompound linkData;

    public LargeWirelessEnergyWorkableHandler(MetaTileEntity metaTileEntity) {
        super(metaTileEntity);
        this.maxProgress = 1;
    }

    @Override
    public LargeWirelessEnergyWorkableHandler initialize(int mode) {
        this.mode = (byte) mode;
        int linkAmount = this.tierSupplier.getAsInt() * 2;
        this.entityLinkName = this.entityLinkName != null ? Arrays.copyOf(this.entityLinkName, linkAmount) : new String[linkAmount];
        this.entityLinkBlockPos = this.entityLinkBlockPos != null ? Arrays.copyOf(this.entityLinkBlockPos, linkAmount) : new BlockPos[linkAmount];
        this.entityEnergyAmps = this.entityEnergyAmps != null ? Arrays.copyOf(this.entityEnergyAmps, linkAmount) : new int[linkAmount];
        if (this.entityLinkWorld != null) {
            this.entityLinkWorld = Arrays.copyOf(this.entityLinkWorld, linkAmount);
        } else {
            this.entityLinkWorld = new int[linkAmount];
            Arrays.fill(this.entityLinkWorld, this.metaTileEntity.getWorld().provider.getDimension());
        }
        this.energyPerTick = (long) (Math.pow(4, this.tierSupplier.getAsInt()) * 8);
        this.updateTotalEnergyPerTick();
        int dimensionID = this.metaTileEntity.getWorld().provider.getDimension();
        this.linkedWorldsCount = (int) Arrays.stream(this.entityLinkWorld).filter(id -> id != dimensionID && id != Integer.MIN_VALUE).count();
        this.fluidConsumption = 10 * this.linkedWorldsCount;
        if (this.linkData != null) {
            int size = this.linkData.getInteger("Size") - this.entityLinkBlockPos.length;
            int remaining = Math.max(0, (this.linkData.getInteger("I") - size));
            this.linkData.setInteger("Size", this.entityLinkBlockPos.length);
            this.linkData.setInteger("I", remaining);
        }
        return super.initialize(mode);
    }

    @Override
    protected boolean startRecipe() {
        return true;
    }

    @Override
    protected void progressRecipe(int progress) {
        this.progress++;
    }

    @Override
    protected boolean completeRecipe() {
        for (int i = 0; i < this.entityLinkBlockPos.length; i++) {
            if (this.entityLinkBlockPos[i] == null)
                continue;
            if (this.metaTileEntity.getWorld().provider.getDimension() != this.entityLinkWorld[i]) {
                int fluidToConsume = this.fluidConsumption / this.linkedWorldsCount;
                FluidStack fluidStack = Nitrogen.getPlasma(fluidToConsume);
                if (fluidStack.isFluidStackIdentical(this.importFluidsSupplier.get().drain(fluidStack, false)))
                    this.importFluidsSupplier.get().drain(fluidStack, true);
                else continue;
            }
            WorldServer world = DimensionManager.getWorld(this.entityLinkWorld[i]);
            if (world == null)
                continue;
            Chunk chunk = world.getChunk(this.entityLinkBlockPos[i]);
            if (!chunk.isLoaded())
                chunk.onLoad();
            TileEntity tileEntity = world.getTileEntity(this.entityLinkBlockPos[i]);
            MetaTileEntity metaTileEntity = BlockMachine.getMetaTileEntity(world, this.entityLinkBlockPos[i]);
            long energyToAdd = this.energyPerTick * this.entityEnergyAmps[i];
            if (tileEntity != null) {
                IEnergyStorage RFContainer = tileEntity.getCapability(ENERGY, null);
                this.transferRF((int) energyToAdd, RFContainer);
            }
            if (metaTileEntity != null) {
                IEnergyContainer EUContainer = metaTileEntity.getCapability(CAPABILITY_ENERGY_CONTAINER, null);
                this.transferEU(energyToAdd, EUContainer);
            }
            if (this.metaTileEntity instanceof TJMultiblockDisplayBase)
                ((TJMultiblockDisplayBase) this.metaTileEntity).calculateMaintenance(this.maxProgress);
        }
        return true;
    }

    protected void transferRF(int energyToAdd, IEnergyStorage RFContainer) {
        if (RFContainer != null) {
            if (this.mode == 0) {
                int energyRemainingToFill = RFContainer.getMaxEnergyStored() - RFContainer.getEnergyStored();
                if (RFContainer.getEnergyStored() < 1 || energyRemainingToFill != 0) {
                    int energyInserted = RFContainer.receiveEnergy(Math.min(Integer.MAX_VALUE, energyToAdd * 4), false);
                    this.importEnergySupplier.get().removeEnergy(energyInserted / 4);
                }
            } else {
                long energyRemainingToFill = this.exportEnergySupplier.get().getEnergyCapacity() - this.exportEnergySupplier.get().getEnergyStored();
                if (this.exportEnergySupplier.get().getEnergyStored() < 1 || energyRemainingToFill != 0) {
                    int energyExtracted = RFContainer.extractEnergy((int) Math.min(Integer.MAX_VALUE, Math.min(energyToAdd * 4L, energyRemainingToFill)), false);
                    this.exportEnergySupplier.get().addEnergy(energyExtracted / 4);
                }
            }
        }
    }

    protected void transferEU(long energyToAdd, IEnergyContainer EUContainer) {
        if (EUContainer != null) {
            if (this.mode == 0) {
                long energyRemainingToFill = EUContainer.getEnergyCapacity() - EUContainer.getEnergyStored();
                if (EUContainer.getEnergyStored() < 1 || energyRemainingToFill != 0) {
                    long energyInserted = EUContainer.addEnergy(Math.min(energyToAdd, energyRemainingToFill));
                    this.importEnergySupplier.get().removeEnergy(energyInserted);
                }
            } else {
                long energyRemainingToFill = this.exportEnergySupplier.get().getEnergyCapacity() - this.exportEnergySupplier.get().getEnergyStored();
                if (this.exportEnergySupplier.get().getEnergyStored() < 1 || energyRemainingToFill != 0) {
                    long energyExtracted = EUContainer.removeEnergy(energyToAdd);
                    this.exportEnergySupplier.get().addEnergy(Math.abs(energyExtracted));
                }
            }
        }
    }


    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound data = super.serializeNBT();
        NBTTagList linkList = new NBTTagList();
        for (int i = 0; i < this.entityLinkBlockPos.length; i++) {
            if (this.entityLinkBlockPos[i] != null) {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setInteger("index", i);
                tag.setInteger("x", this.entityLinkBlockPos[i].getX());
                tag.setInteger("y", this.entityLinkBlockPos[i].getY());
                tag.setInteger("z", this.entityLinkBlockPos[i].getZ());
                tag.setInteger("world", this.entityLinkWorld[i]);
                tag.setInteger("energyAmps", this.entityEnergyAmps[i]);
                tag.setString("name", this.entityLinkName[i]);
                linkList.appendTag(tag);
            }
        }
        data.setTag("links", linkList);
        data.setInteger("blockPosSize", this.entityLinkBlockPos.length);
        if (this.linkData != null)
            data.setTag("link.XYZ", this.linkData);
        return data;
    }

    @Override
    public void deserializeNBT(NBTTagCompound data) {
        super.deserializeNBT(data);
        this.entityLinkName = new String[data.getInteger("blockPosSize")];
        this.entityLinkBlockPos = new BlockPos[data.getInteger("blockPosSize")];
        this.entityLinkWorld = new int[data.getInteger("blockPosSize")];
        this.entityEnergyAmps = new int[data.getInteger("blockPosSize")];
        NBTTagList linkList = data.getTagList("links", Constants.NBT.TAG_COMPOUND);
        for (NBTBase nbtBase : linkList) {
            NBTTagCompound tag = (NBTTagCompound) nbtBase;
            int i = tag.getInteger("index");
            this.entityLinkBlockPos[i] = new BlockPos(tag.getInteger("x"), tag.getInteger("y"), tag.getInteger("z"));
            this.entityLinkWorld[i] = tag.getInteger("world");
            this.entityEnergyAmps[i] = tag.getInteger("energyAmps");
            this.entityLinkName[i] = tag.getString("name");
        }
        if (data.hasKey("link.XYZ"))
            this.linkData = data.getCompoundTag("link.XYZ");
    }

    public void renameLink(String name, String id) {
        int index = id.lastIndexOf(";");
        index = Integer.parseInt(id.substring(index + 1));
        this.entityLinkName[index] = name;
        this.metaTileEntity.markDirty();
    }

    public void updateTotalEnergyPerTick() {
        int amps = Arrays.stream(this.entityEnergyAmps).sum();
        this.energyPerTick = (long) (Math.pow(4, this.tierSupplier.getAsInt()) * 8) * amps;
        this.metaTileEntity.markDirty();
    }

    public void onLink(MetaTileEntity metaTileEntity) {
        this.updateTotalEnergyPerTick();
        int dimensionID = this.metaTileEntity.getWorld().provider.getDimension();
        this.linkedWorldsCount = (int) Arrays.stream(this.entityLinkWorld).filter(id -> id != dimensionID && id != Integer.MIN_VALUE).count();
        this.fluidConsumption = 10 * this.linkedWorldsCount;
    }

    public void setReset(boolean reset) {
        Arrays.fill(this.entityLinkName, null);
        Arrays.fill(this.entityLinkBlockPos, null);
        Arrays.fill(this.entityLinkWorld, Integer.MIN_VALUE);
        Arrays.fill(this.entityEnergyAmps, 0);
        this.linkData.setInteger("I", this.entityLinkBlockPos.length);
        this.updateTotalEnergyPerTick();
    }

    public void setLinkData(NBTTagCompound linkData) {
        this.linkData = linkData;
        this.metaTileEntity.markDirty();
    }

    public NBTTagCompound getLinkData() {
        return this.linkData;
    }

    public int getFluidConsumption() {
        return this.fluidConsumption;
    }

    public String[] getEntityLinkName() {
        return this.entityLinkName;
    }

    public BlockPos[] getEntityLinkBlockPos() {
        return this.entityLinkBlockPos;
    }

    public int[] getEntityLinkWorld() {
        return this.entityLinkWorld;
    }

    public int[] getEntityEnergyAmps() {
        return this.entityEnergyAmps;
    }
}
