package tj.integration.hwyla.providers;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
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
import tj.capability.IParallelItemFluidHandlerInfo;
import tj.capability.TJCapabilities;

import javax.annotation.Nonnull;
import java.util.List;

public class ParallelItemFluidHandlerInfoDataProvider implements IWailaDataProvider {

    public static final ParallelItemFluidHandlerInfoDataProvider INSTANCE = new ParallelItemFluidHandlerInfoDataProvider();

    public void register(IWailaRegistrar registrar) {
        registrar.registerNBTProvider(this, TileEntity.class);
        registrar.registerBodyProvider(this, TileEntity.class);
        registrar.addConfig("TJ", "tj.item_fluid_handler");
    }

    @Nonnull
    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, BlockPos pos) {
        if (!(te instanceof MetaTileEntityHolder))
            return tag;
        final MetaTileEntity metaTileEntity = ((MetaTileEntityHolder) te).getMetaTileEntity();
        if (metaTileEntity == null)
            return tag;
        final IParallelItemFluidHandlerInfo itemFluidHandlerInfo = metaTileEntity.getCapability(TJCapabilities.CAPABILITY_PARALLEL_ITEM_FLUID_HANDLING, null);
        if (itemFluidHandlerInfo == null)
            return tag;
        final NBTTagCompound compound = new NBTTagCompound();
        final NBTTagList itemInputs = new NBTTagList();
        final NBTTagList itemOutputs = new NBTTagList();
        final NBTTagList fluidInputs = new NBTTagList();
        final NBTTagList fluidOutputs = new NBTTagList();
        for (Int2ObjectMap.Entry<List<ItemStack>> entry : itemFluidHandlerInfo.getAllItemInputs().int2ObjectEntrySet())
            for (ItemStack input : entry.getValue())
                itemInputs.appendTag(input.serializeNBT());
        for (Int2ObjectMap.Entry<List<ItemStack>> entry : itemFluidHandlerInfo.getAllItemOutputs().int2ObjectEntrySet())
            for (ItemStack output : entry.getValue())
                itemOutputs.appendTag(output.serializeNBT());
        for (Int2ObjectMap.Entry<List<FluidStack>> entry : itemFluidHandlerInfo.getAllFluidInputs().int2ObjectEntrySet())
            for (FluidStack input : entry.getValue())
                fluidInputs.appendTag(input.writeToNBT(new NBTTagCompound()));
        for (Int2ObjectMap.Entry<List<FluidStack>> entry : itemFluidHandlerInfo.getAllFluidOutputs().int2ObjectEntrySet())
            for (FluidStack output : entry.getValue())
                fluidOutputs.appendTag(output.writeToNBT(new NBTTagCompound()));
        compound.setTag("itemInputs", itemInputs);
        compound.setTag("itemOutputs", itemOutputs);
        compound.setTag("fluidInputs", fluidInputs);
        compound.setTag("fluidOutputs", fluidOutputs);
        tag.setTag("tj.item_fluid_handler", compound);
        return tag;
    }

    @Nonnull
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> tooltip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        if (!config.getConfig("tj.item_fluid_handler"))
            return tooltip;
        if (!(accessor.getTileEntity() instanceof MetaTileEntityHolder))
            return tooltip;
        final MetaTileEntity metaTileEntity = ((MetaTileEntityHolder) accessor.getTileEntity()).getMetaTileEntity();
        if (metaTileEntity == null)
            return tooltip;
        final IParallelItemFluidHandlerInfo itemFluidHandlerInfo = metaTileEntity.getCapability(TJCapabilities.CAPABILITY_PARALLEL_ITEM_FLUID_HANDLING, null);
        if (itemFluidHandlerInfo == null)
            return tooltip;
        final NBTTagCompound compound = accessor.getNBTData().getCompoundTag("tj.item_fluid_handler");
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
