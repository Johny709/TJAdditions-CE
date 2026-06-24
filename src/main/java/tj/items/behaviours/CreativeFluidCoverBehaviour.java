package tj.items.behaviours;

import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.*;
import gregtech.api.items.gui.ItemUIFactory;
import gregtech.api.items.gui.PlayerInventoryHolder;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.util.Position;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import tj.mui.TJGuiTextures;
import tj.mui.TJGuiUtils;
import tj.mui.widgets.ButtonWidget;
import tj.mui.widgets.impl.NewTextFieldWidget;
import tj.mui.widgets.impl.TJLabelWidget;
import tj.mui.widgets.impl.SelectionWidgetGroup;
import tj.mui.widgets.impl.TJPhantomFluidSlotWidget;
import tj.items.TJMetaItems;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CreativeFluidCoverBehaviour implements IItemBehaviour, ItemUIFactory {

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        if (!world.isRemote)
            PlayerInventoryHolder.openHandItemUI(player, hand);
        return ActionResult.newResult(EnumActionResult.SUCCESS, player.getHeldItemMainhand());
    }

    @Override
    public ModularUI createUI(PlayerInventoryHolder holder, EntityPlayer player) {
        final ItemStack itemStack = player.getHeldItem(EnumHand.MAIN_HAND);
        final NBTTagCompound compound = itemStack.getOrCreateSubCompound("init");
        final FluidTankList fluidFilter = new FluidTankList(true, IntStream.range(0, 9)
                .mapToObj(i -> new FluidTank(Integer.MAX_VALUE))
                .collect(Collectors.toList()));

        final BiConsumer<String, String> setFluidCount = (text, id) -> {
            final int index = Integer.parseInt(id);
            if (index < 0 || index >= fluidFilter.getTanks()) return;
            final FluidStack fluidStack = fluidFilter.getTankAt(index).getFluid();
            if (fluidStack == null) return;
            fluidStack.amount = (int) Math.min(Integer.MAX_VALUE, Long.parseLong(text));
            compound.setTag("slot:" + index, fluidStack.writeToNBT(new NBTTagCompound()));
        };
        final IntFunction<String> getFluidCount = index -> String.valueOf(fluidFilter.getTankAt(index).getFluidAmount());
        final Consumer<Widget.ClickData> onIncrement = clickData -> {
            final int speed = compound.getInteger("speed");
            final int value = clickData.isCtrlClick ? 100
                    : clickData.isShiftClick ? 10
                    : 1;
            compound.setInteger("speed", MathHelper.clamp(speed + value, 1, Integer.MAX_VALUE));
        };
        final Consumer<Widget.ClickData> onDecrement = clickData -> {
            final int speed = compound.getInteger("speed");
            final int value = clickData.isCtrlClick ? 100
                    : clickData.isShiftClick ? 10
                    : 1;
            compound.setInteger("speed", MathHelper.clamp(speed - value, 1, Integer.MAX_VALUE));
        };

        final WidgetGroup widgetGroup = new WidgetGroup(new Position(61, 25));
        final SelectionWidgetGroup selectionWidgetGroup = new SelectionWidgetGroup(61, 25, 54, 54);
        final ButtonWidget<?> clickButtonDivide = new ButtonWidget<>(-54, -20, 18, 18, "/2", data -> setFluidCount.accept(String.valueOf(Long.parseLong(getFluidCount.apply(selectionWidgetGroup.getIndex())) / 2), String.valueOf(selectionWidgetGroup.getIndex())));
        final ButtonWidget<?> clickButtonMultiply = new ButtonWidget<>(90, -20, 18, 18, "*2", data -> setFluidCount.accept(String.valueOf(Long.parseLong(getFluidCount.apply(selectionWidgetGroup.getIndex())) * 2), String.valueOf(selectionWidgetGroup.getIndex())));
        final NewTextFieldWidget<?> fluidCountTextField = new NewTextFieldWidget<>(-35, -20, 124, 18, true, null, setFluidCount)
                .setValidator(str -> Pattern.compile("\\*?[0-9_]*\\*?").matcher(str).matches())
                .setUpdateOnTyping(true)
                .setMaxStringLength(11);
        fluidCountTextField.setTextSupplier(() -> getFluidCount.apply((int) fluidCountTextField.getTextIdLong()));
        selectionWidgetGroup.setIndexListener(fluidCountTextField::setTextIdLong);
        for (int i = 0; i < fluidFilter.getTanks(); i++) {
            final int index = i;
            widgetGroup.addWidget(new TJPhantomFluidSlotWidget(18 * (i % 3), 18 * (i / 3), 18, 18, i, fluidFilter, fluid -> {
                if (fluid != null) {
                    compound.setTag("slot:" + index, fluid.writeToNBT(new NBTTagCompound()));
                } else compound.removeTag("slot:" + index);
            }).setBackgroundTexture(GuiTextures.FLUID_SLOT));
            selectionWidgetGroup.addSubWidget(i, clickButtonDivide.setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
            selectionWidgetGroup.addSubWidget(i, clickButtonMultiply.setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
            selectionWidgetGroup.addSubWidget(i, fluidCountTextField);
            selectionWidgetGroup.addSelectionBox(i, 18 * (i % 3), 18 * (i / 3), 18, 18);
        }
        return ModularUI.builder(GuiTextures.BORDERED_BACKGROUND, 176, 187)
                .widget(new TJLabelWidget(7, -18, 162, 18, TJGuiTextures.MACHINE_LABEL_2)
                        .setItemLabel(TJMetaItems.CREATIVE_FLUID_COVER.getStackForm()).setLocale("cover.creative_fluid.title"))
                .widget(new ImageWidget(61, 80, 55, 18, GuiTextures.DISPLAY))
                .widget(new AdvancedTextWidget(63, 85, textList -> textList.add(new TextComponentTranslation("metaitem.creative.cover.display.ticks", compound.getInteger("speed"))), 0xFFFFFF))
                .widget(new ButtonWidget<>(43, 80, 18, 18, "+", onIncrement).setBackgroundTextures(GuiTextures.VANILLA_BUTTON))
                .widget(new ButtonWidget<>(116, 80, 18, 18, "-", onDecrement).setBackgroundTextures(GuiTextures.VANILLA_BUTTON))
                .widget(new ToggleButtonWidget(134, 80, 18, 18, TJGuiTextures.TOGGLE_RESET_BUTTON, () -> false, b -> compound.setInteger("speed", 1))
                        .setTooltipText("machine.universal.toggle.reset"))
                .widget(new ToggleButtonWidget(152, 80, 18, 18, TJGuiTextures.TOGGLE_POWER_BUTTON, () -> compound.getBoolean("power"), b -> compound.setBoolean("power", !compound.getBoolean("power")))
                        .setTooltipText("machine.universal.toggle.run.mode"))
                .widget(widgetGroup)
                .widget(selectionWidgetGroup)
                .widget(TJGuiUtils.bindPlayerInventory(new WidgetGroup(), player.inventory, 7, 105, itemStack))
                .bindOpenListener(() -> {
                    for (int i = 0; i < 9; i++) {
                        if (compound.hasKey("slot:" + i))
                            fluidFilter.getTankAt(i).fill(FluidStack.loadFluidStackFromNBT(compound.getCompoundTag("slot:" + i)), true);
                    }
                }).bindCloseListener(() -> itemStack.getOrCreateSubCompound("init").merge(compound))
                .build(holder, player);
    }

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
        lines.add(I18n.format("cover.creative.only"));
        lines.add(I18n.format("metaitem.creative.fluid.cover.description"));
        lines.add(I18n.format("cover.creative.description"));
    }
}
