package tj.integration.jei.multi.electric;

import gregicadditions.item.CellCasing;
import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.metal.MetalCasing1;
import gregicadditions.machines.GATileEntities;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.integration.jei.multiblock.MultiblockShapeInfo;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumFacing;
import tj.integration.jei.TJMultiblockInfoPage;
import tj.machines.TJMetaTileEntities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static gregicadditions.GAMaterials.Talonite;

public class LargeBatteryChargerInfo extends TJMultiblockInfoPage {

    @Override
    public MultiblockControllerBase getController() {
        return TJMetaTileEntities.LARGE_BATTERY_CHARGER;
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        List<MultiblockShapeInfo> shapeInfos = new ArrayList<>();
        MultiblockShapeInfo.Builder builder = MultiblockShapeInfo.builder()
                .aisle("CCCCC", "~CCC~", "~C~C~", "~C~C~", "~C~C~", "~C~C~", "~C~C~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~")
                .aisle("iCCCC", "ICCCC", "CFBFC", "CFBFC", "CFBFC", "CFBFC", "CFBFC", "~CCC~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~")
                .aisle("MCCCE", "SCCCe", "~BFB~", "~BFB~", "~BFB~", "~BFB~", "~BFB~", "~CFC~", "~~F~~", "~~F~~", "~~F~~", "~~F~~", "~~F~~")
                .aisle("CCCCC", "OCCCC", "CFBFC", "CFBFC", "CFBFC", "CFBFC", "CFBFC", "~CCC~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~")
                .aisle("CCCCC", "~CCC~", "~C~C~", "~C~C~", "~C~C~", "~C~C~", "~C~C~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~")
                .where('S', this.getController(), EnumFacing.WEST)
                .where('C', GAMetaBlocks.METAL_CASING_1.getState(MetalCasing1.CasingType.TALONITE))
                .where('F', MetaBlocks.FRAMES.get(Talonite).getDefaultState())
                .where('M', GATileEntities.MAINTENANCE_HATCH[0], EnumFacing.WEST);
        for (int tier = 0; tier < 15; tier++) {
            shapeInfos.add(builder.where('e', this.getEnergyHatch(tier, true), EnumFacing.EAST)
                    .where('E', this.getEnergyHatch(tier, false), EnumFacing.EAST)
                    .where('I', MetaTileEntities.ITEM_IMPORT_BUS[tier], EnumFacing.WEST)
                    .where('O', MetaTileEntities.ITEM_EXPORT_BUS[tier], EnumFacing.WEST)
                    .where('i', MetaTileEntities.FLUID_IMPORT_HATCH[tier], EnumFacing.WEST)
                    .where('B', GAMetaBlocks.CELL_CASING.getState(CellCasing.CellType.values()[Math.max(0, tier - 3)]))
                    .build());
        }
        return shapeInfos;
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
