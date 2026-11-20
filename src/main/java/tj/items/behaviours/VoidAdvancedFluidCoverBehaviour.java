package tj.items.behaviours;

import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
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
import tj.integration.ae2.IFluidConfigSetter;
import tj.integration.ae2.ITickSetter;
import tj.mui.TJGuiTextures;
import tj.mui.TJGuiUtils;
import tj.mui.widgets.TJModularUI;
import tj.mui.widgets.impl.*;
import tj.items.TJMetaItems;
import tj.items.covers.VoidMode;
import tj.util.TJItemUtils;

import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class VoidAdvancedFluidCoverBehaviour extends VoidFluidCoverBehaviour {

    @Override
    public ModularUI createUI(PlayerInventoryHolder holder, EntityPlayer player) {
        final ItemStack itemStack = player.getHeldItemMainhand();
        final NBTTagCompound compound = itemStack.getOrCreateSubCompound("voidFilter");
        final Object2ObjectMap<FluidStack, FluidStack> fluidType = new Object2ObjectOpenHashMap<>();
        final FluidTankList fluidFilter = new FluidTankList(true, IntStream.range(0, 16)
                .mapToObj(i -> new FluidTank(Integer.MAX_VALUE))
                .collect(Collectors.toList()));
        final ITickSetter tickSetter = new ITickSetter() {
            @Override
            public void setTickTime(long tickTime) {
                compound.setInteger("tickTime", (int) Math.max(1, Math.min(Integer.MAX_VALUE, tickTime)));
            }

            @Override
            public int getTickTime() {
                return compound.getInteger("tickTime");
            }
        };
        final IFluidConfigSetter fluidConfigSetter = new IFluidConfigSetter() {
            @Override
            public void setFluidCount(long count, int index) {
                if (index < 0 || index >= fluidFilter.getTanks()) return;
                final FluidStack fluidStack = fluidFilter.getTankAt(index).getFluid();
                if (fluidStack == null) return;
                fluidStack.amount = (int) Math.min(Integer.MAX_VALUE, count);
                compound.setTag("slot:" + index, fluidStack.writeToNBT(new NBTTagCompound()));
            }

            @Override
            public int getFluidCount(int index) {
                return index >= 0 && index < fluidFilter.getTanks() ? fluidFilter.getTankAt(index).getFluidAmount() : 0;
            }
        };
        final WidgetGroup widgetGroup = new WidgetGroup(new Position(61, 8));
        final SelectionWidgetGroup selectionWidgetGroup = new SelectionWidgetGroup(61, 26, 54, 54);
        for (int i = 0; i < fluidFilter.getTanks(); i++) {
            final int index = i;
            final int x = 18 * (i % 4);
            final int y = 18 * (i / 4);
            widgetGroup.addWidget(new TJPhantomFluidSlotWidget(x, y, 18, 18, i, fluidFilter, fluid -> {
                if (fluid != null) {
                    compound.setTag("slot:" + index, fluid.writeToNBT(new NBTTagCompound()));
                    fluidType.put(fluid, fluid);
                } else compound.removeTag("slot:" + index);
            }, fluidType::remove).setPutFluidsPredicate(fluid -> !fluidType.containsKey(fluid))
                    .setBackgroundTexture(GuiTextures.FLUID_SLOT));
            selectionWidgetGroup.addSelectionBox(i, x, y, 18, 18);
        }
        final PanelWidgetGroup panelWidgetGroup = TJGuiUtils.createTickPanel(tickSetter);
        return TJModularUI.builder(holder, player)
                .setWidthHeight(200, 208)
                .widget(new TJLabelWidget(7, -18, 162, 18, TJGuiTextures.MACHINE_LABEL_2)
                        .setItemLabel(TJMetaItems.VOID_ADVANCED_FLUID_COVER.getStackForm()).setLocale("metaitem.void_advanced_item_cover.name"))
                .widget(new TJCycleButtonWidget<>(7, 84, 162, 18, VoidMode.class,
                        () -> VoidMode.values()[compound.getInteger("voidMode")],  value ->
                        compound.setInteger("voidMode", value.ordinal())).setBackgroundTextures(GuiTextures.VANILLA_BUTTON))
                .widget(new TJToggleButtonWidget(174, 126, 18, 18, TJGuiTextures.TOGGLE_RESET_BUTTON,
                        () -> false, b -> compound.setInteger("tickTime", 1))
                        .setTitleHoverTooltipText("machine.universal.toggle.reset.disabled"))
                .widget(new TJToggleButtonWidget(174, 184, 18, 18, TJGuiTextures.TOGGLE_POWER_BUTTON,
                        () -> compound.getBoolean("isWorking"), b -> compound.setBoolean("isWorking", !compound.getBoolean("isWorking")))
                        .setToggleTitleTooltipHoverText("machine.universal.toggle.run.mode.disabled",
                                "machine.universal.toggle.run.mode.enabled"))
                .widget(new NewTextFieldWidget<>(7, 103, 162, 18, true, () -> String.valueOf(tickSetter.getTickTime()),
                        (text, id) -> tickSetter.setTickTime(Long.parseLong(text)))
                        .setValidator(str -> Pattern.compile("\\*?[0-9_]*\\*?").matcher(str).matches())
                        .setTooltipText("machine.universal.ticks.operation")
                        .setUpdateOnTyping(true)
                        .setMaxStringLength(11))
                .widget(new TJToggleButtonWidget(174, 103, 18, 18, panelWidgetGroup::isPanelOpen, (BooleanConsumer) value -> {
                    if (!value && panelWidgetGroup.isPanelOpen()) {
                        panelWidgetGroup.closePanel();
                    } else if (value) {
                        panelWidgetGroup.openPanel();
                    }
                }).setToggleTexture(GuiTextures.TOGGLE_BUTTON_BACK)
                        .setItemDisplay(TJItemUtils.getItemStackFromName("enderio:item_material", 1, 11)) // infinity bimetal gear
                        .setHoverTooltipText("tj.machine.universal.ticks.operation_configure")
                        .useToggleTexture(true))
                .widget(widgetGroup)
                .widget(selectionWidgetGroup)
                .widget(TJGuiUtils.createFluidConfigPanel(fluidConfigSetter, selectionWidgetGroup))
                .widget(panelWidgetGroup)
                .bindPlayerInventory(itemStack)
                .bindOpenListener(() -> {
                    for (int i = 0; i < fluidFilter.getTanks(); i++) {
                        if (compound.hasKey("slot:" + i)) {
                            fluidFilter.getTankAt(i).fill(FluidStack.loadFluidStackFromNBT(compound.getCompoundTag("slot:" + i)), true);
                            fluidType.put(fluidFilter.getTankAt(i).getFluid(), fluidFilter.getTankAt(i).getFluid());
                        }
                    }
                }).bindCloseListener(() -> itemStack.getOrCreateSubCompound("voidFilter").merge(compound))
                .build();
    }
}
