package tj.items.behaviours;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.CycleButtonWidget;
import gregtech.api.gui.widgets.ToggleButtonWidget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.items.gui.ItemUIFactory;
import gregtech.api.items.gui.PlayerInventoryHolder;
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
import tj.gui.widgets.NewTextFieldWidget;
import tj.gui.widgets.TJLabelWidget;
import tj.items.TJMetaItems;
import tj.items.covers.VoidMode;
import tj.util.references.LongReference;
import tj.util.references.ObjectReference;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

import static tj.gui.TJGuiTextures.MINUS_BUTTON;
import static tj.gui.TJGuiTextures.PLUS_BUTTON;

public class VoidAdvancedEnergyCoverBehaviour extends VoidEnergyCoverBehaviour implements ItemUIFactory {

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        if (!world.isRemote)
            PlayerInventoryHolder.openHandItemUI(player, hand);
        return ActionResult.newResult(EnumActionResult.PASS, player.getHeldItemMainhand());
    }

    @Override
    public ModularUI createUI(PlayerInventoryHolder holder, EntityPlayer player) {
        final ItemStack itemStack = player.getHeldItemMainhand();
        final NBTTagCompound compound = itemStack.getOrCreateSubCompound("voidFilter");
        final ObjectReference<VoidMode> voidMode = new ObjectReference<>(VoidMode.NORMAL);
        final LongReference throughput = new LongReference();
        final BiConsumer<String, String> setThroughput = (text, id) -> {
            throughput.setValue((long) Math.max(0, Math.min(Long.MAX_VALUE, Double.parseDouble(text))));
            compound.setLong("throughput", throughput.getValue());
        };
        return ModularUI.builder(GuiTextures.BORDERED_BACKGROUND, 176, 105 + 82)
                .widget(new TJLabelWidget(7, -18, 162, 18, TJGuiTextures.MACHINE_LABEL_2)
                        .setItemLabel(TJMetaItems.VOID_ADVANCED_ENERGY_COVER.getStackForm()).setLocale("metaitem.void_energy_cover.name"))
                .widget(new ToggleButtonWidget(7, 30, 18, 18, MINUS_BUTTON, () -> false, b -> setThroughput.accept(String.valueOf((double) throughput.getValue() / 2), "")))
                .widget(new ToggleButtonWidget(151, 30, 18, 18, PLUS_BUTTON, () -> false, b -> setThroughput.accept(String.valueOf((double) throughput.getValue() * 2), "")))
                .widget(new NewTextFieldWidget<>(26, 30, 124, 18, true, throughput::toString, setThroughput)
                        .setValidator(str -> Pattern.compile("-*?[0-9_]*\\*?").matcher(str).matches())
                        .setUpdateOnTyping(true))
                .widget(new CycleButtonWidget(26, 50, 124, 18, VoidMode.class, voidMode::getValue, value -> {
                    compound.setInteger("voidMode", value.ordinal());
                    voidMode.setValue(value);
                })).widget(new TJLabelWidget(7, -18, 162, 18, TJGuiTextures.MACHINE_LABEL_2)
                        .setLocale("metaitem.void_energy_cover.name"))
                .widget(TJGuiUtils.bindPlayerInventory(new WidgetGroup(), player.inventory, 7, 105, itemStack))
                .bindOpenListener(()-> {
                    if (compound.hasKey("throughput"))
                        throughput.setValue(compound.getLong("throughput"));
                    if (compound.hasKey("voidMode"))
                        voidMode.setValue(VoidMode.values()[compound.getInteger("voidMode")]);
                }).bindCloseListener(() -> itemStack.getOrCreateSubCompound("voidFilter").merge(compound))
                .build(holder, player);
    }

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
        super.addInformation(itemStack, lines);
        lines.add(I18n.format("cover.creative.description"));
    }
}
