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

public class CoverWorkableInfoDataProvider extends CoverCapabilityInfoDataProvider<IWorkable> {

    public static final CoverWorkableInfoDataProvider INSTANCE = new CoverWorkableInfoDataProvider();

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
        return "tj.cover.workableinfo";
    }

    @Nonnull
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> tooltip, IWailaDataAccessor accessor, IWailaConfigHandler config, IWorkable capability) {
        final NBTTagCompound compound = accessor.getNBTData().getCompoundTag(getCapabilitySideTag(accessor.getSide())).getCompoundTag(this.getConfigName());
        final double maxProgress = (double) compound.getInteger("maxProgress") / 20;
        final double progress = Math.min(maxProgress, (double) compound.getInteger("progress") / 20);
        final boolean isWorking = compound.getBoolean("working");
        final boolean isActive = compound.getBoolean("active");

        tooltip.add(SpecialChars.getRenderString("tj.progressinfo", I18n.format("gregtech.top.progress"),
                String.valueOf(progress), String.valueOf(maxProgress), "s", "s", Color.GREEN.toString(), ",##0.00"));
        if (!isWorking) {
            tooltip.add(I18n.format("gregtech.multiblock.work_paused"));
        } else if (isActive) {
            tooltip.add(I18n.format("gregtech.multiblock.running"));
        }
        return tooltip;
    }

    @Nonnull
    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, BlockPos pos, IWorkable workable) {
        final NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger("progress", workable.getProgress());
        compound.setInteger("maxProgress", workable.getMaxProgress());
        compound.setBoolean("working", workable.isWorkingEnabled());
        compound.setBoolean("active", workable.isActive());
        tag.setTag(this.getConfigName(), compound);
        return tag;
    }
}
