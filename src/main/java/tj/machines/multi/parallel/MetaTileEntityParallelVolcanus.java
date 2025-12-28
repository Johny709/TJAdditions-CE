package tj.machines.multi.parallel;

import gregicadditions.GAUtility;
import gregicadditions.GAValues;
import gregicadditions.client.ClientHandler;
import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.metal.MetalCasing1;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.api.multiblock.PatternMatchContext;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.render.ICubeRenderer;
import gregtech.api.render.OrientedOverlayRenderer;
import gregtech.api.render.Textures;
import gregtech.common.blocks.BlockBoilerCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tj.TJConfig;
import tj.builder.ParallelRecipeMap;
import tj.builder.handlers.ParallelVolcanusRecipeLogic;
import tj.builder.multicontrollers.ParallelRecipeMapMultiblockController;
import tj.util.TooltipHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static gregicadditions.GAMaterials.Pyrotheum;
import static gregicadditions.capabilities.GregicAdditionsCapabilities.MAINTENANCE_HATCH;
import static gregicadditions.capabilities.GregicAdditionsCapabilities.MUFFLER_HATCH;
import static gregtech.api.metatileentity.multiblock.MultiblockAbility.*;
import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;
import static gregtech.api.recipes.RecipeMaps.BLAST_RECIPES;
import static tj.TJRecipeMaps.PARALLEL_BLAST_RECIPES;
import static tj.machines.multi.electric.MetaTileEntityLargeAlloySmelter.heatingCoilPredicate;
import static tj.machines.multi.electric.MetaTileEntityLargeAlloySmelter.heatingCoilPredicate2;
import static tj.multiblockpart.TJMultiblockAbility.REDSTONE_CONTROLLER;


public class MetaTileEntityParallelVolcanus extends ParallelRecipeMapMultiblockController {

    private static final MultiblockAbility<?>[] ALLOWED_ABILITIES = {IMPORT_ITEMS, EXPORT_ITEMS, IMPORT_FLUIDS, EXPORT_FLUIDS, INPUT_ENERGY, MAINTENANCE_HATCH, REDSTONE_CONTROLLER};

    private FluidStack pyro;
    private int blastFurnaceTemperature;
    private int bonusTemperature;

    public MetaTileEntityParallelVolcanus(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, new ParallelRecipeMap[]{PARALLEL_BLAST_RECIPES});
        this.recipeMapWorkable = new ParallelVolcanusRecipeLogic(this, this::getBlastFurnaceTemperature, this::getPyroConsumeAmount, this::getEUPercentage, this::getDurationPercentage, this::getChancePercentage, this::getStack);
        this.recipeMapWorkable.setMaxVoltage(this::getMaxVoltage);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityParallelVolcanus(this.metaTileEntityId);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("tj.multiblock.parallel_volcanus.description"));
        tooltip.add(I18n.format("tj.multiblock.parallel.description"));
        TooltipHelper.shiftText(tooltip, tip -> {
            tip.add(I18n.format("tj.multiblock.parallel.extend.tooltip"));
            tip.add(I18n.format("gregtech.multiblock.vol_cryo.description"));
            tip.add(I18n.format("gtadditions.multiblock.electric_blast_furnace.tooltip.1"));
            tip.add(I18n.format("gtadditions.multiblock.electric_blast_furnace.tooltip.2"));
            tip.add(I18n.format("gtadditions.multiblock.electric_blast_furnace.tooltip.3"));
            super.addInformation(stack, player, tip, advanced);
        });
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        super.addDisplayText(textList);
        if (!this.isStructureFormed()) return;
        textList.add(new TextComponentTranslation("gregtech.multiblock.blast_furnace.max_temperature", this.blastFurnaceTemperature));
        textList.add(new TextComponentTranslation("gtadditions.multiblock.blast_furnace.additional_temperature", this.bonusTemperature));
        FluidStack drained = this.inputFluidInventory.drain(this.pyro, false);
        textList.add(drained != null && drained.amount == this.pyro.amount ? new TextComponentString(net.minecraft.util.text.translation.I18n.translateToLocalFormatted("machine.universal.fluid.input.tick", this.pyro.getLocalizedName(), this.pyro.amount))
                : new TextComponentString(net.minecraft.util.text.translation.I18n.translateToLocalFormatted("tj.multiblock.not_enough_fluid", this.pyro.getLocalizedName(), this.pyro.amount)));
    }

    @Override
    protected BlockPattern createStructurePattern() {
        FactoryBlockPattern factoryPattern = FactoryBlockPattern.start(RIGHT, FRONT, DOWN);
        for (int layer = 0; layer < this.parallelLayer; layer++) {
            if (layer % 4 == 0) {
                String muffler = layer == 0 ? "XXMXX" : "XXPXX";
                factoryPattern.aisle("XXXXX", "XXXXX", muffler, "XXXXX", "XXXXX");
                factoryPattern.aisle("ccccc", "c#c#c", "ccPcc", "c#c#c", "ccccc");
                factoryPattern.aisle("ccccc", "c#c#c", "ccPcc", "c#c#c", "ccccc");
            }
        }
        return factoryPattern.aisle("XXSXX", "XXXXX", "XXXXX", "XXXXX", "XXXXX")
                .setAmountAtLeast('L', 12)
                .where('S', this.selfPredicate())
                .where('L', statePredicate(this.getCasingState()))
                .where('X', statePredicate(this.getCasingState()).or(abilityPartPredicate(ALLOWED_ABILITIES)))
                .where('c', heatingCoilPredicate().or(heatingCoilPredicate2()))
                .where('P', statePredicate(MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.TUNGSTENSTEEL_PIPE)))
                .where('#', isAirPredicate())
                .where('M', abilityPartPredicate(MUFFLER_HATCH))
                .build();
    }

    private IBlockState getCasingState() {
        return GAMetaBlocks.METAL_CASING_1.getState(MetalCasing1.CasingType.HASTELLOY_N);
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        this.maxVoltage = this.getAbilities(INPUT_ENERGY).stream()
                .mapToLong(IEnergyContainer::getInputVoltage)
                .filter(voltage -> voltage <= GAValues.V[7])
                .max()
                .orElse(GAValues.V[7]);
        int energyTier = GAUtility.getTierByVoltage(this.maxVoltage);
        this.bonusTemperature = Math.max(0, 100 * (energyTier - 2));
        this.blastFurnaceTemperature = context.getOrDefault("blastFurnaceTemperature", 0);
        this.blastFurnaceTemperature += this.bonusTemperature;
        this.pyro = Pyrotheum.getFluid((int) Math.pow(2, GAUtility.getTierByVoltage(this.maxVoltage)));
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return ClientHandler.HASTELLOY_N_CASING;
    }

    @Nonnull
    @Override
    protected OrientedOverlayRenderer getFrontOverlay() {
        return Textures.PRIMITIVE_BLAST_FURNACE_OVERLAY;
    }

    public int getBlastFurnaceTemperature() {
        return this.blastFurnaceTemperature;
    }

    public FluidStack getPyroConsumeAmount() {
        return this.pyro;
    }

    @Override
    public int getEUPercentage() {
        return TJConfig.parallelVolcanus.eutPercentage;
    }

    @Override
    public int getDurationPercentage() {
        return TJConfig.parallelVolcanus.durationPercentage;
    }

    @Override
    public int getChancePercentage() {
        return TJConfig.parallelVolcanus.chancePercentage;
    }

    @Override
    public int getStack() {
        return TJConfig.parallelVolcanus.stack;
    }

    @Override
    public int getMaxParallel() {
        return TJConfig.parallelVolcanus.maximumParallel;
    }

    @Override
    public RecipeMap<?>[] getRecipeMaps() {
        return new RecipeMap[]{BLAST_RECIPES};
    }
}
