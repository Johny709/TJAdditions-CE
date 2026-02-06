package tj.machines.multi.parallel;

import tj.TJConfig;
import tj.builder.multicontrollers.ParallelRecipeMapMultiblockController;
import tj.capability.impl.workable.ParallelGAMultiblockRecipeLogic;
import gregicadditions.client.ClientHandler;
import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.components.PistonCasing;
import gregicadditions.item.metal.MetalCasing2;
import gregicadditions.machines.GATileEntities;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.api.multiblock.PatternMatchContext;
import gregtech.api.render.ICubeRenderer;
import gregtech.api.render.OrientedOverlayRenderer;
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

import static tj.multiblockpart.TJMultiblockAbility.REDSTONE_CONTROLLER;
import static gregicadditions.capabilities.GregicAdditionsCapabilities.MAINTENANCE_HATCH;
import static gregicadditions.machines.multi.simple.LargeSimpleRecipeMapMultiblockController.pistonPredicate;
import static gregtech.api.metatileentity.multiblock.MultiblockAbility.*;
import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;
import static gregtech.api.render.Textures.COMPRESSOR_OVERLAY;
import static gregtech.api.render.Textures.FORGE_HAMMER_OVERLAY;


public class MetaTileEntityParallelLargeForgeHammer extends ParallelRecipeMapMultiblockController {

    private static final MultiblockAbility<?>[] ALLOWED_ABILITIES = {IMPORT_ITEMS, EXPORT_ITEMS, MAINTENANCE_HATCH, INPUT_ENERGY, REDSTONE_CONTROLLER};

    public MetaTileEntityParallelLargeForgeHammer(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, GATileEntities.LARGE_FORGE_HAMMER.getRecipeMaps());
        this.recipeMapWorkable = new ParallelGAMultiblockRecipeLogic(this, this::getEUPercentage, this::getDurationPercentage, this::getChancePercentage, this::getStack);
        this.recipeMapWorkable.setMaxVoltage(this::getMaxVoltage);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityParallelLargeForgeHammer(this.metaTileEntityId);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("tj.multiblock.parallel_large_forge_hammer.description"));
        tooltip.add(I18n.format("tj.multiblock.parallel.description"));
        TooltipHelper.shiftText(tooltip, tip -> {
            tip.add(I18n.format("tj.multiblock.parallel.extend.tooltip"));
            super.addInformation(stack, player, tip, advanced);
        });
    }

    @Override
    protected BlockPattern createStructurePattern() {
        FactoryBlockPattern factoryPattern = FactoryBlockPattern.start(RIGHT, UP, BACK);
        for (int layer = 0; layer < this.parallelLayer; layer++) {
            factoryPattern.aisle("XXX", "~X~", "~~~", "~~~", "~~~");
            factoryPattern.aisle("XXX", "XCX", "C#C", "CPC", "CCC");
        }
        return factoryPattern.aisle("XSX", "~X~", "~~~", "~~~", "~~~")
                .setAmountAtLeast('L', 9)
                .where('S', this.selfPredicate())
                .where('L', statePredicate(this.getCasingState()))
                .where('C', statePredicate(this.getCasingState()))
                .where('X', statePredicate(this.getCasingState()).or(abilityPartPredicate(ALLOWED_ABILITIES)))
                .where('P', pistonPredicate())
                .where('#', isAirPredicate())
                .where('~', tile -> true)
                .build();
    }

    private IBlockState getCasingState() {
        return GAMetaBlocks.METAL_CASING_2.getState(MetalCasing2.CasingType.IRON);
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        int piston = context.getOrDefault("Piston", PistonCasing.CasingType.PISTON_LV).getTier();
        this.maxVoltage = (long) (Math.pow(4, piston) * 8);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return ClientHandler.IRON_CASING;
    }

    @Nonnull
    @Override
    protected OrientedOverlayRenderer getFrontOverlay() {
        return this.getRecipeMapIndex() == 0 ? FORGE_HAMMER_OVERLAY : COMPRESSOR_OVERLAY;
    }

    @Override
    public int getEUPercentage() {
        return TJConfig.parallelLargeForgeHammer.eutPercentage;
    }

    @Override
    public int getDurationPercentage() {
        return TJConfig.parallelLargeForgeHammer.durationPercentage;
    }

    @Override
    public int getChancePercentage() {
        return TJConfig.parallelLargeForgeHammer.chancePercentage;
    }

    @Override
    public int getStack() {
        return TJConfig.parallelLargeForgeHammer.stack;
    }

    @Override
    public int getMaxParallel() {
        return TJConfig.parallelLargeForgeHammer.maximumParallel;
    }
}
