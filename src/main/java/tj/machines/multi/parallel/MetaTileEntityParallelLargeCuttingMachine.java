package tj.machines.multi.parallel;

import tj.TJConfig;
import tj.builder.multicontrollers.ParallelRecipeMapMultiblockController;
import tj.capability.impl.workable.ParallelGAMultiblockRecipeLogic;
import gregicadditions.client.ClientHandler;
import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.components.ConveyorCasing;
import gregicadditions.item.components.MotorCasing;
import gregicadditions.item.metal.MetalCasing1;
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
import static gregicadditions.machines.multi.simple.LargeSimpleRecipeMapMultiblockController.conveyorPredicate;
import static gregicadditions.machines.multi.simple.LargeSimpleRecipeMapMultiblockController.motorPredicate;
import static gregtech.api.metatileentity.multiblock.MultiblockAbility.*;
import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;
import static gregtech.api.render.Textures.CUTTER_OVERLAY;
import static gregtech.api.render.Textures.LATHE_OVERLAY;


public class MetaTileEntityParallelLargeCuttingMachine extends ParallelRecipeMapMultiblockController {

    private static final MultiblockAbility<?>[] ALLOWED_ABILITIES = {IMPORT_ITEMS, EXPORT_ITEMS, IMPORT_FLUIDS, MAINTENANCE_HATCH, INPUT_ENERGY, REDSTONE_CONTROLLER};

    public MetaTileEntityParallelLargeCuttingMachine(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, GATileEntities.LARGE_CUTTING.getRecipeMaps());
        this.recipeMapWorkable = new ParallelGAMultiblockRecipeLogic(this, this::getEUPercentage, this::getDurationPercentage, this::getChancePercentage, this::getStack);
        this.recipeMapWorkable.setMaxVoltage(this::getMaxVoltage);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityParallelLargeCuttingMachine(this.metaTileEntityId);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("tj.multiblock.parallel_large_cutting_machine.description"));
        tooltip.add(I18n.format("tj.multiblock.parallel.description"));
        TooltipHelper.shiftTextJEI(tooltip, tip -> super.addInformation(stack, player, tip, advanced));
    }

    @Override
    protected BlockPattern createStructurePattern() {
        FactoryBlockPattern factoryPattern = FactoryBlockPattern.start(RIGHT, UP, BACK);
        if (this.parallelLayer % 2 == 0) {
            factoryPattern.aisle("XXXXXXX", "X#XXX#X", "C#C~C#C");
            factoryPattern.aisle("XcXXXcX", "XMXXXMX", "C#C~C#C");
        } else {
            factoryPattern.aisle("~~XXXXX", "~~XXX#X", "~~~~C#C");
            factoryPattern.aisle("~~XXXcX", "~~XXXMX", "~~~~C#C");
        }
        for (int layer = 1; layer < this.parallelLayer; layer++) {
            if (layer % 2 == 0) {
                factoryPattern.aisle("XXXXXXX", "X#XXX#X", "C#C~C#C");
                factoryPattern.aisle("XcXXXcX", "XMXXXMX", "C#C~C#C");
            }
        }
        String[] controller = this.parallelLayer > 1 ?
                new String[]{"XXXXXXX", "X#XSX#X", "C#C~C#C"} :
                new String[]{"~~XXXXX", "~~XSX#X", "~~~~C#C"};
        return factoryPattern.aisle(controller)
                .setAmountAtLeast('L', 10)
                .where('S', this.selfPredicate())
                .where('L', statePredicate(this.getCasingState()))
                .where('C', statePredicate(this.getCasingState()))
                .where('X', statePredicate(this.getCasingState()).or(abilityPartPredicate(ALLOWED_ABILITIES)))
                .where('c', conveyorPredicate())
                .where('M', motorPredicate())
                .where('#', isAirPredicate())
                .where('~', tile -> true)
                .build();
    }

    private IBlockState getCasingState() {
        return GAMetaBlocks.METAL_CASING_1.getState(MetalCasing1.CasingType.MARAGING_STEEL_250);
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        int motor = context.getOrDefault("Motor", MotorCasing.CasingType.MOTOR_LV).getTier();
        int conveyor = context.getOrDefault("Conveyor", ConveyorCasing.CasingType.CONVEYOR_LV).getTier();
        int min = Math.min(motor, conveyor);
        this.maxVoltage = (long) (Math.pow(4, min) * 8);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return ClientHandler.MARAGING_STEEL_250_CASING;
    }

    @Nonnull
    @Override
    protected OrientedOverlayRenderer getFrontOverlay() {
        return this.getRecipeMapIndex() == 0 ? CUTTER_OVERLAY : LATHE_OVERLAY;
    }

    @Override
    public int getEUPercentage() {
        return TJConfig.parallelLargeCuttingMachine.eutPercentage;
    }

    @Override
    public int getDurationPercentage() {
        return TJConfig.parallelLargeCuttingMachine.durationPercentage;
    }

    @Override
    public int getChancePercentage() {
        return TJConfig.parallelLargeCuttingMachine.chancePercentage;
    }

    @Override
    public int getStack() {
        return TJConfig.parallelLargeCuttingMachine.stack;
    }

    @Override
    public int getMaxParallel() {
        return TJConfig.parallelLargeCuttingMachine.maximumParallel;
    }
}
