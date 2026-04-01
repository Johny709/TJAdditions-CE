package tj.machines.multi.electric;

import gregicadditions.GAValues;
import gregicadditions.capabilities.GregicAdditionsCapabilities;
import gregicadditions.client.ClientHandler;
import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.components.PistonCasing;
import gregicadditions.item.metal.MetalCasing1;
import gregicadditions.machines.multi.simple.LargeSimpleRecipeMapMultiblockController;
import gregicadditions.recipes.GARecipeMaps;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.api.multiblock.PatternMatchContext;
import gregtech.api.render.ICubeRenderer;
import gregtech.api.render.OrientedOverlayRenderer;
import gregtech.common.blocks.BlockMultiblockCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tj.TJConfig;
import tj.builder.multicontrollers.TJRecipeMapMultiblockController;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class MetaTileEntityLargeElectricImplosionCompressor extends TJRecipeMapMultiblockController {

    private static final MultiblockAbility<?>[] ALLOWED_ABILITIES = {MultiblockAbility.IMPORT_ITEMS, MultiblockAbility.EXPORT_ITEMS, MultiblockAbility.INPUT_ENERGY, GregicAdditionsCapabilities.MAINTENANCE_HATCH};

    public MetaTileEntityLargeElectricImplosionCompressor(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, GARecipeMaps.ELECTRIC_IMPLOSION_RECIPES);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityLargeElectricImplosionCompressor(this.metaTileEntityId);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("tj.multiblock.large_electric_implosion_compressor.description"));
        super.addInformation(stack, player, tooltip, advanced);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("~C~C~", "~G~G~", "~G~G~", "~G~G~", "~G~G~", "~G~G~", "~C~C~", "XXXXX", "XXXXX", "XXXXX", "~C~C~", "~G~G~", "~G~G~", "~G~G~", "~G~G~", "~G~G~", "~C~C~")
                .aisle("CCCCC", "GCGCG", "GCGCG", "GCGCG", "GCGCG", "GCGCG", "C#C#C", "X###X", "X###X", "X###X", "C#C#C", "GCGCG", "GCGCG", "GCGCG", "GCGCG", "GCGCG", "CCCCC")
                .aisle("~CCC~", "~GPG~", "~GPG~", "~GPG~", "~GPG~", "~GPG~", "~C#C~", "X###X", "X###X", "X###X", "~C#C~", "~GPG~", "~GPG~", "~GPG~", "~GPG~", "~GPG~", "~CCC~")
                .aisle("CCCCC", "GCGCG", "GCGCG", "GCGCG", "GCGCG", "GCGCG", "C#C#C", "X###X", "X###X", "X###X", "C#C#C", "GCGCG", "GCGCG", "GCGCG", "GCGCG", "GCGCG", "CCCCC")
                .aisle("~C~C~", "~G~G~", "~G~G~", "~G~G~", "~G~G~", "~G~G~", "~C~C~", "XXXXX", "XXSXX", "XXXXX", "~C~C~", "~G~G~", "~G~G~", "~G~G~", "~G~G~", "~G~G~", "~C~C~")
                .where('S', this.selfPredicate())
                .where('C', statePredicate(this.getCasingState()))
                .where('X', statePredicate(this.getCasingState()).or(abilityPartPredicate(ALLOWED_ABILITIES)))
                .where('G', statePredicate(MetaBlocks.MUTLIBLOCK_CASING.getState(BlockMultiblockCasing.MultiblockCasingType.GRATE_CASING)))
                .where('P', LargeSimpleRecipeMapMultiblockController.pistonPredicate())
                .where('#', isAirPredicate())
                .where('~', tile -> true)
                .build();
    }

    private IBlockState getCasingState() {
        return GAMetaBlocks.METAL_CASING_1.getState(MetalCasing1.CasingType.INCOLOY_MA956);
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        int tier = context.getOrDefault("Piston", PistonCasing.CasingType.PISTON_LV).getTier();
        if (tier < GAValues.MAX) {
            this.maxVoltage = 8L << tier * 2;
            this.tier = tier;
        }
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return ClientHandler.INCOLOY_MA956_CASING;
    }

    @Nonnull
    @Override
    protected OrientedOverlayRenderer getFrontOverlay() {
        return ClientHandler.IMPLOSION_OVERLAY;
    }

    @Override
    public int getEUtMultiplier() {
        return TJConfig.largeElectricImplosionCompressor.eutPercentage;
    }

    @Override
    public int getDurationMultiplier() {
        return TJConfig.largeElectricImplosionCompressor.durationPercentage;
    }

    @Override
    public int getChanceMultiplier() {
        return TJConfig.largeElectricImplosionCompressor.chancePercentage;
    }

    @Override
    public int getParallel() {
        return TJConfig.largeElectricImplosionCompressor.stack;
    }
}
