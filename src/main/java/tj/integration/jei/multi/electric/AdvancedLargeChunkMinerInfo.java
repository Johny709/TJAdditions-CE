package tj.integration.jei.multi.electric;

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

public class AdvancedLargeChunkMinerInfo extends TJMultiblockInfoPage {

    private final int type;

    public AdvancedLargeChunkMinerInfo(int type) {
        this.type = type;
    }

    @Override
    public MetaTileEntityAdvancedLargeChunkMiner getController() {
        return TJMetaTileEntities.ADVANCED_LARGE_MINERS[this.type];
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        List<MultiblockShapeInfo> shapeInfos = new ArrayList<>();
        GAMultiblockShapeInfo.Builder builder = GAMultiblockShapeInfo.builder()
                .aisle("F~~~F", "F~~~F", "CCCCC", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~")
                .aisle("~~~~~", "~~~~~", "CCCCC", "~CEM~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~")
                .aisle("~~~~~", "~~~~~", "CCCCC", "~iCo~", "~FFF~", "~FFF~", "~FFF~", "~~F~~", "~~F~~", "~~F~~")
                .aisle("~~~~~", "~~~~~", "CCCCC", "~ISO~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~")
                .aisle("F~~~F", "F~~~F", "CCCCC", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~")
                .where('S', this.getController(), EnumFacing.SOUTH)
                .where('M', GATileEntities.MAINTENANCE_HATCH[0], EnumFacing.NORTH)
                .where('C', this.getController().getCasingState())
                .where('F', this.getController().getFrameState());
        for (int tier = 0; tier < 15; tier++) {
            shapeInfos.add(builder.where('E', this.getEnergyHatch(tier, false), EnumFacing.NORTH)
                    .where('I', MetaTileEntities.ITEM_IMPORT_BUS[tier], EnumFacing.SOUTH)
                    .where('O', MetaTileEntities.ITEM_EXPORT_BUS[tier], EnumFacing.SOUTH)
                    .where('i', MetaTileEntities.FLUID_IMPORT_HATCH[tier], EnumFacing.WEST)
                    .where('o', MetaTileEntities.FLUID_EXPORT_HATCH[tier], EnumFacing.EAST)
                    .build());
        }
        return shapeInfos;
    }

    @Override
    public String[] getDescription() {
        return new String[]{I18n.format("tj.multiblock.advanced_large_miner.description").replace("§7", "§r"),
                I18n.format("gtadditions.machine.miner.multi.description", this.getController().getTier(), this.getController().getTier(), this.getController().getFortuneLvl()),
                I18n.format("gtadditions.machine.miner.fluid_usage", 1 << this.getController().getTier() - 1, this.getController().getDrillingFluid().getLocalizedName())};
    }
}
