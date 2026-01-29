package tj.items.behaviours;

import gregtech.api.block.machines.BlockMachine;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import tj.builder.multicontrollers.ParallelRecipeMapMultiblockController;
import tj.util.ItemStackHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class VoidPlungerBehaviour implements IItemBehaviour {

    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
        NBTTagCompound nbt = player.getHeldItem(hand).getOrCreateSubCompound("Plunger");
        boolean voiding = nbt.getBoolean("Void");
        MetaTileEntity metaTileEntity = BlockMachine.getMetaTileEntity(world, pos);
        if (!world.isRemote) {
            IItemHandlerModifiable importItems, exportItems;
            IMultipleTankHandler importFluids, exportFluids;
            if (metaTileEntity != null) {
                importItems = metaTileEntity.getImportItems();
                importFluids = metaTileEntity.getImportFluids();
                exportItems = metaTileEntity.getExportItems();
                exportFluids = metaTileEntity.getExportFluids();
            } else {
                player.sendMessage(new TextComponentTranslation("metaitem.void_plunger.message.fail"));
                return EnumActionResult.SUCCESS;
            }
            if (metaTileEntity instanceof RecipeMapMultiblockController) {
                RecipeMapMultiblockController controller = (RecipeMapMultiblockController) metaTileEntity;
                importItems = controller.getInputInventory();
                importFluids = controller.getInputFluidInventory();
                exportItems = controller.getOutputInventory();
                exportFluids = controller.getOutputFluidInventory();
            } else if (metaTileEntity instanceof ParallelRecipeMapMultiblockController) {
                ParallelRecipeMapMultiblockController controller = (ParallelRecipeMapMultiblockController) metaTileEntity;
                importItems = controller.getImportItemInventory();
                importFluids = controller.getImportFluidTank();
                exportItems = controller.getExportItemInventory();
                exportFluids = controller.getExportFluidTank();
            }

            ITextComponent importItemText = new TextComponentTranslation("metaitem.void_plunger.message.void.item.input");
            this.listItems(importItems, voiding, player).stream()
                    .filter(item -> !item.isEmpty())
                    .forEach(item -> importItemText.appendText("\n").appendSibling(new TextComponentTranslation(item.getTranslationKey() + ".name")
                                    .setStyle(new Style().setColor(TextFormatting.GOLD))
                    .appendText(" ").appendSibling(new TextComponentString(String.valueOf(item.getCount())))));

            ITextComponent importFluidText = new TextComponentTranslation("metaitem.void_plunger.message.void.fluid.input");
            this.listFluids(importFluids, voiding).stream()
                    .filter(Objects::nonNull)
                    .forEach(fluid -> importFluidText.appendText("\n").appendSibling(new TextComponentTranslation(fluid.getUnlocalizedName())
                            .setStyle(new Style().setColor(TextFormatting.AQUA))
                    .appendText(" ").appendSibling(new TextComponentString(String.valueOf(fluid.amount)))));

            ITextComponent exportItemText = new TextComponentTranslation("metaitem.void_plunger.message.void.item.output");
            this.listItems(exportItems, voiding, player).stream()
                    .filter(item -> !item.isEmpty())
                    .forEach(item -> exportItemText.appendText("\n").appendSibling(new TextComponentTranslation(item.getTranslationKey() + ".name")
                            .setStyle(new Style().setColor(TextFormatting.GOLD))
                    .appendText(" ").appendSibling(new TextComponentString(String.valueOf(item.getCount())))));

            ITextComponent exportFluidText = new TextComponentTranslation("metaitem.void_plunger.message.void.fluid.output");
            this.listFluids(exportFluids, voiding).stream()
                    .filter(Objects::nonNull)
                    .forEach(fluid -> exportFluidText.appendText("\n").appendSibling(new TextComponentTranslation(fluid.getUnlocalizedName())
                            .setStyle(new Style().setColor(TextFormatting.AQUA))
                    .appendText(" ").appendSibling(new TextComponentString(String.valueOf(fluid.amount)))));

            player.sendMessage(new TextComponentTranslation("metaitem.void_plunger.message.success")
                    .appendText("\n").appendSibling(importItemText)
                    .appendText("\n").appendSibling(importFluidText)
                    .appendText("\n").appendSibling(exportItemText)
                    .appendText("\n").appendSibling(exportFluidText));
        }
        return EnumActionResult.SUCCESS;
    }

    private List<ItemStack> listItems(IItemHandlerModifiable itemHandler, boolean voiding, EntityPlayer player) {
        NonNullList<ItemStack> stackList = NonNullList.create();
        NonNullList<ItemStack> uniqueItems = NonNullList.create();
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            ItemStack stack = itemHandler.getStackInSlot(i);
            if (!stack.isEmpty() && !this.containsItem(uniqueItems, stack))
                uniqueItems.add(stack);
        }

        for (ItemStack uniqueStack : uniqueItems) {
            int count = 0;
            for (int i = 0; i < itemHandler.getSlots(); i++) {
                ItemStack stack = itemHandler.getStackInSlot(i).copy();
                if (stack.isItemEqual(uniqueStack)) {
                    count += stack.getCount();
                    if (voiding) {
                        itemHandler.setStackInSlot(i, ItemStack.EMPTY);
                    } else {
                        ItemStack reminder = ItemStackHelper.insertInMainInventory(player.inventory, stack);
                        itemHandler.setStackInSlot(i, reminder);
                        count -= reminder.getCount();
                    }
                }
            }
            ItemStack displayStack = uniqueStack.copy();
            displayStack.setCount(count);
            stackList.add(displayStack);
        }
        return stackList;
    }

    private boolean containsItem(List<ItemStack> stackList, ItemStack stack) {
        for (ItemStack otherStack : stackList) {
            if (otherStack.isItemEqual(stack))
                return true;
        }
        return false;
    }

    private List<FluidStack> listFluids(IMultipleTankHandler fluidHandler, boolean voiding) {
        List<FluidStack> stackList = new ArrayList<>();
        List<FluidStack> uniqueFluids = new ArrayList<>();
        for (int i = 0; i < fluidHandler.getTanks(); i++) {
            FluidStack fluid = fluidHandler.getTankAt(i).getFluid();
            if (fluid != null && !this.containsFluid(uniqueFluids, fluid))
                uniqueFluids.add(fluid);
        }

        for (FluidStack uniqueFluid : uniqueFluids) {
            int amount = 0;
            for (int i = 0; i < fluidHandler.getTanks(); i++) {
                FluidStack fluid = fluidHandler.getTankAt(i).getFluid();
                if (fluid != null && fluid.isFluidEqual(uniqueFluid)) {
                    amount += fluid.amount;
                    fluidHandler.getTankAt(i).drain(Integer.MAX_VALUE, true);
                }
            }
            FluidStack displayStack = uniqueFluid.copy();
            displayStack.amount = amount;
            stackList.add(displayStack);
        }
        return stackList;
    }

    private boolean containsFluid(List<FluidStack> stackList, FluidStack stack) {
        for (FluidStack otherStack : stackList) {
            if (otherStack.isFluidEqual(stack))
                return true;
        }
        return false;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        NBTTagCompound nbt = player.getHeldItem(hand).getOrCreateSubCompound("Plunger");
        boolean voiding = nbt.getBoolean("Void");
        nbt.setBoolean("Void", !voiding);
        if (world.isRemote)
            player.sendMessage(new TextComponentTranslation("metaitem.void_plunger.mode", !voiding));
        return ActionResult.newResult(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
        NBTTagCompound nbt = itemStack.getOrCreateSubCompound("Plunger");
        boolean voiding = nbt.getBoolean("Void");
        lines.add(I18n.format("metaitem.void_plunger.description"));
        lines.add(I18n.format("metaitem.void_plunger.mode", voiding));
    }
}
