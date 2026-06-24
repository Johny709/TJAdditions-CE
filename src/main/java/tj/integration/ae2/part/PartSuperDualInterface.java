package tj.integration.ae2.part;

import appeng.api.config.*;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartModel;
import appeng.core.settings.TickRates;
import appeng.fluids.helper.DualityFluidInterface;
import appeng.helpers.DualityInterface;
import appeng.items.parts.PartModels;
import appeng.parts.PartModel;
import appeng.parts.misc.PartInterface;
import appeng.tile.inventory.AppEngInternalAEInventory;
import appeng.tile.networking.TileCableBus;
import com.circulation.random_complement.client.RCSettings;
import com.circulation.random_complement.client.buttonsetting.IntelligentBlocking;
import com.circulation.random_complement.common.interfaces.RCIConfigurableObject;
import gregtech.api.gui.ModularUI;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import tj.TJ;
import tj.integration.ae2.ISuperDualInterface;
import tj.integration.ae2.blocks.BlockSuperDualInterface;
import tj.mui.uifactory.ITileEntityUI;
import tj.mui.uifactory.TileEntityHolder;
import tj.integration.ae2.helpers.DualitySuperFluidInterface;
import tj.integration.ae2.helpers.DualitySuperInterface;
import tj.items.item.TJItems;

import javax.annotation.Nonnull;
import java.util.List;


public class PartSuperDualInterface extends PartInterface implements ITileEntityUI, ISuperDualInterface {

    public static final ResourceLocation MODEL_BASE = new ResourceLocation(TJ.MODID, "part/me.part.super_dual_interface_base");

    @PartModels
    public static final PartModel MODELS_OFF = new PartModel(MODEL_BASE, new ResourceLocation(TJ.MODID, "part/me.part.super_dual_interface_off"));

    @PartModels
    public static final PartModel MODELS_ON = new PartModel(MODEL_BASE, new ResourceLocation(TJ.MODID, "part/me.part.super_dual_interface_on"));

    @PartModels
    public static final PartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE, new ResourceLocation(TJ.MODID, "part/me.part.super_dual_interface_has_channel"));

    private final DualitySuperFluidInterface dualityFluid = new DualitySuperFluidInterface(this.getProxy(), this, 18);

    public PartSuperDualInterface(ItemStack is) {
        super(is);
        ObfuscationReflectionHelper.setPrivateValue(PartInterface.class, this, new DualitySuperInterface(this.getProxy(), this, 10, 18, 72), "duality");
    }

    @Override
    public boolean onPartActivate(EntityPlayer player, EnumHand hand, Vec3d pos) {
        final TileCableBus tileCableBus = (TileCableBus) this.getTile();
        if (tileCableBus != null) {
            if (!player.getEntityWorld().isRemote) {
                TileEntityHolder holder = new TileEntityHolder(tileCableBus);
                holder.setFacing(this.getSide().getFacing());
                holder.openUI((EntityPlayerMP) player);
            }
            return true;
        }
        return true;
    }

    @Override
    public void gridChanged() {
        super.gridChanged();
        this.dualityFluid.gridChanged();
    }

    @Nonnull
    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(TickRates.Interface.getMin(), TickRates.Interface.getMax(), super.getTickingRequest(node).isSleeping && this.dualityFluid.getTickingRequest(node).isSleeping, true);
    }

    @Nonnull
    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        return TickRateModulation.values()[Math.max(super.tickingRequest(node, ticksSinceLastCall).ordinal(), this.dualityFluid.tickingRequest(node, ticksSinceLastCall).ordinal())];
    }

    @Override
    public void writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        final NBTTagCompound compound = new NBTTagCompound();
        this.dualityFluid.writeToNBT(compound);
        data.setTag("dualityFluid", compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.dualityFluid.readFromNBT(data.getCompoundTag("dualityFluid"));
    }

    @Override
    public ModularUI createUI(TileEntityHolder holder, EntityPlayer player) {
        return BlockSuperDualInterface.createDualInterfaceGUI(holder, player, this);
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
    public void setAutoPull(boolean autoPull) {
        // No such feature
    }
}
