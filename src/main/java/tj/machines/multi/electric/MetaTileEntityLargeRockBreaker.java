package tj.machines.multi.electric;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregicadditions.machines.multi.simple.LargeSimpleRecipeMapMultiblockController;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tj.TJConfig;
import tj.TJRecipeMaps;
import gregicadditions.capabilities.GregicAdditionsCapabilities;
import gregicadditions.client.ClientHandler;
import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.components.MotorCasing;
import gregicadditions.item.components.PumpCasing;
import gregicadditions.item.metal.MetalCasing1;
import gregicadditions.item.metal.MetalCasing2;
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
import gregtech.common.blocks.BlockBoilerCasing;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.BlockTurbineCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import tj.builder.multicontrollers.TJRecipeMapMultiblockController;
import tj.builder.multicontrollers.GUIDisplayBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;

import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;


public class MetaTileEntityLargeRockBreaker extends TJRecipeMapMultiblockController {

    public static final MultiblockAbility<?>[] ALLOWED_ABILITIES = {MultiblockAbility.IMPORT_ITEMS, MultiblockAbility.EXPORT_ITEMS, MultiblockAbility.INPUT_ENERGY, GregicAdditionsCapabilities.MAINTENANCE_HATCH};
    private int slices;

    public MetaTileEntityLargeRockBreaker(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, TJRecipeMaps.ROCK_BREAKER_RECIPES);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityLargeRockBreaker(this.metaTileEntityId);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("tj.multiblock.large_rock_breaker.description"));
    }

    @Override
    protected void addDisplayText(GUIDisplayBuilder builder) {
        super.addDisplayText(builder);
        if (this.isStructureFormed())
            builder.addTextComponent(new TextComponentTranslation("gtadditions.multiblock.universal.tooltip.4", this.getParallel()));
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
                .where('M', LargeSimpleRecipeMapMultiblockController.motorPredicate())
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
        this.tier = Math.min(motor.getTier(), pump.getTier());
        this.maxVoltage = 8L << this.tier * 2;
        this.slices = context.getOrDefault("Pumps", new ArrayList<>()).size() / 2;
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
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        Textures.ROCK_CRUSHER_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
        if (this.recipeLogic.isActive())
            Textures.ROCK_CRUSHER_ACTIVE_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
    }

    @Override
    public int getEUtMultiplier() {
        return TJConfig.largeRockBreaker.eutPercentage;
    }

    @Override
    public int getDurationMultiplier() {
        return TJConfig.largeRockBreaker.durationPercentage;
    }

    @Override
    public int getChanceMultiplier() {
        return TJConfig.largeRockBreaker.chancePercentage;
    }

    @Override
    public int getParallel() {
        return this.slices;
    }
}
