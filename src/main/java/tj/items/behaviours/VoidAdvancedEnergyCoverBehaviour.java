package tj.items.behaviours;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.items.gui.ItemUIFactory;
import gregtech.api.items.gui.PlayerInventoryHolder;
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
import tj.mui.widgets.TJModularUI;
import tj.mui.widgets.impl.*;
import tj.items.TJMetaItems;
import tj.items.covers.VoidMode;
import tj.util.TJItemUtils;
import tj.util.references.LongReference;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;


public class VoidAdvancedEnergyCoverBehaviour extends VoidEnergyCoverBehaviour implements ItemUIFactory {

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        if (!world.isRemote)
            PlayerInventoryHolder.openHandItemUI(player, hand);
        return ActionResult.newResult(EnumActionResult.SUCCESS, player.getHeldItemMainhand());
    }

    @Override
    public ModularUI createUI(PlayerInventoryHolder holder, EntityPlayer player) {
        final ItemStack itemStack = player.getHeldItemMainhand();
        final NBTTagCompound compound = itemStack.getOrCreateSubCompound("voidFilter");
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
        final LongReference throughput = new LongReference();
        final BiConsumer<String, String> setThroughput = (text, id) -> {
            throughput.setValue((long) Math.max(0, Math.min(Long.MAX_VALUE, Double.parseDouble(text))));
            compound.setLong("throughput", throughput.getValue());
        };
        final PanelWidgetGroup panelWidgetGroup = TJGuiUtils.createTickPanel(tickSetter);
        return TJModularUI.builder(holder, player)
                .setWidthHeight(200, 187)
                .widget(new TJLabelWidget(7, -18, 162, 18, TJGuiTextures.MACHINE_LABEL_2)
                        .setItemLabel(TJMetaItems.VOID_ADVANCED_ENERGY_COVER.getStackForm()).setLocale("metaitem.void_advanced_energy_cover.name"))
                .widget(new ButtonWidget<>(7, 30, 18, 18, "/2", b -> setThroughput.accept(String.valueOf((double) throughput.getValue() / 2), "")).setBackgroundTextures(GuiTextures.VANILLA_BUTTON))
                .widget(new ButtonWidget<>(151, 30, 18, 18, "*2", b -> setThroughput.accept(String.valueOf((double) throughput.getValue() * 2), "")).setBackgroundTextures(GuiTextures.VANILLA_BUTTON))
                .widget(new NewTextFieldWidget<>(26, 30, 124, 18, true, throughput::toString, setThroughput)
                        .setValidator(str -> Pattern.compile("-*?[0-9_]*\\*?").matcher(str).matches())
                        .setUpdateOnTyping(true))
                .widget(new TJCycleButtonWidget<>(26, 50, 124, 18, VoidMode.class,
                        () -> VoidMode.values()[compound.getInteger("voidMode")],  value ->
                        compound.setInteger("voidMode", value.ordinal())).setBackgroundTextures(GuiTextures.VANILLA_BUTTON))
                .widget(new TJToggleButtonWidget(174, 105, 18, 18, TJGuiTextures.TOGGLE_RESET_BUTTON,
                        () -> false, b -> compound.setInteger("ticks", 1))
                        .setTitleHoverTooltipText("machine.universal.toggle.reset.disabled"))
                .widget(new TJToggleButtonWidget(174, 162, 18, 18, TJGuiTextures.TOGGLE_POWER_BUTTON,
                        () -> compound.getBoolean("isWorking"), b -> compound.setBoolean("isWorking", !compound.getBoolean("isWorking")))
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
                .bindPlayerInventory(itemStack)
                .bindCloseListener(() -> itemStack.getOrCreateSubCompound("voidFilter").merge(compound))
                .build();
    }

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
        super.addInformation(itemStack, lines);
        lines.add(I18n.format("cover.creative.description"));
    }
}
