package tj.items.behaviours;

import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.gui.widgets.CycleButtonWidget;
import gregtech.api.gui.widgets.ToggleButtonWidget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.items.gui.PlayerInventoryHolder;
import gregtech.api.util.Position;
import gregtech.api.util.function.BooleanConsumer;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import tj.mui.TJGuiTextures;
import tj.mui.TJGuiUtils;
import tj.mui.widgets.impl.NewTextFieldWidget;
import tj.mui.widgets.impl.TJLabelWidget;
import tj.mui.widgets.impl.SelectionWidgetGroup;
import tj.mui.widgets.impl.TJPhantomFluidSlotWidget;
import tj.items.TJMetaItems;
import tj.items.covers.VoidMode;
import tj.util.references.BooleanReference;
import tj.util.references.IntegerReference;
import tj.util.references.ObjectReference;

import java.util.function.BiConsumer;
import java.util.function.IntFunction;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class VoidAdvancedFluidCoverBehaviour extends VoidFluidCoverBehaviour {

    @Override
    public ModularUI createUI(PlayerInventoryHolder holder, EntityPlayer player) {
        final ItemStack itemStack = player.getHeldItemMainhand();
        final NBTTagCompound compound = itemStack.getOrCreateSubCompound("voidFilter");
        final Object2ObjectMap<FluidStack, FluidStack> fluidType = new Object2ObjectOpenHashMap<>();
        final FluidTankList fluidFilter = new FluidTankList(true, IntStream.range(0, 9)
                .mapToObj(i -> new FluidTank(Integer.MAX_VALUE))
                .collect(Collectors.toList()));
        final ObjectReference<VoidMode> voidMode = new ObjectReference<>(VoidMode.NORMAL);
        final BooleanReference isWorking = new BooleanReference();
        final IntegerReference tickTime = new IntegerReference(20);

        final BiConsumer<String, String> setFluidCount = (text, id) -> {
            final int index = Integer.parseInt(id);
            if (index < 0 || index >= fluidFilter.getTanks()) return;
            final FluidStack fluidStack = fluidFilter.getTankAt(index).getFluid();
            if (fluidStack == null) return;
            fluidStack.amount = (int) Math.min(Integer.MAX_VALUE, Long.parseLong(text));
            fluidType.put(fluidStack, fluidStack);
            compound.setTag("slot:" + index, fluidStack.writeToNBT(new NBTTagCompound()));
        };
        final IntFunction<String> getFluidCount = index -> String.valueOf(fluidFilter.getTankAt(index).getFluidAmount());
        final BiConsumer<String, String> setTickTime = (text, id) -> {
            tickTime.setValue((int) Math.max(1, Math.min(Integer.MAX_VALUE, Long.parseLong(text))));
            compound.setInteger("tickTime", tickTime.getValue());
        };
        final BooleanConsumer setWorking = working -> {
            isWorking.setValue(working);
            compound.setBoolean("isWorking", working);
        };

        final WidgetGroup widgetGroup = new WidgetGroup(new Position(61, 48));
        final SelectionWidgetGroup selectionWidgetGroup = new SelectionWidgetGroup(61, 48, 54, 54);
        final ClickButtonWidget clickButtonDivide = new ClickButtonWidget(-54, -20, 18, 18, "/2", data -> setFluidCount.accept(String.valueOf(Long.parseLong(getFluidCount.apply(selectionWidgetGroup.getIndex())) / 2), String.valueOf(selectionWidgetGroup.getIndex())));
        final ClickButtonWidget clickButtonMultiply = new ClickButtonWidget(90, -20, 18, 18, "*2", data -> setFluidCount.accept(String.valueOf(Long.parseLong(getFluidCount.apply(selectionWidgetGroup.getIndex())) * 2), String.valueOf(selectionWidgetGroup.getIndex())));
        final NewTextFieldWidget<?> stackSizeTextField = new NewTextFieldWidget<>(-35, -20, 124, 18, true, null, setFluidCount)
                .setValidator(str -> Pattern.compile("\\*?[0-9_]*\\*?").matcher(str).matches())
                .setUpdateOnTyping(true)
                .setMaxStringLength(11);
        stackSizeTextField.setTextSupplier(() -> getFluidCount.apply((int) stackSizeTextField.getTextIdLong()));
        selectionWidgetGroup.setIndexListener(stackSizeTextField::setTextIdLong);
        for (int i = 0; i < fluidFilter.getTanks(); i++) {
            final int index = i;
            widgetGroup.addWidget(new TJPhantomFluidSlotWidget(18 * (i % 3), 18 * (i / 3), 18, 18, i, fluidFilter, fluid -> {
                if (fluid != null) {
                    compound.setTag("slot:" + index, fluid.writeToNBT(new NBTTagCompound()));
                    fluidType.put(fluid, fluid);
                } else compound.removeTag("slot:" + index);
            }, fluidType::remove).setPutFluidsPredicate(fluid -> !fluidType.containsKey(fluid))
                    .setBackgroundTexture(GuiTextures.FLUID_SLOT));
            selectionWidgetGroup.addSubWidget(i, clickButtonDivide);
            selectionWidgetGroup.addSubWidget(i, clickButtonMultiply);
            selectionWidgetGroup.addSubWidget(i, stackSizeTextField);
            selectionWidgetGroup.addSelectionBox(i, 18 * (i % 3), 18 * (i / 3), 18, 18);
        }
        return ModularUI.builder(GuiTextures.BORDERED_BACKGROUND, 176, 208)
                .widget(new TJLabelWidget(7, -18, 162, 18, TJGuiTextures.MACHINE_LABEL_2)
                        .setItemLabel(TJMetaItems.VOID_ADVANCED_FLUID_COVER.getStackForm()).setLocale("metaitem.void_advanced_item_cover.name"))
                .widget(new NewTextFieldWidget<>(26, 7, 124, 18, true, () -> String.valueOf(tickTime.getValue()), setTickTime)
                        .setValidator(str -> Pattern.compile("\\*?[0-9_]*\\*?").matcher(str).matches())
                        .setTooltipText("machine.universal.ticks.operation")
                        .setUpdateOnTyping(true))
                .widget(new ClickButtonWidget(7, 7, 18, 18, "/2", data -> setTickTime.accept(String.valueOf((long) tickTime.getValue() / 2), "")))
                .widget(new ClickButtonWidget(151, 7, 18, 18, "*2", data -> setTickTime.accept(String.valueOf((long) tickTime.getValue() * 2), "")))
                .widget(new CycleButtonWidget(43, 106, 90, 18, VoidMode.class, voidMode::getValue, value -> {
                    compound.setInteger("voidMode", value.ordinal());
                    voidMode.setValue(value);
                }))
                .widget(new ToggleButtonWidget(151, 106, 18, 18, TJGuiTextures.TOGGLE_POWER_BUTTON, isWorking::isValue, setWorking)
                        .setTooltipText("machine.universal.toggle.run.mode"))
                .widget(widgetGroup)
                .widget(selectionWidgetGroup)
                .widget(TJGuiUtils.bindPlayerInventory(new WidgetGroup(), player.inventory, 7, 126, itemStack))
                .bindOpenListener(() -> {
                    if (compound.hasKey("isWorking"))
                        isWorking.setValue(compound.getBoolean("isWorking"));
                    if (compound.hasKey("tickTime"))
                        tickTime.setValue(compound.getInteger("tickTime"));
                    if (compound.hasKey("voidMode"))
                        voidMode.setValue(VoidMode.values()[compound.getInteger("voidMode")]);
                    for (int i = 0; i < fluidFilter.getTanks(); i++) {
                        if (compound.hasKey("slot:" + i)) {
                            fluidFilter.getTankAt(i).fill(FluidStack.loadFluidStackFromNBT(compound.getCompoundTag("slot:" + i)), true);
                            fluidType.put(fluidFilter.getTankAt(i).getFluid(), fluidFilter.getTankAt(i).getFluid());
                        }
                    }
                }).bindCloseListener(() -> itemStack.getOrCreateSubCompound("voidFilter").merge(compound))
                .build(holder, player);
    }
}
