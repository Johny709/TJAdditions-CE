package tj.machines.multi.electric;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import gregicadditions.GAValues;
import gregicadditions.client.ClientHandler;
import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.fusion.GAFusionCasing;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.impl.EnergyContainerHandler;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.gui.Widget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.BlockWorldState;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.api.multiblock.PatternMatchContext;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.recipeproperties.FusionEUToStartProperty;
import gregtech.api.render.ICubeRenderer;
import gregtech.api.render.OrientedOverlayRenderer;
import gregtech.common.blocks.BlockMultiblockCasing;
import gregtech.common.blocks.BlockWireCoil;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.electric.multiblockpart.MetaTileEntityMultiblockPart;
import net.minecraft.block.Block;
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
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.ArrayUtils;
import tj.TJConfig;
import tj.TJValues;
import tj.blocks.EnergyPortCasings;
import tj.blocks.BlockFusionCasings;
import tj.blocks.BlockFusionGlass;
import tj.blocks.TJMetaBlocks;
import tj.builder.multicontrollers.TJRecipeMapMultiblockController;
import tj.builder.multicontrollers.UIDisplayBuilder;
import tj.capability.*;
import tj.gui.widgets.TJCycleButtonWidget;
import tj.machines.multi.BatchMode;
import tj.textures.TJTextures;
import tj.util.TooltipHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import static gregtech.api.gui.GuiTextures.TOGGLE_BUTTON_BACK;
import static gregtech.api.metatileentity.multiblock.MultiblockAbility.INPUT_ENERGY;
import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;
import static tj.capability.TJMultiblockDataCodes.PARALLEL_LAYER;
import static tj.gui.TJGuiTextures.*;

public class MetaTileEntityIndustrialFusionReactor extends TJRecipeMapMultiblockController implements IHeatInfo, IProgressBar {

    private final Set<BlockPos> activeStates = new HashSet<>();
    private final long energyToStart;
    private int parallelLayer;
    private long heat;
    private long maxHeat;
    private BatchMode batchMode = BatchMode.ONE;
    private Recipe recipe;
    private IEnergyContainer energyContainer;

    public MetaTileEntityIndustrialFusionReactor(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, RecipeMaps.FUSION_RECIPES, false, false);
        this.recipeLogic.setAllowOverclocking(false);
        this.tier = tier;
        this.energyToStart = 160_000_000L << tier - 6;
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
        TooltipHelper.shiftTextJEI(tooltip, tip -> {
            tip.add(I18n.format("tj.multiblock.industrial_fusion_reactor.overclock.description"));
            tip.add(I18n.format("tj.multiblock.universal.tooltip.1", this.recipeMap.getLocalizedName()));
            tip.add(I18n.format("gtadditions.multiblock.universal.tooltip.2", TJValues.thousandTwoPlaceFormat.format(TJConfig.industrialFusionReactor.eutPercentage / 100.0)));
            tip.add(I18n.format("gtadditions.multiblock.universal.tooltip.3", TJValues.thousandTwoPlaceFormat.format(TJConfig.industrialFusionReactor.durationPercentage / 100.0)));
            tip.add(I18n.format("tj.multiblock.universal.tooltip.2", TJConfig.industrialFusionReactor.maximumSlices));
            tip.add(I18n.format("tj.multiblock.industrial_fusion_reactor.energy", this.energyToStart));
        });
    }

    @Override
    protected void reinitializeStructurePattern() {
        this.parallelLayer = 1;
        super.reinitializeStructurePattern();
    }

    @Override
    public void preOverclock(OverclockManager<?> overclockManager, Recipe recipe) {
        super.preOverclock(overclockManager, recipe);
        long recipeEnergy = Math.max(160_000_000, recipe.getRecipePropertyStorage().getRecipePropertyValue(FusionEUToStartProperty.getInstance(), 0L));
        long recipeEnergyOld = recipeEnergy;
        float ocMultiplier = 1;
        while (recipeEnergy <= this.energyToStart) {
            if (recipeEnergy != recipeEnergyOld)
                ocMultiplier *= recipeEnergy > 640_000_000 ? 4 : 2.8F;
            recipeEnergy *= 2;
        }
        overclockManager.setEUt((long) (overclockManager.getEUt() * ocMultiplier));
        overclockManager.setDuration((int) (overclockManager.getDuration() / ocMultiplier));
        overclockManager.setParallel(this.getParallel() * this.batchMode.getAmount());
    }

    @Override
    public void postOverclock(OverclockManager<?> overclockManager, Recipe recipe) {
        overclockManager.setDuration(overclockManager.getDuration() * this.batchMode.getAmount());
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
            case 6: return this.recipeLogic.isActive() ? TJTextures.FUSION_PORT_LUV_ACTIVE : TJTextures.FUSION_PORT_LUV;
            case 7: return this.recipeLogic.isActive() ? TJTextures.FUSION_PORT_ZPM_ACTIVE : TJTextures.FUSION_PORT_ZPM;
            case 8: return this.recipeLogic.isActive() ? TJTextures.FUSION_PORT_UV_ACTIVE : TJTextures.FUSION_PORT_UV;
            case 9: return this.recipeLogic.isActive() ? TJTextures.FUSION_PORT_UHV_ACTIVE : TJTextures.FUSION_PORT_UHV;
            default: return this.recipeLogic.isActive() ? TJTextures.FUSION_PORT_UEV_ACTIVE : TJTextures.FUSION_PORT_UEV;
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
        this.inputEnergyContainer = new EnergyContainerList(energyInputs);
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
    public boolean checkRecipe(Recipe recipe) {
        long energyToStart = recipe.getRecipePropertyStorage().getRecipePropertyValue(FusionEUToStartProperty.getInstance(), 0L) * this.getParallel();
        this.recipe = recipe;
        this.maxHeat = Math.min(this.energyContainer.getEnergyCapacity(), energyToStart);
        return this.heat >= energyToStart;
    }

    @Override
    protected void updateFormedValid() {
        super.updateFormedValid();
        long inputEnergyStored = this.inputEnergyContainer.getEnergyStored();
        if (inputEnergyStored > 0) {
            long energyAdded = this.energyContainer.addEnergy(inputEnergyStored);
            if (energyAdded > 0)
                this.inputEnergyContainer.removeEnergy(energyAdded);
        }

        if (this.heat > this.maxHeat)
            this.heat = this.maxHeat;

        if (!this.recipeLogic.isActive() || !this.recipeLogic.isWorkingEnabled()) {
            this.heat -= Math.min(this.heat, 10000L * this.getParallel());
        }

        if (this.recipe != null && this.recipeLogic.isWorkingEnabled()) {
            long remainingHeat = this.maxHeat - this.heat;
            long energyToRemove = Math.min(remainingHeat, this.inputEnergyContainer.getInputAmperage() * this.inputEnergyContainer.getInputVoltage());
            this.heat += Math.abs(this.energyContainer.removeEnergy(energyToRemove));
        }
    }

    @Override
    protected void mainDisplayTab(List<Widget> widgetGroup) {
        super.mainDisplayTab(widgetGroup);
        widgetGroup.add(new TJCycleButtonWidget(175, 151, 18, 18, BatchMode.class, () -> this.batchMode, this::setBatchMode, BUTTON_BATCH_ONE, BUTTON_BATCH_FOUR, BUTTON_BATCH_SIXTEEN, BUTTON_BATCH_SIXTY_FOUR, BUTTON_BATCH_TWO_HUNDRED_FIFTY_SIX)
                .setTooltipFormat(this::getTooltipFormat)
                .setToggle(true)
                .setButtonTexture(TOGGLE_BUTTON_BACK)
                .setTooltipHoverString("machine.universal.batch.amount"));
    }

    private void setBatchMode(BatchMode batchMode) {
        this.batchMode = batchMode;
        this.markDirty();
    }

    private String[] getTooltipFormat() {
        return ArrayUtils.toArray(String.valueOf(this.batchMode.getAmount()));
    }

    @Override
    protected void addDisplayText(UIDisplayBuilder builder) {
        super.addDisplayText(builder);
        builder.addTranslationLine("tj.multiblock.industrial_fusion_reactor.message", this.parallelLayer);
        if (!this.isStructureFormed()) return;
        builder.energyStoredLine(this.energyContainer.getEnergyStored(), this.energyContainer.getEnergyCapacity())
                .customLine(text -> {
                    text.addTranslationLine("tj.multiblock.industrial_fusion_reactor.heat", this.heat);
                    if (this.recipe != null) {
                        long energyToStart = this.recipe.getRecipePropertyStorage().getRecipePropertyValue(FusionEUToStartProperty.getInstance(), 0L) * this.parallelLayer;
                        text.addTextComponent(new TextComponentTranslation("tj.multiblock.industrial_fusion_reactor.required_heat", TJValues.thousandFormat.format(energyToStart))
                                .setStyle(new Style().setColor(this.heat >= energyToStart ? TextFormatting.GREEN : TextFormatting.RED)));
                    }
                });
    }

    @Override
    public boolean onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        if (!this.getWorld().isRemote) {
            int lastParallelLayer = this.parallelLayer;
            this.parallelLayer += MathHelper.clamp(playerIn.isSneaking() ? -1 : 1, 0, TJConfig.industrialFusionReactor.maximumSlices);
            if (this.parallelLayer != lastParallelLayer) {
                playerIn.sendMessage(new TextComponentTranslation(playerIn.isSneaking() ? "tj.multiblock.parallel.layer.decrement.success" : "tj.multiblock.parallel.layer.increment.success", this.parallelLayer));
            } else playerIn.sendMessage(new TextComponentTranslation(playerIn.isSneaking() ? "tj.multiblock.parallel.layer.decrement.fail" : "tj.multiblock.parallel.layer.increment.fail", this.parallelLayer));
            this.structurePattern = this.createStructurePattern();
            this.invalidateStructure();
            this.writeCustomData(PARALLEL_LAYER, buf -> buf.writeInt(this.parallelLayer));
            this.markDirty();
        }
        return true;
    }

    @Nonnull
    @Override
    protected OrientedOverlayRenderer getFrontOverlay() {
        return ClientHandler.FUSION_REACTOR_OVERLAY;
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeInt(this.parallelLayer);
        this.writeActiveBlockPacket(buf, this.recipeLogic.isActive());
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.parallelLayer = buf.readInt();
        this.readActiveBlockPacket(buf);
        this.structurePattern = this.createStructurePattern();
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == PARALLEL_LAYER) {
            this.parallelLayer = buf.readInt();
            this.structurePattern = this.createStructurePattern();
            this.invalidateStructure();
            this.scheduleRenderUpdate();
        } else if (dataId == 128) {
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
            if (block instanceof EnergyPortCasings) {
                state = state.withProperty(EnergyPortCasings.ACTIVE, isActive);
                this.getWorld().setBlockState(pos, state);
            }
        }
    }

    public void replaceEnergyPortsAsActive(boolean isActive) {
        this.writeCustomData(128, buffer -> this.writeActiveBlockPacket(buffer, isActive));
    }

    @Override
    public void onRemoval() {
        super.onRemoval();
        if (!this.getWorld().isRemote) {
            this.replaceEnergyPortsAsActive(false);
            this.markDirty();
        }
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
    public int getParallel() {
        return this.parallelLayer;
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
    public int getEUtMultiplier() {
        return TJConfig.industrialFusionReactor.eutPercentage;
    }

    @Override
    public int getDurationMultiplier() {
        return TJConfig.industrialFusionReactor.durationPercentage;
    }

    @Override
    public int getTierDifference(long recipeEUt) {
        return 0;
    }
}
