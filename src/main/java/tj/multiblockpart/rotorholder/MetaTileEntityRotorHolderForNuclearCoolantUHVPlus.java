package tj.multiblockpart.rotorholder;

import gregicadditions.client.ClientHandler;
import gregicadditions.machines.multi.impl.MetaTileEntityRotorHolderForNuclearCoolant;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.render.ICubeRenderer;
import net.minecraft.util.ResourceLocation;

public class MetaTileEntityRotorHolderForNuclearCoolantUHVPlus extends MetaTileEntityRotorHolderForNuclearCoolant {

    private ICubeRenderer hatchTexture = null;
    private final int tier;
    private final float maxSpeed;

    public MetaTileEntityRotorHolderForNuclearCoolantUHVPlus(ResourceLocation metaTileEntityId, int tier, float maxSpeed) {
        super(metaTileEntityId, tier, maxSpeed);
        this.tier = tier;
        this.maxSpeed = maxSpeed;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityRotorHolderForNuclearCoolantUHVPlus(metaTileEntityId, getTier(), maxSpeed);
    }

    @Override
    public MultiblockControllerBase getController() {
        return super.getController();
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
            return ClientHandler.VOLTAGE_CASINGS[tier];
        }
        this.setPaintingColor(0xFFFFFF);
        return controller.getBaseTexture(this);
    }

}
