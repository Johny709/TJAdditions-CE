package tj.integration.appeng.container.implementations;

import appeng.api.AEApi;
import appeng.api.config.*;
import appeng.api.definitions.IItemDefinition;
import appeng.api.util.IConfigManager;
import appeng.client.me.SlotME;
import appeng.container.guisync.GuiSync;
import appeng.container.implementations.ContainerUpgradeable;
import appeng.container.slot.*;
import appeng.helpers.DualityInterface;
import appeng.helpers.IInterfaceHost;
import appeng.util.Platform;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ContainerSuperInterface extends ContainerUpgradeable implements IOptionalSlotHost {

    private final DualityInterface myDuality;

    @GuiSync(3)
    public YesNo bMode = YesNo.NO;

    @GuiSync(4)
    public LockCraftingMode lMode = LockCraftingMode.NONE;

    @GuiSync(7)
    public int patternExpansions = 0;

    @GuiSync(8)
    public YesNo iTermMode = YesNo.YES;

    @GuiSync(9)
    public LockCraftingMode lockReason = LockCraftingMode.NONE;

    public ContainerSuperInterface(final InventoryPlayer ip, final IInterfaceHost te) {
        super(ip, te.getInterfaceDuality().getHost());
        this.myDuality = te.getInterfaceDuality();

        for (int row = 0; row < 4; ++row) {
            for (int x = 0; x < 9; x++) {
                this.addSlotToContainer(new OptionalSlotRestrictedInput(SlotRestrictedInput.PlacableItemType.ENCODED_PATTERN, this.myDuality
                        .getPatterns(), this, x + row * 9, 8 + 18 * x, 133 + (18 * row), row, this.getInventoryPlayer()).setStackLimit(1));
            }
        }

        for (int x = 0; x < DualityInterface.NUMBER_OF_CONFIG_SLOTS; x++) {
            this.addSlotToContainer(new SlotFake(this.myDuality.getConfig(), x, 8 + 18 * x, 35));
        }

        for (int x = 0; x < DualityInterface.NUMBER_OF_CONFIG_SLOTS; x++) {
            this.addSlotToContainer(new SlotFake(this.myDuality.getConfig(), x + 9, 8 + 18 * x, 71));
        }

        for (int x = 0; x < DualityInterface.NUMBER_OF_STORAGE_SLOTS; x++) {
            this.addSlotToContainer(new SlotOversized(this.myDuality.getStorage(), x, 8 + 18 * x, 53));
        }

        for (int x = 0; x < DualityInterface.NUMBER_OF_STORAGE_SLOTS; x++) {
            this.addSlotToContainer(new SlotOversized(this.myDuality.getStorage(), x + 9, 8 + 18 * x, 89));
        }
    }

    @Override
    public ItemStack transferStackInSlot(final EntityPlayer p, final int idx) {
        if (Platform.isClient()) {
            return ItemStack.EMPTY;
        }

        final AppEngSlot clickSlot = (AppEngSlot) this.inventorySlots.get(idx); // require AE SLots!

        if (clickSlot instanceof SlotDisabled || clickSlot instanceof SlotInaccessible) {
            return ItemStack.EMPTY;
        }
        if (clickSlot != null && clickSlot.getHasStack()) {
            ItemStack tis = clickSlot.getStack();

            if (tis.isEmpty()) {
                return ItemStack.EMPTY;
            }

            IItemDefinition expansionCard = AEApi.instance().definitions().materials().cardPatternExpansion();
            ContainerSuperInterface casted;

            final List<Slot> selectedSlots = new ArrayList<>();

            /**
             * Gather a list of valid destinations.
             */
            if (clickSlot.isPlayerSide()) {
                tis = this.transferStackToContainer(tis);

                if (!tis.isEmpty()) {
                    if (this instanceof ContainerSuperInterface && expansionCard.isSameAs(tis) && (casted = this).getPatternUpgrades() == casted.availableUpgrades() - 1) {
                        return ItemStack.EMPTY; // Don't insert more pattern expansions than maximum useful
                    }

                    // target slots in the container...
                    for (final Object inventorySlot : this.inventorySlots) {
                        final AppEngSlot cs = (AppEngSlot) inventorySlot;

                        if (!(cs.isPlayerSide()) && !(cs instanceof SlotFake) && !(cs instanceof SlotCraftingMatrix)) {
                            if (cs.isItemValid(tis)) {
                                selectedSlots.add(cs);
                            }
                        }
                    }
                }
            } else {
                tis = tis.copy();

                // target slots in the container...
                for (final Object inventorySlot : this.inventorySlots) {
                    final AppEngSlot cs = (AppEngSlot) inventorySlot;

                    if ((cs.isPlayerSide()) && !(cs instanceof SlotFake) && !(cs instanceof SlotCraftingMatrix)) {
                        if (cs.isItemValid(tis)) {
                            selectedSlots.add(cs);
                        }
                    }
                }
            }

            /**
             * Handle Fake Slot Shift clicking.
             */
            if (selectedSlots.isEmpty() && clickSlot.isPlayerSide()) {
                if (!tis.isEmpty()) {
                    // target slots in the container...
                    for (final Object inventorySlot : this.inventorySlots) {
                        final AppEngSlot cs = (AppEngSlot) inventorySlot;
                        final ItemStack destination = cs.getStack();

                        if (!(cs.isPlayerSide()) && cs instanceof SlotFake) {
                            if (Platform.itemComparisons().isSameItem(destination, tis)) {
                                break;
                            } else if (destination.isEmpty()) {
                                cs.putStack(tis.copy());
                                this.detectAndSendChanges();
                                break;
                            }
                        }
                    }
                }
            }

            if (!tis.isEmpty()) {
                // find partials..
                for (final Slot d : selectedSlots) {
                    if (d instanceof SlotDisabled || d instanceof SlotME) {
                        continue;
                    }

                    if (d.isItemValid(tis)) {
                        if (d.getHasStack()) {
                            final ItemStack t = d.getStack().copy();

                            if (Platform.itemComparisons().isSameItem(tis, t)) // t.isItemEqual(tis))
                            {
                                if (d instanceof SlotRestrictedInput && ((SlotRestrictedInput) d).getPlaceableItemType() == SlotRestrictedInput.PlacableItemType.ENCODED_PATTERN) {
                                    return ItemStack.EMPTY; // don't insert duplicate encoded patterns to interfaces
                                }

                                final int maxSize;
                                if (d instanceof SlotOversized) {
                                    SlotOversized slotOversized = (SlotOversized) d;
                                    maxSize = slotOversized.getSlotStackLimit();
                                } else {
                                    maxSize = Math.min(tis.getMaxStackSize(), d.getSlotStackLimit());
                                }

                                int placeAble = maxSize - t.getCount();
                                if (placeAble <= 0) {
                                    continue;
                                }

                                if (tis.getCount() < placeAble) {
                                    placeAble = tis.getCount();
                                }

                                t.setCount(t.getCount() + placeAble);
                                tis.setCount(tis.getCount() - placeAble);

                                d.putStack(t);

                                if (tis.getCount() <= 0) {
                                    clickSlot.putStack(ItemStack.EMPTY);
                                    d.onSlotChanged();

                                    this.detectAndSendChanges();
                                    return ItemStack.EMPTY;
                                } else {
                                    this.detectAndSendChanges();
                                }
                            }
                        }
                    }
                }

                // any match..
                for (final Slot d : selectedSlots) {
                    if (d instanceof SlotDisabled || d instanceof SlotME) {
                        continue;
                    }

                    if (d.isItemValid(tis)) {
                        if (!d.getHasStack()) {
                            int maxSize = Math.min(tis.getMaxStackSize(), d.getSlotStackLimit());

                            final ItemStack tmp = tis.copy();
                            if (tmp.getCount() > maxSize) {
                                tmp.setCount(maxSize);
                            }

                            tis.setCount(tis.getCount() - tmp.getCount());
                            d.putStack(tmp);

                            if (tis.getCount() <= 0) {
                                clickSlot.putStack(ItemStack.EMPTY);
                                d.onSlotChanged();

                                this.detectAndSendChanges();
                                return ItemStack.EMPTY;
                            } else {
                                this.detectAndSendChanges();

                                if (
                                        (d instanceof SlotRestrictedInput && ((SlotRestrictedInput) d).getPlaceableItemType() == SlotRestrictedInput.PlacableItemType.ENCODED_PATTERN) ||
                                                (this instanceof ContainerSuperInterface && expansionCard.isSameAs(tis) && (casted = this).getPatternUpgrades() == casted.availableUpgrades() - 1)
                                ) {
                                    break; // Only insert one pattern when shift-clicking into interfaces, and don't insert more pattern expansions than maximum useful
                                }
                            }
                        }
                    }
                }
            }

            clickSlot.putStack(!tis.isEmpty() ? tis : ItemStack.EMPTY);
        }

        this.detectAndSendChanges();
        return ItemStack.EMPTY;
    }

    @Override
    protected int getHeight() {
        return 292;
    }

    @Override
    protected void setupConfig() {
        this.setupUpgrades();
    }

    @Override
    public int availableUpgrades() {
        return 4;
    }

    @Override
    public boolean isSlotEnabled(final int idx) {
        return myDuality.getInstalledUpgrades(Upgrades.PATTERN_EXPANSION) >= idx;
    }

    @Override
    public void detectAndSendChanges() {
        this.verifyPermissions(SecurityPermissions.BUILD, false);

        if (patternExpansions != getPatternUpgrades()) {
            patternExpansions = getPatternUpgrades();
            this.myDuality.dropExcessPatterns();
        }

        if (Platform.isServer()){
            lockReason = myDuality.getCraftingLockedReason();
        }
        super.detectAndSendChanges();
    }

    @Override
    public void onUpdate(final String field, final Object oldValue, final Object newValue) {
        super.onUpdate(field, oldValue, newValue);
        if (Platform.isClient() && field.equals("patternExpansions"))
            this.myDuality.dropExcessPatterns();
    }

    @Override
    protected void loadSettingsFromHost(final IConfigManager cm) {
        this.setBlockingMode((YesNo) cm.getSetting(Settings.BLOCK));
        this.setUnlockMode((LockCraftingMode) cm.getSetting(Settings.UNLOCK));
        this.setInterfaceTerminalMode((YesNo) cm.getSetting(Settings.INTERFACE_TERMINAL));
    }

    public LockCraftingMode getUnlockMode() {return this.lMode;}

    public void setUnlockMode(final LockCraftingMode mode) {this.lMode = mode;}

    public YesNo getBlockingMode() {
        return this.bMode;
    }

    private void setBlockingMode(final YesNo bMode) {
        this.bMode = bMode;
    }

    public YesNo getInterfaceTerminalMode() {
        return this.iTermMode;
    }

    private void setInterfaceTerminalMode(final YesNo iTermMode) {
        this.iTermMode = iTermMode;
    }

    public int getPatternUpgrades() {
        return this.myDuality.getInstalledUpgrades(Upgrades.PATTERN_EXPANSION);
    }

    public LockCraftingMode getCraftingLockedReason() {
        return lockReason;
    }
}
