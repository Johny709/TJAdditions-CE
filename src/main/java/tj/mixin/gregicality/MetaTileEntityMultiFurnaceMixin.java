package tj.mixin.gregicality;

import gregicadditions.machines.multi.override.MetaTileEntityMultiFurnace;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tj.builder.multicontrollers.UIDisplayBuilder;

@Mixin(value = MetaTileEntityMultiFurnace.class, remap = false)
public abstract class MetaTileEntityMultiFurnaceMixin extends GARecipeMapMultiblockControllerMixin {

    @Shadow
    protected int heatingCoilLevel;

    @Shadow
    protected int heatingCoilDiscount;

    public MetaTileEntityMultiFurnaceMixin(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    protected void configureDisplayText(UIDisplayBuilder builder) {
        super.configureDisplayText(builder);
        if (!this.isStructureFormed()) return;
        builder.addTextComponent(new TextComponentTranslation("gregtech.multiblock.multi_furnace.heating_coil_level", this.heatingCoilLevel))
                .addTextComponent(new TextComponentTranslation("gregtech.multiblock.multi_furnace.heating_coil_discount", this.heatingCoilDiscount));
    }
}
