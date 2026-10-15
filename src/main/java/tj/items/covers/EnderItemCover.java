package tj.items.covers;

import gregicadditions.GAValues;
import gregtech.api.cover.ICoverable;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.*;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import tj.TJValues;
import tj.mui.widgets.PopUpWidget;
import tj.mui.widgets.impl.TJPhantomItemSlotWidget;
import tj.mui.widgets.impl.TJSlotWidget;
import tj.mui.widgets.impl.ButtonPopUpWidget;
import tj.mui.widgets.impl.TJToggleButtonWidget;
import tj.items.handlers.LargeItemStackHandler;
import tj.textures.TJSimpleOverlayRenderer;
import tj.util.EnderWorldData;
import tj.util.map.Strategies;

import java.awt.*;
import java.util.function.Consumer;

import static gregtech.api.gui.GuiTextures.*;
import static gregtech.common.covers.CoverPump.PumpMode.IMPORT;
import static tj.mui.TJGuiTextures.*;
import static tj.textures.TJTextures.PORTAL_OVERLAY;

public class EnderItemCover extends AbstractEnderCover<LargeItemStackHandler> {

    private final IItemHandler itemInventory;
    private final LargeItemStackHandler itemFilter = new LargeItemStackHandler(9, 1);
    private final Object2ObjectMap<ItemStack, ItemStack> itemType = new Object2ObjectOpenCustomHashMap<>(Strategies.ITEMSTACK_STRATEGY);
    private final int capacity;
    private final int tier;
    private boolean isFilterBlacklist = true;

    public EnderItemCover(ICoverable coverHolder, EnumFacing attachedSide, int tier) {
        super(coverHolder, attachedSide);
        this.tier = tier;
        this.itemInventory = this.coverHolder.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        this.capacity = (int) Math.min(Math.pow(4, tier) * 10, Integer.MAX_VALUE);
        this.maxTransferRate = (int) Math.min(Math.round(Math.pow(4, tier) / 20), Integer.MAX_VALUE);
        this.transferRate = this.maxTransferRate;
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
    protected Object2ObjectMap<String, EnderCoverProfile<LargeItemStackHandler>> getPlayerMap() {
        return EnderWorldData.getINSTANCE().getItemChestPlayerMap();
    }

    @Override
    protected void addToPopUpWidget(PopUpWidget<?> buttonPopUpWidget) {
        ((ButtonPopUpWidget<?>) buttonPopUpWidget).addPopup(112, 61, 60, 78, new TJToggleButtonWidget(151, 161, 18, 18)
                        .setHoverTooltipText("cover.conveyor.item_filter.title")
                        .setToggleTexture(TOGGLE_BUTTON_BACK)
                        .setBackgroundTextures(ITEM_FILTER)
                        .useToggleTexture(true), widgetGroup -> {
            widgetGroup.addWidget(new ImageWidget(0, 0, 60, 78, BORDERED_BACKGROUND));
            widgetGroup.addWidget(new TJToggleButtonWidget(3, 57, 18, 18, BUTTON_BLACKLIST, this::isFilterBlacklist, this::setFilterBlacklist)
                    .setToggleTitleTooltipHoverText("cover.filter.blacklist.disabled",
                            "cover.filter.blacklist.enabled"));
            for (int i = 0; i < this.itemFilter.getSlots(); i++) {
                widgetGroup.addWidget(new TJPhantomItemSlotWidget(18 * (i % 3) + 3, 18 * (i / 3) + 3, 18, 18, i, this.itemFilter,
                        item -> {
                    if (item.isEmpty()) return;
                    this.itemType.put(item, item);
                }, this.itemType::remove).setPutItemsPredicate(item -> !this.itemType.containsKey(item))
                        .setBackgroundTextures(GuiTextures.SLOT));
            }
            return false;
        }).setClickArea(new Rectangle(346, 107, 60, 78));
    }

    @Override
    protected void addWidgets(Consumer<Widget> widget) {
        widget.accept(new TJSlotWidget<>(() -> this.handler, 0, 7, 38)
                .setActiveBackgroundTexture(SLOT));
    }

    private void setFilterBlacklist(boolean isFilterBlacklist) {
        this.isFilterBlacklist = isFilterBlacklist;
        this.markAsDirty();
    }

    private boolean isFilterBlacklist() {
        return this.isFilterBlacklist;
    }

    @Override
    public LargeItemStackHandler createHandler() {
        return new LargeItemStackHandler(1, this.capacity);
    }

    @Override
    protected void addChannelText(ITextComponent keyEntry, String key, LargeItemStackHandler value) {
        final ItemStack item = value.getStackInSlot(0);
        final boolean empty = item.isEmpty();
        final String name = !empty ? item.getTranslationKey() + ".name" : "metaitem.fluid_cell.empty";
        final int capacity = value.getCapacity();
        final int amount = !empty ? item.getCount() : 0;
        keyEntry.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentTranslation(name)
                .appendText("\n")
                .appendSibling(new TextComponentTranslation("machine.universal.item.stored", TJValues.thousandFormat.format(amount),
                        TJValues.thousandFormat.format(capacity)))));
    }

    @Override
    public void update() {
        if (this.isWorkingEnabled && this.handler != null) {
            if (this.pumpMode == IMPORT) {
                this.moveInventoryItems(this.itemInventory, this.handler);
            } else this.moveInventoryItems(this.handler, this.itemInventory);
        }
    }

    private void moveInventoryItems(IItemHandler sourceInventory, IItemHandler targetInventory) {
        for (int srcIndex = 0; srcIndex < sourceInventory.getSlots(); srcIndex++) {
            if (this.itemType.containsKey(sourceInventory.getStackInSlot(srcIndex)) == this.isFilterBlacklist) continue;
            ItemStack sourceStack = sourceInventory.extractItem(srcIndex, Math.min(this.transferRate, this.maxTransferRate), true);
            if (sourceStack.isEmpty()) continue;
            final ItemStack remainder = ItemHandlerHelper.insertItemStacked(targetInventory, sourceStack, true);
            final int amountToInsert = sourceStack.getCount() - remainder.getCount();
            if (amountToInsert > 0) {
                sourceStack = sourceInventory.extractItem(srcIndex, amountToInsert, false);
                ItemHandlerHelper.insertItemStacked(targetInventory, sourceStack, false);
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setTag("itemFilter", this.itemFilter.serializeNBT());
        data.setBoolean("FilterBlacklist", this.isFilterBlacklist);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.itemFilter.deserializeNBT(data.getCompoundTag("itemFilter"));
        this.isFilterBlacklist = data.getBoolean("FilterBlacklist");
        for (int i = 0; i < this.itemFilter.getSlots(); i++) {
            final ItemStack stack = this.itemFilter.getStackInSlot(i);
            if (stack.isEmpty()) continue;
            this.itemType.put(stack, stack);
        }
    }
}
