package tj.integration.jei.multi.electric;

import gregicadditions.GAValues;
import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.components.MotorCasing;
import gregicadditions.machines.GATileEntities;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.integration.jei.multiblock.MultiblockShapeInfo;
import gregtech.integration.jei.multiblock.channel.PlaceholderType;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumFacing;
import tj.TJConfig;
import tj.integration.jei.TJMultiblockInfoPage;
import tj.integration.jei.TJMultiblockShapeInfo;
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
    public MultiblockShapeInfo getMatchingShapes(int extent) {
        return TJMultiblockShapeInfo.builder(FRONT, UP, LEFT)
                .aisle("F~~~F", "F~~~F", "CCCCC", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~")
                .aisle("~~~~~", "~~~~~", "CCCCC", "~CEm~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~")
                .aisle("~~~~~", "~~~~~", "CCMCC", "~iCo~", "~FFF~", "~FFF~", "~FFF~", "~~F~~", "~~F~~", "~~F~~")
                .aisle("~~~~~", "~~~~~", "CCCCC", "~ISO~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~")
                .aisle("F~~~F", "F~~~F", "CCCCC", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~")
                .where('S', this.getController(), EnumFacing.WEST)
                .where('m', GATileEntities.MAINTENANCE_HATCH[0], EnumFacing.EAST)
                .where('C', this.getController().getCasingState())
                .where('F', this.getController().getFrameState())
        
                .where('E', PlaceholderType.ENERGY_INPUT_HATCH, this.getEnergyHatch(0, false), EnumFacing.EAST)
                .where('I', PlaceholderType.INPUT_BUS , MetaTileEntities.ITEM_IMPORT_BUS[0], EnumFacing.WEST)
                .where('O', PlaceholderType.OUTPUT_BUS, MetaTileEntities.ITEM_EXPORT_BUS[0], EnumFacing.WEST)
                .where('i', PlaceholderType.INPUT_HATCH, MetaTileEntities.FLUID_IMPORT_HATCH[0], EnumFacing.NORTH)
                .where('o', PlaceholderType.OUTPUT_HATCH ,MetaTileEntities.FLUID_EXPORT_HATCH[0], EnumFacing.SOUTH)
                .where('M', PlaceholderType.MOTOR, GAMetaBlocks.MOTOR_CASING.getState(MotorCasing.CasingType.values()[0]))
                .build();

    }

    @Override
    public String[] getDescription() {
        return new String[]{I18n.format("tj.multiblock.advanced_large_miner.description").replace("§7", "§r"),
                I18n.format("tj.multiblock.advanced_large_miner.crushed"),
                I18n.format("tj.multiblock.drilling_rig.voltage", GAValues.VN[this.getController().getMinerTier()], GAValues.VN[14]).replace("§7", "§r"),
                I18n.format("gtadditions.machine.miner.multi.description", this.getController().getDiameter(), this.getController().getDiameter(), this.getController().getFortuneLvl()),
                I18n.format("gtadditions.machine.miner.fluid_usage", 1 << this.getController().getDiameter() - 1, this.getController().getDrillingFluid().getLocalizedName()),
                I18n.format("gregtech.multiblock.large_miner.block_per_tick", 1 << this.getController().getDiameter() - 1)};
    }
}
