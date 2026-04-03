package tj.mixin.gregic_probe;

import gregicadditions.GAValues;
import gregtech.api.capability.IWorkable;
import gregtech.api.capability.impl.AbstractRecipeLogic;
import mcjty.theoneprobe.api.ElementAlignment;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.TextStyleClass;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tj.TJConfig;
import tj.TJValues;
import tj.capability.IRecipeInfo;
import tj.util.TJUtility;
import vfyjxf.gregicprobe.integration.gregtech.RecipeInfoProvider;

@Mixin(value = RecipeInfoProvider.class, remap = false)
public abstract class RecipeInfoProviderMixin {

    @Inject(method = "addProbeInfo(Lgregtech/api/capability/IWorkable;Lmcjty/theoneprobe/api/IProbeInfo;Lnet/minecraft/tileentity/TileEntity;Lnet/minecraft/util/EnumFacing;)V",
            at = @At("HEAD"), cancellable = true)
    private void injectAddProbeInfo(IWorkable capability, IProbeInfo probeInfo, TileEntity tileEntity, EnumFacing enumFacing, CallbackInfo ci) {
        if (!TJConfig.machines.theOneProbeInfoProviderOverrides) return;
        long recipeEUt;
        if ((capability instanceof AbstractRecipeLogic && (recipeEUt = ((AbstractRecipeLogic) capability).getRecipeEUt()) > 0) || (capability instanceof IRecipeInfo && (recipeEUt = ((IRecipeInfo) capability).getEnergyPerTick()) > 0)) {
            IProbeInfo horizontalPane = probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER));
            int tier = TJUtility.getTierFromVoltage(recipeEUt);
            horizontalPane.text(TextStyleClass.INFO + "{*gregicprobe:top.eut*} ");
            horizontalPane.text(TextStyleClass.INFO + "§e" + TJValues.thousandFormat.format(recipeEUt) + " §rEU/t §7(" + (tier > 14 ? "§c§lM§e§lA§a§lX§b§l+§d§l" + (tier - 14) : TJValues.VCC[tier] + GAValues.VN[tier]) + "§r§7)");
        }
        ci.cancel();
    }
}
