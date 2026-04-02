package tj.machines.multi.electric;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregicadditions.GAUtility;
import gregicadditions.GAValues;
import gregicadditions.capabilities.GregicAdditionsCapabilities;
import gregicadditions.client.ClientHandler;
import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.components.PistonCasing;
import gregicadditions.item.metal.MetalCasing2;
import gregicadditions.machines.multi.simple.LargeSimpleRecipeMapMultiblockController;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.TextFieldWidget;
import gregtech.api.metatileentity.MTETrait;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.api.multiblock.PatternMatchContext;
import gregtech.api.render.ICubeRenderer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tj.TJConfig;
import tj.builder.multicontrollers.TJMultiblockControllerBase;
import tj.builder.multicontrollers.GUIDisplayBuilder;
import tj.capability.impl.handler.INameHandler;
import tj.capability.impl.workable.NamingMachineWorkableHandler;
import tj.gui.widgets.impl.ButtonPopUpWidget;
import tj.gui.widgets.impl.TJToggleButtonWidget;
import tj.gui.widgets.impl.WindowsWidgetGroup;
import tj.textures.TJTextures;
import tj.util.EnumFacingHelper;
import tj.util.TJUtility;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static gregtech.api.gui.widgets.AdvancedTextWidget.withButton;
import static gregtech.api.metatileentity.multiblock.MultiblockAbility.*;

public class MetaTileEntityLargeNamingMachine extends TJMultiblockControllerBase implements INameHandler {

    private static final MultiblockAbility<?>[] ALLOWED_ABILITIES = {IMPORT_ITEMS, EXPORT_ITEMS, INPUT_ENERGY, GregicAdditionsCapabilities.MAINTENANCE_HATCH};
    private final NamingMachineWorkableHandler workableHandler = new NamingMachineWorkableHandler(this);
    private long maxVoltage;
    private int parallel;
    private String name = "";

    public MetaTileEntityLargeNamingMachine(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityLargeNamingMachine(this.metaTileEntityId);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gtadditions.multiblock.universal.tooltip.4", TJConfig.largeNamingMachine.stack));
        tooltip.add(I18n.format("tj.machine.naming_machine.description"));
    }

    @Override
    protected boolean checkStructureComponents(List<IMultiblockPart> parts, Map<MultiblockAbility<Object>, List<Object>> abilities) {
        return abilities.containsKey(IMPORT_ITEMS) && abilities.containsKey(EXPORT_ITEMS) && abilities.containsKey(INPUT_ENERGY) && super.checkStructureComponents(parts, abilities);
    }

    @Override
    protected boolean shouldUpdate(MTETrait trait) {
        return false;
    }

    @Override
    protected void updateFormedValid() {
        if (((this.getProblems() >> 5) & 1) != 0)
            this.workableHandler.update();
    }

    @Override
    protected void mainDisplayTab(List<Widget> widgetGroup) {
        super.mainDisplayTab(widgetGroup);
        widgetGroup.add(new ButtonPopUpWidget<>()
                .addPopup(widgetGroup1 -> true)
                .addPopup(new TJToggleButtonWidget(175, 152, 18, 18)
                        .setItemDisplay(new ItemStack(Item.getByNameOrId("enderio:item_material"), 1, 11))
                        .setToggleTexture(GuiTextures.TOGGLE_BUTTON_BACK)
                        .setTooltipText("tj.multiblock.tab.settings")
                        .useToggleTexture(true), widgetGroup1 -> {
                    widgetGroup1.addWidget(new WindowsWidgetGroup(12, 60, 160, 40, GuiTextures.BORDERED_BACKGROUND)
                            .addSubWidget(new TextFieldWidget(4, 15, 152, 18, true, this::getName, this::setName)
                                    .setValidator(str -> Pattern.compile(".*").matcher(str).matches())
                                    .setMaxStringLength(1024)));
                    return false;
                }));
    }

    @Override
    protected void addDisplayText(GUIDisplayBuilder builder) {
        super.addDisplayText(builder);
        if (!this.isStructureFormed()) return;
        builder.addVoltageInLine(this.getInputEnergyContainer())
                .addVoltageTierLine(GAUtility.getTierByVoltage(this.maxVoltage))
                .addEnergyInputLine(this.inputEnergyContainer, this.workableHandler.getEnergyPerTick())
                .addTranslationLine("tj.multiblock.max_parallel", this.parallel)
                .customLine(text -> text.addTextComponent(new TextComponentTranslation("gtadditions.multiblock.universal.distinct")
                        .appendText(" ")
                        .appendSibling(this.workableHandler.isDistinct()
                                ? withButton(new TextComponentTranslation("gtadditions.multiblock.universal.distinct.yes"), "distinctEnabled")
                                : withButton(new TextComponentTranslation("gtadditions.multiblock.universal.distinct.no"), "distinctDisabled"))))
                .addIsWorkingLine(this.workableHandler.isWorkingEnabled(), this.workableHandler.isActive(), this.workableHandler.getProgress(), this.workableHandler.getMaxProgress(), this.workableHandler.hasProblem())
                .addRecipeInputLine(this.workableHandler)
                .addRecipeOutputLine(this.workableHandler);
    }

    @Override
    protected void handleDisplayClick(String componentData, Widget.ClickData clickData) {
        this.workableHandler.setDistinct(!componentData.equals("distinctEnabled"));
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("~XXX~", "XXXXX", "~XXX~", "~~C~~", "~~~~~")
                .aisle("XXXXX", "X###X", "X###X", "~CCC~", "~~C~~")
                .aisle("XXXXX", "X###X", "X###X", "CC#CC", "~CPC~")
                .aisle("XXXXX", "X###X", "X###X", "~CCC~", "~~C~~")
                .aisle("~XXX~", "XXSXX", "~XXX~", "~~C~~", "~~~~~")
                .where('S', this.selfPredicate())
                .where('C', statePredicate(this.getCasingState()))
                .where('X', statePredicate(this.getCasingState()).or(abilityPartPredicate(ALLOWED_ABILITIES)))
                .where('P', LargeSimpleRecipeMapMultiblockController.pistonPredicate())
                .where('#', isAirPredicate())
                .where('~', tile -> true)
                .build();
    }

    private IBlockState getCasingState() {
        return GAMetaBlocks.METAL_CASING_2.getState(MetalCasing2.CasingType.IRON);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        TJTextures.TJ_MULTIBLOCK_WORKABLE_OVERLAY.render(renderState, translation, pipeline, this.getFrontFacing(), this.workableHandler.isActive(), this.workableHandler.hasProblem(), this.workableHandler.isWorkingEnabled());
        TJTextures.NAME_TAG.renderSided(EnumFacingHelper.getLeftFacingFrom(this.getFrontFacing()), renderState, translation, pipeline);
        TJTextures.NAME_TAG.renderSided(EnumFacingHelper.getRightFacingFrom(this.getFrontFacing()), renderState, translation, pipeline);
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        int tier = context.getOrDefault("Piston", PistonCasing.CasingType.PISTON_LV).getTier();
        if (tier >= GAValues.MAX) {
            this.maxVoltage = this.inputEnergyContainer.getInputVoltage();
            this.maxVoltage += this.maxVoltage / Integer.MAX_VALUE;
            tier = TJUtility.getTierByVoltage(this.maxVoltage);
        } else this.maxVoltage = 8L << tier * 2;
        this.parallel = TJConfig.largeNamingMachine.stack * tier;
        this.workableHandler.initialize(this.getAbilities(IMPORT_ITEMS).size());
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.workableHandler.invalidate();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setString("tagName", this.name);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.name = data.getString("tagName");
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return ClientHandler.IRON_CASING;
    }

    @Override
    public void setWorkingEnabled(boolean isActivationAllowed) {
        this.workableHandler.setWorkingEnabled(isActivationAllowed);
    }

    @Override
    public boolean isWorkingEnabled() {
        return this.workableHandler.isWorkingEnabled();
    }

    @Override
    public long getMaxVoltage() {
        return this.maxVoltage;
    }

    @Override
    public int getParallel() {
        return this.parallel;
    }

    private void setName(String name) {
        this.name = name;
        this.markDirty();
    }

    @Override
    public String getName() {
        return this.name;
    }
}
