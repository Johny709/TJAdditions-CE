package tj.machines.multi.parallel;

import gregicadditions.GAValues;
import gregicadditions.client.ClientHandler;
import gregicadditions.item.GAHeatingCoil;
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
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.render.ICubeRenderer;
import gregtech.common.blocks.BlockWireCoil;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tj.TJConfig;
import tj.builder.multicontrollers.ParallelRecipeMapMultiblockController;
import tj.util.TJUtility;
import tj.util.TooltipHelper;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static gregicadditions.capabilities.GregicAdditionsCapabilities.MAINTENANCE_HATCH;
import static gregtech.api.metatileentity.multiblock.MultiblockAbility.*;
import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;

public class MetaTileEntityParallelLargeAlloySmelter extends ParallelRecipeMapMultiblockController {

    private static final MultiblockAbility<?>[] ALLOWED_ABILITIES = {IMPORT_ITEMS, EXPORT_ITEMS, INPUT_ENERGY, MAINTENANCE_HATCH};
    private final Set<BlockPos> activeStates = new HashSet<>();

    public MetaTileEntityParallelLargeAlloySmelter(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, RecipeMaps.ALLOY_SMELTER_RECIPES);
        this.recipeLogic.setActiveConsumer((b, i) -> this.replaceCoilsAsActive(b));
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityParallelLargeAlloySmelter(this.metaTileEntityId);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("tj.multiblock.parallel_large_alloy_smelter.description"));
        tooltip.add(I18n.format("tj.multiblock.parallel.description"));
        TooltipHelper.shiftTextJEI(tooltip, tip -> super.addInformation(stack, player, tip, advanced));
    }

    @Override
    protected BlockPattern createStructurePattern() {
        final FactoryBlockPattern factoryPattern = FactoryBlockPattern.start(RIGHT, UP, BACK);
        factoryPattern.aisle("~X~X~", "XXXXX", "~XXX~", "XXXXX", "~X~X~");
        for (int layer = 0; layer < this.parallelLayer; layer++) {
            factoryPattern.aisle("~C~C~", "C#C#C", "~C#C~", "C#C#C", "~C~C~");
            factoryPattern.aisle("~C~C~", "C#C#C", "~C#C~", "C#C#C", "~C~C~");
        }
        return factoryPattern.setAmountAtLeast('L', 7)
                .aisle("~C~C~", "C#C#C", "~C#C~", "C#C#C", "~C~C~")
                .aisle("~X~X~", "XXXXX", "~XSX~", "XXXXX", "~X~X~")
                .where('S', this.selfPredicate())
                .where('L', statePredicate(this.getCasingState()))
                .where('X', statePredicate(this.getCasingState()).or(abilityPartPredicate(ALLOWED_ABILITIES)))
                .where('C', coilPredicate())
                .where('#', isAirPredicate())
                .where('~', tile -> true)
                .build();
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        final int tier = context.getOrDefault("coilIndex", 0) + 1;
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
        this.activeStates.addAll(context.getOrDefault("coilPos", new HashSet<>()));
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

    private IBlockState getCasingState() {
        return GAMetaBlocks.METAL_CASING_1.getState(MetalCasing1.CasingType.ZIRCONIUM_CARBIDE);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return ClientHandler.ZIRCONIUM_CARBIDE_CASING;
    }

    @Override
    public int getEUtMultiplier() {
        return TJConfig.parallelLargeAlloySmelter.eutPercentage;
    }

    @Override
    public int getDurationMultiplier() {
        return TJConfig.parallelLargeAlloySmelter.durationPercentage;
    }

    @Override
    public int getChanceMultiplier() {
        return TJConfig.parallelLargeAlloySmelter.chancePercentage;
    }

    @Override
    public int getParallel() {
        return TJConfig.parallelLargeAlloySmelter.stack;
    }

    @Override
    public int getMaxParallel() {
        return TJConfig.parallelLargeAlloySmelter.maximumParallel;
    }
}
