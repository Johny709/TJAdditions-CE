package tj.items.behaviours;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.*;
import gregtech.api.items.gui.ItemUIFactory;
import gregtech.api.items.gui.PlayerInventoryHolder;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.util.Position;
import gregtech.common.covers.filter.SimpleFluidFilter;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import tj.gui.TJGuiTextures;
import tj.gui.TJGuiUtils;

import java.util.List;
import java.util.function.Consumer;

public class CreativeFluidCoverBehaviour implements IItemBehaviour, ItemUIFactory {

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
        if (!compound.hasKey("speed"))
            compound.setInteger("speed", 1);
        if (!compound.hasKey("power"))
            compound.setBoolean("power", false);
        final SimpleFluidFilter fluidFilter = new SimpleFluidFilter() {
            @Override
            public void setFluidInSlot(int slotIndex, FluidStack fluidStack) {
                super.setFluidInSlot(slotIndex, fluidStack);
                if (fluidStack != null)
                    compound.setTag("slot:" + slotIndex, fluidStack.writeToNBT(new NBTTagCompound()));
                else compound.removeTag("slot:" + slotIndex);
            }
        };
        final Consumer<Widget.ClickData> onIncrement = clickData -> {
            int speed = compound.getInteger("speed");
            int value = clickData.isCtrlClick ? 100
                    : clickData.isShiftClick ? 10
                    : 1;
            speed = MathHelper.clamp(speed +value, 1, Integer.MAX_VALUE);
            compound.setInteger("speed", speed);
        };
        final Consumer<Widget.ClickData> onDecrement = clickData -> {
            int speed = compound.getInteger("speed");
            int value = clickData.isCtrlClick ? 100
                    : clickData.isShiftClick ? 10
                    : 1;
            speed = MathHelper.clamp(speed -value, 1, Integer.MAX_VALUE);
            compound.setInteger("speed", speed);
        };
        for (int i = 0; i < 9; i++) {
            if (compound.hasKey("slot:" + i))
                fluidFilter.setFluidInSlot(i, FluidStack.loadFluidStackFromNBT(compound.getCompoundTag("slot:" + i)));
        }
        final WidgetGroup fluidFilterGroup = new WidgetGroup(new Position(51, 25));
        fluidFilterGroup.addWidget(new LabelWidget(-15, -15, "cover.creative_fluid.title"));
        fluidFilterGroup.addWidget(new ImageWidget(10, 55, 55, 18, GuiTextures.DISPLAY));
        fluidFilterGroup.addWidget(new AdvancedTextWidget(12, 60, textList -> textList.add(new TextComponentTranslation("metaitem.creative.cover.display.ticks", compound.getInteger("speed"))), 0xFFFFFF));
        fluidFilterGroup.addWidget(new ClickButtonWidget(-8, 55, 18, 18, "+", onIncrement));
        fluidFilterGroup.addWidget(new ClickButtonWidget(65, 55, 18, 18, "-", onDecrement));
        fluidFilterGroup.addWidget(new ToggleButtonWidget(83, 55, 18, 18, TJGuiTextures.RESET_BUTTON, () -> false, b -> compound.setInteger("speed", 1))
                .setTooltipText("machine.universal.toggle.reset"));
        fluidFilterGroup.addWidget(new ToggleButtonWidget(101, 55, 18, 18, TJGuiTextures.POWER_BUTTON, () -> compound.getBoolean("power"), b -> compound.setBoolean("power", !compound.getBoolean("power")))
                .setTooltipText("machine.universal.toggle.run.mode"));
        fluidFilter.initUI(fluidFilterGroup::addWidget);
        return ModularUI.builder(GuiTextures.BORDERED_BACKGROUND, 176, 105 + 82)
                .widget(fluidFilterGroup)
                .bindCloseListener(() -> stack.getOrCreateSubCompound("init").merge(compound))
                .widget(TJGuiUtils.bindPlayerInventory(new WidgetGroup(), player.inventory, 7, 105, stack))
                .build(holder, player);
    }

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
        lines.add(I18n.format("cover.creative.only"));
        lines.add(I18n.format("metaitem.creative.fluid.cover.description"));
        lines.add(I18n.format("cover.creative.description"));
    }
}
