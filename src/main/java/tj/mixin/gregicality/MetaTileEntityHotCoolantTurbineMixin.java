package tj.mixin.gregicality;

import gregicadditions.machines.multi.impl.HotCoolantTurbineWorkableHandler;
import gregicadditions.machines.multi.impl.MetaTileEntityRotorHolderForNuclearCoolant;
import gregicadditions.machines.multi.nuclear.MetaTileEntityHotCoolantTurbine;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
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
import tj.builder.multicontrollers.GUIDisplayBuilder;
import tj.capability.IProgressBar;
import tj.capability.ProgressBar;
import tj.util.TJFluidUtils;

import java.util.Queue;
import java.util.function.UnaryOperator;

@Mixin(value = MetaTileEntityHotCoolantTurbine.class, remap = false)
public abstract class MetaTileEntityHotCoolantTurbineMixin extends HotCoolantMultiblockControllerMixin implements IProgressBar {

    @Shadow
    @Final
    public static MultiblockAbility<MetaTileEntityRotorHolderForNuclearCoolant> ABILITY_ROTOR_HOLDER;

    @Shadow
    @Final
    private static int MIN_DURABILITY_TO_WARN;

    public MetaTileEntityHotCoolantTurbineMixin(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    protected void configureDisplayText(GUIDisplayBuilder builder) {
        super.configureDisplayText(builder);
        if (!this.isStructureFormed()) return;
        MetaTileEntityRotorHolderForNuclearCoolant rotorHolder = getAbilities(ABILITY_ROTOR_HOLDER).get(0);
        FluidStack fuelStack = ((HotCoolantTurbineWorkableHandler) this.workableHandler).getFuelStack();
        int fuelAmount = fuelStack == null ? 0 : fuelStack.amount;

        ITextComponent fuelName = new TextComponentTranslation(fuelAmount == 0 ? "gregtech.fluid.empty" : fuelStack.getUnlocalizedName());
        builder.addTranslationLine("gregtech.multiblock.turbine.fuel_amount", fuelAmount, fuelName);

        if (rotorHolder.getRotorEfficiency() > 0.0) {
            builder.addTranslationLine("gregtech.multiblock.turbine.rotor_speed", rotorHolder.getCurrentRotorSpeed(), rotorHolder.getMaxRotorSpeed())
                    .addTranslationLine("gregtech.multiblock.turbine.rotor_efficiency", (int) (rotorHolder.getRotorEfficiency() * 100));
            int rotorDurability = (int) (rotorHolder.getRotorDurability() * 100);
            if (rotorDurability > MIN_DURABILITY_TO_WARN) {
                builder.addTranslationLine("gregtech.multiblock.turbine.rotor_durability", rotorDurability);
            } else {
                builder.addTextComponent(new TextComponentTranslation("gregtech.multiblock.turbine.low_rotor_durability",
                        MIN_DURABILITY_TO_WARN, rotorDurability).setStyle(new Style().setColor(TextFormatting.RED)));
            }
        }
        builder.addTranslationLine("gregtech.multiblock.generation_eu", this.workableHandler.getRecipeOutputVoltage());
    }

    @Override
    public int[][] getBarMatrix() {
        return new int[2][1];
    }

    @Override
    public void getProgressBars(Queue<UnaryOperator<ProgressBar.ProgressBarBuilder>> bars) {
        HotCoolantTurbineWorkableHandler turbineWorkableHandler = (HotCoolantTurbineWorkableHandler) this.workableHandler;
        FluidStack stack = turbineWorkableHandler.getFuelStack();
        bars.add(bar -> bar.setProgress(this.energyContainer::getEnergyStored).setMaxProgress(this.energyContainer::getEnergyCapacity)
                .setLocale("tj.multiblock.bars.energy")
                .setColor(0xFFF6FF00));
        bars.add(bar -> bar.setProgress(this::getFuelAmount).setMaxProgress(this::getFuelCapacity)
                .setLocale("tj.multiblock.bars.fuel").setParams(() -> new Object[]{stack != null ? stack.getLocalizedName() : ""})
                .setFluidStackSupplier(turbineWorkableHandler::getFuelStack));
    }

    @Unique
    private long getFuelAmount() {
        HotCoolantTurbineWorkableHandler turbineWorkableHandler = (HotCoolantTurbineWorkableHandler) this.workableHandler;
        return TJFluidUtils.getFluidAmountFromTanks(turbineWorkableHandler.getFuelStack(), this.importFluidHandler);
    }

    @Unique
    private long getFuelCapacity() {
        HotCoolantTurbineWorkableHandler turbineWorkableHandler = (HotCoolantTurbineWorkableHandler) this.workableHandler;
        return TJFluidUtils.getFluidCapacityFromTanks(turbineWorkableHandler.getFuelStack(), this.importFluidHandler);
    }
}
