package tj.machines.multi.electric;

import gregicadditions.item.GAMetaItems;
import gregtech.api.capability.impl.FuelRecipeLogic;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.Widget;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.stats.IMetaItemStats;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.common.metatileentities.electric.multiblockpart.MetaTileEntityRotorHolder;
import gregtech.common.metatileentities.multi.electric.generator.MetaTileEntityLargeTurbine;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import tj.TJValues;
import tj.builder.WidgetTabBuilder;
import tj.builder.multicontrollers.GUIDisplayBuilder;
import tj.capability.impl.workable.XLAtmosphereCollectorWorkableHandler;
import tj.items.behaviours.TurbineUpgradeBehaviour;
import tj.items.handlers.FilteredItemStackHandler;
import tj.mui.widgets.impl.AdvancedDisplayWidget;
import tj.mui.widgets.impl.ScrollableDisplayWidget;
import tj.mui.widgets.impl.TJSlotWidget;
import tj.util.TextUtils;
import tj.util.TooltipHelper;

import javax.annotation.Nullable;
import java.util.List;

import static gregtech.api.gui.widgets.AdvancedTextWidget.withButton;
import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;

public class MetaTileEntityXLAtmosphereCollector extends MetaTileEntityLargeAtmosphereCollector {

    private final int pageSize = 10;
    private int pageIndex;
    private int parallels = 12;

    public MetaTileEntityXLAtmosphereCollector(ResourceLocation metaTileEntityId, MetaTileEntityLargeTurbine.TurbineType turbineType) {
        super(metaTileEntityId, turbineType);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityXLAtmosphereCollector(this.metaTileEntityId, this.turbineType);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("tj.multiblock.turbine.description"));
        tooltip.add(I18n.format("tj.multiblock.turbine.fast_mode.description"));
        TooltipHelper.shiftTextJEI(tooltip, tip -> {
            tip.add(I18n.format("tj.multiblock.universal.tooltip.1", this.turbineType.recipeMap.getLocalizedName()));
            tip.add(I18n.format("tj.multiblock.universal.tooltip.2", 12));
            tip.add(I18n.format("tj.multiblock.turbine.tooltip.efficiency"));
            tip.add(I18n.format("tj.multiblock.turbine.tooltip.efficiency.normal", (int) XLAtmosphereCollectorWorkableHandler.getTurbineBonus()));
            tip.add(I18n.format("tj.multiblock.turbine.tooltip.efficiency.fast", 100));
        });
    }

    @Override
    protected FuelRecipeLogic createWorkable(long maxVoltage) {
        this.airCollectorHandler = new XLAtmosphereCollectorWorkableHandler(this, this.recipeMap, this::getEnergyContainer, this::getImportFluidHandler);
        this.airCollectorHandler.setExportFluidsSupplier(this::getExportFluidHandler);
        this.fastModeConsumer = this.airCollectorHandler::setFastMode;
        return this.airCollectorHandler;
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return new FilteredItemStackHandler(this, 1, 1)
                .setItemStackPredicate((slot, stack) -> {
                    Item item = stack.getItem();
                    if (item instanceof MetaItem<?>) {
                        MetaItem<?>.MetaValueItem metaItem = ((MetaItem<?>) item).getItem(stack);
                        if (metaItem != null) {
                            List<IMetaItemStats> stats = metaItem.getAllStats();
                            return !stats.isEmpty() && stats.get(0) instanceof TurbineUpgradeBehaviour;
                        }
                    }
                    return false;
                }).setOnContentsChangedPost((slot, stack) -> {
                    this.parallels = 12;
                    Item item = stack.getItem();
                    if (item instanceof MetaItem<?>)
                        this.parallels += ((TurbineUpgradeBehaviour) ((MetaItem<?>) item).getItem(stack).getAllStats().get(0)).getExtraParallels();
                    this.writeCustomData(10, buf -> buf.writeInt(this.parallels));
                    if (this.isStructureFormed())
                        this.invalidateStructure();
                    this.structurePattern = this.createStructurePattern();
                    this.markDirty();
                });
    }

    @Override
    protected void addTabs(WidgetTabBuilder tabBuilder) {
        super.addTabs(tabBuilder);
        tabBuilder.addWidget(new TJSlotWidget<>(this.importItems, 0, 175, 191)
                .setActiveBackgroundTexture(GuiTextures.SLOT, GuiTextures.TURBINE_OVERLAY));
        tabBuilder.addTab("tj.multiblock.tab.rotor", GAMetaItems.HUGE_TURBINE_ROTOR.getStackForm(), rotorTab -> rotorTab.add(new ScrollableDisplayWidget(10, -11, 187, 140)
                .addDisplayWidget(new AdvancedDisplayWidget(0, 2, this::addRotorDisplayText, 0xFFFFFF)
                        .setClickHandler(this::handleRotorDisplayClick)
                        .setMaxWidthLimit(180))
                .setScrollPanelWidth(3)));
    }

    @Override
    protected void addDisplayText(GUIDisplayBuilder builder) {
        super.addDisplayText(builder);
        if (!this.isStructureFormed()) return;
        builder.customLine(text -> {
                    text.addTranslationLine("machine.universal.consuming.seconds", TJValues.thousandFormat.format(this.airCollectorHandler.getConsumption()),
                            this.airCollectorHandler.getFuelName(),
                            TJValues.thousandFormat.format(this.airCollectorHandler.getMaxProgress() / 20));
                    final FluidStack fuelStack = this.airCollectorHandler.getFuelStack();
                    final int fuelAmount = fuelStack == null ? 0 : fuelStack.amount;

                    final ITextComponent fuelName = new TextComponentTranslation(fuelAmount == 0 ? "gregtech.fluid.empty" : fuelStack.getUnlocalizedName());
                    text.addTranslationLine("tj.multiblock.fuel_amount", TJValues.thousandFormat.format(fuelAmount), fuelName.getUnformattedText());

                    text.addTranslationLine("tj.multiblock.extreme_turbine.energy", TJValues.thousandFormat.format(this.airCollectorHandler.getProduction()));

                    text.addTextComponent(new TextComponentTranslation("tj.multiblock.extreme_turbine.fast_mode").appendText(" ")
                            .appendSibling(this.airCollectorHandler.isFastMode() ? withButton(new TextComponentTranslation("tj.multiblock.extreme_turbine.fast_mode.true"), "true")
                                    : withButton(new TextComponentTranslation("tj.multiblock.extreme_turbine.fast_mode.false"), "false")));
                }).addIsWorkingLine(this.airCollectorHandler.isWorkingEnabled(), this.airCollectorHandler.isActive(), this.airCollectorHandler.getProgress(), this.airCollectorHandler.getMaxProgress())
                .addRecipeInputLine(this.airCollectorHandler)
                .addRecipeOutputLine(this.airCollectorHandler);
    }

    private void addRotorDisplayText(GUIDisplayBuilder builder) {
        builder.addTextComponent(new TextComponentString(":")
                .appendText(" ")
                .appendSibling(withButton(new TextComponentString("[<]"), "leftPage"))
                .appendText(" ")
                .appendSibling(withButton(new TextComponentString("[>]"), "rightPage")));
        int rotorHolderSize = getRotorHolders().size();
        for (int i = this.pageIndex, rotorIndex = i + 1; i < this.pageIndex + this.pageSize; i++, rotorIndex++) {
            if (i < rotorHolderSize) {
                final MetaTileEntityRotorHolder rotorHolder = this.getAbilities(ABILITY_ROTOR_HOLDER).get(i);

                final double durability = rotorHolder.getRotorDurability() * 100;
                final double efficiency = rotorHolder.getRotorEfficiency() * 100;

                final String colorText = !rotorHolder.hasRotorInInventory() ? "§f"
                        : durability > 25 ? "§a"
                        : durability > 10 ? "§e" : "§c";

                final String rotorName = rotorHolder.getRotorInventory().getStackInSlot(0).getDisplayName();
                final String shortRotorName = rotorName.length() > 26 ? rotorName.substring(0, 26) + "..." : rotorName;
                builder.addTextComponentWithHover(new TextComponentString("-")
                        .appendText(" ")
                        .appendSibling(new TextComponentString(colorText + "[" + rotorIndex + "] " + (shortRotorName.equals("Air") ? TextUtils.translate("tj.multiblock.extreme_turbine.insertrotor") : shortRotorName))), hoverBuilder -> hoverBuilder.addTranslationLine("tj.multiblock.extreme_turbine.name", new TextComponentTranslation(rotorHolder.getRotorInventory().getStackInSlot(0).getDisplayName().equals("Air") ?
                                        "gregtech.multiblock.extreme_turbine.norotor" : rotorHolder.getRotorInventory().getStackInSlot(0).getDisplayName()))
                                .addTranslationLine("tj.multiblock.parallel.status", new TextComponentTranslation(rotorHolder.isFrontFaceFree() ? "tj.multiblock.extreme_turbine.obstructed.not"
                                        : "tj.multiblock.extreme_turbine.obstructed"))
                                .addTranslationLine("tj.multiblock.extreme_turbine.speed", TJValues.thousandFormat.format(rotorHolder.getCurrentRotorSpeed()), TJValues.thousandFormat.format(rotorHolder.getMaxRotorSpeed()))
                                .addTranslationLine("tj.multiblock.extreme_turbine.efficiency", TJValues.thousandFormat.format(efficiency))
                                .addTranslationLine("tj.multiblock.extreme_turbine.durability", TJValues.thousandFormat.format(durability))
                                .addItemStack(rotorHolder.getRotorInventory().getStackInSlot(0)));
            }
        }
    }

    private void handleRotorDisplayClick(String componentData, Widget.ClickData clickData) {
        if (componentData.equals("leftPage")) {
            if (this.pageIndex > 0)
                this.pageIndex -= this.pageSize;
        } else {
            if (this.pageIndex < this.getRotorHolders().size() - this.pageSize)
                this.pageIndex += this.pageSize;
        }
    }

    @Override
    protected BlockPattern createStructurePattern() {
        final FactoryBlockPattern factoryPattern = FactoryBlockPattern.start(RIGHT, UP, BACK)
                .aisle("CPPPCCCPPPC", "CPPPXXXPPPC", "CPPPXXXPPPC", "CPPPXXXPPPC", "CPPPXXXPPPC", "CPPPXXXPPPC", "CPPPCCCPPPC")
                .aisle("CPPPXXXPPPC", "R#########R", "CPPPCCCPPPC", "CPPPCCCPPPC", "CPPPCCCPPPC", "R#########R", "CPPPXXXPPPC")
                .aisle("CPPPXXXPPPC", "CPPPXXXPPPC", "CPPPCCCPPPC", "CPPPCCCPPPC", "CPPPCCCPPPC", "CPPPCCCPPPC", "CPPPXXXPPPC");
        for (int i = 0; i < (this.parallels / 4) - 2; i++) {
            factoryPattern.aisle("CPPPXXXPPPC", "CPPPCCCPPPC", "CPPPCCCPPPC", "CPPPCCCPPPC", "CPPPCCCPPPC", "CPPPCCCPPPC", "CPPPXXXPPPC");
            factoryPattern.aisle("CPPPXXXPPPC", "R#########R", "CPPPCCCPPPC", "CPPPCCCPPPC", "CPPPCCCPPPC", "R#########R", "CPPPXXXPPPC");
            factoryPattern.aisle("CPPPXXXPPPC", "CPPPCCCPPPC", "CPPPCCCPPPC", "CPPPCCCPPPC", "CPPPCCCPPPC", "CPPPCCCPPPC", "CPPPXXXPPPC");
        }
        return this.turbineType == null ? null : factoryPattern.aisle("CPPPXXXPPPC", "CPPPCCCPPPC", "CPPPCCCPPPC", "CPPPCCCPPPC", "CPPPCCCPPPC", "CPPPCCCPPPC", "CPPPXXXPPPC")
                .aisle("CPPPXXXPPPC", "R#########R", "CPPPCCCPPPC", "CPPPCCCPPPC", "CPPPCCCPPPC", "R#########R", "CPPPXXXPPPC")
                .aisle("CPPPCCCPPPC", "CPPPXXXPPPC", "CPPPXXXPPPC", "CPPPXSXPPPC", "CPPPXXXPPPC", "CPPPXXXPPPC", "CPPPCCCPPPC")
                .where('S', this.selfPredicate())
                .where('C', statePredicate(this.turbineType.casingState))
                .where('P', statePredicate(this.getPipeState()))
                .where('X', statePredicate(this.turbineType.casingState).or(abilityPartPredicate(ALLOWED_ABILITIES)))
                .where('F', abilityPartPredicate(MultiblockAbility.EXPORT_FLUIDS))
                .where('R', abilityPartPredicate(ABILITY_ROTOR_HOLDER))
                .where('#', isAirPredicate())
                .build();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger("parallels", this.parallels);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        if (data.hasKey("parallels")) {
            this.parallels = data.getInteger("parallels");
            this.structurePattern = this.createStructurePattern();
        }
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeInt(this.parallels);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.parallels = buf.readInt();
        this.structurePattern = this.createStructurePattern();
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == 10) {
            this.parallels = buf.readInt();
            this.structurePattern = this.createStructurePattern();
            this.scheduleRenderUpdate();
        }
    }

    @SideOnly(Side.CLIENT)
    public String getRecipeMapName() {
        return this.recipeMap.getLocalizedName();
    }

    @Override
    protected void reinitializeStructurePattern() {
        this.parallels = 12;
        super.reinitializeStructurePattern();
    }
}
