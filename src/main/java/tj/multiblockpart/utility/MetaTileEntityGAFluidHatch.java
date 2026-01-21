package tj.multiblockpart.utility;

import gregicadditions.client.ClientHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.render.ICubeRenderer;
import gregtech.common.metatileentities.electric.multiblockpart.MetaTileEntityFluidHatch;
import net.minecraft.util.ResourceLocation;

public class MetaTileEntityGAFluidHatch extends MetaTileEntityFluidHatch {

    private final boolean isExportHatch;
    private ICubeRenderer hatchTexture = null;

    public MetaTileEntityGAFluidHatch(ResourceLocation metaTileEntityId, int tier, boolean isExportHatch) {
        super(metaTileEntityId, tier, isExportHatch);
        this.isExportHatch = isExportHatch;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityGAFluidHatch(this.metaTileEntityId, this.getTier(), this.isExportHatch);
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
            this.setPaintingColor(DEFAULT_PAINTING_COLOR);
            return ClientHandler.VOLTAGE_CASINGS[getTier()];
        }
        this.setPaintingColor(0xFFFFFF);
        return controller.getBaseTexture(this);
    }
}
