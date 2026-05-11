package tj.machines.multi.electric;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import gregicadditions.GAMaterials;
import gregicadditions.GAUtility;
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
import gregtech.api.render.Textures;
import gregtech.api.unification.material.Materials;
import gregtech.common.blocks.BlockBoilerCasing;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.items.MetaItems;
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
import tj.builder.multicontrollers.TJRecipeMapMultiblockController;
import tj.capability.OverclockManager;
import tj.util.TextUtils;

import javax.annotation.Nullable;
import java.util.List;

import static gregicadditions.capabilities.GregicAdditionsCapabilities.MAINTENANCE_HATCH;
import static gregtech.api.metatileentity.multiblock.MultiblockAbility.*;
import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;
import static tj.capability.TJMultiblockDataCodes.PARALLEL_LAYER;

public class MetaTileEntityLargeGasCentrifuge extends TJRecipeMapMultiblockController {

    private static final MultiblockAbility<?>[] ALLOWED_ABILITIES = {IMPORT_ITEMS, IMPORT_FLUIDS, EXPORT_FLUIDS, INPUT_ENERGY, MAINTENANCE_HATCH};

    private int parallelLayer = 1;

    public MetaTileEntityLargeGasCentrifuge(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, GARecipeMaps.GAS_CENTRIFUGE_RECIPES);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityLargeGasCentrifuge(this.metaTileEntityId);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("tj.multiblock.parallel.extend.tooltip"));
        tooltip.add(I18n.format("tj.multiblock.processing_array.eut"));
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("tj.multiblock.universal.tooltip.2", TJValues.thousandFormat.format(TJConfig.largeGasCentrifuge.maximumSlices)));
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
        final FactoryBlockPattern factoryPattern = FactoryBlockPattern.start(RIGHT, UP, BACK)
                .aisle("~XXX~", "~XXX~", "~T~T~", "~p~p~", "~U~U~", "~N~N~", "~l~l~", "~A~A~", "~R~R~", "~B~B~", "~c~c~", "~E~E~", "~F~F~", "~M~M~");
        for (int layer = 1; layer < this.parallelLayer; layer++) {
            factoryPattern.aisle("XXXXX", "XCCCX", "pPOPp", "UPUPU", "NPNPN", "NPNPN", "lPlPl", "APAPA", "RPRPR", "BPBPB", "cPcPc", "EPEPE", "FPFPF", "MPMPM");
            factoryPattern.aisle("XXXXX", "XCCCX", "~O~O~", "~U~U~", "~N~N~", "~N~N~", "~l~l~", "~A~A~", "~R~R~", "~B~B~", "~c~c~", "~E~E~", "~F~F~", "~M~M~");
        }
        return factoryPattern
                .aisle("XXXXX", "XCCCX", "NPOPN", "NPNPN", "UPNPU", "NPNPN", "lPlPl", "APAPA", "RPRPR", "BPBPB", "cPcPc", "EPEPE", "FPFPF", "MPMPM")
                .aisle("~XXX~", "~XSX~", "~T~T~", "~p~p~", "~U~U~", "~N~N~", "~l~l~", "~A~A~", "~R~R~", "~B~B~", "~c~c~", "~E~E~", "~F~F~", "~M~M~")
                .setAmountAtLeast('L', 5)
                .where('S', this.selfPredicate())
                .where('L', statePredicate(this.getCasingState()))
                .where('C', statePredicate(this.getCasingState()))
                .where('X', statePredicate(this.getCasingState()).or(abilityPartPredicate(ALLOWED_ABILITIES)))
                .where('O', statePredicate(MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID)))
                .where('P', statePredicate(MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.STEEL_PIPE)))
                .where('T', statePredicate(MetaBlocks.FRAMES.get(Materials.Thorium).getDefaultState()))
                .where('p', statePredicate(MetaBlocks.FRAMES.get(GAMaterials.Protactinium.getMaterial()).getDefaultState()))
                .where('U', statePredicate(MetaBlocks.FRAMES.get(GAMaterials.UraniumRadioactive.getMaterial()).getDefaultState()))
                .where('N', statePredicate(MetaBlocks.FRAMES.get(GAMaterials.Neptunium.getMaterial()).getDefaultState()))
                .where('l', statePredicate(MetaBlocks.FRAMES.get(GAMaterials.PlutoniumRadioactive.getMaterial()).getDefaultState()))
                .where('A', statePredicate(MetaBlocks.FRAMES.get(GAMaterials.AmericiumRadioactive.getMaterial()).getDefaultState()))
                .where('R', statePredicate(MetaBlocks.FRAMES.get(GAMaterials.Curium.getMaterial()).getDefaultState()))
                .where('B', statePredicate(MetaBlocks.FRAMES.get(GAMaterials.Berkelium.getMaterial()).getDefaultState()))
                .where('c', statePredicate(MetaBlocks.FRAMES.get(GAMaterials.Californium.getMaterial()).getDefaultState()))
                .where('E', statePredicate(MetaBlocks.FRAMES.get(GAMaterials.Einsteinium.getMaterial()).getDefaultState()))
                .where('F', statePredicate(MetaBlocks.FRAMES.get(GAMaterials.Fermium.getMaterial()).getDefaultState()))
                .where('M', statePredicate(MetaBlocks.FRAMES.get(GAMaterials.Mendelevium.getMaterial()).getDefaultState()))
                .where('~', tile -> true)
                .build();
    }

    private IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STAINLESS_CLEAN);
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
    public boolean onRightClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        return !MetaItems.SCREWDRIVER.isItemEqual(playerIn.getHeldItem(hand)) && super.onRightClick(playerIn, hand, facing, hitResult);
    }

    @Override
    public boolean onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        if (!this.getWorld().isRemote) {
            final int lastParallelLayer = this.parallelLayer;
            this.parallelLayer = MathHelper.clamp(playerIn.isSneaking() ? this.parallelLayer - 1 : this.parallelLayer + 1, 1, TJConfig.largeGasCentrifuge.maximumSlices);
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
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.CLEAN_STAINLESS_STEEL_CASING;
    }

    @Override
    public int getEUtMultiplier() {
        return TJConfig.largeGasCentrifuge.eutPercentage;
    }

    @Override
    public int getDurationMultiplier() {
        return TJConfig.largeGasCentrifuge.durationPercentage;
    }

    @Override
    public int getChanceMultiplier() {
        return TJConfig.largeGasCentrifuge.chancePercentage;
    }

    @Override
    public int getParallel() {
        return 0; // don't display parallel overclocking per tier on tooltip
    }

    private void resetStructure() {
        if (this.isStructureFormed())
            this.invalidateStructure();
        this.structurePattern = this.createStructurePattern();
    }

    @Override
    protected void reinitializeStructurePattern() {
        this.parallelLayer = 1;
        super.reinitializeStructurePattern();
    }
}
