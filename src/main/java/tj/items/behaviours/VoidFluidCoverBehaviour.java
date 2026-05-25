package tj.items.behaviours;

import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.gui.widgets.ToggleButtonWidget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.items.gui.ItemUIFactory;
import gregtech.api.items.gui.PlayerInventoryHolder;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.util.Position;
import gregtech.api.util.function.BooleanConsumer;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import tj.gui.TJGuiTextures;
import tj.gui.TJGuiUtils;
import tj.gui.widgets.NewTextFieldWidget;
import tj.gui.widgets.TJLabelWidget;
import tj.gui.widgets.impl.TJPhantomFluidSlotWidget;
import tj.items.TJMetaItems;
import tj.util.references.BooleanReference;
import tj.util.references.IntegerReference;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class VoidFluidCoverBehaviour implements IItemBehaviour, ItemUIFactory {

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
        final WidgetGroup widgetGroup = new WidgetGroup(new Position(53, 27));
        final Object2ObjectMap<FluidStack, FluidStack> fluidType = new Object2ObjectOpenHashMap<>();
        final IMultipleTankHandler fluidFilter = new FluidTankList(true, IntStream.range(0, 9)
                .mapToObj(i -> new FluidTank(Integer.MAX_VALUE))
                .collect(Collectors.toList()));
        for (int i = 0; i < fluidFilter.getTanks(); i++) {
            final int index = i;
            widgetGroup.addWidget(new TJPhantomFluidSlotWidget(10 + 18 * (i % 3), 18 * (i / 3), 18, 18, i, fluidFilter, fluid -> {
                if (fluid != null) {
                    fluid.amount = Integer.MAX_VALUE;
                    compound.setTag("slot:" + index, fluid.writeToNBT(new NBTTagCompound()));
                    fluidType.put(fluid, fluid);
                } else compound.removeTag("slot:" + index);
            }, fluidType::remove).setPutFluidsPredicate(fluid -> !fluidType.containsKey(fluid))
                    .setBackgroundTexture(GuiTextures.FLUID_SLOT));
        }
        final BooleanReference isWorking = new BooleanReference();
        final IntegerReference tickTime = new IntegerReference(20);
        final BiConsumer<String, String> setTickTime = (text, id) -> {
            tickTime.setValue((int) Math.max(1, Math.min(Integer.MAX_VALUE, Long.parseLong(text))));
            compound.setInteger("tickTime", tickTime.getValue());
        };
        final BooleanConsumer setWorking = working -> {
            isWorking.setValue(working);
            compound.setBoolean("isWorking", working);
        };
        return ModularUI.builder(GuiTextures.BORDERED_BACKGROUND, 176, 105 + 82)
                .widget(new TJLabelWidget(7, -18, 162, 18, TJGuiTextures.MACHINE_LABEL_2)
                        .setItemLabel(TJMetaItems.VOID_FLUID_COVER.getStackForm()).setLocale("metaitem.void_fluid_cover.name"))
                .widget(new NewTextFieldWidget<>(26, 7, 124, 18, true, tickTime::toString, setTickTime)
                        .setValidator(str -> Pattern.compile("\\*?[0-9_]*\\*?").matcher(str).matches())
                        .setTooltipText("machine.universal.ticks.operation")
                        .setUpdateOnTyping(true))
                .widget(new ClickButtonWidget(7, 7, 18, 18, "/2", data -> setTickTime.accept(String.valueOf((long) tickTime.getValue() / 2), "")))
                .widget(new ClickButtonWidget(151, 7, 18, 18, "*2", data -> setTickTime.accept(String.valueOf((long) tickTime.getValue() * 2), "")))
                .widget(new ToggleButtonWidget(151, 85, 18, 18, TJGuiTextures.POWER_BUTTON, isWorking::isValue, setWorking)
                        .setTooltipText("machine.universal.toggle.run.mode"))
                .widget(widgetGroup)
                .widget(TJGuiUtils.bindPlayerInventory(new WidgetGroup(), player.inventory, 7, 105, itemStack))
                .bindOpenListener(() -> {
                    if (compound.hasKey("isWorking"))
                        isWorking.setValue(compound.getBoolean("isWorking"));
                    if (compound.hasKey("tickTime"))
                        tickTime.setValue(compound.getInteger("tickTime"));
                    for (int i = 0; i < fluidFilter.getTanks(); i++) {
                        if (compound.hasKey("slot:" + i)) {
                            fluidFilter.getTankAt(i).fill(FluidStack.loadFluidStackFromNBT(compound.getCompoundTag("slot:" + i)), true);
                            fluidType.put(fluidFilter.getTankAt(i).getFluid(), fluidFilter.getTankAt(i).getFluid());
                        }
                    }
                }).bindCloseListener(() -> itemStack.getOrCreateSubCompound("voidFilter").merge(compound))
                .build(holder, player);
    }

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
        lines.add(I18n.format("metaitem.void_fluid_cover.description"));
        lines.add(I18n.format("cover.creative.description"));
    }
}
