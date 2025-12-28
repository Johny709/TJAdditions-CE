package tj.integration.jei.multi.electric;

import com.google.common.collect.Lists;
import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.fusion.GAFusionCasing;
import gregicadditions.machines.GATileEntities;
import gregtech.api.GTValues;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.common.blocks.BlockMultiblockCasing;
import gregtech.common.blocks.BlockWireCoil;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.integration.jei.multiblock.MultiblockInfoPage;
import gregtech.integration.jei.multiblock.MultiblockShapeInfo;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumFacing;
import tj.blocks.BlockSolidCasings;
import tj.blocks.TJMetaBlocks;
import tj.machines.TJMetaTileEntities;

import java.util.List;


public class ArmorInfuserInfo extends MultiblockInfoPage {
    public ArmorInfuserInfo() {
    }
    @Override
    public MultiblockControllerBase getController() {
        return TJMetaTileEntities.ARMOR_INFUSER;
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        MultiblockShapeInfo shapeInfo = MultiblockShapeInfo.builder()
                .aisle("DDEDD", "~~~~~", "~~~~~", "GGGGG", "~~~~~", "~~~~~", "GGGGG", "~~~~~", "~~~~~", "DDDDD")
                .aisle("ODDDD", "~~A~~", "~~A~~", "G~A~G", "~~A~~", "~~A~~", "G~A~G", "~~A~~", "~~A~~", "DDDDD")
                .aisle("SDDDD", "~AFA~", "~AFA~", "GAFAG", "~AFA~", "~AFA~", "GAFAG", "~AFA~", "~AFA~", "DDDDD")
                .aisle("IDDDD", "~~A~~", "~~A~~", "G~A~G", "~~A~~", "~~A~~", "G~A~G", "~~A~~", "~~A~~", "DDDDD")
                .aisle("MDKJD", "~~~~~", "~~~~~", "GGGGG", "~~~~~", "~~~~~", "GGGGG", "~~~~~", "~~~~~", "DDDDD")
                .where('S', TJMetaTileEntities.ARMOR_INFUSER, EnumFacing.WEST)
                .where('D', TJMetaBlocks.SOLID_CASING.getState(BlockSolidCasings.SolidCasingType.DRACONIC_CASING))
                .where('F', MetaBlocks.WIRE_COIL.getState(BlockWireCoil.CoilType.FUSION_COIL))
                .where('A', MetaBlocks.MUTLIBLOCK_CASING.getState(BlockMultiblockCasing.MultiblockCasingType.FUSION_CASING_MK2))
                .where('G', GAMetaBlocks.FUSION_CASING.getState(GAFusionCasing.CasingType.ADV_FUSION_COIL_1))
                .where('O', MetaTileEntities.ITEM_EXPORT_BUS[GTValues.MV], EnumFacing.WEST)
                .where('I', MetaTileEntities.ITEM_IMPORT_BUS[GTValues.MV], EnumFacing.WEST)
                .where('M', GATileEntities.MAINTENANCE_HATCH[0], EnumFacing.WEST)
                .where('E', MetaTileEntities.ENERGY_INPUT_HATCH[GTValues.UV], EnumFacing.NORTH)
                .where('K', MetaTileEntities.FLUID_IMPORT_HATCH[GTValues.IV], EnumFacing.SOUTH)
                .where('J', MetaTileEntities.FLUID_EXPORT_HATCH[GTValues.IV], EnumFacing.SOUTH)
                .build();
        return Lists.newArrayList(shapeInfo);
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
