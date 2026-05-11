package tj.machines.multi.parallel;

import gregicadditions.GAUtility;
import gregicadditions.machines.GATileEntities;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.recipes.Recipe;
import net.minecraft.block.Block;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import tj.TJConfig;
import gregicadditions.GAConfig;
import gregicadditions.capabilities.GregicAdditionsCapabilities;
import gregicadditions.client.ClientHandler;
import gregicadditions.item.GAHeatingCoil;
import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.GAMultiblockCasing;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.BlockWorldState;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.api.multiblock.PatternMatchContext;
import gregtech.api.render.ICubeRenderer;
import gregtech.common.blocks.BlockWireCoil;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tj.builder.multicontrollers.ParallelRecipeMapMultiblockController;
import tj.capability.OverclockManager;
import tj.util.TooltipHelper;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static gregtech.api.metatileentity.multiblock.MultiblockAbility.INPUT_ENERGY;
import static tj.multiblockpart.TJMultiblockAbility.REDSTONE_CONTROLLER;
import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;
import static gregtech.api.unification.material.Materials.Steel;


public class MetaTileEntityParallelLargeChemicalReactor extends ParallelRecipeMapMultiblockController {

    private static final MultiblockAbility<?>[] ALLOWED_ABILITIES = {MultiblockAbility.IMPORT_ITEMS, MultiblockAbility.EXPORT_ITEMS, MultiblockAbility.IMPORT_FLUIDS, MultiblockAbility.EXPORT_FLUIDS, MultiblockAbility.INPUT_ENERGY, GregicAdditionsCapabilities.MAINTENANCE_HATCH};
    private final Set<BlockPos> activeStates = new HashSet<>();

    public MetaTileEntityParallelLargeChemicalReactor(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, GATileEntities.LARGE_CHEMICAL_REACTOR.recipeMap);
        this.recipeLogic.setActiveConsumer((b, i) -> this.replaceCoilsAsActive(this.recipeLogic.isActive()));
    }

    @Override
    public MetaTileEntityParallelLargeChemicalReactor createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityParallelLargeChemicalReactor(this.metaTileEntityId);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("tj.multiblock.parallel_chemical_reactor.description"));
        tooltip.add(I18n.format("tj.multiblock.parallel.description"));
        TooltipHelper.shiftTextJEI(tooltip, tip -> {
            tip.add(I18n.format("gtadditions.multiblock.large_chemical_reactor.tooltip.1"));
            tip.add(I18n.format("gtadditions.multiblock.large_chemical_reactor.tooltip.2"));
            tip.add(I18n.format("gtadditions.multiblock.large_chemical_reactor.tooltip.3"));
            super.addInformation(stack, player, tip, advanced);
        });
    }

    @Override
    public void preOverclock(OverclockManager<?> overclockManager, Recipe recipe) {
        overclockManager.setDurationMultiplier(4);
        overclockManager.setParallel(this.batchMode.getAmount());
    }

    @Override
    public void postOverclock(OverclockManager<?> overclockManager, Recipe recipe) {
        super.postOverclock(overclockManager, recipe);
        overclockManager.setEUt(overclockManager.getEUt() * (100 - this.energyBonus) / 100);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        final FactoryBlockPattern factoryPattern = FactoryBlockPattern.start(RIGHT, FRONT, DOWN);

        factoryPattern.aisle("XXXXX", "XXXXX", "XXXXX", "XXXXX", "XXXXX");
        for (int layer = 0; layer < this.parallelLayer; layer++) {
            factoryPattern.aisle("F###F", "#MMM#", "#MCM#", "#MMM#", "F###F");
            factoryPattern.aisle("F###F", "#PPP#", "#PcP#", "#PPP#", "F###F");
        }
        return factoryPattern
                .aisle("F###F", "#MMM#", "#MCM#", "#MMM#", "F###F")
                .aisle("XXSXX", "XXXXX", "XXXXX", "XXXXX", "XXXXX")
                .where('S', this.selfPredicate())
                .where('C', statePredicate(this.getCasingState()))
                .where('X', statePredicate(this.getCasingState()).or(abilityPartPredicate(ALLOWED_ABILITIES)))
                .where('M', statePredicate(this.getCasingState()).or(abilityPartPredicate(REDSTONE_CONTROLLER)))
                .where('c', heatingCoilPredicate().or(heatingCoilPredicate2()))
                .where('P', statePredicate(GAMetaBlocks.MUTLIBLOCK_CASING.getState(GAMultiblockCasing.CasingType.PTFE_PIPE)))
                .where('F', statePredicate(MetaBlocks.FRAMES.get(Steel).getDefaultState()))
                .where('#', (tile) -> true)
                .build();
    }

    private IBlockState getCasingState() {
        return GAMetaBlocks.MUTLIBLOCK_CASING.getState(GAMultiblockCasing.CasingType.CHEMICALLY_INERT);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return ClientHandler.CHEMICALLY_INERT;
    }

    public static Predicate<BlockWorldState> heatingCoilPredicate() {
        return blockWorldState -> {
            final IBlockState blockState = blockWorldState.getBlockState();
            if (!(blockState.getBlock() instanceof BlockWireCoil))
                return false;
            final BlockWireCoil blockWireCoil = (BlockWireCoil) blockState.getBlock();
            final BlockWireCoil.CoilType coilType = blockWireCoil.getState(blockState);
            if (Arrays.asList(GAConfig.multis.heatingCoils.gtceHeatingCoilsBlacklist).contains(coilType.getName()))
                return false;

            final int coilLevel = coilType.ordinal();
            final int currentLevel = blockWorldState.getMatchContext().getOrPut("coilLevel", coilLevel);

            final BlockWireCoil.CoilType currentCoilType = blockWorldState.getMatchContext().getOrPut("coilType", coilType);
            final Set<BlockPos> activeStates = blockWorldState.getMatchContext().getOrCreate("activeStates", HashSet::new);
            activeStates.add(blockWorldState.getPos());

            return currentLevel == coilLevel && coilType.equals(currentCoilType);
        };
    }

    public static Predicate<BlockWorldState> heatingCoilPredicate2() {
        return blockWorldState -> {
            final IBlockState blockState = blockWorldState.getBlockState();
            if (!(blockState.getBlock() instanceof GAHeatingCoil))
                return false;
            final GAHeatingCoil blockWireCoil = (GAHeatingCoil) blockState.getBlock();
            final GAHeatingCoil.CoilType coilType = blockWireCoil.getState(blockState);
            if (Arrays.asList(GAConfig.multis.heatingCoils.gregicalityheatingCoilsBlacklist).contains(coilType.getName()))
                return false;

            final int coilLevel = coilType.ordinal() + 8;
            final int currentLevel = blockWorldState.getMatchContext().getOrPut("coilLevel", coilLevel);

            final GAHeatingCoil.CoilType currentCoilType = blockWorldState.getMatchContext().getOrPut("gaCoilType", coilType);
            final Set<BlockPos> activeStates = blockWorldState.getMatchContext().getOrCreate("activeStates", HashSet::new);
            activeStates.add(blockWorldState.getPos());

            return currentLevel == coilLevel && coilType.equals(currentCoilType);
        };
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        this.activeStates.addAll(context.getOrDefault("activeStates", new HashSet<>()));
        this.energyBonus = context.getOrDefault("coilLevel", 0) * 5;
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
        this.tier = GAUtility.getTierByVoltage(this.maxVoltage);
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.activeStates.clear();
        this.energyBonus = -1;
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        this.writeActiveBlockPacket(buf, this.recipeLogic.isActive());
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.readActiveBlockPacket(buf);
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == 128) {
            this.readActiveBlockPacket(buf);
        }
    }

    private void writeActiveBlockPacket(PacketBuffer buffer, boolean isActive) {
        buffer.writeBoolean(isActive);
        buffer.writeInt(this.activeStates.size());
        for (BlockPos pos : this.activeStates) {
            buffer.writeBlockPos(pos);
        }
    }

    private void readActiveBlockPacket(PacketBuffer buffer) {
        boolean isActive = buffer.readBoolean();
        int size = buffer.readInt();
        for (int i = 0; i < size; i++) {
            BlockPos pos = buffer.readBlockPos();
            IBlockState state = this.getWorld().getBlockState(pos);
            Block block = state.getBlock();
            if (block instanceof BlockWireCoil) {
                state = state.withProperty(BlockWireCoil.ACTIVE, isActive);
                this.getWorld().setBlockState(pos, state);
            } else if (block instanceof GAHeatingCoil) {
                state = state.withProperty(GAHeatingCoil.ACTIVE, isActive);
                this.getWorld().setBlockState(pos, state);
            }
        }
    }

    public void replaceCoilsAsActive(boolean isActive) {
        this.writeCustomData(128, buffer -> this.writeActiveBlockPacket(buffer, isActive));
    }

    @Override
    public void onRemoval() {
        super.onRemoval();
        if (!this.getWorld().isRemote) {
            this.replaceCoilsAsActive(false);
            this.markDirty();
        }
    }

    @Override
    public int getMaxParallel() {
        return TJConfig.parallelChemicalReactor.maximumParallel;
    }
}
