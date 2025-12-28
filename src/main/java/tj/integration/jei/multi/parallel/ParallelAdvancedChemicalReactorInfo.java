package tj.integration.jei.multi.parallel;

import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.GAMultiblockCasing;
import gregicadditions.item.components.MotorCasing;
import gregicadditions.item.components.PumpCasing;
import gregicadditions.jei.GAMultiblockShapeInfo;
import gregicadditions.machines.GATileEntities;
import gregtech.api.GTValues;
import gregtech.common.blocks.BlockWireCoil;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.integration.jei.multiblock.MultiblockShapeInfo;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.ArrayUtils;
import tj.TJConfig;
import tj.builder.multicontrollers.ParallelRecipeMapMultiblockController;
import tj.integration.jei.TJMultiblockInfoPage;
import tj.machines.TJMetaTileEntities;

import java.util.ArrayList;
import java.util.List;

import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;
import static gregtech.api.unification.material.Materials.Steel;
import static net.minecraft.util.EnumFacing.EAST;
import static net.minecraft.util.EnumFacing.WEST;


public class ParallelAdvancedChemicalReactorInfo extends TJMultiblockInfoPage implements IParallelMultiblockInfoPage {

    @Override
    public ParallelRecipeMapMultiblockController getController() {
        return TJMetaTileEntities.ADVANCED_PARALLEL_CHEMICAL_REACTOR;
    }

    @Override
    public List<MultiblockShapeInfo[]> getMatchingShapes(MultiblockShapeInfo[] shapes) {
        List<MultiblockShapeInfo[]> shapeInfos = new ArrayList<>();
        int size = Math.min(TJConfig.machines.maxLayersInJEI, this.getController().getMaxParallel());
        for (int shapeInfo = 1; shapeInfo <= size; shapeInfo++) {
            GAMultiblockShapeInfo.Builder builder = GAMultiblockShapeInfo.builder(FRONT, RIGHT, DOWN);
            if (!(shapeInfo % 2 == 0)) {
                builder.aisle("C~~~C~CCCCC~~~~~~", "CCCCC~CCCCC~~~~~~", "C~~~C~CCCCC~~~~~~", "CCCCC~CCCCC~~~~~~", "C~~~C~CCCCC~~~~~~");
                builder.aisle("CCCCC~F~~~F~~~~~~", "CcccC~~~P~~~~~~~~", "CPPPPPPPpP~~~~~~~", "CcccC~~~P~~~~~~~~", "CCCCC~F~~~F~~~~~~");
                builder.aisle("C~~~C~F~~~F~~~~~~", "CPPPPPPPP~~~~~~~~", "CmmmC~~PpP~~~~~~~", "CPPPPPPPP~~~~~~~~", "C~~~C~F~~~F~~~~~~");
                builder.aisle("CCCCC~F~~~F~~~~~~", "CcccC~~~P~~~~~~~~", "CPPPPPPPpP~~~~~~~", "CcccC~~~P~~~~~~~~", "CCCCC~F~~~F~~~~~~");
            } else {
                builder.aisle("C~~~C~CCCCC~C~~~C", "CCCCC~CCCCC~CCCCC", "C~~~C~CCCCC~C~~~C", "CCCCC~CCCCC~CCCCC", "C~~~C~CCCCC~C~~~C");
                builder.aisle("CCCCC~F~~~F~CCCCC", "CcccC~~~P~~~CcccC", "CPPPPPPPpPPPPPPPC", "CcccC~~~P~~~CcccC", "CCCCC~F~~~F~CCCCC");
                builder.aisle("C~~~C~F~~~F~C~~~C", "CPPPPPPPPPPPPPPPC", "CmmmC~~PpP~~CmmmC", "CPPPPPPPPPPPPPPPC", "C~~~C~F~~~F~C~~~C");
                builder.aisle("CCCCC~F~~~F~CCCCC", "CcccC~~~P~~~CcccC", "CPPPPPPPpPPPPPPPC", "CcccC~~~P~~~CcccC", "CCCCC~F~~~F~CCCCC");
            }
            for (int layer = 1; layer < shapeInfo; layer++) {
                if (layer % 2 == 0) {
                    builder.aisle("C~~~C~F~~~F~C~~~C", "CCCCC~~~P~~~CCCCC", "C~~~C~~PpP~~C~~~C", "CCCCC~~~P~~~CCCCC", "C~~~C~F~~~F~C~~~C");
                    builder.aisle("CCCCC~F~~~F~CCCCC", "CcccC~~~P~~~CcccC", "CPPPPPPPpPPPPPPPC", "CcccC~~~P~~~CcccC", "CCCCC~F~~~F~CCCCC");
                    builder.aisle("C~~~C~F~~~F~C~~~C", "CPPPPPPPPPPPPPPPC", "CmmmC~~PpP~~CmmmC", "CPPPPPPPPPPPPPPPC", "C~~~C~F~~~F~C~~~C");
                    builder.aisle("CCCCC~F~~~F~CCCCC", "CcccC~~~P~~~CcccC", "CPPPPPPPpPPPPPPPC", "CcccC~~~P~~~CcccC", "CCCCC~F~~~F~CCCCC");
                }
            }
            builder.aisle(shapeInfo > 1 ?
                    new String[]{"C~~~C~IiSOo~C~~~C", "CCCCC~CCCCC~CCCCC", "C~~~C~CCCCC~C~~~C", "CCCCC~CCCCC~CCCCC", "C~~~C~CMECC~C~~~C"} :
                    new String[]{"C~~~C~IiSOo~~~~~~", "CCCCC~CCCCC~~~~~~", "C~~~C~CCCCC~~~~~~", "CCCCC~CCCCC~~~~~~", "C~~~C~CMECC~~~~~~"});
            MultiblockShapeInfo[] infos = new MultiblockShapeInfo[15];
            for (int tier = 0; tier < infos.length; tier++) {
                infos[tier] = builder.where('S', this.getController(), WEST)
                        .where('C', GAMetaBlocks.MUTLIBLOCK_CASING.getState(GAMultiblockCasing.CasingType.CHEMICALLY_INERT))
                        .where('c', MetaBlocks.WIRE_COIL.getState(BlockWireCoil.CoilType.CUPRONICKEL))
                        .where('P', GAMetaBlocks.MUTLIBLOCK_CASING.getState(GAMultiblockCasing.CasingType.PTFE_PIPE))
                        .where('F', MetaBlocks.FRAMES.get(Steel).getDefaultState())
                        .where('p', GAMetaBlocks.PUMP_CASING.getState(PumpCasing.CasingType.values()[Math.max(0, tier - 1)]))
                        .where('m', GAMetaBlocks.MOTOR_CASING.getState(MotorCasing.CasingType.values()[Math.max(0, tier - 1)]))
                        .where('M', GATileEntities.MAINTENANCE_HATCH[0], EAST)
                        .where('E', this.getEnergyHatch(tier, false), EAST)
                        .where('I', MetaTileEntities.FLUID_IMPORT_HATCH[GTValues.IV], WEST)
                        .where('i', MetaTileEntities.ITEM_IMPORT_BUS[GTValues.IV], WEST)
                        .where('O', MetaTileEntities.FLUID_EXPORT_HATCH[GTValues.IV], WEST)
                        .where('o', MetaTileEntities.ITEM_EXPORT_BUS[GTValues.IV], WEST)
                        .build();
            }
            shapeInfos.add(infos);
        }
        return shapeInfos;
    }

    @Override
    protected void generateBlockTooltips() {
        super.generateBlockTooltips();
        this.addBlockTooltip(GAMetaBlocks.MUTLIBLOCK_CASING.getItemVariant(GAMultiblockCasing.CasingType.CHEMICALLY_INERT), new TextComponentTranslation("gregtech.multiblock.preview.limit", 0)
                .setStyle(new Style().setColor(TextFormatting.RED)));
        this.addBlockTooltip(GAMetaBlocks.PUMP_CASING.getItemVariant(PumpCasing.CasingType.PUMP_LV), COMPONENT_BLOCK_TOOLTIP);
        this.addBlockTooltip(GAMetaBlocks.MOTOR_CASING.getItemVariant(MotorCasing.CasingType.MOTOR_LV), COMPONENT_BLOCK_TOOLTIP);
    }

    @Override
    public String[] getDescription() {
        return ArrayUtils.addAll(new String[] {
                I18n.format("tj.multiblock.advanced_parallel_chemical_reactor.description"),
                I18n.format("tj.multiblock.parallel.description"),
                I18n.format("tj.multiblock.parallel.extend.tooltip"),
                I18n.format("tj.multiblock.parallel.chemical_plant.description"),
                I18n.format("gtadditions.multiblock.large_chemical_reactor.tooltip.1"),
                I18n.format("gtadditions.multiblock.large_chemical_reactor.tooltip.2").replace("§7", "§0").replace("§r", "§r§0"),
                I18n.format("gtadditions.multiblock.large_chemical_reactor.tooltip.3")},
                super.getDescription());
    }

    @Override
    public float getDefaultZoom() {
        return 0.5f;
    }
}
