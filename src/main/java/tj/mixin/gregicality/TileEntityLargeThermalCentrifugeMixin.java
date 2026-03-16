package tj.mixin.gregicality;

import gregicadditions.machines.multi.simple.TileEntityLargeThermalCentrifuge;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tj.builder.multicontrollers.GUIDisplayBuilder;

@Mixin(value = TileEntityLargeThermalCentrifuge.class, remap = false)
public abstract class TileEntityLargeThermalCentrifugeMixin extends LargeSimpleRecipeMapMultiblockControllerMixin {
    @Shadow
    private int speedBonus;

    public TileEntityLargeThermalCentrifugeMixin(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    protected void configureDisplayText(GUIDisplayBuilder builder) {
        super.configureDisplayText(builder);
        if (!this.isStructureFormed()) return;
        builder.addTextComponent(new TextComponentTranslation("gregtech.multiblock.universal.speed_increase", this.speedBonus).setStyle(new Style().setColor(TextFormatting.AQUA)));
    }
}
