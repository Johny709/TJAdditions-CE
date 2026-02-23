package tj.integration.jei.multi.electric;

import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.components.PistonCasing;
import gregicadditions.item.metal.MetalCasing2;
import gregicadditions.jei.GAMultiblockShapeInfo;
import gregicadditions.machines.GATileEntities;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.integration.jei.multiblock.MultiblockShapeInfo;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumFacing;
import tj.TJConfig;
import tj.integration.jei.TJMultiblockInfoPage;
import tj.machines.TJMetaTileEntities;

import java.util.ArrayList;
import java.util.List;

import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;

public class LargeNamingMachineInfo extends TJMultiblockInfoPage {

    @Override
    public MultiblockControllerBase getController() {
        return TJMetaTileEntities.LARGE_NAMING_MACHINE;
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        List<MultiblockShapeInfo> shapeInfos = new ArrayList<>();
        GAMultiblockShapeInfo.Builder builder = GAMultiblockShapeInfo.builder(FRONT, UP, LEFT)
                .aisle("~CCC~", "CCECC", "~CCC~", "~~C~~", "~~~~~")
                .aisle("CCCCC", "C###C", "C###C", "~CCC~", "~~C~~")
                .aisle("CCCCC", "C###C", "C###C", "CC#CC", "~CPC~")
                .aisle("CCCCC", "C###C", "C###C", "~CCC~", "~~C~~")
                .aisle("~CMC~", "CISOC", "~CCC~", "~~C~~", "~~~~~")
                .where('S', this.getController(), EnumFacing.WEST)
                .where('M', GATileEntities.MAINTENANCE_HATCH[0], EnumFacing.WEST)
                .where('C', GAMetaBlocks.METAL_CASING_2.getState(MetalCasing2.CasingType.IRON));
        for (int tier = 0; tier < 15; tier++) {
            shapeInfos.add(builder.where('E', this.getEnergyHatch(tier, false), EnumFacing.EAST)
                    .where('I', MetaTileEntities.ITEM_IMPORT_BUS[tier], EnumFacing.WEST)
                    .where('O', MetaTileEntities.ITEM_EXPORT_BUS[tier], EnumFacing.WEST)
                    .where('P', GAMetaBlocks.PISTON_CASING.getState(PistonCasing.CasingType.values()[Math.max(0, tier - 1)]))
                    .build());
        }
        return shapeInfos;
    }

    @Override
    public String[] getDescription() {
        return new String[]{I18n.format("gtadditions.multiblock.universal.tooltip.4", TJConfig.largeNamingMachine.stack).replace("§7", "§r"),
                I18n.format("tj.machine.naming_machine.description")};
    }
}
