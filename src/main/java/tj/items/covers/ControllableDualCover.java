package tj.items.covers;

import gregicadditions.GAValues;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.cover.ICoverable;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.*;
import gregtech.api.gui.widgets.tab.VerticalTabListRenderer;
import gregtech.api.recipes.CountableIngredient;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.util.Position;
import gregtech.common.covers.CoverConveyor;
import gregtech.common.covers.CoverPump;
import gregtech.common.covers.TransferMode;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.apache.commons.lang3.ArrayUtils;
import tj.TJValues;
import tj.builder.WidgetTabBuilder;
import tj.capability.ParallelRecipeLRUCache;
import tj.mui.TJGuiTextures;
import tj.mui.widgets.ButtonWidget;
import tj.mui.widgets.impl.*;
import tj.mui.widgets.PopUpWidget;
import tj.items.TJMetaItems;
import tj.util.Counter;
import tj.util.TJItemUtils;
import tj.util.map.Strategies;
import tj.util.wrappers.GTFluidStackWrapper;
import tj.util.wrappers.GTItemStackWrapper;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.regex.Pattern;

import static gregtech.api.gui.widgets.tab.VerticalTabListRenderer.HorizontalLocation.LEFT;
import static gregtech.api.gui.widgets.tab.VerticalTabListRenderer.VerticalStartCorner.TOP;
import static gregtech.common.items.MetaItems.*;

public class ControllableDualCover extends DualCover {

    private final ItemStackHandler itemRecipeSearchSlot = new ItemStackHandler(1);
    private final IMultipleTankHandler fluidRecipeSearchSlot = new FluidTankList(true, new FluidTank(Integer.MAX_VALUE));
    private final Object2ObjectMap<ItemStack, Counter> itemExact = new Object2ObjectOpenCustomHashMap<>(Strategies.ITEMSTACK_STRATEGY);
    private final Object2ObjectMap<FluidStack, Counter> fluidExact = new Object2ObjectOpenHashMap<>();
    private final Object2ObjectMap<ItemStack, GTItemStackWrapper> itemRecipeMap = new Object2ObjectOpenCustomHashMap<>(Strategies.ITEMSTACK_STRATEGY);
    private final Object2ObjectMap<FluidStack, GTFluidStackWrapper> fluidRecipeMap = new Object2ObjectOpenHashMap<>();
    private final ParallelRecipeLRUCache recipeLRUCache = new ParallelRecipeLRUCache(10);
    private TransferMode robotArmMode = TransferMode.TRANSFER_ANY;
    private TransferMode regulatorMode = TransferMode.TRANSFER_ANY;
    private RecipeMode itemRecipeMode = RecipeMode.ELECTROLYZER;
    private RecipeMode fluidRecipeMode = RecipeMode.ELECTROLYZER;
    private int itemSupplyThroughput;
    private int fluidSupplyThroughput;

    public ControllableDualCover(ICoverable coverHolder, EnumFacing attachedSide, int tier) {
        super(coverHolder, attachedSide, tier);
        this.itemFilterSlot.setItemStackPredicate((slot, itemStack) -> ITEM_FILTER.isItemEqual(itemStack) || SMART_FILTER.isItemEqual(itemStack) || ORE_DICTIONARY_FILTER.isItemEqual(itemStack))
                .setOnContentsChangedPre((slot, itemStack, insert) -> {
                    if (!insert) return;
                    this.itemFilterType = ITEM_FILTER.isItemEqual(itemStack) ? FilterType.NORMAL : SMART_FILTER.isItemEqual(itemStack) ? FilterType.SMART : ORE_DICTIONARY_FILTER.isItemEqual(itemStack) ? FilterType.ORE_DICT : null;
                });
        this.fluidFilterSlot.setItemStackPredicate((slot, itemStack) -> FLUID_FILTER.isItemEqual(itemStack) || SMART_FILTER.isItemEqual(itemStack))
                .setOnContentsChangedPre((slot, itemStack, insert) -> {
                    if (!insert) return;
                    this.fluidFilterType = FLUID_FILTER.isItemEqual(itemStack) ? FilterType.NORMAL : SMART_FILTER.isItemEqual(itemStack) ? FilterType.SMART : null;
                });
    }

    @Override
    public void onAttached(ItemStack itemStack) {
        super.onAttached(itemStack);
        final NBTTagCompound compound = itemStack.getOrCreateSubCompound("init");
        if (compound.hasKey("robotArmMode"))
            this.robotArmMode = TransferMode.values()[compound.getInteger("robotArmMode")];
        if (compound.hasKey("regulatorMode"))
            this.regulatorMode = TransferMode.values()[compound.getInteger("regulatorMode")];
        if (compound.hasKey("itemRecipeMode"))
            this.itemRecipeMode = RecipeMode.values()[compound.getInteger("itemRecipeMode")];
        if (compound.hasKey("fluidRecipeMode"))
            this.fluidRecipeMode = RecipeMode.values()[compound.getInteger("fluidRecipeMode")];
        if (compound.hasKey("itemSupplyThroughput"))
            this.itemSupplyThroughput = compound.getInteger("itemSupplyThroughput");
        if (compound.hasKey("fluidSupplyThroughput"))
            this.fluidSupplyThroughput = compound.getInteger("fluidSupplyThroughput");
    }

    @Override
    public ModularUI createUI(EntityPlayer player) {
        final WidgetTabBuilder tabBuilder = new WidgetTabBuilder()
                .setTabListRenderer(() -> new VerticalTabListRenderer(TOP, LEFT))
                .addTab(String.format("metaitem.robot.arm.%s.name", GAValues.VN[this.tier].toLowerCase()), TJMetaItems.ROBOT_ARMS[this.tier].getStackForm(), this::createRobotArmTab)
                .addTab(String.format("metaitem.fluid.regulator.%s.name", GAValues.VN[this.tier].toLowerCase()), TJMetaItems.FLUID_REGULATORS[this.tier].getStackForm(), this::createRegulatorTab);
        return ModularUI.builder(GuiTextures.BORDERED_BACKGROUND, 176, 272)
                .widget(new TJLabelWidget(7, -18, 162, 18, TJGuiTextures.MACHINE_LABEL_2)
                        .setItemLabel(this.getPickItem()).setLocale(String.format("metaitem.controllable_dual_cover.%s.name", GAValues.VN[this.tier].toLowerCase())))
                .widget(tabBuilder.build())
                .bindPlayerInventory(player.inventory, GuiTextures.SLOT, 7, 190)
                .build(this, player);
    }

    private void createRobotArmTab(List<Widget> widgetGroup) {
        final WidgetGroup itemWidgetGroup = new WidgetGroup(new Position(7, 95));
        final SelectionWidgetGroup itemSelectionWidgetGroup = new SelectionWidgetGroup(7, 95, 72, 72);
        final ButtonWidget<?> clickButtonDivide = new ButtonWidget<>(84, 18, 38, 18, "/2", data -> this.setItemCount(String.valueOf(Long.parseLong(this.getItemCount(itemSelectionWidgetGroup.getIndex())) / 2), String.valueOf(itemSelectionWidgetGroup.getIndex())));
        final ButtonWidget<?> clickButtonMultiply = new ButtonWidget<>(122, 18, 38, 18, "*2", data -> this.setItemCount(String.valueOf(Long.parseLong(this.getItemCount(itemSelectionWidgetGroup.getIndex())) * 2), String.valueOf(itemSelectionWidgetGroup.getIndex())));
        final NewTextFieldWidget<?> itemCountTextField = new NewTextFieldWidget<>(84, 0, 76, 18, true, null, this::setItemCount)
                .setValidator(str -> Pattern.compile("-*?[0-9_]*\\*?").matcher(str).matches())
                .setTooltipText("tj.machine.universal.item_amount")
                .setUpdateOnTyping(true);
        itemCountTextField.setTooltipFormat(() -> new String[]{this.getItemCount((int) itemCountTextField.getTextIdLong())})
                .setTextSupplier(() -> this.getItemCount((int) itemCountTextField.getTextIdLong()));
        itemSelectionWidgetGroup.setIndexListener(itemCountTextField::setTextIdLong);
        for (int i = 0; i < this.itemFilter.getSlots(); i++) {
            itemWidgetGroup.addWidget(new TJPhantomItemSlotWidget(18 * (i % 4), 18 * (i / 4), 18, 18, i, this.itemFilter, item -> {
                if (!item.isEmpty())
                    this.itemType.put(item, item);
            }, this.itemType::remove).setBackgroundTextures(GuiTextures.SLOT)
                    .setPutItemsPredicate(item -> this.itemType.get(item) == null));
            itemSelectionWidgetGroup.addSubWidget(i, clickButtonDivide.setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
            itemSelectionWidgetGroup.addSubWidget(i, clickButtonMultiply.setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
            itemSelectionWidgetGroup.addSubWidget(i, itemCountTextField);
            itemSelectionWidgetGroup.addSelectionBox(i, 18 * (i % 4), 18 * (i / 4), 18, 18);
        }
        final WidgetGroup itemSupplyWidgetGroup = new WidgetGroup();
        itemSupplyWidgetGroup.addWidget(new NewTextFieldWidget<>(92, 95, 76, 18, true, () -> String.valueOf(this.itemSupplyThroughput), this::setItemSupplyThroughput)
                .setTooltipFormat(() -> ArrayUtils.toArray(String.valueOf(this.itemSupplyThroughput)))
                .setValidator(str -> Pattern.compile("-*?[0-9_]*\\*?").matcher(str).matches())
                .setTooltipText("tj.machine.universal.item_amount")
                .setUpdateOnTyping(true));
        itemSupplyWidgetGroup.addWidget(new ButtonWidget<>(92, 113, 38, 18, "/2", data -> this.setItemSupplyThroughput(String.valueOf((long) this.itemSupplyThroughput / 2), "")).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
        itemSupplyWidgetGroup.addWidget(new ButtonWidget<>(130, 113, 38, 18, "*2", data -> this.setItemSupplyThroughput(String.valueOf((long) this.itemSupplyThroughput * 2), "")).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
        final PopUpWidget<?> itemFilterPopup = new PopUpWidget<>()
                .setClickToDefault(false)
                .setIndexSupplier(() -> {
                    final ItemStack itemStack = this.itemFilterSlot.getStackInSlot(0);
                    return ITEM_FILTER.isItemEqual(itemStack) ? 1 : SMART_FILTER.isItemEqual(itemStack) ? 2 : ORE_DICTIONARY_FILTER.isItemEqual(itemStack) ? 3 : 0;
                }).addPopup(widgetGroup1 -> {
                    widgetGroup1.addWidget(itemSupplyWidgetGroup);
                    return false;
                }).addPopup(widgetGroup1 -> {
                    widgetGroup1.addWidget(itemWidgetGroup);
                    widgetGroup1.addWidget(itemSelectionWidgetGroup);
                    return false;
                }).addPopup(widgetGroup1 -> {
                    widgetGroup1.addWidget(new TJCycleButtonWidget<>(10, 133, 76, 18, RecipeMode.class, () -> this.itemRecipeMode, this::setItemRecipeMode).setCycleTexture(GuiTextures.VANILLA_BUTTON));
                    return false;
                }).addPopup(widgetGroup1 -> {
                    widgetGroup1.addWidget(itemSupplyWidgetGroup);
                    this.oreDictionaryItemFilter.initUI(widgetGroup1::addWidget);
                    return false;
                });
        widgetGroup.add(new LabelWidget(7, 5, "cover.robotic_arm.title", GAValues.VN[this.tier]));
        widgetGroup.add(new ButtonWidget<>(7, 20, 23, 20, "-10", data -> this.setItemTransferRate(this.itemTransferRate - (data.isShiftClick ? 100 : 10))).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
        widgetGroup.add(new ButtonWidget<>(146, 20, 23, 20, "+10", data -> this.setItemTransferRate(this.itemTransferRate + (data.isShiftClick ? 100 : 10))).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
        widgetGroup.add(new ButtonWidget<>(30, 20, 20, 20, "-1", data -> this.setItemTransferRate(this.itemTransferRate - (data.isShiftClick ? 5 : 1))).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
        widgetGroup.add(new ButtonWidget<>(126, 20, 20, 20, "+1", data -> this.setItemTransferRate(this.itemTransferRate + (data.isShiftClick ? 5 : 1))).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
        widgetGroup.add(new ButtonWidget<>(50, 20, 38, 20,"/2", data -> this.setItemTransferRate(this.itemTransferRate / 2D)).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
        widgetGroup.add(new ButtonWidget<>(88, 20, 38, 20, "*2", data -> this.setItemTransferRate(this.itemTransferRate * 2)).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
        widgetGroup.add(new NewTextFieldWidget<>(7, 40, 162, 18, true, () -> String.valueOf(this.itemTransferRate), this::setItemTransferRate)
                .setTooltipText("tj.machine.universal.item_throughput").setTooltipFormat(() -> new String[]{String.valueOf(this.itemTransferRate)})
                .setValidator(str -> Pattern.compile("-*?[0-9_]*\\*?").matcher(str).matches())
                .setUpdateOnTyping(true));
        widgetGroup.add(new TJCycleButtonWidget<>(7, 65, 76, 20, CoverConveyor.ConveyorMode.class, () -> this.conveyorMode, this::setConveyorMode).setCycleTexture(GuiTextures.VANILLA_BUTTON));
        widgetGroup.add(new TJCycleButtonWidget<>(92, 65, 76, 20, TransferMode.class, () -> this.robotArmMode, this::setRobotArmMode)
                .setHoverTooltipText("cover.robotic_arm.transfer_mode.description")
                .setCycleTexture(GuiTextures.VANILLA_BUTTON));
        widgetGroup.add(new ImageWidget(-28, 127, 26, 44, GuiTextures.BORDERED_BACKGROUND));
        widgetGroup.add(new TJSlotWidget<>(this.itemFilterSlot, 0, -24, 131)
                .setActiveBackgroundTexture(GuiTextures.SLOT, GuiTextures.FILTER_SLOT_OVERLAY));
        widgetGroup.add(itemFilterPopup);
        widgetGroup.add(new TJToggleButtonWidget(-24, 149, 18, 18, GuiTextures.BUTTON_BLACKLIST, () -> this.isItemBlacklist, this::setItemBlacklist)
                .setToggleTitleTooltipHoverText("cover.filter.blacklist.disabled", "cover.filter.blacklist.enabled"));
        widgetGroup.add(new NewTextFieldWidget<>(92, 133, 76, 18, true, () -> String.valueOf(this.itemTicks), this::setItemTicks)
                .setValidator(str -> Pattern.compile("-*?[0-9_]*\\*?").matcher(str).matches())
                .setTooltipText("machine.universal.ticks.operation")
                .setUpdateOnTyping(true));
        widgetGroup.add(new ButtonWidget<>(92, 151, 38, 18, "/2", data -> this.setItemTicks(String.valueOf((long) this.itemTicks / 2), "")).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
        widgetGroup.add(new ButtonWidget<>(130, 151, 38, 18, "*2", data -> this.setItemTicks(String.valueOf((long) this.itemTicks * 2), "")).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
        widgetGroup.add(new ImageWidget(-28, 244, 26, 26, GuiTextures.BORDERED_BACKGROUND));
        widgetGroup.add(new TJToggleButtonWidget(-24, 248, 18, 18, TJGuiTextures.TOGGLE_POWER_BUTTON, () -> this.isConveyorWorking, this::setConveyorWorking)
                .setToggleTitleTooltipHoverText("machine.universal.toggle.run.mode.disabled", "machine.universal.toggle.run.mode.enabled"));
    }

    private void createRegulatorTab(List<Widget> widgetGroup) {
        final WidgetGroup fluidWidgetGroup = new WidgetGroup(new Position(7, 115));
        final SelectionWidgetGroup fluidSelectionWidgetGroup = new SelectionWidgetGroup(7, 115, 72, 72);
        final ButtonWidget<?> clickButtonDivide = new ButtonWidget<>(84, 18, 38, 18, "/2", data -> this.setFluidCount(String.valueOf(Long.parseLong(this.getFluidCount(fluidSelectionWidgetGroup.getIndex())) / 2), String.valueOf(fluidSelectionWidgetGroup.getIndex())));
        final ButtonWidget<?> clickButtonMultiply = new ButtonWidget<>(122, 18, 38, 18, "*2", data -> this.setFluidCount(String.valueOf(Long.parseLong(this.getFluidCount(fluidSelectionWidgetGroup.getIndex())) * 2), String.valueOf(fluidSelectionWidgetGroup.getIndex())));
        final NewTextFieldWidget<?> fluidCountTextField = new NewTextFieldWidget<>(84, 0, 76, 18, true, null, this::setFluidCount)
                .setValidator(str -> Pattern.compile("-*?[0-9_]*\\*?").matcher(str).matches())
                .setTooltipText("tj.machine.universal.fluid_amount")
                .setUpdateOnTyping(true);
        fluidCountTextField.setTooltipFormat(() -> new String[]{this.getFluidCount((int) fluidCountTextField.getTextIdLong())})
                .setTextSupplier(() -> this.getFluidCount((int) fluidCountTextField.getTextIdLong()));
        fluidSelectionWidgetGroup.setIndexListener(fluidCountTextField::setTextIdLong);
        for (int i = 0; i < this.fluidFilter.getTanks(); i++) {
            fluidWidgetGroup.addWidget(new TJPhantomFluidSlotWidget(18 * (i % 4), 18 * (i / 4), 18, 18, i, this.fluidFilter, fluid -> {
                if (fluid != null)
                    this.fluidType.put(fluid, fluid);
            }, this.fluidType::remove).setBackgroundTexture(GuiTextures.FLUID_SLOT)
                    .setPutFluidsPredicate(fluid -> this.fluidType.get(fluid) == null));
            fluidSelectionWidgetGroup.addSubWidget(i, clickButtonDivide.setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
            fluidSelectionWidgetGroup.addSubWidget(i, clickButtonMultiply.setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
            fluidSelectionWidgetGroup.addSubWidget(i, fluidCountTextField);
            fluidSelectionWidgetGroup.addSelectionBox(i, 18 * (i % 4), 18 * (i / 4), 18, 18);
        }
        final PopUpWidget<?> fluidFilterPopup = new PopUpWidget<>()
                .setClickToDefault(false)
                .setIndexSupplier(() -> {
                    final ItemStack itemStack = this.fluidFilterSlot.getStackInSlot(0);
                    return FLUID_FILTER.isItemEqual(itemStack) ? 1 : SMART_FILTER.isItemEqual(itemStack) ? 2 : 0;
                }).addPopup(widgetGroup1 -> {
                    widgetGroup1.addWidget(new NewTextFieldWidget<>(92, 115, 76, 18, true, () -> String.valueOf(this.fluidSupplyThroughput), this::setFluidSupplyThroughput)
                            .setTooltipFormat(() -> ArrayUtils.toArray(String.valueOf(this.fluidSupplyThroughput)))
                            .setValidator(str -> Pattern.compile("-*?[0-9_]*\\*?").matcher(str).matches())
                            .setTooltipText("tj.machine.universal.fluid_amount")
                            .setUpdateOnTyping(true));
                    widgetGroup1.addWidget(new ButtonWidget<>(92, 133, 38, 18, "/2", data -> this.setFluidSupplyThroughput(String.valueOf((long) this.fluidSupplyThroughput / 2), "")).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
                    widgetGroup1.addWidget(new ButtonWidget<>(130, 133, 38, 18, "*2", data -> this.setFluidSupplyThroughput(String.valueOf((long) this.fluidSupplyThroughput * 2), "")).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
                    return false;
                }).addPopup(widgetGroup1 -> {
                    widgetGroup1.addWidget(fluidWidgetGroup);
                    widgetGroup1.addWidget(fluidSelectionWidgetGroup);
                    return false;
                }).addPopup(widgetGroup1 -> {
                    widgetGroup1.addWidget(new TJCycleButtonWidget<>(10, 151, 76, 18, RecipeMode.class, () -> this.fluidRecipeMode, this::setFluidRecipeMode));
                    return false;
                });
        widgetGroup.add(new LabelWidget(7, 5, "cover.fluid_regulator.title", GAValues.VN[this.tier]));
        widgetGroup.add(new ButtonWidget<>(7, 20, 37, 20, "-100", data -> this.setFluidTransferRate(this.fluidTransferRate - (data.isShiftClick ? 500 : 100))).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
        widgetGroup.add(new ButtonWidget<>(132, 20, 37, 20, "+100", data -> this.setFluidTransferRate(this.fluidTransferRate + (data.isShiftClick ? 500 : 100))).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
        widgetGroup.add(new ButtonWidget<>(44, 20, 22, 20, "-10", data -> this.setFluidTransferRate(this.fluidTransferRate - (data.isShiftClick ? 50 : 10))).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
        widgetGroup.add(new ButtonWidget<>(108, 20, 24, 20, "+10", data -> this.setFluidTransferRate(this.fluidTransferRate + (data.isShiftClick ? 50 : 10))).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
        widgetGroup.add(new ButtonWidget<>(68, 20, 20, 20, "-1", data -> this.setFluidTransferRate(this.fluidTransferRate - (data.isShiftClick ? 5 : 1))).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
        widgetGroup.add(new ButtonWidget<>(88, 20, 20, 20, "+1", data -> this.setFluidTransferRate(this.fluidTransferRate + (data.isShiftClick ? 5 : 1))).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
        widgetGroup.add(new ButtonWidget<>(7, 40, 81, 20, "/2", data -> this.setFluidTransferRate(this.fluidTransferRate / 2D)).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
        widgetGroup.add(new ButtonWidget<>(88, 40, 81, 20, "*2", data -> this.setFluidTransferRate(this.fluidTransferRate * 2)).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
        widgetGroup.add(new NewTextFieldWidget<>(7, 60, 162, 18, true, () -> String.valueOf(this.fluidTransferRate), this::setFluidTransferRate)
                .setTooltipText("tj.machine.universal.fluid_throughput").setTooltipFormat(() -> new String[]{String.valueOf(this.fluidTransferRate)})
                .setValidator(str -> Pattern.compile("-*?[0-9_]*\\*?").matcher(str).matches())
                .setUpdateOnTyping(true));
        widgetGroup.add(new TJCycleButtonWidget<>(7, 85, 76, 18, CoverPump.PumpMode.class, () -> this.pumpMode, this::setPumpMode).setCycleTexture(GuiTextures.VANILLA_BUTTON));
        widgetGroup.add(new TJCycleButtonWidget<>(88, 85, 76, 18, TransferMode.class, () -> this.regulatorMode, this::setRegulatorMode)
                .setHoverTooltipText("cover.fluid_regulator.transfer_mode.description")
                .setCycleTexture(GuiTextures.VANILLA_BUTTON));
        widgetGroup.add(new ImageWidget(-28, 147, 26, 44, GuiTextures.BORDERED_BACKGROUND));
        widgetGroup.add(new TJSlotWidget<>(this.fluidFilterSlot, 0, -24, 151)
                .setActiveBackgroundTexture(GuiTextures.SLOT, GuiTextures.FILTER_SLOT_OVERLAY));
        widgetGroup.add(fluidFilterPopup);
        widgetGroup.add(new TJToggleButtonWidget(-24, 169, 18, 18, GuiTextures.BUTTON_BLACKLIST, () -> this.isFluidBlacklist, this::setFluidBlacklist)
                .setToggleTitleTooltipHoverText("cover.filter.blacklist.disabled", "cover.filter.blacklist.enabled"));
        widgetGroup.add(new NewTextFieldWidget<>(92, 151, 76, 18, true, () -> String.valueOf(this.fluidTicks), this::setFluidTicks)
                .setValidator(str -> Pattern.compile("-*?[0-9_]*\\*?").matcher(str).matches())
                .setTooltipText("machine.universal.ticks.operation")
                .setUpdateOnTyping(true));
        widgetGroup.add(new ButtonWidget<>(92, 169, 38, 18, "/2", data -> this.setFluidTicks(String.valueOf((long) this.fluidTicks / 2), "")).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
        widgetGroup.add(new ButtonWidget<>(130, 169, 38, 18, "*2", data -> this.setFluidTicks(String.valueOf((long) this.fluidTicks * 2), "")).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
        widgetGroup.add(new ImageWidget(-28, 244, 26, 26, GuiTextures.BORDERED_BACKGROUND));
        widgetGroup.add(new TJToggleButtonWidget(-24, 248, 18, 18, TJGuiTextures.TOGGLE_POWER_BUTTON, () -> this.isPumpWorking, this::setPumpWorking)
                .setToggleTitleTooltipHoverText("machine.universal.toggle.run.mode.disabled", "machine.universal.toggle.run.mode.enabled"));
    }

    @Override
    protected void transferItems(IItemHandler itemHandler, IItemHandler destItemHandler) {
        switch (this.robotArmMode) {
            case TRANSFER_ANY: super.transferItems(itemHandler, destItemHandler);
                break;
            case TRANSFER_EXACT: this.transferItemsSupply(itemHandler, destItemHandler);
                break;
            case KEEP_EXACT: this.transferItemsExact(itemHandler, destItemHandler);
        }
    }

    @Override
    protected void transferFluids(IFluidHandler fluidHandler, IFluidHandler destFluidHandler) {
        switch (this.regulatorMode) {
            case TRANSFER_ANY: super.transferFluids(fluidHandler, destFluidHandler);
                break;
            case TRANSFER_EXACT: this.transferFluidsSupply(fluidHandler, destFluidHandler);
                break;
            case KEEP_EXACT: this.transferFluidsExact(fluidHandler, destFluidHandler);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("robotArmMode", this.robotArmMode.ordinal());
        tagCompound.setInteger("regulatorMode", this.regulatorMode.ordinal());
        tagCompound.setInteger("itemRecipeMode", this.itemRecipeMode.ordinal());
        tagCompound.setInteger("fluidRecipeMode", this.fluidRecipeMode.ordinal());
        tagCompound.setInteger("itemSupplyThroughput", this.itemSupplyThroughput);
        tagCompound.setInteger("fluidSupplyThroughput", this.fluidSupplyThroughput);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        this.robotArmMode = TransferMode.values()[tagCompound.getInteger("robotArmMode")];
        this.regulatorMode = TransferMode.values()[tagCompound.getInteger("regulatorMode")];
        this.itemRecipeMode = RecipeMode.values()[tagCompound.getInteger("itemRecipeMode")];
        this.fluidRecipeMode = RecipeMode.values()[tagCompound.getInteger("fluidRecipeMode")];
        this.itemSupplyThroughput = tagCompound.getInteger("itemSupplyThroughput");
        this.fluidSupplyThroughput = tagCompound.getInteger("fluidSupplyThroughput");
    }

    private void transferItemsSupply(IItemHandler itemHandler, IItemHandler destItemHandler) {
        if (this.itemSupplyThroughput > this.maxItemTransferRate) return;
        switch (this.itemFilterType) {
            case NORMAL:
                for (int i = 0; i < itemHandler.getSlots(); i++) {
                    final ItemStack stack = itemHandler.getStackInSlot(i);
                    final ItemStack filterStack;
                    if (!stack.isEmpty() && this.isItemBlacklist == ((filterStack = this.itemType.get(stack.getItem())) == null)) {
                        final int inserted = TJItemUtils.insertIntoItemHandler(destItemHandler, stack, true).getCount();
                        if (filterStack != null && stack.getCount() >= filterStack.getCount()) {
                            final int extract = Math.min(filterStack.getCount(), stack.getCount() - inserted);
                            if (extract < 1) continue;
                            final ItemStack otherStack = itemHandler.extractItem(i, extract, false);
                            TJItemUtils.insertIntoItemHandler(destItemHandler, otherStack, false);
                        }
                    }
                }
                break;
            case SMART:
                for (int i = 0; i < itemHandler.getSlots(); i++) {
                    final ItemStack stack = itemHandler.getStackInSlot(i);
                    if (!stack.isEmpty()) {
                        GTItemStackWrapper itemStackWrapper = this.itemRecipeMap.get(stack);
                        if (itemStackWrapper == null || itemStackWrapper.getItemStack().isItemEqual(stack)) {
                            this.itemRecipeSearchSlot.setStackInSlot(0, stack);
                            Recipe recipe = this.recipeLRUCache.get(this.itemRecipeSearchSlot, TJValues.DUMMY_FLUID_HANDLER);
                            if (recipe == null) {
                                recipe = this.itemRecipeMode.getRecipeMap().findRecipe(Integer.MAX_VALUE, this.itemRecipeSearchSlot, TJValues.DUMMY_FLUID_HANDLER, Integer.MAX_VALUE);
                                this.recipeLRUCache.put(recipe);
                            }
                            if (recipe != null) {
                                itemStackWrapper = new GTItemStackWrapper(stack, 0);
                                for (CountableIngredient ingredient : recipe.getInputs()) {
                                    if (ingredient.getIngredient().apply(stack))
                                        itemStackWrapper.increment(ingredient.getCount());
                                }
                                this.itemRecipeMap.put(stack, itemStackWrapper);
                            } else continue;
                        }
                        final int inserted = TJItemUtils.insertIntoItemHandler(destItemHandler, stack, true).getCount();
                        if (stack.getCount() >= itemStackWrapper.getCount()) {
                            final int extract = Math.min(stack.getCount() - inserted, itemStackWrapper.getCount());
                            if (extract < 1) continue;
                            final ItemStack otherStack = itemHandler.extractItem(i, extract, false);
                            TJItemUtils.insertIntoItemHandler(destItemHandler, otherStack, false);
                        }
                    }
                }
                break;
            case ORE_DICT:
                for (int i = 0; i < itemHandler.getSlots(); i++) {
                    final ItemStack stack = itemHandler.getStackInSlot(i);
                    if (!stack.isEmpty() && this.oreDictionaryItemFilter.matchItemStack(stack) != null) {
                        final int inserted = TJItemUtils.insertIntoItemHandler(destItemHandler, stack, true).getCount();
                        if (stack.getCount() >= this.itemSupplyThroughput) {
                            final int extract = Math.min(this.itemSupplyThroughput, stack.getCount() - inserted);
                            if (extract < 1) continue;
                            final ItemStack otherStack = itemHandler.extractItem(i, extract, false);
                            TJItemUtils.insertIntoItemHandler(destItemHandler, otherStack, false);
                        }
                    }
                }
                break;
            default:
                for (int i = 0; i < itemHandler.getSlots(); i++) {
                    final ItemStack stack = itemHandler.getStackInSlot(i);
                    if (!stack.isEmpty()) {
                        final int inserted = TJItemUtils.insertIntoItemHandler(destItemHandler, stack, true).getCount();
                        if (stack.getCount() >= this.itemSupplyThroughput) {
                            final int extract = Math.min(this.itemSupplyThroughput, stack.getCount() - inserted);
                            if (extract < 1) continue;
                            final ItemStack otherStack = itemHandler.extractItem(i, extract, false);
                            TJItemUtils.insertIntoItemHandler(destItemHandler, otherStack, false);
                        }
                    }
                }
        }
    }

    private void transferItemsExact(IItemHandler itemHandler, IItemHandler destItemHandler) {
        for (int i = 0; i < destItemHandler.getSlots(); i++) {
            final ItemStack stack = destItemHandler.getStackInSlot(i);
            if (stack.isEmpty()) continue;
            this.itemExact.computeIfAbsent(stack, k -> new Counter(0))
                    .increment(stack.getCount());
        }
        switch (this.itemFilterType) {
            case NORMAL:
                for (int i = 0; i < itemHandler.getSlots(); i++) {
                    final ItemStack stack = itemHandler.getStackInSlot(i);
                    final ItemStack filterStack;
                    if (!stack.isEmpty() && this.isItemBlacklist == ((filterStack = this.itemType.get(stack)) == null)) {
                        if (filterStack == null) continue;
                        final Counter counter = this.itemExact.computeIfAbsent(stack, k -> new Counter(0));
                        final int inserted = TJItemUtils.insertIntoItemHandler(destItemHandler, stack, true).getCount();
                        final int extract = (int) Math.min(stack.getCount() - inserted, filterStack.getCount() - counter.getValue());
                        if (extract < 1) continue;
                        counter.increment(extract);
                        final ItemStack otherStack = itemHandler.extractItem(i, extract, false);
                        TJItemUtils.insertIntoItemHandler(destItemHandler, otherStack, false);
                    }
                }
                break;
            case SMART:
                for (int i = 0; i < itemHandler.getSlots(); i++) {
                    final ItemStack stack = itemHandler.getStackInSlot(i);
                    if (!stack.isEmpty()) {
                        GTItemStackWrapper itemStackWrapper = this.itemRecipeMap.get(stack);
                        if (itemStackWrapper == null) {
                            this.itemRecipeSearchSlot.setStackInSlot(0, stack);
                            Recipe recipe = this.recipeLRUCache.get(this.itemRecipeSearchSlot, TJValues.DUMMY_FLUID_HANDLER);
                            if (recipe == null) {
                                recipe = this.itemRecipeMode.getRecipeMap().findRecipe(Integer.MAX_VALUE, this.itemRecipeSearchSlot, TJValues.DUMMY_FLUID_HANDLER, Integer.MAX_VALUE);
                                this.recipeLRUCache.put(recipe);
                            }
                            if (recipe != null) {
                                itemStackWrapper = new GTItemStackWrapper(stack, 0);
                                for (CountableIngredient ingredient : recipe.getInputs()) {
                                    if (ingredient.getIngredient().apply(stack))
                                        itemStackWrapper.increment(ingredient.getCount());
                                }
                                this.itemRecipeMap.put(stack, itemStackWrapper);
                            } else continue;
                        }
                        final Counter counter = this.itemExact.computeIfAbsent(stack, k -> new Counter(0));
                        final int inserted = TJItemUtils.insertIntoItemHandler(destItemHandler, stack, true).getCount();
                        final int extract = (int) Math.min(stack.getCount() - inserted, itemStackWrapper.getCount() - counter.getValue());
                        if (extract < 1) continue;
                        counter.increment(extract);
                        final ItemStack otherStack = itemHandler.extractItem(i, extract, false);
                        TJItemUtils.insertIntoItemHandler(destItemHandler, otherStack, false);
                    }
                }
                break;
            case ORE_DICT:
                for (int i = 0; i < itemHandler.getSlots(); i++) {
                    final ItemStack stack = itemHandler.getStackInSlot(i);
                    if (!stack.isEmpty() && this.oreDictionaryItemFilter.matchItemStack(stack) != null) {
                        final Counter counter = this.itemExact.computeIfAbsent(stack, k -> new Counter(0));
                        final int inserted = TJItemUtils.insertIntoItemHandler(destItemHandler, stack, true).getCount();
                        final int extract = (int) Math.min(stack.getCount() - inserted, this.itemTransferRate - counter.getValue());
                        if (extract < 1) continue;
                        counter.increment(extract);
                        final ItemStack otherStack = itemHandler.extractItem(i, extract, false);
                        TJItemUtils.insertIntoItemHandler(destItemHandler, otherStack, false);
                    }
                }
                break;
            default:
                for (int i = 0; i < itemHandler.getSlots(); i++) {
                    final ItemStack stack = itemHandler.getStackInSlot(i);
                    if (!stack.isEmpty()) {
                        final Counter counter = this.itemExact.computeIfAbsent(stack, k -> new Counter(0));
                        final int inserted = TJItemUtils.insertIntoItemHandler(destItemHandler, stack, true).getCount();
                        final int extract = (int) Math.min(stack.getCount() - inserted, this.itemTransferRate - counter.getValue());
                        if (extract < 1) continue;
                        counter.increment(extract);
                        final ItemStack otherStack = itemHandler.extractItem(i, extract, false);
                        TJItemUtils.insertIntoItemHandler(destItemHandler, otherStack, false);
                    }
                }
        }
        this.itemExact.clear();
    }

    private void transferFluidsSupply(IFluidHandler fluidHandler, IFluidHandler destFluidHandler) {
        if (this.fluidSupplyThroughput > this.maxFluidTransferRate) return;
        final IFluidTankProperties[] tanks = fluidHandler.getTankProperties();
        switch (this.fluidFilterType) {
            case NORMAL:
                for (IFluidTankProperties tank : tanks) {
                    FluidStack fluidStack = tank.getContents();
                    final FluidStack filterStack;
                    if (fluidStack != null && this.isFluidBlacklist == ((filterStack = this.fluidType.get(fluidStack)) == null)) {
                        fluidStack = fluidHandler.drain(fluidStack, false);
                        if (fluidStack == null || filterStack == null) continue;
                        if (fluidStack.amount >= filterStack.amount) {
                            fluidStack.amount = filterStack.amount;
                            fluidStack.amount = destFluidHandler.fill(fluidStack, true);
                            fluidHandler.drain(fluidStack, true);
                        }
                    }
                }
                break;
            case SMART:
                for (IFluidTankProperties iFluidTankProperties : tanks) {
                    FluidStack fluidStack = iFluidTankProperties.getContents();
                    if (fluidStack != null) {
                        GTFluidStackWrapper fluidStackWrapper = this.fluidRecipeMap.get(fluidStack);
                        if (fluidStackWrapper == null) {
                            this.fluidRecipeSearchSlot.getTankAt(0).drain(Integer.MAX_VALUE, true);
                            this.fluidRecipeSearchSlot.getTankAt(0).fill(fluidStack, true);
                            Recipe recipe = this.recipeLRUCache.get(TJValues.DUMMY_ITEM_HANDLER, this.fluidRecipeSearchSlot);
                            if (recipe == null) {
                                recipe = this.fluidRecipeMode.getRecipeMap().findRecipe(Integer.MAX_VALUE, TJValues.DUMMY_ITEM_HANDLER, this.fluidRecipeSearchSlot, Integer.MAX_VALUE);
                                this.recipeLRUCache.put(recipe);
                            }
                            if (recipe != null) {
                                fluidStackWrapper = new GTFluidStackWrapper(fluidStack, 0);
                                for (FluidStack stack : recipe.getFluidInputs()) {
                                    if (stack.isFluidEqual(fluidStack))
                                        fluidStackWrapper.increment(stack.amount);
                                }
                                this.fluidRecipeMap.put(fluidStack, fluidStackWrapper);
                            } else continue;
                        }
                        fluidStack = fluidHandler.drain(fluidStack, false);
                        if (fluidStack == null) continue;
                        if (fluidStack.amount >= fluidStackWrapper.getCount()) {
                            fluidStack.amount = fluidStackWrapper.getCount();
                            fluidStack.amount = destFluidHandler.fill(fluidStack, true);
                            fluidHandler.drain(fluidStack, true);
                        }
                    }
                }
                break;
            default:
                for (IFluidTankProperties tank : tanks) {
                    FluidStack fluidStack = tank.getContents();
                    if (fluidStack != null) {
                        fluidStack = fluidHandler.drain(fluidStack, false);
                        if (fluidStack == null) continue;
                        if (fluidStack.amount >= this.fluidSupplyThroughput) {
                            fluidStack.amount = this.fluidSupplyThroughput;
                            fluidStack.amount = destFluidHandler.fill(fluidStack, true);
                            fluidHandler.drain(fluidStack, true);
                        }
                    }
                }
        }
    }

    private void transferFluidsExact(IFluidHandler fluidHandler, IFluidHandler destFluidHandler) {
        final IFluidTankProperties[] tanks = fluidHandler.getTankProperties();
        for (IFluidTankProperties tank : destFluidHandler.getTankProperties()) {
            final FluidStack stack = tank.getContents();
            if (stack == null) continue;
            this.fluidExact.computeIfAbsent(stack, k -> new Counter(0))
                    .increment(stack.amount);
        }
        switch (this.fluidFilterType) {
            case NORMAL:
                for (IFluidTankProperties tank : tanks) {
                    FluidStack fluidStack = tank.getContents();
                    final FluidStack filterStack;
                    if (fluidStack != null && this.isFluidBlacklist == ((filterStack = this.fluidType.get(fluidStack)) == null)) {
                        if (filterStack == null) continue;
                        fluidStack = fluidHandler.drain(fluidStack, false);
                        if (fluidStack == null) continue;
                        final Counter counter = this.fluidExact.computeIfAbsent(fluidStack, k -> new Counter(0));
                        fluidStack.amount = (int) Math.min(fluidStack.amount, filterStack.amount - counter.getValue());
                        if (fluidStack.amount > 0) {
                            fluidStack.amount = destFluidHandler.fill(fluidStack, true);
                            counter.increment(fluidStack.amount);
                            fluidHandler.drain(fluidStack, true);
                        }
                    }
                }
                break;
            case SMART:
                for (IFluidTankProperties iFluidTankProperties : tanks) {
                    FluidStack fluidStack = iFluidTankProperties.getContents();
                    if (fluidStack != null) {
                        GTFluidStackWrapper fluidStackWrapper = this.fluidRecipeMap.get(fluidStack);
                        if (fluidStackWrapper == null) {
                            this.fluidRecipeSearchSlot.getTankAt(0).drain(Integer.MAX_VALUE, true);
                            this.fluidRecipeSearchSlot.getTankAt(0).fill(fluidStack, true);
                            Recipe recipe = this.recipeLRUCache.get(TJValues.DUMMY_ITEM_HANDLER, this.fluidRecipeSearchSlot);
                            if (recipe == null) {
                                recipe = this.fluidRecipeMode.getRecipeMap().findRecipe(Integer.MAX_VALUE, TJValues.DUMMY_ITEM_HANDLER, this.fluidRecipeSearchSlot, Integer.MAX_VALUE);
                                this.recipeLRUCache.put(recipe);
                            }
                            if (recipe != null) {
                                fluidStackWrapper = new GTFluidStackWrapper(fluidStack, 0);
                                for (FluidStack stack : recipe.getFluidInputs()) {
                                    if (stack.isFluidEqual(fluidStack))
                                        fluidStackWrapper.increment(stack.amount);
                                }
                                this.fluidRecipeMap.put(fluidStack, fluidStackWrapper);
                            } else continue;
                        }
                        final Counter counter = this.fluidExact.computeIfAbsent(fluidStack, k -> new Counter(0));
                        fluidStack = fluidHandler.drain(fluidStack, false);
                        if (fluidStack == null) continue;
                        fluidStack.amount = (int) Math.min(fluidStack.amount, fluidStackWrapper.getCount() - counter.getValue());
                        if (fluidStack.amount > 0) {
                            fluidStack.amount = destFluidHandler.fill(fluidStack, true);
                            counter.increment(fluidStack.amount);
                            fluidHandler.drain(fluidStack, true);
                        }
                    }
                }
                break;
            default:
                for (IFluidTankProperties tank : tanks) {
                    FluidStack fluidStack = tank.getContents();
                    if (fluidStack != null) {
                        final Counter counter = this.fluidExact.computeIfAbsent(fluidStack, k -> new Counter(0));
                        fluidStack = fluidHandler.drain(fluidStack, false);
                        if (fluidStack == null) continue;
                        fluidStack.amount = (int) Math.min(fluidStack.amount, this.fluidSupplyThroughput - counter.getValue());
                        if (fluidStack.amount > 0) {
                            fluidStack.amount = destFluidHandler.fill(fluidStack, true);
                            counter.increment(fluidStack.amount);
                            fluidHandler.drain(fluidStack, true);
                        }
                    }
                }
        }
        this.fluidExact.clear();
    }

    public void setItemSupplyThroughput(String text, String id) {
        this.itemSupplyThroughput = (int) Math.max(1, Math.min(Integer.MAX_VALUE, Long.parseLong(text)));
        this.markAsDirty();
    }

    public void setFluidSupplyThroughput(String text, String id) {
        this.fluidSupplyThroughput = (int) Math.max(1, Math.min(Integer.MAX_VALUE, Long.parseLong(text)));
        this.markAsDirty();
    }

    public void setRobotArmMode(TransferMode robotArmMode) {
        this.robotArmMode = robotArmMode;
        this.markAsDirty();
    }

    public void setRegulatorMode(TransferMode regulatorMode) {
        this.regulatorMode = regulatorMode;
        this.markAsDirty();
    }

    public void setItemRecipeMode(RecipeMode itemRecipeMode) {
        this.itemRecipeMode = itemRecipeMode;
        this.recipeLRUCache.clear();
        this.itemRecipeMap.clear();
        this.fluidRecipeMap.clear();
        this.markAsDirty();
    }

    public void setFluidRecipeMode(RecipeMode fluidRecipeMode) {
        this.fluidRecipeMode = fluidRecipeMode;
        this.recipeLRUCache.clear();
        this.itemRecipeMap.clear();
        this.fluidRecipeMap.clear();
        this.markAsDirty();
    }

    private void setItemCount(String text, String id) {
        final int index = Integer.parseInt(id);
        if (index < 0 || index >= this.itemFilter.getSlots()) return;
        final ItemStack stack = this.itemFilter.getStackInSlot(index);
        if (stack.isEmpty()) return;
        stack.setCount((int) Math.min(Integer.MAX_VALUE, Long.parseLong(text)));
        this.itemType.put(stack, stack);
        this.markAsDirty();
    }

    private String getItemCount(int index) {
        return String.valueOf(this.itemFilter.getStackInSlot(index).getCount());
    }

    private void setFluidCount(String text, String id) {
        final int index = Integer.parseInt(id);
        if (index < 0 || index >= this.fluidFilter.getTanks()) return;
        final FluidStack fluidStack = this.fluidFilter.getTankAt(index).getFluid();
        if (fluidStack == null) return;
        fluidStack.amount = (int) Math.min(Integer.MAX_VALUE, Long.parseLong(text));
        this.fluidType.put(fluidStack, fluidStack);
        this.markAsDirty();
    }

    private String getFluidCount(int index) {
        return String.valueOf(this.fluidFilter.getTankAt(index).getFluidAmount());
    }

    public enum RecipeMode implements IStringSerializable {
        ELECTROLYZER(RecipeMaps.ELECTROLYZER_RECIPES),
        SIFTER(RecipeMaps.SIFTER_RECIPES),
        CENTRIFUGE(RecipeMaps.CENTRIFUGE_RECIPES);

        private final RecipeMap<?> recipeMap;

        RecipeMode(RecipeMap<?> recipeMap) {
            this.recipeMap = recipeMap;
        }

        public RecipeMap<?> getRecipeMap() {
            return this.recipeMap;
        }

        @Nonnull
        @Override
        public String getName() {
            return "recipemap." + this.recipeMap.getUnlocalizedName() + ".name";
        }
    }
}
