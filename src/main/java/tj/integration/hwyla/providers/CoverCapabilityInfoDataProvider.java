package tj.integration.hwyla.providers;

import gregtech.api.cover.CoverBehavior;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public abstract class CoverCapabilityInfoDataProvider<T> implements IWailaDataProvider {

    public abstract Capability<T> getCapability();

    public abstract String getConfigName();

    @Nonnull
    public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, BlockPos pos, T capability) {
        return tag;
    }

    @Nonnull
    public List<String> getWailaBody(ItemStack itemStack, List<String> tooltip, IWailaDataAccessor accessor, IWailaConfigHandler config, T capability) {
        return tooltip;
    }

    @Nonnull
    @Override
    public final NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, BlockPos pos) {
        if (!(te instanceof MetaTileEntityHolder))
            return tag;
        final MetaTileEntity metaTileEntity = ((MetaTileEntityHolder) te).getMetaTileEntity();
        if (metaTileEntity == null)
            return tag;
        for (EnumFacing facing : EnumFacing.VALUES) {
            final T coverCapability = metaTileEntity.getCoverCapability(this.getCapability(), facing);
            if (coverCapability == null) continue;
            NBTTagCompound compound = new NBTTagCompound();
            compound = this.getNBTData(player, te, compound, world, pos, coverCapability);
            tag.setTag(getCapabilitySideTag(facing), compound);
        }
        return tag;
    }

    @Nonnull
    @Override
    public final List<String> getWailaBody(ItemStack itemStack, List<String> tooltip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        if (!config.getConfig(this.getConfigName()))
            return tooltip;
        if (!(accessor.getTileEntity() instanceof MetaTileEntityHolder))
            return tooltip;
        final MetaTileEntity metaTileEntity = ((MetaTileEntityHolder) accessor.getTileEntity()).getMetaTileEntity();
        if (metaTileEntity == null)
            return tooltip;
        if (metaTileEntity.getCoverAtSide(accessor.getSide()) == null)
            return tooltip;
        final T coverCapability = metaTileEntity.getCoverCapability(this.getCapability(), accessor.getSide());
        if (coverCapability != null) {
            List<String> tooltips = new ArrayList<>();
            tooltips = this.getWailaBody(itemStack, tooltips, accessor, config, coverCapability);
            tooltip.addAll(tooltips);
        }
        return tooltip;
    }

    public static String getCapabilitySideTag(EnumFacing side) {
        return "capabilitySide:" + side.getName();
    }
}
