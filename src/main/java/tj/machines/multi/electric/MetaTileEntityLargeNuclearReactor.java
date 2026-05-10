package tj.machines.multi.electric;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import gregicadditions.GAUtility;
import gregicadditions.client.ClientHandler;
import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.GAMultiblockCasing;
import gregicadditions.item.metal.NuclearCasing;
import gregicadditions.recipes.GARecipeMaps;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.api.multiblock.PatternMatchContext;
import gregtech.api.recipes.Recipe;
import gregtech.api.render.ICubeRenderer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tj.TJConfig;
import tj.TJValues;
import tj.builder.multicontrollers.GUIDisplayBuilder;
import tj.builder.multicontrollers.TJMultiRecipeMapMultiblockController;
import tj.capability.OverclockManager;
import tj.util.TextUtils;

import javax.annotation.Nullable;
import java.util.List;

import static gregicadditions.capabilities.GregicAdditionsCapabilities.MAINTENANCE_HATCH;
import static gregicadditions.machines.multi.simple.LargeSimpleRecipeMapMultiblockController.fieldGenPredicate;
import static gregtech.api.metatileentity.multiblock.MultiblockAbility.*;
import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;
import static tj.capability.TJMultiblockDataCodes.PARALLEL_LAYER;
import static tj.machines.multi.electric.MetaTileEntityLargeGreenhouse.glassPredicate;

public class MetaTileEntityLargeNuclearReactor extends TJMultiRecipeMapMultiblockController {

    private static final MultiblockAbility<?>[] ALLOWED_ABILITIES = {IMPORT_ITEMS, EXPORT_ITEMS, IMPORT_FLUIDS, EXPORT_FLUIDS, INPUT_ENERGY, MAINTENANCE_HATCH};

    private int parallelLayer = 1;

    public MetaTileEntityLargeNuclearReactor(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, GARecipeMaps.NUCLEAR_REACTOR_RECIPES, GARecipeMaps.NUCLEAR_BREEDER_RECIPES);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityLargeNuclearReactor(this.metaTileEntityId);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("tj.multiblock.parallel.extend.tooltip"));
        tooltip.add(I18n.format("tj.multiblock.processing_array.eut"));
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("tj.multiblock.universal.tooltip.2", TJValues.thousandFormat.format(TJConfig.largeNuclearReactor.maximumSlices)));
    }

    @Override
    public void preOverclock(OverclockManager<?> overclockManager, Recipe recipe) {
        overclockManager.setChanceMultiplier(this.getChanceMultiplier());
        overclockManager.setEUt(overclockManager.getEUt() * this.getEUtMultiplier() / 100);
        overclockManager.setDuration(overclockManager.getDuration() * this.getDurationMultiplier() / 100);
        overclockManager.setParallel(this.parallelLayer);
    }

    @Override
    public void postOverclock(OverclockManager<?> overclockManager, Recipe recipe) {
        overclockManager.setEUt(overclockManager.getEUt() * overclockManager.getParallelsPerformed());
    }

    @Override
    protected void addDisplayText(GUIDisplayBuilder builder) {
        super.addDisplayText(builder);
        builder.addTranslationLine("tj.multiblock.slices", TJValues.thousandFormat.format(this.parallelLayer));
    }

    @Override
    protected BlockPattern createStructurePattern() {
        final FactoryBlockPattern factoryPattern = FactoryBlockPattern.start(RIGHT, FRONT, DOWN)
                .aisle("~~~~~C~~~~~", "~~~~GCG~~~~", "~~CCCCCCC~~", "~~CCCCCCC~~", "~GCCCCCCCG~", "CCCCCCCCCCC", "~GCCCCCCCG~", "~~CCCCCCC~~", "~~CCCCCCC~~", "~~~~GCG~~~~", "~~~~~C~~~~~");
        for (int layer = 0; layer < this.parallelLayer; layer++) {
            final String controllerS = layer == this.parallelLayer - 1 ? "~~~~~S~~~~~" : "~~~~~X~~~~~";
            factoryPattern.aisle(controllerS, "~~~~GNG~~~~", "~~XX###XX~~", "~~XR#A#MX~~", "~G#######G~", "XB#P#g#L#TX", "~G#######G~", "~~XE#U#cX~~", "~~XX###XX~~", "~~~~GFG~~~~", "~~~~~X~~~~~");
        }
        return factoryPattern.aisle("~~~~~C~~~~~", "~~~~GCG~~~~", "~~CCCCCCC~~", "~~CCCCCCC~~", "~GCCCCCCCG~", "CCCCCCCCCCC", "~GCCCCCCCG~", "~~CCCCCCC~~", "~~CCCCCCC~~", "~~~~GCG~~~~", "~~~~~C~~~~~")
                .where('S', this.selfPredicate())
                .where('C', statePredicate(this.getCasingState()))
                .where('X', statePredicate(this.getCasingState()).or(abilityPartPredicate(ALLOWED_ABILITIES)))
                .where('N', statePredicate(GAMetaBlocks.NUCLEAR_CASING.getState(NuclearCasing.CasingType.NEPTUNIUM)))
                .where('R', statePredicate(GAMetaBlocks.NUCLEAR_CASING.getState(NuclearCasing.CasingType.CURIUM)))
                .where('A', statePredicate(GAMetaBlocks.NUCLEAR_CASING.getState(NuclearCasing.CasingType.AMERICIUM)))
                .where('M', statePredicate(GAMetaBlocks.NUCLEAR_CASING.getState(NuclearCasing.CasingType.MENDELEVIUM)))
                .where('B', statePredicate(GAMetaBlocks.NUCLEAR_CASING.getState(NuclearCasing.CasingType.BERKELIUM)))
                .where('P', statePredicate(GAMetaBlocks.NUCLEAR_CASING.getState(NuclearCasing.CasingType.PROTACTINIUM)))
                .where('L', statePredicate(GAMetaBlocks.NUCLEAR_CASING.getState(NuclearCasing.CasingType.PLUTONIUM)))
                .where('T', statePredicate(GAMetaBlocks.NUCLEAR_CASING.getState(NuclearCasing.CasingType.THORIUM)))
                .where('E', statePredicate(GAMetaBlocks.NUCLEAR_CASING.getState(NuclearCasing.CasingType.EINSTEINIUM)))
                .where('U', statePredicate(GAMetaBlocks.NUCLEAR_CASING.getState(NuclearCasing.CasingType.URANIUM)))
                .where('c', statePredicate(GAMetaBlocks.NUCLEAR_CASING.getState(NuclearCasing.CasingType.CALIFORNIUM)))
                .where('F', statePredicate(GAMetaBlocks.NUCLEAR_CASING.getState(NuclearCasing.CasingType.FERMIUM)))
                .where('G', glassPredicate())
                .where('g', fieldGenPredicate())
                .where('#', isAirPredicate())
                .where('~', tile -> true)
                .build();
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
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
    public boolean onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        if (!this.getWorld().isRemote) {
            final int lastParallelLayer = this.parallelLayer;
            this.parallelLayer = MathHelper.clamp(playerIn.isSneaking() ? this.parallelLayer - 1 : this.parallelLayer + 1, 1, TJConfig.largeNuclearReactor.maximumSlices);
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
    protected void reinitializeStructurePattern() {
        this.parallelLayer = 1;
        super.reinitializeStructurePattern();
    }

    private void resetStructure() {
        if (this.isStructureFormed())
            this.invalidateStructure();
        this.structurePattern = this.createStructurePattern();
    }

    private IBlockState getCasingState() {
        return GAMetaBlocks.MUTLIBLOCK_CASING.getState(GAMultiblockCasing.CasingType.CLADDED_REACTOR_CASING);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return ClientHandler.CLADDED_REACTOR_CASING;
    }

    @Override
    public int getEUtMultiplier() {
        return TJConfig.largeNuclearReactor.eutPercentage;
    }

    @Override
    public int getDurationMultiplier() {
        return TJConfig.largeNuclearReactor.durationPercentage;
    }

    @Override
    public int getChanceMultiplier() {
        return TJConfig.largeNuclearReactor.chancePercentage;
    }

    @Override
    public int getParallel() {
        return 0; // don't display parallel overclocking per tier on tooltip
    }
}
