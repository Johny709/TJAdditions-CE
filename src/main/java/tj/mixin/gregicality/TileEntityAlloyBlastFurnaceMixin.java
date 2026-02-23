package tj.mixin.gregicality;

import gregicadditions.machines.multi.TileEntityAlloyBlastFurnace;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tj.builder.multicontrollers.UIDisplayBuilder;

@Mixin(value = TileEntityAlloyBlastFurnace.class, remap = false)
public abstract class TileEntityAlloyBlastFurnaceMixin extends GARecipeMapMultiblockControllerMixin {
    @Shadow
    private int blastFurnaceTemperature;

    @Shadow
    private int bonusTemperature;

    public TileEntityAlloyBlastFurnaceMixin(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    protected void configureDisplayText(UIDisplayBuilder builder) {
        super.configureDisplayText(builder);
        if (!this.isStructureFormed()) return;
        builder.addTranslationLine("gregtech.multiblock.blast_furnace.max_temperature", this.blastFurnaceTemperature)
                .addTranslationLine("gtadditions.multiblock.blast_furnace.additional_temperature", this.bonusTemperature);
    }
}
