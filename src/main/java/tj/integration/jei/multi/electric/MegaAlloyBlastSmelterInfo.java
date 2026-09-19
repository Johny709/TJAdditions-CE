package tj.integration.jei.multi.electric;

import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.GATransparentCasing;
import gregicadditions.item.metal.MetalCasing1;
import gregicadditions.machines.GATileEntities;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.integration.jei.multiblock.MultiblockShapeInfo;
import gregtech.integration.jei.multiblock.channel.PlaceholderType;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumFacing;
import org.apache.commons.lang3.ArrayUtils;
import tj.TJConfig;
import tj.blocks.BlockActiveAbility;
import tj.blocks.TJMetaBlocks;
import tj.integration.jei.TJMultiblockInfoPage;
import tj.integration.jei.TJMultiblockShapeInfo;
import tj.machines.TJMetaTileEntities;

import java.util.ArrayList;
import java.util.List;

import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;

public class MegaAlloyBlastSmelterInfo extends TJMultiblockInfoPage {

    @Override
    public MultiblockControllerBase getController() {
        return TJMetaTileEntities.MEGA_ALLOY_BLAST_SMELTER;
    }

    @Override
    public MultiblockShapeInfo getMatchingShapes(int extent) {
        return TJMultiblockShapeInfo.builder(FRONT, UP, LEFT)
                .aisle("~~~CCECC~~~", "~~~HHHHH~~~", "~~~GGGGG~~~", "~~~GGGGG~~~", "~~~GGGGG~~~", "~~~HHHHH~~~", "~~~CCCCC~~~", "~~~~~~~~~~~", "~~~~~~~~~~~", "~~~~~~~~~~~", "~~~~~~~~~~~", "~~~~~~~~~~~", "~~~~~~~~~~~", "~~~~~~~~~~~", "~~~~~~~~~~~", "~~~~~~~~~~~", "~~~~~~~~~~~", "~~~~~~~~~~~", "~~~~~~~~~~~", "~~~~~~~~~~~")
                .aisle("~~CCCCCCC~~", "~~H#####H~~", "~~G#####G~~", "~~G#####G~~", "~~G#####G~~", "~~H#####H~~", "~~C#####C~~", "~~~CCCCC~~~", "~~~GGGGG~~~", "~~~GGGGG~~~", "~~~GGGGG~~~", "~~~GGGGG~~~", "~~~GGGGG~~~", "~~~GGGGG~~~", "~~~GGGGG~~~", "~~~GGGGG~~~", "~~~GGGGG~~~", "~~~GGGGG~~~", "~~~GGGGG~~~", "~~~CCCCC~~~")
                .aisle("~CCCCCCCCC~", "~H#ccccc#H~", "~G#ccccc#G~", "~G#ccccc#G~", "~G#ccccc#G~", "~H#ccccc#H~", "~C#ccccc#C~", "~~CcccccC~~", "~~GcccccG~~", "~~GcccccG~~", "~~GcccccG~~", "~~GcccccG~~", "~~GcccccG~~", "~~GcccccG~~", "~~GcccccG~~", "~~GcccccG~~", "~~GcccccG~~", "~~GcccccG~~", "~~GcccccG~~", "~~CCCCCCC~~")
                .aisle("CCCCCCCCCCC", "H#c#####c#H", "G#c#####c#G", "G#c#####c#G", "G#c#####c#G", "H#c#####c#H", "C#c#####c#C", "~Cc#####cC~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~CCCCCCCCC~")
                .aisle("CCCCCFCCCCC", "H#c#####c#H", "G#c#####c#G", "G#c#####c#G", "G#c#####c#G", "H#c#####c#H", "C#c#####c#C", "~Cc#####cC~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~CCCCCCCCC~")
                .aisle("CCCCFFFCCCC", "H#c#####c#H", "G#c#####c#G", "G#c#####c#G", "G#c#####c#G", "H#c#####c#H", "C#c#####c#C", "~Cc#####cC~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~CCCCMCCCC~")
                .aisle("CCCCCFCCCCC", "H#c#####c#H", "G#c#####c#G", "G#c#####c#G", "G#c#####c#G", "H#c#####c#H", "C#c#####c#C", "~Cc#####cC~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~CCCCCCCCC~")
                .aisle("CCCCCCCCCCC", "H#c#####c#H", "G#c#####c#G", "G#c#####c#G", "G#c#####c#G", "H#c#####c#H", "C#c#####c#C", "~Cc#####cC~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~CCCCCCCCC~")
                .aisle("~CCCCCCCCC~", "~H#ccccc#H~", "~G#ccccc#G~", "~G#ccccc#G~", "~G#ccccc#G~", "~H#ccccc#H~", "~C#ccccc#C~", "~~CcccccC~~", "~~GcccccG~~", "~~GcccccG~~", "~~GcccccG~~", "~~GcccccG~~", "~~GcccccG~~", "~~GcccccG~~", "~~GcccccG~~", "~~GcccccG~~", "~~GcccccG~~", "~~GcccccG~~", "~~GcccccG~~", "~~CCCCCCC~~")
                .aisle("~~CCCCCCC~~", "~~H#####H~~", "~~G#####G~~", "~~G#####G~~", "~~G#####G~~", "~~H#####H~~", "~~C#####C~~", "~~~CCCCC~~~", "~~~GGGGG~~~", "~~~GGGGG~~~", "~~~GGGGG~~~", "~~~GGGGG~~~", "~~~GGGGG~~~", "~~~GGGGG~~~", "~~~GGGGG~~~", "~~~GGGGG~~~", "~~~GGGGG~~~", "~~~GGGGG~~~", "~~~GGGGG~~~", "~~~CCCCC~~~")
                .aisle("~~~CCCCC~~~", "~~~HHHHH~~~", "~~~GCOCG~~~", "~~~GISiG~~~", "~~~GCmCG~~~", "~~~HHHHH~~~", "~~~CCCCC~~~", "~~~~~~~~~~~", "~~~~~~~~~~~", "~~~~~~~~~~~", "~~~~~~~~~~~", "~~~~~~~~~~~", "~~~~~~~~~~~", "~~~~~~~~~~~", "~~~~~~~~~~~", "~~~~~~~~~~~", "~~~~~~~~~~~", "~~~~~~~~~~~", "~~~~~~~~~~~", "~~~~~~~~~~~")
                .where('S', this.getController(), EnumFacing.WEST)
                .where('m', GATileEntities.MAINTENANCE_HATCH[0], EnumFacing.WEST)
                .where('M', PlaceholderType.MUFFLER, GATileEntities.MUFFLER_HATCH[0], EnumFacing.UP)
                .where('C', GAMetaBlocks.METAL_CASING_1.getState(MetalCasing1.CasingType.ZIRCONIUM_CARBIDE))
                .where('H', TJMetaBlocks.ACTIVE_ABILITY_BLOCKS.getState(BlockActiveAbility.AbilityType.HEAT_VENT).withProperty(BlockActiveAbility.ACTIVE, false))
                .where('G', GAMetaBlocks.TRANSPARENT_CASING.getState(GATransparentCasing.CasingType.OSMIRIDIUM_GLASS))
                .where('E', PlaceholderType.ENERGY_INPUT_HATCH,this.getEnergyHatch(0, false), EnumFacing.EAST)
                .where('I', PlaceholderType.INPUT_BUS,MetaTileEntities.ITEM_IMPORT_BUS[0], EnumFacing.WEST)
                .where('i', PlaceholderType.INPUT_HATCH,MetaTileEntities.FLUID_IMPORT_HATCH[0], EnumFacing.WEST)
                .where('O', PlaceholderType.OUTPUT_HATCH,MetaTileEntities.FLUID_EXPORT_HATCH[0], EnumFacing.WEST)
                .where('F', PlaceholderType.FRAMEWORK)
                .where('c', PlaceholderType.COIL)
                .build();
    }

    @Override
    public String[] getDescription() {
        return ArrayUtils.addAll(new String[]{I18n.format("tj.multiblock.mega").replace("§7", "§r")}, super.getDescription());
    }

    @Override
    public float getDefaultZoom() {
        return 0.3F;
    }
}
