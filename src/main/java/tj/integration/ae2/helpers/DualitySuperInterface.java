package tj.integration.ae2.helpers;

import appeng.api.config.*;
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
import tj.TJ;
import tj.integration.ae2.inventory.TJAppEngNetworkInventory;
import appeng.util.inv.InvOperation;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.items.IItemHandler;
import tj.items.item.TJItems;
import tj.util.TJItemUtils;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;

public class DualitySuperInterface extends DualityInterface {

    private final IDualitySuperInterface superDuality = (IDualitySuperInterface) this;

    public DualitySuperInterface(AENetworkProxy networkProxy, IInterfaceHost ih, int upgradeSlots, int storageSlots, int patterns) {
        super(networkProxy, ih);
        // dummy config for Send Real Fluid, Field: (boolean) fluid packet
        this.getConfigManager().registerSetting(Settings.OPERATION_MODE, OperationMode.FILL);
        // dummy config for Allow Splitting Items and Fluids, Field: (boolean) allowSplitting
        this.getConfigManager().registerSetting(Settings.LEVEL_TYPE, LevelType.ENERGY_LEVEL);
        // dummy config for Block All, Field: (int) blockModeEx
        this.getConfigManager().registerSetting(Settings.CONDENSER_OUTPUT, CondenserOutput.TRASH);
        // dummy config for Intelligent Blocking, Field (RCIConfigManager) randomComplement$rcSettings
        this.getConfigManager().registerSetting(Settings.PLACE_BLOCK, YesNo.NO);

        final AppEngInternalInventory patternInventory = new AppEngInternalInventory(this, patterns, 1);
        patternInventory.setFilter(new DualityPatternFilter());
        ObfuscationReflectionHelper.setPrivateValue(DualityInterface.class, this, new IAEItemStack[storageSlots], "requireWork");
        ObfuscationReflectionHelper.setPrivateValue(DualityInterface.class, this, patternInventory, "patterns");
        ObfuscationReflectionHelper.setPrivateValue(DualityInterface.class, this, new AppEngInternalAEInventory(this, storageSlots, 1024), "config");
        ObfuscationReflectionHelper.setPrivateValue(DualityInterface.class, this, new DualityUpgradeInventory(this, upgradeSlots), "upgrades");
        try {
            Field mySource = ObfuscationReflectionHelper.findField(DualityInterface.class, "mySource");
            ObfuscationReflectionHelper.setPrivateValue(DualityInterface.class, this, new TJAppEngNetworkInventory(() -> {
                try {
                    return networkProxy.getStorage();
                } catch (GridAccessException e) {
                    return null;
                }
            }, (IActionSource) mySource.get(this), this, storageSlots, 1024), "storage");
        } catch (IllegalAccessException e) {
            TJ.logger.error("Error when trying to reflect on class {} for field storage", DualityInterface.class.getName());
        }
    }

    @Override
    public IAEItemStack injectCraftedItems(final ICraftingLink link, final IAEItemStack acquired, final Actionable mode) {
        return this.superDuality.injectTheCraftedItems(link, acquired, mode);
    }

    @Nonnull
    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        return this.superDuality.tickingTheRequest(node, ticksSinceLastCall);
    }

    @Override
    public void onChangeInventory(IItemHandler inv, int slot, InvOperation mc, ItemStack removed, ItemStack added) {
        this.superDuality.onChangeTheInventory(inv, slot, mc, removed, added);
    }

    @Override
    public void writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        this.superDuality.serializeToNBT(data);
        data.setInteger("fluidPacketToggle", this.getConfigManager().getSetting(Settings.OPERATION_MODE).ordinal());
        data.setInteger("allowSplittingToggle", this.getConfigManager().getSetting(Settings.LEVEL_TYPE).ordinal());
        data.setInteger("blockModeExCycle", this.getConfigManager().getSetting(Settings.CONDENSER_OUTPUT).ordinal());
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.superDuality.deserializeFromNBT(data);
        this.getConfigManager().putSetting(Settings.OPERATION_MODE, OperationMode.values()[data.getInteger("fluidPacketToggle")]);
        this.getConfigManager().putSetting(Settings.LEVEL_TYPE, LevelType.values()[data.getInteger("allowSplittingToggle")]);
        this.getConfigManager().putSetting(Settings.CONDENSER_OUTPUT, CondenserOutput.values()[data.getInteger("blockModeExCycle")]);
    }

    public static class DualityUpgradeInventory extends UpgradeInventory {

        private int installedCapacity;
        private int installedPatterns;
        private int installedCraftingCard;

        public DualityUpgradeInventory(IAEAppEngInventory parent, int s) {
            super(parent, s);
            this.setFilter(new DualityFilter((DualityInterface) parent));
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

        private final DualityInterface duality;

        public DualityFilter(DualityInterface duality) {
            this.duality = duality;
        }

        @Override
        public boolean allowExtract(IItemHandler iItemHandler, int slot, int i1) {
            final ItemStack stack = iItemHandler.getStackInSlot(slot);
            if (stack.isItemEqual(Api.INSTANCE.definitions().materials().cardCapacity().maybeStack(1).orElse(ItemStack.EMPTY)) || stack.isItemEqual(new ItemStack(TJItems.MAX_CAPACITY_UPGRADE))) {
                final boolean hasMaxUpgrade = TJItemUtils.extractFromItemHandler(iItemHandler, new ItemStack(TJItems.MAX_CAPACITY_UPGRADE), Integer.MAX_VALUE, true).getCount() > 1;
                final long threshold = 1024L << (TJItemUtils.extractFromItemHandler(iItemHandler, Api.INSTANCE.definitions().materials().cardCapacity().maybeStack(1).orElse(ItemStack.EMPTY), Integer.MAX_VALUE, true).getCount() - (hasMaxUpgrade ? 1 : 2)) * 2;
                for (int i = 0; i < this.duality.getStorage().getSlots(); i++) {
                    final ItemStack itemStack = this.duality.getStorage().getStackInSlot(i);
                    if (itemStack.isEmpty()) continue;
                    if (itemStack.getCount() > threshold)
                        return false;
                }
            } else if (stack.isItemEqual(Api.INSTANCE.definitions().materials().cardPatternExpansion().maybeStack(1).orElse(ItemStack.EMPTY))) {
                final ItemStack patterns = TJItemUtils.extractFromItemHandler(iItemHandler, Api.INSTANCE.definitions().materials().cardPatternExpansion().maybeStack(1).orElse(ItemStack.EMPTY), Integer.MAX_VALUE, true);
                final IItemHandler patternHandler = this.duality.getInventoryByName("patterns");
                final int index = 9 * (patterns.getCount() - 1);
                for (int i = index; i < Math.min(patternHandler.getSlots(), index + 9); i++) {
                    if (!patternHandler.getStackInSlot(i).isEmpty())
                        return false;
                }
            }
            return true;
        }

        @Override
        public boolean allowInsert(IItemHandler iItemHandler, int slot, ItemStack itemStack) {
            final ItemStack capacity = Api.INSTANCE.definitions().materials().cardCapacity().maybeStack(1).orElse(ItemStack.EMPTY);
            final ItemStack pattern = Api.INSTANCE.definitions().materials().cardPatternExpansion().maybeStack(1).orElse(ItemStack.EMPTY);
            final ItemStack crafting = Api.INSTANCE.definitions().materials().cardCrafting().maybeStack(1).orElse(ItemStack.EMPTY);
            final ItemStack maxCapacity = new ItemStack(TJItems.MAX_CAPACITY_UPGRADE);
            return itemStack.isItemEqual(capacity) && TJItemUtils.extractFromItemHandler(iItemHandler, capacity, Integer.MAX_VALUE, true).getCount() <= 4 ||
                    (itemStack.isItemEqual(pattern) && TJItemUtils.extractFromItemHandler(iItemHandler, pattern, Integer.MAX_VALUE, true).getCount() <= (duality.getPatterns().getSlots() / 9) - 1) ||
                    (itemStack.isItemEqual(crafting) && TJItemUtils.extractFromItemHandler(iItemHandler, crafting, Integer.MAX_VALUE, true).getCount() <= 1) ||
                    (itemStack.isItemEqual(maxCapacity) && TJItemUtils.extractFromItemHandler(iItemHandler, maxCapacity, Integer.MAX_VALUE, true).getCount() <= 1);
        }
    }

    private static class DualityPatternFilter implements IAEItemFilter {

        @Override
        public boolean allowExtract(IItemHandler iItemHandler, int slot, int i1) {
            return true;
        }

        @Override
        public boolean allowInsert(IItemHandler iItemHandler, int slot, ItemStack itemStack) {
            if (itemStack.isItemEqual(Api.INSTANCE.definitions().items().encodedPattern().maybeStack(1).orElse(ItemStack.EMPTY)) || itemStack.isItemEqual(TJItemUtils.getItemStackFromName("ae2fc:dense_encoded_pattern"))) {
                if (itemStack.getTagCompound() == null)
                    return false;
                for (int i = 0; i < iItemHandler.getSlots(); i++) {
                    final ItemStack stack = iItemHandler.getStackInSlot(i);
                    if (stack.getTagCompound() != null && stack.getTagCompound().equals(itemStack.getTagCompound()))
                        return false;
                }
                return true;
            }
            return false;
        }
    }
}
