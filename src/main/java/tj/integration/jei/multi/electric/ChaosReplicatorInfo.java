package tj.integration.jei.multi.electric;

import gregicadditions.GAMaterials;
import gregicadditions.machines.GATileEntities;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.util.BlockInfo;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.integration.jei.multiblock.MultiblockShapeInfo;
import gregtech.integration.jei.multiblock.channel.PlaceholderType;
import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumFacing;
import org.apache.commons.lang3.ArrayUtils;
import tj.TJConfig;
import tj.integration.jei.TJMultiblockInfoPage;
import tj.integration.jei.TJMultiblockShapeInfo;
import tj.machines.TJMetaTileEntities;

import java.util.ArrayList;
import java.util.List;

import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;
import static gregtech.api.unification.material.type.Material.MATERIAL_REGISTRY;

public class ChaosReplicatorInfo extends TJMultiblockInfoPage {

    @Override
    public MultiblockControllerBase getController() {
        return TJMetaTileEntities.CHAOS_REPLICATOR;
    }

    @Override
    public MultiblockShapeInfo getMatchingShapes(int extent) {
        return TJMultiblockShapeInfo.builder(FRONT, UP, LEFT)
                .aisle("CCCCCCC", "CCCEMCC", "CQQCQQC", "CQQCQQC", "CQQCQQC", "CQQQQQC", "CQQQQQC", "CCCCCCC", "CCCCCCC")
                .aisle("CCCCCCC", "CDDDDDC", "QF###FQ", "QF###FQ", "QF#A#FQ", "QF###FQ", "QF###FQ", "CDDDDDC", "CCCCCCC")
                .aisle("CCCCCCC", "CDDDDDC", "Q#DDD#Q", "Q#####Q", "Q#####Q", "Q#####Q", "Q#DDD#Q", "CDDDDDC", "CCCCCCC")
                .aisle("CCCCCCC", "CDDDDDC", "C#DDD#C", "C##D##C", "CA#R#AC", "Q##D##Q", "Q#DDD#Q", "CDDDDDC", "CCCCCCC")
                .aisle("CCCCCCC", "CDDDDDC", "Q#DDD#Q", "Q#####Q", "Q#####Q", "Q#####Q", "Q#DDD#Q", "CDDDDDC", "CCCCCCC")
                .aisle("CCCCCCC", "CDDDDDC", "QF###FQ", "QF###FQ", "QF~A~FQ", "QF###FQ", "QF###FQ", "CDDDDDC", "CCCCCCC")
                .aisle("CCCCCCC", "CIiSOCC", "CQQCQQC", "CQQCQQC", "CQQCQQC", "CQQQQQC", "CQQQQQC", "CCCCCCC", "CCCCCCC")
                .where('S', this.getController(), EnumFacing.WEST)
                .where('C', Block.getBlockFromName("contenttweaker:chaoticcasing").getDefaultState())
                .where('F', MetaBlocks.FRAMES.get(GAMaterials.EnrichedNaquadahAlloy).getDefaultState())
                .where('R', MetaBlocks.FRAMES.get(MATERIAL_REGISTRY.getObject("chaos")).getDefaultState())
                .where('D', new BlockInfo(Block.getBlockFromName("draconicevolution:infused_obsidian")))
                .where('Q', new BlockInfo(Block.getBlockFromName("enderio:block_fused_quartz")))
                .where('A', new BlockInfo(Block.getBlockFromName("draconicevolution:draconic_block")))
                .where('M', GATileEntities.MAINTENANCE_HATCH[0], EnumFacing.EAST)
                .where('E', PlaceholderType.ENERGY_INPUT_HATCH, this.getEnergyHatch(0, false), EnumFacing.EAST)
                .where('I', PlaceholderType.INPUT_BUS , MetaTileEntities.ITEM_IMPORT_BUS[0], EnumFacing.WEST)
                .where('O', PlaceholderType.OUTPUT_BUS, MetaTileEntities.ITEM_EXPORT_BUS[0], EnumFacing.WEST)
                .where('i', PlaceholderType.INPUT_HATCH, MetaTileEntities.FLUID_IMPORT_HATCH[0], EnumFacing.WEST)
                .build();
    }

    @Override
    public String[] getDescription() {
        return ArrayUtils.addAll(new String[]{I18n.format("tj.multiblock.temporary")},
                super.getDescription());
    }

    @Override
    public float getDefaultZoom() {
        return 0.5f;
    }
}
