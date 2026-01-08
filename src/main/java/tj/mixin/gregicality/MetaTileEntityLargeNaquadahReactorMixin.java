package tj.mixin.gregicality;

import gregicadditions.GAValues;
import gregicadditions.machines.multi.GAFueledMultiblockController;
import gregicadditions.machines.multi.advance.MetaTileEntityLargeNaquadahReactor;
import gregicadditions.recipes.GARecipeMaps;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.FuelRecipeLogic;
import gregtech.api.unification.material.Materials;
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
import tj.capability.IProgressBar;
import tj.capability.ProgressBar;
import tj.capability.impl.TJBoostableFuelRecipeLogic;
import tj.capability.impl.TJCycleFuelRecipeLogic;
import tj.util.TJFluidUtils;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Queue;

@Mixin(value = MetaTileEntityLargeNaquadahReactor.class, remap = false)
public abstract class MetaTileEntityLargeNaquadahReactorMixin extends GAFueledMultiblockController implements IProgressBar {

    @Unique
    private FluidStack booster;

    @Unique
    private FluidStack reagent;

    public MetaTileEntityLargeNaquadahReactorMixin(ResourceLocation metaTileEntityId, long maxVoltage) {
        super(metaTileEntityId, GARecipeMaps.NAQUADAH_REACTOR_FUELS, maxVoltage);
    }

    @Inject(method = "createWorkable", at = @At("HEAD"), cancellable = true)
    private void injectCreateWorkable(long maxVoltage, CallbackInfoReturnable<FuelRecipeLogic> cir) {
        if (TJConfig.machines.generatorWorkableHandlerOverrides) {
            this.booster = Materials.Oxygen.getPlasma(50);
            this.reagent = Materials.Tritium.getFluid(1000);
            MetaTileEntityLargeNaquadahReactor tileEntity = (MetaTileEntityLargeNaquadahReactor) (Object) this;
            cir.setReturnValue(new TJCycleFuelRecipeLogic(tileEntity, this.recipeMap, this::getEnergyContainer, this::getImportFluidHandler, this::getBooster, this::getReagent, this::getFuelMultiplier, this::getEUMultiplier, GAValues.V[10]));
        }
    }

    @Inject(method = "addDisplayText", at = @At("HEAD"), cancellable = true)
    private void injectAddDisplayText(List<ITextComponent> textList, CallbackInfo ci) {
        if (TJConfig.machines.generatorWorkableHandlerOverrides) {
            if (!this.isStructureFormed()) {
                super.addDisplayText(textList);
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
    public int[][] getBarMatrix() {
        return new int[][]{{0}, {0, 0, 0}};
    }

    @Override
    public void getProgressBars(Queue<ProgressBar> bars, ProgressBar.ProgressBarBuilder barBuilder) {
        TJBoostableFuelRecipeLogic workableHandler = (TJBoostableFuelRecipeLogic) this.workableHandler;
        bars.add(barBuilder.setProgress(workableHandler::getEnergyStored).setMaxProgress(workableHandler::getEnergyCapacity)
                .setLocale("tj.multiblock.bars.energy")
                .setColor(0xFFF6FF00)
                .build());
        bars.add(barBuilder.setProgress(this::getFuelAmount).setMaxProgress(this::getFuelCapacity)
                .setLocale("tj.multiblock.bars.fuel").setParams(this::getFuelName)
                .setFluidStackSupplier(workableHandler::getFuelStack)
                .build());
        bars.add(barBuilder.setProgress(this::getReagentAmount).setMaxProgress(this::getReagentCapacity)
                .setLocale("tj.multiblock.bars.fluid").setParams(() -> new Object[]{this.getReagent().getLocalizedName()})
                .setFluidStackSupplier(this::getReagent)
                .build());
        bars.add(barBuilder.setProgress(this::getBoosterAmount).setMaxProgress(this::getBoosterCapacity)
                .setLocale("tj.multiblock.bars.booster").setParams(() -> new Object[]{this.getBooster().getLocalizedName()})
                .setFluidStackSupplier(this::getBooster)
                .build());
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
