package tj.integration.hwyla.providers;

import gregicadditions.machines.multi.IMaintenance;
import gregtech.api.block.machines.BlockMachine;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.IWailaRegistrar;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.List;

public class StructureInfoDataProvider implements IWailaDataProvider {

    public static final StructureInfoDataProvider INSTANCE = new StructureInfoDataProvider();

    public void register(IWailaRegistrar registrar) {
        registrar.registerBodyProvider(this, TileEntity.class);
        registrar.registerNBTProvider(this, TileEntity.class);
        registrar.addConfig("TJ", "tj.structureinfo");
    }

    @Nonnull
    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, BlockPos pos) {
        final MetaTileEntity metaTileEntity = BlockMachine.getMetaTileEntity(world, pos);
        if (metaTileEntity instanceof MultiblockControllerBase) {
            final NBTTagCompound compound = new NBTTagCompound();
            compound.setBoolean("formed", ((MultiblockControllerBase) metaTileEntity).isStructureFormed());
            if (metaTileEntity instanceof IMaintenance) {
                final IMaintenance maintenance = (IMaintenance) metaTileEntity;
                compound.setBoolean("problems", maintenance.hasProblems());
                compound.setByte("amountOfProblems", maintenance.getProblems());
            }
            tag.setTag("tj.structureinfo", compound);
        }
        return tag;
    }

    @Nonnull
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> tooltip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        if (config.getConfig("tj.structureinfo")) {
            final MetaTileEntity metaTileEntity = BlockMachine.getMetaTileEntity(accessor.getWorld(), accessor.getPosition());
            if (metaTileEntity instanceof MultiblockControllerBase) {
                final NBTTagCompound compound = accessor.getNBTData().getCompoundTag("tj.structureinfo");
                tooltip.add(I18n.format(compound.getBoolean("formed") ? "tj.multiblock.structure_formed" :
                        "gregtech.multiblock.invalid_structure"));
                if (metaTileEntity instanceof IMaintenance) {
                    tooltip.add(I18n.format(compound.getBoolean("problems") ? "gtadditions.multiblock.universal.has_problems" :
                            "gtadditions.multiblock.universal.no_problems"));
                    final byte amountOfProblems = compound.getByte("amountOfProblems");
                    if (((amountOfProblems) & 1) == 0) // Wrench
                        tooltip.add(I18n.format("gtadditions.multiblock.universal.problem.wrench"));
                    if (((amountOfProblems >> 1) & 1) == 0) // Screwdriver
                        tooltip.add(I18n.format("gtadditions.multiblock.universal.problem.screwdriver"));
                    if (((amountOfProblems >> 2) & 1) == 0) // Soft Hammer
                        tooltip.add(I18n.format("gtadditions.multiblock.universal.problem.softhammer"));
                    if (((amountOfProblems >> 3) & 1) == 0) // Hard Hammer
                        tooltip.add(I18n.format("gtadditions.multiblock.universal.problem.hardhammer"));
                    if (((amountOfProblems >> 4) & 1) == 0) // Wirecutter
                        tooltip.add(I18n.format("gtadditions.multiblock.universal.problem.wirecutter"));
                    if (((amountOfProblems >> 5) & 1) == 0) // Crowbar
                        tooltip.add(I18n.format("gtadditions.multiblock.universal.problem.crowbar"));
                }
            }
        }
        return tooltip;
    }
}
