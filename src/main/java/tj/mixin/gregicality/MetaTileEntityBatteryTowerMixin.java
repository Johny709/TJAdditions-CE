package tj.mixin.gregicality;

import gregicadditions.GAConfig;
import gregicadditions.machines.multi.MetaTileEntityBatteryTower;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tj.builder.multicontrollers.UIDisplayBuilder;
import tj.capability.IProgressBar;
import tj.capability.ProgressBar;

import java.util.Queue;
import java.util.function.UnaryOperator;

@Mixin(value = MetaTileEntityBatteryTower.class, remap = false)
public abstract class MetaTileEntityBatteryTowerMixin extends GAMultiblockWithDisplayBaseMixin implements IProgressBar {

    @Shadow
    private long passiveDrain;

    public MetaTileEntityBatteryTowerMixin(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Shadow
    public abstract long getEnergyStored();

    @Shadow
    public abstract long getEnergyCapacity();

    @Shadow
    public abstract long getEnergyOutputPerTick();

    @Shadow
    public abstract long getEnergyInputPerTick();

    @Override
    protected void configureDisplayText(UIDisplayBuilder builder) {
        super.configureDisplayText(builder);
        if (!this.isStructureFormed()) return;
        builder.addTextComponent(new TextComponentTranslation("gregtech.multiblock.universal.energy_store", String.format("%,d", this.getEnergyStored()), String.format("%,d", getEnergyCapacity())));
        builder.addTextComponent(new TextComponentTranslation("gtadditions.multiblock.battery_tower.input", String.format("%,d", this.getEnergyInputPerTick())));
        builder.addTextComponent(new TextComponentTranslation("gtadditions.multiblock.battery_tower.output", String.format("%,d", this.getEnergyOutputPerTick())));
        if (GAConfig.multis.batteryTower.lossPercentage != 0)
            builder.addTextComponent(new TextComponentTranslation("gtadditions.multiblock.battery_tower.passive_drain", String.format("%,d", this.passiveDrain)));
    }

    @Override
    public int[][] getBarMatrix() {
        return new int[1][1];
    }

    @Override
    public void getProgressBars(Queue<UnaryOperator<ProgressBar.ProgressBarBuilder>> bars) {
        bars.add(bar -> bar.setProgress(this::getEnergyStored).setMaxProgress(this::getEnergyCapacity)
                .setLocale("tj.multiblock.bars.energy")
                .setColor(0xFFF6FF00));
    }
}
