package tj.integration.hwyla.providers;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.IWailaRegistrar;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public abstract class CapabilityInfoDataProvider<T> implements IWailaDataProvider {

    public void register(IWailaRegistrar registrar) {
        registrar.addConfig("TJ", this.getConfigName());
    }

    public abstract Capability<T> getCapability();

    public abstract String getConfigName();

    @Nonnull
    public List<String> getWailaHead(ItemStack itemStack, List<String> tooltip, IWailaDataAccessor accessor, IWailaConfigHandler config, T capability) {
        return tooltip;
    }

    @Nonnull
    public List<String> getWailaBody(ItemStack itemStack, List<String> tooltip, IWailaDataAccessor accessor, IWailaConfigHandler config, T capability) {
        return tooltip;
    }

    @Nonnull
    public List<String> getWailaTail(ItemStack itemStack, List<String> tooltip, IWailaDataAccessor accessor, IWailaConfigHandler config, T capability) {
        return tooltip;
    }

    @Nonnull
    public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, BlockPos pos, T capability) {
        return tag;
    }

    @Nonnull
    @Override
    public final List<String> getWailaHead(ItemStack itemStack, List<String> tooltip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        if (!config.getConfig(this.getConfigName()))
            return tooltip;
        if (accessor.getTileEntity() == null)
            return tooltip;
        final T capability = accessor.getTileEntity().getCapability(this.getCapability(), null);
        if (capability != null) {
            List<String> tooltips = new ArrayList<>();
            tooltips = this.getWailaHead(itemStack, tooltips, accessor, config, capability);
            tooltip.addAll(tooltips);
        }
        return tooltip;
    }

    @Nonnull
    @Override
    public final List<String> getWailaBody(ItemStack itemStack, List<String> tooltip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        if (!config.getConfig(this.getConfigName()))
            return tooltip;
        if (accessor.getTileEntity() == null)
            return tooltip;
        final T capability = accessor.getTileEntity().getCapability(this.getCapability(), null);
        if (capability != null) {
            List<String> tooltips = new ArrayList<>();
            tooltips = this.getWailaBody(itemStack, tooltips, accessor, config, capability);
            tooltip.addAll(tooltips);
        }
        return tooltip;
    }

    @Nonnull
    @Override
    public final List<String> getWailaTail(ItemStack itemStack, List<String> tooltip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        if (!config.getConfig(this.getConfigName()))
            return tooltip;
        if (accessor.getTileEntity() == null)
            return tooltip;
        final T capability = accessor.getTileEntity().getCapability(this.getCapability(), null);
        if (capability != null) {
            List<String> tooltips = new ArrayList<>();
            tooltips = this.getWailaTail(itemStack, tooltips, accessor, config, capability);
            tooltip.addAll(tooltips);
        }
        return tooltip;
    }

    @Nonnull
    @Override
    public final NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, BlockPos pos) {
        final T capability = te.getCapability(this.getCapability(), null);
        return capability != null ? this.getNBTData(player, te, tag, world, pos, capability) : tag;
    }
}
