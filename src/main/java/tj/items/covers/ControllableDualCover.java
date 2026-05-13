package tj.items.covers;

import gregicadditions.GAValues;
import gregtech.api.cover.ICoverable;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.gui.widgets.CycleButtonWidget;
import gregtech.api.gui.widgets.ToggleButtonWidget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.gui.widgets.tab.VerticalTabListRenderer;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.util.Position;
import gregtech.common.covers.CoverConveyor;
import gregtech.common.covers.CoverPump;
import gregtech.common.covers.TransferMode;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import tj.builder.WidgetTabBuilder;
import tj.gui.TJGuiTextures;
import tj.gui.widgets.NewTextFieldWidget;
import tj.gui.widgets.TJLabelWidget;
import tj.gui.widgets.impl.TJPhantomFluidSlotWidget;
import tj.gui.widgets.impl.TJPhantomItemSlotWidget;

import java.util.regex.Pattern;

import static gregicadditions.item.GAMetaItems.*;
import static gregtech.api.gui.widgets.tab.VerticalTabListRenderer.HorizontalLocation.LEFT;
import static gregtech.api.gui.widgets.tab.VerticalTabListRenderer.VerticalStartCorner.TOP;
import static gregtech.common.items.MetaItems.*;
import static tj.items.TJMetaItems.*;

public class ControllableDualCover extends DualCover {

    private TransferMode robotArmMode = TransferMode.TRANSFER_ANY;
    private TransferMode regulatorMode = TransferMode.TRANSFER_ANY;

    public ControllableDualCover(ICoverable coverHolder, EnumFacing attachedSide, int tier) {
        super(coverHolder, attachedSide, tier);
    }

    @Override
    public ModularUI createUI(EntityPlayer player) {
        final MetaItem<?>.MetaValueItem[] robotArms = {null, ROBOT_ARM_LV, ROBOT_ARM_MV, ROBOT_ARM_HV, ROBOT_ARM_EV, ROBOT_ARM_IV, ROBOT_ARM_LUV, ROBOT_ARM_ZPM, ROBOT_ARM_UV, ROBOT_ARM_UHV, ROBOT_ARM_UEV, ROBOT_ARM_UIV, ROBOT_ARM_UMV, ROBOT_ARM_UXV, ROBOT_ARM_MAX};
        final MetaItem<?>.MetaValueItem[] regulators = {null, FLUID_REGULATOR_LV, FLUID_REGULATOR_MV, FLUID_REGULATOR_HV, FLUID_REGULATOR_EV, FLUID_REGULATOR_IV, FLUID_REGULATOR_LUV, FLUID_REGULATOR_ZPM, FLUID_REGULATOR_UV, FLUID_REGULATOR_UHV, null, null, FLUID_REGULATOR_UMV, null, FLUID_REGULATOR_MAX};
        final WidgetGroup itemWidgetGroup = new WidgetGroup(new Position(10, 95));
        final WidgetGroup fluidWidgetGroup = new WidgetGroup(new Position(10, 115));
        for (int i = 0; i < this.itemFilter.getSlots(); i++) {
            itemWidgetGroup.addWidget(new TJPhantomItemSlotWidget(18 * (i % 4), 18 * (i / 4), 18, 18, i, this.itemFilter, item -> {
                if (!item.isEmpty())
                    this.itemType.add(item.getItem());
            }, item -> this.itemType.remove(item.getItem())).setBackgroundTextures(GuiTextures.SLOT)
                    .setPutItemsPredicate(item -> !this.itemType.contains(item.getItem())));
        }
        for (int i = 0; i < this.fluidFilter.getTanks(); i++) {
            fluidWidgetGroup.addWidget(new TJPhantomFluidSlotWidget(18 * (i % 4), 18 * (i / 4), 18, 18, i, this.fluidFilter, fluid -> {
                if (fluid != null)
                    this.fluidType.add(fluid);
            }, this.fluidType::remove).setBackgroundTexture(GuiTextures.FLUID_SLOT)
                    .setPutFluidsPredicate(fluid -> !this.fluidType.contains(fluid)));
        }
        final WidgetTabBuilder tabBuilder = new WidgetTabBuilder()
                .setTabListRenderer(() -> new VerticalTabListRenderer(TOP, LEFT))
                .addTab(String.format("metaitem.robot.arm.%s.name", GAValues.VN[tier].toLowerCase()), this.tier > 0 ? robotArms[this.tier].getStackForm() : this.getPickItem(), tab -> {
                    tab.add(new ClickButtonWidget(10, 20, 20, 20, "-10", data -> this.setItemTransferRate(this.itemTransferRate - (data.isShiftClick ? 100 : 10))));
                    tab.add(new ClickButtonWidget(146, 20, 20, 20, "+10", data -> this.setItemTransferRate(this.itemTransferRate + (data.isShiftClick ? 100 : 10))));
                    tab.add(new ClickButtonWidget(30, 20, 20, 20, "-1", data -> this.setItemTransferRate(this.itemTransferRate - (data.isShiftClick ? 5 : 1))));
                    tab.add(new ClickButtonWidget(126, 20, 20, 20, "+1", data -> this.setItemTransferRate(this.itemTransferRate + (data.isShiftClick ? 5 : 1))));
                    tab.add(new ClickButtonWidget(50, 20, 38, 20,"/2", data -> this.setItemTransferRate(this.itemTransferRate / 2D)));
                    tab.add(new ClickButtonWidget(88, 20, 38, 20, "*2", data -> this.setItemTransferRate(this.itemTransferRate * 2)));
                    tab.add(new NewTextFieldWidget<>(10, 40, 156, 18, true, () -> String.valueOf(this.itemTransferRate), this::setItemTransferRate)
                            .setValidator(str -> Pattern.compile("-*?[0-9_]*\\*?").matcher(str).matches())
                            .setUpdateOnTyping(true));
                    tab.add(new CycleButtonWidget(10, 65, 75, 20, CoverConveyor.ConveyorMode.class, () -> this.conveyorMode, this::setConveyorMode));
                    tab.add(new CycleButtonWidget(91, 65, 75, 20, TransferMode.class, () -> this.robotArmMode, this::setRobotArmMode)
                            .setTooltipHoverString("cover.robotic_arm.transfer_mode.description"));
                    tab.add(itemWidgetGroup);
                    tab.add(new ToggleButtonWidget(151, 95, 18, 18, GuiTextures.BUTTON_BLACKLIST, () -> this.isItemBlacklist, this::setItemBlacklist)
                            .setTooltipText("cover.filter.blacklist"));
                    tab.add(new ToggleButtonWidget(151, 168, 18, 18, TJGuiTextures.POWER_BUTTON, () -> this.isConveyorWorking, this::setConveyorWorking)
                            .setTooltipText("machine.universal.toggle.run.mode"));
                }).addTab(String.format("metaitem.fluid.regulator.%s.name", GAValues.VN[tier].toLowerCase()), this.tier > 0 ? regulators[this.tier].getStackForm() : this.getPickItem(), tab -> {
                    tab.add(new ClickButtonWidget(10, 20, 34, 20, "-100", data -> this.setFluidTransferRate(this.fluidTransferRate - (data.isShiftClick ? 500 : 100))));
                    tab.add(new ClickButtonWidget(128, 20, 34, 20, "+100", data -> this.setFluidTransferRate(this.fluidTransferRate + (data.isShiftClick ? 500 : 100))));
                    tab.add(new ClickButtonWidget(44, 20, 22, 20, "-10", data -> this.setFluidTransferRate(this.fluidTransferRate - (data.isShiftClick ? 50 : 10))));
                    tab.add(new ClickButtonWidget(106, 20, 24, 20, "+10", data -> this.setFluidTransferRate(this.fluidTransferRate + (data.isShiftClick ? 50 : 10))));
                    tab.add(new ClickButtonWidget(66, 20, 20, 20, "-1", data -> this.setFluidTransferRate(this.fluidTransferRate - (data.isShiftClick ? 5 : 1))));
                    tab.add(new ClickButtonWidget(86, 20, 20, 20, "+1", data -> this.setFluidTransferRate(this.fluidTransferRate + (data.isShiftClick ? 5 : 1))));
                    tab.add(new ClickButtonWidget(10, 40, 76, 20, "/2", data -> this.setFluidTransferRate(this.fluidTransferRate / 2D)));
                    tab.add(new ClickButtonWidget(86, 40, 76, 20, "*2", data -> this.setFluidTransferRate(this.fluidTransferRate * 2)));
                    tab.add(new NewTextFieldWidget<>(10, 60, 156, 18, true, () -> String.valueOf(this.fluidTransferRate), this::setFluidTransferRate)
                            .setValidator(str -> Pattern.compile("-*?[0-9_]*\\*?").matcher(str).matches())
                            .setUpdateOnTyping(true));
                    tab.add(new CycleButtonWidget(10, 85, 75, 18, CoverPump.PumpMode.class, () -> this.pumpMode, this::setPumpMode));
                    tab.add(new CycleButtonWidget(88, 85, 75, 18, TransferMode.class, () -> this.regulatorMode, this::setRegulatorMode)
                            .setTooltipHoverString("cover.fluid_regulator.transfer_mode.description"));
                    tab.add(fluidWidgetGroup);
                    tab.add(new ToggleButtonWidget(151, 115, 18, 18, GuiTextures.BUTTON_BLACKLIST, () -> this.isFluidBlacklist, this::setFluidBlacklist)
                            .setTooltipText("cover.filter.blacklist"));
                    tab.add(new ToggleButtonWidget(151, 168, 18, 18, TJGuiTextures.POWER_BUTTON, () -> this.isPumpWorking, this::setPumpWorking)
                            .setTooltipText("machine.universal.toggle.run.mode"));
                });
        return ModularUI.builder(GuiTextures.BORDERED_BACKGROUND, 176, 272)
                .widget(new TJLabelWidget(7, -18, 162, 18, TJGuiTextures.MACHINE_LABEL_2)
                        .setItemLabel(this.getPickItem()).setLocale(String.format("metaitem.controllable_dual_cover.%s.name", GAValues.VN[this.tier].toLowerCase())))
                .widget(tabBuilder.build())
                .bindPlayerInventory(player.inventory, GuiTextures.SLOT, 7, 190)
                .build(this, player);
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("robotArmMode", this.robotArmMode.ordinal());
        tagCompound.setInteger("regulatorMode", this.regulatorMode.ordinal());
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        this.robotArmMode = TransferMode.values()[tagCompound.getInteger("robotArmMode")];
        this.regulatorMode = TransferMode.values()[tagCompound.getInteger("regulatorMode")];
    }

    public void setRobotArmMode(TransferMode robotArmMode) {
        this.robotArmMode = robotArmMode;
        this.markAsDirty();
    }

    public void setRegulatorMode(TransferMode regulatorMode) {
        this.regulatorMode = regulatorMode;
        this.markAsDirty();
    }
}
