package tj.integration.jei.multi.electric;

import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.GAMultiblockCasing;
import gregicadditions.item.GAMultiblockCasing2;
import gregicadditions.machines.GATileEntities;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.integration.jei.multiblock.MultiblockShapeInfo;
import gregtech.integration.jei.multiblock.channel.PlaceholderType;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumFacing;
import tj.TJConfig;
import tj.integration.jei.TJMultiblockInfoPage;
import tj.integration.jei.TJMultiblockShapeInfo;
import tj.machines.multi.electric.MetaTileEntityLargeWirelessEnergyEmitter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static tj.machines.multi.electric.MetaTileEntityLargeWirelessEnergyEmitter.TransferType.INPUT;

public class LargeWirelessEnergyEmitterInfo extends TJMultiblockInfoPage {

    private final MetaTileEntityLargeWirelessEnergyEmitter.TransferType transferType;
    private final MetaTileEntityLargeWirelessEnergyEmitter tileEntity;

    public LargeWirelessEnergyEmitterInfo(MetaTileEntityLargeWirelessEnergyEmitter.TransferType transferType, MetaTileEntityLargeWirelessEnergyEmitter tileEntity) {
        this.transferType = transferType;
        this.tileEntity = tileEntity;
    }

    @Override
    public MultiblockControllerBase getController() {
        return this.tileEntity;
    }

    @Override
    public MultiblockShapeInfo getMatchingShapes(int extent) {
        return TJMultiblockShapeInfo.builder()
                .aisle("~CCC~", "~CCC~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~")
                .aisle("CCCCC", "CCFCC", "~CFC~", "~CFC~", "~CFC~", "~CFC~", "~CFC~", "~~F~~", "~~F~~", "~~F~~", "~~F~~", "~~F~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~")
                .aisle("iCCCE", "SFIFM", "~FIF~", "~FIF~", "~FIF~", "~FIF~", "~FIF~", "~FIF~", "~FIF~", "~FIF~", "~FIF~", "~FIF~", "~~F~~", "~~F~~", "~~F~~", "~~F~~", "~~F~~")
                .aisle("CCCCC", "CCFCC", "~CFC~", "~CFC~", "~CFC~", "~CFC~", "~CFC~", "~~F~~", "~~F~~", "~~F~~", "~~F~~", "~~F~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~")
                .aisle("~CCC~", "~CCC~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~")
                .where('S', this.getController(), EnumFacing.WEST)
                .where('C', this.tileEntity.getCasingState(transferType))
                .where('F', this.tileEntity.getFrameState(transferType))
                .where('M', GATileEntities.MAINTENANCE_HATCH[0], EnumFacing.EAST)
                .where('E',this.transferType == INPUT ? PlaceholderType.ENERGY_INPUT_HATCH : PlaceholderType.ENERGY_OUTPUT_HATCH ,this.transferType == INPUT ? this.getEnergyHatch(0, false) : this.getEnergyHatch(0, true), EnumFacing.EAST)
                .where('i', PlaceholderType.INPUT_HATCH, MetaTileEntities.FLUID_IMPORT_HATCH[0], EnumFacing.WEST)
                .where('I', PlaceholderType.FRAMEWORK,this.getVoltageCasing(0))
                .build();

    }

    @Override
    protected void generateBlockTooltips() {
        super.generateBlockTooltips();
        Arrays.stream(GAMultiblockCasing.CasingType.values()).forEach(casingType -> this.addBlockTooltip(GAMetaBlocks.MUTLIBLOCK_CASING.getItemVariant(casingType), COMPONENT_BLOCK_TOOLTIP));
        Arrays.stream(GAMultiblockCasing2.CasingType.values()).forEach(casingType -> this.addBlockTooltip(GAMetaBlocks.MUTLIBLOCK_CASING2.getItemVariant(casingType), COMPONENT_BLOCK_TOOLTIP));
    }

    @Override
    public String[] getDescription() {
        return new String[] {
                I18n.format("tj.multiblock.large_wireless_energy_emitter.description").replace("§r", "§7")};
    }

    @Override
    public float getDefaultZoom() {
        return 0.5f;
    }
}
