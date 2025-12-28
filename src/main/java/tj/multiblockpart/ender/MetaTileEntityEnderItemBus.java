package tj.multiblockpart.ender;

import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.PhantomSlotWidget;
import gregtech.api.gui.widgets.ToggleButtonWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.common.covers.filter.SimpleItemFilter;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import tj.gui.widgets.PopUpWidget;
import tj.gui.widgets.TJSlotWidget;
import tj.gui.widgets.impl.ButtonPopUpWidget;
import tj.gui.widgets.impl.TJToggleButtonWidget;
import tj.items.covers.EnderCoverProfile;
import tj.items.handlers.LargeItemStackHandler;
import tj.textures.TJSimpleOverlayRenderer;
import tj.textures.TJTextures;
import tj.util.EnderWorldData;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static gregtech.api.gui.GuiTextures.*;
import static tj.gui.TJGuiTextures.ITEM_FILTER;

public class MetaTileEntityEnderItemBus extends AbstractEnderHatch<IItemHandlerModifiable, LargeItemStackHandler> {

    private final SimpleItemFilter itemFilter;
    private final int capacity;
    private boolean isFilterBlacklist = true;

    public MetaTileEntityEnderItemBus(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
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
    public IItemHandlerModifiable getImportItems() {
        return this.handler;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityEnderItemBus(this.metaTileEntityId, this.getTier());
    }

    @Override
    protected TJSimpleOverlayRenderer getOverlay() {
        return TJTextures.PORTAL_OVERLAY;
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
        this.markDirty();
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
    public MultiblockAbility<IItemHandlerModifiable> getAbility() {
        return MultiblockAbility.IMPORT_ITEMS;
    }

    @Override
    public void registerAbilities(List<IItemHandlerModifiable> list) {
        list.add(this.handler);
    }
}
