package tj.integration.hwyla.providers;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IEnergyContainer;
import mcp.mobius.waila.api.*;
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

public class EnergyInfoDataProvider extends CapabilityInfoDataProvider<IEnergyContainer> {

    public static final EnergyInfoDataProvider INSTANCE = new EnergyInfoDataProvider();

    public void register(IWailaRegistrar registrar) {
        super.register(registrar);
        registrar.registerNBTProvider(this, TileEntity.class);
        registrar.registerBodyProvider(this, TileEntity.class);
    }

    @Override
    public Capability<IEnergyContainer> getCapability() {
        return GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER;
    }

    @Override
    public String getConfigName() {
        return "tj.energyinfo";
    }

    @Nonnull
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> tooltip, IWailaDataAccessor accessor, IWailaConfigHandler config, IEnergyContainer container) {
        final NBTTagCompound compound = accessor.getNBTData().getCompoundTag(this.getConfigName());
        final long energyCapacity = compound.getLong("energyCapacity");
        final long energyStored = Math.min(energyCapacity, compound.getLong("energyStored"));
        tooltip.add(SpecialChars.getRenderString("tj.progressinfo", I18n.format("gregtech.top.energy_stored"),
                String.valueOf(energyStored), String.valueOf(energyCapacity), " EU", " EU", Color.YELLOW.toString(), ",###"));
        return tooltip;
    }

    @Nonnull
    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, BlockPos pos, IEnergyContainer container) {
        final NBTTagCompound compound = new NBTTagCompound();
        compound.setLong("energyStored", container.getEnergyStored());
        compound.setLong("energyCapacity", container.getEnergyCapacity());
        tag.setTag(this.getConfigName(), compound);
        return tag;
    }
}
