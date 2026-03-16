package tj.mixin.gregicality;

import gregicadditions.machines.multi.mega.MetaTileEntityMegaBlastFurnace;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tj.builder.multicontrollers.GUIDisplayBuilder;

@Mixin(value = MetaTileEntityMegaBlastFurnace.class, remap = false)
public abstract class MetaTileEntityMegaBlastFurnaceMixin extends MegaMultiblockRecipeMapControllerMixin {

    @Shadow
    protected int blastFurnaceTemperature;

    @Shadow
    private int bonusTemperature;

    public MetaTileEntityMegaBlastFurnaceMixin(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    protected void configureDisplayText(GUIDisplayBuilder builder) {
        super.configureDisplayText(builder);
        if (!this.isStructureFormed()) return;
        builder.addTranslationLine("gregtech.multiblock.blast_furnace.max_temperature", this.blastFurnaceTemperature)
                .addTranslationLine("gtadditions.multiblock.blast_furnace.additional_temperature", this.bonusTemperature);
    }
}
