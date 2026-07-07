package tj.integration.ae2.part;

import appeng.api.AEApi;
import appeng.api.config.*;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartModel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.core.settings.TickRates;
import appeng.items.parts.PartModels;
import appeng.me.GridAccessException;
import appeng.parts.PartModel;
import appeng.parts.misc.PartInterface;
import appeng.tile.inventory.AppEngInternalAEInventory;
import appeng.tile.networking.TileCableBus;
import appeng.util.item.AEItemStack;
import gregtech.api.gui.ModularUI;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import tj.TJ;
import tj.integration.ae2.ISuperInterface;
import tj.integration.ae2.blocks.BlockStockingInterface;
import tj.integration.ae2.helpers.DualitySuperInterface;
import tj.items.item.TJItems;
import tj.mui.uifactory.ITileEntityUI;
import tj.mui.uifactory.TileEntityHolder;
import tj.mui.widgets.impl.*;

import javax.annotation.Nonnull;


public class PartStockingInterface extends PartInterface implements ITileEntityUI, ISuperInterface {

    public static final ResourceLocation MODEL_BASE = new ResourceLocation(TJ.MODID, "part/me.part.stocking_interface_base");

    @PartModels
    public static final PartModel MODELS_OFF = new PartModel(MODEL_BASE, new ResourceLocation(TJ.MODID, "part/me.part.stocking_interface_off"));

    @PartModels
    public static final PartModel MODELS_ON = new PartModel(MODEL_BASE, new ResourceLocation(TJ.MODID, "part/me.part.stocking_interface_on"));

    @PartModels
    public static final PartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE, new ResourceLocation(TJ.MODID, "part/me.part.stocking_interface_has_channel"));

    private int tickTime = 100;

    public PartStockingInterface(ItemStack is) {
        super(is);
        ObfuscationReflectionHelper.setPrivateValue(PartInterface.class, this, new DualitySuperInterface(this.getProxy(), this, 10, 36, 9), "duality");
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

    @Nonnull
    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(TickRates.Interface.getMin(), TickRates.Interface.getMax(), this.getInterfaceDuality().getConfigManager().getSetting(Settings.BLOCK) == YesNo.NO, false);
    }

    @Nonnull
    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        if (!this.getProxy().isActive())
            return TickRateModulation.SLEEP;
        final TickRateModulation tickRateModulation = super.tickingRequest(node, ticksSinceLastCall);
        if (this.getInterfaceDuality().getConfigManager().getSetting(Settings.BLOCK) == YesNo.YES) {
            try {
                int index = 0;
                final int stackSize = (int) Math.min(Integer.MAX_VALUE, 1024L << this.getInstalledUpgrades(Upgrades.CAPACITY) * 2);
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
        return TickRateModulation.values()[Math.max(tickRateModulation.ordinal(), this.tickTime > ticksSinceLastCall ? TickRateModulation.SLOWER.ordinal() : this.tickTime < ticksSinceLastCall ? TickRateModulation.FASTER.ordinal() : TickRateModulation.SAME.ordinal())];
    }

    @Override
    public void writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger("tickTime", this.tickTime);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.tickTime = Math.max(1, data.getInteger("tickTime"));
    }

    @Override
    public ModularUI createUI(TileEntityHolder holder, EntityPlayer player) {
        return BlockStockingInterface.createInterfaceGUI(holder, player, this);
    }

    @Override
    public ItemStack getItemStackRepresentation() {
        return TJItems.PART_STOCKING_INTERFACE.maybeStack(1).orElse(ItemStack.EMPTY);
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
    public void setAutoPull(boolean blockingMode) {
        this.getInterfaceDuality().getConfigManager().putSetting(Settings.BLOCK, blockingMode ? YesNo.YES : YesNo.NO);
        this.getTile().markDirty();
    }

    @Override
    public void setBlockingMode(boolean blockingMode) {
        // No pattern slots
    }

    @Override
    public void setLockCrafting(LockCraftingMode lockCraftingMode) {
        // No pattern slots
    }

    @Override
    public void setInterfaceTerminal(boolean interfaceTerminal) {
        // No pattern slots
    }

    @Override
    public void setFluidPacket(boolean fluidPacket) {
        // No pattern slots
    }

    @Override
    public void setSplittingItemsFluids(boolean splittingItemsFluids) {
        // No pattern slots
    }

    @Override
    public void setBlockModeEx(CondenserOutput blockModeEx) {
        // No pattern slots
    }

    @Override
    public void setIntelligentBlocking(boolean intelligentBlocking) {
        // No pattern slots
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
    public void setTickTime(String text, String id) {
        this.tickTime = (int) Math.max(1, Math.min(Integer.MAX_VALUE, Long.parseLong(text)));
        this.getTile().markDirty();
    }

    @Override
    public int getTickTime() {
        return this.tickTime;
    }

    @Override
    public void setPriority(String text, String id) {
        this.getInterfaceDuality().setPriority((int) Math.max(Integer.MIN_VALUE, Math.min(Integer.MAX_VALUE, Long.parseLong(text))));
        this.getTile().markDirty();
    }
}
