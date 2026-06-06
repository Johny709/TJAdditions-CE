package tj.integration.ae2.tile;

import appeng.api.config.*;
import appeng.helpers.DualityInterface;
import appeng.tile.misc.TileInterface;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.LabelWidget;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import tj.blocks.block.TJBlocks;
import tj.integration.ae2.helpers.DualitySuperInterface;
import tj.mui.TJGuiTextures;
import tj.mui.uifactory.ITileEntityUI;
import tj.mui.uifactory.TileEntityHolder;
import tj.mui.widgets.ButtonWidget;
import tj.mui.widgets.impl.*;

import java.util.EnumSet;
import java.util.regex.Pattern;

public class TilePatternInterface extends TileInterface implements ITileEntityUI {

    public TilePatternInterface() {
        ObfuscationReflectionHelper.setPrivateValue(TileInterface.class, this, new DualitySuperInterface(this.getProxy(), this, 40, 36, 288), "duality");
    }

    public void openUI(EntityPlayer player, TileEntity tileEntity) {
        this.openUI(player, tileEntity, null);
    }

    public void openUI(EntityPlayer player, TileEntity tileEntity, EnumFacing facing) {
        TileEntityHolder holder = new TileEntityHolder(tileEntity);
        holder.setFacing(facing);
        holder.openUI((EntityPlayerMP) player);
    }

    @Override
    public ItemStack getItemStackRepresentation() {
        return TJBlocks.PATTERN_INTERFACE.maybeStack(1).orElse(ItemStack.EMPTY);
    }

    @Override
    public ModularUI createUI(TileEntityHolder holder, EntityPlayer player) {
        final DualityInterface duality = this.getInterfaceDuality();
        final SlotScrollableWidgetGroup scrollableWidgetGroup = new SlotScrollableWidgetGroup(7, 123, 166, 72, 9)
                .setScrollWidth(4);
        final SlotScrollableWidgetGroup scrollableWidgetGroup1 = new SlotScrollableWidgetGroup(186, 7, 40, 180, 2)
                .setScrollWidth(4);
        final ButtonPopUpWidget<?> buttonPopUpWidget = new ButtonPopUpWidget<>();
        final DualitySuperInterface.DualityUpgradeInventory upgradeHandler = (DualitySuperInterface.DualityUpgradeInventory) duality.getInventoryByName("upgrades");
        for (int i = 0; i < upgradeHandler.getSlots(); i++) {
            scrollableWidgetGroup1.addWidget(new TJSlotWidget<>(upgradeHandler, i, 18 * (i % 2), 18 * (i / 2))
                    .setActiveBackgroundTexture(GuiTextures.SLOT, TJGuiTextures.UPGRADE_OVERLAY));
        }
        for (int i = 0; i < duality.getPatterns().getSlots(); i++) {
            final int index = i;
            scrollableWidgetGroup.addWidget(new TJSlotWidget<>(duality.getPatterns(), i, 18 * (i % 9), 18 * (i / 9))
                    .setActiveSupplier(() -> index / 9 <= upgradeHandler.getInstalledUpgrades(Upgrades.PATTERN_EXPANSION) && buttonPopUpWidget.getIndex() == 0)
                    .setActiveBackgroundTexture(GuiTextures.SLOT, TJGuiTextures.PATTERN_OVERLAY)
                    .setInactiveBackgroundTexture(TJGuiTextures.BLANK_SLOT)
                    .setActiveInit(false));
        }
        final ModularUI.Builder builder = ModularUI.builder(TJGuiTextures.PATTERN_INTERFACE, 229, 292)
                .widget(new LabelWidget(7, 23, "gui.appliedenergistics2.StoredItems"))
                .widget(new LabelWidget(7, 113, "gui.appliedenergistics2.Patterns"))
                .widget(scrollableWidgetGroup)
                .widget(scrollableWidgetGroup1);
        for (int i = 0; i < duality.getStorage().getSlots(); i++) {
            builder.widget(new TJSlotWidget<>(duality.getStorage(), i, 7 + (18 * (i % 9)), 34 + (18 * (i / 9)))
                    .setActiveBackgroundTexture(GuiTextures.SLOT));
        }
        return builder.widget(new TJLabelWidget(7, -18, 162, 18, TJGuiTextures.MACHINE_LABEL_2)
                        .setItemLabel(this.getItemStackRepresentation()).setLocale(this.getItemStackRepresentation().getDisplayName()))
                .widget(new TJLabelWidget(4, 0, 162, 18, null)
                        .setDynamicLocale(this::getCustomInventoryName)
                        .setCentered(false)
                        .setCanSlide(false))
                .widget(new TJToggleButtonWidget(-18, 35, 16, 16, () -> duality.getConfigManager().getSetting(Settings.BLOCK).ordinal() == 0, this::setBlockingMode)
                        .setToggleTooltipHoverText("gui.tooltips.appliedenergistics2.NonBlocking", "gui.tooltips.appliedenergistics2.Blocking")
                        .setToggleTexture(TJGuiTextures.TOGGLE_BLOCKING_MODE)
                        .useToggleTexture(true))
                .widget(new TJToggleButtonWidget(-18, 53, 16, 16, () -> duality.getConfigManager().getSetting(Settings.INTERFACE_TERMINAL).ordinal() == 0, this::setInterfaceTerminal)
                        .setTooltipText("gui.appliedenergistics2.InterfaceTerminalHint")
                        .setToggleTexture(TJGuiTextures.TOGGLE_INTERFACE_TERMINAL)
                        .useToggleTexture(true))
                .widget(new TJToggleButtonWidget(-18, 71, 16, 16, () -> duality.getConfigManager().getSetting(Settings.OPERATION_MODE).ordinal() == 0, this::setFluidPacket)
                        .setToggleTooltipHoverText("ae2fc.tooltip.real_fluid.hint", "ae2fc.tooltip.fake_packet.hint")
                        .setToggleTexture(TJGuiTextures.TOGGLE_SEND_FLUID)
                        .useToggleTexture(true))
                .widget(new TJToggleButtonWidget(-18, 89, 16, 16, () -> duality.getConfigManager().getSetting(Settings.LEVEL_TYPE).ordinal() == 0, this::setSplittingItemsFluids)
                        .setToggleTooltipHoverText("ae2fc.tooltip.allow_splitting.hint", "ae2fc.tooltip.prevent_splitting.hint")
                        .setToggleTexture(TJGuiTextures.TOGGLE_SPLITTING_ITEMS_FLUIDS)
                        .useToggleTexture(true))
                .widget(new TJCycleButtonWidget<>(-18, 107, 16, 16, (EnumSet<CondenserOutput>) Settings.CONDENSER_OUTPUT.getPossibleValues(), () -> (Enum<CondenserOutput>) duality.getConfigManager().getSetting(Settings.CONDENSER_OUTPUT), this::setBlockModeEx)
                        .setCycleHoverTooltipText("ae2fc.tooltip.block_all.hint", "ae2fc.tooltip.block_item.hint", "ae2fc.tooltip.block_fluid.hint")
                        .setCycleTexture(TJGuiTextures.CYCLE_BLOCKING_MODE_EX))
                .widget(buttonPopUpWidget.addPopup(widgetGroup -> true)
                        .addPopup(new ButtonWidget<>(154, 0, 22, 22)
                                .setBackgroundTextures(TJGuiTextures.INTERFACE_SETTINGS)
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
                .bindPlayerInventory(player.inventory, 209)
                .build(holder, player);
    }

    private void setBlockingMode(boolean blockingMode) {
        this.getInterfaceDuality().getConfigManager().putSetting(Settings.BLOCK, blockingMode ? YesNo.YES : YesNo.NO);
        this.markDirty();
    }

    private void setInterfaceTerminal(boolean interfaceTerminal) {
        this.getInterfaceDuality().getConfigManager().putSetting(Settings.INTERFACE_TERMINAL, interfaceTerminal ? YesNo.YES : YesNo.NO);
        this.markDirty();
    }

    private void setFluidPacket(boolean fluidPacket) {
        this.getInterfaceDuality().getConfigManager().putSetting(Settings.OPERATION_MODE, fluidPacket ? OperationMode.FILL : OperationMode.EMPTY);
        ObfuscationReflectionHelper.setPrivateValue(DualityInterface.class, this.getInterfaceDuality(), fluidPacket, "fluidPacket");
        this.markDirty();
    }

    private void setSplittingItemsFluids(boolean splittingItemsFluids) {
        this.getInterfaceDuality().getConfigManager().putSetting(Settings.LEVEL_TYPE, splittingItemsFluids ? LevelType.ITEM_LEVEL : LevelType.ENERGY_LEVEL);
        ObfuscationReflectionHelper.setPrivateValue(DualityInterface.class, this.getInterfaceDuality(), splittingItemsFluids, "allowSplitting");
        this.markDirty();
    }

    private void setBlockModeEx(CondenserOutput blockModeEx) {
        this.getInterfaceDuality().getConfigManager().putSetting(Settings.CONDENSER_OUTPUT, blockModeEx);
        ObfuscationReflectionHelper.setPrivateValue(DualityInterface.class, this.getInterfaceDuality(), blockModeEx.ordinal(), "blockModeEx");
        this.markDirty();
    }

    private void setPriority(String text, String id) {
        this.getInterfaceDuality().setPriority((int) Math.max(Integer.MIN_VALUE, Math.min(Integer.MAX_VALUE, Long.parseLong(text))));
        this.markDirty();
    }
}
