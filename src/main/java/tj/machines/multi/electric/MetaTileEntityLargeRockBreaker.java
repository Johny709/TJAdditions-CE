package tj.machines.multi.electric;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import tj.TJConfig;
import tj.TJRecipeMaps;
import tj.builder.multicontrollers.TJLargeSimpleRecipeMapMultiblockControllerBase;
import gregicadditions.GAUtility;
import gregicadditions.capabilities.GregicAdditionsCapabilities;
import gregicadditions.client.ClientHandler;
import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.components.MotorCasing;
import gregicadditions.item.components.PumpCasing;
import gregicadditions.item.metal.MetalCasing1;
import gregicadditions.item.metal.MetalCasing2;
import gregicadditions.utils.GALog;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.BlockWorldState;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.api.multiblock.PatternMatchContext;
import gregtech.api.recipes.CountableIngredient;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.render.ICubeRenderer;
import gregtech.api.render.OrientedOverlayRenderer;
import gregtech.api.render.Textures;
import gregtech.api.util.GTFluidUtils;
import gregtech.api.util.InventoryUtils;
import gregtech.common.blocks.BlockBoilerCasing;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.BlockTurbineCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;


public class MetaTileEntityLargeRockBreaker extends TJLargeSimpleRecipeMapMultiblockControllerBase {

    private int slices;
    public static final MultiblockAbility<?>[] ALLOWED_ABILITIES = {MultiblockAbility.IMPORT_ITEMS, MultiblockAbility.EXPORT_ITEMS, MultiblockAbility.INPUT_ENERGY, GregicAdditionsCapabilities.MAINTENANCE_HATCH};

    public MetaTileEntityLargeRockBreaker(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, TJRecipeMaps.ROCK_BREAKER_RECIPES, TJConfig.largeRockBreaker.eutPercentage, TJConfig.largeRockBreaker.durationPercentage, TJConfig.largeRockBreaker.chancePercentage, TJConfig.largeRockBreaker.stack);
        this.recipeMapWorkable = new LargeRockBreakerRecipeLogic(this, EUtPercentage, durationPercentage, chancePercentage, stack);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityLargeRockBreaker(this.metaTileEntityId);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("tj.multiblock.large_rock_breaker.description"));
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        super.addDisplayText(textList);
        if (this.isStructureFormed())
            textList.add(new TextComponentTranslation("gtadditions.multiblock.universal.tooltip.4", this.stack * this.slices));
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start(RIGHT, DOWN, BACK)
                .aisle("~~VVV~~", "~~VMV~~", "~~VVV~~")
                .aisle("FFVVVHH", "FfVGVhH", "FFVVVHH")
                .aisle("FFVVVHH", "P#T#T#P", "FFVVVHH").setRepeatable(1, TJConfig.largeRockBreaker.maximumSlices)
                .aisle("FFVVVHH", "FfVGVhH", "FFVVVHH")
                .aisle("~~VVV~~", "~~VSV~~", "~~VVV~~")
                .setAmountAtLeast('L', 15)
                .setAmountAtLeast('l', 6)
                .setAmountAtLeast('I', 6)
                .where('S', selfPredicate())
                .where('L', statePredicate(GAMetaBlocks.METAL_CASING_2.getState(MetalCasing2.CasingType.STABALLOY)))
                .where('l', statePredicate(GAMetaBlocks.METAL_CASING_1.getState(MetalCasing1.CasingType.GRISIUM)))
                .where('I', statePredicate(MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.INVAR_HEATPROOF)))
                .where('V', statePredicate(GAMetaBlocks.METAL_CASING_2.getState(MetalCasing2.CasingType.STABALLOY)).or(abilityPartPredicate(ALLOWED_ABILITIES)))
                .where('f', statePredicate(GAMetaBlocks.METAL_CASING_1.getState(MetalCasing1.CasingType.GRISIUM)).or(abilityPartPredicate(MultiblockAbility.IMPORT_FLUIDS)))
                .where('h', statePredicate(MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.INVAR_HEATPROOF)).or(abilityPartPredicate(MultiblockAbility.IMPORT_FLUIDS)))
                .where('F', statePredicate(GAMetaBlocks.METAL_CASING_1.getState(MetalCasing1.CasingType.GRISIUM)))
                .where('H', statePredicate(MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.INVAR_HEATPROOF)))
                .where('G', statePredicate(MetaBlocks.TURBINE_CASING.getState(BlockTurbineCasing.TurbineCasingType.TITANIUM_GEARBOX)))
                .where('T', statePredicate(MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.TITANIUM_PIPE)))
                .where('M', motorPredicate())
                .where('P', pumpPredicateList())
                .where('~', state -> true)
                .where('#', isAirPredicate())
                .build();
    }

    public Predicate<BlockWorldState> pumpPredicateList() {
        return (blockWorldState) -> {
            IBlockState blockState = blockWorldState.getBlockState();
            if (!(blockState.getBlock() instanceof PumpCasing)) {
                return false;
            } else {
                PumpCasing pumpCasing = (PumpCasing)blockState.getBlock();
                PumpCasing.CasingType tieredCasingType = pumpCasing.getState(blockState);
                PumpCasing.CasingType currentCasing = blockWorldState.getMatchContext().getOrPut("Pump", tieredCasingType);
                List<PumpCasing.CasingType> casingTypeList = blockWorldState.getMatchContext().getOrCreate("Pumps", ArrayList::new);
                casingTypeList.add(tieredCasingType);
                return currentCasing.getName().equals(tieredCasingType.getName());
            }
        };
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        MotorCasing.CasingType motor = context.getOrDefault("Motor", MotorCasing.CasingType.MOTOR_LV);
        PumpCasing.CasingType pump = context.getOrDefault("Pump", PumpCasing.CasingType.PUMP_LV);
        int min = Math.min(motor.getTier(), pump.getTier());
        this.maxVoltage = (long) (Math.pow(4, min) * 8);
        this.slices = Collections.unmodifiableList(context.getOrDefault("Pumps", Collections.emptyList())).size() / 2;
    }

    @Override
    protected void updateFormedValid() {
        if (this.slices != 0)
            super.updateFormedValid();
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.slices = 0;
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return ClientHandler.STABALLOY_CASING;
    }

    @Nonnull
    @Override
    protected OrientedOverlayRenderer getFrontOverlay() {
        return Textures.CANNER_OVERLAY;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        Textures.ROCK_CRUSHER_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
        if (this.recipeMapWorkable.isActive())
            Textures.ROCK_CRUSHER_ACTIVE_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
    }









    private class LargeRockBreakerRecipeLogic extends LargeSimpleMultiblockRecipeLogic {

        public LargeRockBreakerRecipeLogic(RecipeMapMultiblockController tileEntity, int EUtPercentage, int durationPercentage, int chancePercentage, int stack) {
            super(tileEntity, EUtPercentage, durationPercentage, chancePercentage, stack);
        }

        @Override
        public int getStack() {
            return super.getStack() * slices;
        }

        @Override
        protected Recipe createRecipe(long maxVoltage, IItemHandlerModifiable inputs, IMultipleTankHandler fluidInputs, Recipe matchingRecipe) {
            int maxItemsLimit = this.getStack();
            int EUt;
            int duration;
            int currentTier = getOverclockingTier(maxVoltage);
            int tierNeeded;
            int minMultiplier = Integer.MAX_VALUE;

            tierNeeded = Math.max(1, GAUtility.getTierByVoltage(matchingRecipe.getEUt()));
            maxItemsLimit *= currentTier - tierNeeded;
            maxItemsLimit = Math.max(1, maxItemsLimit);
            if (maxItemsLimit == 1) {
                return matchingRecipe;
            }

            Set<ItemStack> countIngredients = new HashSet<>();
            if (!matchingRecipe.getInputs().isEmpty()) {
                this.findIngredients(countIngredients, inputs);
                minMultiplier = Math.min(maxItemsLimit, this.getMinRatioItem(countIngredients, matchingRecipe, maxItemsLimit));
            }

            Map<String, Integer> countFluid = new HashMap<>();
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

            int tierDiff = currentTier - tierNeeded;
            for (int i = 0; i < tierDiff; i++) {
                int attemptItemsLimit = this.getStack();
                attemptItemsLimit *= tierDiff - i;
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
                boolean canFitOutputs = InventoryUtils.simulateItemStackMerge(totalOutputs, this.getOutputInventory());
                canFitOutputs = canFitOutputs && GTFluidUtils.simulateFluidStackMerge(outputF, this.getOutputTank());
                if (!canFitOutputs) {
                    continue;
                }

                newRecipe.inputsIngredients(newRecipeInputs)
                        .fluidInputs(newFluidInputs)
                        .outputs(outputI)
                        .fluidOutputs(outputF)
                        .EUt((int) Math.min(Integer.MAX_VALUE, Math.max(1, (long) EUt * this.getEUtPercentage() / 100)))
                        .duration((int) Math.max(3, duration * (this.getDurationPercentage() / 100.0)));

                return newRecipe.build().getResult();
            }
            return matchingRecipe;
        }
    }
}
