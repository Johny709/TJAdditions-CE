package tj.integration.ae2.part;

import appeng.api.config.*;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartModel;
import appeng.core.Api;
import appeng.core.settings.TickRates;
import appeng.fluids.helper.DualityFluidInterface;
import appeng.fluids.helper.IConfigurableFluidInventory;
import appeng.fluids.helper.IFluidInterfaceHost;
import appeng.fluids.util.AEFluidInventory;
import appeng.helpers.DualityInterface;
import appeng.items.parts.PartModels;
import appeng.parts.PartModel;
import appeng.parts.misc.PartInterface;
import appeng.tile.inventory.AppEngInternalAEInventory;
import appeng.tile.networking.TileCableBus;
import baubles.api.BaublesApi;
import com.circulation.random_complement.client.RCSettings;
import com.circulation.random_complement.client.buttonsetting.IntelligentBlocking;
import com.circulation.random_complement.common.interfaces.RCIConfigManager;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.LabelWidget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.gui.widgets.tab.VerticalTabListRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import tj.TJ;
import tj.blocks.block.TJBlocks;
import tj.builder.WidgetTabBuilder;
import tj.items.handlers.FilteredItemStackHandler;
import tj.mui.TJGuiTextures;
import tj.mui.TJGuiUtils;
import tj.mui.uifactory.ITileEntityUI;
import tj.mui.uifactory.TileEntityHolder;
import tj.integration.ae2.helpers.DualitySuperFluidInterface;
import tj.integration.ae2.helpers.DualitySuperInterface;
import tj.integration.ae2.helpers.IDualitySuperFluidInterface;
import tj.items.item.TJItems;
import tj.mui.widgets.ButtonWidget;
import tj.mui.widgets.impl.*;
import tj.util.TJItemUtils;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static gregtech.api.gui.widgets.tab.VerticalTabListRenderer.HorizontalLocation.LEFT;
import static gregtech.api.gui.widgets.tab.VerticalTabListRenderer.VerticalStartCorner.TOP;

public class PartSuperDualInterface extends PartInterface implements IFluidInterfaceHost, IConfigurableFluidInventory, ITileEntityUI {

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
        final DualityInterface duality = this.getInterfaceDuality();
        final ItemStack patternMultiTool = Optional.of(player.inventory.mainInventory)
                .map(inventory -> {
                    for (ItemStack stack : inventory)
                        if (stack.isItemEqual(TJItemUtils.getItemStackFromName("nae2:pattern_multiplier")))
                            return stack;
                    final IItemHandlerModifiable baubleSlots = BaublesApi.getBaublesHandler(player);
                    for (int i = 0; i < baubleSlots.getSlots(); i++)
                        if (baubleSlots.getStackInSlot(i).isItemEqual(TJItemUtils.getItemStackFromName("nae2:pattern_multiplier")))
                            return baubleSlots.getStackInSlot(i);
                    return ItemStack.EMPTY;
                }).get();
        final NBTTagCompound compound = TJItemUtils.getCompoundFromStack(patternMultiTool);
        final NBTTagCompound invTag = compound.getCompoundTag("inv");
        final NBTTagCompound upgradeTag = compound.getCompoundTag("upgrades");
        final FilteredItemStackHandler multiPatternSlots = new FilteredItemStackHandler(null, 36, 64)
                .setItemStackPredicate((slot, itemStack) -> itemStack.isItemEqual(Api.INSTANCE.definitions().materials().blankPattern().maybeStack(1).orElse(ItemStack.EMPTY)) ||
                        itemStack.isItemEqual(Api.INSTANCE.definitions().items().encodedPattern().maybeStack(1).orElse(ItemStack.EMPTY)) || itemStack.isItemEqual(TJItemUtils.getItemStackFromName("ae2fc:dense_encoded_pattern")));
        multiPatternSlots.setOnContentsChangedPost((slot, itemStack) -> this.writePatternMultiToolToNBT(multiPatternSlots, invTag));
        final FilteredItemStackHandler multiUpgradeSlots = new FilteredItemStackHandler(null, 3, 1)
                .setItemStackPredicate((slot, itemStack) -> itemStack.isItemEqual(Api.INSTANCE.definitions().materials().cardCapacity().maybeStack(1).orElse(ItemStack.EMPTY)));
        multiUpgradeSlots.setOnContentsChangedPost((slot, itemStack) -> this.writePatternMultiToolToNBT(multiUpgradeSlots, upgradeTag));
        final ButtonPopUpWidget<?> buttonPopUpWidget = new ButtonPopUpWidget<>();
        final WidgetTabBuilder tabBuilder = new WidgetTabBuilder()
                .setTabListRenderer(() -> new VerticalTabListRenderer(TOP, LEFT))
                .addTab("tile.me.super_interface.name", TJBlocks.SUPER_INTERFACE.maybeStack(1).orElse(ItemStack.EMPTY), widgets -> this.createInterfaceTab(widgets, patternMultiTool, multiPatternSlots, multiUpgradeSlots, invTag))
                .addTab("tile.me.super_fluid_interface.name", TJBlocks.SUPER_FLUID_INTERFACE.maybeStack(1).orElse(ItemStack.EMPTY), widgets -> this.createFluidInterfaceTab(widgets, buttonPopUpWidget));
        final ModularUI.Builder builder = ModularUI.builder(TJGuiTextures.SUPER_INTERFACE, 211, 292);
        if (!patternMultiTool.isEmpty())
            builder.widget(new ImageWidget(-125, 0, 105, 218, GuiTextures.BORDERED_BACKGROUND));
        return builder.widget(new TJLabelWidget(7, -18, 162, 18, TJGuiTextures.MACHINE_LABEL_2)
                        .setItemLabel(this.getItemStackRepresentation()).setLocale(this.getItemStackRepresentation().getDisplayName()))
                .widget(new TJLabelWidget(4, 0, 162, 18, null)
                        .setDynamicLocale(this::getCustomInventoryName)
                        .setCentered(false)
                        .setCanSlide(false))
                .widget(tabBuilder.build())
                .widget(buttonPopUpWidget.addPopup(widgetGroup -> true)
                        .addPopup(new ButtonWidget<>(154, 0, 22, 22)
                                .setItemDisplay(Api.INSTANCE.definitions().items().certusQuartzWrench().maybeStack(1).orElse(ItemStack.EMPTY))
                                .setBackgroundTextures(TJGuiTextures.INTERFACE_SETTINGS_EDGE_RIGHT)
                                .setTooltipText("gui.appliedenergistics2.Priority"), widgetGroup -> {
                            widgetGroup.addWidget(new ImageWidget(7, 107, 162, 100, GuiTextures.BORDERED_BACKGROUND));
                            widgetGroup.addWidget(new LabelWidget(14, 112, "gui.appliedenergistics2.Priority"));
                            widgetGroup.addWidget(new NewTextFieldWidget<>(14, 153, 148, 18, true, () -> String.valueOf(duality.getPriority()), this::setPriority)
                                    .setValidator(str -> Pattern.compile("-*?[0-9_]*\\*?").matcher(str).matches())
                                    .setUpdateOnTyping(true));
                            widgetGroup.addWidget(new ClickButtonWidget(15, 127, 25, 20, "+1", data -> this.setPriority(String.valueOf((long) duality.getPriority() + 1), "")));
                            widgetGroup.addWidget(new ClickButtonWidget(45, 127, 30, 20, "+10", data -> this.setPriority(String.valueOf((long) duality.getPriority() + 10), "")));
                            widgetGroup.addWidget(new ClickButtonWidget(80, 127, 35, 20, "+100", data -> this.setPriority(String.valueOf((long) duality.getPriority() + 100), "")));
                            widgetGroup.addWidget(new ClickButtonWidget(120, 127, 40, 20, "+1000", data -> this.setPriority(String.valueOf((long) duality.getPriority() + 1000), "")));
                            widgetGroup.addWidget(new ClickButtonWidget(15, 177, 25, 20, "-1", data -> this.setPriority(String.valueOf((long) duality.getPriority() - 1), "")));
                            widgetGroup.addWidget(new ClickButtonWidget(45, 177, 30, 20, "-10", data -> this.setPriority(String.valueOf((long) duality.getPriority() - 10), "")));
                            widgetGroup.addWidget(new ClickButtonWidget(80, 177, 35, 20, "-100", data -> this.setPriority(String.valueOf((long) duality.getPriority() - 100), "")));
                            widgetGroup.addWidget(new ClickButtonWidget(120, 177, 40, 20, "-1000", data -> this.setPriority(String.valueOf((long) duality.getPriority() - 1000), "")));
                            return false;
                        }))
                .widget(TJGuiUtils.bindPlayerInventory(new WidgetGroup(), player.inventory, 7, 209, patternMultiTool))
                .bindOpenListener(() -> {
                    if (!patternMultiTool.isEmpty()) {
                        this.readPatternMultiToolNBT(multiPatternSlots, invTag.getTagList("Items", 10));
                        this.readPatternMultiToolNBT(multiUpgradeSlots, upgradeTag.getTagList("Items", 10));
                        if (patternMultiTool.getTagCompound() == null || patternMultiTool.getTagCompound().isEmpty()) {
                            compound.setTag("inv", invTag);
                            compound.setTag("upgrades", upgradeTag);
                            patternMultiTool.setTagCompound(compound);
                        }
                    }
                }).build(holder, player);
    }

    private void createInterfaceTab(List<Widget> tab, ItemStack patternMultiTool, IItemHandler multiPatternSlots, FilteredItemStackHandler multiUpgradeSlots, NBTTagCompound invTag) {
        final DualityInterface duality = this.getInterfaceDuality();
        final DualitySuperInterface.DualityUpgradeInventory upgradeHandler = (DualitySuperInterface.DualityUpgradeInventory) duality.getInventoryByName("upgrades");
        final SlotScrollableWidgetGroup patternScrollableSlotGroup = new SlotScrollableWidgetGroup(7, 133, 166, 72, 9)
                .setItemStackTransfer(itemStack -> TJItemUtils.insertIntoItemHandler(multiPatternSlots, itemStack, false))
                .setItemHandler(duality.getPatterns())
                .setScrollWidth(4);
        final ButtonPopUpWidget<?> buttonPopUpWidget = new ButtonPopUpWidget<>();
        final SelectionWidgetGroup selectionWidgetGroup = new SelectionWidgetGroup(0, 0, 0, 0);
        final ImageWidget stackSizeDisplay = new ImageWidget(7, 107, 162, 100, GuiTextures.BORDERED_BACKGROUND);
        final LabelWidget stackSizeLabel = new LabelWidget(14, 112, "machine.universal.stack_size");
        final ClickButtonWidget clickButtonAdd1 = new ClickButtonWidget(15, 127, 25, 20, "+1", data -> this.setStackSize(String.valueOf(Long.parseLong(this.getStackSize(selectionWidgetGroup.getIndex())) + 1), String.valueOf(selectionWidgetGroup.getIndex())));
        final ClickButtonWidget clickButtonAdd10 = new ClickButtonWidget(45, 127, 30, 20, "+10", data -> this.setStackSize(String.valueOf(Long.parseLong(this.getStackSize(selectionWidgetGroup.getIndex())) + 10), String.valueOf(selectionWidgetGroup.getIndex())));
        final ClickButtonWidget clickButtonAdd100 = new ClickButtonWidget(80, 127, 35, 20, "+100", data -> this.setStackSize(String.valueOf(Long.parseLong(this.getStackSize(selectionWidgetGroup.getIndex())) + 100), String.valueOf(selectionWidgetGroup.getIndex())));
        final ClickButtonWidget clickButtonAdd1000 = new ClickButtonWidget(120, 127, 40, 20, "+1000", data -> this.setStackSize(String.valueOf(Long.parseLong(this.getStackSize(selectionWidgetGroup.getIndex())) + 1000), String.valueOf(selectionWidgetGroup.getIndex())));
        final ClickButtonWidget clickButtonSub1 = new ClickButtonWidget(15, 177, 25, 20, "-1", data -> this.setStackSize(String.valueOf(Long.parseLong(this.getStackSize(selectionWidgetGroup.getIndex())) - 1), String.valueOf(selectionWidgetGroup.getIndex())));
        final ClickButtonWidget clickButtonSub10 = new ClickButtonWidget(45, 177, 30, 20, "-10", data -> this.setStackSize(String.valueOf(Long.parseLong(this.getStackSize(selectionWidgetGroup.getIndex())) - 10), String.valueOf(selectionWidgetGroup.getIndex())));
        final ClickButtonWidget clickButtonSub100 = new ClickButtonWidget(80, 177, 35, 20, "-100", data -> this.setStackSize(String.valueOf(Long.parseLong(this.getStackSize(selectionWidgetGroup.getIndex())) - 100), String.valueOf(selectionWidgetGroup.getIndex())));
        final ClickButtonWidget clickButtonSub1000 = new ClickButtonWidget(120, 177, 40, 20, "-1000", data -> this.setStackSize(String.valueOf(Long.parseLong(this.getStackSize(selectionWidgetGroup.getIndex())) - 1000), String.valueOf(selectionWidgetGroup.getIndex())));
        final NewTextFieldWidget<?> stackSizeTextField = new NewTextFieldWidget<>(14, 153, 148, 18, true, null, this::setStackSize)
                .setValidator(str -> Pattern.compile("-*?[0-9_]*\\*?").matcher(str).matches())
                .setUpdateOnTyping(true)
                .setMaxStringLength(11);
        stackSizeTextField.setTextSupplier(() -> this.getStackSize((int) stackSizeTextField.getTextIdLong()));
        selectionWidgetGroup.setIndexListener(stackSizeTextField::setTextIdLong);
        for (int i = 0; i < duality.getConfig().getSlots(); i++) {
            tab.add(new TJPhantomItemSlotWidget(7 + (18 * (i % 9)), 34 + (36 * (i / 9)), 18, 18, i, duality.getConfig())
                    .setBackgroundTextures(TJGuiTextures.SLOT_DOWN));
            selectionWidgetGroup.addSubWidget(i, stackSizeDisplay);
            selectionWidgetGroup.addSubWidget(i, stackSizeLabel);
            selectionWidgetGroup.addSubWidget(i, clickButtonAdd1);
            selectionWidgetGroup.addSubWidget(i, clickButtonAdd10);
            selectionWidgetGroup.addSubWidget(i, clickButtonAdd100);
            selectionWidgetGroup.addSubWidget(i, clickButtonAdd1000);
            selectionWidgetGroup.addSubWidget(i, clickButtonSub1);
            selectionWidgetGroup.addSubWidget(i, clickButtonSub10);
            selectionWidgetGroup.addSubWidget(i, clickButtonSub100);
            selectionWidgetGroup.addSubWidget(i, clickButtonSub1000);
            selectionWidgetGroup.addSubWidget(i, stackSizeTextField);
            selectionWidgetGroup.addSelectionBox(i, 7 + (18 * (i % 9)), 34 + (36 * (i / 9)), 18, 18);
        }
        for (int i = 0; i < duality.getPatterns().getSlots(); i++) {
            final int index = i;
            patternScrollableSlotGroup.addWidget(new AEPatternSlotWidget(duality.getPatterns(), i, 18 * (i % 9), 18 * (i / 9))
                    .setActiveSupplier(() -> index / 9 <= upgradeHandler.getInstalledUpgrades(Upgrades.PATTERN_EXPANSION) && selectionWidgetGroup.getIndex() < 0 && buttonPopUpWidget.getIndex() == 0)
                    .setActiveBackgroundTexture(GuiTextures.SLOT, TJGuiTextures.PATTERN_OVERLAY)
                    .setInactiveBackgroundTexture(TJGuiTextures.BLANK_SLOT)
                    .setWidgetGroup(patternScrollableSlotGroup)
                    .setActiveInit(false));
        }
        tab.add(new LabelWidget(7, 109, "gui.appliedenergistics2.StoredItems"));
        tab.add(new LabelWidget(7, 123, "gui.appliedenergistics2.Patterns"));
        tab.add(new LabelWidget(7, 23, "gui.appliedenergistics2.Config"));
        tab.add(patternScrollableSlotGroup);
        tab.add(selectionWidgetGroup);
        this.createPatternMultiToolGUI(tab, patternMultiTool, multiUpgradeSlots, duality.getPatterns(), multiPatternSlots, invTag);
        for (int i = 0; i < upgradeHandler.getSlots(); i++) {
            tab.add(new TJSlotWidget<>(upgradeHandler, i, 186, 7 + (18 * i))
                    .setActiveBackgroundTexture(GuiTextures.SLOT, TJGuiTextures.UPGRADE_OVERLAY));
        }
        for (int i = 0; i < duality.getStorage().getSlots(); i++) {
            tab.add(new TJSlotWidget<>(duality.getStorage(), i, 7 + (18 * (i % 9)), 52 + (36 * (i / 9)))
                    .setActiveBackgroundTexture(GuiTextures.SLOT));
        }
        tab.add(new TJToggleButtonWidget(-18, 58, 16, 16, () -> duality.getConfigManager().getSetting(Settings.BLOCK).ordinal() == 0, this::setBlockingMode)
                .setToggleTooltipHoverText("gui.tooltips.appliedenergistics2.NonBlocking", "gui.tooltips.appliedenergistics2.Blocking")
                .setToggleTexture(TJGuiTextures.TOGGLE_BLOCKING_MODE)
                .useToggleTexture(true));
        tab.add(new TJToggleButtonWidget(-18, 94, 16, 16, () -> duality.getConfigManager().getSetting(Settings.INTERFACE_TERMINAL).ordinal() == 0, this::setInterfaceTerminal)
                .setTooltipText("gui.appliedenergistics2.InterfaceTerminalHint")
                .setToggleTexture(TJGuiTextures.TOGGLE_INTERFACE_TERMINAL)
                .useToggleTexture(true));
        tab.add(new TJToggleButtonWidget(-18, 112, 16, 16, () -> duality.getConfigManager().getSetting(Settings.OPERATION_MODE).ordinal() == 0, this::setFluidPacket)
                .setToggleTooltipHoverText("ae2fc.tooltip.real_fluid.hint", "ae2fc.tooltip.fake_packet.hint")
                .setToggleTexture(TJGuiTextures.TOGGLE_SEND_FLUID)
                .useToggleTexture(true));
        tab.add(new TJToggleButtonWidget(-18, 130, 16, 16, () -> duality.getConfigManager().getSetting(Settings.LEVEL_TYPE).ordinal() == 0, this::setSplittingItemsFluids)
                .setToggleTooltipHoverText("ae2fc.tooltip.allow_splitting.hint", "ae2fc.tooltip.prevent_splitting.hint")
                .setToggleTexture(TJGuiTextures.TOGGLE_SPLITTING_ITEMS_FLUIDS)
                .useToggleTexture(true));
        tab.add(new TJCycleButtonWidget<>(-18, 148, 16, 16, (EnumSet<CondenserOutput>) Settings.CONDENSER_OUTPUT.getPossibleValues(), () -> (Enum<CondenserOutput>) duality.getConfigManager().getSetting(Settings.CONDENSER_OUTPUT), this::setBlockModeEx)
                .setCycleHoverTooltipText("ae2fc.tooltip.block_all.hint", "ae2fc.tooltip.block_item.hint", "ae2fc.tooltip.block_fluid.hint")
                .setCycleTexture(TJGuiTextures.CYCLE_BLOCKING_MODE_EX));
        tab.add(new TJToggleButtonWidget(-18, 166, 16, 16, () -> duality.getConfigManager().getSetting(Settings.PLACE_BLOCK).ordinal() == 0, this::setIntelligentBlocking)
                .setToggleTooltipHoverText("gui.intelligent_blocking.CLOSE.text", "gui.intelligent_blocking.OPEN.text")
                .setToggleTexture(TJGuiTextures.TOGGLE_BLOCKING_MODE)
                .setInvertTexture(true)
                .useToggleTexture(true));
    }

    private void createFluidInterfaceTab(List<Widget> tab, ButtonPopUpWidget<?> buttonPopUpWidget) {
        final DualityFluidInterface duality = this.getDualityFluidInterface();
        final IItemHandler upgradeHandler = duality.getInventoryByName("upgrades");
        for (int i = 0; i < duality.getConfig().getSlots(); i++) {
            final int index = i;
            tab.add(new TJPhantomAEFluidSlotWidget(7 + (18 * (i % 9)), 34 + (72 * (i / 9)), 18, 18, i, duality.getConfig(), fluidStack -> ((IDualitySuperFluidInterface) duality).onFluidInventoryHasChanged(duality.getConfig(), index, null, null, null))
                    .setBackgroundTexture(TJGuiTextures.SLOT_DOWN));
        }
        for (int i = 0; i < upgradeHandler.getSlots(); i++) {
            tab.add(new TJSlotWidget<>(upgradeHandler, i, 186, 7 + (18 * i))
                    .setActiveBackgroundTexture(GuiTextures.SLOT, TJGuiTextures.UPGRADE_OVERLAY));
        }
        for (int i = 0; i < duality.getTanks().getSlots(); i++) {
            tab.add(new AEFluidTankWidget((AEFluidInventory) duality.getTanks(), i, 7 + (18 * (i % 9)), 52 + (72 * (i / 9)), 18, 54)
                    .setActiveSupplier(() -> buttonPopUpWidget.getIndex() == 0)
                    .setBackgroundTextures(GuiTextures.SLOT));
        }
        tab.add(new LabelWidget(7, 181, "gui.appliedenergistics2.StoredFluids"));
        tab.add(new LabelWidget(7, 23, "gui.appliedenergistics2.Config"));
        tab.add(new LabelWidget(7, 198, "container.inventory"));
    }

    private void createPatternMultiToolGUI(List<Widget> tab, ItemStack patternMultiTool, FilteredItemStackHandler multiUpgradeSlots, IItemHandler patternSlots, IItemHandler multiPatternSlots, NBTTagCompound invTag) {
        if (!patternMultiTool.isEmpty()) {
            final List<AEPatternSlotWidget> patternSlotWidgets = new ArrayList<>();
            final SlotScrollableWidgetGroup multiPatternSlotGroup = new SlotScrollableWidgetGroup(-118, 14, 72, 162, 4)
                    .setItemStackTransfer(itemStack -> TJItemUtils.insertIntoItemHandler(patternSlots, itemStack, false))
                    .setItemHandler(multiPatternSlots)
                    .setScrollWidth(0);
            tab.add(new ImageWidget(-125, 0, 105, 218, GuiTextures.BORDERED_BACKGROUND));
            tab.add(new LabelWidget(-118, 4, "item.nae2.pattern_multiplier.name"));
            tab.add(new ClickButtonWidget(-118, 176, 18, 18, "*2", data -> this.changePatternAmount(multiPatternSlots, 2, patternSlotWidgets, () -> this.writePatternMultiToolToNBT(multiPatternSlots, invTag))));
            tab.add(new ClickButtonWidget(-118, 194, 18, 18, "/2", data -> this.changePatternAmount(multiPatternSlots, -2, patternSlotWidgets, () -> this.writePatternMultiToolToNBT(multiPatternSlots, invTag))));
            tab.add(new ClickButtonWidget(-100, 176, 18, 18, "*3", data -> this.changePatternAmount(multiPatternSlots, 3, patternSlotWidgets, () -> this.writePatternMultiToolToNBT(multiPatternSlots, invTag))));
            tab.add(new ClickButtonWidget(-100, 194, 18, 18, "/3", data -> this.changePatternAmount(multiPatternSlots, -3, patternSlotWidgets, () -> this.writePatternMultiToolToNBT(multiPatternSlots, invTag))));
            tab.add(new ClickButtonWidget(-82, 176, 18, 18, "*4", data -> this.changePatternAmount(multiPatternSlots, 4, patternSlotWidgets, () -> this.writePatternMultiToolToNBT(multiPatternSlots, invTag))));
            tab.add(new ClickButtonWidget(-82, 194, 18, 18, "/4", data -> this.changePatternAmount(multiPatternSlots, -4, patternSlotWidgets, () -> this.writePatternMultiToolToNBT(multiPatternSlots, invTag))));
            for (int i = 0; i < multiPatternSlots.getSlots(); i++) {
                final int index = i;
                final AEPatternSlotWidget patternSlotWidget = new AEPatternSlotWidget(multiPatternSlots, i, 18 * (i / 9), 18 * (i % 9))
                        .setActiveBackgroundTexture(GuiTextures.SLOT, TJGuiTextures.PATTERN_OVERLAY)
                        .setActiveSupplier(() -> index / 9 <= multiUpgradeSlots.getSlotsFilled())
                        .setSlotLocationInfo(true, false)
                        .setInactiveBackgroundTexture(TJGuiTextures.BLANK_SLOT)
                        .setWidgetGroup(multiPatternSlotGroup);
                multiPatternSlotGroup.addWidget(patternSlotWidget);
                patternSlotWidgets.add(patternSlotWidget);
            }
            tab.add(multiPatternSlotGroup);
            for (int i = 0; i < multiUpgradeSlots.getSlots(); i++) {
                tab.add(new TJSlotWidget<>(multiUpgradeSlots, i, -46, 14 + (i * 18))
                        .setActiveBackgroundTexture(GuiTextures.SLOT, TJGuiTextures.UPGRADE_OVERLAY));
            }
        }
    }

    private void writePatternMultiToolToNBT(IItemHandler itemHandler, NBTTagCompound compound) {
        final NBTTagList tagList = new NBTTagList();
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            final ItemStack stack = itemHandler.getStackInSlot(i);
            if (!stack.isEmpty()) {
                final NBTTagCompound tagCompound = stack.serializeNBT();
                tagCompound.setInteger("Slot", i);
                tagList.appendTag(tagCompound);
            }
        }
        compound.setTag("Items", tagList);
    }

    private void readPatternMultiToolNBT(IItemHandlerModifiable itemHandler, NBTTagList tagList) {
        for (int i = 0; i < tagList.tagCount(); i++) {
            final NBTTagCompound compound = tagList.getCompoundTagAt(i);
            if (compound.hasKey("Slot")) {
                final ItemStack patternStack = TJItemUtils.getItemStackFromName(compound.getString("id"), compound.getInteger("Count"), compound.getShort("Damage"));
                patternStack.setTagCompound(compound.getCompoundTag("tag"));
                itemHandler.setStackInSlot(compound.getInteger("Slot"), patternStack);
            }
        }
    }

    private void changePatternAmount(IItemHandler patternSlots, int multiplier, List<AEPatternSlotWidget> patternSlotWidgets, Runnable callback) {
        final boolean divide = multiplier < 0;
        if (divide)
            multiplier = Math.abs(multiplier);
        final int finalMultiplier = multiplier;
        for (int i = 0; i < patternSlots.getSlots(); i++) {
            final ItemStack stack = patternSlots.getStackInSlot(i);
            final NBTTagCompound compound = stack.getTagCompound();
            if (stack.isEmpty() || compound == null) continue;
            final ResourceLocation resourcelocation = Item.REGISTRY.getNameForObject(stack.getItem());
            final String id = resourcelocation != null ? resourcelocation.toString() : "minecraft:air";
            final NBTTagList inputList = compound.getTagList(id.equals("ae2fc:dense_encoded_pattern") ? "Inputs" : "in", 10);
            final NBTTagList outputList = compound.getTagList(id.equals("ae2fc:dense_encoded_pattern") ? "Outputs" : "out", 10);
            final NBTTagList newInputList = new NBTTagList(), newOutputList = new NBTTagList();
            final Predicate<Boolean> setPatternInputs = simulate -> {
                for (int j = 0; j < inputList.tagCount(); j++) {
                    final NBTTagCompound patternCompound = inputList.getCompoundTagAt(j);
                    final long amount = patternCompound.hasKey("Cnt") ? patternCompound.getLong("Cnt") : patternCompound.getInteger("Count");
                    final long newAmount = divide ? amount / finalMultiplier : amount * finalMultiplier;
                    if (patternCompound.isEmpty()) {
                        if (!simulate)
                            newInputList.appendTag(patternCompound);
                        continue;
                    }
                    if (newAmount > 0 && newAmount <= Integer.MAX_VALUE) {
                        if (!simulate) {
                            if (id.equals("ae2fc:dense_encoded_pattern")) {
                                patternCompound.setLong("Cnt", newAmount);
                            } else patternCompound.setInteger("Count", (int) newAmount);
                            newInputList.appendTag(patternCompound);
                        }
                    } else return false;
                }
                for (int j = 0; j < outputList.tagCount(); j++) {
                    final NBTTagCompound patternCompound = outputList.getCompoundTagAt(j);
                    final long amount = patternCompound.hasKey("Cnt") ? patternCompound.getLong("Cnt") : patternCompound.getInteger("Count");
                    final long newAmount = divide ? amount / finalMultiplier : amount * finalMultiplier;
                    if (patternCompound.isEmpty()) {
                        if (!simulate)
                            newOutputList.appendTag(patternCompound);
                        continue;
                    }
                    if (newAmount > 0 && newAmount <= Integer.MAX_VALUE) {
                        if (!simulate) {
                            if (id.equals("ae2fc:dense_encoded_pattern")) {
                                patternCompound.setLong("Cnt", newAmount);
                            } else patternCompound.setInteger("Count", (int) newAmount);
                            newOutputList.appendTag(patternCompound);
                        }
                    } else return false;
                }
                if (!simulate) {
                    compound.setTag("in", newInputList);
                    compound.setTag("out", newOutputList);
                    if (id.equals("ae2fc:dense_encoded_pattern")) {
                        compound.setTag("Inputs", newInputList);
                        compound.setTag("Outputs", newOutputList);
                    }
                }
                return true;
            };
            if (setPatternInputs.test(true))
                setPatternInputs.test(false);
        }
        for (AEPatternSlotWidget slotWidget : patternSlotWidgets)
            slotWidget.forceUpdate();
        callback.run();
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

    private void setBlockingMode(boolean blockingMode) {
        this.getInterfaceDuality().getConfigManager().putSetting(Settings.BLOCK, blockingMode ? YesNo.YES : YesNo.NO);
        this.getTile().markDirty();
    }

    private void setInterfaceTerminal(boolean interfaceTerminal) {
        this.getInterfaceDuality().getConfigManager().putSetting(Settings.INTERFACE_TERMINAL, interfaceTerminal ? YesNo.YES : YesNo.NO);
        this.getTile().markDirty();
    }

    private void setFluidPacket(boolean fluidPacket) {
        this.getInterfaceDuality().getConfigManager().putSetting(Settings.OPERATION_MODE, fluidPacket ? OperationMode.FILL : OperationMode.EMPTY);
        ObfuscationReflectionHelper.setPrivateValue(DualityInterface.class, this.getInterfaceDuality(), fluidPacket, "fluidPacket");
        this.getTile().markDirty();
    }

    private void setSplittingItemsFluids(boolean splittingItemsFluids) {
        this.getInterfaceDuality().getConfigManager().putSetting(Settings.LEVEL_TYPE, splittingItemsFluids ? LevelType.ITEM_LEVEL : LevelType.ENERGY_LEVEL);
        ObfuscationReflectionHelper.setPrivateValue(DualityInterface.class, this.getInterfaceDuality(), splittingItemsFluids, "allowSplitting");
        this.getTile().markDirty();
    }

    private void setBlockModeEx(CondenserOutput blockModeEx) {
        this.getInterfaceDuality().getConfigManager().putSetting(Settings.CONDENSER_OUTPUT, blockModeEx);
        ObfuscationReflectionHelper.setPrivateValue(DualityInterface.class, this.getInterfaceDuality(), blockModeEx.ordinal(), "blockModeEx");
        this.getTile().markDirty();
    }

    private void setIntelligentBlocking(boolean intelligentBlocking) {
        this.getInterfaceDuality().getConfigManager().putSetting(Settings.PLACE_BLOCK, intelligentBlocking ? YesNo.YES : YesNo.NO);
        final RCIConfigManager configManager = ObfuscationReflectionHelper.getPrivateValue(DualityInterface.class, this.getInterfaceDuality(), "randomComplement$rcSettings");
        configManager.putSetting(RCSettings.IntelligentBlocking, intelligentBlocking ? IntelligentBlocking.OPEN : IntelligentBlocking.CLOSE);
        this.getTile().markDirty();
    }

    private void setStackSize(String text, String id) {
        final int slot = Integer.parseInt(id);
        final int maxSize = this.getInterfaceDuality().getConfig().getSlotLimit(0);
        final int stackSize = (int) Math.max(1, Math.min(Long.parseLong(text), maxSize));
        final ItemStack itemStack = this.getInterfaceDuality().getConfig().extractItem(slot, Integer.MAX_VALUE, false);
        if (itemStack.isEmpty()) return;
        itemStack.setCount(stackSize);
        ((AppEngInternalAEInventory) this.getInterfaceDuality().getConfig()).setStackInSlot(slot, itemStack);
        this.getTile().markDirty();
    }

    private String getStackSize(int index) {
        return String.valueOf(this.getInterfaceDuality().getConfig().getStackInSlot(index).getCount());
    }

    private void setPriority(String text, String id) {
        this.getInterfaceDuality().setPriority((int) Math.max(Integer.MIN_VALUE, Math.min(Integer.MAX_VALUE, Long.parseLong(text))));
        this.getTile().markDirty();
    }
}
