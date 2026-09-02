package tj.integration.theoneprobe.providers;

import gregicadditions.machines.multi.IMaintenance;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import mcjty.theoneprobe.api.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import tj.integration.theoneprobe.impl.ElementTJText;

public class StructureInfoProvider implements IProbeInfoProvider {

    @Override
    public void addProbeInfo(ProbeMode probeMode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
        if (blockState.getBlock().hasTileEntity(blockState)) {
            final TileEntity tileEntity = world.getTileEntity(data.getPos());
            if (tileEntity instanceof MetaTileEntityHolder) {
                final MetaTileEntity metaTileEntity = ((MetaTileEntityHolder) tileEntity).getMetaTileEntity();
                if (metaTileEntity instanceof MultiblockControllerBase) {
                    final boolean isStructureFormed = ((MultiblockControllerBase) metaTileEntity).isStructureFormed();

                    final IProbeInfo structureInfo = probeInfo.vertical(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_TOPLEFT));
                    structureInfo.text(TextStyleClass.INFO + (isStructureFormed ? "§a" + "{*tj.multiblock.structure_formed*}" : "§c" + "{*gregtech.multiblock.invalid_structure*}"));

                    if (metaTileEntity instanceof IMaintenance) {
                        final boolean hasProblems = ((IMaintenance) metaTileEntity).hasProblems();
                        final byte amountOfProblems = ((IMaintenance) metaTileEntity).getProblems();

                        structureInfo.element(new ElementTJText(!hasProblems ? "§a" + "{*gtadditions.multiblock.universal.no_problems*}" : "§c" + "{*gtadditions.multiblock.universal.has_problems*}"));

                        if (((amountOfProblems) & 1) == 0) // Wrench
                            structureInfo.element(new ElementTJText("§c" + "{*gtadditions.multiblock.universal.problem.wrench*}"));
                        if (((amountOfProblems >> 1) & 1) == 0) // Screwdriver
                            structureInfo.element(new ElementTJText("§c" + "{*gtadditions.multiblock.universal.problem.screwdriver*}"));
                        if (((amountOfProblems >> 2) & 1) == 0) // Soft Hammer
                            structureInfo.element(new ElementTJText("§c" + "{*gtadditions.multiblock.universal.problem.softhammer*}"));
                        if (((amountOfProblems >> 3) & 1) == 0) // Hard Hammer
                            structureInfo.element(new ElementTJText("§c" + "{*gtadditions.multiblock.universal.problem.hardhammer*}"));
                        if (((amountOfProblems >> 4) & 1) == 0) // Wirecutter
                            structureInfo.element(new ElementTJText("§c" + "{*gtadditions.multiblock.universal.problem.wirecutter*}"));
                        if (((amountOfProblems >> 5) & 1) == 0) // Crowbar
                            structureInfo.element(new ElementTJText("§c" + "{*gtadditions.multiblock.universal.problem.crowbar*}"));
                    }
                }
            }
        }
    }

    @Override
    public String getID() {
        return "tj:structure_info_provider";
    }
}
