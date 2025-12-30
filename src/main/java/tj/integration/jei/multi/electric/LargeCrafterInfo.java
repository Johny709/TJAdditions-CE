package tj.integration.jei.multi.electric;

import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.GAMultiblockCasing;
import gregicadditions.item.GATransparentCasing;
import gregicadditions.item.components.ConveyorCasing;
import gregicadditions.item.components.RobotArmCasing;
import gregicadditions.jei.GAMultiblockShapeInfo;
import gregicadditions.machines.GATileEntities;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.integration.jei.multiblock.MultiblockShapeInfo;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import tj.TJConfig;
import tj.integration.jei.TJMultiblockInfoPage;
import tj.machines.TJMetaTileEntities;
import tj.util.Color;
import tj.util.TooltipHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;
import static gregtech.api.unification.material.Materials.Osmiridium;

public class LargeCrafterInfo extends TJMultiblockInfoPage {

    @Override
    public MultiblockControllerBase getController() {
        return TJMetaTileEntities.LARGE_CRAFTER;
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        List<MultiblockShapeInfo> shapeInfos = new ArrayList<>();
        GAMultiblockShapeInfo.Builder builder = GAMultiblockShapeInfo.builder(FRONT, UP, LEFT)
                .aisle("CCCCC", "FCCCF", "FCECF", "FCCCF", "~CCC~")
                .aisle("CCCCC", "G#c#G", "GR#RG", "F#c#F", "~CCC~")
                .aisle("CCCCC", "G#c#G", "GR#RG", "F#c#F", "~CCC~")
                .aisle("CCCCC", "G#c#G", "GR#RG", "F#c#F", "~CCC~")
                .aisle("CCCCC", "FIMOF", "FCSCF", "FCHCF", "~CCC~")
                .where('S', this.getController(), EnumFacing.WEST)
                .where('C', GAMetaBlocks.MUTLIBLOCK_CASING.getState(GAMultiblockCasing.CasingType.LARGE_ASSEMBLER))
                .where('F', MetaBlocks.FRAMES.get(Osmiridium).getDefaultState())
                .where('M', GATileEntities.MAINTENANCE_HATCH[0], EnumFacing.WEST);
        for (int tier = 0; tier < 15; tier++) {
            shapeInfos.add(builder.where('E', this.getEnergyHatch(tier, false), EnumFacing.EAST)
                    .where('I', MetaTileEntities.ITEM_IMPORT_BUS[Math.min(9, tier)], EnumFacing.WEST)
                    .where('O', MetaTileEntities.ITEM_EXPORT_BUS[Math.min(9, tier)], EnumFacing.WEST)
                    .where('H', TJMetaTileEntities.CRAFTER_HATCHES[Math.max(0, tier -1)], EnumFacing.WEST)
                    .where('c', GAMetaBlocks.CONVEYOR_CASING.getState(ConveyorCasing.CasingType.values()[Math.max(0, tier -1)]))
                    .where('R', GAMetaBlocks.ROBOT_ARM_CASING.getState(RobotArmCasing.CasingType.values()[Math.max(0, tier - 1)]))
                    .where('G', GAMetaBlocks.TRANSPARENT_CASING.getState(GATransparentCasing.CasingType.values()[Math.min(6, tier)]))
                    .build());
        }
        return shapeInfos;
    }

    @Override
    protected void generateBlockTooltips() {
        super.generateBlockTooltips();
        this.addBlockTooltip(MetaBlocks.METAL_CASING.getItemVariant(BlockMetalCasing.MetalCasingType.STEEL_SOLID), new TextComponentTranslation("gregtech.multiblock.preview.limit", 25)
                .setStyle(new Style().setColor(TextFormatting.RED)));
        Arrays.stream(GATransparentCasing.CasingType.values()).forEach(casingType -> this.addBlockTooltip(GAMetaBlocks.TRANSPARENT_CASING.getItemVariant(casingType), COMPONENT_TIER_ANY_TOOLTIP));
        Arrays.stream(ConveyorCasing.CasingType.values()).forEach(casingType -> this.addBlockTooltip(GAMetaBlocks.CONVEYOR_CASING.getItemVariant(casingType), COMPONENT_BLOCK_TOOLTIP));
        Arrays.stream(RobotArmCasing.CasingType.values()).forEach(casingType -> this.addBlockTooltip(GAMetaBlocks.ROBOT_ARM_CASING.getItemVariant(casingType), COMPONENT_BLOCK_TOOLTIP));
    }

    @Override
    public String[] getDescription() {
        return new String[]{I18n.format("gtadditions.multiblock.universal.tooltip.4", TJConfig.largeArchitectWorkbench.stack).replace("§r", "§7"),
                I18n.format("tj.multiblock.large_crafter.description"),
                TooltipHelper.blinkingText(Color.YELLOW,  20, "tj.multiblock.large_crafter.warning")};
    }
}
