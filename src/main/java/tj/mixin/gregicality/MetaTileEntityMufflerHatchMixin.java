package tj.mixin.gregicality;

import gregicadditions.capabilities.impl.GARecipeMapMultiblockController;
import gregicadditions.machines.multi.multiblockpart.MetaTileEntityMufflerHatch;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import tj.capability.IMuffler;

import static gregicadditions.capabilities.impl.GARecipeMapMultiblockController.XSTR_RAND;

@Mixin(value = MetaTileEntityMufflerHatch.class, remap = false)
public abstract class MetaTileEntityMufflerHatchMixin {

    @Shadow
    @SideOnly(Side.CLIENT)
    public abstract void pollutionParticles();

    @Inject(method = "update", at = @At(value = "INVOKE", target = "Lgregicadditions/machines/multi/multiblockpart/MetaTileEntityMufflerHatch;getController()Lgregtech/api/metatileentity/multiblock/MultiblockControllerBase;"), cancellable = true)
    private void injectUpdate(CallbackInfo ci) {
        MetaTileEntityMufflerHatch mufflerHatch = (MetaTileEntityMufflerHatch)(Object)this;
        MultiblockControllerBase controllerBase = mufflerHatch.getController();
        if (mufflerHatch.getWorld().isRemote)
            if (controllerBase instanceof GARecipeMapMultiblockController && ((GARecipeMapMultiblockController) controllerBase).isActive())
                this.pollutionParticles();
            else if (controllerBase instanceof IMuffler && ((IMuffler) controllerBase).isActive())
                this.pollutionParticles();
        ci.cancel();
    }

    @Inject(method = "pollutionParticles", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/EnumFacing;getYOffset()I", ordinal = 2, remap = true),
            cancellable = true, locals = LocalCapture.CAPTURE_FAILSOFT)
    private void injectPollutionParticles(CallbackInfo ci, BlockPos pos, EnumFacing facing, float xPos, float yPos, float zPos, float ySpd) {
        float xSpd;
        float zSpd;
        if (facing.getYOffset() == -1) {
            float temp = XSTR_RAND.nextFloat() * 2 * (float) Math.PI;
            xSpd = (float) Math.sin(temp) * 0.1F;
            zSpd = (float) Math.cos(temp) * 0.1F;
        } else {
            xSpd = facing.getXOffset() * (0.1F + 0.2F * XSTR_RAND.nextFloat());
            zSpd = facing.getZOffset() * (0.1F + 0.2F * XSTR_RAND.nextFloat());
        }
        MetaTileEntityMufflerHatch mufflerHatch = (MetaTileEntityMufflerHatch)(Object)this;
        MultiblockControllerBase controllerBase = mufflerHatch.getController();
        if (controllerBase instanceof GARecipeMapMultiblockController)
            ((GARecipeMapMultiblockController) controllerBase).runMufflerEffect(xPos + 0.5F * XSTR_RAND.nextFloat(), yPos + 0.5F * XSTR_RAND.nextFloat(), zPos + 0.5F * XSTR_RAND.nextFloat(), xSpd, ySpd, zSpd);
        else if (controllerBase instanceof IMuffler)
            ((IMuffler) controllerBase).runMufflerEffect(xPos + 0.5F * XSTR_RAND.nextFloat(), yPos + 0.5F * XSTR_RAND.nextFloat(), zPos + 0.5F * XSTR_RAND.nextFloat(), xSpd, ySpd, zSpd);
        ci.cancel();
    }
}
