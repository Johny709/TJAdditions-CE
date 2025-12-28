package tj.multiblockpart.utility;

import gregicadditions.client.ClientHandler;
import gregicadditions.machines.multi.multiblockpart.MetaTileEntityMultiFluidHatch;
import gregtech.api.capability.impl.FluidHandlerProxy;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.ItemHandlerProxy;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.render.ICubeRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidTank;

import javax.annotation.Nullable;
import java.util.List;

public class MetaTileEntityTJMultiFluidHatch extends MetaTileEntityMultiFluidHatch {

    private final int TANK_SIZE;
    private final boolean isExportHatch;
    private ICubeRenderer hatchTexture = null;
    private final int tier;

    public MetaTileEntityTJMultiFluidHatch(ResourceLocation metaTileEntityId, int tier, boolean isExportHatch, int tankSize) {
        super(metaTileEntityId, tier, isExportHatch);
        this.TANK_SIZE = tankSize;
        this.isExportHatch = isExportHatch;
        this.tier = tier;
        initializeInventory();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityTJMultiFluidHatch(metaTileEntityId, this.getTier(), this.isExportHatch, this.TANK_SIZE);
    }

    @Override
    protected void initializeInventory() {
        FluidTank[] fluidsHandlers = new FluidTank[(int) Math.pow(this.getTier(), 2)];
        for (int i = 0; i <fluidsHandlers.length; i++) {
            fluidsHandlers[i] = new FluidTank(TANK_SIZE);
        }
        this.fluidTanks = new FluidTankList(false, fluidsHandlers);
        this.fluidInventory = fluidTanks;
        this.importItems = this.createImportItemHandler();
        this.exportItems = this.createExportItemHandler();
        this.itemInventory = new ItemHandlerProxy(this.importItems, this.exportItems);
        this.importFluids = this.createImportFluidHandler();
        this.exportFluids = this.createExportFluidHandler();
        this.fluidInventory = new FluidHandlerProxy(this.importFluids, this.exportFluids);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gtadditions.machine.multi_fluid_hatch_universal.tooltip.1", TANK_SIZE));
        tooltip.add(I18n.format("gtadditions.machine.multi_fluid_hatch_universal.tooltip.2", (int) Math.pow(this.getTier(), 2)));
        tooltip.add(I18n.format("gregtech.universal.enabled"));
    }

    @Override
    public ICubeRenderer getBaseTexture() {
        MultiblockControllerBase controller = getController();
        if (controller != null) {
            this.hatchTexture = controller.getBaseTexture(this);
        }
        if (controller == null && this.hatchTexture != null) {
            return this.hatchTexture;
        }
        if (controller == null) {
            return ClientHandler.VOLTAGE_CASINGS[tier + 2];
        }
        this.setPaintingColor(0xFFFFFF);
        return controller.getBaseTexture(this);
    }
}
