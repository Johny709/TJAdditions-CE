package tj.integration.jei.multi.electric;

import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.components.MotorCasing;
import gregicadditions.jei.GAMultiblockShapeInfo;
import gregicadditions.machines.GATileEntities;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.integration.jei.multiblock.MultiblockShapeInfo;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumFacing;
import tj.integration.jei.TJMultiblockInfoPage;
import tj.machines.TJMetaTileEntities;
import tj.machines.multi.electric.MetaTileEntityAdvancedLargeChunkMiner;

import java.util.ArrayList;
import java.util.List;

import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;

public class AdvancedLargeChunkMinerInfo extends TJMultiblockInfoPage {

    private final int type;

    public AdvancedLargeChunkMinerInfo(int type) {
        this.type = type;
    }

    @Override
    public MetaTileEntityAdvancedLargeChunkMiner getController() {
        return TJMetaTileEntities.ADVANCED_LARGE_CHUNK_MINERS[this.type];
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        List<MultiblockShapeInfo> shapeInfos = new ArrayList<>();
        GAMultiblockShapeInfo.Builder builder = GAMultiblockShapeInfo.builder(FRONT, UP, LEFT)
                .aisle("F~~~F", "F~~~F", "CCCCC", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~")
                .aisle("~~~~~", "~~~~~", "CCCCC", "~CEm~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~")
                .aisle("~~~~~", "~~~~~", "CCMCC", "~iCo~", "~FFF~", "~FFF~", "~FFF~", "~~F~~", "~~F~~", "~~F~~")
                .aisle("~~~~~", "~~~~~", "CCCCC", "~ISO~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~")
                .aisle("F~~~F", "F~~~F", "CCCCC", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~")
                .where('S', this.getController(), EnumFacing.WEST)
                .where('m', GATileEntities.MAINTENANCE_HATCH[0], EnumFacing.EAST)
                .where('C', this.getController().getCasingState())
                .where('F', this.getController().getFrameState());
        for (int tier = 0; tier < 15; tier++) {
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
