package tj.mixin.gregicality;

import gregicadditions.GAMaterials;
import gregicadditions.machines.multi.MetaTileEntityCosmicRayDetector;
import gregtech.api.capability.IMultipleTankHandler;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tj.builder.multicontrollers.UIDisplayBuilder;
import tj.mixin.gregtech.MultiblockWithDisplayBaseMixin;

@Mixin(value = MetaTileEntityCosmicRayDetector.class, remap = false)
public abstract class MetaTileEntityCosmicRayDetectorMixin extends MultiblockWithDisplayBaseMixin {
    @Shadow
    private long maxVoltage;

    @Shadow
    private boolean canSeeSky;

    @Shadow
    protected IMultipleTankHandler exportFluidHandler;

    @Shadow
    private boolean hasEnoughEnergy;

    @Shadow
    private int amount;

    public MetaTileEntityCosmicRayDetectorMixin(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    protected void configureDisplayText(UIDisplayBuilder builder) {
        super.configureDisplayText(builder);
        if (!this.isStructureFormed()) return;
        builder.addTranslationLine("gregtech.multiblock.universal.framework", this.maxVoltage);
        if (!this.canSeeSky)
            builder.addTextComponent(new TextComponentTranslation("gtadditions.multiblock.cosmic_ray_detector.tooltip.1")
                    .setStyle(new Style().setColor(TextFormatting.RED)));
        if (this.exportFluidHandler.fill(GAMaterials.HeavyLeptonMix.getFluid(1), false) < 1) {
            builder.addTextComponent(new TextComponentTranslation("gtadditions.multiblock.cosmic_ray_detector.tooltip.8")
                    .setStyle(new Style().setColor(TextFormatting.RED)));
        } else if (!this.hasEnoughEnergy) {
            builder.addTextComponent(new TextComponentTranslation("gtadditions.multiblock.cosmic_ray_detector.tooltip.8")
                    .setStyle(new Style().setColor(TextFormatting.RED)));
        }
        if (this.hasEnoughEnergy && this.canSeeSky)
            builder.addTranslationLine("gtadditions.multiblock.cosmic_ray_detector.tooltip.5", this.amount);
    }
}
