package tj.integration.hwyla.providers;

import gregtech.api.block.machines.BlockMachine;
import gregtech.api.metatileentity.MetaTileEntity;
import mcp.mobius.waila.api.*;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import tj.capability.IHeatInfo;
import tj.capability.TJCapabilities;

import javax.annotation.Nonnull;
import java.util.List;

public class HeatInfoDataProvider implements IWailaDataProvider {

    public static final HeatInfoDataProvider INSTANCE = new HeatInfoDataProvider();

    public void register(IWailaRegistrar registrar) {
        registrar.registerNBTProvider(this, TileEntity.class);
        registrar.registerBodyProvider(this, TileEntity.class);
        registrar.addConfig("TJ", "tj.heatinfo");
    }

    @Nonnull
    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, BlockPos pos) {
        final MetaTileEntity metaTileEntity = BlockMachine.getMetaTileEntity(world, pos);
        if (metaTileEntity != null) {
            final IHeatInfo heatInfo = metaTileEntity.getCapability(TJCapabilities.CAPABILITY_HEAT, null);
            if (heatInfo != null) {
                final NBTTagCompound compound = new NBTTagCompound();
                compound.setLong("heat", heatInfo.heat());
                compound.setLong("maxHeat", heatInfo.maxHeat());
                tag.setTag("tj.heatinfo", compound);
            }
        }
        return tag;
    }

    @Nonnull
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> tooltip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        if (config.getConfig("tj.heatinfo")) {
            final MetaTileEntity metaTileEntity = BlockMachine.getMetaTileEntity(accessor.getWorld(), accessor.getPosition());
            if (metaTileEntity != null) {
                final IHeatInfo heatInfo = metaTileEntity.getCapability(TJCapabilities.CAPABILITY_HEAT, accessor.getSide());
                if (heatInfo != null) {
                    final NBTTagCompound compound = accessor.getNBTData().getCompoundTag("tj.heatinfo");
                    final long maxHeat = compound.getLong("maxHeat");
                    final long heat = Math.min(maxHeat, compound.getLong("heat"));
                    tooltip.add(SpecialChars.getRenderString("tj.progressinfo",
                            I18n.format("tj.top.progress.heat"),
                            String.valueOf(heat),
                            String.valueOf(maxHeat),
                            "°C",
                            "°C",
                            "RED"));
                }
            }
        }
        return tooltip;
    }
}
