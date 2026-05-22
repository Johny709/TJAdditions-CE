package tj.items.behaviours;

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
import tj.gui.TJGuiTextures;
import tj.gui.TJGuiUtils;
import tj.gui.widgets.*;
import tj.items.TJMetaItems;
import tj.util.references.BooleanReference;
import tj.util.references.IntegerReference;
import tj.util.references.LongReference;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

import static gregtech.api.gui.GuiTextures.BORDERED_BACKGROUND;
import static tj.gui.TJGuiTextures.*;
import static tj.gui.TJGuiTextures.MINUS_BUTTON;

public class CreativeEnergyCoverBehaviour implements IItemBehaviour, ItemUIFactory {

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        if (!world.isRemote && hand == EnumHand.MAIN_HAND) {
            final PlayerInventoryHolder holder = new PlayerInventoryHolder(player, hand);
            holder.openUI();
            return ActionResult.newResult(EnumActionResult.SUCCESS, player.getHeldItem(hand));
        }
        return IItemBehaviour.super.onItemRightClick(world, player, hand);
    }

    @Override
    public ModularUI createUI(PlayerInventoryHolder holder, EntityPlayer player) {
        final ItemStack stack = player.getHeldItem(EnumHand.MAIN_HAND);
        final NBTTagCompound compound = stack.getOrCreateSubCompound("init");
        final BooleanReference simulateVoltage = new BooleanReference();
        final BooleanReference draining = new BooleanReference();
        final BooleanReference active = new BooleanReference(true);
        final LongReference energyRate = new LongReference(Long.MAX_VALUE);
        final LongReference voltage = new LongReference(32);
        final LongReference amps = new LongReference(1);
        final IntegerReference ticks = new IntegerReference(1);
        final BooleanConsumer setSimulateVoltage = simulate -> {
            simulateVoltage.setValue(simulate);
            compound.setBoolean("simulateVoltage", simulateVoltage.isValue());
        };
        final BooleanConsumer setDraining = isDraining -> {
            draining.setValue(isDraining);
            compound.setBoolean("draining", draining.isValue());
        };
        final BooleanConsumer setActive = isActive -> {
            active.setValue(isActive);
            compound.setBoolean("active", active.isValue());
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
        final BiConsumer<String, String> setTicks = (text, id) -> {
            ticks.setValue((int) Math.max(1, Math.min(Integer.MAX_VALUE, Long.parseLong(text))));
            compound.setInteger("ticks", ticks.getValue());
        };
        return ModularUI.builder(BORDERED_BACKGROUND, 176, 170)
                .widget(new TJLabelWidget(7, -18, 162, 18, TJGuiTextures.MACHINE_LABEL_2)
                        .setItemLabel(TJMetaItems.CREATIVE_ENERGY_COVER.getStackForm()).setLocale("metaitem.creative.energy.cover.name"))
                .widget(new NewTextFieldWidget<>(26, 7, 124, 18, true, ticks::toString, setTicks)
                        .setValidator(str -> Pattern.compile("\\*?[0-9_]*\\*?").matcher(str).matches())
                        .setTooltipText("machine.universal.ticks.operation")
                        .setUpdateOnTyping(true))
                .widget(new ToggleButtonWidget(7, 7, 18, 18, MINUS_BUTTON, () -> false, minus -> setTicks.accept(String.valueOf((long) ticks.getValue() - 1), "")))
                .widget(new ToggleButtonWidget(151, 7, 18, 18, PLUS_BUTTON, () -> false, plus -> setTicks.accept(String.valueOf((long) ticks.getValue() + 1), "")))
                .widget(new ToggleButtonWidget(7, 27, 18, 18, POWER_BUTTON, active::isValue, setActive)
                        .setTooltipText("machine.universal.toggle.run.mode"))
                .widget(new ToggleButtonWidget(26, 27, 124, 18, simulateVoltage::isValue, setSimulateVoltage))
                .widget(new CycleButtonWidget(151, 27, 18, 18, draining::isValue, setDraining, "machine.universal.mode.transfer.in", "machine.universal.mode.transfer.out"))
                .widget(new PopUpWidget<>()
                        .setClickToDefault(false)
                        .setIndexSupplier(() -> simulateVoltage.isValue() ? 1 : 0)
                        .addPopup(widgetGroup -> {
                            widgetGroup.addWidget(new NewTextFieldWidget<>(26, 45, 124, 18, true, energyRate::toString, setEnergyRate)
                                    .setValidator(str -> Pattern.compile("\\*?[0-9_]*\\*?").matcher(str).matches())
                                    .setBackgroundText("metaitem.creative_energy_cover.set.energy_rate")
                                    .setTooltipText("metaitem.creative_energy_cover.set.energy_rate")
                                    .setUpdateOnTyping(true));
                            widgetGroup.addWidget(new ToggleButtonWidget(7, 45, 18, 18, MINUS_BUTTON, () -> false, minus -> setEnergyRate.accept(String.valueOf((double) energyRate.getValue() / 2), "")));
                            widgetGroup.addWidget(new ToggleButtonWidget(151, 45, 18, 18, PLUS_BUTTON, () -> false, plus -> setEnergyRate.accept(String.valueOf((double) energyRate.getValue() * 2), "")));
                            widgetGroup.addWidget(new ToggleButtonWidget(151, 63, 18, 18, RESET_BUTTON, () -> false, reset -> setEnergyRate.accept(String.valueOf(Long.MAX_VALUE), ""))
                                    .setTooltipText("machine.universal.toggle.reset"));
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
                            widgetGroup.addWidget(new ToggleButtonWidget(7, 45, 18, 18, MINUS_BUTTON, () -> false, minus -> setVoltage.accept(String.valueOf(voltage.getValue() / 2), "")));
                            widgetGroup.addWidget(new ToggleButtonWidget(151, 45, 18, 18, PLUS_BUTTON, () -> false, plus -> setVoltage.accept(String.valueOf(voltage.getValue() * 2), "")));
                            widgetGroup.addWidget(new ToggleButtonWidget(7, 63, 18, 18, MINUS_BUTTON, () -> false, minus -> setAmps.accept(String.valueOf(amps.getValue() / 2), "")));
                            widgetGroup.addWidget(new ToggleButtonWidget(151, 63, 18, 18, PLUS_BUTTON, () -> false, plus -> setAmps.accept(String.valueOf(amps.getValue() * 2), "")));
                            widgetGroup.addWidget(new LabelWidget(32, 32, "metaitem.creative_energy_cover.simulate_voltage", true));
                            return false;
                        }).addPopup(widgetGroup -> false))
                .bindOpenListener(() -> {
                    if (compound.hasKey("simulateVoltage"))
                        simulateVoltage.setValue(compound.getBoolean("simulateVoltage"));
                    if (compound.hasKey("draining"))
                        draining.setValue(compound.getBoolean("draining"));
                    if (compound.hasKey("active"))
                        active.setValue(compound.getBoolean("active"));
                    if (compound.hasKey("energyRate"))
                        energyRate.setValue(compound.getLong("energyRate"));
                    if (compound.hasKey("voltage"))
                        voltage.setValue(compound.getLong("voltage"));
                    if (compound.hasKey("amps"))
                        amps.setValue(compound.getLong("amps"));
                    if (compound.hasKey("ticks"))
                        ticks.setValue(compound.getInteger("ticks"));
                }).bindCloseListener(() -> stack.getOrCreateSubCompound("init").merge(compound))
                .widget(TJGuiUtils.bindPlayerInventory(new WidgetGroup(), player.inventory, 7, 90, stack))
                .build(holder, player);
    }

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
        lines.add(I18n.format("cover.creative.only"));
        lines.add(I18n.format("metaitem.creative.energy.cover.description"));
        lines.add(I18n.format("cover.creative.description"));
    }
}
