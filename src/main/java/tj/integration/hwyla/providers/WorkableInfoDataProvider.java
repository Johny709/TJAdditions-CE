package tj.integration.hwyla.providers;

import gregicadditions.GAValues;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IWorkable;
import gregtech.api.capability.impl.AbstractRecipeLogic;
import mcp.mobius.waila.api.*;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import tj.TJValues;
import tj.mixin.gregtech.IMixinAbstractRecipeLogic;
import tj.util.Color;
import tj.util.TJUtility;

import javax.annotation.Nonnull;
import java.util.List;

public class WorkableInfoDataProvider extends CapabilityInfoDataProvider<IWorkable> {

    public static final WorkableInfoDataProvider INSTANCE = new WorkableInfoDataProvider();

    @Override
    public void register(IWailaRegistrar registrar) {
        super.register(registrar);
        registrar.registerNBTProvider(this, TileEntity.class);
        registrar.registerBodyProvider(this, TileEntity.class);
    }

    @Override
    public Capability<IWorkable> getCapability() {
        return GregtechTileCapabilities.CAPABILITY_WORKABLE;
    }

    @Override
    public String getConfigName() {
        return "tj.workableinfo";
    }

    @Nonnull
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> tooltip, IWailaDataAccessor accessor, IWailaConfigHandler config, IWorkable workable) {
        final NBTTagCompound compound = accessor.getNBTData().getCompoundTag(this.getConfigName());
        final double maxProgress = (double) compound.getInteger("maxProgress") / 20;
        final double progress = Math.min(maxProgress, (double) compound.getInteger("progress") / 20);
        tooltip.add(SpecialChars.getRenderString("tj.progressinfo", I18n.format("gregtech.top.progress"),
                String.valueOf(progress), String.valueOf(maxProgress), "s", "s", Color.GREEN.toString(), ",##0.00"));
        if (workable instanceof AbstractRecipeLogic) {
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
        }
        return tooltip;
    }

    @Nonnull
    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, BlockPos pos, IWorkable workable) {
        final NBTTagCompound compound = new NBTTagCompound();
        if (workable instanceof AbstractRecipeLogic) {
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
            tag.setTag("tj.recipeinfo", recipeCompound);
        }
        compound.setInteger("progress", workable.getProgress());
        compound.setInteger("maxProgress", workable.getMaxProgress());
        tag.setTag(this.getConfigName(), compound);
        return tag;
    }
}
