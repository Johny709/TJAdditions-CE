package tj.machines.multi.electric;

import gregicadditions.GAUtility;
import gregicadditions.GAValues;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.impl.EnergyContainerHandler;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
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
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tj.TJValues;
import tj.blocks.*;
import tj.builder.multicontrollers.GUIDisplayBuilder;
import tj.builder.multicontrollers.TJRecipeMapMultiblockController;
import tj.capability.*;
import tj.capability.impl.workable.MegaRecipeLogic;
import tj.machines.multi.BatchMode;
import tj.textures.TJOrientedOverlayRenderer;
import tj.textures.TJTextures;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import static gregicadditions.capabilities.GregicAdditionsCapabilities.MAINTENANCE_HATCH;
import static gregtech.api.metatileentity.multiblock.MultiblockAbility.*;
import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;
import static tj.gui.TJGuiTextures.BAR_RED;

public class MetaTileEntityMegaFusion extends TJRecipeMapMultiblockController implements IHeatInfo, IProgressBar {

    private static final MultiblockAbility<?>[] ALLOWED_ABILITIES = {IMPORT_FLUIDS, EXPORT_FLUIDS, MAINTENANCE_HATCH};
    private final Set<BlockPos> activeStates = new HashSet<>();
    private final long energyToStart = 1_280_000_000;
    private IEnergyContainer energyContainer;
    private BatchMode batchMode = BatchMode.ONE;
    private Recipe recipe;
    private long heat;
    private long maxHeat;

    public MetaTileEntityMegaFusion(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, RecipeMaps.FUSION_RECIPES, true, false);
        this.recipeLogic.setAllowOverclocking(false);
        this.energyContainer = new EnergyContainerHandler(this, Integer.MAX_VALUE, 0, 0 ,0, 0) {
            @Override
            public String getName() {
                return "EnergyContainerInternal";
            }
        };
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityMegaFusion(this.metaTileEntityId);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("tj.multiblock.mega_fusion.description"));
        tooltip.add(I18n.format("tj.multiblock.processing_array.eut"));
        super.addInformation(stack, player, tooltip, advanced);
    }

    @Override
    protected MegaRecipeLogic<?> createRecipeLogic() {
        return new MegaRecipeLogic<>(this);
    }

    @Override
    public boolean checkRecipe(Recipe recipe) {
        final long energyToStart = recipe.getRecipePropertyStorage().getRecipePropertyValue(FusionEUToStartProperty.getInstance(), 0L);
        this.recipe = recipe;
        this.maxHeat = Math.min(this.energyContainer.getEnergyCapacity(), energyToStart);
        return this.heat >= energyToStart;
    }

    @Override
    public void preOverclock(OverclockManager<?> overclockManager, Recipe recipe) {
        super.preOverclock(overclockManager, recipe);
        long recipeEnergy = Math.max(160_000_000, recipe.getRecipePropertyStorage().getRecipePropertyValue(FusionEUToStartProperty.getInstance(), 0L));
        final long recipeEnergyOld = recipeEnergy;
        float ocMultiplier = 1;
        while (recipeEnergy <= this.energyToStart) {
            if (recipeEnergy != recipeEnergyOld)
                ocMultiplier *= recipeEnergy > 640_000_000 ? 4 : 2.8F;
            recipeEnergy *= 2;
        }
        overclockManager.setEUt((long) (overclockManager.getEUt() * ocMultiplier));
        overclockManager.setDuration((int) (overclockManager.getDuration() / ocMultiplier));
        overclockManager.setParallel((int) (this.energyContainer.getEnergyCapacity() / recipe.getRecipePropertyStorage().getRecipePropertyValue(FusionEUToStartProperty.getInstance(), 0L)));
    }

    @Override
    public void postOverclock(OverclockManager<?> overclockManager, Recipe recipe) {
        overclockManager.setEUt(overclockManager.getEUt() * overclockManager.getParallelsPerformed());
        overclockManager.setDuration(overclockManager.getDuration() * this.batchMode.getAmount());
    }

    @Override
    protected void updateFormedValid() {
        super.updateFormedValid();
        final long inputEnergyStored = this.inputEnergyContainer.getEnergyStored();
        if (inputEnergyStored > 0) {
            final long energyAdded = this.energyContainer.addEnergy(inputEnergyStored);
            if (energyAdded > 0)
                this.inputEnergyContainer.removeEnergy(energyAdded);
        }

        if (this.heat > this.maxHeat)
            this.heat = this.maxHeat;

        if (!this.recipeLogic.isActive() || !this.recipeLogic.isWorkingEnabled()) {
            this.heat -= Math.min(this.heat, 10000L * this.getParallel());
        }

        if (this.recipe != null && this.recipeLogic.isWorkingEnabled()) {
            final long remainingHeat = this.maxHeat - this.heat;
            final long energyToRemove = Math.min(remainingHeat, this.inputEnergyContainer.getInputAmperage() * this.inputEnergyContainer.getInputVoltage());
            this.heat += Math.abs(this.energyContainer.removeEnergy(energyToRemove));
        }
    }

    @Override
    protected void addDisplayText(GUIDisplayBuilder builder) {
        super.addDisplayText(builder);
        if (!this.isStructureFormed()) return;
        builder.addEnergyStoredLine(this.energyContainer.getEnergyStored(), this.energyContainer.getEnergyCapacity(), 2)
                .addTranslationLine("tj.multiblock.industrial_fusion_reactor.heat", TJValues.thousandFormat.format(this.heat))
                .customLine(text -> {
                    if (this.recipe != null) {
                        final long energyToStart = this.recipe.getRecipePropertyStorage().getRecipePropertyValue(FusionEUToStartProperty.getInstance(), 0L);
                        text.addTranslationLine(text1 -> text1.setStyle(new Style().setColor(this.heat >= energyToStart ? TextFormatting.GREEN : TextFormatting.RED)
                                        .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentTranslation("tj.multiblock.industrial_fusion_reactor.required_heat.tooltip", "§c" + TJValues.thousandFormat.format(energyToStart))))),
                                "tj.multiblock.industrial_fusion_reactor.required_heat", TJValues.thousandFormat.format(energyToStart));
                    }
                });
    }

    @Override
    protected BlockPattern createStructurePattern() {
        final List<String[]> pattern = new ArrayList<>();
        final FactoryBlockPattern factoryPattern = FactoryBlockPattern.start(LEFT, FRONT, DOWN);
        pattern.add(new String[]{"~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~CCCCCCC~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~CCCCCCCCCCCCC~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~CCCCCCCCCCCCCCCCCCC~~~~~~~~~~~~~", "~~~~~~~~~~~~CCCCCCCCCCCCCCCCCCCCC~~~~~~~~~~~~", "~~~~~~~~~~CCCCCCCCCCCCCCCCCCCCCCCCC~~~~~~~~~~", "~~~~~~~~~CCCCCCCCC~~~~~~~~~CCCCCCCCC~~~~~~~~~", "~~~~~~~~CCCCCCCC~~~~~~~~~~~~~CCCCCCCC~~~~~~~~", "~~~~~~~CCCCCCC~~~~~~~~~~~~~~~~~CCCCCCC~~~~~~~", "~~~~~~~CCCCCC~~~~~~~~~~~~~~~~~~~CCCCCC~~~~~~~", "~~~~~~CCCCCC~~~~~~~~~~~~~~~~~~~~~CCCCCC~~~~~~", "~~~~~CCCCCC~~~~~~~~~~~~~~~~~~~~~~~CCCCCC~~~~~", "~~~~~CCCCC~~~~~~~~~~~~~~~~~~~~~~~~~CCCCC~~~~~", "~~~~~CCCCC~~~~~~~~~~~~~~~~~~~~~~~~~CCCCC~~~~~", "~~~~CCCCC~~~~~~~~~~~~~~~~~~~~~~~~~~~CCCCC~~~~", "~~~~CCCCC~~~~~~~~~~~~~~~~~~~~~~~~~~~CCCCC~~~~", "~~~~CCCC~~~~~~~~~~~~~~~~~~~~~~~~~~~~~CCCC~~~~", "~~~CCCCC~~~~~~~~~~~~~~~~~~~~~~~~~~~~~CCCCC~~~", "~~~CCCCC~~~~~~~~~~~~~~~~~~~~~~~~~~~~~CCCCC~~~", "~~~CCCCC~~~~~~~~~~~~~~~~~~~~~~~~~~~~~CCCCC~~~", "~~~CCCCC~~~~~~~~~~~~~~~~~~~~~~~~~~~~~CCCCC~~~", "~~~CCCCC~~~~~~~~~~~~~~~~~~~~~~~~~~~~~CCCCC~~~", "~~~CCCCC~~~~~~~~~~~~~~~~~~~~~~~~~~~~~CCCCC~~~", "~~~CCCCC~~~~~~~~~~~~~~~~~~~~~~~~~~~~~CCCCC~~~", "~~~~CCCC~~~~~~~~~~~~~~~~~~~~~~~~~~~~~CCCC~~~~", "~~~~CCCCC~~~~~~~~~~~~~~~~~~~~~~~~~~~CCCCC~~~~", "~~~~CCCCC~~~~~~~~~~~~~~~~~~~~~~~~~~~CCCCC~~~~", "~~~~~CCCCC~~~~~~~~~~~~~~~~~~~~~~~~~CCCCC~~~~~", "~~~~~CCCCC~~~~~~~~~~~~~~~~~~~~~~~~~CCCCC~~~~~", "~~~~~CCCCCC~~~~~~~~~~~~~~~~~~~~~~~CCCCCC~~~~~", "~~~~~~CCCCCC~~~~~~~~~~~~~~~~~~~~~CCCCCC~~~~~~", "~~~~~~~CCCCCC~~~~~~~~~~~~~~~~~~~CCCCCC~~~~~~~", "~~~~~~~CCCCCCC~~~~~~~~~~~~~~~~~CCCCCCC~~~~~~~", "~~~~~~~~CCCCCCCC~~~~~~~~~~~~~CCCCCCCC~~~~~~~~", "~~~~~~~~~CCCCCCCCC~~~~~~~~~CCCCCCCCC~~~~~~~~~", "~~~~~~~~~~CCCCCCCCCCCCCCCCCCCCCCCCC~~~~~~~~~~", "~~~~~~~~~~~~CCCCCCCCCCCCCCCCCCCCC~~~~~~~~~~~~", "~~~~~~~~~~~~~CCCCCCCCCCCCCCCCCCC~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~CCCCCCCCCCCCC~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~CCCCCCC~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"});
        pattern.add(new String[]{"~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~CCCCCCCCCCC~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~CCCCCcccccccCCCCC~~~~~~~~~~~~~~", "~~~~~~~~~~~~CCCCcccccccccccccCCCC~~~~~~~~~~~~", "~~~~~~~~~~~CCcccccccccccccccccccCC~~~~~~~~~~~", "~~~~~~~~~CCCcccccccccccccccccccccCCC~~~~~~~~~", "~~~~~~~~CCcccccccccccccccccccccccccCC~~~~~~~~", "~~~~~~~CCcccccccccCCCCCCCCCcccccccccCC~~~~~~~", "~~~~~~CCccccccccCCCC~~~~~CCCCccccccccCC~~~~~~", "~~~~~~CcccccccCCC~~~~~~~~~~~CCCcccccccC~~~~~~", "~~~~~CCccccccCC~~~~~~~~~~~~~~~CCccccccCC~~~~~", "~~~~CCccccccCC~~~~~~~~~~~~~~~~~CCccccccCC~~~~", "~~~~CccccccCC~~~~~~~~~~~~~~~~~~~CCccccccC~~~~", "~~~CCcccccCC~~~~~~~~~~~~~~~~~~~~~CCcccccCC~~~", "~~~CCcccccC~~~~~~~~~~~~~~~~~~~~~~~CcccccCC~~~", "~~~CcccccCC~~~~~~~~~~~~~~~~~~~~~~~CCcccccC~~~", "~~CCcccccC~~~~~~~~~~~~~~~~~~~~~~~~~CcccccCC~~", "~~CCccccCC~~~~~~~~~~~~~~~~~~~~~~~~~CCccccCC~~", "~~CcccccCC~~~~~~~~~~~~~~~~~~~~~~~~~CCcccccC~~", "~~CcccccC~~~~~~~~~~~~~~~~~~~~~~~~~~~CcccccC~~", "~~CcccccC~~~~~~~~~~~~~~~~~~~~~~~~~~~CcccccC~~", "~~CcccccC~~~~~~~~~~~~~~~~~~~~~~~~~~~CcccccC~~", "~~CcccccC~~~~~~~~~~~~~~~~~~~~~~~~~~~CcccccC~~", "~~CcccccC~~~~~~~~~~~~~~~~~~~~~~~~~~~CcccccC~~", "~~CcccccCC~~~~~~~~~~~~~~~~~~~~~~~~~CCcccccC~~", "~~CCccccCC~~~~~~~~~~~~~~~~~~~~~~~~~CCccccCC~~", "~~CCcccccC~~~~~~~~~~~~~~~~~~~~~~~~~CcccccCC~~", "~~~CcccccCC~~~~~~~~~~~~~~~~~~~~~~~CCcccccC~~~", "~~~CCcccccC~~~~~~~~~~~~~~~~~~~~~~~CcccccCC~~~", "~~~CCcccccCC~~~~~~~~~~~~~~~~~~~~~CCcccccCC~~~", "~~~~CccccccCC~~~~~~~~~~~~~~~~~~~CCccccccC~~~~", "~~~~CCccccccCC~~~~~~~~~~~~~~~~~CCccccccCC~~~~", "~~~~~CCccccccCC~~~~~~~~~~~~~~~CCccccccCC~~~~~", "~~~~~~CcccccccCCC~~~~~~~~~~~CCCcccccccC~~~~~~", "~~~~~~CCccccccccCCCC~~~~~CCCCccccccccCC~~~~~~", "~~~~~~~CCcccccccccCCCCCCCCCcccccccccCC~~~~~~~", "~~~~~~~~CCcccccccccccccccccccccccccCC~~~~~~~~", "~~~~~~~~~CCCcccccccccccccccccccccCCC~~~~~~~~~", "~~~~~~~~~~~CCcccccccccccccccccccCC~~~~~~~~~~~", "~~~~~~~~~~~~CCCCcccccccccccccCCCC~~~~~~~~~~~~", "~~~~~~~~~~~~~~CCCCCcccccccCCCCC~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~CCCCCCCCCCC~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"});
        pattern.add(new String[]{"~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~CCCCCCCCCCC~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~CCC###########CCC~~~~~~~~~~~~~~", "~~~~~~~~~~~~CC#################CC~~~~~~~~~~~~", "~~~~~~~~~~~C#####################C~~~~~~~~~~~", "~~~~~~~~~CC#######################CC~~~~~~~~~", "~~~~~~~~C###########################C~~~~~~~~", "~~~~~~~C#############################C~~~~~~~", "~~~~~~C###############################C~~~~~~", "~~~~~C#############CCCCCCC#############C~~~~~", "~~~~~C###########CC~~~~~~~CC###########C~~~~~", "~~~~C##########CC~~~~~~~~~~~CC##########C~~~~", "~~~C##########C~~~~~~~~~~~~~~~C##########C~~~", "~~~C#########C~~~~~~~~~~~~~~~~~C#########C~~~", "~~C#########C~~~~~~~~~~~~~~~~~~~C#########C~~", "~~C########C~~~~~~~~~~~~~~~~~~~~~C########C~~", "~~C########C~~~~~~~~~~~~~~~~~~~~~C########C~~", "~C########C~~~~~~~~~~~~~~~~~~~~~~~C########C~", "~C########C~~~~~~~~~~~~~~~~~~~~~~~C########C~", "~C#######C~~~~~~~~~~~~~~~~~~~~~~~~~C#######C~", "~C#######C~~~~~~~~~~~~~~~~~~~~~~~~~C#######C~", "~C#######C~~~~~~~~~~~~~~~~~~~~~~~~~C#######C~", "~C#######C~~~~~~~~~~~~~~~~~~~~~~~~~C#######C~", "~C#######C~~~~~~~~~~~~~~~~~~~~~~~~~C#######C~", "~C#######C~~~~~~~~~~~~~~~~~~~~~~~~~C#######C~", "~C#######C~~~~~~~~~~~~~~~~~~~~~~~~~C#######C~", "~C########C~~~~~~~~~~~~~~~~~~~~~~~C########C~", "~C########C~~~~~~~~~~~~~~~~~~~~~~~C########C~", "~~C########C~~~~~~~~~~~~~~~~~~~~~C########C~~", "~~C########C~~~~~~~~~~~~~~~~~~~~~C########C~~", "~~C#########C~~~~~~~~~~~~~~~~~~~C#########C~~", "~~~C#########C~~~~~~~~~~~~~~~~~C#########C~~~", "~~~C##########C~~~~~~~~~~~~~~~C##########C~~~", "~~~~C##########CC~~~~~~~~~~~CC##########C~~~~", "~~~~~C###########CC~~~~~~~CC###########C~~~~~", "~~~~~C#############CCCCCCC#############C~~~~~", "~~~~~~C###############################C~~~~~~", "~~~~~~~C#############################C~~~~~~~", "~~~~~~~~C###########################C~~~~~~~~", "~~~~~~~~~CC#######################CC~~~~~~~~~", "~~~~~~~~~~~C#####################C~~~~~~~~~~~", "~~~~~~~~~~~~CC#################CC~~~~~~~~~~~~", "~~~~~~~~~~~~~~CCC###########CCC~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~CCCCCCCCCCC~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"});
        pattern.add(new String[]{"~~~~~~~~~~~~~~~~~~~~CCCCC~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~CCCC#####CCCC~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~CCC#############CCC~~~~~~~~~~~~~", "~~~~~~~~~~~CC###################CC~~~~~~~~~~~", "~~~~~~~~~~C#######################C~~~~~~~~~~", "~~~~~~~~EC#########################CE~~~~~~~~", "~~~~~~~E#############################E~~~~~~~", "~~~~~~E###############################E~~~~~~", "~~~~~E#################################E~~~~~", "~~~~~C#################################C~~~~~", "~~~~C#############CCCCCCCCC#############C~~~~", "~~~C############CC~~~~~~~~~CC############C~~~", "~~~C###########E~~~~~~~~~~~~~E###########C~~~", "~~C###########E~~~~~~~~~~~~~~~E###########C~~", "~~C##########E~~~~~~~~~~~~~~~~~E##########C~~", "~~C#########E~~~~~~~~~~~~~~~~~~~E#########C~~", "~C#########C~~~~~~~~~~~~~~~~~~~~~C#########C~", "~C#########C~~~~~~~~~~~~~~~~~~~~~C#########C~", "~C########C~~~~~~~~~~~~~~~~~~~~~~~C########C~", "~C########C~~~~~~~~~~~~~~~~~~~~~~~C########C~", "C#########C~~~~~~~~~~~~~~~~~~~~~~~C#########C", "C#########C~~~~~~~~~~~~~~~~~~~~~~~C#########C", "C#########C~~~~~~~~~~~~~~~~~~~~~~~C#########C", "C#########C~~~~~~~~~~~~~~~~~~~~~~~C#########C", "C#########C~~~~~~~~~~~~~~~~~~~~~~~C#########C", "~C########C~~~~~~~~~~~~~~~~~~~~~~~C########C~", "~C########C~~~~~~~~~~~~~~~~~~~~~~~C########C~", "~C#########C~~~~~~~~~~~~~~~~~~~~~C#########C~", "~C#########C~~~~~~~~~~~~~~~~~~~~~C#########C~", "~~C#########E~~~~~~~~~~~~~~~~~~~E#########C~~", "~~C##########E~~~~~~~~~~~~~~~~~E##########C~~", "~~C###########E~~~~~~~~~~~~~~~E###########C~~", "~~~C###########E~~~~~~~~~~~~~E###########C~~~", "~~~C############CC~~~~~~~~~CC############C~~~", "~~~~C#############CCCCCCCCC#############C~~~~", "~~~~~C#################################C~~~~~", "~~~~~E#################################E~~~~~", "~~~~~~E###############################E~~~~~~", "~~~~~~~E#############################E~~~~~~~", "~~~~~~~~EC#########################CE~~~~~~~~", "~~~~~~~~~~C#######################C~~~~~~~~~~", "~~~~~~~~~~~CC###################CC~~~~~~~~~~~", "~~~~~~~~~~~~~CCC#############CCC~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~CCCC#####CCCC~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~CCCCC~~~~~~~~~~~~~~~~~~~~"});
        pattern.add(new String[]{"~~~~~~~~~~~~~~~~~~XXXXXXXXX~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~GGG#########GGG~~~~~~~~~~~~~~~", "~~~~~~~~~~~~GGG###############GGG~~~~~~~~~~~~", "~~~~~~~~~~~G#####################G~~~~~~~~~~~", "~~~~~~~~~GG#######################GG~~~~~~~~~", "~~~~~~~~E###########################E~~~~~~~~", "~~~~~~~E#############################E~~~~~~~", "~~~~~~E###############################E~~~~~~", "~~~~~E#################################E~~~~~", "~~~~G###################################G~~~~", "~~~~G##############GGGGGGG##############G~~~~", "~~~G#############GG~~~~~~~GG#############G~~~", "~~G############EG~~~~~~~~~~~GE############G~~", "~~G###########E~~~~~~~~~~~~~~~E###########G~~", "~~G##########E~~~~~~~~~~~~~~~~~E##########G~~", "~G##########E~~~~~~~~~~~~~~~~~~~E##########G~", "~G##########G~~~~~~~~~~~~~~~~~~~G##########G~", "~G#########G~~~~~~~~~~~~~~~~~~~~~G#########G~", "X##########G~~~~~~~~~~~~~~~~~~~~~G##########X", "X#########G~~~~~~~~~~~~~~~~~~~~~~~G#########X", "X#########G~~~~~~~~~~~~~~~~~~~~~~~G#########X", "X#########G~~~~~~~~~~~~~~~~~~~~~~~G#########X", "X#########G~~~~~~~~~~~~~~~~~~~~~~~G#########X", "X#########G~~~~~~~~~~~~~~~~~~~~~~~G#########X", "X#########G~~~~~~~~~~~~~~~~~~~~~~~G#########X", "X#########G~~~~~~~~~~~~~~~~~~~~~~~G#########X", "X##########G~~~~~~~~~~~~~~~~~~~~~G##########X", "~G#########G~~~~~~~~~~~~~~~~~~~~~G#########G~", "~G##########G~~~~~~~~~~~~~~~~~~~G##########G~", "~G##########E~~~~~~~~~~~~~~~~~~~E##########G~", "~~G##########E~~~~~~~~~~~~~~~~~E##########G~~", "~~G###########E~~~~~~~~~~~~~~~E###########G~~", "~~G############EG~~~~~~~~~~~GE############G~~", "~~~G#############GG~~~~~~~GG#############G~~~", "~~~~G##############GGGGGGG##############G~~~~", "~~~~G###################################G~~~~", "~~~~~E#################################E~~~~~", "~~~~~~E###############################E~~~~~~", "~~~~~~~E#############################E~~~~~~~", "~~~~~~~~E###########################E~~~~~~~~", "~~~~~~~~~GG#######################GG~~~~~~~~~", "~~~~~~~~~~~G#####################G~~~~~~~~~~~", "~~~~~~~~~~~~GGG###############GGG~~~~~~~~~~~~", "~~~~~~~~~~~~~~~GGG#########GGG~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~XXXXXXXXX~~~~~~~~~~~~~~~~~~"});
        pattern.forEach(factoryPattern::aisle);
        factoryPattern.aisle("~~~~~~~~~~~~~~~~~~XXXXSXXXX~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~GGGG#########GGGG~~~~~~~~~~~~~~", "~~~~~~~~~~~~GG#################GG~~~~~~~~~~~~", "~~~~~~~~~~GG#####################GG~~~~~~~~~~", "~~~~~~~~~G#########################G~~~~~~~~~", "~~~~~~~~E###########################E~~~~~~~~", "~~~~~~~E#############################E~~~~~~~", "~~~~~~E###############################E~~~~~~", "~~~~~E#################################E~~~~~", "~~~~G###################################G~~~~", "~~~G###############GGGGGGG###############G~~~", "~~~G#############GG~~~~~~~GG#############G~~~", "~~G############EG~~~~~~~~~~~GE############G~~", "~~G###########E~~~~~~~~~~~~~~~E###########G~~", "~G###########E~~~~~~~~~~~~~~~~~E###########G~", "~G##########E~~~~~~~~~~~~~~~~~~~E##########G~", "~G##########G~~~~~~~~~~~~~~~~~~~G##########G~", "~G#########G~~~~~~~~~~~~~~~~~~~~~G#########G~", "X##########G~~~~~~~~~~~~~~~~~~~~~G##########X", "X#########G~~~~~~~~~~~~~~~~~~~~~~~G#########X", "X#########G~~~~~~~~~~~~~~~~~~~~~~~G#########X", "X#########G~~~~~~~~~~~~~~~~~~~~~~~G#########X", "X#########G~~~~~~~~~~~~~~~~~~~~~~~G#########X", "X#########G~~~~~~~~~~~~~~~~~~~~~~~G#########X", "X#########G~~~~~~~~~~~~~~~~~~~~~~~G#########X", "X#########G~~~~~~~~~~~~~~~~~~~~~~~G#########X", "X##########G~~~~~~~~~~~~~~~~~~~~~G##########X", "~G#########G~~~~~~~~~~~~~~~~~~~~~G#########G~", "~G##########G~~~~~~~~~~~~~~~~~~~G##########G~", "~G##########E~~~~~~~~~~~~~~~~~~~E##########G~", "~G###########E~~~~~~~~~~~~~~~~~E###########G~", "~~G###########E~~~~~~~~~~~~~~~E###########G~~", "~~G############EG~~~~~~~~~~~GE############G~~", "~~~G#############GG~~~~~~~GG#############G~~~", "~~~G###############GGGGGGG###############G~~~", "~~~~G###################################G~~~~", "~~~~~E#################################E~~~~~", "~~~~~~E###############################E~~~~~~", "~~~~~~~E#############################E~~~~~~~", "~~~~~~~~E###########################E~~~~~~~~", "~~~~~~~~~G#########################G~~~~~~~~~", "~~~~~~~~~~GG#####################GG~~~~~~~~~~", "~~~~~~~~~~~~GG#################GG~~~~~~~~~~~~", "~~~~~~~~~~~~~~GGGG#########GGGG~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~XXXXXXXXX~~~~~~~~~~~~~~~~~~");
        for (int i = 0; i < pattern.size(); i++) {
            final String[] aisle = pattern.get(i);
            final String[] aisle2 = new String[aisle.length];
            System.arraycopy(aisle, 0, aisle2, 0, aisle.length);
            pattern.set(i, aisle2);
        }
        Collections.reverse(pattern);
        pattern.forEach(factoryPattern::aisle);
        return factoryPattern.where('S', this.selfPredicate())
                .where('C', statePredicate(this.getCasingState()))
                .where('G', statePredicate(this.getCasingState())
                        .or(statePredicate(TJMetaBlocks.FUSION_GLASS.getState(BlockFusionGlass.GlassType.FUSION_GLASS_UEV))))
                .where('c', statePredicate(TJMetaBlocks.FUSION_CASING.getState(BlockFusionCasings.FusionType.FUSION_COIL_UEV)))
                .where('X', statePredicate(this.getCasingState())
                        .or(statePredicate(TJMetaBlocks.FUSION_GLASS.getState(BlockFusionGlass.GlassType.FUSION_GLASS_UEV)))
                        .or(abilityPartPredicate(ALLOWED_ABILITIES)))
                .where('E', statePredicate(this.getCasingState())
                        .or(statePredicate(TJMetaBlocks.FUSION_GLASS.getState(BlockFusionGlass.GlassType.FUSION_GLASS_UEV)))
                        .or(tilePredicate(MetaTileEntityIndustrialFusionReactor.energyHatchPredicate(10)))
                        .or(this.energyPortPredicate()))
                .where('#', isAirPredicate())
                .where('~', tile -> true)
                .build();
    }

    private IBlockState getCasingState() {
        return TJMetaBlocks.FUSION_CASING.getState(BlockFusionCasings.FusionType.FUSION_CASING_UEV);
    }

    public Predicate<BlockWorldState> energyPortPredicate() {
        return (blockWorldState) -> {
            final IBlockState blockState = blockWorldState.getBlockState();
            final Block block = blockState.getBlock();
            if (block instanceof EnergyPortCasings) {
                final EnergyPortCasings abilityCasings = (EnergyPortCasings) block;
                abilityCasings.setController(this);
                final EnergyPortCasings.AbilityType tieredCasingType = abilityCasings.getState(blockState);
                final List<EnergyPortCasings.AbilityType> currentCasing = blockWorldState.getMatchContext().getOrCreate("EnergyPort", ArrayList::new);
                final Set<BlockPos> activeStates = blockWorldState.getMatchContext().getOrCreate("activeStates", HashSet::new);
                final LongList amps = blockWorldState.getMatchContext().getOrCreate("EnergyAmps", LongArrayList::new);
                currentCasing.add(tieredCasingType);
                amps.add(abilityCasings.getAmps());
                activeStates.add(blockWorldState.getPos());
                return currentCasing.get(0).getName().equals(tieredCasingType.getName());
            }
            return false;
        };
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        this.activeStates.addAll(context.getOrDefault("activeStates", new HashSet<>()));
        final LongList energyPortAmps = context.getOrDefault("EnergyAmps", new LongArrayList());
        final List<AdvEnergyPortCasings.AbilityType> energyPorts = context.getOrDefault("EnergyPort", new ArrayList<>());
        final int fusionTier = 10;
        long energyCapacity = 0;
        for (int i = 0; i < energyPortAmps.size(); i++) {
            energyCapacity += (long) (10000000 * energyPortAmps.get(i) * Math.pow(2, energyPorts.get(i).getTier() - GAValues.LuV));
        }
        for (IEnergyContainer container : this.getAbilities(INPUT_ENERGY)) {
            energyCapacity += (long) (10000000 * container.getInputAmperage() * Math.pow(2, GAUtility.getTierByVoltage(container.getInputVoltage()) - GAValues.LuV));
        }
        this.energyContainer = new EnergyContainerHandler(this, energyCapacity, GAValues.V[fusionTier], 0, 0, 0) {
            @Override
            public String getName() {
                return "EnergyContainerInternal";
            }
        };
        ((EnergyContainerHandler) this.energyContainer).setEnergyStored(this.energyContainer.getEnergyStored());
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.activeStates.clear();
        this.inputEnergyContainer = new EnergyContainerList(Collections.emptyList());
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setLong("heat", this.heat);
        data.setLong("maxHeat", this.maxHeat);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.heat = data.getLong("heat");
        this.maxHeat = data.getLong("maxHeat");
    }


    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == TJCapabilities.CAPABILITY_HEAT)
            return TJCapabilities.CAPABILITY_HEAT.cast(this);
        return super.getCapability(capability, side);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return TJTextures.FUSION_PORT_UEV;
    }

    @Override
    public TJOrientedOverlayRenderer getFrontalOverlay() {
        return TJTextures.TJ_FUSION_REACTOR_OVERLAY;
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
        return 0; // don't display parallel overclocking per tier on tooltip
    }

    @Override
    public int getTier() {
        return 10;
    }

    @Override
    public int getTierDifference(long recipeEUt) {
        return 0;
    }

    @Override
    public long heat() {
        return this.heat;
    }

    @Override
    public long maxHeat() {
        return this.maxHeat;
    }
}
