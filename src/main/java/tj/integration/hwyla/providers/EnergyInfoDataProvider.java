package tj.integration.hwyla.providers;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IEnergyContainer;
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

public class EnergyInfoDataProvider implements IWailaDataProvider {

    public static final EnergyInfoDataProvider INSTANCE = new EnergyInfoDataProvider();

    public void register(IWailaRegistrar registrar) {
        registrar.registerNBTProvider(this, TileEntity.class);
        registrar.registerBodyProvider(this, TileEntity.class);
        registrar.addConfig("TJ", "tj.energyinfo");
    }

    @Nonnull
    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, BlockPos pos) {
        if (!(te instanceof MetaTileEntityHolder))
            return tag;
        final MetaTileEntity metaTileEntity = ((MetaTileEntityHolder) te).getMetaTileEntity();
        if (metaTileEntity == null)
            return tag;
        final IEnergyContainer energyContainer = metaTileEntity.getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, null);
        if (energyContainer == null)
            return tag;
        final NBTTagCompound compound = new NBTTagCompound();
        compound.setLong("energyStored", energyContainer.getEnergyStored());
        compound.setLong("energyCapacity", energyContainer.getEnergyCapacity());
        tag.setTag("tj.energyinfo", compound);
        return tag;
    }

    @Nonnull
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> tooltip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        if (!config.getConfig("tj.energyinfo"))
            return tooltip;
        if (!(accessor.getTileEntity() instanceof MetaTileEntityHolder))
            return tooltip;
        final MetaTileEntity metaTileEntity = ((MetaTileEntityHolder) accessor.getTileEntity()).getMetaTileEntity();
        if (metaTileEntity == null)
            return tooltip;
        final IEnergyContainer energyContainer = metaTileEntity.getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, accessor.getSide());
        if (energyContainer == null)
            return tooltip;
        final NBTTagCompound compound = accessor.getNBTData().getCompoundTag("tj.energyinfo");
        final long energyCapacity = compound.getLong("energyCapacity");
        final long energyStored = Math.min(energyCapacity, compound.getLong("energyStored"));
        tooltip.add(SpecialChars.getRenderString("tj.progressinfo", I18n.format("gregtech.top.energy_stored"),
                String.valueOf(energyStored), String.valueOf(energyCapacity), " EU", " EU", "YELLOW", ",###"));
        return tooltip;
    }
}
