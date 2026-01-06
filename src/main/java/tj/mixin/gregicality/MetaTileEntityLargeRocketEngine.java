package tj.mixin.gregicality;

import gregicadditions.machines.multi.GAFueledMultiblockController;
import gregicadditions.recipes.GARecipeMaps;
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
import tj.capability.ProgressBar;
import tj.capability.impl.TJBoostableFuelRecipeLogic;
import tj.capability.IProgressBar;
import tj.gui.TJGuiTextures;

import javax.annotation.Nonnull;

import java.util.List;
import java.util.Queue;

import static gregicadditions.GAMaterials.LiquidOxygen;
import static gregtech.api.unification.material.Materials.Air;

@Mixin(value = gregicadditions.machines.multi.advance.MetaTileEntityLargeRocketEngine.class, remap = false)
public abstract class MetaTileEntityLargeRocketEngine extends GAFueledMultiblockController implements IProgressBar {

    @Unique
    private FluidStack booster;

    public MetaTileEntityLargeRocketEngine(ResourceLocation metaTileEntityId, long maxVoltage) {
        super(metaTileEntityId, GARecipeMaps.ROCKET_FUEL_RECIPES, maxVoltage);
    }

    @Override
    public int[][] getBarMatrix() {
        return new int[][]{{1, 1}, {1}, {1}};
    }

    @Override
    public void getProgressBars(Queue<ProgressBar> bars) {
        TJBoostableFuelRecipeLogic workableHandler = (TJBoostableFuelRecipeLogic) this.workableHandler;
        bars.add(new ProgressBar(workableHandler::getProgress, workableHandler::getMaxProgress, TJGuiTextures.BAR_RED, "tj.multiblock.fuel"));
        bars.add(new ProgressBar(workableHandler::getEnergyStored, workableHandler::getEnergyCapacity, TJGuiTextures.BAR_YELLOW, "tj.multiblock.fuel"));
        bars.add(new ProgressBar(workableHandler::getProgress, workableHandler::getMaxProgress, TJGuiTextures.BAR_RED, "tj.multiblock.fuel"));
        bars.add(new ProgressBar(workableHandler::getEnergyStored, workableHandler::getEnergyCapacity, TJGuiTextures.BAR_YELLOW, "tj.multiblock.fuel"));
    }

    @Inject(method = "createWorkable", at = @At("HEAD"), cancellable = true)
    private void injectCreateWorkable(long maxVoltage, CallbackInfoReturnable<FuelRecipeLogic> cir) {
        if (TJConfig.machines.generatorWorkableHandlerOverrides) {
            gregicadditions.machines.multi.advance.MetaTileEntityLargeRocketEngine tileEntity = (gregicadditions.machines.multi.advance.MetaTileEntityLargeRocketEngine) (Object) this;
            cir.setReturnValue(new TJBoostableFuelRecipeLogic(tileEntity, this.recipeMap, this::getEnergyContainer, this::getImportFluidHandler, this::getBooster, this::getFuelMultiplier, this::getEUMultiplier, 655360) {
                @Override
                protected boolean checkRecipe(FuelRecipe recipe) {
                    int amount = recipe.getRecipeFluid().amount * getVoltageMultiplier(this.getMaxVoltage(), recipe.getMinVoltage());
                    booster = LiquidOxygen.getFluid(4 * (int) Math.ceil(amount / 10.0));
                    FluidStack airStack = this.fluidTank.get().drain(Air.getFluid(37500), true);
                    return airStack != null && airStack.amount == 37500;
                }
            });
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
