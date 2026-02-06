package tj.machines.multi.parallel;

import gregicadditions.GAUtility;
import gregicadditions.client.ClientHandler;
import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.metal.MetalCasing1;
import gregicadditions.item.metal.MetalCasing2;
import gregicadditions.machines.GATileEntities;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.api.multiblock.PatternMatchContext;
import gregtech.api.render.ICubeRenderer;
import gregtech.api.render.OrientedOverlayRenderer;
import gregtech.api.render.Textures;
import gregtech.common.blocks.BlockBoilerCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tj.TJConfig;
import tj.capability.impl.workable.ParallelElectricBlastFurnaceRecipeLogic;
import tj.builder.multicontrollers.ParallelRecipeMapMultiblockController;
import tj.builder.multicontrollers.UIDisplayBuilder;
import tj.util.TooltipHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static gregicadditions.capabilities.GregicAdditionsCapabilities.MAINTENANCE_HATCH;
import static gregicadditions.capabilities.GregicAdditionsCapabilities.MUFFLER_HATCH;
import static gregtech.api.metatileentity.multiblock.MultiblockAbility.*;
import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;
import static tj.machines.multi.electric.MetaTileEntityLargeAlloySmelter.heatingCoilPredicate;
import static tj.machines.multi.electric.MetaTileEntityLargeAlloySmelter.heatingCoilPredicate2;
import static tj.multiblockpart.TJMultiblockAbility.REDSTONE_CONTROLLER;


public class MetaTileEntityParallelAlloyBlastSmelter extends ParallelRecipeMapMultiblockController {

    private int blastFurnaceTemperature;
    private int bonusTemperature;
    private static final MultiblockAbility<?>[] ALLOWED_ABILITIES = {IMPORT_ITEMS, IMPORT_FLUIDS, EXPORT_FLUIDS, INPUT_ENERGY, MAINTENANCE_HATCH, REDSTONE_CONTROLLER};

    public MetaTileEntityParallelAlloyBlastSmelter(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, GATileEntities.ALLOY_BLAST_FURNACE.recipeMap);
        this.recipeMapWorkable = new ParallelElectricBlastFurnaceRecipeLogic(this, this::getBlastFurnaceTemperature);
        this.recipeMapWorkable.setMaxVoltage(this::getMaxVoltage);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityParallelAlloyBlastSmelter(this.metaTileEntityId);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("tj.multiblock.parallel_alloy_blast_smelter.description"));
        tooltip.add(I18n.format("tj.multiblock.parallel.description"));
        TooltipHelper.shiftText(tooltip, tip -> {
            tip.add(I18n.format("tj.multiblock.parallel.extend.tooltip"));
            tip.add(I18n.format("gtadditions.multiblock.electric_blast_furnace.tooltip.1"));
            tip.add(I18n.format("gtadditions.multiblock.electric_blast_furnace.tooltip.2"));
            tip.add(I18n.format("gtadditions.multiblock.electric_blast_furnace.tooltip.3"));
            super.addInformation(stack, player, tip, advanced);
        });
    }

    @Override
    protected void addDisplayText(UIDisplayBuilder builder) {
        super.addDisplayText(builder);
        if (!this.isStructureFormed()) return;
        builder.addTextComponent(new TextComponentTranslation("gregtech.multiblock.blast_furnace.max_temperature", this.blastFurnaceTemperature))
                .addTextComponent(new TextComponentTranslation("gtadditions.multiblock.blast_furnace.additional_temperature", this.bonusTemperature));

    }

    @Override
    protected BlockPattern createStructurePattern() {
        FactoryBlockPattern factoryPattern = FactoryBlockPattern.start(RIGHT, FRONT, DOWN);
        for (int layer = 0; layer < this.parallelLayer; layer++) {
            if (layer % 4 == 0) {
                String mufflerMM = layer == 0 ? "XXXMXXX" : "XXXPXXX";
                factoryPattern.aisle("~XXXXX~", "XXXXXXX", "XXXXXXX", mufflerMM, "XXXXXXX", "XXXXXXX", "~XXXXX~");
                factoryPattern.aisle("~AAAAA~", "AcccccA", "Ac#c#cA", "AccPccA", "Ac#c#cA", "AcccccA", "~AAAAA~");
                factoryPattern.aisle("~AAAAA~", "AcccccA", "Ac#c#cA", "AccPccA", "Ac#c#cA", "AcccccA", "~AAAAA~");
            }
        }
        return factoryPattern.aisle("~XXSXX~", "XXXXXXX", "XXXXXXX", "XXXXXXX", "XXXXXXX", "XXXXXXX", "~XXXXX~")
                .setAmountAtLeast('L', 22)
                .where('S', this.selfPredicate())
                .where('L', statePredicate(this.getCasingState()))
                .where('X', statePredicate(this.getCasingState()).or(abilityPartPredicate(ALLOWED_ABILITIES)))
                .where('A', statePredicate(GAMetaBlocks.METAL_CASING_2.getState(MetalCasing2.CasingType.STABALLOY)))
                .where('P', statePredicate(MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.TUNGSTENSTEEL_PIPE)))
                .where('c', heatingCoilPredicate().or(heatingCoilPredicate2()))
                .where('#', isAirPredicate())
                .where('M', abilityPartPredicate(MUFFLER_HATCH))
                .where('~', tile -> true)
                .build();
    }

    private IBlockState getCasingState() {
        return GAMetaBlocks.METAL_CASING_1.getState(MetalCasing1.CasingType.ZIRCONIUM_CARBIDE);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return ClientHandler.ZIRCONIUM_CARBIDE_CASING;
    }

    @Nonnull
    @Override
    protected OrientedOverlayRenderer getFrontOverlay() {
        return Textures.PRIMITIVE_BLAST_FURNACE_OVERLAY;
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
                .sum() / this.parallelLayer;
        amps = Math.min(1024, amps);
        while (amps >= 4) {
            amps /= 4;
            this.maxVoltage *= 4;
        }
        int energyTier = GAUtility.getTierByVoltage(this.maxVoltage);
        this.bonusTemperature = Math.max(0, 100 * (energyTier - 2));
        this.blastFurnaceTemperature = context.getOrDefault("blastFurnaceTemperature", 0);
        this.blastFurnaceTemperature += this.bonusTemperature;
    }

    public int getBlastFurnaceTemperature() {
        return this.blastFurnaceTemperature;
    }

    @Override
    public int getMaxParallel() {
        return TJConfig.parallelAlloyBlastSmelter.maximumParallel;
    }
}
