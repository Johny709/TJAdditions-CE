package tj.machines.multi.electric;

import gregicadditions.GAValues;
import gregicadditions.client.ClientHandler;
import gregicadditions.item.GAHeatingCoil;
import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.GATransparentCasing;
import gregicadditions.item.metal.MetalCasing1;
import gregicadditions.recipes.GARecipeMaps;
import gregtech.api.GTValues;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.BlockWorldState;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.api.multiblock.PatternMatchContext;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.recipeproperties.BlastTemperatureProperty;
import gregtech.api.render.ICubeRenderer;
import gregtech.common.blocks.BlockFireboxCasing;
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
import tj.blocks.BlockActiveAbility;
import tj.builder.multicontrollers.GUIDisplayBuilder;
import tj.builder.multicontrollers.TJRecipeMapMultiblockController;
import tj.capability.OverclockManager;
import tj.capability.impl.handler.IRecipeHandler;
import tj.capability.impl.workable.BasicRecipeLogic;
import tj.capability.impl.workable.MegaRecipeLogic;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static gregicadditions.capabilities.GregicAdditionsCapabilities.MAINTENANCE_HATCH;
import static gregicadditions.capabilities.GregicAdditionsCapabilities.MUFFLER_HATCH;
import static gregtech.api.metatileentity.multiblock.MultiblockAbility.*;

public class MetaTileEntityMegaAlloyBlastSmelter extends TJRecipeMapMultiblockController {

    private static final MultiblockAbility<?>[] ALLOWED_ABILITIES = {IMPORT_ITEMS, IMPORT_FLUIDS, EXPORT_FLUIDS, INPUT_ENERGY, MAINTENANCE_HATCH};
    private final Set<BlockPos> activeStates = new HashSet<>();
    private int blastFurnaceTemperature;
    private int bonusTemperature;

    public MetaTileEntityMegaAlloyBlastSmelter(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, GARecipeMaps.BLAST_ALLOY_RECIPES);
        this.recipeLogic.setActiveConsumer(this::replaceCoilsAsActive);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityMegaAlloyBlastSmelter(this.metaTileEntityId);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("tj.multiblock.mega"));
        super.addInformation(stack, player, tooltip, advanced);
    }

    @Override
    protected BasicRecipeLogic<? extends IRecipeHandler> createRecipeLogic() {
        return new MegaRecipeLogic<>(this);
    }

    @Override
    public boolean checkRecipe(Recipe recipe) {
        return this.blastFurnaceTemperature >= recipe.getRecipePropertyStorage().getRecipePropertyValue(BlastTemperatureProperty.getInstance(), 0);
    }

    @Override
    public void preOverclock(OverclockManager<?> overclockManager, Recipe recipe) {
        long recipeEUt = overclockManager.getEUt() * 4;
        int duration = overclockManager.getDuration();
        int heat = this.blastFurnaceTemperature - recipe.getRecipePropertyStorage().getRecipePropertyValue(BlastTemperatureProperty.getInstance(), 0);
        // Apply EUt discount for every 900K above the base recipe temperature
        recipeEUt /= (long) Math.max(1.00, (1.00 + 0.05 * (heat / 900D)));
        while (duration > 1 && recipeEUt <= this.maxVoltage) {
            if (heat < 1800) break;
            heat -= 1800;
            duration /= 4;
            recipeEUt *= 4;
        }
        overclockManager.setParallel((int) Math.min(Integer.MAX_VALUE, 1L << overclockManager.getParallel() * 2));
        overclockManager.setEUtAndDuration(recipeEUt / 4, duration);
    }

    @Override
    protected void addDisplayText(GUIDisplayBuilder builder) {
        super.addDisplayText(builder);
        if (!this.isStructureFormed()) return;
        builder.addTranslationLine("gregtech.multiblock.blast_furnace.max_temperature", this.blastFurnaceTemperature)
                .addTranslationLine("gtadditions.multiblock.blast_furnace.additional_temperature", this.bonusTemperature);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("~~~XXXXX~~~", "~~~HHHHH~~~", "~~~GGGGG~~~", "~~~GGGGG~~~", "~~~GGGGG~~~", "~~~HHHHH~~~", "~~~CCCCC~~~", "~~~~~~~~~~~", "~~~~~~~~~~~", "~~~~~~~~~~~", "~~~~~~~~~~~", "~~~~~~~~~~~", "~~~~~~~~~~~", "~~~~~~~~~~~", "~~~~~~~~~~~", "~~~~~~~~~~~", "~~~~~~~~~~~", "~~~~~~~~~~~", "~~~~~~~~~~~", "~~~~~~~~~~~")
                .aisle("~~XXXXXXX~~", "~~H#####H~~", "~~G#####G~~", "~~G#####G~~", "~~G#####G~~", "~~H#####H~~", "~~C#####C~~", "~~~CCCCC~~~", "~~~GGGGG~~~", "~~~GGGGG~~~", "~~~GGGGG~~~", "~~~GGGGG~~~", "~~~GGGGG~~~", "~~~GGGGG~~~", "~~~GGGGG~~~", "~~~GGGGG~~~", "~~~GGGGG~~~", "~~~GGGGG~~~", "~~~GGGGG~~~", "~~~CCCCC~~~")
                .aisle("~XXXXXXXXX~", "~H#ccccc#H~", "~G#ccccc#G~", "~G#ccccc#G~", "~G#ccccc#G~", "~H#ccccc#H~", "~C#ccccc#C~", "~~CcccccC~~", "~~GcccccG~~", "~~GcccccG~~", "~~GcccccG~~", "~~GcccccG~~", "~~GcccccG~~", "~~GcccccG~~", "~~GcccccG~~", "~~GcccccG~~", "~~GcccccG~~", "~~GcccccG~~", "~~GcccccG~~", "~~CCCCCCC~~")
                .aisle("XXXXXXXXXXX", "H#c#####c#H", "G#c#####c#G", "G#c#####c#G", "G#c#####c#G", "H#c#####c#H", "C#c#####c#C", "~Cc#####cC~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~CCCCCCCCC~")
                .aisle("XXXXXFXXXXX", "H#c#####c#H", "G#c#####c#G", "G#c#####c#G", "G#c#####c#G", "H#c#####c#H", "C#c#####c#C", "~Cc#####cC~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~CCCCCCCCC~")
                .aisle("XXXXFFFXXXX", "H#c#####c#H", "G#c#####c#G", "G#c#####c#G", "G#c#####c#G", "H#c#####c#H", "C#c#####c#C", "~Cc#####cC~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~CCCCMCCCC~")
                .aisle("XXXXXFXXXXX", "H#c#####c#H", "G#c#####c#G", "G#c#####c#G", "G#c#####c#G", "H#c#####c#H", "C#c#####c#C", "~Cc#####cC~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~CCCCCCCCC~")
                .aisle("XXXXXXXXXXX", "H#c#####c#H", "G#c#####c#G", "G#c#####c#G", "G#c#####c#G", "H#c#####c#H", "C#c#####c#C", "~Cc#####cC~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~Gc#####cG~", "~CCCCCCCCC~")
                .aisle("~XXXXXXXXX~", "~H#ccccc#H~", "~G#ccccc#G~", "~G#ccccc#G~", "~G#ccccc#G~", "~H#ccccc#H~", "~C#ccccc#C~", "~~CcccccC~~", "~~GcccccG~~", "~~GcccccG~~", "~~GcccccG~~", "~~GcccccG~~", "~~GcccccG~~", "~~GcccccG~~", "~~GcccccG~~", "~~GcccccG~~", "~~GcccccG~~", "~~GcccccG~~", "~~GcccccG~~", "~~CCCCCCC~~")
                .aisle("~~XXXXXXX~~", "~~H#####H~~", "~~G#####G~~", "~~G#####G~~", "~~G#####G~~", "~~H#####H~~", "~~C#####C~~", "~~~CCCCC~~~", "~~~GGGGG~~~", "~~~GGGGG~~~", "~~~GGGGG~~~", "~~~GGGGG~~~", "~~~GGGGG~~~", "~~~GGGGG~~~", "~~~GGGGG~~~", "~~~GGGGG~~~", "~~~GGGGG~~~", "~~~GGGGG~~~", "~~~GGGGG~~~", "~~~CCCCC~~~")
                .aisle("~~~XXXXX~~~", "~~~HHHHH~~~", "~~~GXXXG~~~", "~~~GXSXG~~~", "~~~GXXXG~~~", "~~~HHHHH~~~", "~~~CCCCC~~~", "~~~~~~~~~~~", "~~~~~~~~~~~", "~~~~~~~~~~~", "~~~~~~~~~~~", "~~~~~~~~~~~", "~~~~~~~~~~~", "~~~~~~~~~~~", "~~~~~~~~~~~", "~~~~~~~~~~~", "~~~~~~~~~~~", "~~~~~~~~~~~", "~~~~~~~~~~~", "~~~~~~~~~~~")
                .where('S', this.selfPredicate())
                .where('C', statePredicate(this.getCasingState()))
                .where('X', statePredicate(this.getCasingState()).or(abilityPartPredicate(ALLOWED_ABILITIES)).or(multiiPartPredicate()))
                .where('G', statePredicate(GAMetaBlocks.TRANSPARENT_CASING.getState(GATransparentCasing.CasingType.OSMIRIDIUM_GLASS)))
                .where('M', abilityPartPredicate(MUFFLER_HATCH))
                .where('H', heatVentPredicate())
                .where('F', frameworkPredicate())
                .where('c', coilPredicate())
                .where('#', isAirPredicate())
                .where('~', tile -> true)
                .build();
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        final int tier = context.getOrDefault("frameworkTier", 0);
        if (tier < GAValues.MAX) {
            this.maxVoltage = 8L << tier * 2;
            this.tier = tier;
        }
        this.activeStates.addAll(context.getOrDefault("coilPos", new HashSet<>()));
        this.activeStates.addAll(context.getOrDefault("heatVents", new HashSet<>()));
        this.bonusTemperature = Math.max(0, 100 * (this.tier - 2));
        this.blastFurnaceTemperature = context.getOrDefault("coilTemperature", 0);
        this.blastFurnaceTemperature += this.bonusTemperature;
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.blastFurnaceTemperature = 0;
        this.bonusTemperature = 0;
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
                state = state.withProperty(BlockFireboxCasing.ACTIVE, isActive);
                this.getWorld().setBlockState(pos, state);
            } else if (block instanceof GAHeatingCoil) {
                state = state.withProperty(GAHeatingCoil.ACTIVE, isActive);
                this.getWorld().setBlockState(pos, state);
            } else if (block instanceof BlockActiveAbility) {
                state = state.withProperty(BlockActiveAbility.ACTIVE, isActive);
                this.getWorld().setBlockState(pos, state);
            }
        }
    }

    private void replaceCoilsAsActive(boolean isActive) {
        this.writeCustomData(128, buffer -> this.writeActiveBlockPacket(buffer, isActive));
    }

    @Override
    public void onRemoval() {
        super.onRemoval();
        if (!this.getWorld().isRemote) {
            this.replaceCoilsAsActive(false);
        }
    }

    public static Predicate<BlockWorldState> heatVentPredicate() {
        return blockWorldState -> {
            final IBlockState state = blockWorldState.getBlockState();
            if (state.getBlock() instanceof BlockActiveAbility) {
                if (!((BlockActiveAbility) state.getBlock()).getState(state).getName().equals("heat_vent"))
                    return false;
                blockWorldState.getMatchContext().getOrCreate("heatVents", HashSet::new).add(blockWorldState.getPos());
                return true;
            } else return false;
        };
    }

    private IBlockState getCasingState() {
        return GAMetaBlocks.METAL_CASING_1.getState(MetalCasing1.CasingType.ZIRCONIUM_CARBIDE);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return ClientHandler.ZIRCONIUM_CARBIDE_CASING;
    }

    @Override
    public int getParallel() {
        return Math.max(0, this.tier - GTValues.LuV);
    }
}
