package tj.mixin.gregicality;

import gregicadditions.Gregicality;
import gregicadditions.machines.multi.simple.LargeSimpleRecipeMapMultiblockController;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tj.builder.multicontrollers.UIDisplayBuilder;

@Mixin(value = LargeSimpleRecipeMapMultiblockController.class, remap = false)
public abstract class LargeSimpleRecipeMapMultiblockControllerMixin extends GARecipeMapMultiblockControllerMixin {

    @Shadow
    public long maxVoltage;

    public LargeSimpleRecipeMapMultiblockControllerMixin(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    protected void configureDisplayText(UIDisplayBuilder builder) {
        super.configureDisplayText(builder);
        if (!this.isStructureFormed()) return;
        builder.addTextComponent(new TextComponentTranslation("gregtech.multiblock.universal.framework", this.maxVoltage));
    }
}
