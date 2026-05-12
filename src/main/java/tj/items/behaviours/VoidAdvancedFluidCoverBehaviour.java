package tj.items.behaviours;

import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.CycleButtonWidget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.items.gui.PlayerInventoryHolder;
import gregtech.api.util.Position;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import tj.gui.TJGuiTextures;
import tj.gui.TJGuiUtils;
import tj.gui.widgets.NewTextFieldWidget;
import tj.gui.widgets.TJLabelWidget;
import tj.gui.widgets.impl.SelectionWidgetGroup;
import tj.gui.widgets.impl.TJPhantomFluidSlotWidget;
import tj.items.covers.VoidMode;
import tj.util.references.ObjectReference;

import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class VoidAdvancedFluidCoverBehaviour extends VoidFluidCoverBehaviour {

    @Override
    public ModularUI createUI(PlayerInventoryHolder holder, EntityPlayer player) {
        final ItemStack itemStack = player.getHeldItemMainhand();
        final NBTTagCompound compound = itemStack.getOrCreateSubCompound("voidFilter");
        final FluidTankList fluidFilter = new FluidTankList(true, IntStream.range(0, 9)
                .mapToObj(i -> new FluidTank(Integer.MAX_VALUE))
                .collect(Collectors.toList()));
        final WidgetGroup widgetGroup = new WidgetGroup(new Position(63, 27));
        final SelectionWidgetGroup selectionWidgetGroup = new SelectionWidgetGroup(63, 27, 54, 54);
        for (int i = 0; i < fluidFilter.getTanks(); i++) {
            final int index = i;
            widgetGroup.addWidget(new TJPhantomFluidSlotWidget(18 * (i % 3), 18 * (i / 3), 18, 18, i, fluidFilter, fluid -> {
                if (fluid != null)
                    compound.setTag("slot:" + index, fluid.writeToNBT(new NBTTagCompound()));
                else compound.removeTag("slot:" + index);
            }).setBackgroundTexture(GuiTextures.FLUID_SLOT));
            selectionWidgetGroup.addSubWidget(i, new NewTextFieldWidget<>(0, -20, 54, 18, true, () -> String.valueOf(fluidFilter.getTankAt(index).getFluidAmount()), (text, id) -> {
                FluidStack stack = fluidFilter.getTankAt(index).drain(Integer.MAX_VALUE, false);
                if (stack == null) return;
                stack = fluidFilter.getTankAt(index).drain(Integer.MAX_VALUE, true);
                stack.amount = Math.max(1, (int) Math.min(Integer.MAX_VALUE, Long.parseLong(text)));
                compound.setTag("slot:" + index, stack.writeToNBT(new NBTTagCompound()));
                fluidFilter.getTankAt(index).fill(stack, true);
            }).setValidator(str -> Pattern.compile("\\*?[0-9_]*\\*?").matcher(str).matches())
                    .setUpdateOnTyping(true)
                    .setMaxStringLength(11));
            selectionWidgetGroup.addSelectionBox(i, 18 * (i % 3), 18 * (i / 3), 18, 18);
        }
        final ObjectReference<VoidMode> voidMode = new ObjectReference<>(VoidMode.NORMAL);
        widgetGroup.addWidget(new CycleButtonWidget(0, 54, 54, 54, VoidMode.class, voidMode::getValue, value -> {
            compound.setInteger("voidMode", value.ordinal());
            voidMode.setValue(value);
        }));
        return ModularUI.builder(GuiTextures.BORDERED_BACKGROUND, 176, 105 + 82)
                .widget(new TJLabelWidget(7, -18, 162, 18, TJGuiTextures.MACHINE_LABEL_2)
                        .setLocale("metaitem.void_advanced_item_cover.name"))
                .widget(widgetGroup)
                .widget(selectionWidgetGroup)
                .widget(TJGuiUtils.bindPlayerInventory(new WidgetGroup(), player.inventory, 7, 105, itemStack))
                .bindOpenListener(() -> {
                    if (compound.hasKey("voidMode"))
                        voidMode.setValue(VoidMode.values()[compound.getInteger("voidMode")]);
                    for (int i = 0; i < fluidFilter.getTanks(); i++) {
                        if (compound.hasKey("slot:" + i))
                            fluidFilter.getTankAt(i).fill(FluidStack.loadFluidStackFromNBT(compound.getCompoundTag("slot:" + i)), true);
                    }
                }).bindCloseListener(() -> itemStack.getOrCreateSubCompound("voidFilter").merge(compound))
                .build(holder, player);
    }
}
