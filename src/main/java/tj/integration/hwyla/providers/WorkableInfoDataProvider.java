package tj.integration.hwyla.providers;

import gregicadditions.GAValues;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IWorkable;
import gregtech.api.capability.impl.AbstractRecipeLogic;
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
import tj.mixin.gregtech.IMixinAbstractRecipeLogic;
import tj.util.TJUtility;

import javax.annotation.Nonnull;
import java.util.List;

public class WorkableInfoDataProvider implements IWailaDataProvider {

    public static final WorkableInfoDataProvider INSTANCE = new WorkableInfoDataProvider();

    public void register(IWailaRegistrar registrar) {
        registrar.registerNBTProvider(this, TileEntity.class);
        registrar.registerBodyProvider(this, TileEntity.class);
        registrar.addConfig("TJ", "tj.workable");
    }

    @Nonnull
    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, BlockPos pos) {
        if (!(te instanceof MetaTileEntityHolder))
            return tag;
        final MetaTileEntity metaTileEntity = ((MetaTileEntityHolder) te).getMetaTileEntity();
        if (metaTileEntity == null)
            return tag;
        final IWorkable workable = metaTileEntity.getCapability(GregtechTileCapabilities.CAPABILITY_WORKABLE, null);
        if (!(workable instanceof AbstractRecipeLogic))
            return tag;
        final NBTTagCompound compound = new NBTTagCompound();
        final NBTTagCompound recipeCompound = new NBTTagCompound();
        final NBTTagList itemOutputs = new NBTTagList();
        final NBTTagList fluidOutputs = new NBTTagList();
        for (ItemStack item : ((IMixinAbstractRecipeLogic) workable).getItemOutputs())
            itemOutputs.appendTag(item.serializeNBT());
        for (FluidStack fluid : ((IMixinAbstractRecipeLogic) workable).getFluidOutputs())
            fluidOutputs.appendTag(fluid.writeToNBT(new NBTTagCompound()));
        recipeCompound.setTag("itemOutputs", itemOutputs);
        recipeCompound.setTag("fluidOutputs", fluidOutputs);
        compound.setBoolean("active", workable.isActive());
        compound.setInteger("eut", ((AbstractRecipeLogic) workable).getRecipeEUt());
        tag.setTag("tj.workable", compound);
        tag.setTag("tj.recipeinfo", recipeCompound);
        return tag;
    }

    @Nonnull
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> tooltip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        if (!config.getConfig("tj.workable"))
            return tooltip;
        if (!(accessor.getTileEntity() instanceof MetaTileEntityHolder))
            return tooltip;
        final MetaTileEntity metaTileEntity = ((MetaTileEntityHolder) accessor.getTileEntity()).getMetaTileEntity();
        if (metaTileEntity == null)
            return tooltip;
        final IWorkable workable = metaTileEntity.getCapability(GregtechTileCapabilities.CAPABILITY_WORKABLE, null);
        if (!(workable instanceof AbstractRecipeLogic))
            return tooltip;
        final NBTTagCompound compound = accessor.getNBTData().getCompoundTag("tj.workable");
        final NBTTagCompound recipeCompound = accessor.getNBTData().getCompoundTag("tj.recipeinfo");
        final int recipeEUt = compound.getInteger("eut");
        final int tier = TJUtility.getTierFromVoltage(recipeEUt);
        if (compound.hasKey("eut"))
            tooltip.add(I18n.format("tj.multiblock.eu", TJValues.thousandFormat.format(recipeEUt),
                    tier > 14 ? "§c§lM§e§lA§a§lX§b§l+§d§l" + (tier - 14) : TJValues.VCC[tier] + GAValues.VN[tier]));
        if (compound.getBoolean("active"))
            tooltip.add(I18n.format("machine.universal.running"));
        final NBTTagList itemOutputs = recipeCompound.getTagList("itemOutputs", 10);
        final NBTTagList fluidOutputs = recipeCompound.getTagList("fluidOutputs", 10);
        if (!itemOutputs.isEmpty() || !fluidOutputs.isEmpty()) {
            tooltip.add(I18n.format("tj.top.outputs"));
            tooltip.add(SpecialChars.getRenderString("tj.recipeinfo", "output"));
        }
        return tooltip;
    }
}
