package tj.machines.multi.steam;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import tj.TJRecipeMaps;
import tj.builder.multicontrollers.TJLargeSimpleRecipeMapMultiblockControllerBase;
import gregicadditions.utils.GALog;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.api.recipes.CountableIngredient;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.render.ICubeRenderer;
import gregtech.api.render.OrientedOverlayRenderer;
import gregtech.api.render.Textures;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

public class MetaTileEntityMegaCokeOven extends TJLargeSimpleRecipeMapMultiblockControllerBase {

    private static final MultiblockAbility<?>[] ALLOWED_ABILITIES = new MultiblockAbility[]{MultiblockAbility.EXPORT_FLUIDS, MultiblockAbility.EXPORT_ITEMS, MultiblockAbility.IMPORT_ITEMS};

    public MetaTileEntityMegaCokeOven (ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, TJRecipeMaps.COKE_OVEN_RECIPES, 0, 100, 100, 512, false, false, false);
        this.recipeMapWorkable = new MegaCokeOvenRecipeLogic(this, EUtPercentage, durationPercentage, chancePercentage, stack);
        maintenance_problems = 0b111111;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityMegaCokeOven(this.metaTileEntityId);/*(3)!*/
    }

    @Override
    protected boolean checkStructureComponents(List<IMultiblockPart> parts, Map<MultiblockAbility<Object>, List<Object>> abilities) {
        //basically check minimal requirements for inputs count
        //noinspection SuspiciousMethodCalls
        int itemInputsCount = abilities.getOrDefault(MultiblockAbility.IMPORT_ITEMS, Collections.emptyList())
                .stream().map(it -> (IItemHandler) it).mapToInt(IItemHandler::getSlots).sum();
        //noinspection SuspiciousMethodCalls
        int fluidInputsCount = abilities.getOrDefault(MultiblockAbility.IMPORT_FLUIDS, Collections.emptyList()).size();
        //noinspection SuspiciousMethodCalls
        return itemInputsCount >= recipeMap.getMinInputs() &&
                fluidInputsCount >= recipeMap.getMinFluidInputs();
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start() /*(4)!*/
                .aisle("FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF")
                .aisle("FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF")
                .aisle("FFFFFFFFF", "CCCCCCCCC", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF")
                .aisle("FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF")
                .aisle("FFFFFFFFF", "CCCCCCCCC", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF")
                .aisle("FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF")
                .aisle("FFFFFFFFF", "CCCCCCCCC", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF")
                .aisle("FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF")
                .aisle("FFFFFFFFF", "FFFFSFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF")
                .setAmountAtLeast('L', 100)
                .where('S', selfPredicate())
                .where('L', statePredicate(this.getCasingState()))
                .where('F', statePredicate(getCasingState()).or(abilityPartPredicate(ALLOWED_ABILITIES)))
                .where('C', statePredicate(getCasingState()))
                .where('#', isAirPredicate())
                .build();
    }

    protected IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.COKE_BRICKS);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.COKE_BRICKS;
    }

    @Nonnull
    @Override
    protected OrientedOverlayRenderer getFrontOverlay() {
        return Textures.PYROLYSE_OVEN_OVERLAY;
    }







    private class MegaCokeOvenRecipeLogic extends LargeSimpleMultiblockRecipeLogic {

        public MegaCokeOvenRecipeLogic(RecipeMapMultiblockController tileEntity, int EUtPercentage, int durationPercentage, int chancePercentage, int stack) {
            super(tileEntity, EUtPercentage, durationPercentage, chancePercentage, stack);
        }

        @Override
        protected Recipe createRecipe(long maxVoltage, IItemHandlerModifiable inputs, IMultipleTankHandler fluidInputs, Recipe matchingRecipe) {
            int maxItemsLimit = stack;
            int EUt;
            int duration;
            int minMultiplier = Integer.MAX_VALUE;

            Set<ItemStack> countIngredients = new HashSet<>();
            if (!matchingRecipe.getInputs().isEmpty()) {
                this.findIngredients(countIngredients, inputs);
                minMultiplier = Math.min(maxItemsLimit, this.getMinRatioItem(countIngredients, matchingRecipe, maxItemsLimit));
            }

            Object2IntMap<String> countFluid = new Object2IntOpenHashMap<>();
            if (!matchingRecipe.getFluidInputs().isEmpty()) {

                this.findFluid(countFluid, fluidInputs);
                minMultiplier = Math.min(minMultiplier, this.getMinRatioFluid(countFluid, matchingRecipe, maxItemsLimit));
            }

            if (minMultiplier == Integer.MAX_VALUE) {
                GALog.logger.error("Cannot calculate ratio of items for large multiblocks");
                return null;
            }

            EUt = matchingRecipe.getEUt();
            duration = matchingRecipe.getDuration();

            int attemptItemsLimit = stack;
            attemptItemsLimit = Math.max(1, attemptItemsLimit);
            attemptItemsLimit = Math.min(minMultiplier, attemptItemsLimit);
            List<CountableIngredient> newRecipeInputs = new ArrayList<>();
            List<FluidStack> newFluidInputs = new ArrayList<>();
            List<ItemStack> outputI = new ArrayList<>();
            List<FluidStack> outputF = new ArrayList<>();
            this.multiplyInputsAndOutputs(newRecipeInputs, newFluidInputs, outputI, outputF, matchingRecipe, attemptItemsLimit);


            RecipeBuilder<?> newRecipe = recipeMap.recipeBuilder();
            copyChancedItemOutputs(newRecipe, matchingRecipe, attemptItemsLimit);

            // determine if there is enough room in the output to fit all of this
            // if there isn't, we can't process this recipe.
            List<ItemStack> totalOutputs = newRecipe.getChancedOutputs().stream().map(Recipe.ChanceEntry::getItemStack).collect(Collectors.toList());
            totalOutputs.addAll(outputI);

            newRecipe.inputsIngredients(newRecipeInputs)
                    .fluidInputs(newFluidInputs)
                    .outputs(outputI)
                    .fluidOutputs(outputF)
                    .EUt(Math.max(0, EUt * EUtPercentage / 100))
                    .duration((int) Math.max(3, duration * (durationPercentage / 100.0)));
            return newRecipe.build().getResult();
        }
    }
}
