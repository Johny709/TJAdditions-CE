package tj.mixin.gregicality;

import gregicadditions.machines.multi.override.MetaTileEntityPyrolyseOven;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tj.builder.multicontrollers.GUIDisplayBuilder;

@Mixin(value = MetaTileEntityPyrolyseOven.class, remap = false)
public abstract class MetaTileEntityPyrolyseOvenMixin extends GARecipeMapMultiblockControllerMixin {

    @Shadow
    protected int heatingCoilLevel;

    @Shadow
    protected int heatingCoilDiscount;

    public MetaTileEntityPyrolyseOvenMixin(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    protected void configureDisplayText(GUIDisplayBuilder builder) {
        super.configureDisplayText(builder);
        if (!this.isStructureFormed()) return;
        builder.addTranslationLine("gregtech.multiblock.multi_furnace.heating_coil_level", this.heatingCoilLevel)
                .addTextComponent(new TextComponentTranslation("gregtech.multiblock.universal.energy_usage", this.heatingCoilDiscount)
                        .setStyle(new Style().setColor(TextFormatting.AQUA)));
    }
}
