package tj.integration.hwyla.providers;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IFuelInfo;
import gregtech.api.capability.IFuelable;
import gregtech.api.capability.impl.ItemFuelInfo;
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
import tj.TJValues;
import tj.util.Color;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

public class FuelableInfoDataProvider extends CapabilityInfoDataProvider<IFuelable> {

    public static final FuelableInfoDataProvider INSTANCE = new FuelableInfoDataProvider();

    @Override
    public void register(IWailaRegistrar registrar) {
        super.register(registrar);
        registrar.registerNBTProvider(this, TileEntity.class);
        registrar.registerBodyProvider(this, TileEntity.class);
    }

    @Override
    public Capability<IFuelable> getCapability() {
        return GregtechCapabilities.CAPABILITY_FUELABLE;
    }

    @Override
    public String getConfigName() {
        return "tj.fuelable";
    }

    @Nonnull
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> tooltip, IWailaDataAccessor accessor, IWailaConfigHandler config, IFuelable fuelable) {
        final NBTTagList tagList = accessor.getNBTData().getTagList(this.getConfigName(), 10);
        for (int i = 0; i < tagList.tagCount(); i++) {
            final NBTTagCompound compound = tagList.getCompoundTagAt(i);
            if (compound.hasKey("item")) {
                final ItemStack stack = new ItemStack(compound.getCompoundTag("item"));
                tooltip.add(I18n.format("gregtech.top.fuel_name") + " " + stack.getDisplayName());
            } else tooltip.add(I18n.format("gregtech.top.fuel_name") + I18n.format(compound.getString("fuelName")));

            final int fuelRemaining = compound.getInteger("fuelRemaining");
            final int fuelCapacity = compound.getInteger("fuelCapacity");
            final int fuelConsumed = compound.getInteger("fuelConsumed");
            final double burnTimePrecise = compound.getLong("fuelBurnTime") / 20.0;

            if (fuelRemaining < fuelConsumed) {
                tooltip.add(I18n.format("gregtech.top.fuel_min_consume") + " " + TJValues.thousandTwoPlaceFormat.format(fuelConsumed));
            } else tooltip.add(I18n.format("gregtech.top.fuel_burn") + " " + TJValues.thousandTwoPlaceFormat.format(burnTimePrecise) +
                        " " + I18n.format("gregtech.top.fuel_time"));
            tooltip.add(SpecialChars.getRenderString("tj.progressinfo", "", String.valueOf(fuelRemaining),
                    String.valueOf(fuelCapacity), "", "", Color.GOLD.toString(), ",###"));
        }
        if (tagList.isEmpty())
            tooltip.add(I18n.format("gregtech.top.fuel_none"));
        return tooltip;
    }

    @Nonnull
    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, BlockPos pos, IFuelable fuelable) {
        final NBTTagList tagList = new NBTTagList();
        final Collection<IFuelInfo> fuelInfos = fuelable.getFuels();
        if (fuelInfos != null) {
            for (IFuelInfo fuelInfo : fuelInfos) {
                final NBTTagCompound compound = new NBTTagCompound();
                compound.setString("fuelName", fuelInfo.getFuelName());
                compound.setInteger("fuelRemaining", fuelInfo.getFuelRemaining());
                compound.setInteger("fuelCapacity", fuelInfo.getFuelCapacity());
                compound.setInteger("fuelConsumed", fuelInfo.getFuelMinConsumed());
                compound.setLong("fuelBurnTime", fuelInfo.getFuelBurnTimeLong());
                if (fuelInfo instanceof ItemFuelInfo) {
                    final ItemStack stack = ((ItemFuelInfo) fuelInfo).getItemStack();
                    if (stack != null)
                        compound.setTag("item", stack.serializeNBT());
                }
                tagList.appendTag(compound);
            }
        }
        tag.setTag(this.getConfigName(), tagList);
        return tag;
    }
}
