package tj.integration.jei.multi.electric;

import gregicadditions.item.CellCasing;
import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.metal.MetalCasing1;
import gregicadditions.machines.GATileEntities;
import gregtech.api.GTValues;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.integration.jei.multiblock.MultiblockShapeInfo;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumFacing;
import tj.integration.jei.TJMultiblockInfoPage;
import tj.machines.TJMetaTileEntities;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static gregicadditions.GAMaterials.Talonite;

public class LargeBatteryChargerInfo extends TJMultiblockInfoPage {

    @Override
    public MultiblockControllerBase getController() {
        return TJMetaTileEntities.LARGE_BATTERY_CHARGER;
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        MultiblockShapeInfo.Builder shapeInfo = MultiblockShapeInfo.builder()
                .aisle("CCCCC", "~CCC~", "~C~C~", "~C~C~", "~C~C~", "~C~C~", "~C~C~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~")
                .aisle("iCCCC", "ICCCC", "CFBFC", "CFBFC", "CFBFC", "CFBFC", "CFBFC", "~CCC~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~")
                .aisle("MCCCE", "SCCCe", "~BFB~", "~BFB~", "~BFB~", "~BFB~", "~BFB~", "~CFC~", "~~F~~", "~~F~~", "~~F~~", "~~F~~", "~~F~~")
                .aisle("CCCCC", "OCCCC", "CFBFC", "CFBFC", "CFBFC", "CFBFC", "CFBFC", "~CCC~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~")
                .aisle("CCCCC", "~CCC~", "~C~C~", "~C~C~", "~C~C~", "~C~C~", "~C~C~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~")
                .where('S', this.getController(), EnumFacing.WEST)
                .where('C', GAMetaBlocks.METAL_CASING_1.getState(MetalCasing1.CasingType.TALONITE))
                .where('F', MetaBlocks.FRAMES.get(Talonite).getDefaultState())
                .where('M', GATileEntities.MAINTENANCE_HATCH[0], EnumFacing.WEST)
                .where('I', MetaTileEntities.ITEM_IMPORT_BUS[GTValues.UV], EnumFacing.WEST)
                .where('O', MetaTileEntities.ITEM_EXPORT_BUS[GTValues.UV], EnumFacing.WEST)
                .where('i', MetaTileEntities.FLUID_IMPORT_HATCH[GTValues.UV], EnumFacing.WEST);
        return Arrays.stream(CellCasing.CellType.values())
                .map(cellType -> shapeInfo.where('B', GAMetaBlocks.CELL_CASING.getState(cellType))
                        .where('e', this.getEnergyHatch(cellType.getTier(), true), EnumFacing.EAST)
                        .where('E', this.getEnergyHatch(cellType.getTier(), false), EnumFacing.EAST)
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    protected void generateBlockTooltips() {
        super.generateBlockTooltips();
        Arrays.stream(CellCasing.CellType.values()).forEach(cellType -> this.addBlockTooltip(GAMetaBlocks.CELL_CASING.getItemVariant(cellType), COMPONENT_BLOCK_TOOLTIP));
    }

    @Override
    public String[] getDescription() {
        return new String[] {
                I18n.format("tj.multiblock.large_battery_charger.description").replace("§r", "§7")};
    }

    @Override
    public float getDefaultZoom() {
        return 0.5f;
    }
}
