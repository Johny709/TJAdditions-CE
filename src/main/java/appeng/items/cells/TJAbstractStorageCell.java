package appeng.items.cells;

import appeng.api.AEApi;
import appeng.api.config.FuzzyMode;
import appeng.api.exceptions.MissingDefinitionException;
import appeng.api.implementations.items.IItemGroup;
import appeng.api.implementations.items.IStorageCell;
import appeng.api.implementations.items.IUpgradeModule;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.core.AEConfig;
import appeng.core.features.AEFeature;
import appeng.core.localization.GuiText;
import appeng.items.AEBaseItem;
import appeng.items.contents.CellConfig;
import appeng.items.contents.CellUpgrades;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import appeng.items.materials.TJAE2MaterialType;

import java.util.List;
import java.util.Set;

/**
 * More advanced form of {@link appeng.items.storage.AbstractStorageCell}
 */
public abstract class TJAbstractStorageCell<T extends IAEStack<T>> extends AEBaseItem implements IStorageCell<T>, IItemGroup {
    protected final TJAE2MaterialType materialType;
    protected final int totalBytes;

    public TJAbstractStorageCell(final TJAE2MaterialType materialType, final int kiloBytes) {
        this.setMaxStackSize(1);
        long totalBytes = Math.min(Integer.MAX_VALUE, (long) kiloBytes * 1024);
        this.totalBytes = (int) totalBytes;
        this.materialType = materialType;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addCheckedInformation(final ItemStack stack, final World world, final List<String> lines, final ITooltipFlag advancedTooltips) {
        AEApi.instance()
                .client()
                .addCellInformation(AEApi.instance().registries().cell().getCellInventory(stack, null, this.getChannel()), lines);
    }

    @Override
    public int getBytes(final ItemStack cellItem) {
        return this.totalBytes;
    }

    @Override
    public int getTotalTypes(final ItemStack cellItem) {
        return 63;
    }

    @Override
    public boolean isBlackListed(final ItemStack cellItem, final T requestedAddition) {
        return false;
    }

    @Override
    public boolean storableInStorageCell() {
        return false;
    }

    @Override
    public boolean isStorageCell(final ItemStack i) {
        return true;
    }

    @Override
    public String getUnlocalizedGroupName(final Set<ItemStack> others, final ItemStack is) {
        return GuiText.StorageCells.getUnlocalized();
    }

    @Override
    public boolean isEditable(final ItemStack is) {
        return true;
    }

    @Override
    public IItemHandler getUpgradesInventory(final ItemStack is) {
        return new CellUpgrades(is, 2);
    }

    @Override
    public IItemHandler getConfigInventory(final ItemStack is) {
        return new CellConfig(is);
    }

    @Override
    public FuzzyMode getFuzzyMode(final ItemStack is) {
        final String fz = Platform.openNbtData(is).getString("FuzzyMode");
        try {
            return FuzzyMode.valueOf(fz);
        } catch (final Throwable t) {
            return FuzzyMode.IGNORE_ALL;
        }
    }

    @Override
    public void setFuzzyMode(final ItemStack is, final FuzzyMode fzMode) {
        Platform.openNbtData(is).setString("FuzzyMode", fzMode.name());
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(final World world, final EntityPlayer player, final EnumHand hand) {
        this.disassembleDrive(player.getHeldItem(hand), world, player);
        return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }

    private boolean disassembleDrive(final ItemStack stack, final World world, final EntityPlayer player) {
        if (player.isSneaking()) {
            if (Platform.isClient()) {
                return false;
            }

            final InventoryPlayer playerInventory = player.inventory;
            final IMEInventoryHandler inv = AEApi.instance().registries().cell().getCellInventory(stack, null, this.getChannel());
            if (inv != null && playerInventory.getCurrentItem() == stack) {
                final InventoryAdaptor ia = InventoryAdaptor.getAdaptor(player);
                final IItemList<IAEItemStack> list = inv.getAvailableItems(this.getChannel().createList());
                if (list.isEmpty() && ia != null) {
                    playerInventory.setInventorySlotContents(playerInventory.currentItem, ItemStack.EMPTY);

                    // drop core
                    final ItemStack extraB = ia.addItems(this.materialType.stack(1));
                    if (!extraB.isEmpty()) {
                        player.dropItem(extraB, false);
                    }

                    // drop upgrades
                    final IItemHandler upgradesInventory = this.getUpgradesInventory(stack);
                    for (int upgradeIndex = 0; upgradeIndex < upgradesInventory.getSlots(); upgradeIndex++) {
                        final ItemStack upgradeStack = upgradesInventory.getStackInSlot(upgradeIndex);
                        final ItemStack leftStack = ia.addItems(upgradeStack);
                        if (!leftStack.isEmpty() && upgradeStack.getItem() instanceof IUpgradeModule) {
                            player.dropItem(upgradeStack, false);
                        }
                    }

                    // drop empty storage cell case
                    this.dropEmptyStorageCellCase(ia, player);

                    if (player.inventoryContainer != null) {
                        player.inventoryContainer.detectAndSendChanges();
                    }

                    return true;
                }
            }
        }
        return false;
    }

    protected abstract void dropEmptyStorageCellCase(final InventoryAdaptor ia, final EntityPlayer player);

    @Override
    public EnumActionResult onItemUseFirst(final EntityPlayer player, final World world, final BlockPos pos, final EnumFacing side, final float hitX, final float hitY, final float hitZ, final EnumHand hand) {
        return this.disassembleDrive(player.getHeldItem(hand), world, player) ? EnumActionResult.SUCCESS : EnumActionResult.PASS;
    }

    @Override
    public ItemStack getContainerItem(final ItemStack itemStack) {
        return AEApi.instance()
                .definitions()
                .materials()
                .emptyStorageCell()
                .maybeStack(1)
                .orElseThrow(() -> new MissingDefinitionException("Tried to use empty storage cells while basic storage cells are defined."));
    }

    @Override
    public boolean hasContainerItem(final ItemStack stack) {
        return AEConfig.instance().isFeatureEnabled(AEFeature.ENABLE_DISASSEMBLY_CRAFTING);
    }
}
