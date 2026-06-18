package tj.integration.jei.multi.electric;

import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.GAMultiblockCasing;
import gregicadditions.item.GATransparentCasing;
import gregicadditions.item.components.ConveyorCasing;
import gregicadditions.item.components.RobotArmCasing;
import gregicadditions.machines.GATileEntities;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.integration.jei.multiblock.MultiblockShapeInfo;
import gregtech.integration.jei.multiblock.channel.PlaceholderType;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import tj.TJConfig;
import tj.integration.jei.TJMultiblockInfoPage;
import tj.integration.jei.TJMultiblockShapeInfo;
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
    public MultiblockShapeInfo getMatchingShapes(int extent) {
        return TJMultiblockShapeInfo.builder(FRONT, UP, LEFT)
                .aisle("CCCCC", "FCCCF", "FCECF", "FCCCF", "~CCC~")
                .aisle("CCCCC", "G#c#G", "GR#RG", "F#c#F", "~CCC~")
                .aisle("CCCCC", "G#c#G", "GR#RG", "F#c#F", "~CCC~")
                .aisle("CCCCC", "G#c#G", "GR#RG", "F#c#F", "~CCC~")
                .aisle("CCCCC", "FIMOF", "FCSCF", "FCHCF", "~CCC~")
                .where('S', this.getController(), EnumFacing.WEST)
                .where('C', GAMetaBlocks.MUTLIBLOCK_CASING.getState(GAMultiblockCasing.CasingType.LARGE_ASSEMBLER))
                .where('F', MetaBlocks.FRAMES.get(Osmiridium).getDefaultState())
                .where('M', GATileEntities.MAINTENANCE_HATCH[0], EnumFacing.WEST)
                .where('E', PlaceholderType.ENERGY_INPUT_HATCH, this.getEnergyHatch(0, false), EnumFacing.EAST)
                .where('I', PlaceholderType.INPUT_BUS , MetaTileEntities.ITEM_IMPORT_BUS[0], EnumFacing.WEST)
                .where('O', PlaceholderType.OUTPUT_BUS, MetaTileEntities.ITEM_EXPORT_BUS[0], EnumFacing.WEST)
                .where('H', PlaceholderType.CRAFTER_HATCH, TJMetaTileEntities.CRAFTER_HATCHES[0], EnumFacing.WEST)
                .where('c', GAMetaBlocks.CONVEYOR_CASING.getState(ConveyorCasing.CasingType.values()[0]))
                .where('r', PlaceholderType.ROBOT_ARM, GAMetaBlocks.ROBOT_ARM_CASING.getState(RobotArmCasing.CasingType.values()[0]))
                .where('G', PlaceholderType.GLASS, GAMetaBlocks.TRANSPARENT_CASING.getState(GATransparentCasing.CasingType.values()[0]))
                .build();
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
        return new String[]{I18n.format("gtadditions.multiblock.universal.tooltip.4", TJConfig.largeArchitectWorkbench.stack).replace("§7", "§r"),
                I18n.format("tj.multiblock.large_crafter.description"),
                I18n.format("tj.multiblock.large_crafter.description.1").replace("§7", "§r"),
                TooltipHelper.blinkingText(Color.YELLOW,  20, "tj.multiblock.large_crafter.warning")};
    }
}
