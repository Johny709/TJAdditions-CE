package tj.integration.jei.multi.electric;

import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.GATransparentCasing;
import gregicadditions.machines.GATileEntities;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.unification.material.Materials;
import gregtech.common.blocks.BlockBoilerCasing;
import gregtech.common.blocks.BlockFireboxCasing;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.integration.jei.multiblock.MultiblockShapeInfo;
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

public class TJMegaBlastFurnaceInfo extends TJMultiblockInfoPage {

    @Override
    public MultiblockControllerBase getController() {
        return TJMetaTileEntities.MEGA_BLAST_FURNACE;
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        final List<MultiblockShapeInfo> shapeInfos = new ArrayList<>();
        final TJMultiblockShapeInfo.Builder builder = TJMultiblockShapeInfo.builder(FRONT, UP, LEFT)
                .aisle("~~~~~CCECC~~~~~", "~~~~~CCGCC~~~~~", "~~~~~CCCCC~~~~~", "~~~~~CCPCC~~~~~", "~~~~~~~P~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~")
                .aisle("~~~CCCBBBCCC~~~", "~~~CGCCFCCGC~~~", "~~~CCCCPCCCC~~~", "~~~CPCCPCCPC~~~", "~~~~PPPPPPP~~~~", "~LLLLLLLLLLLLL~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~")
                .aisle("~~CCBBCBCBBCC~~", "~~GCF#C#C#FCG~~", "~~CCP#C#C#PCC~~", "~~PCPCCCCCPCP~~", "~~PPP#####PPP~~", "~LLLLLLLLLLLLL~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~")
                .aisle("~CCCBBCBCBBCCC~", "~CCC##C#C##CCC~", "~CCC##C#C##CCC~", "~CCCCCCCCCCCCC~", "~~P#########P~~", "~LL#########LL~", "~~~~~~RRR~~~~~~", "~~~~~~~R~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~RRR~~~~~~")
                .aisle("~CBBCBBCBBCBBC~", "~CF#C##C##C#FC~", "~CP#C##C##C#PC~", "~PPCCCCCCCCCPP~", "~PP####R####PP~", "~LL####R####LL~", "~~~~RRRRRRR~~~~", "~~~~~~~U~~~~~~~", "~~~~~~~U~~~~~~~", "~~~~~~~U~~~~~~~", "~~~~~~~U~~~~~~~", "~~~~~~~U~~~~~~~", "~~~~~~~U~~~~~~~", "~~~~~~~U~~~~~~~", "~~~~RRRRRRR~~~~")
                .aisle("CCBBBCBCBCBBBCC", "CC###C#C#C###CC", "CC###C#C#C###CC", "CCCCCCCCCCCCCCC", "~P###R###R###P~", "~LL##R###R##LL~", "~~~~RRRRRRR~~~~", "~~~~~ccccc~~~~~", "~~~~~ccccc~~~~~", "~~~~~ccccc~~~~~", "~~~~~ccccc~~~~~", "~~~~~ccccc~~~~~", "~~~~~ccccc~~~~~", "~~~~~ccccc~~~~~", "~~~~RRRRRRR~~~~")
                .aisle("CBCCBBCCCBBCCBC", "C#CC##CCC##CC#C", "C#CC##CCC##CC#C", "CCCCCCCCCCCCCCC", "~P###########P~", "~LL#########CC~", "~~~RRRRRRRRR~~~", "~~~~~c~~~c~~~~~", "~~~~~c~~~c~~~~~", "~~~~~c~~~c~~~~~", "~~~~~c~~~c~~~~~", "~~~~~c~~~c~~~~~", "~~~~~c~~~c~~~~~", "~~~~~c~~~c~~~~~", "~~~RRRRRRRRR~~~")
                .aisle("CBBBCCCfCCCBBBC", "GF##CCCfCCC##FG", "CP##CCCfCCC##PC", "PPCCCCCfCCCCCPP", "PP##R##f##R##PP", "~LL#R##f##R#CP~", "~~~RRRRRRRRR~P~", "~~~RUc~~~cPPPP~", "~~~~Uc~~~cP~~~~", "~~~~Uc~~~cP~~~~", "~~~~Uc~~~cP~~~~", "~~~~Uc~~~cP~~~~", "~~~~Uc~~~cP~~~~", "~~~~Uc~~~cP~~~~", "~~~RRRRmPPPR~~~")
                .aisle("CBCCBBCCCBBCCBC", "C#CC##CCC##CC#C", "C#CC##CCC##CC#C", "CCCCCCCCCCCCCCC", "~P###########P~", "~LL#########CC~", "~~~RRRRRRRRR~~~", "~~~~~c~~~c~~~~~", "~~~~~c~~~c~~~~~", "~~~~~c~~~c~~~~~", "~~~~~c~~~c~~~~~", "~~~~~c~~~c~~~~~", "~~~~~c~~~c~~~~~", "~~~~~c~~~c~~~~~", "~~~RRRRRRRRR~~~")
                .aisle("CCBBBCBCBCBBBCC", "CC###C#C#C###CC", "CC###C#C#C###CC", "CCCCCCCCCCCCCCC", "~P###R###R###P~", "~LL##R###R##LL~", "~~~~RRRRRRR~~~~", "~~~~~ccccc~~~~~", "~~~~~ccccc~~~~~", "~~~~~ccccc~~~~~", "~~~~~ccccc~~~~~", "~~~~~ccccc~~~~~", "~~~~~ccccc~~~~~", "~~~~~ccccc~~~~~", "~~~~RRRRRRR~~~~")
                .aisle("~CBBCBBCBBCBBC~", "~CF#C##C##C#FC~", "~CP#C##C##C#PC~", "~PPCCCCCCCCCPP~", "~PP####R####PP~", "~LL####R####LL~", "~~~~RRRRRRR~~~~", "~~~~~~~U~~~~~~~", "~~~~~~~U~~~~~~~", "~~~~~~~U~~~~~~~", "~~~~~~~U~~~~~~~", "~~~~~~~U~~~~~~~", "~~~~~~~U~~~~~~~", "~~~~~~~U~~~~~~~", "~~~~RRRRRRR~~~~")
                .aisle("~CCCBBCPCBBCCC~", "~CCC##CPC##CCC~", "~CCC##CPC##CCC~", "~CCCCCCPCCCCCC~", "~~P###PP####P~~", "~LL#########LL~", "~~~~~~RRR~~~~~~", "~~~~~~~R~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~RRR~~~~~~")
                .aisle("~~CCBBCGCBBCC~~", "~~GCF#CGC#FCG~~", "~~CCP#CGC#PCC~~", "~~PCPCCGCCPCP~~", "~~PPPPPP#PPPP~~", "~LLLLLLLLLLLLL~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~")
                .aisle("~L~CCCCGCCCC~~~", "~L~CGCCGCCGC~~~", "~L~CCCCGCCCC~~~", "~L~CPCCGCCPC~~~", "~L~~PP~~~PP~~~~", "~LLLLLLLLLLLLL~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~")
                .aisle("~~~~~ICSOC~~~~~", "~~~~~iMGoC~~~~~", "~~~~~CCGCC~~~~~", "~~~~~CCGCC~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~")
                .where('S', this.getController(), EnumFacing.WEST)
                .where('m', GATileEntities.MUFFLER_HATCH[0], EnumFacing.UP)
                .where('M', GATileEntities.MAINTENANCE_HATCH[0], EnumFacing.WEST)
                .where('C', MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.INVAR_HEATPROOF))
                .where('B', MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.PRIMITIVE_BRICKS))
                .where('G', GAMetaBlocks.TRANSPARENT_CASING.getState(GATransparentCasing.CasingType.OSMIRIDIUM_GLASS))
                .where('F', MetaBlocks.BOILER_FIREBOX_CASING.getState(BlockFireboxCasing.FireboxCasingType.TUNGSTENSTEEL_FIREBOX))
                .where('P', MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.TUNGSTENSTEEL_PIPE))
                .where('R', MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.TUNGSTENSTEEL_ROBUST))
                .where('L', MetaBlocks.FRAMES.get(Materials.BlackSteel).getDefaultState())
                .where('U', MetaBlocks.FRAMES.get(Materials.BlueSteel).getDefaultState());
        final int maxTier = TJConfig.machines.disableLayersInJEI ? 4 : 15;
        for (int tier = TJConfig.machines.disableLayersInJEI ? 3 : 0; tier < maxTier; tier++) {
            shapeInfos.add(builder.where('E', this.getEnergyHatch(tier, false), EnumFacing.EAST)
                    .where('I', MetaTileEntities.ITEM_IMPORT_BUS[tier], EnumFacing.WEST)
                    .where('O', MetaTileEntities.ITEM_EXPORT_BUS[tier], EnumFacing.WEST)
                    .where('i', MetaTileEntities.FLUID_IMPORT_HATCH[tier], EnumFacing.WEST)
                    .where('o', MetaTileEntities.FLUID_EXPORT_HATCH[tier], EnumFacing.WEST)
                    .where('f', this.getVoltageCasing(tier))
                    .where('c', this.getCoils(tier))
                    .build());
        }
        return shapeInfos;
    }

    @Override
    public String[] getDescription() {
        return ArrayUtils.addAll(new String[]{I18n.format("tj.multiblock.temporary"),
                        I18n.format("tj.multiblock.mega").replace("§7", "§r")},
                super.getDescription());
    }

    @Override
    public float getDefaultZoom() {
        return 0.3F;
    }
}
