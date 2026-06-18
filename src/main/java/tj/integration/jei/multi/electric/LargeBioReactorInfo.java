package tj.integration.jei.multi.electric;

import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.GAMultiblockCasing2;
import gregicadditions.item.GATransparentCasing;
import gregicadditions.item.components.EmitterCasing;
import gregicadditions.item.components.FieldGenCasing;
import gregicadditions.item.components.PumpCasing;
import gregicadditions.item.components.SensorCasing;
import gregicadditions.machines.GATileEntities;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.integration.jei.multiblock.MultiblockShapeInfo;
import gregtech.integration.jei.multiblock.channel.PlaceholderType;
import net.minecraft.util.EnumFacing;
import tj.TJConfig;
import tj.integration.jei.TJMultiblockInfoPage;
import tj.integration.jei.TJMultiblockShapeInfo;
import tj.machines.TJMetaTileEntities;

import java.util.ArrayList;
import java.util.List;

import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;

public class LargeBioReactorInfo extends TJMultiblockInfoPage {

    @Override
    public MultiblockControllerBase getController() {
        return TJMetaTileEntities.LARGE_BIO_REACTOR;
    }

    @Override
    public MultiblockShapeInfo getMatchingShapes(int extent) {
        return TJMultiblockShapeInfo.builder(FRONT, UP, LEFT)
                .aisle("~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~c~c~~~~~~", "~~~~~CcEcC~~~~~", "~~~~~~c~c~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~")
                .aisle("~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~GCG~~~~~~", "~~~~~CcCcC~~~~~", "~~~CC#####CC~~~", "~~~~~CcCcC~~~~~", "~~~~~~GCG~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~")
                .aisle("~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~GGCGG~~~~~", "~~~~~GGCGG~~~~~", "~~~~GG###GG~~~~", "~~~CC#c#c#CC~~~", "~~C###c#c###C~~", "~~~CC#c#c#CC~~~", "~~~~GG###GG~~~~", "~~~~~GGCGG~~~~~", "~~~~~GGCGG~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~")
                .aisle("~~~~~~~~~~~~~~~", "~~~~~~GCG~~~~~~", "~~~~GG###GG~~~~", "~~~~G#####G~~~~", "~~~G#######G~~~", "~~C#########C~~", "~C###########C~", "~~C#########C~~", "~~~G#######G~~~", "~~~~G#####G~~~~", "~~~~GG###GG~~~~", "~~~~~~GCG~~~~~~", "~~~~~~~~~~~~~~~")
                .aisle("~~~~~~~~~~~~~~~", "~~~~~GGCGG~~~~~", "~~~GG#####GG~~~", "~~~G#######G~~~", "~~G#########G~~", "~~C#########C~~", "~C###########C~", "~~C#########C~~", "~~G#########G~~", "~~~G#######G~~~", "~~~GG#####GG~~~", "~~~~~GGCGG~~~~~", "~~~~~~~~~~~~~~~")
                .aisle("~~~~~~CCC~~~~~~", "~~~~GG###GG~~~~", "~~GG#######GG~~", "~~G#########G~~", "~~G#########G~~", "~C###########C~", "C#############C", "~C###########C~", "~~G#########G~~", "~~G#########G~~", "~~GG#######GG~~", "~~~~GG###GG~~~~", "~~~~~~CCC~~~~~~")
                .aisle("~~~~~CCCCC~~~~~", "~~~GG#####GG~~~", "~~G#########G~~", "~~G#########G~~", "~G###########G~", "ccc#########ccc", "c#c####e####c#c", "ccc#########ccc", "~G###########G~", "~~G#########G~~", "~~G#########G~~", "~~~GG#####GG~~~", "~~~~~CICOC~~~~~")
                .aisle("~~~~~CCCCC~~~~~", "~~~CC#####CC~~~", "~~C#########C~~", "~~C#########C~~", "~C###########C~", "~C#####P#####C~", "C#####sFs#####C", "~C#####P#####C~", "~C###########C~", "~~C#########C~~", "~~C#########C~~", "~~~CC#####CC~~~", "~~~~~CCMCC~~~~~")
                .aisle("~~~~~CCCCC~~~~~", "~~~GG#####GG~~~", "~~G#########G~~", "~~G#########G~~", "~G###########G~", "ccc#########ccc", "c#c####e####c#c", "ccc#########ccc", "~G###########G~", "~~G#########G~~", "~~G#########G~~", "~~~GG#####GG~~~", "~~~~~CiCoC~~~~~")
                .aisle("~~~~~~CCC~~~~~~", "~~~~GG###GG~~~~", "~~GG#######GG~~", "~~G#########G~~", "~~G#########G~~", "~C###########C~", "C#############C", "~C###########C~", "~~G#########G~~", "~~G#########G~~", "~~GG#######GG~~", "~~~~GG###GG~~~~", "~~~~~~CCC~~~~~~")
                .aisle("~~~~~~~~~~~~~~~", "~~~~~GGCGG~~~~~", "~~~GG#####GG~~~", "~~~G#######G~~~", "~~G#########G~~", "~~C#########C~~", "~C###########C~", "~~C#########C~~", "~~G#########G~~", "~~~G#######G~~~", "~~~GG#####GG~~~", "~~~~~GGCGG~~~~~", "~~~~~~~~~~~~~~~")
                .aisle("~~~~~~~~~~~~~~~", "~~~~~~GCG~~~~~~", "~~~~GG###GG~~~~", "~~~~G#####G~~~~", "~~~G#######G~~~", "~~C#########C~~", "~C###########C~", "~~C#########C~~", "~~~G#######G~~~", "~~~~G#####G~~~~", "~~~~GG###GG~~~~", "~~~~~~GCG~~~~~~", "~~~~~~~~~~~~~~~")
                .aisle("~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~GGCGG~~~~~", "~~~~~GGCGG~~~~~", "~~~~GG###GG~~~~", "~~~CC#c#c#CC~~~", "~~C###c#c###C~~", "~~~CC#c#c#CC~~~", "~~~~GG###GG~~~~", "~~~~~GGCGG~~~~~", "~~~~~GGCGG~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~")
                .aisle("~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~GCG~~~~~~", "~~~~~CcCcC~~~~~", "~~~CC#####CC~~~", "~~~~~CcCcC~~~~~", "~~~~~~GCG~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~")
                .aisle("~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~c~c~~~~~~", "~~~~~CcScC~~~~~", "~~~~~~c~c~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~")
                .where('S', this.getController(), EnumFacing.WEST)
                .where('C', GAMetaBlocks.MUTLIBLOCK_CASING2.getState(GAMultiblockCasing2.CasingType.BIO_REACTOR))
                .where('G', GAMetaBlocks.TRANSPARENT_CASING.getState(GATransparentCasing.CasingType.OSMIRIDIUM_GLASS))
                .where('M', GATileEntities.MAINTENANCE_HATCH[0], EnumFacing.UP)
                .where('E', PlaceholderType.ENERGY_INPUT_HATCH, this.getEnergyHatch(0, false), EnumFacing.EAST)
                .where('I', PlaceholderType.INPUT_BUS , MetaTileEntities.ITEM_IMPORT_BUS[0], EnumFacing.UP)
                .where('O', PlaceholderType.OUTPUT_BUS, MetaTileEntities.ITEM_EXPORT_BUS[0], EnumFacing.UP)
                .where('i', PlaceholderType.INPUT_HATCH, MetaTileEntities.FLUID_IMPORT_HATCH[0], EnumFacing.UP)
                .where('o', PlaceholderType.OUTPUT_HATCH ,MetaTileEntities.FLUID_EXPORT_HATCH[0], EnumFacing.UP)
                .where('P', GAMetaBlocks.PUMP_CASING.getState(PumpCasing.CasingType.values()[0]))
                .where('F', GAMetaBlocks.FIELD_GEN_CASING.getState(FieldGenCasing.CasingType.values()[0]))
                .where('e', GAMetaBlocks.EMITTER_CASING.getState(EmitterCasing.CasingType.values()[0]))
                .where('s', GAMetaBlocks.SENSOR_CASING.getState(SensorCasing.CasingType.values()[0]))
                .where('c', PlaceholderType.COIL)
                .build();
    }

    @Override
    public float getDefaultZoom() {
        return 0.3F;
    }
}
