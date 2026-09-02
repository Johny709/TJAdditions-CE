package tj.integration.hwyla.providers;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IWorkable;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import mcp.mobius.waila.api.*;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.List;

public class ProgressInfoDataProvider implements IWailaDataProvider {

    public static final ProgressInfoDataProvider INSTANCE = new ProgressInfoDataProvider();

    public void register(IWailaRegistrar registrar) {
        registrar.registerNBTProvider(this, TileEntity.class);
        registrar.registerBodyProvider(this, TileEntity.class);
        registrar.addConfig("TJ", "tj.progressinfo");
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
        if (workable == null)
            return tag;
        final NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger("progress", workable.getProgress());
        compound.setInteger("maxProgress", workable.getMaxProgress());
        tag.setTag("tj.progressinfo", compound);
        return tag;
    }

    @Nonnull
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> tooltip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        if (!config.getConfig("tj.progressinfo"))
            return tooltip;
        if (!(accessor.getTileEntity() instanceof MetaTileEntityHolder))
            return tooltip;
        final MetaTileEntity metaTileEntity = ((MetaTileEntityHolder) accessor.getTileEntity()).getMetaTileEntity();
        if (metaTileEntity == null)
            return tooltip;
        final IWorkable workable = metaTileEntity.getCapability(GregtechTileCapabilities.CAPABILITY_WORKABLE, null);
        if (workable == null)
            return tooltip;
        final NBTTagCompound compound = accessor.getNBTData().getCompoundTag("tj.progressinfo");
        final double maxProgress = (double) compound.getInteger("maxProgress") / 20;
        final double progress = Math.min(maxProgress, (double) compound.getInteger("progress") / 20);
        tooltip.add(SpecialChars.getRenderString("tj.progressinfo", I18n.format("gregtech.top.progress"),
                String.valueOf(progress), String.valueOf(maxProgress), "s", "s", "GREEN", ",##0.00"));
        return tooltip;
    }
}
