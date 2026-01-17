package tj.integration.jei.multi.electric;

import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.fusion.GAFusionCasing;
import gregicadditions.machines.GATileEntities;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.common.blocks.BlockMultiblockCasing;
import gregtech.common.blocks.BlockWireCoil;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.integration.jei.multiblock.MultiblockShapeInfo;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumFacing;
import tj.blocks.BlockSolidCasings;
import tj.blocks.TJMetaBlocks;
import tj.integration.jei.TJMultiblockInfoPage;
import tj.machines.TJMetaTileEntities;

import java.util.ArrayList;
import java.util.List;


public class ArmorInfuserInfo extends TJMultiblockInfoPage {

    @Override
    public MultiblockControllerBase getController() {
        return TJMetaTileEntities.ARMOR_INFUSER;
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        List<MultiblockShapeInfo> shapeInfos = new ArrayList<>();
        MultiblockShapeInfo.Builder builder = MultiblockShapeInfo.builder()
                .aisle("DDEDD", "~~~~~", "~~~~~", "GGGGG", "~~~~~", "~~~~~", "GGGGG", "~~~~~", "~~~~~", "DDDDD")
                .aisle("ODDDD", "~~A~~", "~~A~~", "G~A~G", "~~A~~", "~~A~~", "G~A~G", "~~A~~", "~~A~~", "DDDDD")
                .aisle("SDDDD", "~AFA~", "~AFA~", "GAFAG", "~AFA~", "~AFA~", "GAFAG", "~AFA~", "~AFA~", "DDDDD")
                .aisle("IDDDD", "~~A~~", "~~A~~", "G~A~G", "~~A~~", "~~A~~", "G~A~G", "~~A~~", "~~A~~", "DDDDD")
                .aisle("MDKJD", "~~~~~", "~~~~~", "GGGGG", "~~~~~", "~~~~~", "GGGGG", "~~~~~", "~~~~~", "DDDDD")
                .where('S', this.getController(), EnumFacing.WEST)
                .where('D', TJMetaBlocks.SOLID_CASING.getState(BlockSolidCasings.SolidCasingType.DRACONIC_CASING))
                .where('F', MetaBlocks.WIRE_COIL.getState(BlockWireCoil.CoilType.FUSION_COIL))
                .where('A', MetaBlocks.MUTLIBLOCK_CASING.getState(BlockMultiblockCasing.MultiblockCasingType.FUSION_CASING_MK2))
                .where('G', GAMetaBlocks.FUSION_CASING.getState(GAFusionCasing.CasingType.ADV_FUSION_COIL_1))
                .where('M', GATileEntities.MAINTENANCE_HATCH[0], EnumFacing.WEST);
        for (int tier = 0; tier < 15; tier++) {
            shapeInfos.add(builder.where('E', this.getEnergyHatch(tier, false), EnumFacing.NORTH)
                    .where('I', MetaTileEntities.ITEM_IMPORT_BUS[Math.min(9, tier)], EnumFacing.WEST)
                    .where('O', MetaTileEntities.ITEM_EXPORT_BUS[Math.min(9, tier)], EnumFacing.WEST)
                    .where('K', MetaTileEntities.FLUID_IMPORT_HATCH[Math.min(9, tier)], EnumFacing.SOUTH)
                    .where('J', MetaTileEntities.FLUID_EXPORT_HATCH[Math.min(9, tier)], EnumFacing.SOUTH)
                    .build());
        }
        return shapeInfos;
    }

    @Override
    public String[] getDescription() {
        return new String[] {
                I18n.format("tj.multiblock.armor_infuser.description")};
    }

    @Override
    public float getDefaultZoom() {
        return 0.5f;
    }
}
