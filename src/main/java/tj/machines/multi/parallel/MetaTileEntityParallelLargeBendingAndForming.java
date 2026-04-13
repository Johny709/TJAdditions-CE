package tj.machines.multi.parallel;

import gregicadditions.GAValues;
import gregicadditions.machines.GATileEntities;
import gregtech.api.capability.IEnergyContainer;
import tj.TJConfig;
import tj.builder.multicontrollers.ParallelRecipeMapMultiblockController;
import gregicadditions.item.components.MotorCasing;
import gregicadditions.item.components.PistonCasing;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.api.multiblock.PatternMatchContext;
import gregtech.api.render.ICubeRenderer;
import gregtech.api.render.OrientedOverlayRenderer;
import gregtech.api.render.Textures;
import gregtech.common.blocks.BlockBoilerCasing;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tj.util.TJUtility;
import tj.util.TooltipHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static tj.multiblockpart.TJMultiblockAbility.REDSTONE_CONTROLLER;
import static gregicadditions.capabilities.GregicAdditionsCapabilities.MAINTENANCE_HATCH;
import static gregicadditions.machines.multi.simple.LargeSimpleRecipeMapMultiblockController.motorPredicate;
import static gregicadditions.machines.multi.simple.LargeSimpleRecipeMapMultiblockController.pistonPredicate;
import static gregtech.api.metatileentity.multiblock.MultiblockAbility.*;
import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;


public class MetaTileEntityParallelLargeBendingAndForming extends ParallelRecipeMapMultiblockController {

    private static final MultiblockAbility<?>[] ALLOWED_ABILITIES = {IMPORT_ITEMS, EXPORT_ITEMS, MAINTENANCE_HATCH, INPUT_ENERGY, REDSTONE_CONTROLLER};

    public MetaTileEntityParallelLargeBendingAndForming(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, GATileEntities.LARGE_BENDER_AND_FORMING.getRecipeMaps());
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityParallelLargeBendingAndForming(this.metaTileEntityId);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("tj.multiblock.parallel_large_bending_and_forming.description"));
        tooltip.add(I18n.format("tj.multiblock.parallel.description"));
        TooltipHelper.shiftTextJEI(tooltip, tip -> super.addInformation(stack, player, tip, advanced));
    }

    @Override
    protected BlockPattern createStructurePattern() {
        final FactoryBlockPattern factoryPattern = FactoryBlockPattern.start(RIGHT, UP, BACK);
        factoryPattern.aisle("XXXXX", "XXXXX", "XPXPX");
        for (int layer = 0; layer < this.parallelLayer; layer++) {
            factoryPattern.aisle("XXXXX", "XMpMX", "XPXPX");
        }
        return factoryPattern.aisle("XXXXX", "XXSXX", "XPXPX")
                .setAmountAtLeast('L', 5)
                .where('S', this.selfPredicate())
                .where('L', statePredicate(this.getCasingState()))
                .where('X', statePredicate(getCasingState()).or(abilityPartPredicate(ALLOWED_ABILITIES)))
                .where('P', statePredicate(MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.TITANIUM_PIPE)))
                .where('M', motorPredicate())
                .where('p', pistonPredicate())
                .build();
    }

    private IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.TITANIUM_STABLE);
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        final int motor = context.getOrDefault("Motor", MotorCasing.CasingType.MOTOR_LV).getTier();
        final int piston = context.getOrDefault("Piston", PistonCasing.CasingType.PISTON_LV).getTier();
        final int tier = Math.min(motor, piston);
        if (tier >= GAValues.MAX) {
            this.maxVoltage = this.getAbilities(INPUT_ENERGY).stream()
                    .mapToLong(IEnergyContainer::getInputVoltage)
                    .max()
                    .orElse(0);
            long amps = this.getAbilities(INPUT_ENERGY).stream()
                    .filter(energy -> energy.getInputVoltage() == this.maxVoltage)
                    .mapToLong(IEnergyContainer::getInputAmperage)
                    .sum() / Math.max(1, this.parallelLayer);
            amps = Math.min(4096, amps);
            while (amps >= 4) {
                amps /= 4;
                this.maxVoltage *= 4;
            }
            if (this.maxVoltage >= Integer.MAX_VALUE)
                this.maxVoltage += this.maxVoltage / Integer.MAX_VALUE;
        } else this.maxVoltage = 8L << tier * 2;
        this.tier = TJUtility.getTierByVoltage(this.maxVoltage);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.STABLE_TITANIUM_CASING;
    }

    @Nonnull
    @Override
    protected OrientedOverlayRenderer getFrontOverlay() {
        switch (this.getRecipeMapIndex()) {
            case 1: return Textures.FORMING_PRESS_OVERLAY;
            case 2: return Textures.WIREMILL_OVERLAY;
            default: return Textures.BENDER_OVERLAY;
        }
    }

    @Override
    public int getEUtMultiplier() {
        return TJConfig.parallelLargeBendingAndForming.eutPercentage;
    }

    @Override
    public int getDurationMultiplier() {
        return TJConfig.parallelLargeBendingAndForming.durationPercentage;
    }

    @Override
    public int getChanceMultiplier() {
        return TJConfig.parallelLargeBendingAndForming.chancePercentage;
    }

    @Override
    public int getParallel() {
        return TJConfig.parallelLargeBendingAndForming.stack;
    }

    @Override
    public int getMaxParallel() {
        return TJConfig.parallelLargeBendingAndForming.maximumParallel;
    }
}
