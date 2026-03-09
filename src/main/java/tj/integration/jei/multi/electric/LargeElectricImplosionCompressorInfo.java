package tj.integration.jei.multi.electric;

import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.components.PistonCasing;
import gregicadditions.item.metal.MetalCasing1;
import gregicadditions.jei.GAMultiblockShapeInfo;
import gregicadditions.machines.GATileEntities;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.common.blocks.BlockMultiblockCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.integration.jei.multiblock.MultiblockShapeInfo;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumFacing;
import org.apache.commons.lang3.ArrayUtils;
import tj.integration.jei.TJMultiblockInfoPage;
import tj.machines.TJMetaTileEntities;

import java.util.ArrayList;
import java.util.List;

import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;

public class LargeElectricImplosionCompressorInfo extends TJMultiblockInfoPage {

    @Override
    public MultiblockControllerBase getController() {
        return TJMetaTileEntities.LARGE_ELECTRIC_IMPLOSION_COMPRESSOR;
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        List<MultiblockShapeInfo> shapeInfos = new ArrayList<>();
        GAMultiblockShapeInfo.Builder builder = GAMultiblockShapeInfo.builder(FRONT, UP, LEFT)
                .aisle("~C~C~", "~G~G~", "~G~G~", "~G~G~", "~G~G~", "~G~G~", "~C~C~", "CCCCC", "CCECC", "CCCCC", "~C~C~", "~G~G~", "~G~G~", "~G~G~", "~G~G~", "~G~G~", "~C~C~")
                .aisle("CCCCC", "GCGCG", "GCGCG", "GCGCG", "GCGCG", "GCGCG", "C#C#C", "C###C", "C###C", "C###C", "C#C#C", "GCGCG", "GCGCG", "GCGCG", "GCGCG", "GCGCG", "CCCCC")
                .aisle("~CCC~", "~GPG~", "~GPG~", "~GPG~", "~GPG~", "~GPG~", "~C#C~", "C###C", "C###C", "C###C", "~C#C~", "~GPG~", "~GPG~", "~GPG~", "~GPG~", "~GPG~", "~CCC~")
                .aisle("CCCCC", "GCGCG", "GCGCG", "GCGCG", "GCGCG", "GCGCG", "C#C#C", "C###C", "C###C", "C###C", "C#C#C", "GCGCG", "GCGCG", "GCGCG", "GCGCG", "GCGCG", "CCCCC")
                .aisle("~C~C~", "~G~G~", "~G~G~", "~G~G~", "~G~G~", "~G~G~", "~C~C~", "CCCCC", "CISOC", "CCMCC", "~C~C~", "~G~G~", "~G~G~", "~G~G~", "~G~G~", "~G~G~", "~C~C~")
                .where('S', this.getController(), EnumFacing.WEST)
                .where('M', GATileEntities.MAINTENANCE_HATCH[0], EnumFacing.EAST)
                .where('C', GAMetaBlocks.METAL_CASING_1.getState(MetalCasing1.CasingType.INCOLOY_MA956))
                .where('G', MetaBlocks.MUTLIBLOCK_CASING.getState(BlockMultiblockCasing.MultiblockCasingType.GRATE_CASING));
        for (int tier = 0; tier < 15; tier++) {
            shapeInfos.add(builder.where('E', this.getEnergyHatch(tier, false), EnumFacing.EAST)
                    .where('I', MetaTileEntities.ITEM_IMPORT_BUS[tier], EnumFacing.WEST)
                    .where('O', MetaTileEntities.ITEM_EXPORT_BUS[tier], EnumFacing.WEST)
                    .where('P', GAMetaBlocks.PISTON_CASING.getState(PistonCasing.CasingType.values()[Math.max(0, tier - 1)]))
                    .build());
        }
        return shapeInfos;
    }

    @Override
    public float getDefaultZoom() {
        return 0.5F;
    }

    @Override
    public String[] getDescription() {
        return ArrayUtils.addAll(new String[]{I18n.format("tj.multiblock.large_electric_implosion_compressor.description").replace("§7", "§r")},
                super.getDescription());
    }
}
