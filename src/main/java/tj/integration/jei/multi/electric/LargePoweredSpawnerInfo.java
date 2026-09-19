package tj.integration.jei.multi.electric;

import gregicadditions.GAMaterials;
import gregicadditions.machines.GATileEntities;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.util.BlockInfo;
import gregtech.common.blocks.BlockBoilerCasing;
import gregtech.common.blocks.BlockMultiblockCasing;
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

public class LargePoweredSpawnerInfo extends TJMultiblockInfoPage {

    @Override
    public MultiblockControllerBase getController() {
        return TJMetaTileEntities.LARGE_POWERED_SPAWNER;
    }

    @Override
    public MultiblockShapeInfo getMatchingShapes(int extent) {
         return TJMultiblockShapeInfo.builder(FRONT, UP, LEFT)
                .aisle("CCECC", "F~~~F", "F~~~F", "F~~~F", "CCCCC")
                .aisle("CCCCC", "~TmT~", "~TmT~", "~TmT~", "CGGGC")
                .aisle("CCCCC", "~mem~", "~mem~", "~mem~", "CGGGC")
                .aisle("CCCCC", "~TmT~", "~TmT~", "~TmT~", "CGGGC")
                .aisle("IiSOC", "F~~~F", "F~~~F", "F~~~F", "CCMCC")
                .where('S', this.getController(), EnumFacing.WEST)
                .where('C', Block.getBlockFromName("contenttweaker:soulcasing").getDefaultState())
                .where('F', MetaBlocks.FRAMES.get(GAMaterials.Protactinium.getMaterial()).getDefaultState())
                .where('T', MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.TUNGSTENSTEEL_PIPE))
                .where('m', new BlockInfo(Block.getBlockFromName("enderio:block_decoration1")))
                .where('e', Block.getBlockFromName("enderio:block_alloy").getStateFromMeta(8))
                .where('G', MetaBlocks.MUTLIBLOCK_CASING.getState(BlockMultiblockCasing.MultiblockCasingType.GRATE_CASING))
                .where('M', GATileEntities.MAINTENANCE_HATCH[0], EnumFacing.WEST)
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
