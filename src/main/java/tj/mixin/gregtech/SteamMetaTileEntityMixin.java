package tj.mixin.gregtech;

import gregtech.api.capability.impl.RecipeLogicSteam;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.ProgressWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.SteamMetaTileEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tj.TJConfig;
import tj.gui.TJGuiTextures;
import tj.gui.widgets.TJLabelWidget;
import tj.gui.widgets.TJProgressBarWidget;
import tj.machines.singleblock.MetaTileEntityCoalBoiler;
import tj.util.TJFluidUtils;

@Mixin(value = SteamMetaTileEntity.class, remap = false)
public abstract class SteamMetaTileEntityMixin extends MetaTileEntity {

    @Shadow
    @Final
    public TextureArea BRONZE_BACKGROUND_TEXTURE;

    @Shadow
    protected RecipeLogicSteam workableHandler;

    @Shadow
    @Final
    public TextureArea BRONZE_SLOT_BACKGROUND_TEXTURE;

    @Shadow
    @Final
    protected boolean isHighPressure;

    public SteamMetaTileEntityMixin(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Shadow
    protected abstract TextureArea getFullGuiTexture(String pathTemplate);

    @Inject(method = "createUITemplate", at = @At("HEAD"), cancellable = true)
    private void injectCreateUITemplate(EntityPlayer player, CallbackInfoReturnable<ModularUI.Builder> cir) {
        if (!TJConfig.machines.multiblockUIOverrides) return;
        cir.setReturnValue(ModularUI.builder(BRONZE_BACKGROUND_TEXTURE, 176, 166)
                .widget(new TJLabelWidget(7, -18, 166, 20, this.isHighPressure ? TJGuiTextures.MACHINE_LABEL_STEEL : TJGuiTextures.MACHINE_LABEL_BRONZE)
                        .setItemLabel(this.getStackForm()).setLocale(this.getMetaFullName()))
                .widget(new TJProgressBarWidget(7, 5, 18, 74, this::getSteamAmount, this::getSteamCapacity, true, ProgressWidget.MoveType.VERTICAL)
                        .setLocale("tj.multiblock.bars.fluid", () -> new Object[]{MetaTileEntityCoalBoiler.STEAM.getLocalizedName()})
                        .setFluid(() -> MetaTileEntityCoalBoiler.STEAM)
                        .setTexture(BRONZE_SLOT_BACKGROUND_TEXTURE))
                .widget(new ImageWidget(79, 42, 18, 18, this.getFullGuiTexture("not_enough_steam_%s"))
                        .setPredicate(() -> this.workableHandler.isHasNotEnoughEnergy()))
                .bindPlayerInventory(player.inventory, BRONZE_SLOT_BACKGROUND_TEXTURE));
    }

    @Unique
    private long getSteamAmount() {
        return TJFluidUtils.getFluidAmountFromTanks(MetaTileEntityCoalBoiler.STEAM, this.getImportFluids());
    }

    @Unique
    private long getSteamCapacity() {
        return TJFluidUtils.getFluidCapacityFromTanks(MetaTileEntityCoalBoiler.STEAM, this.getImportFluids());
    }
}
