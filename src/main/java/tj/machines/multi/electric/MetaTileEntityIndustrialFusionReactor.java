package tj.machines.multi.electric;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import gregicadditions.GAValues;
import gregicadditions.client.ClientHandler;
import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.fusion.GAFusionCasing;
import gregicadditions.machines.multi.simple.LargeSimpleRecipeMapMultiblockController;
import gregicadditions.utils.GALog;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.EnergyContainerHandler;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.gui.Widget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.BlockWorldState;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.api.multiblock.PatternMatchContext;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.recipeproperties.FusionEUToStartProperty;
import gregtech.api.render.ICubeRenderer;
import gregtech.api.render.OrientedOverlayRenderer;
import gregtech.common.blocks.BlockMultiblockCasing;
import gregtech.common.blocks.BlockWireCoil;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.items.MetaItems;
import gregtech.common.metatileentities.electric.multiblockpart.MetaTileEntityMultiblockPart;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.apache.commons.lang3.ArrayUtils;
import tj.TJConfig;
import tj.TJValues;
import tj.blocks.EnergyPortCasings;
import tj.blocks.BlockFusionCasings;
import tj.blocks.BlockFusionGlass;
import tj.blocks.TJMetaBlocks;
import tj.capability.impl.handler.IFusionProvider;
import tj.builder.multicontrollers.TJRecipeMapMultiblockControllerBase;
import tj.builder.multicontrollers.UIDisplayBuilder;
import tj.capability.IHeatInfo;
import tj.capability.IProgressBar;
import tj.capability.ProgressBar;
import tj.capability.TJCapabilities;
import tj.gui.widgets.TJCycleButtonWidget;
import tj.machines.multi.BatchMode;
import tj.textures.TJTextures;
import tj.util.TooltipHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import static gregtech.api.gui.GuiTextures.TOGGLE_BUTTON_BACK;
import static gregtech.api.metatileentity.multiblock.MultiblockAbility.INPUT_ENERGY;
import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;
import static tj.capability.TJMultiblockDataCodes.PARALLEL_LAYER;
import static tj.gui.TJGuiTextures.*;

public class MetaTileEntityIndustrialFusionReactor extends TJRecipeMapMultiblockControllerBase implements IHeatInfo, IFusionProvider, IProgressBar {

    private int parallelLayer;
    private long energyToStart;
    private final int tier;
    private EnergyContainerList inputEnergyContainers;
    private long heat;
    private long maxHeat;
    private static final DecimalFormat formatter = new DecimalFormat("#0.00");
    private final Set<BlockPos> activeStates = new HashSet<>();
    private BatchMode batchMode = BatchMode.ONE;
    private Recipe recipe;

    public MetaTileEntityIndustrialFusionReactor(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, RecipeMaps.FUSION_RECIPES);
        this.recipeMapWorkable = new IndustrialFusionRecipeLogic(this, TJConfig.industrialFusionReactor.eutPercentage, TJConfig.industrialFusionReactor.durationPercentage, 100, 1);
        this.tier = tier;
        switch (tier) {
            case 6:
                this.energyToStart = 160_000_000;
                break;
            case 7:
                this.energyToStart = 320_000_000;
                break;
            case 8:
                this.energyToStart = 640_000_000;
                break;
            case 9:
                this.energyToStart = 1_280_000_000;
                break;
            case 10:
                this.energyToStart = 2_560_000_000L;
        }
        this.energyContainer = new EnergyContainerHandler(this, Integer.MAX_VALUE, 0, 0 ,0, 0) {
            @Override
            public String getName() {
                return "EnergyContainerInternal";
            }
        };
        this.reinitializeStructurePattern();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityIndustrialFusionReactor(this.metaTileEntityId, this.tier);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("tj.multiblock.industrial_fusion_reactor.description"));
        TooltipHelper.shiftText(tooltip, tip -> {
            tip.add(I18n.format("tj.multiblock.industrial_fusion_reactor.overclock.description").replace("§r", "§7"));
            tip.add(I18n.format("tj.multiblock.universal.tooltip.1", this.recipeMap.getLocalizedName()));
            tip.add(I18n.format("gtadditions.multiblock.universal.tooltip.2", formatter.format(TJConfig.industrialFusionReactor.eutPercentage / 100.0)));
            tip.add(I18n.format("gtadditions.multiblock.universal.tooltip.3", formatter.format(TJConfig.industrialFusionReactor.durationPercentage / 100.0)));
            tip.add(I18n.format("tj.multiblock.universal.tooltip.2", TJConfig.industrialFusionReactor.maximumSlices));
            tip.add(I18n.format("tj.multiblock.industrial_fusion_reactor.energy", this.energyToStart));
        });
    }

    public void resetStructure() {
        this.invalidateStructure();
        this.recipeMapWorkable.previousRecipe.clear();
        this.structurePattern = this.createStructurePattern();
    }

    @Override
    public void onRemoval() {
        super.onRemoval();
        if (!this.getWorld().isRemote) {
            this.replaceEnergyPortsAsActive(false);
        }
    }

    public int getTier() {
        return this.tier;
    }

    @Override
    protected void reinitializeStructurePattern() {
        this.parallelLayer = 1;
        super.reinitializeStructurePattern();
    }

    @Override
    public void replaceEnergyPortsAsActive(boolean active) {
        this.activeStates.forEach(pos -> {
            IBlockState state = this.getWorld().getBlockState(pos);
            if (state.getBlock() instanceof EnergyPortCasings) {
                state = state.withProperty(EnergyPortCasings.ACTIVE, active);
                this.getWorld().setBlockState(pos, state);
            }
        });
    }

    @Override
    protected BlockPattern createStructurePattern() {
        FactoryBlockPattern factoryPattern = FactoryBlockPattern.start(LEFT, FRONT, DOWN);
                for (int count = 1; count < this.parallelLayer; count++) {
                    factoryPattern.aisle("###############", "######ICI######", "####CC###CC####", "###C#######C###", "##C#########C##", "##C#########C##", "#I###########I#", "#C###########C#", "#I###########I#", "##C#########C##", "##C#########C##", "###C#######C###", "####CC###CC####", "######ICI######", "###############");
                    factoryPattern.aisle("######OGO######", "####GGcccGG####", "###EccOGOccE###", "##EcEG###GEcE##", "#GcE#######EcG#", "#GcG#######GcG#", "OcO#########OcO", "GcG#########GcG", "OcO#########OcO", "#GcG#######GcG#", "#GcE#######EcG#", "##EcEG###GEcE##", "###EccOGOccE###", "####GGcccGG####", "######OGO######");
                }
        factoryPattern.aisle("###############", "######ICI######", "####CC###CC####", "###C#######C###", "##C#########C##", "##C#########C##", "#I###########I#", "#C###########C#", "#I###########I#", "##C#########C##", "##C#########C##", "###C#######C###", "####CC###CC####", "######ICI######", "###############");
        factoryPattern.aisle("######OSO######", "####GGcccGG####", "###EccOGOccE###", "##EcEG###GEcE##", "#GcE#######EcG#", "#GcG#######GcG#", "OcO#########OcO", "GcG#########GcG", "OcO#########OcO", "#GcG#######GcG#", "#GcE#######EcG#", "##EcEG###GEcE##", "###EccOGOccE###", "####GGcccGG####", "######OGO######");
        factoryPattern.aisle("###############", "######ICI######", "####CC###CC####", "###C#######C###", "##C#########C##", "##C#########C##", "#I###########I#", "#C###########C#", "#I###########I#", "##C#########C##", "##C#########C##", "###C#######C###", "####CC###CC####", "######ICI######", "###############")
                .where('S', this.selfPredicate())
                .where('C', statePredicate(this.getCasingState()))
                .where('G', statePredicate(this.getCasingState()).or(statePredicate(this.getGlassState())))
                .where('c', statePredicate(this.getCoilState()))
                .where('O', statePredicate(this.getCasingState()).or(statePredicate(this.getGlassState())).or(abilityPartPredicate(MultiblockAbility.EXPORT_FLUIDS)))
                .where('E', statePredicate(this.getCasingState()).or(statePredicate(this.getGlassState())).or(tilePredicate(energyHatchPredicate(tier))).or(energyPortPredicate(tier)))
            .where('I', statePredicate(this.getCasingState()).or(abilityPartPredicate(MultiblockAbility.IMPORT_FLUIDS)))
            .where('#', (tile) -> true);
        return this.tier != 0 ? factoryPattern.build() : null;
    }

    public static BiFunction<BlockWorldState, MetaTileEntity, Boolean> energyHatchPredicate(int tier) {
        return (state, tile) -> {
            if (tile instanceof MetaTileEntityMultiblockPart) {
                MetaTileEntityMultiblockPart multiblockPart = (MetaTileEntityMultiblockPart) tile;
                if (multiblockPart instanceof IMultiblockAbilityPart<?>) {
                    IMultiblockAbilityPart<?> abilityPart = (IMultiblockAbilityPart<?>) multiblockPart;
                    return abilityPart.getAbility() == INPUT_ENERGY && multiblockPart.getTier() >= tier;
                }
            }
            return false;
        };
    }

    public Predicate<BlockWorldState> energyPortPredicate(int tier) {
        return (blockWorldState) -> {
            IBlockState blockState = blockWorldState.getBlockState();
            if (blockState.getBlock() instanceof EnergyPortCasings) {
                EnergyPortCasings abilityCasings = (EnergyPortCasings) blockState.getBlock();
                EnergyPortCasings.AbilityType tieredCasingType = abilityCasings.getState(blockState);
                List<EnergyPortCasings.AbilityType> currentCasing = blockWorldState.getMatchContext().getOrCreate("EnergyPort", ArrayList::new);
                currentCasing.add(tieredCasingType);
                if (currentCasing.get(0).getName().equals(tieredCasingType.getName()) && currentCasing.get(0).getTier() >= tier && blockWorldState.getWorld() != null) {
                    this.activeStates.add(blockWorldState.getPos());
                    return true;
                }
            }
            return false;
        };
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        switch (tier) {
            case 6: return this.recipeMapWorkable.isActive() ? TJTextures.FUSION_PORT_LUV_ACTIVE : TJTextures.FUSION_PORT_LUV;
            case 7: return this.recipeMapWorkable.isActive() ? TJTextures.FUSION_PORT_ZPM_ACTIVE : TJTextures.FUSION_PORT_ZPM;
            case 8: return this.recipeMapWorkable.isActive() ? TJTextures.FUSION_PORT_UV_ACTIVE : TJTextures.FUSION_PORT_UV;
            case 9: return this.recipeMapWorkable.isActive() ? TJTextures.FUSION_PORT_UHV_ACTIVE : TJTextures.FUSION_PORT_UHV;
            default: return this.recipeMapWorkable.isActive() ? TJTextures.FUSION_PORT_UEV_ACTIVE : TJTextures.FUSION_PORT_UEV;
        }
    }

    public IBlockState getCasingState() {
        switch (tier) {
            case 6: return MetaBlocks.MUTLIBLOCK_CASING.getState(BlockMultiblockCasing.MultiblockCasingType.FUSION_CASING);
            case 7: return MetaBlocks.MUTLIBLOCK_CASING.getState(BlockMultiblockCasing.MultiblockCasingType.FUSION_CASING_MK2);
            case 8: return GAMetaBlocks.FUSION_CASING.getState(GAFusionCasing.CasingType.FUSION_3);
            case 9: return TJMetaBlocks.FUSION_CASING.getState(BlockFusionCasings.FusionType.FUSION_CASING_UHV);
            default: return TJMetaBlocks.FUSION_CASING.getState(BlockFusionCasings.FusionType.FUSION_CASING_UEV);
        }
    }

    public IBlockState getGlassState() {
        switch (tier) {
            case 6: return TJMetaBlocks.FUSION_GLASS.getState(BlockFusionGlass.GlassType.FUSION_GLASS_LUV);
            case 7: return TJMetaBlocks.FUSION_GLASS.getState(BlockFusionGlass.GlassType.FUSION_GLASS_ZPM);
            case 8: return TJMetaBlocks.FUSION_GLASS.getState(BlockFusionGlass.GlassType.FUSION_GLASS_UV);
            case 9: return TJMetaBlocks.FUSION_GLASS.getState(BlockFusionGlass.GlassType.FUSION_GLASS_UHV);
            default: return TJMetaBlocks.FUSION_GLASS.getState(BlockFusionGlass.GlassType.FUSION_GLASS_UEV);
        }
    }

    public IBlockState getCoilState() {
        switch (tier) {
            case 6: return MetaBlocks.WIRE_COIL.getState(BlockWireCoil.CoilType.FUSION_COIL);
            case 7: return GAMetaBlocks.FUSION_CASING.getState(GAFusionCasing.CasingType.FUSION_COIL_2);
            case 8: return GAMetaBlocks.FUSION_CASING.getState(GAFusionCasing.CasingType.FUSION_COIL_3);
            case 9: return TJMetaBlocks.FUSION_CASING.getState(BlockFusionCasings.FusionType.FUSION_COIL_UHV);
            default: return TJMetaBlocks.FUSION_CASING.getState(BlockFusionCasings.FusionType.FUSION_COIL_UEV);
        }
    }


    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        long euCapacity = 0;
        long energyStored = this.energyContainer.getEnergyStored();
        int energyPortAmount = Collections.unmodifiableList(context.getOrDefault("EnergyPort", Collections.emptyList())).size();
        euCapacity += energyPortAmount * 10000000L * (long) Math.pow(2, tier - 6);

        List<IEnergyContainer> energyInputs = getAbilities(INPUT_ENERGY);
        this.inputEnergyContainers = new EnergyContainerList(energyInputs);
        euCapacity += energyInputs.size() * 10000000L * (long) Math.pow(2, tier - 6);
        this.energyContainer = new EnergyContainerHandler(this, euCapacity, GAValues.V[tier], 0, 0, 0) {
            @Override
            public String getName() {
                return "EnergyContainerInternal";
            }
        };
        ((EnergyContainerHandler) this.energyContainer).setEnergyStored(energyStored);
    }

    @Override
    public boolean checkRecipe(Recipe recipe, boolean consumeIfSuccess) {
        long energyToStart = this.recipe.getRecipePropertyStorage().getRecipePropertyValue(FusionEUToStartProperty.getInstance(), 0L) * this.getParallels();
        return this.heat >= energyToStart;
    }

    @Override
    protected void updateFormedValid() {
        super.updateFormedValid();
        long inputEnergyStored = this.inputEnergyContainers.getEnergyStored();
        if (inputEnergyStored > 0) {
            long energyAdded = this.energyContainer.addEnergy(inputEnergyStored);
            if (energyAdded > 0)
                this.inputEnergyContainers.removeEnergy(energyAdded);
        }

        if (this.heat > this.maxHeat)
            this.heat = this.maxHeat;

        if (!this.recipeMapWorkable.isActive() || !this.recipeMapWorkable.isWorkingEnabled()) {
            this.heat -= Math.min(this.heat, 10000L * this.getParallels());
        }

        if (this.recipe != null && this.recipeMapWorkable.isWorkingEnabled()) {
            long remainingHeat = this.maxHeat - this.heat;
            long energyToRemove = Math.min(remainingHeat, this.inputEnergyContainers.getInputAmperage() * this.inputEnergyContainers.getInputVoltage());
            this.heat += Math.abs(this.energyContainer.removeEnergy(energyToRemove));
        }
    }

    @Override
    protected void mainDisplayTab(List<Widget> widgetGroup) {
        super.mainDisplayTab(widgetGroup);
        widgetGroup.add(new TJCycleButtonWidget(175, 151, 18, 18, BatchMode.class, this::getBatchMode, this::setBatchMode, BUTTON_BATCH_ONE, BUTTON_BATCH_FOUR, BUTTON_BATCH_SIXTEEN, BUTTON_BATCH_SIXTY_FOUR, BUTTON_BATCH_TWO_HUNDRED_FIFTY_SIX)
                .setTooltipFormat(this::getTooltipFormat)
                .setToggle(true)
                .setButtonTexture(TOGGLE_BUTTON_BACK)
                .setTooltipHoverString("machine.universal.batch.amount"));
    }

    private void setBatchMode(BatchMode batchMode) {
        this.batchMode = batchMode;
        this.markDirty();
    }

    @Override
    public BatchMode getBatchMode() {
        return this.batchMode;
    }

    private String[] getTooltipFormat() {
        return ArrayUtils.toArray(String.valueOf(this.batchMode.getAmount()));
    }

    @Override
    protected void addDisplayText(UIDisplayBuilder builder) {
        super.addDisplayText(builder);
        builder.addTextComponent(new TextComponentString(net.minecraft.util.text.translation.I18n.translateToLocalFormatted("tj.multiblock.industrial_fusion_reactor.message", this.parallelLayer)));
        if (!this.isStructureFormed()) return;
        builder.energyStoredLine(this.energyContainer.getEnergyStored(), this.energyContainer.getEnergyCapacity())
                .customLine(text -> {
                    text.addTextComponent(new TextComponentString(net.minecraft.util.text.translation.I18n.translateToLocalFormatted("tj.multiblock.industrial_fusion_reactor.heat", this.heat)));
                    if (this.recipe != null) {
                        long energyToStart = this.recipe.getRecipePropertyStorage().getRecipePropertyValue(FusionEUToStartProperty.getInstance(), 0L) * this.parallelLayer;
                        text.addTextComponent(new TextComponentTranslation("tj.multiblock.industrial_fusion_reactor.required_heat", TJValues.thousandFormat.format(energyToStart))
                                .setStyle(new Style().setColor(this.heat >= energyToStart ? TextFormatting.GREEN : TextFormatting.RED)));
                    }
                });
    }

    @Override
    public boolean onRightClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        if (playerIn.getHeldItemMainhand().isItemEqual(MetaItems.SCREWDRIVER.getStackForm()))
            return false;
        return super.onRightClick(playerIn, hand, facing, hitResult);
    }

    @Override
    public boolean onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        ITextComponent textComponent;
        if (!playerIn.isSneaking()) {
            if (this.parallelLayer < TJConfig.industrialFusionReactor.maximumSlices) {
                this.parallelLayer++;
                textComponent = new TextComponentTranslation("tj.multiblock.parallel.layer.increment.success").appendSibling(new TextComponentString(" " + this.parallelLayer));
            } else
                textComponent = new TextComponentTranslation("tj.multiblock.parallel.layer.increment.fail").appendSibling(new TextComponentString(" " + this.parallelLayer));
        } else {
            if (this.parallelLayer > 1) {
                this.parallelLayer--;
                textComponent = new TextComponentTranslation("tj.multiblock.parallel.layer.decrement.success").appendSibling(new TextComponentString(" " + this.parallelLayer));
            } else
                textComponent = new TextComponentTranslation("tj.multiblock.parallel.layer.decrement.fail").appendSibling(new TextComponentString(" " + this.parallelLayer));
        }
        if (this.getWorld().isRemote)
            playerIn.sendMessage(textComponent);
        else {
            writeCustomData(PARALLEL_LAYER, buf -> buf.writeInt(this.parallelLayer));
        }
        this.resetStructure();
        return true;
    }

    @Nonnull
    @Override
    protected OrientedOverlayRenderer getFrontOverlay() {
        return ClientHandler.FUSION_REACTOR_OVERLAY;
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == PARALLEL_LAYER) {
            this.parallelLayer = buf.readInt();
            this.structurePattern = createStructurePattern();
            this.scheduleRenderUpdate();
        }
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
        this.structurePattern = createStructurePattern();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        NBTTagCompound tagCompound = super.writeToNBT(data);
        tagCompound.setLong("Heat", this.heat);
        tagCompound.setLong("MaxHeat", this.maxHeat);
        tagCompound.setInteger("Parallel", this.parallelLayer);
        tagCompound.setInteger("BatchMode", this.batchMode.ordinal());
        return tagCompound;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.maxHeat = data.getLong("MaxHeat");
        this.heat = data.getLong("Heat");
        this.parallelLayer = data.getInteger("Parallel");
        this.batchMode = BatchMode.values()[data.getInteger("BatchMode")];
        if (data.hasKey("Parallel"))
            this.structurePattern = createStructurePattern();
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == TJCapabilities.CAPABILITY_HEAT)
            return TJCapabilities.CAPABILITY_HEAT.cast(this);
        return super.getCapability(capability, side);
    }

    @Override
    public int[][] getBarMatrix() {
        return new int[1][1];
    }

    @Override
    public void getProgressBars(Queue<UnaryOperator<ProgressBar.ProgressBarBuilder>> bars) {
        bars.add(bar -> bar.setProgress(this::heat).setMaxProgress(this::maxHeat)
                .setLocale("tj.multiblock.bars.heat")
                .setBarTexture(BAR_RED));
    }

    @Override
    public int getParallels() {
        return this.parallelLayer;
    }

    @Override
    public long getEnergyToStart() {
        return this.energyToStart;
    }

    @Override
    public long heat() {
        return this.heat;
    }

    @Override
    public long maxHeat() {
        return this.maxHeat;
    }

    @Override
    public void setRecipe(long heat, Recipe recipe) {
        long energyCapacity = this.energyContainer.getEnergyCapacity();
        long heatCapacity = heat * this.getParallels();
        this.recipe = recipe;
        this.maxHeat = Math.min(energyCapacity, heatCapacity);
    }

    private static class IndustrialFusionRecipeLogic extends LargeSimpleRecipeMapMultiblockController.LargeSimpleMultiblockRecipeLogic {

        private final int EUtPercentage;
        private final int durationPercentage;
        private final IFusionProvider fusionReactor;

        public IndustrialFusionRecipeLogic(RecipeMapMultiblockController tileEntity, int EUtPercentage, int durationPercentage, int chancePercentage, int stack) {
            super(tileEntity, EUtPercentage, durationPercentage, chancePercentage, stack);
            this.fusionReactor = (IFusionProvider) tileEntity;
            this.EUtPercentage = EUtPercentage;
            this.durationPercentage = durationPercentage;
            this.recipeMap = tileEntity.recipeMap;
            this.allowOverclocking = false;
        }

        @Override
        protected void completeRecipe() {
            super.completeRecipe();
            this.fusionReactor.setRecipe(0L, null);
        }

        @Override
        protected Recipe createRecipe(long maxVoltage, IItemHandlerModifiable inputs, IMultipleTankHandler fluidInputs, Recipe matchingRecipe) {
            int EUt;
            int duration;
            int minMultiplier = Integer.MAX_VALUE;
            long recipeEnergy = Math.max(160_000_000, matchingRecipe.getRecipePropertyStorage().getRecipePropertyValue(FusionEUToStartProperty.getInstance(), 0L));

            this.fusionReactor.setRecipe(recipeEnergy, matchingRecipe);
            Map<String, Integer> countFluid = new HashMap<>();
            if (!matchingRecipe.getFluidInputs().isEmpty()) {

                this.findFluid(countFluid, fluidInputs);
                minMultiplier = Math.min(minMultiplier, this.getMinRatioFluid(countFluid, matchingRecipe, this.fusionReactor.getParallels() * this.fusionReactor.getBatchMode().getAmount()));
            }

            if (minMultiplier == Integer.MAX_VALUE) {
                GALog.logger.error("Cannot calculate ratio of items for large multiblocks");
                return null;
            }
            EUt = matchingRecipe.getEUt();
            duration = matchingRecipe.getDuration();

            float tierDiff = fusionOverclockMultiplier(this.fusionReactor.getEnergyToStart(), recipeEnergy);

            List<FluidStack> newFluidInputs = new ArrayList<>();
            List<FluidStack> outputF = new ArrayList<>();
            multiplyInputsAndOutputs(newFluidInputs, outputF, matchingRecipe, minMultiplier);

            RecipeBuilder<?> newRecipe = this.recipeMap.recipeBuilder();

            newRecipe.fluidInputs(newFluidInputs)
                    .fluidOutputs(outputF)
                    .EUt((int) Math.max(1, ((EUt * this.EUtPercentage * minMultiplier / 100.0) * tierDiff) / this.fusionReactor.getBatchMode().getAmount()))
                    .duration((int) Math.max(1, ((duration * (this.durationPercentage / 100.0)) / tierDiff) * this.fusionReactor.getBatchMode().getAmount()));

            return newRecipe.build().getResult();
        }

        private void multiplyInputsAndOutputs(List<FluidStack> newFluidInputs, List<FluidStack> outputF, Recipe recipe, int multiplier) {
            for (FluidStack fluidS : recipe.getFluidInputs()) {
                FluidStack newFluid = new FluidStack(fluidS.getFluid(), fluidS.amount * multiplier);
                newFluidInputs.add(newFluid);
            }
            for (FluidStack fluid : recipe.getFluidOutputs()) {
                int fluidNum = fluid.amount * multiplier;
                FluidStack fluidCopy = fluid.copy();
                fluidCopy.amount = fluidNum;
                outputF.add(fluidCopy);
            }
        }

        private float fusionOverclockMultiplier(long energyToStart, long recipeEnergy) {
            recipeEnergy = Math.max(160_000_000, recipeEnergy);
            long recipeEnergyOld = recipeEnergy;
            float OCMultiplier = 1;
            while (recipeEnergy <= energyToStart) {
                if (recipeEnergy != recipeEnergyOld)
                    OCMultiplier *= recipeEnergy > 640_000_000 ? 4 : 2.8F;
                recipeEnergy *= 2;
            }
            return OCMultiplier;
        }

        @Override
        protected void setActive(boolean active) {
            this.fusionReactor.replaceEnergyPortsAsActive(active);
            super.setActive(active);
        }
    }
}
