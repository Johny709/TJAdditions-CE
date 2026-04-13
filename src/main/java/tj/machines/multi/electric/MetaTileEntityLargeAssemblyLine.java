package tj.machines.multi.electric;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import gregicadditions.GAValues;
import gregicadditions.capabilities.GregicAdditionsCapabilities;
import gregicadditions.capabilities.IQubitContainer;
import gregicadditions.capabilities.impl.QubitContainerList;
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
import gregtech.common.blocks.BlockMultiblockCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.items.MetaItems;
import gregtech.common.metatileentities.electric.multiblockpart.MetaTileEntityMultiblockPart;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import tj.TJConfig;
import tj.blocks.BlockSolidCasings;
import tj.blocks.TJMetaBlocks;
import tj.builder.multicontrollers.TJMultiRecipeMapMultiblockController;
import tj.builder.multicontrollers.GUIDisplayBuilder;
import tj.capability.OverclockManager;
import tj.capability.impl.handler.IAssemblyHandler;
import tj.capability.impl.workable.BasicRecipeLogic;
import tj.textures.TJOrientedOverlayRenderer;
import tj.textures.TJTextures;
import tj.util.TJItemUtils;
import tj.util.TextUtils;

import javax.annotation.Nullable;

import java.util.*;
import java.util.function.BiFunction;

import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;
import static tj.capability.TJMultiblockDataCodes.PARALLEL_LAYER;
import static tj.machines.multi.electric.MetaTileEntityLargeGreenhouse.glassPredicate;


public class MetaTileEntityLargeAssemblyLine extends TJMultiRecipeMapMultiblockController implements IAssemblyHandler {

    private final List<BlockPos> inputBusPos = new ArrayList<>();
    private IQubitContainer qubitContainer;
    private int parallelLayer = 4;

    public MetaTileEntityLargeAssemblyLine(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, true, false, GARecipeMaps.ASSEMBLY_LINE_RECIPES);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityLargeAssemblyLine(this.metaTileEntityId);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("tj.multiblock.large_assembly_line.description"));
        tooltip.add(I18n.format("tj.multiblock.large_assembly_line.tooltip"));
        tooltip.add(I18n.format("tj.multiblock.parallel.extend.tooltip"));
        super.addInformation(stack, player, tooltip, advanced);
    }

    @Override
    protected BasicRecipeLogic<IAssemblyHandler> createRecipeLogic() {
        return new AssemblyRecipeLogic(this);
    }

    @Override
    public boolean checkRecipe(Recipe recipe) {
        ((AssemblyRecipeLogic) this.recipeLogic).setRecipeQubit(recipe.getIntegerProperty("qubitConsume"));
        return super.checkRecipe(recipe);
    }

    @Override
    public void preOverclock(OverclockManager<?> overclockManager, Recipe recipe) {
        super.preOverclock(overclockManager, recipe);
        if (this.recipeMapIndex > 0)
            overclockManager.setParallel(1);
    }

    @Override
    protected void addDisplayText(GUIDisplayBuilder builder) {
        super.addDisplayText(builder);
        builder.addTranslationLine("tj.multiblock.slices", this.parallelLayer);
        final int qubit = ((AssemblyRecipeLogic) this.recipeLogic).getRecipeQubit();
        if (!this.isStructureFormed() || qubit < 1) return;
        if (this.qubitContainer.getQubitStored() >= qubit)
            builder.addTranslationLine(2, "tj.multiblock.large_assembly_line.qubit", qubit);
        else builder.addTextComponent(new TextComponentTranslation("gtadditions.multiblock.not_enough_qubit").setStyle(new Style().setColor(TextFormatting.RED)), 2);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        final FactoryBlockPattern factoryPattern = FactoryBlockPattern.start(RIGHT, UP, BACK)
                .aisle("CCCCC", "COOOC", "C###C", "COOOC", "EXXXE", "~EXE~", "~~e~~");
        for (int i = 0; i < this.parallelLayer; i++) {
            factoryPattern.aisle("FCICF", "G#c#G", "G###G", "G#r#G", "EAaAE", "~EAE~", "~~e~~");
        }
        return factoryPattern.aisle("CCCCC", "CCCCC", "C###C", "CCCCC", "EXSXE", "~EXE~", "~~e~~")
                .where('S', this.selfPredicate())
                .where('C', statePredicate(this.getCasingState()))
                .where('E', statePredicate(MetaBlocks.MUTLIBLOCK_CASING.getState(BlockMultiblockCasing.MultiblockCasingType.GRATE_CASING)))
                .where('G', glassPredicate())
                .where('A', statePredicate(GAMetaBlocks.MUTLIBLOCK_CASING.getState(GAMultiblockCasing.CasingType.ASSEMBLY_LINE_CASING)))
                .where('a', statePredicate(MetaBlocks.MUTLIBLOCK_CASING.getState(BlockMultiblockCasing.MultiblockCasingType.ASSEMBLER_CASING)))
                .where('X', statePredicate(this.getCasingState()).or(abilityPartPredicate(GregicAdditionsCapabilities.MAINTENANCE_HATCH)))
                .where('I', tilePredicate(inputBusPredicate()))
                .where('O', statePredicate(this.getCasingState()).or(abilityPartPredicate(MultiblockAbility.EXPORT_ITEMS)))
                .where('F', statePredicate(this.getCasingState()).or(abilityPartPredicate(MultiblockAbility.IMPORT_FLUIDS, GregicAdditionsCapabilities.MAINTENANCE_HATCH)))
                .where('e', statePredicate(this.getCasingState()).or(abilityPartPredicate(MultiblockAbility.INPUT_ENERGY, GregicAdditionsCapabilities.INPUT_QBIT)))
                .where('c', LargeSimpleRecipeMapMultiblockController.conveyorPredicate())
                .where('r', LargeSimpleRecipeMapMultiblockController.robotArmPredicate())
                .where('#', isAirPredicate())
                .where('~', tile -> true)
                .build();
    }

    private IBlockState getCasingState() {
        return TJMetaBlocks.SOLID_CASING.getState(BlockSolidCasings.SolidCasingType.ASSEMBLER_CASING);
    }

    public static BiFunction<BlockWorldState, MetaTileEntity, Boolean> inputBusPredicate() {
        return (state, tile) -> {
            if (tile instanceof MetaTileEntityMultiblockPart) {
                final MetaTileEntityMultiblockPart multiblockPart = (MetaTileEntityMultiblockPart) tile;
                if (multiblockPart instanceof IMultiblockAbilityPart<?>) {
                    final IMultiblockAbilityPart<?> abilityPart = (IMultiblockAbilityPart<?>) multiblockPart;
                    final boolean isInputBus = abilityPart.getAbility() == MultiblockAbility.IMPORT_ITEMS;
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
        final int conveyor = context.getOrDefault("Conveyor", ConveyorCasing.CasingType.CONVEYOR_LV).getTier();
        final int robotArm = context.getOrDefault("RobotArm", RobotArmCasing.CasingType.ROBOT_ARM_LV).getTier();
        final int tier = Math.min(conveyor, robotArm);
        this.qubitContainer = new QubitContainerList(this.getAbilities(GregicAdditionsCapabilities.INPUT_QBIT));
        this.inputBusPos.addAll(context.getOrDefault("InputBuses", new HashSet<>()));
        this.inputBusPos.sort(Comparator.comparingInt(pos -> Math.abs(pos.getX() - this.getPos().getX()) + Math.abs(pos.getY() - this.getPos().getY()) + Math.abs(pos.getZ() - this.getPos().getZ())));
        if (tier < GAValues.MAX) {
            this.maxVoltage = 8L << tier * 2;
            this.tier = tier;
        }
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.inputBusPos.clear();
        this.qubitContainer = new QubitContainerList(Collections.emptyList());
    }

    @Override
    public boolean onRightClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        return !MetaItems.SCREWDRIVER.isItemEqual(playerIn.getHeldItem(hand)) && super.onRightClick(playerIn, hand, facing, hitResult);
    }

    @Override
    public boolean onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        if (!this.getWorld().isRemote) {
            final int lastParallelLayer = this.parallelLayer;
            this.parallelLayer = MathHelper.clamp(playerIn.isSneaking() ? this.parallelLayer - 1 : this.parallelLayer + 1, 1, TJConfig.largeAssemblyLine.maximumSlices);
            if (this.parallelLayer != lastParallelLayer) {
                playerIn.sendMessage(TextUtils.addTranslationText(playerIn.isSneaking() ? "tj.multiblock.parallel.layer.decrement.success" : "tj.multiblock.parallel.layer.increment.success", this.parallelLayer));
            } else playerIn.sendMessage(TextUtils.addTranslationText(playerIn.isSneaking() ? "tj.multiblock.parallel.layer.decrement.fail" : "tj.multiblock.parallel.layer.increment.fail", this.parallelLayer));
            this.resetStructure();
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
        this.resetStructure();
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == PARALLEL_LAYER) {
            this.parallelLayer = buf.readInt();
            this.resetStructure();
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
        this.resetStructure();
    }

    @Override
    public IItemHandlerModifiable getInputBusAt(int index) {
        if (index >= this.inputBusPos.size())
            return null;
        final TileEntity tileEntity = this.getWorld().getTileEntity(this.inputBusPos.get(index));
        if (!(tileEntity instanceof MetaTileEntityHolder))
            return null;
        MetaTileEntity metaTileEntity = ((MetaTileEntityHolder) tileEntity).getMetaTileEntity();
        return metaTileEntity != null ? metaTileEntity.getImportItems() : null;
    }

    @Override
    public IQubitContainer getQubitContainer() {
        return this.qubitContainer;
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return TJTextures.ASSEMBLER;
    }

    @Override
    public TJOrientedOverlayRenderer getFrontalOverlay() {
        return TJTextures.TJ_ASSEMBLER_OVERLAY;
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

    @Override
    protected void reinitializeStructurePattern() {
        this.parallelLayer = 4;
        super.reinitializeStructurePattern();
    }

    private void resetStructure() {
        if (this.isStructureFormed())
            this.invalidateStructure();
        this.structurePattern = this.createStructurePattern();
    }

    private static class AssemblyRecipeLogic extends BasicRecipeLogic<IAssemblyHandler> {

        private int recipeQubit;

        public AssemblyRecipeLogic(MetaTileEntity metaTileEntity) {
            super(metaTileEntity);
        }

        public void setRecipeQubit(int recipeQubit) {
            this.recipeQubit = recipeQubit;
        }

        @Override
        protected void progressRecipe(int progress) {
            if (this.recipeQubit < 1 || this.handler.getQubitContainer().removeQubit(this.recipeQubit) == -this.recipeQubit) {
                super.progressRecipe(progress);
            } else if (this.progress > 1)
                this.progress--;
        }

        @Override
        protected boolean completeRecipe() {
            this.recipeQubit = 0;
            return super.completeRecipe();
        }

        @Override
        protected int checkItemInputsAmount(int parallels, Recipe recipe, IItemHandlerModifiable inputBus) {
            for (int i = 0; i < recipe.getInputs().size(); i++) {
                final CountableIngredient ingredient = recipe.getInputs().get(i);
                inputBus = this.handler.getInputBusAt(i);
                if (inputBus == null) return 0;
                if (ingredient.getCount() > 0) {
                    parallels = Math.min(parallels, TJItemUtils.extractFromItemHandlerByIngredient(inputBus, ingredient.getIngredient(), ingredient.getCount() * parallels, true) / ingredient.getCount());
                    if (parallels < 1) return 0;
                } else if (!TJItemUtils.checkItemHandlerForIngredient(inputBus, ingredient.getIngredient()))
                    return 0;
            }
            return parallels;
        }

        @Override
        protected void consumeItemInputs(int parallels, Recipe recipe, IItemHandlerModifiable inputBus) {
            for (int i = 0; i < recipe.getInputs().size(); i++) {
                final CountableIngredient ingredient = recipe.getInputs().get(i);
                inputBus = this.handler.getInputBusAt(i);
                TJItemUtils.extractFromItemHandlerByIngredientToList(inputBus, ingredient.getIngredient(), ingredient.getCount() * parallels, false, this.getItemInputs());
            }
        }

        @Override
        public NBTTagCompound serializeNBT() {
            final NBTTagCompound compound = super.serializeNBT();
            compound.setInteger("qubit", this.recipeQubit);
            return compound;
        }

        @Override
        public void deserializeNBT(NBTTagCompound compound) {
            super.deserializeNBT(compound);
            this.recipeQubit = compound.getInteger("qubit");
        }

        public int getRecipeQubit() {
            return this.recipeQubit;
        }
    }
}
