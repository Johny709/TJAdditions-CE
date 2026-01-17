package tj.mixin.gregicality;

import gregicadditions.GAUtility;
import gregicadditions.GAValues;
import gregicadditions.capabilities.GAEnergyContainerHandler;
import gregicadditions.item.fusion.GAFusionCasing;
import gregicadditions.machines.multi.advance.MetaTileEntityAdvFusionReactor;
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
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import tj.blocks.AdvEnergyPortCasings;
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

@Mixin(value = MetaTileEntityAdvFusionReactor.class, remap = false)
public abstract class MetaTileEntityAdvFusionReactorMixin extends RecipeMapMultiblockControllerMixin implements IProgressBar {

    @Shadow
    private EnergyContainerList inputEnergyContainers;

    @Shadow
    private boolean canWork;

    @Shadow
    private long heat;

    @Shadow
    private int tier;

    public MetaTileEntityAdvFusionReactorMixin(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Shadow
    public abstract void invalidateStructure();

    @Redirect(method = "createStructurePattern", at = @At(value = "INVOKE", target = "Lgregtech/api/multiblock/FactoryBlockPattern;where(CLjava/util/function/Predicate;)Lgregtech/api/multiblock/FactoryBlockPattern;", ordinal = 4))
    private FactoryBlockPattern redirectCreateStructurePattern_divertor(FactoryBlockPattern pattern, char symbol, Predicate<BlockWorldState> blockMatcher) {
        pattern.where(symbol, blockMatcher.or(this.energyPortPredicate()));
        return pattern;
    }

    @Redirect(method = "createStructurePattern", at = @At(value = "INVOKE", target = "Lgregtech/api/multiblock/FactoryBlockPattern;where(CLjava/util/function/Predicate;)Lgregtech/api/multiblock/FactoryBlockPattern;", ordinal = 5))
    private FactoryBlockPattern redirectCreateStructurePattern_vacuum(FactoryBlockPattern pattern, char symbol, Predicate<BlockWorldState> blockMatcher) {
        pattern.where(symbol, blockMatcher.or(this.energyPortPredicate()));
        return pattern;
    }

    @Redirect(method = "createStructurePattern", at = @At(value = "INVOKE", target = "Lgregtech/api/multiblock/FactoryBlockPattern;where(CLjava/util/function/Predicate;)Lgregtech/api/multiblock/FactoryBlockPattern;", ordinal = 6))
    private FactoryBlockPattern redirectCreateStructurePattern_cryostat(FactoryBlockPattern pattern, char symbol, Predicate<BlockWorldState> blockMatcher) {
        pattern.where(symbol, blockMatcher.or(this.energyPortPredicate()));
        return pattern;
    }

    @Redirect(method = "createStructurePattern", at = @At(value = "INVOKE", target = "Lgregtech/api/multiblock/FactoryBlockPattern;where(CLjava/util/function/Predicate;)Lgregtech/api/multiblock/FactoryBlockPattern;", ordinal = 7))
    private FactoryBlockPattern redirectCreateStructurePattern_blanket(FactoryBlockPattern pattern, char symbol, Predicate<BlockWorldState> blockMatcher) {
        pattern.where(symbol, blockMatcher.or(this.energyPortPredicate()));
        return pattern;
    }

    @Inject(method = "formStructure", at = @At(value = "INVOKE", target = "Lgregicadditions/machines/multi/advance/MetaTileEntityAdvFusionReactor;initializeAbilities()V"),
        locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
    private void injectFormStructure(PatternMatchContext context, CallbackInfo ci, GAFusionCasing.CasingType coil, int cryostatTier, long energyStored) {
        this.inputInventory = new ItemHandlerList(this.getAbilities(MultiblockAbility.IMPORT_ITEMS));
        this.inputFluidInventory = new FluidTankList(true, this.getAbilities(MultiblockAbility.IMPORT_FLUIDS));
        this.outputInventory = new ItemHandlerList(this.getAbilities(MultiblockAbility.EXPORT_ITEMS));
        this.outputFluidInventory = new FluidTankList(true, this.getAbilities(MultiblockAbility.EXPORT_FLUIDS));
        List<IEnergyContainer> energyInputs = this.getAbilities(MultiblockAbility.INPUT_ENERGY);
        List<AdvEnergyPortCasings.AbilityType> energyPorts = context.getOrDefault("EnergyPort", new ArrayList<>());
        this.inputEnergyContainers = new EnergyContainerList(energyInputs);
        long euCapacity = 0, count = 0;
        for (IEnergyContainer energyContainer : energyInputs) {
            euCapacity += 10000000L * (long) Math.pow(2, Math.min(GAUtility.getTierByVoltage(energyContainer.getInputVoltage()), this.tier) - GAValues.LuV);
            count++;
        }
        for (AdvEnergyPortCasings.AbilityType energyPort : energyPorts) {
            euCapacity += 10000000L * (long) Math.pow(2, Math.min(energyPort.getTier(), this.tier) - GAValues.LuV);
            count++;
        }
        if (count > 16) {
            this.invalidateStructure();
            ci.cancel();
            return;
        }
        this.energyContainer = new GAEnergyContainerHandler(this, euCapacity, GAValues.V[this.tier], 0, 0, 0) {
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
        if (!this.canWork)
            builder.addTextComponent(new TextComponentTranslation("gregicality.multiblock.invalid_configuraion.1").setStyle(new Style().setColor(TextFormatting.RED)))
                    .addTextComponent(new TextComponentTranslation("gregicality.multiblock.invalid_configuraion.2"));
        builder.addTextComponent(new TextComponentString("EU: " + this.energyContainer.getEnergyStored() + " / " + this.energyContainer.getEnergyCapacity()))
                .addTextComponent(new TextComponentTranslation("gtadditions.multiblock.fusion_reactor.heat", this.heat));
    }

    @Unique
    public Predicate<BlockWorldState> energyPortPredicate() {
        return (blockWorldState) -> {
            IBlockState blockState = blockWorldState.getBlockState();
            if (blockState.getBlock() instanceof AdvEnergyPortCasings) {
                AdvEnergyPortCasings abilityCasings = (AdvEnergyPortCasings) blockState.getBlock();
                AdvEnergyPortCasings.AbilityType tieredCasingType = abilityCasings.getState(blockState);
                List<AdvEnergyPortCasings.AbilityType> currentCasing = blockWorldState.getMatchContext().getOrCreate("EnergyPort", ArrayList::new);
                currentCasing.add(tieredCasingType);
                return currentCasing.get(0).getName().equals(tieredCasingType.getName());
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
        bars.add(bar -> bar.setProgress(() -> this.heat).setMaxProgress(() -> 1600000000 << this.tier - GAValues.UHV)
                .setBarTexture(TJGuiTextures.BAR_RED)
                .setLocale("tj.multiblock.bars.heat"));
    }
}
