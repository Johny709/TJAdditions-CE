package tj.mixin.gregicality;

import gregicadditions.machines.multi.simple.TileEntityChemicalPlant;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tj.builder.multicontrollers.GUIDisplayBuilder;

@Mixin(value = TileEntityChemicalPlant.class, remap = false)
public abstract class TileEntityChemicalPlantMixin extends MultiRecipeMapMultiblockControllerMixin {

    @Shadow
    private int energyBonus;

    public TileEntityChemicalPlantMixin(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    protected void configureDisplayText(GUIDisplayBuilder builder) {
        super.configureDisplayText(builder);
        if (!this.isStructureFormed()) return;
        builder.addTextComponent(new TextComponentTranslation("gregtech.multiblock.universal.energy_usage", 100 - this.energyBonus).setStyle(new Style().setColor(TextFormatting.AQUA)));
    }
}
