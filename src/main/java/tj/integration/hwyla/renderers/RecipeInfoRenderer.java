package tj.integration.hwyla.renderers;

import it.unimi.dsi.fastutil.objects.*;
import mcp.mobius.waila.api.IWailaCommonAccessor;
import mcp.mobius.waila.api.IWailaTooltipRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fluids.FluidStack;
import tj.mui.TJGuiUtils;
import tj.util.map.Strategies;
import tj.util.wrappers.GTFluidStackWrapper;
import tj.util.wrappers.GTItemStackWrapper;

import javax.annotation.Nonnull;
import java.awt.*;

public class RecipeInfoRenderer implements IWailaTooltipRenderer {

    @Nonnull
    @Override
    public Dimension getSize(@Nonnull String[] strings, @Nonnull IWailaCommonAccessor iWailaCommonAccessor) {
        final NBTTagCompound compound = iWailaCommonAccessor.getNBTData().getCompoundTag("tj.recipeinfo");
        final NBTTagList items = compound.getTagList(strings[0].equals("input") ? "itemInputs" : "itemOutputs", 10);
        final NBTTagList fluids = compound.getTagList(strings[0].equals("input") ? "fluidInputs" : "fluidOutputs", 10);
        final Object2ObjectMap<ItemStack, GTItemStackWrapper> itemMap = new Object2ObjectOpenCustomHashMap<>(Strategies.ITEMSTACK_STRATEGY);
        final Object2ObjectMap<FluidStack, GTFluidStackWrapper> fluidMap = new Object2ObjectOpenHashMap<>();
        for (int i = 0; i < items.tagCount(); i++) {
            final ItemStack itemStack = new ItemStack(items.getCompoundTagAt(i));
            itemMap.computeIfAbsent(itemStack, key -> new GTItemStackWrapper(key, 0));
        }
        for (int i = 0; i < fluids.tagCount(); i++) {
            final FluidStack fluidStack = FluidStack.loadFluidStackFromNBT(fluids.getCompoundTagAt(i));
            if (fluidStack == null) continue;
            fluidMap.computeIfAbsent(fluidStack, key -> new GTFluidStackWrapper(key, 0));
        }
        return new Dimension(18 * (itemMap.size() + fluidMap.size()), 18);
    }

    @Override
    public void draw(@Nonnull String[] strings, @Nonnull IWailaCommonAccessor iWailaCommonAccessor) {
        final NBTTagCompound compound = iWailaCommonAccessor.getNBTData().getCompoundTag("tj.recipeinfo");
        final NBTTagList items = compound.getTagList(strings[0].equals("input") ? "itemInputs" : "itemOutputs", 10);
        final NBTTagList fluids = compound.getTagList(strings[0].equals("input") ? "fluidInputs" : "fluidOutputs", 10);
        final Object2ObjectMap<ItemStack, GTItemStackWrapper> itemMap = new Object2ObjectLinkedOpenCustomHashMap<>(Strategies.ITEMSTACK_STRATEGY);
        final Object2ObjectMap<FluidStack, GTFluidStackWrapper> fluidMap = new Object2ObjectLinkedOpenHashMap<>();
        for (int i = 0; i < items.tagCount(); i++) {
            final ItemStack itemStack = new ItemStack(items.getCompoundTagAt(i));
            itemMap.computeIfAbsent(itemStack, key -> new GTItemStackWrapper(key, 0))
                    .increment(itemStack.getCount());
        }
        for (int i = 0; i < fluids.tagCount(); i++) {
            final FluidStack fluidStack = FluidStack.loadFluidStackFromNBT(fluids.getCompoundTagAt(i));
            if (fluidStack == null) continue;
            fluidMap.computeIfAbsent(fluidStack, key -> new GTFluidStackWrapper(key, 0))
                    .increment(fluidStack.amount);
        }
        int offsetX = 0;
        for (Object2ObjectMap.Entry<ItemStack, GTItemStackWrapper> entry : itemMap.object2ObjectEntrySet())
            TJGuiUtils.drawItemStack(18 * offsetX++, 0, entry.getKey(), entry.getValue().getCountLong());
        for (Object2ObjectMap.Entry<FluidStack, GTFluidStackWrapper> entry : fluidMap.object2ObjectEntrySet())
            TJGuiUtils.drawFluidStack(18 * offsetX++, 0, entry.getKey(), entry.getValue().getCountLong());
    }
}
