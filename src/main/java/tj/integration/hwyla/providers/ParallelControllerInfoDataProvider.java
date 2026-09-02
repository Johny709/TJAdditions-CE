package tj.integration.hwyla.providers;

import gregicadditions.GAValues;
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
import tj.TJValues;
import tj.capability.IParallelController;
import tj.capability.TJCapabilities;
import tj.util.TJUtility;

import javax.annotation.Nonnull;
import java.util.List;

public class ParallelControllerInfoDataProvider implements IWailaDataProvider {

    public static final ParallelControllerInfoDataProvider INSTANCE = new ParallelControllerInfoDataProvider();

    public void register(IWailaRegistrar registrar) {
        registrar.registerNBTProvider(this, TileEntity.class);
        registrar.registerBodyProvider(this, TileEntity.class);
        registrar.addConfig("TJ", "tj.parallel_controller");
    }

    @Nonnull
    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, BlockPos pos) {
        if (!(te instanceof MetaTileEntityHolder))
            return tag;
        final MetaTileEntity metaTileEntity = ((MetaTileEntityHolder) te).getMetaTileEntity();
        if (metaTileEntity == null)
            return tag;
        final IParallelController controller = metaTileEntity.getCapability(TJCapabilities.CAPABILITY_PARALLEL_CONTROLLER, null);
        if (controller == null)
            return tag;
        final NBTTagCompound compound = new NBTTagCompound();
        compound.setLong("energyStored", controller.getEnergyStored());
        compound.setLong("energyCapacity", controller.getEnergyCapacity());
        compound.setLong("maxEU", controller.getMaxEUt());
        compound.setLong("energyTotal", controller.getTotalEnergyConsumption());
        compound.setLong("voltageTier", controller.getVoltageTier());
        compound.setInteger("bonusEU", controller.getEUBonus());
        compound.setString("recipeMap", controller.getMultiblockRecipe().getUnlocalizedName());
        tag.setTag("tj.parallel_controller", compound);
        return tag;
    }

    @Nonnull
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> tooltip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        if (!config.getConfig("tj.parallel_controller"))
            return tooltip;
        if (!(accessor.getTileEntity() instanceof MetaTileEntityHolder))
            return tooltip;
        final MetaTileEntity metaTileEntity = ((MetaTileEntityHolder) accessor.getTileEntity()).getMetaTileEntity();
        if (metaTileEntity == null)
            return tooltip;
        final IParallelController controller = metaTileEntity.getCapability(TJCapabilities.CAPABILITY_PARALLEL_CONTROLLER, accessor.getSide());
        if (controller == null)
            return tooltip;
        final NBTTagCompound compound = accessor.getNBTData().getCompoundTag("tj.parallel_controller");
        final long energyCapacity = compound.getLong("energyCapacity");
        final long energyStored = Math.min(energyCapacity, compound.getLong("energyStored"));
        final long maxEUt = compound.getLong("maxEU");
        final long totalEnergy = compound.getLong("energyTotal");
        final long voltageTier = compound.getLong("voltageTier");
        final int energyBonus = compound.getInteger("bonusEU");
        final int tier = TJUtility.getTierFromVoltage(voltageTier);
        final int euTier = TJUtility.getTierFromVoltage(maxEUt);
        tooltip.add(I18n.format("tj.multiblock.max_voltage", TJValues.thousandFormat.format(maxEUt),
                TJValues.VCC[euTier] + GAValues.VN[euTier]));
        if (energyBonus > 0)
            tooltip.add(I18n.format("tj.multiblock.parallel_controller.energy_bonus", 100 - energyBonus));
        tooltip.add(I18n.format("machine.universal.tooltip.voltage_tier", TJValues.VCC[tier] + GAValues.VN[tier]));
        tooltip.add(I18n.format("tj.multiblock.parallel.sum", TJValues.thousandFormat.format(totalEnergy)));
        if (compound.hasKey("recipeMap"))
            tooltip.add(I18n.format("tj.multiblock.universal.tooltip.1",
                    I18n.format("recipemap." + compound.getString("recipeMap") + ".name")));
        if (energyCapacity > 0) {
            tooltip.add(SpecialChars.getRenderString("tj.progressinfo", I18n.format("tj.top.parallel_controller.energy_stored"),
                    String.valueOf(energyStored), String.valueOf(energyCapacity), " EU", " EU", "YELLOW"));
        }
        return tooltip;
    }
}
