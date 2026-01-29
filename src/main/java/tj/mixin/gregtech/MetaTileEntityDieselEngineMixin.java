package tj.mixin.gregtech;

import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.FuelRecipeLogic;
import gregtech.api.unification.material.Materials;
import gregtech.common.metatileentities.multi.electric.generator.MetaTileEntityDieselEngine;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.*;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tj.TJConfig;
import tj.builder.multicontrollers.MultiblockDisplayBuilder;
import tj.builder.multicontrollers.MultiblockDisplaysUtility;
import tj.builder.multicontrollers.UIDisplayBuilder;
import tj.capability.IProgressBar;
import tj.capability.ProgressBar;
import tj.capability.impl.workable.TJBoostableFuelRecipeLogic;
import tj.capability.impl.workable.TJCycleFuelRecipeLogic;
import tj.util.TJFluidUtils;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Queue;
import java.util.function.UnaryOperator;

@Mixin(value = MetaTileEntityDieselEngine.class, remap = false)
public abstract class MetaTileEntityDieselEngineMixin extends FueledMultiblockControllerMixin implements IProgressBar {

    @Unique
    private FluidStack booster;

    @Unique
    private FluidStack reagent;

    public MetaTileEntityDieselEngineMixin(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Inject(method = "createWorkable", at = @At("HEAD"), cancellable = true)
    private void injectCreateWorkable(long maxVoltage, CallbackInfoReturnable<FuelRecipeLogic> cir) {
        if (TJConfig.machines.generatorWorkableHandlerOverrides) {
            this.booster = Materials.Oxygen.getFluid(20);
            this.reagent = Materials.Lubricant.getFluid(100);
            MetaTileEntityDieselEngine tileEntity = (MetaTileEntityDieselEngine) (Object) this;
            cir.setReturnValue(new TJCycleFuelRecipeLogic(tileEntity, this.recipeMap, this::getEnergyContainer, this::getImportFluidHandler, this::getBooster, this::getReagent, this::getFuelMultiplier, this::getEUMultiplier, maxVoltage * 4));
        }
    }

    @Inject(method = "addDisplayText", at = @At("HEAD"), cancellable = true)
    private void injectAddDisplayText(List<ITextComponent> textList, CallbackInfo ci) {
        if (TJConfig.machines.generatorWorkableHandlerOverrides) {
            if (!this.isStructureFormed()) {
                MultiblockDisplaysUtility.isInvalid(textList, this.isStructureFormed());
                ci.cancel();
                return;
            }
            TJCycleFuelRecipeLogic workableHandler = (TJCycleFuelRecipeLogic) this.workableHandler;
            FluidStack fuelStack = workableHandler.getFuelStack();
            FluidStack booster = importFluidHandler.drain(this.booster, false);
            FluidStack fuelConsumed = fuelStack == null ? null : fuelStack.copy();
            if (fuelConsumed != null)
                fuelConsumed.amount = (int) workableHandler.getConsumption();
            fuelConsumed = importFluidHandler.drain(fuelConsumed, false);
            boolean isBoosted = workableHandler.isBoosted();
            int boosterAmount = booster == null ? 0 : booster.amount;
            int fuelAmount = fuelStack == null ? 0 : fuelStack.amount;
            MultiblockDisplayBuilder.start(textList)
                    .fluidInput(fuelConsumed != null && fuelConsumed.amount == workableHandler.getConsumption(), fuelConsumed, workableHandler.getMaxProgress())
                    .custom(text -> {
                        if (fuelStack == null)
                            text.add(new TextComponentTranslation("gregtech.multiblock.large_rocket_engine.no_fuel").setStyle(new Style().setColor(TextFormatting.RED)));
                        else text.add(new TextComponentString(I18n.translateToLocalFormatted("tj.multiblock.fuel_amount", fuelAmount, fuelStack.getLocalizedName())));

                        if (isBoosted) {
                            text.add(new TextComponentTranslation("gregtech.multiblock.large_rocket_engine.boost").setStyle(new Style().setColor(TextFormatting.GREEN)));
                            if (booster != null)
                                text.add(new TextComponentString(String.format("%s: %dmb", booster.getLocalizedName(), boosterAmount)).setStyle(new Style().setColor(TextFormatting.AQUA)));
                        }
                        text.add(new TextComponentString(I18n.translateToLocalFormatted("tj.multiblock.extreme_turbine.energy", workableHandler.getProduction())));
                        text.add(new TextComponentString(I18n.translateToLocalFormatted("tj.multiblock.large_combustion_engine.cycles", 20 - workableHandler.getCurrentCycle())));
                    }).isWorking(workableHandler.isWorkingEnabled(), workableHandler.isActive(), workableHandler.getProgress(), workableHandler.getMaxProgress());
            ci.cancel();
        }
    }

    @Override
    protected void configureDisplayText(UIDisplayBuilder builder) {
        super.configureDisplayText(builder);
        if (!this.isStructureFormed()) return;
        TJCycleFuelRecipeLogic workableHandler = (TJCycleFuelRecipeLogic) this.workableHandler;
        FluidStack fuelStack = workableHandler.getFuelStack();
        FluidStack booster = this.importFluidHandler.drain(this.booster, false);
        FluidStack fuelConsumed = fuelStack == null ? null : fuelStack.copy();
        if (fuelConsumed != null)
            fuelConsumed.amount = (int) workableHandler.getConsumption();
        boolean isBoosted = workableHandler.isBoosted();
        int boosterAmount = booster == null ? 0 : booster.amount;
        int fuelAmount = fuelStack == null ? 0 : fuelStack.amount;
        builder.fluidInputLine(this.importFluidHandler, fuelConsumed, workableHandler.getMaxProgress())
                .customLine(text -> {
                    if (fuelStack == null)
                        text.addTextComponent(new TextComponentTranslation("gregtech.multiblock.large_rocket_engine.no_fuel").setStyle(new Style().setColor(TextFormatting.RED)));
                    else text.addTextComponent(new TextComponentString(I18n.translateToLocalFormatted("tj.multiblock.fuel_amount", fuelAmount, fuelStack.getLocalizedName())));

                    if (isBoosted) {
                        text.addTextComponent(new TextComponentTranslation("gregtech.multiblock.large_rocket_engine.boost").setStyle(new Style().setColor(TextFormatting.GREEN)));
                        if (booster != null)
                            text.addTextComponent(new TextComponentString(String.format("%s: %dmb", booster.getLocalizedName(), boosterAmount)).setStyle(new Style().setColor(TextFormatting.AQUA)));
                    }
                    text.addTextComponent(new TextComponentString(I18n.translateToLocalFormatted("tj.multiblock.extreme_turbine.energy", workableHandler.getProduction())))
                            .addTextComponent(new TextComponentString(I18n.translateToLocalFormatted("tj.multiblock.large_combustion_engine.cycles", 20 - workableHandler.getCurrentCycle())));
                }).isWorkingLine(workableHandler.isWorkingEnabled(), workableHandler.isActive(), workableHandler.getProgress(), workableHandler.getMaxProgress());
    }


    @Override
    public int[][] getBarMatrix() {
        return new int[][]{{0}, {0, 0, 0}};
    }

    @Override
    public void getProgressBars(Queue<UnaryOperator<ProgressBar.ProgressBarBuilder>> bars) {
        TJBoostableFuelRecipeLogic workableHandler = (TJBoostableFuelRecipeLogic) this.workableHandler;
        bars.add(bar -> bar.setProgress(workableHandler::getEnergyStored).setMaxProgress(workableHandler::getEnergyCapacity)
                .setLocale("tj.multiblock.bars.energy")
                .setColor(0xFFF6FF00));
        bars.add(bar -> bar.setProgress(this::getFuelAmount).setMaxProgress(this::getFuelCapacity)
                .setLocale("tj.multiblock.bars.fuel").setParams(this::getFuelName)
                .setFluidStackSupplier(workableHandler::getFuelStack));
        bars.add(bar -> bar.setProgress(this::getReagentAmount).setMaxProgress(this::getReagentCapacity)
                .setLocale("tj.multiblock.bars.fluid").setParams(() -> new Object[]{this.getReagent().getLocalizedName()})
                .setFluidStackSupplier(this::getReagent));
        bars.add(bar -> bar.setProgress(this::getBoosterAmount).setMaxProgress(this::getBoosterCapacity)
                .setLocale("tj.multiblock.bars.booster").setParams(() -> new Object[]{this.getBooster().getLocalizedName()})
                .setFluidStackSupplier(this::getBooster));
    }

    @Unique
    private Object[] getFuelName() {
        TJBoostableFuelRecipeLogic workableHandler = (TJBoostableFuelRecipeLogic) this.workableHandler;
        return new Object[]{workableHandler.getFuelName()};
    }

    @Unique
    private long getFuelAmount() {
        TJBoostableFuelRecipeLogic workableHandler = (TJBoostableFuelRecipeLogic) this.workableHandler;
        return TJFluidUtils.getFluidAmountFromTanks(workableHandler.getFuelStack(), this.getImportFluidHandler());
    }

    @Unique
    private long getFuelCapacity() {
        TJBoostableFuelRecipeLogic workableHandler = (TJBoostableFuelRecipeLogic) this.workableHandler;
        return TJFluidUtils.getFluidCapacityFromTanks(workableHandler.getFuelStack(), this.getImportFluidHandler());
    }

    @Unique
    private long getReagentAmount() {
        return TJFluidUtils.getFluidAmountFromTanks(this.getReagent(), this.getImportFluidHandler());
    }

    @Unique
    private long getReagentCapacity() {
        return TJFluidUtils.getFluidCapacityFromTanks(this.getReagent(), this.getImportFluidHandler());
    }

    @Unique
    private long getBoosterAmount() {
        return TJFluidUtils.getFluidAmountFromTanks(this.getBooster(), this.getImportFluidHandler());
    }

    @Unique
    private long getBoosterCapacity() {
        return TJFluidUtils.getFluidCapacityFromTanks(this.getBooster(), this.getImportFluidHandler());
    }

    @Unique
    @Nonnull
    private FluidStack getBooster() {
        return this.booster;
    }

    @Unique
    @Nonnull
    private FluidStack getReagent() {
        return this.reagent;
    }

    @Unique
    private IEnergyContainer getEnergyContainer() {
        return this.energyContainer;
    }

    @Unique
    private IMultipleTankHandler getImportFluidHandler() {
        return this.importFluidHandler;
    }

    @Unique
    private int getFuelMultiplier() {
        return 2;
    }

    @Unique
    private int getEUMultiplier() {
        return 3;
    }
}
