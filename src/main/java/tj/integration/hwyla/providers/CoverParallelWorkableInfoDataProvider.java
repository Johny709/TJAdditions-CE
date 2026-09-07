package tj.integration.hwyla.providers;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaRegistrar;
import mcp.mobius.waila.api.SpecialChars;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import tj.capability.IMultipleWorkable;
import tj.capability.TJCapabilities;
import tj.util.Color;

import javax.annotation.Nonnull;
import java.util.List;

public class CoverParallelWorkableInfoDataProvider extends CoverCapabilityInfoDataProvider<IMultipleWorkable> {

    public static final CoverParallelWorkableInfoDataProvider INSTANCE = new CoverParallelWorkableInfoDataProvider();

    public void register(IWailaRegistrar registrar) {
        registrar.registerNBTProvider(this, TileEntity.class);
        registrar.registerBodyProvider(this, TileEntity.class);
        registrar.addConfig("TJ", this.getConfigName());
    }

    @Override
    public Capability<IMultipleWorkable> getCapability() {
        return TJCapabilities.CAPABILITY_MULTIPLE_WORKABLE;
    }

    @Override
    public String getConfigName() {
        return "tj.cover.parallel_workable";
    }

    @Nonnull
    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, BlockPos pos, IMultipleWorkable multipleWorkable) {
        final NBTTagList workableList = new NBTTagList();
        for (int i = 0; i < multipleWorkable.getSize(); i++) {
            final NBTTagCompound compound = new NBTTagCompound();
            compound.setInteger("progress", multipleWorkable.getProgress(i));
            compound.setInteger("maxProgress", multipleWorkable.getMaxProgress(i));
            compound.setBoolean("working", multipleWorkable.isWorkingEnabled(i));
            compound.setBoolean("active", multipleWorkable.isInstanceActive(i));
            compound.setBoolean("problem", multipleWorkable.hasProblems(i));
            workableList.appendTag(compound);
        }
        tag.setTag("tj.parallel_workable.list", workableList);
        return tag;
    }

    @Nonnull
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> tooltip, IWailaDataAccessor accessor, IWailaConfigHandler config, IMultipleWorkable capability) {
        final NBTTagList workableList = accessor.getNBTData().getCompoundTag(getCapabilitySideTag(accessor.getSide()))
                .getTagList("tj.parallel_workable.list", 10);
        tooltip.add("§b(" + 1 + "/" + workableList.tagCount() + ")");
        for (int i = 0; i < workableList.tagCount(); i++) {
            final NBTTagCompound compound = workableList.getCompoundTagAt(i);
            final double maxProgress = (double) compound.getInteger("maxProgress") / 20;
            final double progress = Math.min(maxProgress, (double) compound.getInteger("progress") / 20);
            final boolean working = compound.getBoolean("working");
            final boolean active = compound.getBoolean("active");
            final boolean problem = compound.getBoolean("problem");

            tooltip.add("§b[" + (i + 1) + "]§7 " + I18n.format("tj.multiblock.parallel.status", I18n.format(!working ? "gregtech.multiblock.work_paused" :
                    problem ? "machine.universal.has_problems" :
                            active ? "gregtech.multiblock.running" :
                                    "gregtech.multiblock.idling")));
            tooltip.add(SpecialChars.getRenderString("tj.progressinfo", I18n.format("gregtech.top.progress"),
                    String.valueOf(progress), String.valueOf(maxProgress), "s", "s", Color.GREEN.toString(), ",##0.00"));
        }
        return tooltip;
    }
}
