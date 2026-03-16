package tj.mixin.gregicality;

import gregicadditions.machines.multi.simple.TileEntityLargeChemicalReactor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tj.builder.multicontrollers.GUIDisplayBuilder;

@Mixin(value = TileEntityLargeChemicalReactor.class, remap = false)
public abstract class TileEntityLargeChemicalReactorMixin extends GARecipeMapMultiblockControllerMixin {

    @Shadow
    private int energyBonus;

    public TileEntityLargeChemicalReactorMixin(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    protected void configureDisplayText(GUIDisplayBuilder builder) {
        super.configureDisplayText(builder);
        if (!this.isStructureFormed()) return;
        builder.addTextComponent(new TextComponentTranslation("gregtech.multiblock.universal.energy_usage", 100 - this.energyBonus).setStyle(new Style().setColor(TextFormatting.AQUA)));
    }
}
