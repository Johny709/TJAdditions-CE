package tj.integration.jei.multi.electric;

import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.GAMultiblockCasing;
import gregicadditions.item.GAMultiblockCasing2;
import gregicadditions.machines.GATileEntities;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.common.blocks.BlockBoilerCasing;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.BlockMultiblockCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.integration.jei.multiblock.MultiblockShapeInfo;
import gregtech.integration.jei.multiblock.channel.PlaceholderType;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.ArrayUtils;
import tj.TJConfig;
import tj.integration.jei.TJMultiblockInfoPage;
import tj.integration.jei.TJMultiblockShapeInfo;
import tj.machines.TJMetaTileEntities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;

public class TJMegaVacuumFreezerInfo extends TJMultiblockInfoPage {

    @Override
    public MultiblockControllerBase getController() {
        return TJMetaTileEntities.MEGA_VACUUM_FREEZER;
    }

    @Override
    public MultiblockShapeInfo getMatchingShapes(int extent) {
        return TJMultiblockShapeInfo.builder(FRONT, UP, LEFT)
                .aisle("~CCCCC~", "~CCECC~", "~CCCCC~", "~CCCCC~", "~CCCCC~", "~CCCCC~", "~CCCCC~")
                .aisle("CCCCCCC", "CP#F#PC", "CP###PC", "CPPPPPC", "CP###PC", "CP#F#PC", "CCCCCCC")
                .aisle("CCCCCCC", "C#####C", "C#####C", "CPGGGPC", "C#####C", "C#####C", "CCCCCCC")
                .aisle("CCCCCCC", "C#####C", "C#####C", "CPGGGPC", "C#####C", "C#####C", "CCCCCCC")
                .aisle("CCCCCCC", "C#####C", "C#####C", "CPGGGPC", "C#####C", "C#####C", "CCCCCCC")
                .aisle("CCCCCCC", "CP#F#PC", "CP###PC", "CPPPPPC", "CP###PC", "CP#F#PC", "CCCCCCC")
                .aisle("~IOMCC~", "~ioSCC~", "~CCCCC~", "~CCCCC~", "~CCCCC~", "~CCCCC~", "~CCCCC~")
                .where('S', this.getController(), EnumFacing.WEST)
                .where('M', GATileEntities.MAINTENANCE_HATCH[0], EnumFacing.WEST)
                .where('C', MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.ALUMINIUM_FROSTPROOF))
                .where('P', MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.TUNGSTENSTEEL_PIPE))
                .where('G', MetaBlocks.MUTLIBLOCK_CASING.getState(BlockMultiblockCasing.MultiblockCasingType.GRATE_CASING))
                .where('E', PlaceholderType.ENERGY_INPUT_HATCH, this.getEnergyHatch(0, false), EnumFacing.EAST)
                    .where('I', PlaceholderType.INPUT_BUS , MetaTileEntities.ITEM_IMPORT_BUS[0], EnumFacing.WEST)
                    .where('O', PlaceholderType.OUTPUT_BUS, MetaTileEntities.ITEM_EXPORT_BUS[0], EnumFacing.WEST)
                    .where('i', PlaceholderType.INPUT_HATCH, MetaTileEntities.FLUID_IMPORT_HATCH[0], EnumFacing.WEST)
                    .where('o', PlaceholderType.OUTPUT_HATCH ,MetaTileEntities.FLUID_EXPORT_HATCH[0], EnumFacing.WEST)
                    .where('F', PlaceholderType.FRAMEWORK)
                    .build();
    }

    @Override
    protected void generateBlockTooltips() {
        this.addBlockTooltip(MetaBlocks.METAL_CASING.getItemVariant(BlockMetalCasing.MetalCasingType.ALUMINIUM_FROSTPROOF), new TextComponentTranslation("gregtech.multiblock.preview.limit", 100)
                .setStyle(new Style().setColor(TextFormatting.RED)));
        Arrays.stream(GAMultiblockCasing.CasingType.values()).forEach(casingType -> this.addBlockTooltip(GAMetaBlocks.MUTLIBLOCK_CASING.getItemVariant(casingType), COMPONENT_BLOCK_TOOLTIP));
        Arrays.stream(GAMultiblockCasing2.CasingType.values()).forEach(casingType -> this.addBlockTooltip(GAMetaBlocks.MUTLIBLOCK_CASING2.getItemVariant(casingType), COMPONENT_BLOCK_TOOLTIP));
    }

    @Override
    public String[] getDescription() {
        return ArrayUtils.addAll(new String[]{I18n.format("tj.multiblock.temporary"),
                        I18n.format("tj.multiblock.mega").replace("§7", "§r")},
                super.getDescription());
    }

    @Override
    public float getDefaultZoom() {
        return 0.5F;
    }
}
