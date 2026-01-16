package tj.mixin.gregicality;

import gregicadditions.GAUtility;
import gregicadditions.GAValues;
import gregicadditions.Gregicality;
import gregicadditions.machines.multi.drill.MetaTileEntityFluidDrillingPlant;
import gregicadditions.worldgen.PumpjackHandler;
import gregtech.api.capability.IEnergyContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import tj.builder.multicontrollers.UIDisplayBuilder;
import tj.capability.IProgressBar;
import tj.capability.ProgressBar;
import tj.mixin.gregtech.MultiblockWithDisplayBaseMixin;

import java.util.Queue;
import java.util.function.UnaryOperator;

@Mixin(value = MetaTileEntityFluidDrillingPlant.class, remap = false)
public abstract class MetaTileEntityFluidDrillingPlantMixin extends MultiblockWithDisplayBaseMixin implements IProgressBar {

    @Shadow
    private IEnergyContainer energyContainer;

    @Shadow
    private int[] currentLocation;

    @Shadow
    @Final
    private int rigTier;

    @Shadow
    private Fluid veinFluid;

    public MetaTileEntityFluidDrillingPlantMixin(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Shadow
    public abstract int getAvailableFluidAmount();

    @Shadow
    public abstract int getResidualFluidAmount();

    @Shadow
    protected abstract int overclockFluidProduction();

    @Shadow
    public abstract int getFluidAmountToDrain(int fluidAmount);

    @Shadow
    public abstract int getFluidAmountForUse(int fluidAmount);

    @Shadow
    public abstract int getVoltageTier();

    @Override
    protected void configureDisplayText(UIDisplayBuilder builder) {
        super.configureDisplayText(builder);
        if (!this.isStructureFormed()) return;
        PumpjackHandler.OilWorldInfo oilWorldInfo = PumpjackHandler.getOilWorldInfo(getWorld(), this.currentLocation[0], this.currentLocation[1]);
        if (this.energyContainer != null && this.energyContainer.getEnergyCapacity() > 0) {
            long maxVoltage = this.energyContainer.getInputVoltage();
            String voltageName = GAValues.VN[GAUtility.getTierByVoltage(maxVoltage)];
            builder.addTextComponent(new TextComponentTranslation("gregtech.multiblock.max_energy_per_tick", maxVoltage, voltageName));
        }
        if (oilWorldInfo == null || oilWorldInfo.getType() == null) {
            builder.addTextComponent(new TextComponentTranslation("gtadditions.multiblock.drilling_rig.no_fluid").setStyle(new Style().setColor(TextFormatting.RED)));
        } else {
            builder.addTextComponent(new TextComponentTranslation("gtadditions.multiblock.drilling_rig.rig_production", this.getAvailableFluidAmount() <= 0 ? this.getResidualFluidAmount() * this.rigTier : this.overclockFluidProduction()));
            if (this.rigTier != 4) {
                builder.addTextComponent(new TextComponentTranslation("gtadditions.multiblock.drilling_rig.fluid_drain", this.getFluidAmountToDrain(this.getFluidAmountForUse(this.getAvailableFluidAmount()))));
            }
            ITextComponent fluidName = new TextComponentTranslation(oilWorldInfo.getType().getFluid().getUnlocalizedName());
            builder.addTextComponent(new TextComponentTranslation("gtadditions.multiblock.drilling_rig.fluid", fluidName))
                    .addTextComponent(new TextComponentTranslation("gtadditions.multiblock.drilling_rig.chunk_capacity", oilWorldInfo.capacity / 1000))
                    .addTextComponent(new TextComponentTranslation("gtadditions.multiblock.drilling_rig.chunk_remaining", oilWorldInfo.current / 1000))
                    .addTextComponent(new TextComponentTranslation("gtadditions.multiblock.drilling_rig.replenish", oilWorldInfo.type.replenishRate * this.getVoltageTier()));
        }
    }

    @Override
    public String getJEIRecipeUid() {
        return Gregicality.MODID + ":drilling_rig";
    }

    @Override
    public int[][] getBarMatrix() {
        return new int[1][1];
    }

    @Override
    public void getProgressBars(Queue<UnaryOperator<ProgressBar.ProgressBarBuilder>> bars) {
        bars.add(bar -> bar.setProgress(this::getVeinStackAmount).setMaxProgress(this::getVeinStackCapacity)
                .setLocale("tj.multiblock.bars.fluid").setParams(() -> new Object[]{this.veinFluid != null ? this.veinFluid.getUnlocalizedName() : ""})
                .setFluidStackSupplier(() -> this.veinFluid != null ? new FluidStack(this.veinFluid, 1) : null));
    }

    @Unique
    private long getVeinStackAmount() {
        if (this.currentLocation == null)
            return 0;
        PumpjackHandler.OilWorldInfo oilWorldInfo = PumpjackHandler.getOilWorldInfo(getWorld(), this.currentLocation[0], this.currentLocation[1]);
        return oilWorldInfo != null ? oilWorldInfo.current : 0;
    }

    @Unique
    private long getVeinStackCapacity() {
        if (this.currentLocation == null)
            return 0;
        PumpjackHandler.OilWorldInfo oilWorldInfo = PumpjackHandler.getOilWorldInfo(getWorld(), this.currentLocation[0], this.currentLocation[1]);
        return oilWorldInfo != null ? oilWorldInfo.capacity : 0;
    }
}
