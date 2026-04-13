package tj.machines.multi.electric;

import gregicadditions.GAConfig;
import gregicadditions.GAValues;
import gregicadditions.capabilities.GregicAdditionsCapabilities;
import gregicadditions.client.ClientHandler;
import gregicadditions.item.GAHeatingCoil;
import gregicadditions.item.metal.MetalCasing1;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.BlockWorldState;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.api.multiblock.PatternMatchContext;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.render.ICubeRenderer;
import gregtech.api.render.OrientedOverlayRenderer;
import gregtech.api.render.Textures;
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
import tj.builder.multicontrollers.TJRecipeMapMultiblockController;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static gregicadditions.item.GAMetaBlocks.METAL_CASING_1;

public class MetaTileEntityLargeAlloySmelter extends TJRecipeMapMultiblockController {

    private static final MultiblockAbility<?>[] ALLOWED_ABILITIES = {MultiblockAbility.IMPORT_ITEMS, MultiblockAbility.EXPORT_ITEMS, MultiblockAbility.INPUT_ENERGY, GregicAdditionsCapabilities.MAINTENANCE_HATCH};
    private final Set<BlockPos> activeStates = new HashSet<>();

    public MetaTileEntityLargeAlloySmelter(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, RecipeMaps.ALLOY_SMELTER_RECIPES);
        this.recipeLogic.setActiveConsumer(this::replaceCoilsAsActive);
    }

    @Override
    public MetaTileEntity createMetaTileEntity (MetaTileEntityHolder holder) {
        return new MetaTileEntityLargeAlloySmelter(metaTileEntityId);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("tj.multiblock.large_alloy_smelter.description"));
        super.addInformation(stack, player, tooltip, advanced);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("HHHHH", "HHHHH", "HHHHH", "~H~H~")
                .aisle("HHHHH", "HcCcH", "c#c#c", "~c~c~")
                .aisle("HHHHH", "HcCcH", "c#c#c", "~c~c~")
                .aisle("HHHHH", "HcCcH", "c#c#c", "~c~c~")
                .aisle("HHHHH", "HHSHH", "HHHHH", "~H~H~")
                .setAmountAtLeast('L', 15)
                .where('S', selfPredicate())
                .where('L', statePredicate(this.getCasingState()))
                .where('C', statePredicate(getCasingState()))
                .where('H', statePredicate(getCasingState()).or(abilityPartPredicate(ALLOWED_ABILITIES)))
                .where('c', heatingCoilPredicate().or(heatingCoilPredicate2()))
                .where('#', isAirPredicate())
                .where('~', (tile) -> true)
                .build();

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

            final int blastFurnaceTemperature = coilType.getCoilTemperature();
            final int currentTemperature = blockWorldState.getMatchContext().getOrPut("blastFurnaceTemperature", blastFurnaceTemperature);

            BlockWireCoil.CoilType currentCoilType = blockWorldState.getMatchContext().getOrPut("coilType", coilType);
            Set<BlockPos> activeStates = blockWorldState.getMatchContext().getOrCreate("activeStates", HashSet::new);
            activeStates.add(blockWorldState.getPos());

            return currentTemperature == blastFurnaceTemperature && coilType.equals(currentCoilType);
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

            final int blastFurnaceTemperature = coilType.getCoilTemperature();
            final int currentTemperature = blockWorldState.getMatchContext().getOrPut("blastFurnaceTemperature", blastFurnaceTemperature);

            final GAHeatingCoil.CoilType currentCoilType = blockWorldState.getMatchContext().getOrPut("gaCoilType", coilType);
            final Set<BlockPos> activeStates = blockWorldState.getMatchContext().getOrCreate("activeStates", HashSet::new);
            activeStates.add(blockWorldState.getPos());

            return currentTemperature == blastFurnaceTemperature && coilType.equals(currentCoilType);
        };
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        int tier = 0;
        final BlockWireCoil.CoilType coilType;
        final GAHeatingCoil.CoilType gaCoilType;
        if ((coilType = context.getOrDefault("coilType", BlockWireCoil.CoilType.CUPRONICKEL)) != null)
            tier = coilType.ordinal() + 1;
        else if ((gaCoilType = context.getOrDefault("gaCoilType", GAHeatingCoil.CoilType.TITAN_STEEL_COIL)) != null)
            tier = gaCoilType.ordinal() + 8;
        if (tier < GAValues.MAX) {
            this.maxVoltage = 8L << tier * 2;
            this.tier = tier;
        }
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

    public IBlockState getCasingState() {
        return METAL_CASING_1.getState(MetalCasing1.CasingType.ZIRCONIUM_CARBIDE);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return ClientHandler.ZIRCONIUM_CARBIDE_CASING;
    }

    @Nonnull
    @Override
    protected OrientedOverlayRenderer getFrontOverlay() {
        return Textures.BLAST_FURNACE_OVERLAY;
    }

    @Override
    public int getEUtMultiplier() {
        return TJConfig.largeAlloySmelter.eutPercentage;
    }

    @Override
    public int getDurationMultiplier() {
        return TJConfig.largeAlloySmelter.durationPercentage;
    }

    @Override
    public int getParallel() {
        return TJConfig.largeAlloySmelter.stack;
    }
}
