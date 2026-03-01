package tj.machines.multi.electric;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import gregicadditions.capabilities.GregicAdditionsCapabilities;
import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.GAMultiblockCasing;
import gregicadditions.item.components.ConveyorCasing;
import gregicadditions.item.components.RobotArmCasing;
import gregicadditions.machines.multi.simple.LargeSimpleRecipeMapMultiblockController;
import gregicadditions.recipes.GARecipeMaps;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.BlockWorldState;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.api.multiblock.PatternMatchContext;
import gregtech.api.recipes.CountableIngredient;
import gregtech.api.recipes.Recipe;
import gregtech.api.render.ICubeRenderer;
import gregtech.api.render.OrientedOverlayRenderer;
import gregtech.api.render.Textures;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.BlockMultiblockCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.items.MetaItems;
import gregtech.common.metatileentities.electric.multiblockpart.MetaTileEntityMultiblockPart;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.items.IItemHandlerModifiable;
import tj.TJConfig;
import tj.TJRecipeMaps;
import tj.builder.multicontrollers.TJMultiRecipeMapMultiblockController;
import tj.builder.multicontrollers.UIDisplayBuilder;
import tj.util.ItemStackHelper;

import javax.annotation.Nonnull;

import java.util.*;
import java.util.function.BiFunction;


import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;
import static tj.capability.TJMultiblockDataCodes.PARALLEL_LAYER;
import static tj.machines.multi.electric.MetaTileEntityLargeGreenhouse.glassPredicate;

public class MetaTileEntityLargeAssemblyLine extends TJMultiRecipeMapMultiblockController {

    private final List<BlockPos> inputBusPos = new ArrayList<>();
    private int parallelLayer = 4;

    public MetaTileEntityLargeAssemblyLine(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, GARecipeMaps.ASSEMBLY_LINE_RECIPES, TJRecipeMaps.LARGE_ASSEMBLY_LINE_RECIPES);
        this.reinitializeStructurePattern();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityLargeAssemblyLine(this.metaTileEntityId);
    }

    @Override
    public boolean checkRecipe(Recipe recipe) {
        for (int i = 0; i < recipe.getInputs().size(); i++) {
            CountableIngredient ingredient = recipe.getInputs().get(i);
            IItemHandlerModifiable inputBus = this.getInputBusAt(i);
            if (inputBus == null) continue;
            if (ingredient.getCount() > 0) {
                if (ItemStackHelper.extractFromItemHandlerByIngredient(inputBus, ingredient.getIngredient(), ingredient.getCount(), true) < ingredient.getCount())
                    return false;
            } else if (!ItemStackHelper.checkItemHandlerForIngredient(inputBus, ingredient.getIngredient()))
                return false;
        }
        return true;
    }

    @Override
    protected void addDisplayText(UIDisplayBuilder builder) {
        super.addDisplayText(builder);
        builder.addTranslationLine("tj.multiblock.industrial_fusion_reactor.message", this.parallelLayer);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        FactoryBlockPattern factoryPattern = FactoryBlockPattern.start(RIGHT, UP, BACK)
                .aisle("FOIOF", "CCCCC", "C###C", "CCCCC", "CXXXC", "~CXC~", "~~e~~");
        for (int i = 0; i < this.parallelLayer; i++) {
            factoryPattern.aisle("FCICF", "G#c#G", "G###G", "G#r#G", "EAaAE", "~EAE~", "~~e~~");
        }
        return factoryPattern.aisle("FCICF", "CCCCC", "C###C", "CCCCC", "CXSXC", "~CXC~", "~~e~~")
                .where('S', this.selfPredicate())
                .where('C', statePredicate(this.getCasingState()))
                .where('E', statePredicate(MetaBlocks.MUTLIBLOCK_CASING.getState(BlockMultiblockCasing.MultiblockCasingType.GRATE_CASING)))
                .where('G', glassPredicate())
                .where('A', statePredicate(GAMetaBlocks.MUTLIBLOCK_CASING.getState(GAMultiblockCasing.CasingType.ASSEMBLY_LINE_CASING)))
                .where('a', statePredicate(MetaBlocks.MUTLIBLOCK_CASING.getState(BlockMultiblockCasing.MultiblockCasingType.ASSEMBLER_CASING)))
                .where('X', statePredicate(this.getCasingState()).or(abilityPartPredicate(GregicAdditionsCapabilities.MAINTENANCE_HATCH)))
                .where('I', tilePredicate(inputBusPredicate()))
                .where('O', statePredicate(this.getCasingState()).or(abilityPartPredicate(MultiblockAbility.EXPORT_ITEMS)))
                .where('F', statePredicate(this.getCasingState()).or(abilityPartPredicate(MultiblockAbility.IMPORT_FLUIDS)))
                .where('e', statePredicate(this.getCasingState()).or(abilityPartPredicate(MultiblockAbility.INPUT_ENERGY)))
                .where('c', LargeSimpleRecipeMapMultiblockController.conveyorPredicate())
                .where('r', LargeSimpleRecipeMapMultiblockController.robotArmPredicate())
                .where('#', isAirPredicate())
                .where('~', tile -> true)
                .build();
    }

    private IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID);
    }

    public static BiFunction<BlockWorldState, MetaTileEntity, Boolean> inputBusPredicate() {
        return (state, tile) -> {
            if (tile instanceof MetaTileEntityMultiblockPart) {
                MetaTileEntityMultiblockPart multiblockPart = (MetaTileEntityMultiblockPart) tile;
                if (multiblockPart instanceof IMultiblockAbilityPart<?>) {
                    IMultiblockAbilityPart<?> abilityPart = (IMultiblockAbilityPart<?>) multiblockPart;
                    boolean isInputBus = abilityPart.getAbility() == MultiblockAbility.IMPORT_ITEMS;
                    if (isInputBus) state.getMatchContext().getOrCreate("InputBuses", HashSet::new).add(state.getPos());
                    return isInputBus;
                }
            }
            return false;
        };
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        int conveyor = context.getOrDefault("Conveyor", ConveyorCasing.CasingType.CONVEYOR_LV).getTier();
        int robotArm = context.getOrDefault("RobotArm", RobotArmCasing.CasingType.ROBOT_ARM_LV).getTier();
        this.inputBusPos.addAll(context.getOrDefault("InputBuses", new HashSet<>()));
        this.inputBusPos.sort(Comparator.comparing(pos -> Math.abs(pos.getX() - this.getPos().getX()) + Math.abs(pos.getY() - this.getPos().getY()) + Math.abs(pos.getZ() - this.getPos().getZ())));
        this.tier = Math.min(conveyor, robotArm);
        this.maxVoltage = 8L << this.tier * 2;
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.inputBusPos.clear();
    }

    @Override
    public boolean onRightClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        return !MetaItems.SCREWDRIVER.isItemEqual(playerIn.getHeldItem(hand)) && super.onRightClick(playerIn, hand, facing, hitResult);
    }

    @Override
    public boolean onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        if (!this.getWorld().isRemote) {
            int lastParallelLayer = this.parallelLayer;
            this.parallelLayer = MathHelper.clamp(playerIn.isSneaking() ? this.parallelLayer - 1 : this.parallelLayer + 1, 0, TJConfig.largeAssemblyLine.maximumSlices);
            if (this.parallelLayer != lastParallelLayer) {
                playerIn.sendMessage(new TextComponentTranslation(playerIn.isSneaking() ? "tj.multiblock.parallel.layer.decrement.success" : "tj.multiblock.parallel.layer.increment.success", this.parallelLayer));
            } else playerIn.sendMessage(new TextComponentTranslation(playerIn.isSneaking() ? "tj.multiblock.parallel.layer.decrement.fail" : "tj.multiblock.parallel.layer.increment.fail", this.parallelLayer));
            this.invalidateStructure();
            this.structurePattern = this.createStructurePattern();
            this.writeCustomData(PARALLEL_LAYER, buf -> buf.writeInt(this.parallelLayer));
            this.markDirty();
        }
        return true;
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeInt(this.parallelLayer);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.parallelLayer = buf.readInt();
        this.structurePattern = this.createStructurePattern();
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == PARALLEL_LAYER) {
            this.parallelLayer = buf.readInt();
            this.invalidateStructure();
            this.structurePattern = this.createStructurePattern();
            this.scheduleRenderUpdate();
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger("slices", this.parallelLayer);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.parallelLayer = data.getInteger("slices");
    }

    private IItemHandlerModifiable getInputBusAt(int index) {
        TileEntity tileEntity = this.getWorld().getTileEntity(this.inputBusPos.get(index));
        if (!(tileEntity instanceof MetaTileEntityHolder))
            return null;
        MetaTileEntity metaTileEntity = ((MetaTileEntityHolder) tileEntity).getMetaTileEntity();
        return metaTileEntity != null ? metaTileEntity.getImportItems() : null;
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.SOLID_STEEL_CASING;
    }

    @Nonnull
    @Override
    protected OrientedOverlayRenderer getFrontOverlay() {
        return Textures.ASSEMBLER_OVERLAY;
    }

    @Override
    public int getEUtMultiplier() {
        return this.recipeMapIndex == 0 ? TJConfig.largeAssemblyLine.eutPercentage : super.getEUtMultiplier();
    }

    @Override
    public int getDurationMultiplier() {
        return this.recipeMapIndex == 0 ? TJConfig.largeAssemblyLine.durationPercentage : super.getDurationMultiplier();
    }

    @Override
    public int getChanceMultiplier() {
        return this.recipeMapIndex == 0 ? TJConfig.largeAssemblyLine.chancePercentage : super.getChanceMultiplier();
    }

    @Override
    public int getParallel() {
        return this.recipeMapIndex == 0 ? TJConfig.largeAssemblyLine.stack : super.getParallel();
    }
}
