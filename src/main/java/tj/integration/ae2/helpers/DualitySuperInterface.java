package tj.integration.ae2.helpers;

import appeng.api.config.Actionable;
import appeng.api.config.Upgrades;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.storage.data.IAEItemStack;
import appeng.core.Api;
import appeng.helpers.DualityInterface;
import appeng.helpers.IInterfaceHost;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.parts.automation.UpgradeInventory;
import appeng.tile.inventory.AppEngInternalAEInventory;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.inv.IAEAppEngInventory;
import appeng.util.inv.filter.IAEItemFilter;
import net.minecraft.nbt.NBTTagCompound;
import tj.integration.ae2.inventory.TJAppEngNetworkInventory;
import appeng.util.inv.InvOperation;
import gregtech.api.util.GTLog;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.items.IItemHandler;
import tj.items.item.TJItems;
import tj.util.TJItemUtils;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;

public class DualitySuperInterface extends DualityInterface {

    public DualitySuperInterface(AENetworkProxy networkProxy, IInterfaceHost ih) {
        super(networkProxy, ih);
        ObfuscationReflectionHelper.setPrivateValue(DualityInterface.class, this, new IAEItemStack[18], "requireWork");
        ObfuscationReflectionHelper.setPrivateValue(DualityInterface.class, this, new AppEngInternalInventory(this, 72, 1), "patterns");
        ObfuscationReflectionHelper.setPrivateValue(DualityInterface.class, this, new AppEngInternalAEInventory(this, 18, 1024), "config");
        ObfuscationReflectionHelper.setPrivateValue(DualityInterface.class, this, new DualityUpgradeInventory(this, 10), "upgrades");
        try {
            Field mySource = ObfuscationReflectionHelper.findField(DualityInterface.class, "mySource");
            ObfuscationReflectionHelper.setPrivateValue(DualityInterface.class, this, new TJAppEngNetworkInventory(() -> {
                try {
                    return networkProxy.getStorage();
                } catch (GridAccessException e) {
                    return null;
                }
            }, (IActionSource) mySource.get(this), this, 18, 1024), "storage");
        } catch (IllegalAccessException e) {
            GTLog.logger.error("Error when trying to reflect on class {} for field storage", DualityInterface.class.getName());
        }
    }

    @Override
    public IAEItemStack injectCraftedItems(final ICraftingLink link, final IAEItemStack acquired, final Actionable mode) {
        return ((IDualitySuperInterface) this).injectTheCraftedItems(link, acquired, mode);
    }

    @Nonnull
    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        return ((IDualitySuperInterface) this).tickingTheRequest(node, ticksSinceLastCall);
    }

    @Override
    public void onChangeInventory(IItemHandler inv, int slot, InvOperation mc, ItemStack removed, ItemStack added) {
        ((IDualitySuperInterface) this).onChangeTheInventory(inv, slot, mc, removed, added);
    }

    public static class DualityUpgradeInventory extends UpgradeInventory {

        private int installedCapacity;
        private int installedPatterns;
        private int installedCraftingCard;

        public DualityUpgradeInventory(IAEAppEngInventory parent, int s) {
            super(parent, s);
            this.setFilter(new DualityFilter());
        }

        @Override
        public int getMaxInstalled(Upgrades upgrades) {
            switch (upgrades) {
                case CAPACITY: return this.installedCapacity;
                case PATTERN_EXPANSION: return this.installedPatterns;
                case CRAFTING: return this.installedCraftingCard;
                default: return 0;
            }
        }

        @Override
        public int getInstalledUpgrades(Upgrades u) {
            switch (u) {
                case CAPACITY: return this.installedCapacity;
                case PATTERN_EXPANSION: return this.installedPatterns;
                case CRAFTING: return this.installedCraftingCard;
                default: return 0;
            }
        }

        @Override
        protected void onContentsChanged(int slot) {
            this.installedCapacity = 0;
            this.installedPatterns = 0;
            this.installedCraftingCard = 0;
            final ItemStack capacity = Api.INSTANCE.definitions().materials().cardCapacity().maybeStack(1).orElse(ItemStack.EMPTY);
            final ItemStack pattern = Api.INSTANCE.definitions().materials().cardPatternExpansion().maybeStack(1).orElse(ItemStack.EMPTY);
            final ItemStack crafting = Api.INSTANCE.definitions().materials().cardCrafting().maybeStack(1).orElse(ItemStack.EMPTY);
            final ItemStack maxCapacity = new ItemStack(TJItems.MAX_CAPACITY_UPGRADE);
            for (int i = 0; i < this.getSlots(); i++) {
                final ItemStack stack = this.getStackInSlot(i);
                if (stack.isItemEqual(capacity)) {
                    this.installedCapacity++;
                } else if (stack.isItemEqual(pattern)) {
                    this.installedPatterns++;
                } else if (stack.isItemEqual(crafting)) {
                    this.installedCraftingCard++;
                } else if (stack.isItemEqual(maxCapacity)) {
                    this.installedCapacity = 16;
                }
            }
            super.onContentsChanged(slot);
        }

        @Override
        public NBTTagCompound serializeNBT() {
            final NBTTagCompound compound = super.serializeNBT();
            compound.setInteger("installedCapacity", this.installedCapacity);
            compound.setInteger("installedPatterns", this.installedPatterns);
            compound.setInteger("installedCraftingCard", this.installedCraftingCard);
            return compound;
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt) {
            super.deserializeNBT(nbt);
            this.installedCapacity = nbt.getInteger("installedCapacity");
            this.installedPatterns = nbt.getInteger("installedPatterns");
            this.installedCraftingCard = nbt.getInteger("installedCraftingCard");
        }
    }

    private static class DualityFilter implements IAEItemFilter {

        @Override
        public boolean allowExtract(IItemHandler iItemHandler, int slot, int i1) {
            return true;
        }

        @Override
        public boolean allowInsert(IItemHandler iItemHandler, int slot, ItemStack itemStack) {
            final ItemStack capacity = Api.INSTANCE.definitions().materials().cardCapacity().maybeStack(1).orElse(ItemStack.EMPTY);
            final ItemStack pattern = Api.INSTANCE.definitions().materials().cardPatternExpansion().maybeStack(1).orElse(ItemStack.EMPTY);
            final ItemStack crafting = Api.INSTANCE.definitions().materials().cardCrafting().maybeStack(1).orElse(ItemStack.EMPTY);
            final ItemStack maxCapacity = new ItemStack(TJItems.MAX_CAPACITY_UPGRADE);
            return itemStack.isItemEqual(capacity) ||
                    (itemStack.isItemEqual(pattern) && TJItemUtils.extractFromItemHandler(iItemHandler, pattern, Integer.MAX_VALUE, true).getCount() <= 7) ||
                    (itemStack.isItemEqual(crafting) && TJItemUtils.extractFromItemHandler(iItemHandler, crafting, Integer.MAX_VALUE, true).getCount() <= 1) ||
                    (itemStack.isItemEqual(maxCapacity) && TJItemUtils.extractFromItemHandler(iItemHandler, maxCapacity, Integer.MAX_VALUE, true).getCount() <= 1);
        }
    }
}
