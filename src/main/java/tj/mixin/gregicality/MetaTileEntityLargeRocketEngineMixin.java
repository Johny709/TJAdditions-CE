package tj.mixin.gregicality;

import gregicadditions.machines.multi.advance.MetaTileEntityLargeRocketEngine;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.FuelRecipeLogic;
import gregtech.api.recipes.recipes.FuelRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.*;
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
import tj.capability.ProgressBar;
import tj.capability.impl.workable.TJBoostableFuelRecipeLogic;
import tj.capability.IProgressBar;
import tj.util.TJFluidUtils;

import javax.annotation.Nonnull;

import java.util.List;
import java.util.Queue;
import java.util.function.UnaryOperator;

import static gregicadditions.GAMaterials.LiquidOxygen;
import static gregtech.api.unification.material.Materials.Air;

@Mixin(value = MetaTileEntityLargeRocketEngine.class, remap = false)
public abstract class MetaTileEntityLargeRocketEngineMixin extends GAFueledMultiblockControllerMixin implements IProgressBar {

    @Unique
    private FluidStack air;

    @Unique
    private FluidStack booster;

    public MetaTileEntityLargeRocketEngineMixin(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Inject(method = "createWorkable", at = @At("HEAD"), cancellable = true)
    private void injectCreateWorkable(long maxVoltage, CallbackInfoReturnable<FuelRecipeLogic> cir) {
        if (TJConfig.machines.generatorWorkableHandlerOverrides) {
            this.air = Air.getFluid(37500);
            MetaTileEntityLargeRocketEngine tileEntity = (MetaTileEntityLargeRocketEngine) (Object) this;
            cir.setReturnValue(new TJBoostableFuelRecipeLogic(tileEntity, this.recipeMap, this::getEnergyContainer, this::getImportFluidHandler, this::getBooster, this::getFuelMultiplier, this::getEUMultiplier, 655360) {
                private FluidStack airStack;

                @Override
                protected boolean checkRecipe(FuelRecipe recipe) {
                    int amount = recipe.getRecipeFluid().amount * getVoltageMultiplier(this.getMaxVoltage(), recipe.getMinVoltage());
                    booster = LiquidOxygen.getFluid(4 * (int) Math.ceil(amount / 10.0));
                    this.airStack = this.fluidTank.get().drain(air, true);
                    return this.airStack != null && this.airStack.amount == air.amount;
                }

                @Override
                protected long startRecipe(FuelRecipe currentRecipe, int fuelAmountUsed, int recipeDuration) {
                    this.fluidInputs.add(this.airStack);
                    return super.startRecipe(currentRecipe, fuelAmountUsed, recipeDuration);
                }
            });
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
            TJBoostableFuelRecipeLogic workableHandler = (TJBoostableFuelRecipeLogic) this.workableHandler;
            FluidStack fuelStack = workableHandler.getFuelStack();
            FluidStack booster = importFluidHandler.drain(this.getBooster(), false);
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
                        else text.add(new TextComponentString(net.minecraft.util.text.translation.I18n.translateToLocalFormatted("tj.multiblock.fuel_amount", fuelAmount, fuelStack.getLocalizedName())));

                        if (isBoosted) {
                            text.add(new TextComponentTranslation("gregtech.multiblock.large_rocket_engine.boost").setStyle(new Style().setColor(TextFormatting.GREEN)));
                            if (booster != null)
                                text.add(new TextComponentString(String.format("%s: %dmb", booster.getLocalizedName(), boosterAmount)).setStyle(new Style().setColor(TextFormatting.AQUA)));
                        }
                        text.add(new TextComponentString(net.minecraft.util.text.translation.I18n.translateToLocalFormatted("tj.multiblock.extreme_turbine.energy", workableHandler.getProduction())));
                    }).isWorking(workableHandler.isWorkingEnabled(), workableHandler.isActive(), workableHandler.getProgress(), workableHandler.getMaxProgress());
            ci.cancel();
        }
    }

    @Override
    protected void configureDisplayText(UIDisplayBuilder builder) {
        super.configureDisplayText(builder);
        if (!this.isStructureFormed()) return;
        TJBoostableFuelRecipeLogic workableHandler = (TJBoostableFuelRecipeLogic) this.workableHandler;
        FluidStack fuelStack = workableHandler.getFuelStack();
        FluidStack booster = this.importFluidHandler.drain(this.getBooster(), false);
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
                    else text.addTextComponent(new TextComponentString(net.minecraft.util.text.translation.I18n.translateToLocalFormatted("tj.multiblock.fuel_amount", fuelAmount, fuelStack.getLocalizedName())));

                    if (isBoosted) {
                        text.addTextComponent(new TextComponentTranslation("gregtech.multiblock.large_rocket_engine.boost").setStyle(new Style().setColor(TextFormatting.GREEN)));
                        if (booster != null)
                            text.addTextComponent(new TextComponentString(String.format("%s: %dmb", booster.getLocalizedName(), boosterAmount)).setStyle(new Style().setColor(TextFormatting.AQUA)));
                    }
                    text.addTextComponent(new TextComponentString(net.minecraft.util.text.translation.I18n.translateToLocalFormatted("tj.multiblock.extreme_turbine.energy", workableHandler.getProduction())));
                }).isWorkingLine(workableHandler.isWorkingEnabled(), workableHandler.isActive(), workableHandler.getProgress(), workableHandler.getMaxProgress())
                .addRecipeInputLine(workableHandler)
                .addRecipeOutputLine(workableHandler);
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
        bars.add(bar -> bar.setProgress(this::getAirAmount).setMaxProgress(this::getAirCapacity)
                .setLocale("tj.multiblock.bars.fluid").setParams(() -> new Object[]{this.getAir() != null ? this.getAir().getLocalizedName() : ""})
                .setFluidStackSupplier(this::getAir));
        bars.add(bar -> bar.setProgress(this::getBoosterAmount).setMaxProgress(this::getBoosterCapacity)
                .setLocale("tj.multiblock.bars.booster").setParams(() -> new Object[]{this.getBooster() != null ? this.getBooster().getLocalizedName() : ""})
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
    private long getBoosterAmount() {
        return TJFluidUtils.getFluidAmountFromTanks(this.getBooster(), this.getImportFluidHandler());
    }

    @Unique
    private long getBoosterCapacity() {
        return TJFluidUtils.getFluidCapacityFromTanks(this.getBooster(), this.getImportFluidHandler());
    }

    @Unique
    private long getAirAmount() {
        return TJFluidUtils.getFluidAmountFromTanks(this.air, this.getImportFluidHandler());
    }

    @Unique
    private long getAirCapacity() {
        return TJFluidUtils.getFluidCapacityFromTanks(this.air, this.getImportFluidHandler());
    }

    @Unique
    private FluidStack getAir() {
        return this.air;
    }

    @Unique
    @Nonnull
    private FluidStack getBooster() {
        return this.booster;
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
