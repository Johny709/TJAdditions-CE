package tj.integration.jei.multi.parallel;

import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.GAMultiblockCasing;
import gregicadditions.item.components.MotorCasing;
import gregicadditions.item.components.PumpCasing;
import gregicadditions.machines.GATileEntities;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.integration.jei.multiblock.MultiblockShapeInfo;
import gregtech.integration.jei.multiblock.channel.PlaceholderType;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.ArrayUtils;
import tj.TJConfig;
import tj.integration.jei.TJMultiblockInfoPage;
import tj.integration.jei.TJMultiblockShapeInfo;
import tj.machines.TJMetaTileEntities;
import tj.machines.multi.parallel.MetaTileEntityParallelAdvancedLargeChemicalReactor;

import java.util.ArrayList;
import java.util.List;

import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;
import static gregtech.api.unification.material.Materials.Steel;
import static net.minecraft.util.EnumFacing.EAST;
import static net.minecraft.util.EnumFacing.WEST;


public class ParallelAdvancedChemicalReactorInfo extends TJMultiblockInfoPage implements IParallelMultiblockInfoPage {

    @Override
    public MetaTileEntityParallelAdvancedLargeChemicalReactor getController() {
        return TJMetaTileEntities.ADVANCED_PARALLEL_CHEMICAL_REACTOR;
    }

    @Override
    public MultiblockShapeInfo getMatchingShapes(int extent) {
        final TJMultiblockShapeInfo.Builder builder = TJMultiblockShapeInfo.builder(FRONT, RIGHT, DOWN);
        if (!(extent % 2 == 0)) {
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
        for (int layer = 1; layer < extent; layer++) {
            if (layer % 2 == 0) {
                builder.aisle("C~~~C~F~~~F~C~~~C", "CCCCC~~~P~~~CCCCC", "C~~~C~~PpP~~C~~~C", "CCCCC~~~P~~~CCCCC", "C~~~C~F~~~F~C~~~C");
                builder.aisle("CCCCC~F~~~F~CCCCC", "CcccC~~~P~~~CcccC", "CPPPPPPPpPPPPPPPC", "CcccC~~~P~~~CcccC", "CCCCC~F~~~F~CCCCC");
                builder.aisle("C~~~C~F~~~F~C~~~C", "CPPPPPPPPPPPPPPPC", "CmmmC~~PpP~~CmmmC", "CPPPPPPPPPPPPPPPC", "C~~~C~F~~~F~C~~~C");
                builder.aisle("CCCCC~F~~~F~CCCCC", "CcccC~~~P~~~CcccC", "CPPPPPPPpPPPPPPPC", "CcccC~~~P~~~CcccC", "CCCCC~F~~~F~CCCCC");
            }
        }
        return builder.aisle(extent > 1 ?
                new String[]{"C~~~C~IiSOo~C~~~C", "CCCCC~CCCCC~CCCCC", "C~~~C~CCCCC~C~~~C", "CCCCC~CCCCC~CCCCC", "C~~~C~CMECC~C~~~C"} :
                new String[]{"C~~~C~IiSOo~~~~~~", "CCCCC~CCCCC~~~~~~", "C~~~C~CCCCC~~~~~~", "CCCCC~CCCCC~~~~~~", "C~~~C~CMECC~~~~~~"})
       .where('S', this.getController(), WEST)
                    .where('C', GAMetaBlocks.MUTLIBLOCK_CASING.getState(GAMultiblockCasing.CasingType.CHEMICALLY_INERT))
                    .where('c', PlaceholderType.COIL)
                    .where('P', GAMetaBlocks.MUTLIBLOCK_CASING.getState(GAMultiblockCasing.CasingType.PTFE_PIPE))
                    .where('F', MetaBlocks.FRAMES.get(Steel).getDefaultState())
                    .where('p', PlaceholderType.PUMP,GAMetaBlocks.PUMP_CASING.getState(PumpCasing.CasingType.values()[0]))
                    .where('m', PlaceholderType.MOTOR , GAMetaBlocks.MOTOR_CASING.getState(MotorCasing.CasingType.values()[0]))
                    .where('M', GATileEntities.MAINTENANCE_HATCH[0], EAST)
                    .where('E', PlaceholderType.ENERGY_INPUT_HATCH, this.getEnergyHatch(0, false), EAST)
                    .where('i', PlaceholderType.INPUT_HATCH, MetaTileEntities.FLUID_IMPORT_HATCH[0], WEST)
                    .where('I', PlaceholderType.INPUT_BUS , MetaTileEntities.ITEM_IMPORT_BUS[0], WEST)
                    .where('o', PlaceholderType.OUTPUT_HATCH ,MetaTileEntities.FLUID_EXPORT_HATCH[0], WEST)
                    .where('O', PlaceholderType.OUTPUT_BUS, MetaTileEntities.ITEM_EXPORT_BUS[0], WEST)
                    .build();
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
