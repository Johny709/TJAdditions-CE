package tj.machines.multi.parallel;

import gregicadditions.machines.GATileEntities;
import tj.TJConfig;
import tj.builder.multicontrollers.ParallelRecipeMapMultiblockController;
import tj.capability.impl.ParallelGAMultiblockRecipeLogic;
import gregicadditions.item.components.PumpCasing;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.api.multiblock.PatternMatchContext;
import gregtech.api.recipes.Recipe;
import gregtech.api.render.ICubeRenderer;
import gregtech.api.render.OrientedOverlayRenderer;
import gregtech.api.render.Textures;
import gregtech.common.blocks.BlockBoilerCasing;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.BlockMultiblockCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tj.util.TooltipHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static tj.machines.multi.parallel.MetaTileEntityParallelLargeChemicalReactor.heatingCoilPredicate;
import static tj.machines.multi.parallel.MetaTileEntityParallelLargeChemicalReactor.heatingCoilPredicate2;
import static tj.multiblockpart.TJMultiblockAbility.REDSTONE_CONTROLLER;
import static gregicadditions.capabilities.GregicAdditionsCapabilities.MAINTENANCE_HATCH;
import static gregicadditions.machines.multi.simple.LargeSimpleRecipeMapMultiblockController.pumpPredicate;
import static gregtech.api.metatileentity.multiblock.MultiblockAbility.*;
import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;
import static gregtech.api.render.Textures.ARC_FURNACE_OVERLAY;
import static gregtech.api.render.Textures.PLASMA_ARC_FURNACE_OVERLAY;


public class MetaTileEntityParallelLargeArcFurnace extends ParallelRecipeMapMultiblockController {

    private static final MultiblockAbility<?>[] ALLOWED_ABILITIES = {IMPORT_ITEMS, EXPORT_ITEMS, IMPORT_FLUIDS, EXPORT_FLUIDS, MAINTENANCE_HATCH, INPUT_ENERGY, REDSTONE_CONTROLLER};

    public MetaTileEntityParallelLargeArcFurnace(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, GATileEntities.LARGE_ARC_FURNACE.getRecipeMaps());
        this.recipeMapWorkable = new ParallelGAMultiblockRecipeLogic(this, this::getEUPercentage, this::getDurationPercentage, this::getChancePercentage, this::getStack) {

            @Override
            protected void setupRecipe(Recipe recipe, int i) {
                int energyBonus = this.controller.getEUBonus();
                long resultOverclock = this.overclockManager.getEUt();
                resultOverclock -= (long) (resultOverclock * energyBonus * 0.01f);
                this.overclockManager.setEUt(resultOverclock);
                super.setupRecipe(recipe, i);
            }
        };
        this.recipeMapWorkable.setMaxVoltage(this::getMaxVoltage);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityParallelLargeArcFurnace(this.metaTileEntityId);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("tj.multiblock.parallel_large_arc_furnace.description"));
        tooltip.add(I18n.format("tj.multiblock.parallel.description"));
        TooltipHelper.shiftText(tooltip, tip -> {
            tip.add(I18n.format("tj.multiblock.parallel.extend.tooltip"));
            tip.add(I18n.format("gtadditions.multiblock.large_chemical_reactor.tooltip.2"));
            super.addInformation(stack, player, tip, advanced);
        });
    }

    @Override
    protected BlockPattern createStructurePattern() {
        FactoryBlockPattern factoryPattern = FactoryBlockPattern.start(RIGHT, FRONT, DOWN);
        for (int layer = 0; layer < this.parallelLayer; layer++) {

            String entityS = layer == this.parallelLayer - 1 ? "~GSG~" : "~GGG~";

            factoryPattern.aisle("~XXX~", "XXcXX", "XXcXX", "XXcXX", "~XXX~");
            factoryPattern.aisle(entityS, "GT#TG", "GP#PG", "GT#TG", "~GGG~");
        }
        return factoryPattern.aisle("~XXX~", "XXcXX", "XXcXX", "XXcXX", "~XXX~")
                .setAmountAtLeast('L', 9)
                .where('S', this.selfPredicate())
                .where('L', statePredicate(this.getCasingState()))
                .where('X', statePredicate(this.getCasingState()).or(abilityPartPredicate(ALLOWED_ABILITIES)))
                .where('G', statePredicate(MetaBlocks.MUTLIBLOCK_CASING.getState(BlockMultiblockCasing.MultiblockCasingType.GRATE_CASING)))
                .where('P', pumpPredicate())
                .where('c', heatingCoilPredicate().or(heatingCoilPredicate2()))
                .where('T', statePredicate(MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.TITANIUM_PIPE)))
                .where('#', isAirPredicate())
                .where('~', tile -> true)
                .build();
    }

    private IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.INVAR_HEATPROOF);
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        int pump = context.getOrDefault("Pump", PumpCasing.CasingType.PUMP_LV).getTier();
        this.maxVoltage = (long) (Math.pow(4, pump) * 8);
        this.energyBonus = context.getOrDefault("coilLevel", 0) * 5;
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.HEAT_PROOF_CASING;
    }

    @Nonnull
    @Override
    protected OrientedOverlayRenderer getFrontOverlay() {
        return this.getRecipeMapIndex() == 0 ? ARC_FURNACE_OVERLAY : PLASMA_ARC_FURNACE_OVERLAY;
    }

    @Override
    public int getEUPercentage() {
        return TJConfig.parallelLargeArcFurnace.eutPercentage;
    }

    @Override
    public int getDurationPercentage() {
        return TJConfig.parallelLargeArcFurnace.durationPercentage;
    }

    @Override
    public int getChancePercentage() {
        return TJConfig.parallelLargeArcFurnace.chancePercentage;
    }

    @Override
    public int getStack() {
        return TJConfig.parallelLargeArcFurnace.stack;
    }

    @Override
    public int getMaxParallel() {
        return TJConfig.parallelLargeArcFurnace.maximumParallel;
    }
}
