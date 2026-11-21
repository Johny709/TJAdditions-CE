package tj.items.behaviours;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.*;
import gregtech.api.items.gui.ItemUIFactory;
import gregtech.api.items.gui.PlayerInventoryHolder;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.util.function.BooleanConsumer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import tj.integration.ae2.ITickSetter;
import tj.mui.TJGuiTextures;
import tj.mui.TJGuiUtils;
import tj.mui.widgets.ButtonWidget;
import tj.mui.widgets.PopUpWidget;
import tj.mui.widgets.TJModularUI;
import tj.mui.widgets.impl.NewTextFieldWidget;
import tj.mui.widgets.impl.PanelWidgetGroup;
import tj.mui.widgets.impl.TJLabelWidget;
import tj.items.TJMetaItems;
import tj.mui.widgets.impl.TJToggleButtonWidget;
import tj.util.TJItemUtils;
import tj.util.references.BooleanReference;
import tj.util.references.LongReference;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

import static tj.mui.TJGuiTextures.*;

public class CreativeEnergyCoverBehaviour implements IItemBehaviour, ItemUIFactory {

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        if (!world.isRemote)
            PlayerInventoryHolder.openHandItemUI(player, hand);
        return ActionResult.newResult(EnumActionResult.SUCCESS, player.getHeldItemMainhand());
    }

    @Override
    public ModularUI createUI(PlayerInventoryHolder holder, EntityPlayer player) {
        final ItemStack stack = player.getHeldItem(EnumHand.MAIN_HAND);
        final NBTTagCompound compound = stack.getOrCreateSubCompound("init");
        final ITickSetter tickSetter = new ITickSetter() {
            @Override
            public void setTickTime(long tickTime) {
                compound.setInteger("ticks", (int) Math.max(1, Math.min(Integer.MAX_VALUE, tickTime)));
            }

            @Override
            public int getTickTime() {
                return compound.getInteger("ticks");
            }
        };
        final BooleanReference simulateVoltage = new BooleanReference();
        final BooleanReference draining = new BooleanReference();
        final LongReference energyRate = new LongReference(Long.MAX_VALUE);
        final LongReference voltage = new LongReference(32);
        final LongReference amps = new LongReference(1);
        final BooleanConsumer setSimulateVoltage = simulate -> {
            simulateVoltage.setValue(simulate);
            compound.setBoolean("simulateVoltage", simulateVoltage.isValue());
        };
        final BooleanConsumer setDraining = isDraining -> {
            draining.setValue(isDraining);
            compound.setBoolean("draining", draining.isValue());
        };
        final BiConsumer<String, String> setEnergyRate = (text, id) -> {
            energyRate.setValue((long) Math.max(0, Math.min(Long.MAX_VALUE, Double.parseDouble(text))));
            compound.setLong("energyRate", energyRate.getValue());
        };
        final BiConsumer<String, String> setVoltage = (text, id) -> {
            voltage.setValue(Math.max(0, Math.min(2147483648L, Long.parseLong(text))));
            compound.setLong("voltage", voltage.getValue());
        };
        final BiConsumer<String, String> setAmps = (text, id) -> {
            amps.setValue(Math.max(0, Math.min(4294967295L, Long.parseLong(text))));
            compound.setLong("amps", amps.getValue());
        };
        final PanelWidgetGroup panelWidgetGroup = TJGuiUtils.createTickPanel(tickSetter);
        return TJModularUI.builder(holder, player)
                .setWidthHeight(200, 187)
                .widget(new TJLabelWidget(7, -18, 162, 18, TJGuiTextures.MACHINE_LABEL_2)
                        .setItemLabel(TJMetaItems.CREATIVE_ENERGY_COVER.getStackForm()).setLocale("metaitem.creative.energy.cover.name"))
                .widget(new TJToggleButtonWidget(26, 27, 124, 18, GuiTextures.VANILLA_BUTTON, simulateVoltage::isValue, setSimulateVoltage))
                .widget(new TJToggleButtonWidget(151, 27, 18, 18, GuiTextures.VANILLA_BUTTON, draining::isValue, setDraining)
                        .setToggleTitleTooltipHoverText("machine.universal.mode.transfer.in", "machine.universal.mode.transfer.out"))
                .widget(new PopUpWidget<>()
                        .setClickToDefault(false)
                        .setIndexSupplier(() -> simulateVoltage.isValue() ? 1 : 0)
                        .addPopup(widgetGroup -> {
                            widgetGroup.addWidget(new NewTextFieldWidget<>(26, 45, 124, 18, true, energyRate::toString, setEnergyRate)
                                    .setValidator(str -> Pattern.compile("\\*?[0-9_]*\\*?").matcher(str).matches())
                                    .setBackgroundText("metaitem.creative_energy_cover.set.energy_rate")
                                    .setTooltipText("metaitem.creative_energy_cover.set.energy_rate")
                                    .setUpdateOnTyping(true));
                            widgetGroup.addWidget(new ButtonWidget<>(7, 45, 18, 18, "-", minus -> setEnergyRate.accept(String.valueOf((double) energyRate.getValue() / 2), "")).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
                            widgetGroup.addWidget(new ButtonWidget<>(151, 45, 18, 18, "+", plus -> setEnergyRate.accept(String.valueOf((double) energyRate.getValue() * 2), "")).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
                            widgetGroup.addWidget(new TJToggleButtonWidget(151, 63, 18, 18, TOGGLE_RESET_BUTTON, () -> false, reset -> setEnergyRate.accept(String.valueOf(Long.MAX_VALUE), ""))
                                    .setTitleHoverTooltipText("machine.universal.toggle.reset.disabled"));
                            widgetGroup.addWidget(new LabelWidget(32, 32, "metaitem.creative_energy_cover.simulate_voltage", false));
                            return false;
                        }).addPopup(widgetGroup -> false))
                .widget(new PopUpWidget<>()
                        .setClickToDefault(false)
                        .setIndexSupplier(() -> simulateVoltage.isValue() ? 0 : 1)
                        .addPopup(widgetGroup -> {
                            widgetGroup.addWidget(new NewTextFieldWidget<>(26, 45, 124, 18, true, voltage::toString, setVoltage)
                                    .setValidator(str -> Pattern.compile("\\*?[0-9_]*\\*?").matcher(str).matches())
                                    .setBackgroundText("metaitem.creative_energy_cover.set.voltage")
                                    .setTooltipText("metaitem.creative_energy_cover.set.voltage")
                                    .setUpdateOnTyping(true));
                            widgetGroup.addWidget(new NewTextFieldWidget<>(26, 63, 124, 18, true, amps::toString, setAmps)
                                    .setValidator(str -> Pattern.compile("\\*?[0-9_]*\\*?").matcher(str).matches())
                                    .setBackgroundText("metaitem.creative_energy_cover.set.amps")
                                    .setTooltipText("metaitem.creative_energy_cover.set.amps")
                                    .setUpdateOnTyping(true));
                            widgetGroup.addWidget(new ButtonWidget<>(7, 45, 18, 18, "-", minus -> setVoltage.accept(String.valueOf(voltage.getValue() / 2), "")).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
                            widgetGroup.addWidget(new ButtonWidget<>(151, 45, 18, 18, "+", plus -> setVoltage.accept(String.valueOf(voltage.getValue() * 2), "")).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
                            widgetGroup.addWidget(new ButtonWidget<>(7, 63, 18, 18, "-", minus -> setAmps.accept(String.valueOf(amps.getValue() / 2), "")).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
                            widgetGroup.addWidget(new ButtonWidget<>(151, 63, 18, 18, "+", plus -> setAmps.accept(String.valueOf(amps.getValue() * 2), "")).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
                            widgetGroup.addWidget(new LabelWidget(32, 32, "metaitem.creative_energy_cover.simulate_voltage", true));
                            return false;
                        }).addPopup(widgetGroup -> false))
                .widget(new TJToggleButtonWidget(174, 105, 18, 18, TJGuiTextures.TOGGLE_RESET_BUTTON,
                        () -> false, b -> compound.setInteger("speed", 1))
                        .setTitleHoverTooltipText("machine.universal.toggle.reset.disabled"))
                .widget(new TJToggleButtonWidget(174, 163, 18, 18, TJGuiTextures.TOGGLE_POWER_BUTTON,
                        () -> compound.getBoolean("active"), b -> compound.setBoolean("active", !compound.getBoolean("active")))
                        .setToggleTitleTooltipHoverText("machine.universal.toggle.run.mode.disabled",
                                "machine.universal.toggle.run.mode.enabled"))
                .widget(new NewTextFieldWidget<>(7, 82, 162, 18, true, () -> String.valueOf(tickSetter.getTickTime()),
                        (text, id) -> tickSetter.setTickTime(Long.parseLong(text)))
                        .setValidator(str -> Pattern.compile("\\*?[0-9_]*\\*?").matcher(str).matches())
                        .setTooltipText("machine.universal.ticks.operation")
                        .setUpdateOnTyping(true)
                        .setMaxStringLength(11))
                .widget(new TJToggleButtonWidget(174, 82, 18, 18, panelWidgetGroup::isPanelOpen, (BooleanConsumer) value -> {
                    if (!value && panelWidgetGroup.isPanelOpen()) {
                        panelWidgetGroup.closePanel();
                    } else if (value) {
                        panelWidgetGroup.openPanel();
                    }
                }).setToggleTexture(GuiTextures.TOGGLE_BUTTON_BACK)
                        .setItemDisplay(TJItemUtils.getItemStackFromName("enderio:item_material", 1, 11)) // infinity bimetal gear
                        .setHoverTooltipText("tj.machine.universal.ticks.operation_configure")
                        .useToggleTexture(true))
                .widget(panelWidgetGroup)
                .bindPlayerInventory(stack)
                .bindOpenListener(() -> {
                    if (compound.hasKey("simulateVoltage"))
                        simulateVoltage.setValue(compound.getBoolean("simulateVoltage"));
                    if (compound.hasKey("draining"))
                        draining.setValue(compound.getBoolean("draining"));
                    if (compound.hasKey("energyRate"))
                        energyRate.setValue(compound.getLong("energyRate"));
                    if (compound.hasKey("voltage"))
                        voltage.setValue(compound.getLong("voltage"));
                    if (compound.hasKey("amps"))
                        amps.setValue(compound.getLong("amps"));
                }).bindCloseListener(() -> stack.getOrCreateSubCompound("init").merge(compound))
                .build();
    }

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
        lines.add(I18n.format("cover.creative.only"));
        lines.add(I18n.format("metaitem.creative.energy.cover.description"));
        lines.add(I18n.format("cover.creative.description"));
    }
}
