package tj.integration.jei.multi.electric;

import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.components.MotorCasing;
import gregicadditions.item.components.PumpCasing;
import gregicadditions.machines.GATileEntities;
import gregtech.api.GTValues;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.common.blocks.BlockBoilerCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.integration.jei.multiblock.MultiblockShapeInfo;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumFacing;
import tj.blocks.BlockSolidCasings;
import tj.blocks.TJMetaBlocks;
import tj.integration.jei.TJMultiblockInfoPage;
import tj.machines.TJMetaTileEntities;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static gregicadditions.GAMaterials.Seaborgium;

public class InfiniteFluidDrillInfo extends TJMultiblockInfoPage {

    @Override
    public MultiblockControllerBase getController() {
        return TJMetaTileEntities.INFINITE_FLUID_DRILL;
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        MultiblockShapeInfo.Builder shapeInfo = MultiblockShapeInfo.builder()
                .aisle("CF~FC", "CF~FC", "CCCCC", "~CCC~", "~~C~~", "~~C~~", "~~C~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~")
                .aisle("F~~~F", "F~~~F", "CCmCC", "I###O", "~C#C~", "~C#C~", "~C#C~", "~FCF~", "~FFF~", "~FFF~", "~~F~~", "~~~~~", "~~~~~", "~~~~~")
                .aisle("~~~~~", "~~~~~", "CmPmC", "S#T#E", "C#T#C", "C#T#C", "C#T#C", "~CCC~", "~FCF~", "~FFF~", "~FFF~", "~~F~~", "~~F~~", "~~F~~")
                .aisle("F~~~F", "F~~~F", "CCmCC", "M###O", "~C#C~", "~C#C~" ,"~C#C~", "~FCF~", "~FFF~", "~FFF~", "~~F~~", "~~~~~", "~~~~~", "~~~~~")
                .aisle("CF~FC", "CF~FC", "CCCCC", "~CCC~", "~~C~~", "~~C~~", "~~C~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~")
                .where('S', this.getController(), EnumFacing.WEST)
                .where('C', TJMetaBlocks.SOLID_CASING.getState(BlockSolidCasings.SolidCasingType.SEABORGIUM_CASING))
                .where('F', MetaBlocks.FRAMES.get(Seaborgium).getDefaultState())
                .where('T', MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.TUNGSTENSTEEL_PIPE))
                .where('I', MetaTileEntities.FLUID_IMPORT_HATCH[GTValues.MAX], EnumFacing.WEST)
                .where('O', MetaTileEntities.FLUID_EXPORT_HATCH[GTValues.MAX], EnumFacing.WEST)
                .where('M', GATileEntities.MAINTENANCE_HATCH[2], EnumFacing.WEST);
        return Arrays.stream(MotorCasing.CasingType.values())
                .map(casingType -> shapeInfo.where('m', GAMetaBlocks.MOTOR_CASING.getState(casingType))
                        .where('P', GAMetaBlocks.PUMP_CASING.getState(PumpCasing.CasingType.values()[casingType.ordinal()]))
                        .where('E', this.getEnergyHatch(casingType.getTier(), false), EnumFacing.EAST)
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    protected void generateBlockTooltips() {
        super.generateBlockTooltips();
        Arrays.stream(MotorCasing.CasingType.values()).forEach(casingType -> this.addBlockTooltip(GAMetaBlocks.MOTOR_CASING.getItemVariant(casingType), COMPONENT_BLOCK_TOOLTIP));
        Arrays.stream(PumpCasing.CasingType.values()).forEach(casingType -> this.addBlockTooltip(GAMetaBlocks.PUMP_CASING.getItemVariant(casingType), COMPONENT_BLOCK_TOOLTIP));
    }

    @Override
    public String[] getDescription() {
        return new String[] {
                I18n.format("gtadditions.multiblock.drilling_rig.tooltip.1")};
    }

    @Override
    public float getDefaultZoom() {
        return 0.5f;
    }
}
