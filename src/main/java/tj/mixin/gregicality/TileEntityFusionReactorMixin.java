package tj.mixin.gregicality;

import gregicadditions.GAValues;
import gregicadditions.machines.multi.TileEntityFusionReactor;
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
import tj.blocks.EnergyPortCasings;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@Mixin(value = TileEntityFusionReactor.class, remap = false)
public abstract class TileEntityFusionReactorMixin extends RecipeMapMultiblockController implements ITileEntityFusionReactorMixin {

    @Shadow
    private EnergyContainerList inputEnergyContainers;

    public TileEntityFusionReactorMixin(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap) {
        super(metaTileEntityId, recipeMap);
    }

    @Redirect(method = "createStructurePattern", at = @At(value = "INVOKE", target = "Lgregtech/api/multiblock/FactoryBlockPattern;where(CLjava/util/function/Predicate;)Lgregtech/api/multiblock/FactoryBlockPattern;", ordinal = 4))
    private FactoryBlockPattern redirectCreateStructurePattern(FactoryBlockPattern pattern, char symbol, Predicate<BlockWorldState> blockMatcher) {
        pattern.where(symbol, blockMatcher.or(energyPortPredicate(getTier())));
        return pattern;
    }

    @Inject(method = "formStructure", at = @At(value = "INVOKE", target = "Lgregicadditions/machines/multi/TileEntityFusionReactor;initializeAbilities()V"),
            locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
    private void injectFormStructure(PatternMatchContext context, CallbackInfo ci, long energyStored) {
        int energyPorts = context.getOrDefault("EnergyPort", new ArrayList<>()).size();
        inputInventory = new ItemHandlerList(getAbilities(MultiblockAbility.IMPORT_ITEMS));
        inputFluidInventory = new FluidTankList(true, getAbilities(MultiblockAbility.IMPORT_FLUIDS));
        outputInventory = new ItemHandlerList(getAbilities(MultiblockAbility.EXPORT_ITEMS));
        outputFluidInventory = new FluidTankList(true, getAbilities(MultiblockAbility.EXPORT_FLUIDS));
        List<IEnergyContainer> energyInputs = getAbilities(MultiblockAbility.INPUT_ENERGY);
        inputEnergyContainers = new EnergyContainerList(energyInputs);
        long euCapacity = (energyInputs.size() + energyPorts) * 10000000L * (long) Math.pow(2, getTier() - 6);
        energyContainer = new EnergyContainerHandler(this, euCapacity, GAValues.V[getTier()], 0, 0, 0) {
            @Override
            public String getName() {
                return "EnergyContainerInternal";
            }
        };
        ((EnergyContainerHandler) energyContainer).setEnergyStored(energyStored);
        ci.cancel();
    }

    @Unique
    public Predicate<BlockWorldState> energyPortPredicate(int tier) {
        return (blockWorldState) -> {
            IBlockState blockState = blockWorldState.getBlockState();
            if (blockState.getBlock() instanceof EnergyPortCasings) {
                EnergyPortCasings abilityCasings = (EnergyPortCasings) blockState.getBlock();
                EnergyPortCasings.AbilityType tieredCasingType = abilityCasings.getState(blockState);
                List<EnergyPortCasings.AbilityType> currentCasing = blockWorldState.getMatchContext().getOrCreate("EnergyPort", ArrayList::new);
                currentCasing.add(tieredCasingType);
                return currentCasing.get(0).getName().equals(tieredCasingType.getName()) && currentCasing.get(0).getTier() >= tier;
            }
            return false;
        };
    }
}
