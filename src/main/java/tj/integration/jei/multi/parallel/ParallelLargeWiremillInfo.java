package tj.integration.jei.multi.parallel;

import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.components.MotorCasing;
import gregicadditions.item.metal.MetalCasing1;
import gregicadditions.machines.GATileEntities;
import gregtech.common.blocks.BlockTurbineCasing;
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
import tj.machines.multi.parallel.MetaTileEntityParallelLargeWiremill;

import java.util.ArrayList;
import java.util.List;

import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;
import static net.minecraft.util.EnumFacing.EAST;
import static net.minecraft.util.EnumFacing.WEST;


public class ParallelLargeWiremillInfo extends TJMultiblockInfoPage implements IParallelMultiblockInfoPage {

    @Override
    public MetaTileEntityParallelLargeWiremill getController() {
        return TJMetaTileEntities.PARALLEL_LARGE_WIREMILL;
    }

    @Override
    public MultiblockShapeInfo getMatchingShapes(int extent) {

        final TJMultiblockShapeInfo.Builder builder = new TJMultiblockShapeInfo.Builder(FRONT, UP, LEFT);
        for (int layer = 0; layer < extent; layer++) {
            String energyE = layer == 0 ? "CCECC" : "CCCCC";
            builder.aisle(energyE, "~CCC~", "~~~~~", "~~~~~", "~~~~~");
            builder.aisle("CCCCC", "CmGmC", "C###C", "CmGmC", "CCCCC");
        }
        return builder.aisle("CCMCC", "~ISO~", "~~~~~", "~~~~~", "~~~~~").where('S', this.getController(), WEST)
                    .where('C', GAMetaBlocks.METAL_CASING_1.getState(MetalCasing1.CasingType.MARAGING_STEEL_250))
                    .where('G', MetaBlocks.TURBINE_CASING.getState(BlockTurbineCasing.TurbineCasingType.TITANIUM_GEARBOX))
                    .where('m', PlaceholderType.MOTOR , GAMetaBlocks.MOTOR_CASING.getState(MotorCasing.CasingType.values()[0]))
                    .where('M', GATileEntities.MAINTENANCE_HATCH[0], WEST)
                    .where('E', PlaceholderType.ENERGY_INPUT_HATCH, this.getEnergyHatch(0, false), EAST)
                    .where('I', PlaceholderType.INPUT_BUS , MetaTileEntities.ITEM_IMPORT_BUS[0], WEST)
                    .where('O', MetaTileEntities.ITEM_EXPORT_BUS[0], WEST)
                    .build();

    }

    @Override
    protected void generateBlockTooltips() {
        super.generateBlockTooltips();
        this.addBlockTooltip(GAMetaBlocks.METAL_CASING_1.getItemVariant(MetalCasing1.CasingType.MARAGING_STEEL_250), new TextComponentTranslation("gregtech.multiblock.preview.limit", 12)
                .setStyle(new Style().setColor(TextFormatting.RED)));
        this.addBlockTooltip(GAMetaBlocks.MOTOR_CASING.getItemVariant(MotorCasing.CasingType.MOTOR_LV), COMPONENT_BLOCK_TOOLTIP);
    }

    @Override
    public String[] getDescription() {
        return ArrayUtils.addAll(new String[] {
                I18n.format("tj.multiblock.parallel_large_wiremill.description"),
                I18n.format("tj.multiblock.parallel.description")},
                super.getDescription());
    }

    @Override
    public float getDefaultZoom() {
        return 0.5f;
    }
}
