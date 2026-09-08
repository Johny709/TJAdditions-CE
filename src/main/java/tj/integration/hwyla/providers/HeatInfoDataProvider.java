package tj.integration.hwyla.providers;

import mcp.mobius.waila.api.*;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import tj.capability.IHeatInfo;
import tj.capability.TJCapabilities;
import tj.util.Color;

import javax.annotation.Nonnull;
import java.util.List;

public class HeatInfoDataProvider extends CapabilityInfoDataProvider<IHeatInfo> {

    public static final HeatInfoDataProvider INSTANCE = new HeatInfoDataProvider();

    @Override
    public void register(IWailaRegistrar registrar) {
        super.register(registrar);
        registrar.registerNBTProvider(this, TileEntity.class);
        registrar.registerBodyProvider(this, TileEntity.class);
    }

    @Override
    public Capability<IHeatInfo> getCapability() {
        return TJCapabilities.CAPABILITY_HEAT;
    }

    @Override
    public String getConfigName() {
        return "tj.heatinfo";
    }

    @Nonnull
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> tooltip, IWailaDataAccessor accessor, IWailaConfigHandler config, IHeatInfo heatInfo) {
        final NBTTagCompound compound = accessor.getNBTData().getCompoundTag(this.getConfigName());
        final long maxHeat = compound.getLong("maxHeat");
        final long heat = Math.min(maxHeat, compound.getLong("heat"));
        tooltip.add(SpecialChars.getRenderString("tj.progressinfo", I18n.format("tj.top.progress.heat"),
                String.valueOf(heat), String.valueOf(maxHeat), "°C", "°C", Color.RED.toString(), ",###"));
        return tooltip;
    }

    @Nonnull
    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, BlockPos pos, IHeatInfo heatInfo) {
        final NBTTagCompound compound = new NBTTagCompound();
        compound.setLong("heat", heatInfo.heat());
        compound.setLong("maxHeat", heatInfo.maxHeat());
        tag.setTag(this.getConfigName(), compound);
        return tag;
    }
}
