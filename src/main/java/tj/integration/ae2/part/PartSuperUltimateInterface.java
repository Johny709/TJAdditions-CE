package tj.integration.ae2.part;

import appeng.api.AEApi;
import appeng.api.config.*;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartModel;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.core.settings.TickRates;
import appeng.fluids.helper.DualityFluidInterface;
import appeng.fluids.util.AEFluidStack;
import appeng.helpers.DualityInterface;
import appeng.items.parts.PartModels;
import appeng.me.GridAccessException;
import appeng.parts.PartModel;
import appeng.parts.misc.PartInterface;
import appeng.tile.inventory.AppEngInternalAEInventory;
import appeng.tile.networking.TileCableBus;
import appeng.util.item.AEItemStack;
import com.circulation.random_complement.client.RCSettings;
import com.circulation.random_complement.client.buttonsetting.IntelligentBlocking;
import com.circulation.random_complement.common.interfaces.RCIConfigurableObject;
import gregtech.api.gui.ModularUI;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import tj.TJ;
import tj.integration.ae2.ISuperDualInterface;
import tj.integration.ae2.blocks.BlockSuperUltimateInterface;
import tj.integration.ae2.helpers.DualitySuperFluidInterface;
import tj.integration.ae2.helpers.DualitySuperInterface;
import tj.items.item.TJItems;
import tj.mui.uifactory.ITileEntityUI;
import tj.mui.uifactory.TileEntityHolder;
import tj.util.TJItemUtils;

import javax.annotation.Nonnull;
import java.util.List;


public class PartSuperUltimateInterface extends PartInterface implements ITileEntityUI, ISuperDualInterface {

    public static final ResourceLocation MODEL_BASE = new ResourceLocation(TJ.MODID, "part/me.part.super_ultimate_interface_base");

    @PartModels
    public static final PartModel MODELS_OFF = new PartModel(MODEL_BASE, new ResourceLocation(TJ.MODID, "part/me.part.super_ultimate_interface_off"));

    @PartModels
    public static final PartModel MODELS_ON = new PartModel(MODEL_BASE, new ResourceLocation(TJ.MODID, "part/me.part.super_ultimate_interface_on"));

    @PartModels
    public static final PartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE, new ResourceLocation(TJ.MODID, "part/me.part.super_ultimate_interface_has_channel"));

    private final DualitySuperFluidInterface dualityFluid = new DualitySuperFluidInterface(this.getProxy(), this, 72);
    private final BlockPos.MutableBlockPos interfacePos = new BlockPos.MutableBlockPos();
    private int tickTime = 100;

    public PartSuperUltimateInterface(ItemStack is) {
        super(is);
        ObfuscationReflectionHelper.setPrivateValue(PartInterface.class, this, new DualitySuperInterface(this.getProxy(), this, 160, 72, 1152), "duality");
        this.getInterfaceDuality().getConfigManager().registerSetting(Settings.PLACE_BLOCK, YesNo.NO);
        this.getInterfaceDuality().getConfigManager().registerSetting(Settings.STICKY_MODE, YesNo.NO);
        this.getDualityFluidInterface().getConfigManager().registerSetting(Settings.PLACE_BLOCK, YesNo.NO);
        this.getDualityFluidInterface().getConfigManager().registerSetting(Settings.STICKY_MODE, YesNo.NO);
    }

    @Override
    public boolean onPartActivate(EntityPlayer player, EnumHand hand, Vec3d pos) {
        final TileCableBus tileCableBus = (TileCableBus) this.getTile();
        if (tileCableBus != null && !player.getEntityWorld().isRemote) {
            TileEntityHolder holder = new TileEntityHolder(tileCableBus);
            holder.setFacing(this.getSide().getFacing());
            holder.openUI((EntityPlayerMP) player);
        }
        return true;
    }

    @Override
    public ModularUI createUI(TileEntityHolder holder, EntityPlayer player) {
        return BlockSuperUltimateInterface.createDualInterfaceGUI(holder, player, this);
    }

    @Override
    public void gridChanged() {
        super.gridChanged();
        this.dualityFluid.gridChanged();
    }

    @Override
    public void writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        final NBTTagCompound compound = new NBTTagCompound();
        this.dualityFluid.writeToNBT(compound);
        data.setTag("dualityFluid", compound);
        data.setInteger("stockingItem", this.getInterfaceDuality().getConfigManager().getSetting(Settings.PLACE_BLOCK).ordinal());
        data.setInteger("autoOutputItem", this.getInterfaceDuality().getConfigManager().getSetting(Settings.STICKY_MODE).ordinal());
        data.setInteger("stockingFluid", this.getDualityFluidInterface().getConfigManager().getSetting(Settings.PLACE_BLOCK).ordinal());
        data.setInteger("autoOutputFluid", this.getDualityFluidInterface().getConfigManager().getSetting(Settings.STICKY_MODE).ordinal());
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.dualityFluid.readFromNBT(data.getCompoundTag("dualityFluid"));
        this.getInterfaceDuality().getConfigManager().putSetting(Settings.PLACE_BLOCK, YesNo.values()[data.getInteger("stockingItem")]);
        this.getInterfaceDuality().getConfigManager().putSetting(Settings.STICKY_MODE, YesNo.values()[data.getInteger("autoOutputItem")]);
        this.getDualityFluidInterface().getConfigManager().putSetting(Settings.PLACE_BLOCK, YesNo.values()[data.getInteger("stockingFluid")]);
        this.getDualityFluidInterface().getConfigManager().putSetting(Settings.STICKY_MODE, YesNo.values()[data.getInteger("autoOutputFluid")]);
    }

    @Nonnull
    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        if (!this.getProxy().isActive())
            return TickRateModulation.SLEEP;
        final TickRateModulation tickRateModulation = TickRateModulation.values()[Math.max(super.tickingRequest(node, ticksSinceLastCall).ordinal(), this.dualityFluid.tickingRequest(node, ticksSinceLastCall).ordinal())];
        if (this.getInterfaceDuality().getConfigManager().getSetting(Settings.PLACE_BLOCK) == YesNo.YES) {
            try {
                int index = 0;
                final int stackSize = (int) Math.min(Integer.MAX_VALUE, 1024L << this.getInterfaceDuality().getInstalledUpgrades(Upgrades.CAPACITY) * 2);
                final IItemList<?> iItemList = this.getProxy().getStorage().getInventory(AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class)).getStorageList();
                for (IAEStack<?> items : iItemList) {
                    if (index < this.getInterfaceDuality().getConfig().getSlots()) {
                        if (!items.isItem()) continue;
                        final AEItemStack aeItemStack = (AEItemStack) items;
                        final ItemStack itemStack = aeItemStack.createItemStack();
                        if (itemStack.isEmpty()) continue;
                        itemStack.setCount(Math.min(itemStack.getCount(), stackSize));
                        ((AppEngInternalAEInventory) this.getInterfaceDuality().getConfig()).setStackInSlot(index++, itemStack);
                    } else break;
                }
            } catch (GridAccessException ignored) {}
        }
        if (this.getInterfaceDuality().getConfigManager().getSetting(Settings.STICKY_MODE) == YesNo.YES) {
            final BlockPos pos = this.getTile().getPos();
            for (EnumFacing facing : this.getTargets()) {
                this.interfacePos.setPos(pos.getX(), pos.getY(), pos.getZ());
                final TileEntity tileEntity = this.getTile().getWorld().getTileEntity(this.interfacePos.move(facing));
                if (tileEntity != null) {
                    final IItemHandler itemHandler = this.getInterfaceDuality().getStorage();
                    final IItemHandler destItemHandler = tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing.getOpposite());
                    if (destItemHandler != null) {
                        for (int i = 0; i < itemHandler.getSlots(); i++) {
                            final ItemStack stack = itemHandler.getStackInSlot(i);
                            if (!stack.isEmpty()) {
                                final int inserted = TJItemUtils.insertIntoItemHandler(destItemHandler, stack, true).getCount();
                                final int extract = stack.getCount() - inserted;
                                if (extract < 1) continue;
                                final ItemStack otherStack = itemHandler.extractItem(i, extract, false);
                                TJItemUtils.insertIntoItemHandler(destItemHandler, otherStack, false);
                            }
                        }
                    }
                }
            }
        }
        if (this.getDualityFluidInterface().getConfigManager().getSetting(Settings.PLACE_BLOCK) == YesNo.YES) {
            try {
                int index = 0;
                final int stackSize = (int) Math.min(Integer.MAX_VALUE, 64000L << this.getDualityFluidInterface().getInstalledUpgrades(Upgrades.CAPACITY) * 2);
                final IItemList<?> iItemList = this.getProxy().getStorage().getInventory(AEApi.instance().storage().getStorageChannel(IFluidStorageChannel.class)).getStorageList();
                for (IAEStack<?> fluids : iItemList) {
                    if (index < this.getDualityFluidInterface().getConfig().getSlots()) {
                        if (fluids.isItem()) continue;
                        final AEFluidStack aeFluidStack = (AEFluidStack) fluids;
                        final FluidStack fluidStack = aeFluidStack.getFluidStack();
                        fluidStack.amount = Math.min(fluidStack.amount, stackSize);
                        this.getDualityFluidInterface().getConfig().setFluidInSlot(index++, AEFluidStack.fromFluidStack(fluidStack));
                    } else break;
                }
            } catch (GridAccessException ignored) {}
        }
        if (this.getDualityFluidInterface().getConfigManager().getSetting(Settings.STICKY_MODE) == YesNo.YES) {
            final BlockPos pos = this.getTile().getPos();
            for (EnumFacing facing : this.getTargets()) {
                this.interfacePos.setPos(pos.getX(), pos.getY(), pos.getZ());
                final TileEntity tileEntity = this.getTile().getWorld().getTileEntity(this.interfacePos.move(facing));
                if (tileEntity != null) {
                    final IFluidHandler fluidHandler = this.getDualityFluidInterface().getTanks();
                    final IFluidHandler destFluidHandler = tileEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing.getOpposite());
                    if (destFluidHandler != null) {
                        for (IFluidTankProperties tank : fluidHandler.getTankProperties()) {
                            FluidStack fluidStack = tank.getContents();
                            if (fluidStack != null) {
                                fluidStack = fluidHandler.drain(fluidStack, false);
                                if (fluidStack == null) continue;
                                fluidStack.amount = destFluidHandler.fill(fluidStack, true);
                                fluidHandler.drain(fluidStack, true);
                            }
                        }
                    }
                }
            }
        }
        return TickRateModulation.values()[Math.max(tickRateModulation.ordinal(), this.tickTime > ticksSinceLastCall ? TickRateModulation.SLOWER.ordinal() : this.tickTime < ticksSinceLastCall ? TickRateModulation.FASTER.ordinal() : TickRateModulation.SAME.ordinal())];
    }

    @Nonnull
    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(TickRates.Interface.getMin(), TickRates.Interface.getMax(), super.getTickingRequest(node).isSleeping && this.dualityFluid.getTickingRequest(node).isSleeping && super.getTickingRequest(node).isSleeping && this.dualityFluid.getTickingRequest(node).isSleeping && this.getInterfaceDuality().getConfigManager().getSetting(Settings.PLACE_BLOCK) == YesNo.NO && this.getDualityFluidInterface().getConfigManager().getSetting(Settings.PLACE_BLOCK) == YesNo.NO, true);
    }

    @Override
    public boolean hasCapability(Capability<?> capabilityClass) {
        return super.hasCapability(capabilityClass) || this.dualityFluid.hasCapability(capabilityClass, null);
    }

    @Override
    public <T> T getCapability(Capability<T> capabilityClass) {
        final T capability = super.getCapability(capabilityClass);
        return capability != null ? capability : this.dualityFluid.getCapability(capabilityClass, null);
    }

    @Nonnull
    @Override
    public IPartModel getStaticModels() {
        if (this.isActive() && this.isPowered()) {
            return MODELS_HAS_CHANNEL;
        } else if (this.isPowered()) {
            return MODELS_ON;
        } else {
            return MODELS_OFF;
        }
    }

    @Override
    public ItemStack getItemStackRepresentation() {
        return TJItems.PART_SUPER_DUAL_INTERFACE.maybeStack(1).orElse(ItemStack.EMPTY);
    }

    @Override
    public DualityFluidInterface getDualityFluidInterface() {
        return this.dualityFluid;
    }

    @Override
    public IFluidHandler getFluidInventoryByName(String name) {
        return this.dualityFluid.getFluidInventoryByName(name);
    }

    @Override
    public void getDrops(List<ItemStack> drops, boolean wrenched) {
        super.getDrops(drops, wrenched);
        this.dualityFluid.addDrops(drops);
    }

    @Override
    public void setBlockingMode(boolean blockingMode) {
        this.getInterfaceDuality().getConfigManager().putSetting(Settings.BLOCK, blockingMode ? YesNo.YES : YesNo.NO);
        this.getTile().markDirty();
    }

    @Override
    public void setLockCrafting(LockCraftingMode lockCrafting) {
        this.getInterfaceDuality().getConfigManager().putSetting(Settings.UNLOCK, lockCrafting);
        this.getTile().markDirty();
    }

    @Override
    public void setInterfaceTerminal(boolean interfaceTerminal) {
        this.getInterfaceDuality().getConfigManager().putSetting(Settings.INTERFACE_TERMINAL, interfaceTerminal ? YesNo.YES : YesNo.NO);
        this.getTile().markDirty();
    }

    @Override
    public void setFluidPacket(boolean fluidPacket) {
        this.getInterfaceDuality().getConfigManager().putSetting(Settings.OPERATION_MODE, fluidPacket ? OperationMode.FILL : OperationMode.EMPTY);
        ObfuscationReflectionHelper.setPrivateValue(DualityInterface.class, this.getInterfaceDuality(), fluidPacket, "fluidPacket");
        this.getTile().markDirty();
    }

    @Override
    public void setSplittingItemsFluids(boolean splittingItemsFluids) {
        this.getInterfaceDuality().getConfigManager().putSetting(Settings.LEVEL_TYPE, splittingItemsFluids ? LevelType.ITEM_LEVEL : LevelType.ENERGY_LEVEL);
        ObfuscationReflectionHelper.setPrivateValue(DualityInterface.class, this.getInterfaceDuality(), splittingItemsFluids, "allowSplitting");
        this.getTile().markDirty();
    }

    @Override
    public void setBlockModeEx(CondenserOutput blockModeEx) {
        this.getInterfaceDuality().getConfigManager().putSetting(Settings.CONDENSER_OUTPUT, blockModeEx);
        ObfuscationReflectionHelper.setPrivateValue(DualityInterface.class, this.getInterfaceDuality(), blockModeEx.ordinal(), "blockModeEx");
        this.getTile().markDirty();
    }

    @Override
    public void setIntelligentBlocking(boolean intelligentBlocking) {
        ((RCIConfigurableObject) this.getInterfaceDuality()).r$getConfigManager().putSetting(RCSettings.IntelligentBlocking, intelligentBlocking ? IntelligentBlocking.OPEN : IntelligentBlocking.CLOSE);
        this.getTile().markDirty();
    }

    @Override
    public void setStackSize(String text, String id) {
        final int slot = Integer.parseInt(id);
        final int maxSize = this.getInterfaceDuality().getConfig().getSlotLimit(0);
        final int stackSize = (int) Math.max(1, Math.min(Long.parseLong(text), maxSize));
        final ItemStack itemStack = this.getInterfaceDuality().getConfig().extractItem(slot, Integer.MAX_VALUE, false);
        if (itemStack.isEmpty()) return;
        itemStack.setCount(stackSize);
        ((AppEngInternalAEInventory) this.getInterfaceDuality().getConfig()).setStackInSlot(slot, itemStack);
        this.getTile().markDirty();
    }

    @Override
    public String getStackSize(int index) {
        return String.valueOf(this.getInterfaceDuality().getConfig().getStackInSlot(index).getCount());
    }

    @Override
    public void setPriority(String text, String id) {
        this.getInterfaceDuality().setPriority((int) Math.max(Integer.MIN_VALUE, Math.min(Integer.MAX_VALUE, Long.parseLong(text))));
        this.getTile().markDirty();
    }

    @Override
    public void setTickTime(String tickTime, String id) {
        this.tickTime = (int) Math.max(1, Math.min(Integer.MAX_VALUE, Long.parseLong(tickTime)));
        this.getTile().markDirty();
    }

    @Override
    public int getTickTime() {
        return this.tickTime;
    }

    @Override
    public void setItemAutoPull(boolean autoPull) {
        this.getInterfaceDuality().getConfigManager().putSetting(Settings.PLACE_BLOCK, autoPull ? YesNo.YES : YesNo.NO);
        this.getTile().markDirty();
    }

    @Override
    public void setFluidAutoPull(boolean autoPull) {
        this.getDualityFluidInterface().getConfigManager().putSetting(Settings.PLACE_BLOCK, autoPull ? YesNo.YES : YesNo.NO);
        this.getTile().markDirty();
    }

    @Override
    public void setItemAutoPush(boolean autoPush) {
        this.getInterfaceDuality().getConfigManager().putSetting(Settings.STICKY_MODE, autoPush ? YesNo.YES : YesNo.NO);
        this.getTile().markDirty();
    }

    @Override
    public void setFluidAutoPush(boolean autoPush) {
        this.getDualityFluidInterface().getConfigManager().putSetting(Settings.STICKY_MODE, autoPush ? YesNo.YES : YesNo.NO);
        this.getTile().markDirty();
    }
}
