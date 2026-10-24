package tj.multiblockpart.ender;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import tj.TJValues;
import tj.mui.widgets.PopUpWidget;
import tj.mui.widgets.impl.TJPhantomItemSlotWidget;
import tj.mui.widgets.impl.TJSlotWidget;
import tj.mui.widgets.impl.ButtonPopUpWidget;
import tj.mui.widgets.impl.TJToggleButtonWidget;
import tj.items.covers.EnderCoverProfile;
import tj.items.handlers.LargeItemStackHandler;
import tj.textures.TJSimpleOverlayRenderer;
import tj.textures.TJTextures;
import tj.util.EnderWorldData;
import tj.util.TextUtils;
import tj.util.map.Strategies;

import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

import static gregtech.api.gui.GuiTextures.*;
import static tj.mui.TJGuiTextures.ITEM_FILTER;

public class MetaTileEntityEnderItemBus extends AbstractEnderHatch<IItemHandlerModifiable, LargeItemStackHandler> {

    private final LargeItemStackHandler itemFilter = new LargeItemStackHandler(16, 1);
    private final Object2ObjectMap<ItemStack, ItemStack> itemType = new Object2ObjectOpenCustomHashMap<>(Strategies.ITEMSTACK_STRATEGY);
    private final int capacity;
    private boolean isFilterBlacklist = true;

    public MetaTileEntityEnderItemBus(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
        this.capacity = (int) Math.min(Math.pow(4, tier) * 10, Integer.MAX_VALUE);
        this.maxTransferRate = (int) Math.min(Math.round(Math.pow(4, tier) / 20), Integer.MAX_VALUE);
        this.transferRate = this.maxTransferRate;
        if (FMLCommonHandler.instance().getSide().isClient())
            this.handler = this.createHandler();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityEnderItemBus(this.metaTileEntityId, this.getTier());
    }

    @Override
    protected void addToPopUpWidget(PopUpWidget<?> buttonPopUpWidget) {
        ((ButtonPopUpWidget<?>) buttonPopUpWidget).addPopup(94, 43, 78, 96, new TJToggleButtonWidget(151, 161, 18, 18)
                .setHoverTooltipText("cover.conveyor.item_filter.title")
                .setToggleTexture(TOGGLE_BUTTON_BACK)
                .setBackgroundTextures(ITEM_FILTER)
                .useToggleTexture(true), widgetGroup -> {
            widgetGroup.addWidget(new ImageWidget(0, 0, 78, 96, BORDERED_BACKGROUND));
            widgetGroup.addWidget(new TJToggleButtonWidget(3, 75, 18, 18, BUTTON_BLACKLIST, this::isFilterBlacklist, this::setFilterBlacklist)
                    .setToggleTitleTooltipHoverText("cover.filter.blacklist.disabled",
                            "cover.filter.blacklist.enabled"));
            for (int i = 0; i < this.itemFilter.getSlots(); i++) {
                widgetGroup.addWidget(new TJPhantomItemSlotWidget(18 * (i % 4) + 3, 18 * (i / 4) + 3, 18, 18, i, this.itemFilter,
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

    @Override
    public LargeItemStackHandler createHandler() {
        return new LargeItemStackHandler(1, this.capacity);
    }

    @Override
    protected void addChannelText(ITextComponent keyEntry, String key, LargeItemStackHandler value) {
        final ItemStack item = value.getStackInSlot(0);
        final boolean empty = item.isEmpty();
        final String name = !empty ? item.getTranslationKey() + ".name" : TextUtils.translate("metaitem.fluid_cell.empty");
        final int capacity = value.getCapacity();
        final int amount = !empty ? item.getCount() : 0;
        keyEntry.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentTranslation(name)
                .appendText("\n")
                .appendText(TextUtils.translate("machine.universal.item.stored", TJValues.thousandFormat.format(amount), TJValues.thousandFormat.format(capacity)))));
    }

    @Override
    public MultiblockAbility<IItemHandlerModifiable> getAbility() {
        return MultiblockAbility.IMPORT_ITEMS;
    }

    @Override
    public void registerAbilities(List<IItemHandlerModifiable> list) {
        list.add(this.handler);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setTag("itemFilter", this.itemFilter.serializeNBT());
        data.setBoolean("filterBlacklist", this.isFilterBlacklist);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.itemFilter.deserializeNBT(data.getCompoundTag("itemFilter"));
        this.isFilterBlacklist = data.getBoolean("filterBlacklist");
        for (int i = 0; i < this.itemFilter.getSlots(); i++) {
            final ItemStack stack = this.itemFilter.getStackInSlot(i);
            if (stack.isEmpty()) continue;
            this.itemType.put(stack, stack);
        }
    }

    @Override
    public IItemHandlerModifiable getImportItems() {
        return this.handler;
    }

    @Override
    protected TJSimpleOverlayRenderer getOverlay() {
        return TJTextures.PORTAL_OVERLAY;
    }

    @Override
    protected Object2ObjectMap<String, EnderCoverProfile<LargeItemStackHandler>> getPlayerMap() {
        return EnderWorldData.getINSTANCE().getItemChestPlayerMap();
    }

    private void setFilterBlacklist(boolean isFilterBlacklist) {
        this.isFilterBlacklist = isFilterBlacklist;
        this.markDirty();
    }

    private boolean isFilterBlacklist() {
        return this.isFilterBlacklist;
    }
}
