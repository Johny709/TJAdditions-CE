package tj.mixin.gregicality;

import gregicadditions.GAUtility;
import gregicadditions.GAValues;
import gregicadditions.capabilities.GAEnergyContainerHandler;
import gregicadditions.item.fusion.GAFusionCasing;
import gregicadditions.machines.multi.advance.MetaTileEntityAdvFusionReactor;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.impl.EnergyContainerHandler;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.multiblock.BlockWorldState;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.api.multiblock.PatternMatchContext;
import gregtech.api.recipes.RecipeMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import tj.blocks.AdvEnergyPortCasings;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@Mixin(value = MetaTileEntityAdvFusionReactor.class, remap = false)
public abstract class MetaTileEntityAdvFusionReactorMixin extends RecipeMapMultiblockController implements IMetaTileEntityAdvFusionReactorMixin {

    @Shadow
    private EnergyContainerList inputEnergyContainers;

    @Shadow public abstract void invalidateStructure();

    public MetaTileEntityAdvFusionReactorMixin(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap) {
        super(metaTileEntityId, recipeMap);
    }

    @Redirect(method = "createStructurePattern", at = @At(value = "INVOKE", target = "Lgregtech/api/multiblock/FactoryBlockPattern;where(CLjava/util/function/Predicate;)Lgregtech/api/multiblock/FactoryBlockPattern;", ordinal = 4))
    private FactoryBlockPattern redirectCreateStructurePattern_divertor(FactoryBlockPattern pattern, char symbol, Predicate<BlockWorldState> blockMatcher) {
        pattern.where(symbol, blockMatcher.or(energyPortPredicate()));
        return pattern;
    }

    @Redirect(method = "createStructurePattern", at = @At(value = "INVOKE", target = "Lgregtech/api/multiblock/FactoryBlockPattern;where(CLjava/util/function/Predicate;)Lgregtech/api/multiblock/FactoryBlockPattern;", ordinal = 5))
    private FactoryBlockPattern redirectCreateStructurePattern_vacuum(FactoryBlockPattern pattern, char symbol, Predicate<BlockWorldState> blockMatcher) {
        pattern.where(symbol, blockMatcher.or(energyPortPredicate()));
        return pattern;
    }

    @Redirect(method = "createStructurePattern", at = @At(value = "INVOKE", target = "Lgregtech/api/multiblock/FactoryBlockPattern;where(CLjava/util/function/Predicate;)Lgregtech/api/multiblock/FactoryBlockPattern;", ordinal = 6))
    private FactoryBlockPattern redirectCreateStructurePattern_cryostat(FactoryBlockPattern pattern, char symbol, Predicate<BlockWorldState> blockMatcher) {
        pattern.where(symbol, blockMatcher.or(energyPortPredicate()));
        return pattern;
    }

    @Redirect(method = "createStructurePattern", at = @At(value = "INVOKE", target = "Lgregtech/api/multiblock/FactoryBlockPattern;where(CLjava/util/function/Predicate;)Lgregtech/api/multiblock/FactoryBlockPattern;", ordinal = 7))
    private FactoryBlockPattern redirectCreateStructurePattern_blanket(FactoryBlockPattern pattern, char symbol, Predicate<BlockWorldState> blockMatcher) {
        pattern.where(symbol, blockMatcher.or(energyPortPredicate()));
        return pattern;
    }

    @Inject(method = "formStructure", at = @At(value = "INVOKE", target = "Lgregicadditions/machines/multi/advance/MetaTileEntityAdvFusionReactor;initializeAbilities()V"),
        locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
    private void injectFormStructure(PatternMatchContext context, CallbackInfo ci, GAFusionCasing.CasingType coil, int cryostatTier, long energyStored) {
        inputInventory = new ItemHandlerList(getAbilities(MultiblockAbility.IMPORT_ITEMS));
        inputFluidInventory = new FluidTankList(true, getAbilities(MultiblockAbility.IMPORT_FLUIDS));
        outputInventory = new ItemHandlerList(getAbilities(MultiblockAbility.EXPORT_ITEMS));
        outputFluidInventory = new FluidTankList(true, getAbilities(MultiblockAbility.EXPORT_FLUIDS));
        List<IEnergyContainer> energyInputs = getAbilities(MultiblockAbility.INPUT_ENERGY);
        List<AdvEnergyPortCasings.AbilityType> energyPorts = context.getOrDefault("EnergyPort", new ArrayList<>());
        inputEnergyContainers = new EnergyContainerList(energyInputs);
        long euCapacity = 0, count = 0;
        for (IEnergyContainer energyContainer : energyInputs) {
            euCapacity += 10000000L * (long) Math.pow(2, Math.min(GAUtility.getTierByVoltage(energyContainer.getInputVoltage()), getTier()) - GAValues.LuV);
            count++;
        }
        for (AdvEnergyPortCasings.AbilityType energyPort : energyPorts) {
            euCapacity += 10000000L * (long) Math.pow(2, Math.min(energyPort.getTier(), getTier()) - GAValues.LuV);
            count++;
        }
        if (count > 16) {
            invalidateStructure();
            ci.cancel();
            return;
        }
        this.energyContainer = new GAEnergyContainerHandler(this, euCapacity, GAValues.V[getTier()], 0, 0, 0) {
            @Override
            public String getName() {
                return "EnergyContainerInternal";
            }
        };
        ((EnergyContainerHandler) energyContainer).setEnergyStored(energyStored);
        ci.cancel();
    }

    @Unique
    public Predicate<BlockWorldState> energyPortPredicate() {
        return (blockWorldState) -> {
            IBlockState blockState = blockWorldState.getBlockState();
            if (blockState.getBlock() instanceof AdvEnergyPortCasings) {
                AdvEnergyPortCasings abilityCasings = (AdvEnergyPortCasings) blockState.getBlock();
                AdvEnergyPortCasings.AbilityType tieredCasingType = abilityCasings.getState(blockState);
                List<AdvEnergyPortCasings.AbilityType> currentCasing = blockWorldState.getMatchContext().getOrCreate("EnergyPort", ArrayList::new);
                currentCasing.add(tieredCasingType);
                return currentCasing.get(0).getName().equals(tieredCasingType.getName());
            }
            return false;
        };
    }
}
