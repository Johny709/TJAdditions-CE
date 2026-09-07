package tj.integration.hwyla.providers;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IWorkable;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaRegistrar;
import mcp.mobius.waila.api.SpecialChars;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import tj.util.Color;

import javax.annotation.Nonnull;
import java.util.List;

public class CoverProgressInfoDataProvider extends CoverCapabilityInfoDataProvider<IWorkable> {

    public static final CoverProgressInfoDataProvider INSTANCE = new CoverProgressInfoDataProvider();

    public void register(IWailaRegistrar registrar) {
        registrar.registerNBTProvider(this, TileEntity.class);
        registrar.registerBodyProvider(this, TileEntity.class);
        registrar.addConfig("TJ", this.getConfigName());
    }

    @Override
    public Capability<IWorkable> getCapability() {
        return GregtechTileCapabilities.CAPABILITY_WORKABLE;
    }

    @Override
    public String getConfigName() {
        return "tj.cover.progressinfo";
    }

    @Nonnull
    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, BlockPos pos, IWorkable workable) {
        final NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger("progress", workable.getProgress());
        compound.setInteger("maxProgress", workable.getMaxProgress());
        tag.setTag("tj.progressinfo", compound);
        return tag;
    }

    @Nonnull
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> tooltip, IWailaDataAccessor accessor, IWailaConfigHandler config, IWorkable capability) {
        final NBTTagCompound compound = accessor.getNBTData().getCompoundTag(getCapabilitySideTag(accessor.getSide())).getCompoundTag("tj.progressinfo");
        final double maxProgress = (double) compound.getInteger("maxProgress") / 20;
        final double progress = Math.min(maxProgress, (double) compound.getInteger("progress") / 20);
        tooltip.add(SpecialChars.getRenderString("tj.progressinfo", I18n.format("gregtech.top.progress"),
                String.valueOf(progress), String.valueOf(maxProgress), "s", "s", Color.GREEN.toString(), ",##0.00"));
        return tooltip;
    }
}
