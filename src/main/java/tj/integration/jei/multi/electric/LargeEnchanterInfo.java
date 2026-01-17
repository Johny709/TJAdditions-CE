package tj.integration.jei.multi.electric;

import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.GATransparentCasing;
import gregicadditions.item.components.EmitterCasing;
import gregicadditions.item.metal.MetalCasing2;
import gregicadditions.jei.GAMultiblockShapeInfo;
import gregicadditions.machines.GATileEntities;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.integration.jei.multiblock.MultiblockShapeInfo;
import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import tj.integration.jei.TJMultiblockInfoPage;
import tj.machines.TJMetaTileEntities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;
import static gregtech.api.unification.material.Materials.BlackSteel;

public class LargeEnchanterInfo extends TJMultiblockInfoPage {

    @Override
    public MultiblockControllerBase getController() {
        return TJMetaTileEntities.LARGE_ENCHANTER;
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        List<MultiblockShapeInfo> shapeInfos = new ArrayList<>();
        GAMultiblockShapeInfo.Builder builder = GAMultiblockShapeInfo.builder(FRONT, RIGHT, DOWN)
                .aisle("~~~~~~~", "~~~~~~~", "~~~C~~~", "~~CCC~~", "~~~C~~~", "~~~~~~~", "~~~~~~~")
                .aisle("~~~~~~~", "~~~~~~~", "~~CCC~~", "~~CeC~~", "~~CCC~~", "~~~~~~~", "~~~~~~~")
                .aisle("~~~~~~~", "~C~~~C~", "~~CGC~~", "~~G#G~~", "~~CGC~~", "~C~~~C~", "~~~~~~~")
                .aisle("~~~~~~~", "~CFFFC~", "~FCCCF~", "~FCCCF~", "~FCCCF~", "~CFFFC~", "~~~~~~~")
                .aisle("~~~~~~~", "~CCGCC~", "~C###C~", "~G###G~", "~C###C~", "~CCGCC~", "~~~~~~~")
                .aisle("~~~~~~~", "~CGGGC~", "~GBBBG~", "~GB#BG~", "~GBBBG~", "~CGGGC~", "~~~~~~~")
                .aisle("~~~~~~~", "~CGGGC~", "~GBBBG~", "~GB#BG~", "~GBBBG~", "~CGGGC~", "~~~~~~~")
                .aisle("~~~~~~~", "~CGGGC~", "~GBBBG~", "~GB#BG~", "~GBBBG~", "~CGGGC~", "~~~~~~~")
                .aisle("~~~~~~~", "~iISOC~", "~CoooC~", "~CoooC~", "~CoooC~", "~CCECC~", "~~~~~~~")
                .aisle("~CCMCC~", "CCCCCCC", "CCCCCCC", "CCCCCCC", "CCCCCCC", "CCCCCCC", "~CCCCC~")
                .where('S', this.getController(), EnumFacing.WEST)
                .where('C', GAMetaBlocks.METAL_CASING_2.getState(MetalCasing2.CasingType.BLACK_STEEL))
                .where('o', Blocks.OBSIDIAN.getDefaultState())
                .where('B', Block.getBlockFromName("apotheosis:hellshelf").getDefaultState())
                .where('F', MetaBlocks.FRAMES.get(BlackSteel).getDefaultState())
                .where('M', GATileEntities.MAINTENANCE_HATCH[0], EnumFacing.WEST);
        for (int tier = 0; tier < 15; tier++) {
            shapeInfos.add(builder.where('E', this.getEnergyHatch(tier, false), EnumFacing.EAST)
                    .where('I', MetaTileEntities.ITEM_IMPORT_BUS[Math.min(9 - 1, tier)], EnumFacing.WEST)
                    .where('O', MetaTileEntities.ITEM_EXPORT_BUS[Math.min(9 - 1, tier)], EnumFacing.WEST)
                    .where('i', MetaTileEntities.FLUID_IMPORT_HATCH[Math.min(9 - 1, tier)], EnumFacing.WEST)
                    .where('e', GAMetaBlocks.EMITTER_CASING.getState(EmitterCasing.CasingType.values()[Math.max(0, tier - 1)]))
                    .where('G', GAMetaBlocks.TRANSPARENT_CASING.getState(GATransparentCasing.CasingType.values()[Math.min(6, tier)]))
                    .build());
        }
        return shapeInfos;
    }

    @Override
    protected void generateBlockTooltips() {
        super.generateBlockTooltips();
        this.addBlockTooltip(GAMetaBlocks.METAL_CASING_2.getItemVariant(MetalCasing2.CasingType.BLACK_STEEL), new TextComponentTranslation("gregtech.multiblock.preview.limit", 64)
                .setStyle(new Style().setColor(TextFormatting.RED)));
        Arrays.stream(GATransparentCasing.CasingType.values()).forEach(casingType -> this.addBlockTooltip(GAMetaBlocks.TRANSPARENT_CASING.getItemVariant(casingType), COMPONENT_TIER_ANY_TOOLTIP));
        Arrays.stream(EmitterCasing.CasingType.values()).forEach(casingType -> this.addBlockTooltip(GAMetaBlocks.EMITTER_CASING.getItemVariant(casingType), COMPONENT_BLOCK_TOOLTIP));
    }


    @Override
    public String[] getDescription() {
        return new String[]{I18n.format("tj.multiblock.large_enchanter.description")};
    }

    @Override
    public float getDefaultZoom() {
        return 0.5f;
    }
}
