package tj.items.behaviours;

import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.*;
import gregtech.api.items.gui.ItemUIFactory;
import gregtech.api.items.gui.PlayerInventoryHolder;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import tj.gui.TJGuiUtils;
import tj.gui.widgets.PopUpWidgetGroup;
import tj.gui.widgets.TJTextFieldWidget;

import java.util.List;
import java.util.regex.Pattern;

import static gregtech.api.gui.GuiTextures.BORDERED_BACKGROUND;
import static gregtech.api.gui.GuiTextures.DISPLAY;
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
        if (!compound.hasKey("simulateVoltage"))
            compound.setBoolean("simulateVoltage", false);
        if (!compound.hasKey("draining"))
            compound.setBoolean("draining", false);
        if (!compound.hasKey("active"))
            compound.setBoolean("active", true);
        if (!compound.hasKey("energyRate"))
            compound.setLong("energyRate", Long.MAX_VALUE);
        if (!compound.hasKey("voltage"))
            compound.setLong("voltage", 0);
        if (!compound.hasKey("amps"))
            compound.setLong("amps", 0);
        return ModularUI.builder(BORDERED_BACKGROUND, 176, 170)
                .label(30, 4, "metaitem.creative.energy.cover.name")
                .widget(new ImageWidget(26, 40, 124, 18, DISPLAY))
                .widget(new ToggleButtonWidget(7, 22, 18, 18, POWER_BUTTON, () -> compound.getBoolean("active"), b -> compound.setBoolean("active", !compound.getBoolean("active")))
                        .setTooltipText("machine.universal.toggle.run.mode"))
                .widget(new ToggleButtonWidget(26, 22, 124, 18, () -> compound.getBoolean("simulateVoltage"), b -> compound.setBoolean("simulateVoltage", !compound.getBoolean("simulateVoltage"))))
                .widget(new CycleButtonWidget(151, 22, 18, 18, () -> compound.getBoolean("draining"), b -> compound.setBoolean("draining", !compound.getBoolean("draining")), "machine.universal.mode.transfer.in", "machine.universal.mode.transfer.out"))
                .widget(new PopUpWidgetGroup(7, 40, 162, 40)
                        .addWidgets(new TJTextFieldWidget(24, 5, 119, 12, false, () -> String.valueOf(compound.getLong("energyRate")), s -> compound.setLong("energyRate", Long.parseLong(s)))
                                .setTooltipText("metaitem.creative_energy_cover.set.energy_rate")
                                .setValidator(str -> Pattern.compile("\\*?[0-9_]*\\*?").matcher(str).matches()))
                        .addWidgets(new ToggleButtonWidget(0, 0, 18, 18, PLUS_BUTTON, () -> false, (plus) -> compound.setLong("energyRate", Math.max(1, compound.getLong("energyRate") * 2))))
                        .addWidgets(new ToggleButtonWidget(144, 0, 18, 18, MINUS_BUTTON, () -> false, (minus) -> compound.setLong("energyRate", Math.max(1, compound.getLong("energyRate") / 2))))
                        .addWidgets(new ToggleButtonWidget(144, 18, 18, 18, RESET_BUTTON, () -> false, (reset) -> compound.setLong("energyRate", Long.MAX_VALUE))
                                .setTooltipText("machine.universal.toggle.reset"))
                        .addWidgets(new LabelWidget(25, -13, "metaitem.creative_energy_cover.simulate_voltage", false))
                        .setPredicate(() -> compound.getBoolean("simulateVoltage")))
                .widget(new PopUpWidgetGroup(7, 40, 162, 40)
                        .addWidgets(new ImageWidget(19, 18, 124, 18, DISPLAY))
                        .addWidgets(new TJTextFieldWidget(24, 5, 119, 12, false, () -> String.valueOf(compound.getLong("voltage")), s -> compound.setLong("voltage", Long.parseLong(s)))
                                .setTooltipText("metaitem.creative_energy_cover.set.voltage")
                                .setValidator(str -> Pattern.compile("\\*?[0-9_]*\\*?").matcher(str).matches()))
                        .addWidgets(new TJTextFieldWidget(24, 23, 119, 12, false, () -> String.valueOf(compound.getLong("amps")), s -> compound.setLong("amps", Long.parseLong(s)))
                                .setTooltipText("metaitem.creative_energy_cover.set.amps")
                                .setValidator(str -> Pattern.compile("\\*?[0-9_]*\\*?").matcher(str).matches()))
                        .addWidgets(new ToggleButtonWidget(0, 0, 18, 18, PLUS_BUTTON, () -> false, (plus) -> compound.setLong("voltage", Math.max(1, compound.getLong("voltage") * 2))))
                        .addWidgets(new ToggleButtonWidget(144, 0, 18, 18, MINUS_BUTTON, () -> false, (minus) -> compound.setLong("voltage", Math.max(1, compound.getLong("voltage") / 2))))
                        .addWidgets(new ToggleButtonWidget(0, 18, 18, 18, PLUS_BUTTON, () -> false, (plus) -> compound.setLong("amps", Math.max(1, compound.getLong("amps") * 2))))
                        .addWidgets(new ToggleButtonWidget(144, 18, 18, 18, MINUS_BUTTON, () -> false, (minus) -> compound.setLong("amps", Math.max(1, compound.getLong("amps") / 2))))
                        .addWidgets(new LabelWidget(25, -13, "metaitem.creative_energy_cover.simulate_voltage", true))
                        .setPredicate(() -> compound.getBoolean("simulateVoltage"))
                        .setInverted())
                .bindCloseListener(() -> stack.getOrCreateSubCompound("init").merge(compound))
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
