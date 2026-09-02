package tj.integration.hwyla.providers;

import gregicadditions.GAValues;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import mcp.mobius.waila.api.*;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import tj.TJValues;
import tj.capability.IMultipleWorkable;
import tj.capability.TJCapabilities;
import tj.util.TJUtility;

import javax.annotation.Nonnull;
import java.util.List;

public class ParallelWorkableInfoDataProvider implements IWailaDataProvider {

    public static final ParallelWorkableInfoDataProvider INSTANCE = new ParallelWorkableInfoDataProvider();

    public void register(IWailaRegistrar registrar) {
        registrar.registerNBTProvider(this, TileEntity.class);
        registrar.registerBodyProvider(this, TileEntity.class);
        registrar.addConfig("TJ", "tj.parallel_workable");
    }

    @Nonnull
    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, BlockPos pos) {
        if (!(te instanceof MetaTileEntityHolder))
            return tag;
        final MetaTileEntity metaTileEntity = ((MetaTileEntityHolder) te).getMetaTileEntity();
        if (metaTileEntity == null)
            return tag;
        final IMultipleWorkable workable = metaTileEntity.getCapability(TJCapabilities.CAPABILITY_MULTIPLE_WORKABLE, null);
        if (workable == null)
            return tag;
        final NBTTagList workableList = new NBTTagList();
        for (int i = 0; i < workable.getSize(); i++) {
            final NBTTagCompound compound = new NBTTagCompound();
            compound.setInteger("progress", workable.getProgress(i));
            compound.setInteger("maxProgress", workable.getMaxProgress(i));
            compound.setLong("eut", workable.getRecipeEUt(i));
            compound.setBoolean("working", workable.isWorkingEnabled(i));
            compound.setBoolean("active", workable.isInstanceActive(i));
            compound.setBoolean("problem", workable.hasProblems(i));
            workableList.appendTag(compound);
        }
        tag.setTag("tj.parallel_workable.list", workableList);
        return tag;
    }

    @Nonnull
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> tooltip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        if (!config.getConfig("tj.parallel_workable"))
            return tooltip;
        if (!(accessor.getTileEntity() instanceof MetaTileEntityHolder))
            return tooltip;
        final MetaTileEntity metaTileEntity = ((MetaTileEntityHolder) accessor.getTileEntity()).getMetaTileEntity();
        if (metaTileEntity == null)
            return tooltip;
        final IMultipleWorkable workable = metaTileEntity.getCapability(TJCapabilities.CAPABILITY_MULTIPLE_WORKABLE, null);
        if (workable == null)
            return tooltip;
        final NBTTagList workableList = accessor.getNBTData().getTagList("tj.parallel_workable.list", 10);
        tooltip.add("§b(" + 1 + "/" + workableList.tagCount() + ")");
        for (int i = 0; i < workableList.tagCount(); i++) {
            final NBTTagCompound compound = workableList.getCompoundTagAt(i);
            final double maxProgress = (double) compound.getInteger("maxProgress") / 20;
            final double progress = Math.min(maxProgress, (double) compound.getInteger("progress") / 20);
            final long eut = compound.getLong("eut");
            final int tier = TJUtility.getTierFromVoltage(eut);
            final boolean working = compound.getBoolean("working");
            final boolean active = compound.getBoolean("active");
            final boolean problem = compound.getBoolean("problem");

            tooltip.add("§b[" + (i + 1) + "]§7 " + I18n.format("tj.multiblock.parallel.status", I18n.format(!working ? "gregtech.multiblock.work_paused" :
                    problem ? "machine.universal.has_problems" :
                    active ? "gregtech.multiblock.running" :
                    "gregtech.multiblock.idling")));
            tooltip.add(SpecialChars.getRenderString("tj.progressinfo", I18n.format("gregtech.top.progress"),
                    String.valueOf(progress), String.valueOf(maxProgress), "s", "s", "GREEN", ",##0.00"));
            tooltip.add(I18n.format("tj.multiblock.eu", TJValues.thousandFormat.format(eut),
                    tier > 14 ? "§c§lM§e§lA§a§lX§b§l+§d§l" + (tier - 14) : TJValues.VCC[tier] + GAValues.VN[tier]));
        }
        return tooltip;
    }
}
