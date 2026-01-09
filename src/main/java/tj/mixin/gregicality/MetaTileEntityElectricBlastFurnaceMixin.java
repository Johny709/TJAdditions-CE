package tj.mixin.gregicality;

import gregicadditions.machines.multi.override.MetaTileEntityElectricBlastFurnace;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import org.spongepowered.asm.mixin.Mixin;
import tj.builder.multicontrollers.UIDisplayBuilder;

@Mixin(value = MetaTileEntityElectricBlastFurnace.class, remap = false)
public abstract class MetaTileEntityElectricBlastFurnaceMixin extends GARecipeMapMultiblockControllerMixin implements IMetaTileEntityElectricBlastFurnaceMixin {

    public MetaTileEntityElectricBlastFurnaceMixin(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    protected void configureDisplayText(UIDisplayBuilder builder) {
        super.configureDisplayText(builder);
        builder.addTextComponent(new TextComponentTranslation("gregtech.multiblock.blast_furnace.max_temperature", this.getBlastFurnaceTemperature()))
                .addTextComponent(new TextComponentTranslation("gtadditions.multiblock.blast_furnace.additional_temperature", this.getBonusTemperature()));
    }
}
