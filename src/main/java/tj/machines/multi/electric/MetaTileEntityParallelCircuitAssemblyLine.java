package tj.machines.multi.electric;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import gregicadditions.capabilities.GregicAdditionsCapabilities;
import gregicadditions.capabilities.IQubitContainer;
import gregicadditions.item.GAMultiblockCasing;
import gregicadditions.item.GAMultiblockCasing2;
import gregicadditions.item.components.ConveyorCasing;
import gregicadditions.item.components.RobotArmCasing;
import gregicadditions.machines.multi.simple.LargeSimpleRecipeMapMultiblockController;
import gregicadditions.recipes.GARecipeMaps;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.api.multiblock.PatternMatchContext;
import gregtech.api.recipes.CountableIngredient;
import gregtech.api.recipes.Recipe;
import gregtech.api.render.ICubeRenderer;
import gregtech.common.blocks.BlockMultiblockCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.items.MetaItems;
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
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import tj.TJConfig;
import tj.blocks.BlockSolidCasings;
import tj.blocks.TJMetaBlocks;
import tj.builder.multicontrollers.GUIDisplayBuilder;
import tj.builder.multicontrollers.TJRecipeMapMultiblockController;
import tj.capability.impl.handler.IAssemblyHandler;
import tj.capability.impl.workable.BasicRecipeLogic;
import tj.textures.TJOrientedOverlayRenderer;
import tj.textures.TJTextures;
import tj.util.ItemStackHelper;
import tj.util.TextUtils;

import javax.annotation.Nullable;
import java.util.*;

import static gregicadditions.machines.multi.mega.MegaMultiblockRecipeMapController.frameworkPredicate;
import static gregicadditions.machines.multi.mega.MegaMultiblockRecipeMapController.frameworkPredicate2;
import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;
import static tj.capability.TJMultiblockDataCodes.PARALLEL_LAYER;
import static tj.machines.multi.electric.MetaTileEntityLargeAssemblyLine.inputBusPredicate;
import static tj.machines.multi.electric.MetaTileEntityLargeGreenhouse.glassPredicate;

public class MetaTileEntityParallelCircuitAssemblyLine extends TJRecipeMapMultiblockController implements IAssemblyHandler {

    private final List<BlockPos> inputBusPos = new ArrayList<>();
    private int parallelLayer = 6;

    public MetaTileEntityParallelCircuitAssemblyLine(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, GARecipeMaps.CIRCUIT_ASSEMBLER_RECIPES, true, false);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityParallelCircuitAssemblyLine(this.metaTileEntityId);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("tj.multiblock.parallel_circuit_assembly_line.description"));
        tooltip.add(I18n.format("tj.multiblock.large_assembly_line.tooltip"));
        super.addInformation(stack, player, tooltip, advanced);
    }

    @Override
    protected BasicRecipeLogic<IAssemblyHandler> createRecipeLogic() {
        return new CircuitRecipeLogic(this);
    }

    @Override
    protected void addDisplayText(GUIDisplayBuilder builder) {
        super.addDisplayText(builder);
        builder.addTranslationLine("tj.multiblock.slices", this.parallelLayer);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        FactoryBlockPattern factoryPattern = FactoryBlockPattern.start(RIGHT, UP, BACK)
                .aisle("CCCCC", "GCOCG", "GC#CG", "EAeAE", "~EAE~");
        for (int i = 0; i < this.parallelLayer; i++) {
            factoryPattern.aisle("FCICF", "G#c#G", "Gr#rG", "EAaAE", "~EAE~");
        }
        return factoryPattern.aisle("CCCCC", "GCCCG", "GC#CG", "EASAE", "~EAE~")
                .where('S', this.selfPredicate())
                .where('C', statePredicate(this.getCasingState()))
                .where('E', statePredicate(MetaBlocks.MUTLIBLOCK_CASING.getState(BlockMultiblockCasing.MultiblockCasingType.GRATE_CASING)))
                .where('G', glassPredicate())
                .where('A', frameworkPredicate().or(frameworkPredicate2()))
                .where('a', statePredicate(MetaBlocks.MUTLIBLOCK_CASING.getState(BlockMultiblockCasing.MultiblockCasingType.ASSEMBLER_CASING)))
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

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        int framework = 0, framework2 = 0;
        if (context.get("framework") instanceof GAMultiblockCasing.CasingType)
            framework = ((GAMultiblockCasing.CasingType) context.get("framework")).getTier();
        if (context.get("framework2") instanceof GAMultiblockCasing2.CasingType)
            framework2 = ((GAMultiblockCasing2.CasingType) context.get("framework2")).getTier();
        int conveyor = context.getOrDefault("Conveyor", ConveyorCasing.CasingType.CONVEYOR_LV).getTier();
        int robotArm = context.getOrDefault("RobotArm", RobotArmCasing.CasingType.ROBOT_ARM_LV).getTier();
        this.inputBusPos.addAll(context.getOrDefault("InputBuses", new HashSet<>()));
        this.inputBusPos.sort(Comparator.comparingInt(pos -> Math.abs(pos.getX() - this.getPos().getX()) + Math.abs(pos.getY() - this.getPos().getY()) + Math.abs(pos.getZ() - this.getPos().getZ())));
        this.tier = Math.min(conveyor, Math.min(robotArm, Math.max(framework, framework2)));
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
            this.parallelLayer = MathHelper.clamp(playerIn.isSneaking() ? this.parallelLayer - 1 : this.parallelLayer + 1, 0, TJConfig.parallelCircuitAssemblyLine.maximumSlices);
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
        TileEntity tileEntity = this.getWorld().getTileEntity(this.inputBusPos.get(index));
        if (!(tileEntity instanceof MetaTileEntityHolder))
            return null;
        MetaTileEntity metaTileEntity = ((MetaTileEntityHolder) tileEntity).getMetaTileEntity();
        return metaTileEntity != null ? metaTileEntity.getImportItems() : null;
    }

    @Override
    public IQubitContainer getQubitContainer() {
        return null;
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
        return TJConfig.parallelCircuitAssemblyLine.eutPercentage;
    }

    @Override
    public int getDurationMultiplier() {
        return TJConfig.parallelCircuitAssemblyLine.durationPercentage;
    }

    @Override
    public int getChanceMultiplier() {
        return TJConfig.parallelCircuitAssemblyLine.chancePercentage;
    }

    @Override
    public int getParallel() {
        return TJConfig.parallelCircuitAssemblyLine.stack;
    }

    @Override
    protected void reinitializeStructurePattern() {
        this.parallelLayer = 6;
        super.reinitializeStructurePattern();
    }

    private void resetStructure() {
        if (this.isStructureFormed())
            this.invalidateStructure();
        this.structurePattern = this.createStructurePattern();
    }

    private static class CircuitRecipeLogic extends BasicRecipeLogic<IAssemblyHandler> {

        public CircuitRecipeLogic(MetaTileEntity metaTileEntity) {
            super(metaTileEntity);
        }

        @Override
        protected int checkItemInputsAmount(int parallels, Recipe recipe, IItemHandlerModifiable inputBus) {
            for (int i = 0; i < recipe.getInputs().size(); i++) {
                CountableIngredient ingredient = recipe.getInputs().get(i);
                inputBus = this.handler.getInputBusAt(i);
                if (inputBus == null) return 0;
                if (ingredient.getCount() > 0) {
                    parallels = Math.min(parallels, ItemStackHelper.extractFromItemHandlerByIngredient(inputBus, ingredient.getIngredient(), ingredient.getCount() * parallels, true) / ingredient.getCount());
                    if (parallels < 1) return 0;
                } else if (!ItemStackHelper.checkItemHandlerForIngredient(inputBus, ingredient.getIngredient()))
                    return 0;
            }
            return parallels;
        }

        @Override
        protected void consumeItemInputs(int parallels, Recipe recipe, IItemHandlerModifiable inputBus) {
            for (int i = 0; i < recipe.getInputs().size(); i++) {
                CountableIngredient ingredient = recipe.getInputs().get(i);
                inputBus = this.handler.getInputBusAt(i);
                ItemStackHelper.extractFromItemHandlerByIngredientToList(inputBus, ingredient.getIngredient(), ingredient.getCount() * parallels, false, this.getItemInputs());
            }
        }
    }
}
