package tj.items.covers;

import gregicadditions.GAValues;
import gregtech.api.cover.ICoverable;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.*;
import gregtech.common.covers.filter.SimpleItemFilter;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import tj.gui.widgets.PopUpWidget;
import tj.gui.widgets.TJSlotWidget;
import tj.gui.widgets.impl.ButtonPopUpWidget;
import tj.gui.widgets.impl.TJToggleButtonWidget;
import tj.items.handlers.LargeItemStackHandler;
import tj.textures.TJSimpleOverlayRenderer;
import tj.util.EnderWorldData;

import java.awt.*;
import java.util.Map;
import java.util.function.Consumer;

import static gregtech.api.gui.GuiTextures.*;
import static gregtech.common.covers.CoverPump.PumpMode.IMPORT;
import static tj.gui.TJGuiTextures.*;
import static tj.textures.TJTextures.PORTAL_OVERLAY;

public class EnderCoverItem extends AbstractEnderCover<LargeItemStackHandler> {

    private final IItemHandler itemInventory;
    private final SimpleItemFilter itemFilter;
    private final int capacity;
    private final int tier;
    private boolean isFilterBlacklist = true;

    public EnderCoverItem(ICoverable coverHolder, EnumFacing attachedSide, int tier) {
        super(coverHolder, attachedSide);
        this.tier = tier;
        this.itemInventory = this.coverHolder.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        this.capacity = (int) Math.min(Math.pow(4, tier) * 10, Integer.MAX_VALUE);
        this.maxTransferRate = (int) Math.min(Math.round(Math.pow(4, tier) / 20), Integer.MAX_VALUE);
        this.transferRate = this.maxTransferRate;
        this.itemFilter = new SimpleItemFilter() {
            @Override
            public void initUI(Consumer<Widget> widgetGroup) {
                for (int i = 0; i < 9; i++) {
                    widgetGroup.accept(new PhantomSlotWidget(itemFilterSlots, i, 3 + 18 * (i % 3), 3 + 18 * (i / 3)).setBackgroundTexture(SLOT));
                }
                widgetGroup.accept(new ToggleButtonWidget(21, 57, 18, 18, BUTTON_FILTER_DAMAGE,
                        () -> ignoreDamage, this::setIgnoreDamage).setTooltipText("cover.item_filter.ignore_damage"));
                widgetGroup.accept(new ToggleButtonWidget(39, 57, 18, 18, BUTTON_FILTER_NBT,
                        () -> ignoreNBT, this::setIgnoreNBT).setTooltipText("cover.item_filter.ignore_nbt"));
            }
        };
        if (FMLCommonHandler.instance().getSide().isClient())
            this.handler = this.createHandler();
    }

    @Override
    public boolean canAttach() {
        return this.coverHolder.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, this.attachedSide) != null;
    }

    @Override
    protected TJSimpleOverlayRenderer getOverlay() {
        return PORTAL_OVERLAY;
    }

    @Override
    protected int getPortalColor() {
        return 0xff3e00;
    }

    @Override
    public int getTier() {
        return this.tier;
    }

    @Override
    protected String getName() {
        return "metaitem.ender_item_cover_" + GAValues.VN[this.tier].toLowerCase() + ".name";
    }

    @Override
    protected Map<String, EnderCoverProfile<LargeItemStackHandler>> getPlayerMap() {
        return EnderWorldData.getINSTANCE().getItemChestPlayerMap();
    }

    @Override
    protected void addToPopUpWidget(PopUpWidget<?> buttonPopUpWidget) {
        ((ButtonPopUpWidget<?>) buttonPopUpWidget).addPopup(112, 61, 60, 78, new TJToggleButtonWidget(151, 161, 18, 18)
                        .setTooltipText("cover.conveyor.item_filter.title")
                        .setToggleTexture(TOGGLE_BUTTON_BACK)
                        .setBackgroundTextures(ITEM_FILTER)
                        .useToggleTexture(true), widgetGroup -> {
            widgetGroup.addWidget(new ImageWidget(0, 0, 60, 78, BORDERED_BACKGROUND));
            widgetGroup.addWidget(new ToggleButtonWidget(3, 57, 18, 18, BUTTON_BLACKLIST, this::isFilterBlacklist, this::setFilterBlacklist)
                    .setTooltipText("cover.filter.blacklist"));
            this.itemFilter.initUI(widgetGroup::addWidget);
            return false;
        }).setClickArea(new Rectangle(346, 107, 60, 78));
    }

    @Override
    protected void addWidgets(Consumer<Widget> widget) {
        widget.accept(new TJSlotWidget<>(null, 0, 7, 38)
                .setItemHandlerSupplier(() -> this.handler)
                .setBackgroundTexture(SLOT));
    }

    private void setFilterBlacklist(boolean isFilterBlacklist) {
        this.isFilterBlacklist = isFilterBlacklist;
        this.markAsDirty();
    }

    private boolean isFilterBlacklist() {
        return this.isFilterBlacklist;
    }

    @Override
    protected LargeItemStackHandler createHandler() {
        return new LargeItemStackHandler(1, this.capacity);
    }

    @Override
    protected void addChannelText(ITextComponent keyEntry, String key, LargeItemStackHandler value) {
        ItemStack item = value.getStackInSlot(0);
        boolean empty = item.isEmpty();
        String name = !empty ? item.getTranslationKey() + ".name" : net.minecraft.util.text.translation.I18n.translateToLocal("metaitem.fluid_cell.empty");
        int capacity = value.getCapacity();
        int amount = !empty ? item.getCount() : 0;
        keyEntry.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentTranslation(name)
                .appendText("\n")
                .appendText(I18n.translateToLocalFormatted("machine.universal.item.stored", amount, capacity))));
    }

    @Override
    public void update() {
        if (this.isWorkingEnabled && this.handler != null) {
            if (this.pumpMode == IMPORT) {
                this.moveInventoryItems(this.itemInventory, this.handler);
            } else {
                this.moveInventoryItems(this.handler, this.itemInventory);
            }
        }
    }

    private void moveInventoryItems(IItemHandler sourceInventory, IItemHandler targetInventory) {
        for (int srcIndex = 0; srcIndex < sourceInventory.getSlots(); srcIndex++) {
            ItemStack sourceStack = sourceInventory.extractItem(srcIndex, Math.min(this.transferRate, this.maxTransferRate), true);
            boolean isFilterStack = this.itemFilter.matchItemStack(sourceStack) != null;
            if (sourceStack.isEmpty() || this.isFilterBlacklist == isFilterStack) {
                continue;
            }
            ItemStack remainder = ItemHandlerHelper.insertItemStacked(targetInventory, sourceStack, true);
            int amountToInsert = sourceStack.getCount() - remainder.getCount();
            if (amountToInsert > 0) {
                sourceStack = sourceInventory.extractItem(srcIndex, amountToInsert, false);
                ItemHandlerHelper.insertItemStacked(targetInventory, sourceStack, false);
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        this.itemFilter.writeToNBT(data);
        data.setBoolean("FilterBlacklist", this.isFilterBlacklist);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.itemFilter.readFromNBT(data);
        this.isFilterBlacklist = data.getBoolean("FilterBlacklist");
    }
}
