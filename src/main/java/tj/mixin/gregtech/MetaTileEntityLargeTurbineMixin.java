package tj.mixin.gregtech;

import gregtech.common.metatileentities.electric.multiblockpart.MetaTileEntityRotorHolder;
import gregtech.common.metatileentities.multi.electric.generator.LargeTurbineWorkableHandler;
import gregtech.common.metatileentities.multi.electric.generator.MetaTileEntityLargeTurbine;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import tj.builder.multicontrollers.UIDisplayBuilder;
import tj.capability.IProgressBar;
import tj.capability.ProgressBar;
import tj.util.TJFluidUtils;

import java.util.Queue;
import java.util.function.UnaryOperator;

@Mixin(value = MetaTileEntityLargeTurbine.class, remap = false)
public abstract class MetaTileEntityLargeTurbineMixin extends RotorHolderMultiblockControllerMixin implements IProgressBar {

    @Shadow
    @Final
    private static int MIN_DURABILITY_TO_WARN;

    public MetaTileEntityLargeTurbineMixin(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    protected void configureDisplayText(UIDisplayBuilder builder) {
        super.configureDisplayText(builder);
        if (!this.isStructureFormed()) return;
        MetaTileEntityRotorHolder rotorHolder = this.getRotorHolder();
        FluidStack fuelStack = ((LargeTurbineWorkableHandler) this.workableHandler).getFuelStack();
        int fuelAmount = fuelStack == null ? 0 : fuelStack.amount;

        ITextComponent fuelName = new TextComponentTranslation(fuelAmount == 0 ? "gregtech.fluid.empty" : fuelStack.getUnlocalizedName());
        builder.addTextComponent(new TextComponentTranslation("gregtech.multiblock.turbine.fuel_amount", fuelAmount, fuelName));

        if (rotorHolder.getRotorEfficiency() > 0.0) {
            builder.addTextComponent(new TextComponentTranslation("gregtech.multiblock.turbine.rotor_speed", rotorHolder.getCurrentRotorSpeed(), rotorHolder.getMaxRotorSpeed()))
                    .addTextComponent(new TextComponentTranslation("gregtech.multiblock.turbine.rotor_efficiency", (int) (rotorHolder.getRotorEfficiency() * 100)));
            int rotorDurability = (int) (rotorHolder.getRotorDurability() * 100);
            if (rotorDurability > MIN_DURABILITY_TO_WARN) {
                builder.addTextComponent(new TextComponentTranslation("gregtech.multiblock.turbine.rotor_durability", rotorDurability));
            } else {
                builder.addTextComponent(new TextComponentTranslation("gregtech.multiblock.turbine.low_rotor_durability",
                        MIN_DURABILITY_TO_WARN, rotorDurability).setStyle(new Style().setColor(TextFormatting.RED)));
            }
        }

        if (!this.isRotorFaceFree()) {
            builder.addTextComponent(new TextComponentTranslation("gregtech.multiblock.turbine.obstructed")
                    .setStyle(new Style().setColor(TextFormatting.RED)));
        }
    }

    @Override
    public int[][] getBarMatrix() {
        return new int[1][1];
    }

    @Override
    public void getProgressBars(Queue<UnaryOperator<ProgressBar.ProgressBarBuilder>> bars) {
        LargeTurbineWorkableHandler workableHandler = ((gregicadditions.machines.multi.override.MetaTileEntityLargeTurbine.LargeTurbineWorkableHandler) this.workableHandler);
        FluidStack stack = workableHandler.getFuelStack();
        bars.add(bar -> bar.setProgress(this::getFuelAmount).setMaxProgress(this::getFuelCapacity)
                .setLocale("tj.multiblock.bars.fuel").setParams(() -> new Object[]{stack != null ? stack.getLocalizedName() : ""})
                .setFluidStackSupplier(workableHandler::getFuelStack));
    }

    @Unique
    private long getFuelAmount() {
        LargeTurbineWorkableHandler workableHandler = ((gregicadditions.machines.multi.override.MetaTileEntityLargeTurbine.LargeTurbineWorkableHandler) this.workableHandler);
        return TJFluidUtils.getFluidAmountFromTanks(workableHandler.getFuelStack(), this.importFluidHandler);
    }

    @Unique
    private long getFuelCapacity() {
        LargeTurbineWorkableHandler workableHandler = ((gregicadditions.machines.multi.override.MetaTileEntityLargeTurbine.LargeTurbineWorkableHandler) this.workableHandler);
        return TJFluidUtils.getFluidCapacityFromTanks(workableHandler.getFuelStack(), this.importFluidHandler);
    }
}
