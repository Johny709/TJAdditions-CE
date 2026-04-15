package tj.builder.multicontrollers;

import gregicadditions.GAConfig;
import gregicadditions.capabilities.GregicAdditionsCapabilities;
import gregicadditions.item.GAHeatingCoil;
import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.GAMultiblockCasing;
import gregicadditions.item.GAMultiblockCasing2;
import gregicadditions.machines.GATileEntities;
import gregicadditions.machines.multi.IMaintenance;
import gregicadditions.machines.multi.multiblockpart.MetaTileEntityMaintenanceHatch;
import gregicadditions.machines.multi.multiblockpart.MetaTileEntityMufflerHatch;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.*;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.multiblock.BlockWorldState;
import gregtech.api.multiblock.PatternMatchContext;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.XSTR;
import gregtech.common.blocks.BlockWireCoil;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import tj.TJValues;
import tj.builder.WidgetTabBuilder;
import tj.capability.IMachineHandler;
import tj.capability.IMuffler;
import tj.capability.IProgressBar;
import tj.capability.ProgressBar;
import tj.gui.TJGuiTextures;
import tj.gui.TJHorizontoalTabListRenderer;
import tj.gui.widgets.AdvancedDisplayWidget;
import tj.gui.widgets.TJLabelWidget;
import tj.gui.widgets.TJProgressBarWidget;
import tj.gui.widgets.impl.ScrollableDisplayWidget;
import tj.gui.widgets.impl.AnimatedImageWidget;
import tj.multiblockpart.TJMultiblockAbility;

import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import static gregicadditions.capabilities.GregicAdditionsCapabilities.MAINTENANCE_HATCH;
import static gregicadditions.capabilities.MultiblockDataCodes.STORE_TAPED;
import static gregtech.api.metatileentity.multiblock.MultiblockAbility.IMPORT_ITEMS;
import static tj.gui.TJGuiTextures.*;
import static tj.gui.TJHorizontoalTabListRenderer.HorizontalStartCorner.LEFT;
import static tj.gui.TJHorizontoalTabListRenderer.VerticalLocation.BOTTOM;

public abstract class TJMultiblockControllerBase extends MultiblockControllerBase implements IControllable, IMaintenance, IMuffler, IMachineHandler {

    private final List<ItemStack> recoveryItems = new ArrayList<ItemStack>() {{
        add(OreDictUnifier.get(OrePrefix.dustTiny, Materials.Ash));
    }};

    private final boolean hasMuffler;
    private final boolean hasMaintenance;
    private final boolean hasDistinct;

    public static final XSTR XSTR_RAND = new XSTR();

    private int timeActive;
    private static final int minimumMaintenanceTime = 5184000; // 72 real-life hours = 5184000 ticks

    // Used for data preservation with Maintenance Hatch
    private boolean storedTaped = false;

    /**
     * This value stores whether each of the 5 maintenance problems have been fixed.
     * A value of 0 means the problem is not fixed, else it is fixed
     * Value positions correspond to the following from left to right: 0=Wrench, 1=Screwdriver, 2=Soft Hammer, 3=Hard Hammer, 4=Wire Cutter, 5=Crowbar
     */
    protected byte maintenance_problems;

    protected boolean doStructureCheck = false;
    protected boolean isWorkingEnabled = true;

    protected IItemHandlerModifiable importItemInventory;
    protected IItemHandlerModifiable exportItemInventory;
    protected IMultipleTankHandler importFluidTank;
    protected IMultipleTankHandler exportFluidTank;
    protected IEnergyContainer inputEnergyContainer;
    protected IEnergyContainer outputEnergyContainer;

    protected Instant placedDown = Instant.now();
    protected Instant activeDate;

    public TJMultiblockControllerBase(ResourceLocation metaTileEntityId) {
        this(metaTileEntityId, true, true);
    }

    public TJMultiblockControllerBase(ResourceLocation metaTileEntityId, boolean hasMaintenance) {
        this(metaTileEntityId, hasMaintenance, true);
    }

    public TJMultiblockControllerBase(ResourceLocation metaTileEntityId, boolean hasMaintenance, boolean hasDistinct) {
        super(metaTileEntityId);
        this.hasMuffler = false;
        this.hasDistinct = hasDistinct;
        this.hasMaintenance = hasMaintenance;
        this.maintenance_problems = 0b000000;
        if (!hasMaintenance) this.maintenance_problems = 0b111111;
    }

    /**
     * Sets the maintenance problem corresponding to index to fixed
     *
     * @param index the index of the maintenance problem
     */
    public void setMaintenanceFixed(int index) {
        this.maintenance_problems |= 1 << index;
    }

    /**
     * Used to cause a single random maintenance problem
     */
    protected void causeProblems() {
        this.maintenance_problems &= ~(1 << ((int) (XSTR_RAND.nextFloat()*5)));
    }

    /**
     *
     * @return the byte value representing the maintenance problems
     */
    public byte getProblems() {
        return this.maintenance_problems;
    }

    /**
     *
     * @return the amount of maintenance problems the multiblock has
     */
    public int getNumProblems() {
        return 6 - Integer.bitCount(this.maintenance_problems);
    }

    /**
     *
     * @return whether the multiblock has any maintenance problems
     */
    public boolean hasProblems() {
        return this.maintenance_problems < 63;
    }

    @Override
    protected boolean checkStructureComponents(List<IMultiblockPart> parts, Map<MultiblockAbility<Object>, List<Object>> abilities) {
        return !this.hasMaintenance || abilities.getOrDefault(MAINTENANCE_HATCH, Collections.emptyList()).size() == 1;
    }

    /**
     * Used to calculate whether a maintenance problem should happen based on machine time active
     * @param duration duration in ticks to add to the counter of active time
     */
    @Override
    public void calculateMaintenance(int duration) {
        final MetaTileEntityMaintenanceHatch maintenanceHatch = getAbilities(GregicAdditionsCapabilities.MAINTENANCE_HATCH).get(0);
        if (maintenanceHatch.getType() == 2 || !GAConfig.GT5U.enableMaintenance) {
            return;
        }

        this.timeActive += duration;
        if (minimumMaintenanceTime - this.timeActive <= 0)
            if(XSTR_RAND.nextFloat() - 0.75f >= 0) {
                this.causeProblems();
                maintenanceHatch.setTaped(false);
                this.timeActive -= minimumMaintenanceTime;
            }
    }

    public void storeTaped(boolean isTaped) {
        this.storedTaped = isTaped;
        this.writeCustomData(STORE_TAPED, buf -> buf.writeBoolean(isTaped));
    }

    private void readMaintenanceData(MetaTileEntityMaintenanceHatch hatch) {
        if (hatch.hasMaintenanceData()) {
            final Tuple<Byte, Integer> data = hatch.readMaintenanceData();
            this.maintenance_problems = data.getFirst();
            this.timeActive = data.getSecond();
        }
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        final List<IItemHandlerModifiable> itemHandlerCollection = new ArrayList<>();
        itemHandlerCollection.addAll(this.getAbilities(TJMultiblockAbility.CIRCUIT_SLOT));
        itemHandlerCollection.addAll(this.getAbilities(MultiblockAbility.IMPORT_ITEMS));

        this.importItemInventory = new ItemHandlerList(itemHandlerCollection);
        this.importFluidTank = new FluidTankList(true, this.getAbilities(MultiblockAbility.IMPORT_FLUIDS));
        this.exportItemInventory = new ItemHandlerList(this.getAbilities(MultiblockAbility.EXPORT_ITEMS));
        this.exportFluidTank = new FluidTankList(true, this.getAbilities(MultiblockAbility.EXPORT_FLUIDS));
        this.inputEnergyContainer = new EnergyContainerList(this.getAbilities(MultiblockAbility.INPUT_ENERGY));
        this.outputEnergyContainer = new EnergyContainerList(this.getAbilities(MultiblockAbility.OUTPUT_ENERGY));
        if (this.hasMaintenance) {
            if (getAbilities(GregicAdditionsCapabilities.MAINTENANCE_HATCH).isEmpty())
                return;
            final MetaTileEntityMaintenanceHatch maintenanceHatch = getAbilities(GregicAdditionsCapabilities.MAINTENANCE_HATCH).get(0);
            if (maintenanceHatch.getType() == 2 || !GAConfig.GT5U.enableMaintenance) {
                this.maintenance_problems = 0b111111;
            } else {
                readMaintenanceData(maintenanceHatch);
                if (maintenanceHatch.getType() == 0 && this.storedTaped) {
                    maintenanceHatch.setTaped(true);
                    this.storeTaped(false);
                }
            }
        }
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.importItemInventory = new ItemHandlerList(Collections.emptyList());
        this.importFluidTank = new FluidTankList(true);
        this.exportItemInventory = new ItemHandlerList(Collections.emptyList());
        this.exportFluidTank = new FluidTankList(true);
        this.inputEnergyContainer = new EnergyContainerList(Collections.emptyList());
        this.outputEnergyContainer = new EnergyContainerList(Collections.emptyList());
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        int height = this.getExtended();
        int[][] barMatrix = null;
        height += this.getHolder().getMetaTileEntity() instanceof IProgressBar && (barMatrix = ((IProgressBar) this.getHolder().getMetaTileEntity()).getBarMatrix()) != null ? barMatrix.length * 10 : 0;
        final ModularUI.Builder builder = ModularUI.builder(GuiTextures.BORDERED_BACKGROUND, 200, 216);
        final WidgetTabBuilder tabBuilder = new WidgetTabBuilder()
                .setTabListRenderer(() -> new TJHorizontoalTabListRenderer(LEFT, BOTTOM))
                .setPosition(0, 1)
                .offsetPosition(0, height)
                .offsetY(132 - this.getExtended());
        builder.image(0, -20, 200, 237 + height, GuiTextures.BORDERED_BACKGROUND)
                .image(6, -14, 188, 145, MULTIBLOCK_DISPLAY_BASE)
                .widget(new TJLabelWidget(9, -38, 184, 18, MACHINE_LABEL_2, this::getRecipeUid)
                        .setItemLabel(this.getStackForm())
                        .setLocale(this.getMetaFullName()));
        this.addTabs(tabBuilder, entityPlayer);
        if (barMatrix != null)
            this.addBars(barMatrix, builder);
        return builder.bindPlayerInventory(entityPlayer.inventory, GuiTextures.SLOT ,7, 134 + height)
                .widget(tabBuilder.build())
                .widget(tabBuilder.buildWidgetGroup())
                .widget(new AnimatedImageWidget(164, 102, 26, 26, 41, TJ_LOGO_ANIMATED))
                .build(this.getHolder(), entityPlayer);
    }

    private void addBars(int[][] barMatrix, ModularUI.Builder builder) {
        final Queue<UnaryOperator<ProgressBar.ProgressBarBuilder>> bars = new ArrayDeque<>();
        ((IProgressBar) this.getHolder().getMetaTileEntity()).getProgressBars(bars);
        for (int i = 0; i < barMatrix.length; i++) {
            final int[] column = barMatrix[i];
            for (int j = 0; j < column.length; j++) {
                final ProgressBar bar = bars.poll().apply(new ProgressBar.ProgressBarBuilder()).build();
                final int height = 188 / column.length;
                builder.widget(new TJProgressBarWidget(7 + (j * height), 132 + (i * 10), height, 10, bar.getProgress(), bar.getMaxProgress(), bar.isFluid())
                        .setTexture(TJGuiTextures.FLUID_BAR).setBarTexture(bar.getBarTexture())
                        .setLocale(bar.getLocale(), bar.getParams())
                        .setFluid(bar.getFluidStackSupplier()));
            }
        }
    }

    @OverridingMethodsMustInvokeSuper
    protected void addTabs(WidgetTabBuilder tabBuilder, EntityPlayer player) {
        tabBuilder.addTab("tj.multiblock.tab.display", this.getStackForm(), this::mainDisplayTab);
        tabBuilder.addTab("tj.multiblock.tab.maintenance", GATileEntities.MAINTENANCE_HATCH[0].getStackForm(), maintenanceTab ->
                maintenanceTab.add(new ScrollableDisplayWidget(10, -11, 187, 140)
                        .addDisplayWidget(new AdvancedDisplayWidget(0, 0, this::addMaintenanceDisplayText, 0xFFFFFF)
                                .setMaxWidthLimit(180))
                        .setScrollPanelWidth(3)));
    }

    @OverridingMethodsMustInvokeSuper
    protected void mainDisplayTab(List<Widget> widgetGroup) {
        widgetGroup.add(new ScrollableDisplayWidget(10, -11, 187, 140)
                .addDisplayWidget(new AdvancedDisplayWidget(0, 0, this::addDisplayText, 0xFFFFFF)
                        .setClickHandler(this::handleDisplayClick)
                        .setMaxWidthLimit(180))
                .setScrollPanelWidth(3));
        widgetGroup.add(new ToggleButtonWidget(175, 169, 18, 18, POWER_BUTTON, this::isWorkingEnabled, this::setWorkingEnabled)
                .setTooltipText("machine.universal.toggle.run.mode"));
        widgetGroup.add(new ToggleButtonWidget(175, 133, 18, 18, CAUTION_BUTTON, this::getDoStructureCheck, this::setDoStructureCheck)
                .setTooltipText("machine.universal.toggle.check.mode"));
    }

    protected void addDisplayText(GUIDisplayBuilder builder) {
        if (!this.isStructureFormed()) {
            final ITextComponent tooltip = new TextComponentTranslation("gregtech.multiblock.invalid_structure.tooltip");
            tooltip.setStyle(new Style().setColor(TextFormatting.GRAY));
            builder.customLine(text -> text.addTextComponent(new TextComponentTranslation("gregtech.multiblock.invalid_structure")
                    .setStyle(new Style().setColor(TextFormatting.RED)
                            .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, tooltip)))));
        }
    }

    protected void addMaintenanceDisplayText(GUIDisplayBuilder builder) {
        final Instant now = Instant.now();
        final SimpleDateFormat dateFormat = new SimpleDateFormat("E, MMMM d, yyyy hh:mm:ss aa");
        final long timeElapsed = now.getEpochSecond() - this.placedDown.getEpochSecond();
        builder.addTranslationLine("tj.multiblock.date.placed_down", dateFormat.format(Date.from(this.placedDown)))
                .addTranslationLine("tj.multiblock.date.ago", TJValues.thousandFormat.format(timeElapsed / 3600), TJValues.thousandFormat.format((timeElapsed % 3600) / 60), TJValues.thousandFormat.format(timeElapsed % 60))
                .addEmptyLine()
                .addMufflerDisplayLine(!this.hasMufflerHatch() || this.isMufflerFaceFree(), 999)
                .addMaintenanceDisplayLines(this.getProblems(), this.hasProblems(), 1000);
    }

    protected void handleDisplayClick(String componentData, Widget.ClickData clickData) {}

    @Override
    public boolean isWorkingEnabled() {
        return this.isWorkingEnabled;
    }

    @Override
    public void setWorkingEnabled(boolean isActivationAllowed) {
        this.isWorkingEnabled = isActivationAllowed;
        this.markDirty();
    }

    protected boolean getDoStructureCheck() {
        if (this.isStructureFormed())
            this.doStructureCheck = false;
        return this.doStructureCheck;
    }

    protected void setDoStructureCheck(boolean check) {
        if (this.isStructureFormed()) {
            this.doStructureCheck = true;
            this.invalidateStructure();
            this.structurePattern = this.createStructurePattern();
        }
    }

    protected void outputRecoveryItems() {
        final MetaTileEntityMufflerHatch muffler = getAbilities(GregicAdditionsCapabilities.MUFFLER_HATCH).get(0);
        muffler.recoverItemsTable(recoveryItems.stream().map(ItemStack::copy).collect(Collectors.toList()));
    }

    public boolean isMufflerFaceFree() {
        return this.isStructureFormed() && this.hasMuffler && getAbilities(GregicAdditionsCapabilities.MUFFLER_HATCH).get(0).isFrontFaceFree();
    }

    protected void setRecoveryItems(ItemStack... recoveryItems) {
        this.recoveryItems.clear();
        this.recoveryItems.addAll(Arrays.asList(recoveryItems));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void runMufflerEffect(float xPos, float yPos, float zPos, float xSpd, float ySpd, float zSpd) {
        this.getWorld().spawnParticle(EnumParticleTypes.SMOKE_LARGE, xPos, yPos, zPos, xSpd, ySpd, zSpd);
    }

    @Override
    public boolean isActive() {
        return this.isStructureFormed();
    }

    public boolean hasMufflerHatch() {
        return this.hasMuffler;
    }

    @Override
    public boolean hasMaintenanceHatch() {
        return this.hasMaintenance;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE)
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        return super.getCapability(capability, side);
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeByte(this.maintenance_problems);
        buf.writeInt(this.timeActive);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.maintenance_problems = buf.readByte();
        this.timeActive = buf.readInt();
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == STORE_TAPED) {
            this.storedTaped = buf.readBoolean();
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setByte("Maintenance", this.maintenance_problems);
        data.setInteger("ActiveTimer", this.timeActive);
        data.setBoolean("IsWorking", this.isWorkingEnabled);
        data.setLong("placedDownDate", this.placedDown.getEpochSecond());
        if (this.activeDate != null)
            data.setLong("activeTime", this.activeDate.getEpochSecond());
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.maintenance_problems = data.getByte("Maintenance");
        this.timeActive = data.getInteger("ActiveTimer");
        this.isWorkingEnabled = data.getBoolean("IsWorking");
        if (data.hasKey("placedDownDate"))
            this.placedDown = Instant.ofEpochSecond(data.getLong("placedDownDate"));
        if (data.hasKey("activeTime"))
            this.activeDate = Instant.ofEpochSecond(data.getLong("activeTime"));
    }

    public boolean hasDistinct() {
        return this.hasDistinct;
    }

    @Override
    public IItemHandlerModifiable getInputBus(int index) {
        return this.getAbilities(IMPORT_ITEMS).get(index);
    }

    @Override
    public IItemHandlerModifiable getImportItemInventory() {
        return this.importItemInventory;
    }

    @Override
    public IItemHandlerModifiable getExportItemInventory() {
        return this.exportItemInventory;
    }

    @Override
    public IMultipleTankHandler getImportFluidTank() {
        return this.importFluidTank;
    }

    @Override
    public IMultipleTankHandler getExportFluidTank() {
        return this.exportFluidTank;
    }

    @Override
    public IEnergyContainer getInputEnergyContainer() {
        return this.inputEnergyContainer;
    }

    @Override
    public IEnergyContainer getOutputEnergyContainer() {
        return this.outputEnergyContainer;
    }

    @Override
    public int getMaintenanceProblems() {
        return this.getNumProblems();
    }

    protected int getOffsetY(int y) {
        int height = this.getExtended();
        final int[][] barMatrix;
        height += this.getHolder().getMetaTileEntity() instanceof IProgressBar && (barMatrix = ((IProgressBar) this.getHolder().getMetaTileEntity()).getBarMatrix()) != null ? barMatrix.length * 10 : 0;
        return y + height;
    }

    protected int getExtended() {
        return 0;
    }

    /**
     * Recipe Uid for JEI recipe click area.
     */
    public String getRecipeUid() {
        return null;
    }

    public static Predicate<BlockWorldState> frameworkPredicate() {
        return blockWorldState -> {
            final IBlockState state = blockWorldState.getBlockState();
            final Block block = state.getBlock();
            if (block instanceof GAMultiblockCasing) {
                final int tier = GAMetaBlocks.MUTLIBLOCK_CASING.getState(state).getTier();
                if (tier < 0) return false;
                return blockWorldState.getMatchContext().getOrPut("frameworkTier", tier) == tier;
            } else if (block instanceof GAMultiblockCasing2) {
                final int tier = GAMetaBlocks.MUTLIBLOCK_CASING2.getState(state).getTier();
                if (tier < 0) return false;
                return blockWorldState.getMatchContext().getOrPut("frameworkTier", tier) == tier;
            }
            return false;
        };
    }

    public static Predicate<BlockWorldState> coilPredicate() {
        return blockWorldState -> {
            final IBlockState state = blockWorldState.getBlockState();
            final Block block = state.getBlock();
            if (block instanceof BlockWireCoil) {
                final BlockWireCoil.CoilType coilType = ((BlockWireCoil) block).getState(state);
                final String name = blockWorldState.getMatchContext().getOrPut("coilName", coilType.getName());
                if (!coilType.getName().equals(name)) return false;
                blockWorldState.getMatchContext().getOrPut("coilLevel", coilType.getLevel());
                blockWorldState.getMatchContext().getOrPut("coilTemperature", coilType.getCoilTemperature());
                blockWorldState.getMatchContext().getOrPut("coilEnergyDiscount", coilType.getEnergyDiscount());
                return true;
            } else if (block instanceof GAHeatingCoil) {
                final GAHeatingCoil.CoilType coilType = ((GAHeatingCoil) block).getState(state);
                final String name = blockWorldState.getMatchContext().getOrPut("coilName", coilType.getName());
                if (!coilType.getName().equals(name)) return false;
                blockWorldState.getMatchContext().getOrPut("coilLevel", coilType.getLevel());
                blockWorldState.getMatchContext().getOrPut("coilTemperature", coilType.getCoilTemperature());
                blockWorldState.getMatchContext().getOrPut("coilEnergyDiscount", coilType.getEnergyDiscount());
                return true;
            }
            return false;
        };
    }
}
