package tj.mixin.gregicality;

import gregicadditions.GAValues;
import gregicadditions.machines.multi.TileEntityFusionReactor;
import gregtech.api.GTValues;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.impl.EnergyContainerHandler;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.multiblock.BlockWorldState;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.api.multiblock.PatternMatchContext;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import tj.blocks.EnergyPortCasings;
import tj.builder.multicontrollers.UIDisplayBuilder;
import tj.capability.IProgressBar;
import tj.capability.ProgressBar;
import tj.gui.TJGuiTextures;
import tj.mixin.gregtech.RecipeMapMultiblockControllerMixin;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

@Mixin(value = TileEntityFusionReactor.class, remap = false)
public abstract class TileEntityFusionReactorMixin extends RecipeMapMultiblockControllerMixin implements IProgressBar {

    @Shadow
    private EnergyContainerList inputEnergyContainers;

    @Shadow
    @Final
    private int tier;

    @Shadow
    private long heat;

    @Shadow
    public abstract long getHeat();

    public TileEntityFusionReactorMixin(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Redirect(method = "createStructurePattern", at = @At(value = "INVOKE", target = "Lgregtech/api/multiblock/FactoryBlockPattern;where(CLjava/util/function/Predicate;)Lgregtech/api/multiblock/FactoryBlockPattern;", ordinal = 4))
    private FactoryBlockPattern redirectCreateStructurePattern(FactoryBlockPattern pattern, char symbol, Predicate<BlockWorldState> blockMatcher) {
        pattern.where(symbol, blockMatcher.or(energyPortPredicate(this.tier)));
        return pattern;
    }

    @Inject(method = "formStructure", at = @At(value = "INVOKE", target = "Lgregicadditions/machines/multi/TileEntityFusionReactor;initializeAbilities()V"),
            locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
    private void injectFormStructure(PatternMatchContext context, CallbackInfo ci, long energyStored) {
        int energyPorts = context.getOrDefault("EnergyPort", new ArrayList<>()).size();
        this.inputInventory = new ItemHandlerList(this.getAbilities(MultiblockAbility.IMPORT_ITEMS));
        this.inputFluidInventory = new FluidTankList(true, this.getAbilities(MultiblockAbility.IMPORT_FLUIDS));
        this.outputInventory = new ItemHandlerList(this.getAbilities(MultiblockAbility.EXPORT_ITEMS));
        this.outputFluidInventory = new FluidTankList(true, this.getAbilities(MultiblockAbility.EXPORT_FLUIDS));
        List<IEnergyContainer> energyInputs = this.getAbilities(MultiblockAbility.INPUT_ENERGY);
        this.inputEnergyContainers = new EnergyContainerList(energyInputs);
        long euCapacity = (energyInputs.size() + energyPorts) * 10000000L * (long) Math.pow(2, this.tier - 6);
        this.energyContainer = new EnergyContainerHandler(this, euCapacity, GAValues.V[this.tier], 0, 0, 0) {
            @Override
            public String getName() {
                return "EnergyContainerInternal";
            }
        };
        ((EnergyContainerHandler) this.energyContainer).setEnergyStored(energyStored);
        ci.cancel();
    }

    @Override
    protected void configureDisplayText(UIDisplayBuilder builder) {
        super.configureDisplayText(builder);
        if (!this.isStructureFormed()) return;
        builder.addTextComponent(new TextComponentString("EU: " + this.energyContainer.getEnergyStored() + " / " + this.energyContainer.getEnergyCapacity()))
                .addTextComponent(new TextComponentTranslation("gtadditions.multiblock.fusion_reactor.heat", this.heat));
    }

    @Unique
    public Predicate<BlockWorldState> energyPortPredicate(int tier) {
        return (blockWorldState) -> {
            IBlockState blockState = blockWorldState.getBlockState();
            if (blockState.getBlock() instanceof EnergyPortCasings) {
                EnergyPortCasings abilityCasings = (EnergyPortCasings) blockState.getBlock();
                EnergyPortCasings.AbilityType tieredCasingType = abilityCasings.getState(blockState);
                List<EnergyPortCasings.AbilityType> currentCasing = blockWorldState.getMatchContext().getOrCreate("EnergyPort", ArrayList::new);
                currentCasing.add(tieredCasingType);
                return currentCasing.get(0).getName().equals(tieredCasingType.getName()) && currentCasing.get(0).getTier() >= tier;
            }
            return false;
        };
    }

    @Override
    public int[][] getBarMatrix() {
        return new int[1][1];
    }

    @Override
    public void getProgressBars(Queue<UnaryOperator<ProgressBar.ProgressBarBuilder>> bars) {
        bars.add(bar -> bar.setProgress(this::getHeat).setMaxProgress(() -> 160000000 << this.tier - GTValues.LuV)
                .setLocale("tj.multiblock.bars.heat")
                .setBarTexture(TJGuiTextures.BAR_RED));
    }
}
