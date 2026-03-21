package tj.mixin.gregicality;

import gregicadditions.machines.multi.MetaTileEntityIndustrialPrimitiveBlastFurnace;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tj.builder.multicontrollers.GUIDisplayBuilder;
import tj.mixin.gregtech.MultiblockWithDisplayBaseMixin;

@Mixin(value = MetaTileEntityIndustrialPrimitiveBlastFurnace.class, remap = false)
public abstract class MetaTileEntityIndustrialPrimitiveBlastFurnaceMixin extends MultiblockWithDisplayBaseMixin {

    @Shadow
    protected int size;

    @Shadow
    private float fuelUnitsLeft;

    @Shadow
    private int efficiency;

    @Shadow
    private int currentProgress;

    @Shadow
    private int maxProgressDuration;

    public MetaTileEntityIndustrialPrimitiveBlastFurnaceMixin(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    protected void configureDisplayText(GUIDisplayBuilder builder) {
        super.configureDisplayText(builder);
        if (!this.isStructureFormed()) return;
        builder.addTranslationLine("gregtech.multiblock.machine.primitive_blast_furnace.industrial.size", this.size)
                .addTranslationLine("gregtech.multiblock.machine.primitive_blast_furnace.industrial.fuelUnitsLeft", (int) (this.fuelUnitsLeft * 100))
                .addTranslationLine("gregtech.multiblock.machine.primitive_blast_furnace.industrial.fuelEfficiency", this.efficiency);
        if (this.currentProgress > 0)
            builder.addTranslationLine("gregtech.multiblock.progress", (int) (this.currentProgress / (double) (this.maxProgressDuration) * 100));
    }
}
