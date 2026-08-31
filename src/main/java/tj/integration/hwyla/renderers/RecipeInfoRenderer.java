package tj.integration.hwyla.renderers;

import mcp.mobius.waila.api.IWailaCommonAccessor;
import mcp.mobius.waila.api.IWailaTooltipRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fluids.FluidStack;
import tj.mui.TJGuiUtils;

import javax.annotation.Nonnull;
import java.awt.*;

public class RecipeInfoRenderer implements IWailaTooltipRenderer {

    @Nonnull
    @Override
    public Dimension getSize(@Nonnull String[] strings, @Nonnull IWailaCommonAccessor iWailaCommonAccessor) {
        final NBTTagCompound compound = iWailaCommonAccessor.getNBTData().getCompoundTag("tj.recipeinfo");
        final NBTTagList items = compound.getTagList(strings[0].equals("input") ? "itemInputs" : "itemOutputs", 10);
        final NBTTagList fluids = compound.getTagList(strings[0].equals("input") ? "fluidInputs" : "fluidOutputs", 10);
        return new Dimension(18 * (items.tagCount() + fluids.tagCount()), 18);
    }

    @Override
    public void draw(@Nonnull String[] strings, @Nonnull IWailaCommonAccessor iWailaCommonAccessor) {
        final NBTTagCompound compound = iWailaCommonAccessor.getNBTData().getCompoundTag("tj.recipeinfo");
        final NBTTagList items = compound.getTagList(strings[0].equals("input") ? "itemInputs" : "itemOutputs", 10);
        final NBTTagList fluids = compound.getTagList(strings[0].equals("input") ? "fluidInputs" : "fluidOutputs", 10);
        int offsetX = 0;
        for (int i = 0; i < items.tagCount(); i++) {
            TJGuiUtils.drawItemStack(new ItemStack(items.getCompoundTagAt(i)), 18 * offsetX++, 0);
        }
        for (int i = 0; i < fluids.tagCount(); i++) {
            TJGuiUtils.drawFluidStack(18 * offsetX++, 0, FluidStack.loadFluidStackFromNBT(fluids.getCompoundTagAt(i)));
        }
    }
}
