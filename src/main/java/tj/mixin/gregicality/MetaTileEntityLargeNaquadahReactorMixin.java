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
import tj.capability.impl.TJCycleFuelRecipeLogic;

import javax.annotation.Nonnull;
import java.util.List;

@Mixin(value = MetaTileEntityLargeNaquadahReactor.class, remap = false)
public abstract class MetaTileEntityLargeNaquadahReactorMixin extends GAFueledMultiblockController {

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
