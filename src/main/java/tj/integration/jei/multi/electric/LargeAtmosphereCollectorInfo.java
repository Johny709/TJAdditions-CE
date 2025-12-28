package tj.integration.jei.multi.electric;

import com.google.common.collect.Lists;
import gregicadditions.jei.GAMultiblockShapeInfo;
import gregicadditions.machines.GATileEntities;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.common.metatileentities.multi.electric.generator.MetaTileEntityLargeTurbine;
import gregtech.integration.jei.multiblock.MultiblockInfoPage;
import gregtech.integration.jei.multiblock.MultiblockShapeInfo;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumFacing;
import tj.machines.multi.electric.MetaTileEntityLargeAtmosphereCollector;

import java.util.List;

import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;

public class LargeAtmosphereCollectorInfo extends MultiblockInfoPage {

    private final MetaTileEntityLargeTurbine.TurbineType turbineType;
    private final MetaTileEntityLargeAtmosphereCollector tileEntity;

    public LargeAtmosphereCollectorInfo(MetaTileEntityLargeTurbine.TurbineType turbineType, MetaTileEntityLargeAtmosphereCollector tileEntity) {
        this.turbineType = turbineType;
        this.tileEntity = tileEntity;
    }

    @Override
    public MultiblockControllerBase getController() {
        return tileEntity;
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        GAMultiblockShapeInfo builder = GAMultiblockShapeInfo.builder(LEFT, FRONT, DOWN)
                .aisle("CCC", "CCC", "CfC", "PPP", "PPP", "PPP", "CCC")
                .aisle("CFC", "C#M", "C#S", "P#P", "P#P", "P#P", "CRC")
                .aisle("CCC", "CCC", "CoC", "PPP", "PPP", "PPP", "CCC")
                .where('S', tileEntity, EnumFacing.WEST)
                .where('C', turbineType.casingState)
                .where('P', tileEntity.getPipeState())
                .where('R', MetaTileEntities.ROTOR_HOLDER[0], EnumFacing.SOUTH)
                .where('F', MetaTileEntities.FLUID_EXPORT_HATCH[3 + turbineType.ordinal()], EnumFacing.NORTH)
                .where('f', MetaTileEntities.FLUID_IMPORT_HATCH[3 + turbineType.ordinal()], EnumFacing.UP)
                .where('o', MetaTileEntities.FLUID_EXPORT_HATCH[3 + turbineType.ordinal()], EnumFacing.DOWN)
                .where('M', GATileEntities.MAINTENANCE_HATCH[0], EnumFacing.WEST)
                .build();
        return Lists.newArrayList(builder);
    }

    @Override
    public String[] getDescription() {
        return new String[] {
                I18n.format("tj.multiblock.large_atmosphere_collector.description"), I18n.format("tj.multiblock.turbine.fast_mode.description")};
    }
}
