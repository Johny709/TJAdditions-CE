package tj.integration.jei.multi.electric;

import gregicadditions.machines.GATileEntities;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.integration.jei.multiblock.MultiblockShapeInfo;
import net.minecraft.util.EnumFacing;
import tj.TJConfig;
import tj.blocks.BlockFusionCasings;
import tj.blocks.BlockFusionGlass;
import tj.blocks.BlockSolidCasings;
import tj.blocks.TJMetaBlocks;
import tj.integration.jei.TJMultiblockInfoPage;
import tj.integration.jei.TJMultiblockShapeInfo;
import tj.machines.TJMetaTileEntities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;

public class InterStellarForgeInfo extends TJMultiblockInfoPage {

    @Override
    public MultiblockControllerBase getController() {
        return TJMetaTileEntities.INTERSTELLAR_FORGE;
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        final List<String[]> pattern = new ArrayList<>();
        final List<MultiblockShapeInfo> shapeInfos = new ArrayList<>();
        final TJMultiblockShapeInfo.Builder builder = TJMultiblockShapeInfo.builder(FRONT, RIGHT, DOWN);
        pattern.add(new String[]{"~~~~~~~~CCCCCCCCCCC~~~~~~~~", "~~~~~CCCCCCCCCCCCCCCCC~~~~~", "~~~~CCCCCCCCCCCCCCCCCCC~~~~", "~~~CCCCCCC~CC~CC~CCCCCCC~~~", "~~CCCCC~~~~CC~CC~~~~CCCCC~~", "~CCCCC~~~~~~~~~~~~~~~CCCCC~", "~CCCC~~~~~~~~~~~~~~~~~CCCC~", "~CCC~~~~~~~CC~CC~~~~~~~CCC~", "CCCC~~~~~~~CC~CC~~~~~~~CCCC", "CCCC~~~~~~~~~~~~~~~~~~~CCCC", "CCC~~~~~~~~~~~~~~~~~~~~~CCC", "CCCCC~~CC~~~~~~~~~CC~~CCCCC", "CCCCC~~CC~~~~~~~~~CC~~CCCCC", "CCC~~~~~~~~~~~~~~~~~~~~~CCC", "CCCCC~~CC~~~~~~~~~CC~~CCCCC", "CCCCC~~CC~~~~~~~~~CC~~CCCCC", "CCC~~~~~~~~~~~~~~~~~~~~~CCC", "CCCC~~~~~~~~~~~~~~~~~~~CCCC", "CCCC~~~~~~~CC~CC~~~~~~~CCCC", "~CCC~~~~~~~CC~CC~~~~~~~CCC~", "~CCCC~~~~~~~~~~~~~~~~~CCCC~", "~CCCCC~~~~~~~~~~~~~~~CCCCC~", "~~CCCCC~~~~CC~CC~~~~CCCCC~~", "~~~CCCCCCC~CC~CC~CCCCCCC~~~", "~~~~CCCCCCCCCCCCCCCCCCC~~~~", "~~~~~CCCCCCCCCCCCCCCCC~~~~~", "~~~~~~~~CCCCCCCCCCC~~~~~~~~"});
        pattern.add(new String[]{"~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~VVVVVVVVV~~~~~~~~~", "~~~~~~VVV~~~VGV~~~VVV~~~~~~", "~~~~~V~~~~~CVGVC~~~~~V~~~~~", "~~~~V~~~~~~CVGVC~~~~~~V~~~~", "~~~V~~~~~~~~VGV~~~~~~~~V~~~", "~~V~~~~~~~~~VGV~~~~~~~~~V~~", "~~V~~~~~~~~CVGVC~~~~~~~~V~~", "~~V~~~~~~~~CVGVC~~~~~~~~V~~", "~V~~~~~~~~~~VGV~~~~~~~~~~V~", "~V~~~~~~~~~~VGV~~~~~~~~~~V~", "~V~CC~~CC~~~VGV~~~CC~~CC~V~", "~VVVVVVVVVVVVVVVVVVVVVVVVV~", "~VGGGGGGGGGGVGVGGGGGGGGGGV~", "~VVVVVVVVVVVVVVVVVVVVVVVVV~", "~V~CC~~CC~~~VGV~~~CC~~CC~V~", "~V~~~~~~~~~~VGV~~~~~~~~~~V~", "~V~~~~~~~~~~VGV~~~~~~~~~~V~", "~~V~~~~~~~~CVGVC~~~~~~~~V~~", "~~V~~~~~~~~CVGVC~~~~~~~~V~~", "~~V~~~~~~~~~VGV~~~~~~~~~V~~", "~~~V~~~~~~~~VGV~~~~~~~~V~~~", "~~~~V~~~~~~CVGVC~~~~~~V~~~~", "~~~~~V~~~~~CVGVC~~~~~V~~~~~", "~~~~~~VVV~~~VGV~~~VVV~~~~~~", "~~~~~~~~~VVVVVVVVV~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~"});
        pattern.add(new String[]{"~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~VVVVVVVVV~~~~~~~~~", "~~~~~~VVV~~~GcG~~~VVV~~~~~~", "~~~~~V~~~~~CGcGC~~~~~V~~~~~", "~~~~V~~~~~~CGcGC~~~~~~V~~~~", "~~~V~~~~~~~~GcG~~~~~~~~V~~~", "~~V~~~~~~~~~GcG~~~~~~~~~V~~", "~~V~~~~~~~~CGcGC~~~~~~~~V~~", "~~V~~~~~~~~CGcGC~~~~~~~~V~~", "~V~~~~~~~~~~GcG~~~~~~~~~~V~", "~V~~~~~~~~~~GcG~~~~~~~~~~V~", "~V~CC~~CC~~~GcG~~~CC~~CC~V~", "~VGGGGGGGGGGVcVGGGGGGGGGGV~", "~VcccccccccccccccccccccccV~", "~VGGGGGGGGGGVcVGGGGGGGGGGV~", "~V~CC~~CC~~~GcG~~~CC~~CC~V~", "~V~~~~~~~~~~GcG~~~~~~~~~~V~", "~V~~~~~~~~~~GcG~~~~~~~~~~V~", "~~V~~~~~~~~CGcGC~~~~~~~~V~~", "~~V~~~~~~~~CGcGC~~~~~~~~V~~", "~~V~~~~~~~~~GcG~~~~~~~~~V~~", "~~~V~~~~~~~~GcG~~~~~~~~V~~~", "~~~~V~~~~~~CGcGC~~~~~~V~~~~", "~~~~~V~~~~~CGcGC~~~~~V~~~~~", "~~~~~~VVV~~~GcG~~~VVV~~~~~~", "~~~~~~~~~VVVVVVVVV~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~"});
        pattern.add(new String[]{"~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~VVVVVVVVV~~~~~~~~~", "~~~~~~VVV~~~VGV~~~VVV~~~~~~", "~~~~~V~~~~~CVGVC~~~~~V~~~~~", "~~~~V~~~~~~CVGVC~~~~~~V~~~~", "~~~V~~~~~~~~VGV~~~~~~~~V~~~", "~~V~~~~~~~~~VGV~~~~~~~~~V~~", "~~V~~~~~~~~CVGVC~~~~~~~~V~~", "~~V~~~~~~~~CVGVC~~~~~~~~V~~", "~V~~~~~~~~~~VGV~~~~~~~~~~V~", "~V~~~~~~~~~~VGV~~~~~~~~~~V~", "~V~CC~~CC~~~VGV~~~CC~~CC~V~", "~VVVVVVVVVVVVVVVVVVVVVVVVV~", "~VGGGGGGGGGGVGVGGGGGGGGGGV~", "~VVVVVVVVVVVVVVVVVVVVVVVVV~", "~V~CC~~CC~~~VGV~~~CC~~CC~V~", "~V~~~~~~~~~~VGV~~~~~~~~~~V~", "~V~~~~~~~~~~VGV~~~~~~~~~~V~", "~~V~~~~~~~~CVGVC~~~~~~~~V~~", "~~V~~~~~~~~CVGVC~~~~~~~~V~~", "~~V~~~~~~~~~VGV~~~~~~~~~V~~", "~~~V~~~~~~~~VGV~~~~~~~~V~~~", "~~~~V~~~~~~CVGVC~~~~~~V~~~~", "~~~~~V~~~~~CVGVC~~~~~V~~~~~", "~~~~~~VVV~~~VGV~~~VVV~~~~~~", "~~~~~~~~~VVVVVVVVV~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~"});
        pattern.add(new String[]{"~~~~~~~~CCCCCCCCCCC~~~~~~~~", "~~~~~CCCCCCCCCCCCCCCCC~~~~~", "~~~~CCCCCCCCCCCCCCCCCCC~~~~", "~~~CCCCCCC~CC~CC~CCCCCCC~~~", "~~CCCCC~~~~CC~CC~~~~CCCCC~~", "~CCCCC~~~~~~~~~~~~~~~CCCCC~", "~CCCC~~~~~~~~~~~~~~~~~CCCC~", "~CCC~~~~~~~CC~CC~~~~~~~CCC~", "CCCC~~~~~~~CCCCC~~~~~~~CCCC", "CCCC~~~~~~~~~~~~~~~~~~~CCCC", "CCC~~~~~~~~~~~~~~~~~~~~~CCC", "CCCCC~~CC~~~~~~~~~CC~~CCCCC", "CCCCC~~CC~~~~~~~~~CC~~CCCCC", "CCC~~~~~C~~~~~~~~~C~~~~~CCC", "CCCCC~~CC~~~~~~~~~CC~~CCCCC", "CCCCC~~CC~~~~~~~~~CC~~CCCCC", "CCC~~~~~~~~~~~~~~~~~~~~~CCC", "CCCC~~~~~~~~~~~~~~~~~~~CCCC", "CCCC~~~~~~~CCCCC~~~~~~~CCCC", "~CCC~~~~~~~CC~CC~~~~~~~CCC~", "~CCCC~~~~~~~~~~~~~~~~~CCCC~", "~CCCCC~~~~~~~~~~~~~~~CCCCC~", "~~CCCCC~~~~CC~CC~~~~CCCCC~~", "~~~CCCCCCC~CC~CC~CCCCCCC~~~", "~~~~CCCCCCCCCCCCCCCCCCC~~~~", "~~~~~CCCCCCCCCCCCCCCCC~~~~~", "~~~~~~~~CCCCCCCCCCC~~~~~~~~"});
        pattern.add(new String[]{"~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~C~C~~~~~~~~~~~~", "~~~~~~~~~~~~C~C~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~C~C~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~CC~~~C~~~~~~~~~C~~~CC~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~CC~~~C~~~~~~~~~C~~~CC~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~C~C~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~C~C~~~~~~~~~~~~", "~~~~~~~~~~~~C~C~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~"});
        pattern.add(new String[]{"~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~C~C~~~~~~~~~~~~", "~~~~~~~~~~~~C~C~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~C~C~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~CC~~~C~~~~~~~~~C~~~CC~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~CC~~~C~~~~~~~~~C~~~CC~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~C~C~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~C~C~~~~~~~~~~~~", "~~~~~~~~~~~~C~C~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~"});
        pattern.add(new String[]{"~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~CCC~~~~~~~~~~~~", "~~~~~~~~~~~~C~C~~~~~~~~~~~~", "~~~~~~~~~~~~C~C~~~~~~~~~~~~", "~~~~~~~~~~~~C~C~~~~~~~~~~~~", "~~~~~~~~~~~~C~C~~~~~~~~~~~~", "~~~~~~~~~~~~C~C~~~~~~~~~~~~", "~~~~~~~~~~~C~~~C~~~~~~~~~~~", "~~~~~~~~~~CC~~~CC~~~~~~~~~~", "~~~~CCCCCC~~CCC~~CCCCCC~~~~", "~~~~C~~~~~~~CvC~~~~~~~C~~~~", "~~~~CCCCCC~~CCC~~CCCCCC~~~~", "~~~~~~~~~~CC~~~CC~~~~~~~~~~", "~~~~~~~~~~~C~~~C~~~~~~~~~~~", "~~~~~~~~~~~~C~C~~~~~~~~~~~~", "~~~~~~~~~~~~C~C~~~~~~~~~~~~", "~~~~~~~~~~~~C~C~~~~~~~~~~~~", "~~~~~~~~~~~~C~C~~~~~~~~~~~~", "~~~~~~~~~~~~C~C~~~~~~~~~~~~", "~~~~~~~~~~~~CCC~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~"});
        pattern.add(new String[]{"~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~C~C~~~~~~~~~~~~", "~~~~~~~~~~VVVVVVV~~~~~~~~~~", "~~~~~~~~VVVVVVVVVVV~~~~~~~~", "~~~~~~~VVVVVVVVVVVVV~~~~~~~", "~~~~~~VVVVVVVVVVVVVVV~~~~~~", "~~~~~~VVVVVVVVVVVVVVV~~~~~~", "~~~~~VVVVVVVVVVVVVVVVV~~~~~", "~~~~~VVVVVVVVVVVVVVVVV~~~~~", "~~~~CVVVVVVVVVVVVVVVVVC~~~~", "~~~~~VVVVVVVVVVVVVVVVV~~~~~", "~~~~CVVVVVVVVVVVVVVVVVC~~~~", "~~~~~VVVVVVVVVVVVVVVVV~~~~~", "~~~~~VVVVVVVVVVVVVVVVV~~~~~", "~~~~~~VVVVVVVVVVVVVVV~~~~~~", "~~~~~~VVVVVVVVVVVVVVV~~~~~~", "~~~~~~~VVVVVVVVVVVVV~~~~~~~", "~~~~~~~~VVVVVVVVVVV~~~~~~~~", "~~~~~~~~~~VVVVVVV~~~~~~~~~~", "~~~~~~~~~~~~C~C~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~"});
        pattern.add(new String[]{"~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~C~C~~~~~~~~~~~~", "~~~~~~~~~~~~IsO~~~~~~~~~~~~", "~~~~~~~~~~~~ccc~~~~~~~~~~~~", "~~~~~~~~~~ccC~Ccc~~~~~~~~~~", "~~~~~~~~~c~~C~C~~c~~~~~~~~~", "~~~~~~~~c~~~CCC~~~c~~~~~~~~", "~~~~~~~~c~~TTTTT~~c~~~~~~~~", "~~~~~CCcCCCTTTTTCCCcCC~~~~~", "~~~~~~Cc~~CTTTTTC~~cC~~~~~~", "~~~~~CCcCCCTTTTTCCCcCC~~~~~", "~~~~~~~~c~~TTTTT~~c~~~~~~~~", "~~~~~~~~c~~~CCC~~~c~~~~~~~~", "~~~~~~~~~c~~C~C~~c~~~~~~~~~", "~~~~~~~~~~ccC~Ccc~~~~~~~~~~", "~~~~~~~~~~~~ccc~~~~~~~~~~~~", "~~~~~~~~~~~~iMo~~~~~~~~~~~~", "~~~~~~~~~~~~C~C~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~"});
        pattern.add(new String[]{"~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~C~C~~~~~~~~~~~~", "~~~~~~~~~~~~C~C~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~T~~~T~~~~~~~~~~~", "~~~~~~CC~~~~DGD~~~~CC~~~~~~", "~~~~~~~~~~~~G#G~~~~~~~~~~~~", "~~~~~~CC~~~~DGD~~~~CC~~~~~~", "~~~~~~~~~~~T~~~T~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~C~C~~~~~~~~~~~~", "~~~~~~~~~~~~C~C~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~"});
        pattern.add(new String[]{"~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~VVV~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~VVV~~~~~~~~~~~~", "~~~~~~~~~~~~C~C~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~T~~~T~~~~~~~~~~~", "~V~~~VC~~~~~DGD~~~~~CV~~~V~", "~V~~~V~~~~~~G#G~~~~~~V~~~V~", "~V~~~VC~~~~~DGD~~~~~CV~~~V~", "~~~~~~~~~~~T~~~T~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~C~C~~~~~~~~~~~~", "~~~~~~~~~~~~VVV~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~VVV~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~"});
        pattern.add(new String[]{"~~~~~~~~~~~~~V~~~~~~~~~~~~~", "~~~~~~~~~VVVVGVVVV~~~~~~~~~", "~~~~~~VVV~~~~G~~~~VVV~~~~~~", "~~~~~V~~~~~~~G~~~~~~~V~~~~~", "~~~~V~~~~~~~~G~~~~~~~~V~~~~", "~~~V~~~~~~~VVGVV~~~~~~~V~~~", "~~V~~~~~~~~~C~C~~~~~~~~~V~~", "~~V~~~~~~~~~~~~~~~~~~~~~V~~", "~~V~~~~~~~~~~~~~~~~~~~~~V~~", "~V~~~~~~~~~~~~~~~~~~~~~~~V~", "~V~~~~~~~~~~~~~~~~~~~~~~~V~", "~V~~~V~~~~~T~~~T~~~~~V~~~V~", "~V~~~VC~~~~~DGD~~~~~CV~~CV~", "VGGGGG~~~~~~G#G~~~~~~GGGGGV", "~V~~~VC~~~~~DGD~~~~~CV~~CV~", "~V~~~V~~~~~T~~~T~~~~~V~~~V~", "~V~~~~~~~~~~~~~~~~~~~~~~~V~", "~V~~~~~~~~~~~~~~~~~~~~~~~V~", "~~V~~~~~~~~~~~~~~~~~~~~~V~~", "~~V~~~~~~~~~~~~~~~~~~~~~V~~", "~~V~~~~~~~~~C~C~~~~~~~~~V~~", "~~~V~~~~~~~VVGVV~~~~~~~V~~~", "~~~~V~~~~~~~~G~~~~~~~~V~~~~", "~~~~~V~~~~~~~G~~~~~~~V~~~~~", "~~~~~~VVV~~~~G~~~~VVV~~~~~~", "~~~~~~~~~VVVVGVVVV~~~~~~~~~", "~~~~~~~~~~~~~V~~~~~~~~~~~~~"});
        pattern.forEach(builder::aisle);
        builder.aisle("~~~~~~~~~~~~VVV~~~~~~~~~~~~", "~~~~~~~~~~~VGcGV~~~~~~~~~~~", "~~~~~~~~~~~~GcG~~~~~~~~~~~~", "~~~~~~~~~~~~GcG~~~~~~~~~~~~", "~~~~~~~~~~~~GcG~~~~~~~~~~~~", "~~~~~~~~~~~VGcGV~~~~~~~~~~~", "~~~~~~~~~~~~CcC~~~~~~~~~~~~", "~~~~~~~~~~~~ccc~~~~~~~~~~~~", "~~~~~~~~~~ccC~Ccc~~~~~~~~~~", "~~~~~~~~~c~~C~C~~c~~~~~~~~~", "~~~~~~~~c~~~C~C~~~c~~~~~~~~", "~V~~~V~~c~~TC~CT~~c~~V~~~V~", "VGGGGGCcCCCCDGDCCCCcCGGGGGV", "Vccccccc~~~~G#G~~~~cccccccV", "VGGGGGCcCCCCDGDCCCCcCGGGGGV", "~V~~~V~~c~~TC~CT~~c~~V~~~V~", "~~~~~~~~c~~~C~C~~~c~~~~~~~~", "~~~~~~~~~c~~C~C~~c~~~~~~~~~", "~~~~~~~~~~ccC~Ccc~~~~~~~~~~", "~~~~~~~~~~~~ccc~~~~~~~~~~~~", "~~~~~~~~~~~~CcC~~~~~~~~~~~~", "~~~~~~~~~~~VGcGV~~~~~~~~~~~", "~~~~~~~~~~~~GcG~~~~~~~~~~~~", "~~~~~~~~~~~~GcG~~~~~~~~~~~~", "~~~~~~~~~~~~GcG~~~~~~~~~~~~", "~~~~~~~~~~~VGcGV~~~~~~~~~~~", "~~~~~~~~~~~~VVV~~~~~~~~~~~~");
        for (int i = 0; i < pattern.size(); i++) {
            final String[] aisle = pattern.get(i);
            final String[] aisle2 = new String[aisle.length];
            for (int j = 0; j < aisle.length; j++) {
                aisle2[j] = aisle[j].replace("s", "S").replace("m", "M");
            }
            pattern.set(i, aisle2);
        }
        Collections.reverse(pattern);
        pattern.forEach(builder::aisle);
        builder.where('S', this.getController(), EnumFacing.WEST)
                .where('M', GATileEntities.MAINTENANCE_HATCH[0], EnumFacing.EAST)
                .where('C', TJMetaBlocks.SOLID_CASING.getState(BlockSolidCasings.SolidCasingType.CHAOS_ALLOY))
                .where('s', TJMetaBlocks.SOLID_CASING.getState(BlockSolidCasings.SolidCasingType.CHAOS_ALLOY))
                .where('m', TJMetaBlocks.SOLID_CASING.getState(BlockSolidCasings.SolidCasingType.CHAOS_ALLOY))
                .where('V', TJMetaBlocks.SOLID_CASING.getState(BlockSolidCasings.SolidCasingType.VIBRANIUM))
                .where('G', TJMetaBlocks.FUSION_GLASS.getState(BlockFusionGlass.GlassType.FUSION_GLASS_UEV))
                .where('c', TJMetaBlocks.FUSION_CASING.getState(BlockFusionCasings.FusionType.FUSION_COIL_UEV));
        final int maxTier = TJConfig.machines.disableLayersInJEI ? 4 : 15;
        for (int tier = TJConfig.machines.disableLayersInJEI ? 3 : 0; tier < maxTier; tier++) {
            shapeInfos.add(builder.where('E', this.getEnergyHatch(tier, false), EnumFacing.WEST)
                    .where('I', MetaTileEntities.ITEM_IMPORT_BUS[tier], EnumFacing.WEST)
                    .where('i', MetaTileEntities.FLUID_IMPORT_HATCH[tier], EnumFacing.WEST)
                    .where('O', MetaTileEntities.ITEM_EXPORT_BUS[tier], EnumFacing.WEST)
                    .where('o', MetaTileEntities.FLUID_EXPORT_HATCH[tier], EnumFacing.WEST)
                    .where('v', this.getVoltageCasing(tier))
                    .build());
        }
        return shapeInfos;
    }

    @Override
    public float getDefaultZoom() {
        return 0.2F;
    }
}
