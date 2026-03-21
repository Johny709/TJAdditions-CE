package tj.machines.multi.parallel;

import gregicadditions.item.GAHeatingCoil;
import gregtech.common.blocks.BlockWireCoil;
import net.minecraft.block.Block;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import tj.TJConfig;
import tj.builder.multicontrollers.ParallelRecipeMapMultiblockController;
import tj.capability.OverclockManager;
import gregicadditions.capabilities.GregicAdditionsCapabilities;
import gregicadditions.client.ClientHandler;
import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.GAMultiblockCasing;
import gregicadditions.item.components.MotorCasing;
import gregicadditions.item.components.PumpCasing;
import gregicadditions.machines.GATileEntities;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static tj.machines.multi.parallel.MetaTileEntityParallelLargeChemicalReactor.heatingCoilPredicate;
import static tj.machines.multi.parallel.MetaTileEntityParallelLargeChemicalReactor.heatingCoilPredicate2;
import static tj.multiblockpart.TJMultiblockAbility.REDSTONE_CONTROLLER;
import static gregicadditions.machines.multi.simple.LargeSimpleRecipeMapMultiblockController.motorPredicate;
import static gregicadditions.machines.multi.simple.LargeSimpleRecipeMapMultiblockController.pumpPredicate;
import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;
import static gregtech.api.unification.material.Materials.Steel;


public class MetaTileEntityParallelAdvancedLargeChemicalReactor extends ParallelRecipeMapMultiblockController {

    private static final MultiblockAbility<?>[] ALLOWED_ABILITIES = {MultiblockAbility.IMPORT_ITEMS, MultiblockAbility.EXPORT_ITEMS,
            MultiblockAbility.IMPORT_FLUIDS, MultiblockAbility.EXPORT_FLUIDS, MultiblockAbility.INPUT_ENERGY, GregicAdditionsCapabilities.MAINTENANCE_HATCH};
    private final Set<BlockPos> activeStates = new HashSet<>();

    public MetaTileEntityParallelAdvancedLargeChemicalReactor(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, GATileEntities.CHEMICAL_PLANT.getRecipeMaps());
        this.recipeLogic.setActiveConsumer((b, i) -> this.replaceCoilsAsActive(this.recipeLogic.isActive()));
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityParallelAdvancedLargeChemicalReactor(this.metaTileEntityId);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("tj.multiblock.advanced_parallel_chemical_reactor.description"));
        tooltip.add(I18n.format("tj.multiblock.parallel.description"));
        TooltipHelper.shiftTextJEI(tooltip, tip -> {
            tip.add(I18n.format("tj.multiblock.parallel.chemical_plant.description"));
            tip.add(I18n.format("gtadditions.multiblock.large_chemical_reactor.tooltip.1"));
            tip.add(I18n.format("gtadditions.multiblock.large_chemical_reactor.tooltip.2"));
            tip.add(I18n.format("gtadditions.multiblock.large_chemical_reactor.tooltip.3"));
            super.addInformation(stack, player, tip, advanced);
        });
    }

    @Override
    public void preOverclock(OverclockManager<?> overclockManager, Recipe recipe) {
        super.preOverclock(overclockManager, recipe);
        overclockManager.setEuMultiplier(4);
    }

    @Override
    public void postOverclock(OverclockManager<?> overclockManager, Recipe recipe) {
        overclockManager.setEUt(overclockManager.getEUt() * (100 - this.energyBonus) / 100);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        FactoryBlockPattern factoryPattern = FactoryBlockPattern.start(RIGHT, FRONT, DOWN);
        if (!(this.parallelLayer % 2 == 0)) {
            factoryPattern.aisle("C~~~C~XXXXX~~~~~~", "CCCCC~XXXXX~~~~~~", "C~~~C~XXXXX~~~~~~", "CCCCC~XXXXX~~~~~~", "C~~~C~XXXXX~~~~~~");
            factoryPattern.aisle("CCCCC~F~~~F~~~~~~", "CcccC~~~P~~~~~~~~", "CPPPPPPPpP~~~~~~~", "CcccC~~~P~~~~~~~~", "CCCCC~F~~~F~~~~~~");
            factoryPattern.aisle("C~~~C~F~~~F~~~~~~", "CPPPPPPPP~~~~~~~~", "MmmmC~~PpP~~~~~~~", "CPPPPPPPP~~~~~~~~", "C~~~C~F~~~F~~~~~~");
            factoryPattern.aisle("CCCCC~F~~~F~~~~~~", "CcccC~~~P~~~~~~~~", "CPPPPPPPpP~~~~~~~", "CcccC~~~P~~~~~~~~", "CCCCC~F~~~F~~~~~~");
        } else {
            factoryPattern.aisle("C~~~C~XXXXX~C~~~C", "CCCCC~XXXXX~CCCCC", "C~~~C~XXXXX~C~~~C", "CCCCC~XXXXX~CCCCC", "C~~~C~XXXXX~C~~~C");
            factoryPattern.aisle("CCCCC~F~~~F~CCCCC", "CcccC~~~P~~~CcccC", "CPPPPPPPpPPPPPPPC", "CcccC~~~P~~~CcccC", "CCCCC~F~~~F~CCCCC");
            factoryPattern.aisle("C~~~C~F~~~F~C~~~C", "CPPPPPPPPPPPPPPPC", "MmmmC~~PpP~~CmmmM", "CPPPPPPPPPPPPPPPC", "C~~~C~F~~~F~C~~~C");
            factoryPattern.aisle("CCCCC~F~~~F~CCCCC", "CcccC~~~P~~~CcccC", "CPPPPPPPpPPPPPPPC", "CcccC~~~P~~~CcccC", "CCCCC~F~~~F~CCCCC");
        }
        for (int count = 1; count < this.parallelLayer; count++) {
            if (count % 2 == 0) {
                factoryPattern.aisle("C~~~C~F~~~F~C~~~C", "CCCCC~~~P~~~CCCCC", "C~~~C~~PpP~~C~~~C", "CCCCC~~~P~~~CCCCC", "C~~~C~F~~~F~C~~~C");
                factoryPattern.aisle("CCCCC~F~~~F~CCCCC", "CcccC~~~P~~~CcccC", "CPPPPPPPpPPPPPPPC", "CcccC~~~P~~~CcccC", "CCCCC~F~~~F~CCCCC");
                factoryPattern.aisle("C~~~C~F~~~F~C~~~C", "CPPPPPPPPPPPPPPPC", "MmmmC~~PpP~~CmmmM", "CPPPPPPPPPPPPPPPC", "C~~~C~F~~~F~C~~~C");
                factoryPattern.aisle("CCCCC~F~~~F~CCCCC", "CcccC~~~P~~~CcccC", "CPPPPPPPpPPPPPPPC", "CcccC~~~P~~~CcccC", "CCCCC~F~~~F~CCCCC");
            }
        }
        String[] controller = this.parallelLayer > 1 ?
                new String[]{"C~~~C~XXSXX~C~~~C", "CCCCC~XXXXX~CCCCC", "C~~~C~XXXXX~C~~~C", "CCCCC~XXXXX~CCCCC", "C~~~C~XXXXX~C~~~C"} :
                new String[]{"C~~~C~XXSXX~~~~~~", "CCCCC~XXXXX~~~~~~", "C~~~C~XXXXX~~~~~~", "CCCCC~XXXXX~~~~~~", "C~~~C~XXXXX~~~~~~"};

        return factoryPattern.aisle(controller)
                .where('S', this.selfPredicate())
                .where('X', statePredicate(this.getCasingState()).or(abilityPartPredicate(ALLOWED_ABILITIES)))
                .where('C', statePredicate(this.getCasingState()))
                .where('P', statePredicate(GAMetaBlocks.MUTLIBLOCK_CASING.getState(GAMultiblockCasing.CasingType.PTFE_PIPE)))
                .where('F', statePredicate(MetaBlocks.FRAMES.get(Steel).getDefaultState()))
                .where('c', heatingCoilPredicate().or(heatingCoilPredicate2()))
                .where('p', pumpPredicate())
                .where('m', motorPredicate())
                .where('M', statePredicate(this.getCasingState()).or(abilityPartPredicate(REDSTONE_CONTROLLER)))
                .where('~', tile -> true)
                .build();
    }

    private IBlockState getCasingState() {
        return GAMetaBlocks.MUTLIBLOCK_CASING.getState(GAMultiblockCasing.CasingType.CHEMICALLY_INERT);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return ClientHandler.CHEMICALLY_INERT;
    }

    @Nonnull
    @Override
    protected OrientedOverlayRenderer getFrontOverlay() {
        return Textures.CHEMICAL_REACTOR_OVERLAY;
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        int motor = context.getOrDefault("Motor", MotorCasing.CasingType.MOTOR_LV).getTier();
        int pump = context.getOrDefault("Pump", PumpCasing.CasingType.PUMP_LV).getTier();
        this.tier = Math.min(motor, pump);
        this.maxVoltage = 8L << this.tier * 2;
        this.energyBonus = context.getOrDefault("coilLevel", 0) * 5;
        this.activeStates.addAll(context.getOrDefault("activeStates", new HashSet<>()));
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.activeStates.clear();
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
        if (this.recipeLogic.isActive() != isActive)
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
    public int getEUtMultiplier() {
        return TJConfig.advancedParallelChemicalReactor.eutPercentage;
    }

    @Override
    public int getDurationMultiplier() {
        return TJConfig.advancedParallelChemicalReactor.durationPercentage;
    }

    @Override
    public int getChanceMultiplier() {
        return TJConfig.advancedParallelChemicalReactor.chancePercentage;
    }

    @Override
    public int getParallel() {
        return TJConfig.advancedParallelChemicalReactor.stack;
    }

    @Override
    public int getMaxParallel() {
        return TJConfig.advancedParallelChemicalReactor.maximumParallel;
    }
}
