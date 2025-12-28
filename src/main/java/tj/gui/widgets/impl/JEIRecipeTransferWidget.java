package tj.gui.widgets.impl;

import gregtech.api.gui.Widget;
import gregtech.api.gui.impl.ModularUIContainer;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import mezz.jei.api.gui.IGuiIngredient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tj.gui.widgets.IGTRecipeTransferHandler;
import tj.util.consumers.QuintConsumer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class JEIRecipeTransferWidget extends Widget implements IGTRecipeTransferHandler {

    private QuintConsumer<List<ItemStack>, List<ItemStack>, List<FluidStack>, List<FluidStack>, EntityPlayer> recipeConsumer;

    public JEIRecipeTransferWidget(int x, int y, int width, int height) {
        super(new Position(x, y), new Size(width, height));
    }

    public JEIRecipeTransferWidget setRecipeConsumer(QuintConsumer<List<ItemStack>, List<ItemStack>, List<FluidStack>, List<FluidStack>, EntityPlayer> recipeConsumer) {
        this.recipeConsumer = recipeConsumer;
        return this;
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        super.handleClientAction(id, buffer);
        if (id == 1) {
            NonNullList<ItemStack> itemInputs = NonNullList.create();
            NonNullList<ItemStack> itemOutputs = NonNullList.create();
            List<FluidStack> fluidInputs = new ArrayList<>();
            List<FluidStack> fluidOutputs = new ArrayList<>();
            int itemIngredientAmount = buffer.readVarInt();
            int fluidIngredientAmount = buffer.readVarInt();
            try {
                for (int i = 0; i < itemIngredientAmount; i++) {
                    ItemStack itemStack = buffer.readItemStack();
                    if (buffer.readString(Short.MAX_VALUE).equals("Input")) {
                        itemInputs.add(itemStack);
                    } else itemOutputs.add(itemStack);
                }
                for (int i = 0; i < fluidIngredientAmount; i++) {
                    FluidStack fluidStack = FluidStack.loadFluidStackFromNBT(buffer.readCompoundTag());
                    if (buffer.readString(Short.MAX_VALUE).equals("Input")) {
                        fluidInputs.add(fluidStack);
                    } else fluidOutputs.add(fluidStack);
                }
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
            this.recipeConsumer.accept(itemInputs, itemOutputs, fluidInputs, fluidOutputs, gui.entityPlayer);
        }
    }


    @Override
    @SideOnly(Side.CLIENT)
    public String transferRecipe(ModularUIContainer container, Map<Integer, IGuiIngredient<ItemStack>> itemIngredients, Map<Integer, IGuiIngredient<FluidStack>> fluidIngredients, EntityPlayer player, boolean maxTransfer, boolean doTransfer) {
        if (!doTransfer) {
            return null;
        }
        this.writeClientAction(1, buffer -> {
            buffer.writeVarInt(itemIngredients.size());
            buffer.writeVarInt(fluidIngredients.size());
            for (Map.Entry<Integer, IGuiIngredient<ItemStack>> entry : itemIngredients.entrySet()) {
                ItemStack itemStack = entry.getValue().getDisplayedIngredient();
                buffer.writeItemStack(itemStack);
                if (entry.getValue().isInput())
                    buffer.writeString("Input");
                else buffer.writeString("Output");
            }
            for (Map.Entry<Integer, IGuiIngredient<FluidStack>> entry : fluidIngredients.entrySet()) {
                FluidStack fluidStack = entry.getValue().getDisplayedIngredient();
                buffer.writeCompoundTag(fluidStack.writeToNBT(new NBTTagCompound()));
                if (entry.getValue().isInput())
                    buffer.writeString("Input");
                else buffer.writeString("Output");
            }
        });
        return null;
    }
}
