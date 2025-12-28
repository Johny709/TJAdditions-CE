package tj.mixin.gregicality;

import gregicadditions.GAUtility;
import gregicadditions.machines.energy.GAMetaTileEntityTransformer;
import gregicadditions.theoneprobe.GATransformerProvider;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.metatileentity.MetaTileEntity;
import mcjty.theoneprobe.api.ElementAlignment;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.TextStyleClass;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import tj.TJConfig;
import tj.TJValues;

@Mixin(value = GATransformerProvider.class, remap = false)
public abstract class GATransformerProviderMixin {

    @Inject(method = "addProbeInfo(Lgregtech/api/capability/IEnergyContainer;Lmcjty/theoneprobe/api/IProbeInfo;Lnet/minecraft/tileentity/TileEntity;Lnet/minecraft/util/EnumFacing;)V",
            at = @At(value = "INVOKE", target = "Lgregicadditions/machines/energy/GAMetaTileEntityTransformer;isInverted()Z"),
            locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
    private void injectAddProbeInfo(IEnergyContainer capability, IProbeInfo probeInfo, TileEntity tileEntity, EnumFacing sideHit, CallbackInfo ci,
                                    MetaTileEntity metaTileEntity, GAMetaTileEntityTransformer mteTransformer, String inputVoltageN, String outputVoltageN, long inputAmperage, long outputAmperage, IProbeInfo horizontalPane) {
        if (!TJConfig.machines.theOneProbeInfoProviderOverrides) return;
        String transformInfo;
        int inputTier = GAUtility.getTierByVoltage(capability.getInputVoltage()) + 1;
        int outputTier = GAUtility.getTierByVoltage(capability.getOutputVoltage()) + 1;
        // Step Up/Step Down line
        if (mteTransformer.isInverted()) {
            transformInfo = "{*gregtech.top.transform_up*} ";
        } else {
            transformInfo = "{*gregtech.top.transform_down*} ";
        }
        transformInfo += TJValues.VCC[inputTier] + inputVoltageN + " §7(§e" + TJValues.thousandFormat.format(inputAmperage) + "A§7) -> "
                + TJValues.VCC[outputTier] + outputVoltageN + " §7(§e" + TJValues.thousandFormat.format(outputAmperage) + "A§7)";
        horizontalPane.text(TextStyleClass.INFO + transformInfo);

        // Input/Output side line
        horizontalPane = probeInfo.vertical(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER));
        if (capability.inputsEnergy(sideHit)) {
            transformInfo = "{*gregtech.top.transform_input*} "
                    + TJValues.VCC[inputTier] + inputVoltageN + " §7(§e" + TJValues.thousandFormat.format(inputAmperage) + "A§7)";
            horizontalPane.text(TextStyleClass.INFO + transformInfo);

        } else if (capability.outputsEnergy(sideHit)) {
            transformInfo = "{*gregtech.top.transform_output*} "
                    + TJValues.VCC[outputTier] + outputVoltageN + " §7(§e" + TJValues.thousandFormat.format(outputAmperage) + "A§7)";
            horizontalPane.text(TextStyleClass.INFO + transformInfo);
        }
        ci.cancel();
    }
}
