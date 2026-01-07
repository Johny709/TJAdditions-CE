package tj.builder.multicontrollers;

import gregicadditions.GAConfig;
import gregicadditions.capabilities.GregicAdditionsCapabilities;
import gregicadditions.machines.GATileEntities;
import gregicadditions.machines.multi.IMaintenance;
import gregicadditions.machines.multi.multiblockpart.MetaTileEntityMaintenanceHatch;
import gregicadditions.machines.multi.multiblockpart.MetaTileEntityMufflerHatch;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.*;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.multiblock.PatternMatchContext;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.XSTR;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tj.builder.WidgetTabBuilder;
import tj.capability.IMuffler;
import tj.capability.IProgressBar;
import tj.capability.ProgressBar;
import tj.gui.TJGuiTextures;
import tj.gui.TJHorizontoalTabListRenderer;
import tj.gui.widgets.TJProgressBarWidget;

import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.*;
import java.util.stream.Collectors;

import static gregicadditions.capabilities.GregicAdditionsCapabilities.MAINTENANCE_HATCH;
import static gregicadditions.capabilities.MultiblockDataCodes.STORE_TAPED;
import static tj.gui.TJGuiTextures.*;
import static tj.gui.TJHorizontoalTabListRenderer.HorizontalStartCorner.LEFT;
import static tj.gui.TJHorizontoalTabListRenderer.VerticalLocation.BOTTOM;

public abstract class TJMultiblockDisplayBase extends MultiblockWithDisplayBase implements IControllable, IMaintenance, IMuffler {

    private final List<ItemStack> recoveryItems = new ArrayList<ItemStack>() {{
        add(OreDictUnifier.get(OrePrefix.dustTiny, Materials.Ash));
    }};

    private final boolean hasMuffler;
    private final boolean hasMaintenance;

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

    public TJMultiblockDisplayBase(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
        this.hasMuffler = false;
        this.hasMaintenance = true;
        this.maintenance_problems = 0b000000;
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
        return abilities.getOrDefault(MAINTENANCE_HATCH, Collections.emptyList()).size() == 1;
    }

    /**
     * Used to calculate whether a maintenance problem should happen based on machine time active
     * @param duration duration in ticks to add to the counter of active time
     */
    public void calculateMaintenance(int duration) {
        MetaTileEntityMaintenanceHatch maintenanceHatch = getAbilities(GregicAdditionsCapabilities.MAINTENANCE_HATCH).get(0);
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
            Tuple<Byte, Integer> data = hatch.readMaintenanceData();
            this.maintenance_problems = data.getFirst();
            this.timeActive = data.getSecond();
        }
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        if (this.hasMaintenance) {
            if (getAbilities(GregicAdditionsCapabilities.MAINTENANCE_HATCH).isEmpty())
                return;
            MetaTileEntityMaintenanceHatch maintenanceHatch = getAbilities(GregicAdditionsCapabilities.MAINTENANCE_HATCH).get(0);
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
    protected ModularUI.Builder createUITemplate(EntityPlayer entityPlayer) {
        int height = this.getExtended();
        int[][] barMatrix = null;
        height += this.getHolder().getMetaTileEntity() instanceof IProgressBar && (barMatrix = ((IProgressBar) this.getHolder().getMetaTileEntity()).getBarMatrix()) != null ? barMatrix.length * 10 : 0;
        ModularUI.Builder builder = ModularUI.extendedBuilder();
        WidgetTabBuilder tabBuilder = new WidgetTabBuilder()
                .setTabListRenderer(() -> new TJHorizontoalTabListRenderer(LEFT, BOTTOM))
                .setPosition(-10, 1)
                .offsetPosition(0, height)
                .offsetY(132);
        if (height > 0)
            builder.image(-10, 132, 195, height, TJGuiTextures.MULTIBLOCK_DISPLAY_SLICE);
        builder.image(-10, -20, 195, 152, TJGuiTextures.MULTIBLOCK_DISPLAY_SCREEN);
        builder.image(-10, 132 + height, 195, 85, TJGuiTextures.MULTIBLOCK_DISPLAY_SLOTS);
        this.addTabs(tabBuilder, entityPlayer);
        if (barMatrix != null)
            this.addBars(barMatrix, builder);
        builder.bindPlayerInventory(entityPlayer.inventory, GuiTextures.SLOT ,-3, 134 + height);
        builder.widget(new LabelWidget(0, -13, this.getMetaFullName(), 0xFFFFFF));
        builder.widget(tabBuilder.build());
        return builder;
    }

    private void addBars(int[][] barMatrix, ModularUI.Builder builder) {
        Queue<ProgressBar> bars = new ArrayDeque<>();
        ((IProgressBar) this.getHolder().getMetaTileEntity()).getProgressBars(bars, new ProgressBar.ProgressBarBuilder());
        for (int i = 0; i < barMatrix.length; i++) {
            int[] column = barMatrix[i];
            for (int j = 0; j < column.length; j++) {
                ProgressBar bar = bars.poll();
                int height = 183 / column.length;
                builder.widget(new TJProgressBarWidget(-3 + (j * height), 132 + (i * 10), height, 10, bar.getProgress(), bar.getMaxProgress(), bar.isFluid())
                        .setStartTexture(TJGuiTextures.FLUID_BAR_START).setEndTexture(TJGuiTextures.FLUID_BAR_END)
                        .setTexture(TJGuiTextures.FLUID_BAR).setBarTexture(bar.getBarTexture())
                        .setLocale(bar.getLocale(), bar.getParams())
                        .setFluid(bar.getFluidStackSupplier()));
            }
        }
    }

    protected int getExtended() {
        return 0;
    }

    @OverridingMethodsMustInvokeSuper
    protected void addTabs(WidgetTabBuilder tabBuilder, EntityPlayer player) {
        tabBuilder.addTab("tj.multiblock.tab.display", this.getStackForm(), this::mainDisplayTab);
        tabBuilder.addTab("tj.multiblock.tab.maintenance", GATileEntities.MAINTENANCE_HATCH[0].getStackForm(), maintenanceTab ->
                maintenanceTab.add(new AdvancedTextWidget(10, -2 - this.getExtended(), textList -> {
            MultiblockDisplaysUtility.mufflerDisplay(textList, !this.hasMufflerHatch() || this.isMufflerFaceFree());
            MultiblockDisplaysUtility.maintenanceDisplay(textList, this.maintenance_problems, this.hasProblems());
            }, 0xFFFFFF).setMaxWidthLimit(180)));
    }

    protected void mainDisplayTab(List<Widget> widgetGroup) {
        widgetGroup.add(new AdvancedTextWidget(10, -2 - this.getExtended(), this::addDisplayText, 0xFFFFFF)
                .setMaxWidthLimit(180).setClickHandler(this::handleDisplayClick));
        widgetGroup.add(new ToggleButtonWidget(172, 169, 18, 18, POWER_BUTTON, this::isWorkingEnabled, this::setWorkingEnabled)
                .setTooltipText("machine.universal.toggle.run.mode"));
        widgetGroup.add(new ToggleButtonWidget(172, 133, 18, 18, CAUTION_BUTTON, this::getDoStructureCheck, this::setDoStructureCheck)
                .setTooltipText("machine.universal.toggle.check.mode"));
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        MultiblockDisplaysUtility.isInvalid(textList, isStructureFormed());
    }

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
        MetaTileEntityMufflerHatch muffler = getAbilities(GregicAdditionsCapabilities.MUFFLER_HATCH).get(0);
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
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.maintenance_problems = data.getByte("Maintenance");
        this.timeActive = data.getInteger("ActiveTimer");
        this.isWorkingEnabled = data.getBoolean("IsWorking");
    }
}
