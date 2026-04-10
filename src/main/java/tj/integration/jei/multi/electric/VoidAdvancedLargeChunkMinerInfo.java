package tj.integration.jei.multi.electric;

import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.components.MotorCasing;
import gregicadditions.machines.GATileEntities;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.integration.jei.multiblock.MultiblockShapeInfo;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumFacing;
import tj.TJConfig;
import tj.blocks.BlockSolidCasings;
import tj.blocks.TJMetaBlocks;
import tj.integration.jei.TJMultiblockInfoPage;
import tj.integration.jei.TJMultiblockShapeInfo;
import tj.machines.TJMetaTileEntities;
import tj.machines.multi.electric.MetaTileEntityVoidLargeAdvancedChunkMiner;

import java.util.ArrayList;
import java.util.List;

import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;
import static gregtech.api.unification.material.type.Material.MATERIAL_REGISTRY;

public class VoidAdvancedLargeChunkMinerInfo extends TJMultiblockInfoPage {

    @Override
    public MetaTileEntityVoidLargeAdvancedChunkMiner getController() {
        return TJMetaTileEntities.VOID_LARGE_ADVANCED_CHUNK_MINER;
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        final List<MultiblockShapeInfo> shapeInfos = new ArrayList<>();
        final TJMultiblockShapeInfo.Builder builder = TJMultiblockShapeInfo.builder(FRONT, UP, LEFT)
                .aisle("F~~~F", "F~~~F", "CCCCC", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~")
                .aisle("~~~~~", "~~~~~", "CCCCC", "~CEm~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~")
                .aisle("~~~~~", "~~~~~", "CCMCC", "~iCo~", "~FFF~", "~FFF~", "~FFF~", "~~F~~", "~~F~~", "~~F~~")
                .aisle("~~~~~", "~~~~~", "CCCCC", "~ISO~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~")
                .aisle("F~~~F", "F~~~F", "CCCCC", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~")
                .where('S', this.getController(), EnumFacing.WEST)
                .where('m', GATileEntities.MAINTENANCE_HATCH[0], EnumFacing.EAST)
                .where('C', TJMetaBlocks.SOLID_CASING.getState(BlockSolidCasings.SolidCasingType.CHAOS_ALLOY))
                .where('F', MetaBlocks.FRAMES.get(MATERIAL_REGISTRY.getObject("chaos")).getDefaultState());
        final int maxTier = TJConfig.machines.disableLayersInJEI ? 4 : 15;
        for (int tier = TJConfig.machines.disableLayersInJEI ? 3 : 0; tier < maxTier; tier++) {
            shapeInfos.add(builder.where('E', this.getEnergyHatch(tier, false), EnumFacing.EAST)
                    .where('I', MetaTileEntities.ITEM_IMPORT_BUS[tier], EnumFacing.WEST)
                    .where('O', MetaTileEntities.ITEM_EXPORT_BUS[tier], EnumFacing.WEST)
                    .where('i', MetaTileEntities.FLUID_IMPORT_HATCH[tier], EnumFacing.NORTH)
                    .where('o', MetaTileEntities.FLUID_EXPORT_HATCH[tier], EnumFacing.SOUTH)
                    .where('M', GAMetaBlocks.MOTOR_CASING.getState(MotorCasing.CasingType.values()[Math.max(0, tier - 1)]))
                    .build());
        }
        return shapeInfos;
    }

    @Override
    public String[] getDescription() {
        return new String[]{I18n.format("tj.multiblock.advanced_large_miner.description").replace("§7", "§r"),
                I18n.format("tj.multiblock.advanced_large_miner.crushed"),
                I18n.format("gtadditions.machine.miner.multi.description", this.getController().getDiameter(), this.getController().getDiameter(), this.getController().getFortuneLvl()),
                I18n.format("gtadditions.machine.miner.fluid_usage", 1 << this.getController().getDiameter() - 1, this.getController().getDrillingFluid().getLocalizedName()),
                I18n.format("gregtech.multiblock.large_miner.block_per_tick", 1 << this.getController().getDiameter() - 1)};
    }
}
