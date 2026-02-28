package tj.machines.multi.electric;

import gregicadditions.capabilities.GregicAdditionsCapabilities;
import gregicadditions.client.ClientHandler;
import gregicadditions.item.GATransparentCasing;
import gregicadditions.item.components.PumpCasing;
import gregicadditions.machines.multi.simple.LargeSimpleRecipeMapMultiblockController;
import gregicadditions.recipes.GARecipeMaps;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.BlockWorldState;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.api.multiblock.PatternMatchContext;
import gregtech.api.render.ICubeRenderer;
import gregtech.api.render.OrientedOverlayRenderer;
import gregtech.api.render.Textures;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tj.TJConfig;
import tj.TJRecipeMaps;
import tj.TJValues;
import tj.builder.multicontrollers.TJMultiRecipeMapMultiblockController;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

public class MetaTileEntityLargeGreenhouse extends TJMultiRecipeMapMultiblockController {

    private static final MultiblockAbility<?>[] ALLOWED_ABILITIES = {MultiblockAbility.IMPORT_ITEMS, MultiblockAbility.EXPORT_ITEMS, MultiblockAbility.IMPORT_FLUIDS, MultiblockAbility.INPUT_ENERGY, GregicAdditionsCapabilities.MAINTENANCE_HATCH};

    public MetaTileEntityLargeGreenhouse(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, GARecipeMaps.GREEN_HOUSE_RECIPES, TJRecipeMaps.GREENHOUSE_TREE_RECIPES);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder Holder) {
        return new MetaTileEntityLargeGreenhouse(metaTileEntityId);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("tj.mulitblock.greenhouse.treemode"));
        tooltip.add(I18n.format("gtadditions.multiblock.universal.tooltip.2", TJValues.thousandTwoPlaceFormat.format(TJConfig.largeGreenhouse.eutPercentageTree / 100.0)));
        tooltip.add(I18n.format("gtadditions.multiblock.universal.tooltip.3", TJValues.thousandTwoPlaceFormat.format(TJConfig.largeGreenhouse.durationPercentageTree / 100.0)));
        tooltip.add(I18n.format("gtadditions.multiblock.universal.tooltip.4", TJConfig.largeGreenhouse.stackTree));
        tooltip.add(I18n.format("gtadditions.multiblock.universal.tooltip.5", TJConfig.largeGreenhouse.chancePercentageTree));
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("~HHHHH~", "~HHHHH~", "~HHHHH~", "~GGGGG~", "~GGGGG~", "~GGGGG~", "~GGGGG~", "~~~~~~~")
                .aisle("HHHHHHH", "HDDDDDH", "H#####H", "G#####G", "G#####G", "G#####G", "G#####G", "~GGGGG~")
                .aisle("HHHHHHH", "HDDDDDH", "H#####H", "G#####G", "G#####G", "G#####G", "G#####G", "~GGGGG~")
                .aisle("HHHPHHH", "HDDDDDH", "H#####H", "G#####G", "G#####G", "G#####G", "G#####G", "~GGGGG~")
                .aisle("HHHHHHH", "HDDDDDH", "H#####H", "G#####G", "G#####G", "G#####G", "G#####G", "~GGGGG~")
                .aisle("HHHHHHH", "HDDDDDH", "H#####H", "G#####G", "G#####G", "G#####G", "G#####G", "~GGGGG~")
                .aisle("~HHHHH~", "~HHSHH~", "~HHHHH~", "~GGGGG~", "~GGGGG~", "~GGGGG~", "~GGGGG~", "~~~~~~~")
                .setAmountAtLeast('L', 25)
                .where('S', selfPredicate())
                .where('L', statePredicate(getCasingState()))
                .where('H', statePredicate(getCasingState()).or(abilityPartPredicate(ALLOWED_ABILITIES)))
                .where('G', glassPredicate())
                .where('P', LargeSimpleRecipeMapMultiblockController. pumpPredicate())
                .where('D', blockPredicate(Block.getBlockFromName("randomthings:fertilizeddirt")))
                .where('#', isAirPredicate())
                .where('~', (tile) -> true)
                .build();
    }

    protected IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STAINLESS_CLEAN);
    }

    public static Predicate<BlockWorldState> glassPredicate() {
        return blockWorldState -> {
            IBlockState blockState = blockWorldState.getBlockState();
            if (!(blockState.getBlock() instanceof GATransparentCasing))
                return false;
            GATransparentCasing glassCasing = (GATransparentCasing)blockState.getBlock();
            GATransparentCasing.CasingType tieredCasingType = glassCasing.getState(blockState);
            GATransparentCasing.CasingType currentCasing = blockWorldState.getMatchContext().getOrPut("Glass", tieredCasingType);
            return currentCasing.getName().equals(tieredCasingType.getName());
        };
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        this.tier = context.getOrDefault("Pump", PumpCasing.CasingType.PUMP_LV).getTier();
        this.maxVoltage = 8L << this.tier * 2;
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.CLEAN_STAINLESS_STEEL_CASING;
    }

    @Nonnull
    @Override
    protected OrientedOverlayRenderer getFrontOverlay() {
        return this.recipeMapIndex == 0 ? Textures.FERMENTER_OVERLAY : super.getFrontOverlay();
    }

    @Override
    public int getEUtMultiplier() {
        return this.recipeMapIndex == 0 ? TJConfig.largeGreenhouse.eutPercentage : TJConfig.largeGreenhouse.eutPercentageTree;
    }

    @Override
    public int getDurationMultiplier() {
        return this.recipeMapIndex == 0 ? TJConfig.largeGreenhouse.durationPercentage : TJConfig.largeGreenhouse.durationPercentageTree;
    }

    @Override
    public int getChanceMultiplier() {
        return this.recipeMapIndex == 0 ? TJConfig.largeGreenhouse.chancePercentage : TJConfig.largeGreenhouse.chancePercentageTree;
    }

    @Override
    public int getParallel() {
        return this.recipeMapIndex == 0 ? TJConfig.largeGreenhouse.stack : TJConfig.largeGreenhouse.stackTree;
    }
}
