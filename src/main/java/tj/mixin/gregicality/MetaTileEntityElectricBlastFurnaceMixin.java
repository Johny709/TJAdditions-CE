package tj.mixin.gregicality;

import gregicadditions.machines.multi.override.MetaTileEntityElectricBlastFurnace;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tj.builder.multicontrollers.UIDisplayBuilder;

@Mixin(value = MetaTileEntityElectricBlastFurnace.class, remap = false)
public abstract class MetaTileEntityElectricBlastFurnaceMixin extends GARecipeMapMultiblockControllerMixin {

    @Shadow
    protected int blastFurnaceTemperature;

    @Shadow
    private int bonusTemperature;

    public MetaTileEntityElectricBlastFurnaceMixin(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    protected void configureDisplayText(UIDisplayBuilder builder) {
        super.configureDisplayText(builder);
        builder.addTextComponent(new TextComponentTranslation("gregtech.multiblock.blast_furnace.max_temperature", this.blastFurnaceTemperature))
                .addTextComponent(new TextComponentTranslation("gtadditions.multiblock.blast_furnace.additional_temperature", this.bonusTemperature));
    }
}
