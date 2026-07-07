package tj.mixin.gregic_probe;

import gregtech.api.capability.IWorkable;
import gregtech.api.capability.impl.AbstractRecipeLogic;
import mcjty.theoneprobe.api.ElementAlignment;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.TextStyleClass;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tj.TJConfig;
import tj.integration.theoneprobe.impl.ElementFluidStack;
import tj.mixin.gregtech.IMixinAbstractRecipeLogic;
import vfyjxf.gregicprobe.integration.gregtech.RecipeFluidOutputInfoProvider;

import java.util.List;

@Mixin(value = RecipeFluidOutputInfoProvider.class, remap = false)
public abstract class MixinRecipeFluidOutputInfoProvider {

    @Inject(method = "addProbeInfo(Lgregtech/api/capability/IWorkable;Lmcjty/theoneprobe/api/IProbeInfo;Lnet/minecraft/tileentity/TileEntity;Lnet/minecraft/util/EnumFacing;)V",
            at = @At("HEAD"), cancellable = true)
    private void injectAddProbeInfo(IWorkable capability, IProbeInfo probeInfo, TileEntity tileEntity, EnumFacing enumFacing, CallbackInfo ci) {
        if (!TJConfig.machines.theOneProbeInfoProviderOverrides) return;
        if (capability.getProgress() > 0 && capability instanceof AbstractRecipeLogic) {
            final List<FluidStack> fluidOutputs = ((IMixinAbstractRecipeLogic) capability).getFluidOutputs();
            if (!fluidOutputs.isEmpty()) {
                final IProbeInfo horizontalPane = probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER));
                horizontalPane.text(TextStyleClass.INFO + "{*gregicprobe.top.fluid.outputs*} ");
                for (FluidStack fluidStack : fluidOutputs) {
                    if (fluidStack == null) continue;
                    horizontalPane.element(new ElementFluidStack(fluidStack));
                }
            }
        }
        ci.cancel();
    }
}
