package tj.integration.hwyla.providers;

import gregicadditions.GAValues;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import mcp.mobius.waila.api.*;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import tj.TJValues;
import tj.capability.IRecipeInfo;
import tj.capability.TJCapabilities;
import tj.util.TJUtility;

import javax.annotation.Nonnull;
import java.util.List;

public class RecipeInfoDataProvider implements IWailaDataProvider {

    public static final RecipeInfoDataProvider INSTANCE = new RecipeInfoDataProvider();

    public void register(IWailaRegistrar registrar) {
        registrar.registerBodyProvider(this, TileEntity.class);
        registrar.registerNBTProvider(this, TileEntity.class);
        registrar.addConfig("TJ", "tj.recipeinfo");
    }

    @Nonnull
    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, BlockPos pos) {
        if (!(te instanceof MetaTileEntityHolder))
            return tag;
        final MetaTileEntity metaTileEntity = ((MetaTileEntityHolder) te).getMetaTileEntity();
        if (metaTileEntity == null)
            return tag;
        final IRecipeInfo recipeInfo = metaTileEntity.getCapability(TJCapabilities.CAPABILITY_RECIPE_INFO, null);
        if (recipeInfo == null)
            return tag;
        final NBTTagCompound compound = new NBTTagCompound();
        final NBTTagList itemInputs = new NBTTagList();
        final NBTTagList itemOutputs = new NBTTagList();
        final NBTTagList fluidInputs = new NBTTagList();
        final NBTTagList fluidOutputs = new NBTTagList();
        for (ItemStack input : recipeInfo.getItemInputs())
            itemInputs.appendTag(input.serializeNBT());
        for (ItemStack output : recipeInfo.getItemOutputs())
            itemOutputs.appendTag(output.serializeNBT());
        for (FluidStack input : recipeInfo.getFluidInputs())
            fluidInputs.appendTag(input.writeToNBT(new NBTTagCompound()));
        for (FluidStack output : recipeInfo.getFluidOutputs())
            fluidOutputs.appendTag(output.writeToNBT(new NBTTagCompound()));
        compound.setTag("itemInputs", itemInputs);
        compound.setTag("itemOutputs", itemOutputs);
        compound.setTag("fluidInputs", fluidInputs);
        compound.setTag("fluidOutputs", fluidOutputs);
        compound.setLong("energyPerTick", recipeInfo.getEnergyPerTick());
        compound.setBoolean("active", recipeInfo.isActive());
        compound.setBoolean("problem", recipeInfo.isHasProblems());
        tag.setTag("tj.recipeinfo", compound);
        return tag;
    }

    @Nonnull
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> tooltip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        if (!config.getConfig("tj.recipeinfo"))
            return tooltip;
        if (!(accessor.getTileEntity() instanceof MetaTileEntityHolder))
            return tooltip;
        final MetaTileEntity metaTileEntity = ((MetaTileEntityHolder) accessor.getTileEntity()).getMetaTileEntity();
        if (metaTileEntity == null)
            return tooltip;
        final IRecipeInfo recipeInfo = metaTileEntity.getCapability(TJCapabilities.CAPABILITY_RECIPE_INFO, accessor.getSide());
        if (recipeInfo == null)
            return tooltip;
        final NBTTagCompound compound = accessor.getNBTData().getCompoundTag("tj.recipeinfo");
        final long energyPerTick = compound.getLong("energyPerTick");
        final int tier = TJUtility.getTierFromVoltage(energyPerTick);
        if (energyPerTick > 0)
            tooltip.add(I18n.format("tj.multiblock.eu",
                    TJValues.thousandFormat.format(energyPerTick),
                    tier > 14 ? "§c§lM§e§lA§a§lX§b§l+§d§l" + (tier - 14) : TJValues.VCC[tier] + GAValues.VN[tier]));
        if (compound.getBoolean("problem")) {
            tooltip.add(I18n.format("machine.universal.has_problems"));
        } else if (compound.getBoolean("active")) {
            tooltip.add(I18n.format("machine.universal.running"));
        }
        final NBTTagList itemInputs = compound.getTagList("itemInputs", 10);
        final NBTTagList itemOutputs = compound.getTagList("itemOutputs", 10);
        final NBTTagList fluidInputs = compound.getTagList("fluidInputs", 10);
        final NBTTagList fluidOutputs = compound.getTagList("fluidOutputs", 10);
        if (!itemInputs.isEmpty() || !fluidInputs.isEmpty()) {
            tooltip.add(I18n.format("tj.top.inputs"));
            tooltip.add(SpecialChars.getRenderString("tj.recipeinfo", "input"));
        }
        if (!itemOutputs.isEmpty() || !fluidOutputs.isEmpty()) {
            tooltip.add(I18n.format("tj.top.outputs"));
            tooltip.add(SpecialChars.getRenderString("tj.recipeinfo", "output"));
        }
        return tooltip;
    }
}
