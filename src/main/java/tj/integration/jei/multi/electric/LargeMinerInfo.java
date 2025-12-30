package tj.integration.jei.multi.electric;

import com.google.common.collect.Lists;
import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.components.MotorCasing;
import gregicadditions.machines.GATileEntities;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.integration.jei.multiblock.MultiblockShapeInfo;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import tj.integration.jei.TJMultiblockInfoPage;
import tj.machines.TJMiner;
import tj.machines.multi.electric.MetaTileEntityEliteLargeMiner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LargeMinerInfo extends TJMultiblockInfoPage {

    private final MetaTileEntityEliteLargeMiner largeMiner;

    public LargeMinerInfo(MetaTileEntityEliteLargeMiner largeMiner) {
        this.largeMiner = largeMiner;
    }

    @Override
    public MultiblockControllerBase getController() {
        return this.largeMiner;
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        List<MultiblockShapeInfo> shapeInfos = new ArrayList<>();
        if (this.largeMiner.getType() == TJMiner.Type.DESTROYER) {
            MultiblockShapeInfo.Builder builder = MultiblockShapeInfo.builder()
                    .aisle("F###F", "F###F", "PPPPP", "#####", "#####", "#####", "#####", "#####", "#####", "#####")
                    .aisle("#####", "#####", "PPPPP", "#MPO#", "##F##", "##F##", "##F##", "#####", "#####", "#####")
                    .aisle("#####", "#####", "PPmPP", "#SPE#", "##F##", "##F##", "##F##", "##F##", "##F##", "##F##")
                    .aisle("#####", "#####", "PPPPP", "#IPP#", "##F##", "##F##", "##F##", "#####", "#####", "#####")
                    .aisle("F###F", "F###F", "PPPPP", "#####", "#####", "#####", "#####", "#####", "#####", "#####")
                    .where('S', this.getController(), EnumFacing.WEST)
                    .where('M', GATileEntities.MAINTENANCE_HATCH[0], EnumFacing.WEST)
                    .where('P', this.largeMiner.getCasingState())
                    .where('F', this.largeMiner.getFrameState());
            for (int tier = 0; tier < 15; tier++) {
                shapeInfos.add(builder.where('E', this.getEnergyHatch(tier, false), EnumFacing.EAST)
                        .where('O', MetaTileEntities.ITEM_EXPORT_BUS[Math.min(9, tier)], EnumFacing.EAST)
                        .where('I', MetaTileEntities.FLUID_IMPORT_HATCH[Math.min(9, tier)], EnumFacing.EAST)
                        .where('m', GAMetaBlocks.MOTOR_CASING.getState(MotorCasing.CasingType.values()[Math.max(0, tier - 1)]))
                        .build());
            }
            return shapeInfos;
        }
        MultiblockShapeInfo.Builder builder = MultiblockShapeInfo.builder()
                .aisle("F###F", "F###F", "PPPPP", "#####", "#####", "#####", "#####", "#####", "#####", "#####")
                .aisle("#####", "#####", "PPPPP", "#MPO#", "##F##", "##F##", "##F##", "#####", "#####", "#####")
                .aisle("#####", "#####", "PPPPP", "#SPE#", "##F##", "##F##", "##F##", "##F##", "##F##", "##F##")
                .aisle("#####", "#####", "PPPPP", "#IPP#", "##F##", "##F##", "##F##", "#####", "#####", "#####")
                .aisle("F###F", "F###F", "PPPPP", "#####", "#####", "#####", "#####", "#####", "#####", "#####")
                .where('S', this.getController(), EnumFacing.WEST)
                .where('M', GATileEntities.MAINTENANCE_HATCH[0], EnumFacing.WEST)
                .where('P', this.largeMiner.getCasingState())
                .where('F', this.largeMiner.getFrameState());
        for (int tier = 0; tier < 15; tier++) {
            shapeInfos.add(builder.where('E', this.getEnergyHatch(tier, false), EnumFacing.EAST)
                    .where('O', MetaTileEntities.ITEM_EXPORT_BUS[Math.min(9, tier)], EnumFacing.EAST)
                    .where('I', MetaTileEntities.FLUID_IMPORT_HATCH[Math.min(9, tier)], EnumFacing.WEST)
                    .build());
        }
        return shapeInfos;
    }

    @Override
    protected void generateBlockTooltips() {
        super.generateBlockTooltips();
        if (this.largeMiner.getType() == TJMiner.Type.DESTROYER)
            Arrays.stream(MotorCasing.CasingType.values()).forEach(casingType -> this.addBlockTooltip(GAMetaBlocks.MOTOR_CASING.getItemVariant(casingType), COMPONENT_BLOCK_TOOLTIP));
    }

    @Override
    public String[] getDescription() {
        if (this.largeMiner.getType() == TJMiner.Type.DESTROYER) {
            return new String[]{I18n.format("tj.multiblock.elite_large_miner.description", this.largeMiner.type.chunk, this.largeMiner.type.chunk, this.largeMiner.type.fortuneString),
            I18n.format("tj.multiblock.elite_large_miner.filter.warning")};
        }
        return new String[]{I18n.format("gtadditions.machine.miner.multi.description", this.largeMiner.type.chunk, this.largeMiner.type.chunk, this.largeMiner.type.fortuneString)};
    }

    @Override
    public float getDefaultZoom() {
        return 0.5f;
    }
}
