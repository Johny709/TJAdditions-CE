package tj.integration.jei.multi.electric;

import gregicadditions.item.GAMetaItems;
import gregicadditions.machines.GATileEntities;
import gregicadditions.machines.multi.impl.MetaTileEntityRotorHolderForNuclearCoolant;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.unification.material.Materials;
import gregtech.common.items.behaviors.TurbineRotorBehavior;
import gregtech.common.metatileentities.MetaTileEntities;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import tj.capability.impl.workable.XLHotCoolantTurbineWorkableHandler;
import tj.integration.jei.TJMultiblockInfoPage;
import tj.integration.jei.TJMultiblockShapeInfo;
import tj.integration.jei.multi.parallel.IParallelMultiblockInfoPage;
import tj.machines.multi.electric.MetaTileEntityXLHotCoolantTurbine;

import java.util.ArrayList;
import java.util.List;

import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;

public class XLHotCoolantTurbineInfo extends TJMultiblockInfoPage implements IParallelMultiblockInfoPage {

    public final MetaTileEntityXLHotCoolantTurbine turbine;

    public XLHotCoolantTurbineInfo(MetaTileEntityXLHotCoolantTurbine turbine) {
        this.turbine = turbine;
    }

    @Override
    public MultiblockControllerBase getController() {
        return turbine;
    }

    @Override
    public List<TJMultiblockShapeInfo[]> getMatchingShapes(TJMultiblockShapeInfo[] shapes) {
        MetaTileEntityHolder holderNorth = new MetaTileEntityHolder();
        MetaTileEntityHolder holderSouth = new MetaTileEntityHolder();
        holderNorth.setMetaTileEntity(GATileEntities.ROTOR_HOLDER[2]);
        holderSouth.setMetaTileEntity(GATileEntities.ROTOR_HOLDER[2]);
        ItemStack rotorStack = GAMetaItems.HUGE_TURBINE_ROTOR.getStackForm();
        //noinspection ConstantConditions
        TurbineRotorBehavior.getInstanceFor(rotorStack).setPartMaterial(rotorStack, Materials.Darmstadtium);
        ((MetaTileEntityRotorHolderForNuclearCoolant) holderNorth.getMetaTileEntity()).getRotorInventory().setStackInSlot(0, rotorStack);
        ((MetaTileEntityRotorHolderForNuclearCoolant) holderSouth.getMetaTileEntity()).getRotorInventory().setStackInSlot(0, rotorStack);
        List<TJMultiblockShapeInfo[]> shapeInfos = new ArrayList<>();
        for (int shapeInfo = 0; shapeInfo < 7; shapeInfo++) {
            TJMultiblockShapeInfo.Builder builder = TJMultiblockShapeInfo.builder(FRONT, UP, LEFT)
                    .aisle("CCCCCCC", "CCCCCCC", "CCCCCCC", "CCCECCC", "CCCCCCC", "CCCCCCC", "CCCCCCC")
                    .aisle("CCCCCCC", "R#####T", "CCCCCCC", "CCCCCCC", "CCCCCCC", "R#####T", "CCCCCCC")
                    .aisle("CCCCCCC", "CCCCCCC", "CCCCCCC", "CCCCCCC", "CCCCCCC", "CCCCCCC", "CCCCCCC");
            for (int j = 0; j <= shapeInfo; j++) {
                builder.aisle("CCCCCCC", "CCCCCCC", "CCCCCCC", "CCCCCCC", "CCCCCCC", "CCCCCCC", "CCCCCCC");
                builder.aisle("CCCCCCC", "R#####T", "CCCCCCC", "CCCCCCC", "CCCCCCC", "R#####T", "CCCCCCC");
                builder.aisle("CCCCCCC", "CCCCCCC", "CCCCCCC", "CCCCCCC", "CCCCCCC", "CCCCCCC", "CCCCCCC");
            }
            builder.aisle("CCCCCCC", "CCCCCCC", "CCCCCCC", "CCCCCCC", "CCCCCCC", "CCCCCCC", "CCCCCCC")
                    .aisle("CCCCCCC", "R#####T", "CCCCCCC", "CCCCCCC", "CCCCCCC", "R#####T", "CCCCCCC")
                    .aisle("CCCCCCC", "CCCCCCC", "CCCOCCC", "CCISJCC", "CCCMCCC", "CCCCCCC", "CCCCCCC");
            TJMultiblockShapeInfo[] infos = new TJMultiblockShapeInfo[15];
            for (int tier = 0; tier < infos.length; tier++) {
                infos[tier] = builder.where('S', this.turbine, EnumFacing.WEST)
                        .where('C', this.turbine.turbineType.casingState)
                        .where('R', holderNorth.getMetaTileEntity(), EnumFacing.NORTH)
                        .where('T', holderSouth.getMetaTileEntity(), EnumFacing.SOUTH)
                        .where('E', this.getEnergyHatch(tier, true), EnumFacing.EAST)
                        .where('I', MetaTileEntities.FLUID_IMPORT_HATCH[tier], EnumFacing.WEST)
                        .where('J', MetaTileEntities.ITEM_IMPORT_BUS[tier], EnumFacing.WEST)
                        .where('O', MetaTileEntities.FLUID_EXPORT_HATCH[tier], EnumFacing.WEST)
                        .where('M', GATileEntities.MAINTENANCE_HATCH[0], EnumFacing.WEST)
                        .where(!this.turbine.turbineType.hasOutputHatch ? 'O' : '#', !this.turbine.turbineType.hasOutputHatch ? this.turbine.turbineType.casingState : Blocks.AIR.getDefaultState())
                        .build();
            }
            shapeInfos.add(infos);
        }
        return shapeInfos;
    }

    @Override
    public String[] getDescription() {
        return new String[]{I18n.format("tj.multiblock.turbine.description"),
                I18n.format("tj.multiblock.turbine.fast_mode.description"),
                I18n.format("tj.multiblock.universal.tooltip.1", this.turbine.getRecipeMapName()),
                I18n.format("tj.multiblock.universal.tooltip.2", 12),
                I18n.format("tj.multiblock.turbine.tooltip.efficiency"),
                I18n.format("tj.multiblock.turbine.tooltip.efficiency.normal", (int) XLHotCoolantTurbineWorkableHandler.getTurbineBonus()),
                I18n.format("tj.multiblock.turbine.tooltip.efficiency.fast", 100)};
    }

    @Override
    public float getDefaultZoom() {
        return 0.5f;
    }
}
