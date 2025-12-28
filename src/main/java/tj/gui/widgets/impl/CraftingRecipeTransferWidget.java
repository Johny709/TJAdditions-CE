package tj.gui.widgets.impl;

import com.google.common.base.Preconditions;
import gregtech.api.gui.Widget;
import gregtech.api.gui.igredient.IRecipeTransferHandlerWidget;
import gregtech.api.gui.impl.ModularUIContainer;
import gregtech.api.util.GTLog;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import mezz.jei.api.gui.IGuiIngredient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class CraftingRecipeTransferWidget extends Widget implements IRecipeTransferHandlerWidget {

    private final BiConsumer<Integer, ItemStack> itemStackBiConsumer;

    public CraftingRecipeTransferWidget(BiConsumer<Integer, ItemStack> itemStackBiConsumer) {
        super(new Position(1, 1), new Size(1, 1));
        this.itemStackBiConsumer = itemStackBiConsumer;
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        if (id == 1) {
            Map<Integer, ItemStack> itemStackMap = new HashMap<>();
            for (int i = 0; i < 9; i++)
                itemStackMap.put(i, ItemStack.EMPTY);
            try {
                int size = buffer.readVarInt();
                for (int i = 0; i < size; i++)
                    itemStackMap.put(buffer.readVarInt(), buffer.readItemStack());
                for (Map.Entry<Integer, ItemStack> itemStackEntry : itemStackMap.entrySet())
                    this.itemStackBiConsumer.accept(itemStackEntry.getKey(), itemStackEntry.getValue());
            } catch (IOException e) {
                GTLog.logger.error(e);
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public String transferRecipe(ModularUIContainer container, Map<Integer, IGuiIngredient<ItemStack>> ingredients, EntityPlayer player, boolean maxTransfer, boolean doTransfer) {
        if (!doTransfer) {
            return null;
        }
        ingredients.values().removeIf(it -> !it.isInput());
        writeClientAction(1, buf -> {
            buf.writeVarInt(ingredients.size());
            for (Map.Entry<Integer, IGuiIngredient<ItemStack>> entry : ingredients.entrySet()) {
                buf.writeVarInt(entry.getKey() - 1);
                ItemStack itemStack = entry.getValue().getDisplayedIngredient();
                Preconditions.checkNotNull(itemStack);
                buf.writeItemStack(itemStack);
            }
        });
        return null;
    }
}
