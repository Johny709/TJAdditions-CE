package tj.machines.multi.electric;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregicadditions.capabilities.GregicAdditionsCapabilities;
import gregicadditions.client.ClientHandler;
import gregicadditions.item.CellCasing;
import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.metal.MetalCasing1;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.*;
import gregtech.api.metatileentity.MTETrait;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.BlockWorldState;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.api.multiblock.PatternMatchContext;
import gregtech.api.render.ICubeRenderer;
import gregtech.api.render.Textures;
import gregtech.common.covers.CoverPump;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.ArrayUtils;
import tj.TJValues;
import tj.builder.WidgetTabBuilder;
import tj.capability.impl.handler.IBatteryHandler;
import tj.capability.impl.workable.BasicEnergyHandler;
import tj.builder.multicontrollers.ExtendableMultiblockController;
import tj.builder.multicontrollers.UIDisplayBuilder;
import tj.capability.IEnderNotifiable;
import tj.capability.IProgressBar;
import tj.capability.ProgressBar;
import tj.capability.AbstractWorkableHandler;
import tj.gui.widgets.AdvancedDisplayWidget;
import tj.gui.widgets.NewTextFieldWidget;
import tj.gui.widgets.TJAdvancedTextWidget;
import tj.gui.widgets.impl.ClickPopUpWidget;
import tj.gui.widgets.impl.ScrollableDisplayWidget;
import tj.gui.widgets.impl.TJToggleButtonWidget;
import tj.items.covers.EnderCoverProfile;
import tj.util.EnderWorldData;
import tj.util.predicates.QuadActionResultPredicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

import static gregtech.api.gui.GuiTextures.*;
import static gregtech.api.gui.GuiTextures.TOGGLE_BUTTON_BACK;
import static gregtech.api.gui.widgets.AdvancedTextWidget.withButton;
import static gregtech.api.gui.widgets.ProgressWidget.MoveType.VERTICAL;
import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;
import static net.minecraft.util.text.TextFormatting.GRAY;
import static net.minecraft.util.text.TextFormatting.YELLOW;
import static tj.gui.TJGuiTextures.*;
import static tj.gui.TJGuiTextures.LIST_OVERLAY;
import static tj.machines.multi.electric.MetaTileEntityLargeGreenhouse.glassPredicate;

public class MetaTileEntityEnderBatteryTower extends ExtendableMultiblockController implements IEnderNotifiable<BasicEnergyHandler>, IProgressBar, IBatteryHandler {

    private static final MultiblockAbility<?>[] ALLOWED_ABILITIES = {MultiblockAbility.INPUT_ENERGY, MultiblockAbility.OUTPUT_ENERGY, GregicAdditionsCapabilities.MAINTENANCE_HATCH};
    private final long maxTransferRate = Long.MAX_VALUE;

    private final EnderBatteryTowerWorkableHandler workableHandler = new EnderBatteryTowerWorkableHandler(this);

    private BasicEnergyHandler handler;
    private BasicEnergyHandler energyBuffer;
    private List<IEnergyContainer> inputEnergy;
    private List<IEnergyContainer> outputEnergy;
    protected String frequency;
    protected String channel;
    protected String displayName;
    protected UUID ownerId;
    private long transferRate;
    private CoverPump.PumpMode pumpMode = CoverPump.PumpMode.EXPORT;

    public MetaTileEntityEnderBatteryTower(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityEnderBatteryTower(this.metaTileEntityId);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(net.minecraft.client.resources.I18n.format("gtadditions.multiblock.battery_tower.tooltip.1"));
        tooltip.add(net.minecraft.client.resources.I18n.format("tj.multiblock.ender_battery_tower.description"));
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

    protected BasicEnergyHandler createHandler() {
        return new BasicEnergyHandler(this.inputEnergyContainer != null ? this.inputEnergyContainer.getEnergyCapacity() : 0);
    }

    protected Map<String, EnderCoverProfile<BasicEnergyHandler>> getPlayerMap() {
        return EnderWorldData.getINSTANCE().getEnergyContainerPlayerMap();
    }

    @Nonnull
    protected EnderCoverProfile<BasicEnergyHandler> getEnderProfile()  {
        return this.getPlayerMap().getOrDefault(this.frequency, this.getPlayerMap().get(null));
    }

    @Override
    protected int getExtended() {
        return 18;
    }

    @Override
    protected void mainDisplayTab(List<Widget> widgetGroup) {
        super.mainDisplayTab(widgetGroup);
        widgetGroup.add(new CycleButtonWidget(7, 114, 188, 18, CoverPump.PumpMode.class, () -> this.pumpMode, this::setPumpMode));
    }

    @Override
    protected void addTabs(WidgetTabBuilder tabBuilder, EntityPlayer player) {
        super.addTabs(tabBuilder, player);
        int[] searchResults = new int[3];
        int[][] patternFlags = new int[3][9];
        long[][] permissions = new long[1][7];
        String[] search = {"", "", ""};
        String[] playerName = {""};
        tabBuilder.addTab(this.getMetaFullName(), this.getStackForm(), tab -> {
            NewTextFieldWidget<?> textFieldWidgetRename = new NewTextFieldWidget<>(12, 20, 159, 13)
                    .setValidator(str -> Pattern.compile(".*").matcher(str).matches())
                    .setBackgroundText("machine.universal.toggle.rename.channel")
                    .setTooltipText("machine.universal.toggle.rename.channel")
                    .setTextResponder(this::editChannel)
                    .setMaxStringLength(256);
            NewTextFieldWidget<?> textFieldWidgetEntry = new NewTextFieldWidget<>(12, 20, 159, 13)
                    .setValidator(str -> Pattern.compile(".*").matcher(str).matches())
                    .setBackgroundText("machine.universal.toggle.add.channel")
                    .setTooltipText("machine.universal.toggle.add.channel")
                    .setTextId(player.getUniqueID().toString())
                    .setTextResponder(this::addChannel)
                    .setMaxStringLength(256);
            AdvancedDisplayWidget displayWidget = new AdvancedDisplayWidget(0, 2, this.addChannelDisplayText(searchResults, patternFlags, search), 0xFFFFFF);
            displayWidget.setMaxWidthLimit(1000);
            tab.add(new ClickPopUpWidget(0, -30, 0, 0)
                    .addPopup(widgetGroup -> {
                        widgetGroup.addWidget(new ImageWidget(35, 17, 130, 18, DISPLAY));
                        widgetGroup.addWidget(new ImageWidget(35, 40, 130, 18, DISPLAY));
                        widgetGroup.addWidget(new ImageWidget(30, this.getOffsetY(144), 139, 18, DISPLAY));
                        widgetGroup.addWidget(new ScrollableDisplayWidget(10, 60, 183, 97)
                                .addDisplayWidget(displayWidget)
                                .setScrollPanelWidth(3));
                        widgetGroup.addWidget(new NewTextFieldWidget<>(38, 45, 112, 13, false)
                                .setValidator(str -> Pattern.compile(".*").matcher(str).matches())
                                .setBackgroundText("machine.universal.toggle.current.channel")
                                .setTooltipText("machine.universal.toggle.current.channel")
                                .setTextId(player.getUniqueID().toString())
                                .setTextSupplier(() -> this.channel)
                                .setTextResponder(this::setChannel)
                                .setMaxStringLength(256)
                                .setUpdateOnTyping(true));
                        widgetGroup.addWidget(new NewTextFieldWidget<>(38, 22, 112, 13, false)
                                .setValidator(str -> Pattern.compile("\\*?[0-9_]*\\*?").matcher(str).matches())
                                .setBackgroundText("metaitem.ender_cover.transfer")
                                .setTooltipText("metaitem.ender_cover.transfer")
                                .setTooltipFormat(this::getTooltipFormat)
                                .setTextResponder(this::setTransferRate)
                                .setTextSupplier(this::getTransferRate)
                                .setUpdateOnTyping(true));
                        widgetGroup.addWidget(new NewTextFieldWidget<>(33, this.getOffsetY(149), 112, 13, false)
                                .setValidator(str -> Pattern.compile(".*").matcher(str).matches())
                                .setTextResponder((result, id) -> search[0] = result)
                                .setBackgroundText("machine.universal.search")
                                .setTextSupplier(() -> search[0])
                                .setMaxStringLength(256)
                                .setUpdateOnTyping(true));
                        widgetGroup.addWidget(new TJToggleButtonWidget(169, 17, 18, 18, TJValues::isFalse, this::onIncrement)
                                .setTooltipText("machine.universal.toggle.increment.disabled")
                                .setButtonId(player.getUniqueID().toString())
                                .setToggleTexture(TOGGLE_DISPLAY)
                                .useToggleTexture(true)
                                .setDisplayText("§e+"));
                        widgetGroup.addWidget(new TJToggleButtonWidget(12, 17, 18, 18, TJValues::isFalse, this::onDecrement)
                                .setTooltipText("machine.universal.toggle.decrement.disabled")
                                .setButtonId(player.getUniqueID().toString())
                                .setToggleTexture(TOGGLE_DISPLAY)
                                .useToggleTexture(true)
                                .setDisplayText("§e-"));
                        widgetGroup.addWidget(new TJToggleButtonWidget(7, this.getOffsetY(144), 18, 18)
                                .setTooltipText("machine.universal.toggle.clear")
                                .setButtonId(player.getUniqueID().toString())
                                .setBackgroundTextures(BUTTON_CLEAR_GRID)
                                .setToggleButtonResponder(this::onClear)
                                .setToggleTexture(TOGGLE_BUTTON_BACK)
                                .useToggleTexture(true));
                        this.addEnergyWidgets(widgetGroup::addWidget);
                        return true;
                    }).addPopup(130, 61, 60, 78, new TJToggleButtonWidget(175, this.getOffsetY(144), 18, 18) // search settings button
                            .setItemDisplay(new ItemStack(Item.getByNameOrId("enderio:item_material"), 1, 11))
                            .setTooltipText("machine.universal.search.settings")
                            .setToggleTexture(TOGGLE_BUTTON_BACK)
                            .useToggleTexture(true), widgetGroup -> this.addSearchTextWidgets(widgetGroup, patternFlags, 0))
                    .addClosingButton(new TJToggleButtonWidget(10, 35, 81, 18)
                            .setDisplayText("machine.universal.cancel")
                            .setToggleTexture(TOGGLE_BUTTON_BACK)
                            .setButtonSupplier(TJValues::isFalse)
                            .useToggleTexture(true))
                    .addClosingButton(new TJToggleButtonWidget(91, 35, 81, 18)
                            .setButtonResponderWithMouse(textFieldWidgetRename::triggerResponse)
                            .setDisplayText("machine.universal.ok")
                            .setToggleTexture(TOGGLE_BUTTON_BACK)
                            .setButtonSupplier(TJValues::isFalse)
                            .useToggleTexture(true))
                    .addPopup(0, 61, 182, 60, displayWidget, false, widgetGroup -> {
                        widgetGroup.addWidget(new ImageWidget(0, 0, 182, 60, BORDERED_BACKGROUND));
                        widgetGroup.addWidget(new ImageWidget(10, 15, 162, 18, DISPLAY));
                        widgetGroup.addWidget(new AdvancedTextWidget(45, 4, (textList) -> {
                            int index = textFieldWidgetRename.getTextId().lastIndexOf(":");
                            String entry = textFieldWidgetRename.getTextId().substring(0, index);
                            textList.add(new TextComponentTranslation("machine.universal.renaming", entry));
                        }, 0x404040));
                        widgetGroup.addWidget(textFieldWidgetRename);
                        return false;
                    }).addClosingButton(new TJToggleButtonWidget(3, 19, 176, 18)
                            .setDisplayText("machine.universal.ok")
                            .setToggleTexture(TOGGLE_BUTTON_BACK)
                            .useToggleTexture(true))
                    .addPopupCondition(this.handleDisplayClick(textFieldWidgetRename)).addFailPopup(0, 40, 182, 40, widgetGroup2 -> {
                        widgetGroup2.addWidget(new ImageWidget(0, 0, 182, 40, BORDERED_BACKGROUND));
                        widgetGroup2.addWidget(new AdvancedTextWidget(30, 4, textList -> textList.add(new TextComponentTranslation("metaitem.ender_cover.operation_false")), 0x404040));
                    }).addClosingButton(new TJToggleButtonWidget(10, 35, 81, 18)
                            .setDisplayText("machine.universal.cancel")
                            .setToggleTexture(TOGGLE_BUTTON_BACK)
                            .setButtonSupplier(TJValues::isFalse)
                            .useToggleTexture(true))
                    .addClosingButton(new TJToggleButtonWidget(91, 35, 81, 18)
                            .setButtonResponderWithMouse(textFieldWidgetEntry::triggerResponse)
                            .setDisplayText("machine.universal.ok")
                            .setToggleTexture(TOGGLE_BUTTON_BACK)
                            .setButtonSupplier(TJValues::isFalse)
                            .useToggleTexture(true))
                    .addPopup(0, 61, 182, 60, new TJToggleButtonWidget(169, 40, 18, 18)
                            .setTooltipText("machine.universal.toggle.add.channel")
                            .setButtonId("channel:" + player.getUniqueID())
                            .setToggleTexture(TOGGLE_DISPLAY)
                            .useToggleTexture(true)
                            .setDisplayText("§eO"), widgetGroup -> {
                        widgetGroup.addWidget(new ImageWidget(0, 0, 182, 60, BORDERED_BACKGROUND));
                        widgetGroup.addWidget(new ImageWidget(10, 15, 162, 18, DISPLAY));
                        widgetGroup.addWidget(new AdvancedTextWidget(55, 4, textList -> textList.add(new TextComponentTranslation("machine.universal.toggle.add.channel")), 0x404040));
                        widgetGroup.addWidget(textFieldWidgetEntry);
                        return false;
                    }).addClosingButton(new TJToggleButtonWidget(3, 19, 176, 18)
                            .setDisplayText("machine.universal.ok")
                            .setToggleTexture(TOGGLE_BUTTON_BACK)
                            .useToggleTexture(true))
                    .addPopupCondition(this::handleButtonClick).addFailPopup(0, 40, 182, 40, widgetGroup2 -> {
                        widgetGroup2.addWidget(new ImageWidget(0, 0, 182, 40, BORDERED_BACKGROUND));
                        widgetGroup2.addWidget(new AdvancedTextWidget(30, 4, textList -> textList.add(new TextComponentTranslation("metaitem.ender_cover.operation_false")), 0x404040));
                    }));
        }).addTab("tj.multiblock.tab.frequencies", new ItemStack(Item.getByNameOrId("appliedenergistics2:part"), 1, 76), tab -> {
            NewTextFieldWidget<?> textFieldWidgetRename = new NewTextFieldWidget<>(12, 20, 159, 13)
                    .setValidator(str -> Pattern.compile(".*").matcher(str).matches())
                    .setBackgroundText("machine.universal.toggle.rename.frequency")
                    .setTooltipText("machine.universal.toggle.rename.frequency")
                    .setTextResponder(this::renameFrequency)
                    .setMaxStringLength(256);
            NewTextFieldWidget<?> textFieldWidgetChannel = new NewTextFieldWidget<>(12, 20, 159, 13)
                    .setValidator(str -> Pattern.compile(".*").matcher(str).matches())
                    .setBackgroundText("machine.universal.toggle.add.frequency")
                    .setTooltipText("machine.universal.toggle.add.frequency")
                    .setTextId(player.getUniqueID().toString())
                    .setTextResponder(this::addFrequency)
                    .setMaxStringLength(256);
            AdvancedDisplayWidget displayWidget = new AdvancedDisplayWidget(0, 2, this.addFrequencyDisplayText(searchResults, patternFlags, search), 0xFFFFFF);
            displayWidget.setMaxWidthLimit(1000);
            tab.add(new ClickPopUpWidget(0, -30, 0, 0)
                    .addPopup(widgetGroup -> {
                        widgetGroup.addWidget(new ImageWidget(35, 17, 130, 18, DISPLAY));
                        widgetGroup.addWidget(new ImageWidget(30, (this.getOffsetY(144)), 139, 18, DISPLAY));
                        widgetGroup.addWidget(new ScrollableDisplayWidget(10, 38, 183, 119)
                                .addDisplayWidget(displayWidget)
                                .setScrollPanelWidth(3));
                        widgetGroup.addWidget(new NewTextFieldWidget<>(32, 22, 136, 18)
                                .setValidator(str -> Pattern.compile(".*").matcher(str).matches())
                                .setBackgroundText("machine.universal.toggle.current.frequency")
                                .setTooltipText("machine.universal.toggle.current.frequency")
                                .setTextId(player.getUniqueID().toString())
                                .setTextSupplier(() -> this.frequency)
                                .setTextResponder(this::setFrequency)
                                .setMaxStringLength(256)
                                .setUpdateOnTyping(true));
                        widgetGroup.addWidget(new TJToggleButtonWidget(7, 15, 18, 18)
                                .setButtonSupplier(this::isPublic)
                                .setButtonId(player.getUniqueID().toString())
                                .setToggleButtonResponder(this::setPublic)
                                .setToggleTexture(UNLOCK_LOCK)
                                .useToggleTexture(true));
                        widgetGroup.addWidget(new NewTextFieldWidget<>(33, this.getOffsetY(149), 112, 13, false)
                                .setValidator(str -> Pattern.compile(".*").matcher(str).matches())
                                .setTextResponder((result, id) -> search[1] = result)
                                .setBackgroundText("machine.universal.search")
                                .setTextSupplier(() -> search[1])
                                .setMaxStringLength(256)
                                .setUpdateOnTyping(true));
                        return true;
                    }).addClosingButton(new TJToggleButtonWidget(10, 35, 81, 18)
                            .setDisplayText("machine.universal.cancel")
                            .setToggleTexture(TOGGLE_BUTTON_BACK)
                            .setButtonSupplier(TJValues::isFalse)
                            .useToggleTexture(true))
                    .addClosingButton(new TJToggleButtonWidget(91, 35, 81, 18)
                            .setButtonResponderWithMouse(textFieldWidgetRename::triggerResponse)
                            .setDisplayText("machine.universal.ok")
                            .setToggleTexture(TOGGLE_BUTTON_BACK)
                            .setButtonSupplier(TJValues::isFalse)
                            .useToggleTexture(true))
                    .addPopup(0, 61, 182, 60, displayWidget, false, widgetGroup -> {
                        widgetGroup.addWidget(new ImageWidget(0, 0, 182, 60, BORDERED_BACKGROUND));
                        widgetGroup.addWidget(new ImageWidget(10, 17, 162, 18, DISPLAY));
                        widgetGroup.addWidget(new AdvancedTextWidget(45, 4, (textList) -> {
                            int index = textFieldWidgetRename.getTextId().lastIndexOf(":");
                            String entry = textFieldWidgetRename.getTextId().substring(0, index);
                            textList.add(new TextComponentTranslation("machine.universal.renaming", entry));
                        }, 0x404040));
                        widgetGroup.addWidget(textFieldWidgetRename);
                        return false;
                    }).addClosingButton(new TJToggleButtonWidget(3, 19, 176, 18)
                            .setDisplayText("machine.universal.ok")
                            .setToggleTexture(TOGGLE_BUTTON_BACK)
                            .useToggleTexture(true))
                    .addPopupCondition(this.handleDisplayClick(textFieldWidgetRename)).addFailPopup(0, 40, 182, 40, widgetGroup2 -> {
                        widgetGroup2.addWidget(new ImageWidget(0, 0, 182, 40, BORDERED_BACKGROUND));
                        widgetGroup2.addWidget(new AdvancedTextWidget(30, 4, textList -> textList.add(new TextComponentTranslation("metaitem.ender_cover.operation_false")), 0x404040));
                    }).addClosingButton(new TJToggleButtonWidget(10, 35, 81, 18)
                            .setDisplayText("machine.universal.cancel")
                            .setToggleTexture(TOGGLE_BUTTON_BACK)
                            .setButtonSupplier(TJValues::isFalse)
                            .useToggleTexture(true))
                    .addClosingButton(new TJToggleButtonWidget(91, 35, 81, 18)
                            .setButtonResponderWithMouse(textFieldWidgetChannel::triggerResponse)
                            .setDisplayText("machine.universal.ok")
                            .setToggleTexture(TOGGLE_BUTTON_BACK)
                            .setButtonSupplier(TJValues::isFalse)
                            .useToggleTexture(true))
                    .addPopup(0, 61, 182, 60, new TJToggleButtonWidget(169, 17, 18, 18)
                            .setTooltipText("machine.universal.toggle.add.frequency")
                            .setToggleTexture(TOGGLE_DISPLAY)
                            .useToggleTexture(true)
                            .setDisplayText("§eO"), widgetGroup -> {
                        widgetGroup.addWidget(new ImageWidget(0, 0, 182, 60, BORDERED_BACKGROUND));
                        widgetGroup.addWidget(new ImageWidget(10, 15, 162, 18, DISPLAY));
                        widgetGroup.addWidget(new AdvancedTextWidget(55, 4, textList -> textList.add(new TextComponentTranslation("machine.universal.toggle.add.frequency")), 0x404040));
                        widgetGroup.addWidget(textFieldWidgetChannel);
                        return false;
                    }).addPopup(0, 38, 182, 130, new TJToggleButtonWidget(7, this.getOffsetY(144), 18, 18)
                            .setToggleTexture(TOGGLE_BUTTON_BACK)
                            .setBackgroundTextures(LIST_OVERLAY)
                            .useToggleTexture(true), widgetGroup -> {
                        widgetGroup.addWidget(new ClickPopUpWidget(0, 0, 0, 0)
                                .addPopup(widgetGroup1 -> {
                                    TJAdvancedTextWidget playerTextWidget = new TJAdvancedTextWidget(2, 3, this.addPlayerDisplayText(searchResults, patternFlags, search), 0xFFFFFF);
                                    widgetGroup1.addWidget(new ClickPopUpWidget(0, 0, 0, 0)
                                            .addPopup(widgetGroup2 -> {
                                                widgetGroup2.addWidget(new ImageWidget(0, 0, 182, 130, BORDERED_BACKGROUND));
                                                widgetGroup2.addWidget(new ImageWidget(3, 25, 176, 80, DISPLAY));
                                                widgetGroup2.addWidget(new ImageWidget(30, 106, 115, 18, DISPLAY));
                                                widgetGroup2.addWidget(new ScrollableDisplayWidget(3, 25, 185, 80)
                                                        .addTextWidget(playerTextWidget));
                                                widgetGroup2.addWidget(new AdvancedTextWidget(10, 4, textList -> textList.add(new TextComponentString(I18n.translateToLocalFormatted("metaitem.ender_cover.allowed_players", this.frequency))), 0x404040));
                                                widgetGroup2.addWidget(new NewTextFieldWidget<>(32, 110, 112, 13, false)
                                                        .setValidator(str -> Pattern.compile(".*").matcher(str).matches())
                                                        .setTextResponder((result, id) -> search[2] = result)
                                                        .setBackgroundText("machine.universal.search")
                                                        .setTextSupplier(() -> search[2])
                                                        .setMaxStringLength(256)
                                                        .setUpdateOnTyping(true));
                                                return true;
                                            }).addPopup(0, 0, 182, 100, playerTextWidget, false, widgetGroup2 -> {
                                                widgetGroup2.addWidget(new ImageWidget(0, 0, 182, 100, BORDERED_BACKGROUND));
                                                widgetGroup2.addWidget(new AdvancedTextWidget(10, 4, textList -> textList.add(new TextComponentString(I18n.translateToLocalFormatted("metaitem.ender_cover.edit_permission", playerName[0]))), 0x404040));
                                                widgetGroup2.addWidget(new TJToggleButtonWidget(3, 25, 88, 18)
                                                        .setToggleDisplayText("machine.universal.false", "machine.universal.true")
                                                        .setToggleButtonResponder((toggle, id) -> permissions[0][0] = toggle ? 1 : 0)
                                                        .setTooltipText("metaitem.ender_cover.permission.0")
                                                        .setButtonSupplier(() -> permissions[0][0] != 0)
                                                        .setToggleTexture(TOGGLE_BUTTON_BACK)
                                                        .useToggleTexture(true));
                                                widgetGroup2.addWidget(new TJToggleButtonWidget(91, 25, 88, 18)
                                                        .setToggleDisplayText("machine.universal.false", "machine.universal.true")
                                                        .setToggleButtonResponder((toggle, id) -> permissions[0][1] = toggle ? 1 : 0)
                                                        .setTooltipText("metaitem.ender_cover.permission.1")
                                                        .setButtonSupplier(() -> permissions[0][1] != 0)
                                                        .setToggleTexture(TOGGLE_BUTTON_BACK)
                                                        .useToggleTexture(true));
                                                widgetGroup2.addWidget(new TJToggleButtonWidget(3, 43, 88, 18)
                                                        .setToggleDisplayText("machine.universal.false", "machine.universal.true")
                                                        .setToggleButtonResponder((toggle, id) -> permissions[0][2] = toggle ? 1 : 0)
                                                        .setTooltipText("metaitem.ender_cover.permission.2")
                                                        .setButtonSupplier(() -> permissions[0][2] != 0)
                                                        .setToggleTexture(TOGGLE_BUTTON_BACK)
                                                        .useToggleTexture(true));
                                                widgetGroup2.addWidget(new TJToggleButtonWidget(91, 43, 88, 18)
                                                        .setToggleDisplayText("machine.universal.false", "machine.universal.true")
                                                        .setToggleButtonResponder((toggle, id) -> permissions[0][3] = toggle ? 1 : 0)
                                                        .setTooltipText("metaitem.ender_cover.permission.3")
                                                        .setButtonSupplier(() -> permissions[0][3] != 0)
                                                        .setToggleTexture(TOGGLE_BUTTON_BACK)
                                                        .useToggleTexture(true));
                                                widgetGroup2.addWidget(new TJToggleButtonWidget(3, 61, 88, 18)
                                                        .setToggleDisplayText("machine.universal.false", "machine.universal.true")
                                                        .setToggleButtonResponder((toggle, id) -> permissions[0][4] = toggle ? 1 : 0)
                                                        .setTooltipText("metaitem.ender_cover.permission.4")
                                                        .setButtonSupplier(() -> permissions[0][4] != 0)
                                                        .setToggleTexture(TOGGLE_BUTTON_BACK)
                                                        .useToggleTexture(true));
                                                widgetGroup2.addWidget(new TJToggleButtonWidget(91, 61, 88, 18).setToggleDisplayText("machine.universal.false", "machine.universal.true")
                                                        .setToggleButtonResponder((toggle, id) -> permissions[0][5] = toggle ? 1 : 0)
                                                        .setTooltipText("metaitem.ender_cover.permission.5")
                                                        .setButtonSupplier(() -> permissions[0][5] != 0)
                                                        .setToggleTexture(TOGGLE_BUTTON_BACK)
                                                        .useToggleTexture(true));
                                                widgetGroup2.addWidget(new ImageWidget(3, 79, 176, 18, DISPLAY));
                                                widgetGroup2.addWidget(new NewTextFieldWidget<>(5, 84, 174, 13)
                                                        .setValidator(str -> Pattern.compile("-*?[0-9_]*\\*?").matcher(str).matches())
                                                        .setTextResponder((text, id) -> permissions[0][6] = Long.parseLong(text))
                                                        .setTextSupplier(() -> String.valueOf(permissions[0][6]))
                                                        .setTooltipText("metaitem.ender_cover.permission.6")
                                                        .setUpdateOnTyping(true));
                                                return false;
                                            }).addClosingButton(new TJToggleButtonWidget(3, 19, 176, 18)
                                                    .setDisplayText("machine.universal.ok")
                                                    .setToggleTexture(TOGGLE_BUTTON_BACK)
                                                    .useToggleTexture(true))
                                            .addPopupCondition(this.handlePlayerDisplayClick(playerName, permissions)).addFailPopup(0, 40, 182, 40, widgetGroup2 -> {
                                                widgetGroup2.addWidget(new ImageWidget(0, 0, 182, 40, BORDERED_BACKGROUND));
                                                widgetGroup2.addWidget(new AdvancedTextWidget(30, 4, textList -> textList.add(new TextComponentTranslation("metaitem.ender_cover.operation_false")), 0x404040));
                                            }));
                                    return true;
                                }).addPopup(117, 25, 60, 78, new TJToggleButtonWidget(151, 106, 18, 18) // search settings button
                                        .setItemDisplay(new ItemStack(Item.getByNameOrId("enderio:item_material"), 1, 11))
                                        .setTooltipText("machine.universal.search.settings")
                                        .setToggleTexture(TOGGLE_BUTTON_BACK)
                                        .useToggleTexture(true), innerWidgetGroup -> this.addSearchTextWidgets(innerWidgetGroup, patternFlags, 2)));
                        return false;
                    }).addPopup(130, 61, 60, 78, new TJToggleButtonWidget(175, this.getOffsetY(144), 18, 18) // search settings button
                            .setItemDisplay(new ItemStack(Item.getByNameOrId("enderio:item_material"), 1, 11))
                            .setTooltipText("machine.universal.search.settings")
                            .setToggleTexture(TOGGLE_BUTTON_BACK)
                            .useToggleTexture(true), widgetGroup -> this.addSearchTextWidgets(widgetGroup, patternFlags, 1)));
        });
    }

    protected void addEnergyWidgets(Consumer<Widget> widget) {
        widget.accept(new ProgressWidget(this::getEnergyStored, 12, 40, 18, 18) {
            private long energyStored;
            private long energyCapacity;

            @Override
            public void drawInForeground(int mouseX, int mouseY) {
                if(isMouseOverElement(mouseX, mouseY)) {
                    List<String> hoverList = Collections.singletonList(net.minecraft.client.resources.I18n.format("machine.universal.energy.stored", this.energyStored, this.energyCapacity));
                    this.drawHoveringText(ItemStack.EMPTY, hoverList, 300, mouseX, mouseY);
                }
            }

            @Override
            public void detectAndSendChanges() {
                super.detectAndSendChanges();
                if (handler != null) {
                    long energyStored = handler.getEnergyStored();
                    long energyCapacity = handler.getEnergyCapacity();
                    this.writeUpdateInfo(1, buffer -> buffer.writeLong(energyStored));
                    this.writeUpdateInfo(2, buffer -> buffer.writeLong(energyCapacity));
                }
            }

            @Override
            public void readUpdateInfo(int id, PacketBuffer buffer) {
                super.readUpdateInfo(id, buffer);
                if (id == 1) {
                    this.energyStored = buffer.readLong();
                } else if (id == 2) {
                    this.energyCapacity = buffer.readLong();
                }
            }
        }.setProgressBar(BAR_STEEL, BAR_HEAT, VERTICAL));
    }

    private boolean addSearchTextWidgets(WidgetGroup widgetGroup, int[][] patternFlags, int i) {
        widgetGroup.addWidget(new ImageWidget(0, 0, 60, 78, BORDERED_BACKGROUND));
        widgetGroup.addWidget(new ImageWidget(3, 57, 54, 18, DISPLAY));
        widgetGroup.addWidget(new AdvancedTextWidget(5, 62, textList -> textList.add(new TextComponentTranslation("string.regex.flag", this.getFlags(patternFlags[i]))), 0x404040));
        widgetGroup.addWidget(new TJToggleButtonWidget(3, 3, 18, 18)
                .setToggleButtonResponder((pressed, s) -> patternFlags[i][0] = pressed ? Pattern.UNIX_LINES : 0)
                .setDisplayText("string.regex.pattern.unix_lines.flag")
                .setTooltipText("string.regex.pattern.unix_lines")
                .setButtonSupplier(() -> patternFlags[i][0] != 0)
                .setToggleTexture(TOGGLE_BUTTON_BACK)
                .useToggleTexture(true));
        widgetGroup.addWidget(new TJToggleButtonWidget(21, 3, 18, 18)
                .setToggleButtonResponder((pressed, s) -> patternFlags[i][1] = pressed ? Pattern.CASE_INSENSITIVE : 0)
                .setDisplayText("string.regex.pattern.case_insensitive.flag")
                .setTooltipText("string.regex.pattern.case_insensitive")
                .setButtonSupplier(() -> patternFlags[i][1] != 0)
                .setToggleTexture(TOGGLE_BUTTON_BACK)
                .useToggleTexture(true));
        widgetGroup.addWidget(new TJToggleButtonWidget(39, 3, 18, 18)
                .setToggleButtonResponder((pressed, s) -> patternFlags[i][2] = pressed ? Pattern.COMMENTS : 0)
                .setDisplayText("string.regex.pattern.comments.flag")
                .setButtonSupplier(() -> patternFlags[i][2] != 0)
                .setTooltipText("string.regex.pattern.comments")
                .setToggleTexture(TOGGLE_BUTTON_BACK)
                .useToggleTexture(true));
        widgetGroup.addWidget(new TJToggleButtonWidget(3, 21, 18, 18)
                .setToggleButtonResponder((pressed, s) -> patternFlags[i][3] = pressed ? Pattern.MULTILINE : 0)
                .setDisplayText("string.regex.pattern.multiline.flag")
                .setTooltipText("string.regex.pattern.multiline")
                .setButtonSupplier(() -> patternFlags[i][3] != 0)
                .setToggleTexture(TOGGLE_BUTTON_BACK)
                .useToggleTexture(true));
        widgetGroup.addWidget(new TJToggleButtonWidget(21, 21, 18, 18)
                .setToggleButtonResponder((pressed, s) -> patternFlags[i][4] = pressed ? Pattern.LITERAL : 0)
                .setDisplayText("string.regex.pattern.literal.flag")
                .setButtonSupplier(() -> patternFlags[i][4] != 0)
                .setTooltipText("string.regex.pattern.literal")
                .setToggleTexture(TOGGLE_BUTTON_BACK)
                .useToggleTexture(true));
        widgetGroup.addWidget(new TJToggleButtonWidget(39, 21, 18, 18)
                .setToggleButtonResponder((pressed, s) -> patternFlags[i][5] = pressed ? Pattern.DOTALL : 0)
                .setDisplayText("string.regex.pattern.dotall.flag")
                .setButtonSupplier(() -> patternFlags[i][5] != 0)
                .setTooltipText("string.regex.pattern.dotall")
                .setToggleTexture(TOGGLE_BUTTON_BACK)
                .useToggleTexture(true));
        widgetGroup.addWidget(new TJToggleButtonWidget(3, 39, 18, 18)
                .setToggleButtonResponder((pressed, s) -> patternFlags[i][6] = pressed ? Pattern.UNICODE_CASE : 0)
                .setDisplayText("string.regex.pattern.unicode_case.flag")
                .setTooltipText("string.regex.pattern.unicode_case")
                .setButtonSupplier(() -> patternFlags[i][6] != 0)
                .setToggleTexture(TOGGLE_BUTTON_BACK)
                .useToggleTexture(true));
        widgetGroup.addWidget(new TJToggleButtonWidget(21, 39, 18, 18)
                .setToggleButtonResponder((pressed, s) -> patternFlags[i][7] = pressed ? Pattern.CANON_EQ : 0)
                .setDisplayText("string.regex.pattern.canon_eq.flag")
                .setButtonSupplier(() -> patternFlags[i][7] != 0)
                .setTooltipText("string.regex.pattern.canon_eq")
                .setToggleTexture(TOGGLE_BUTTON_BACK)
                .useToggleTexture(true));
        widgetGroup.addWidget(new TJToggleButtonWidget(39, 39, 18, 18)
                .setToggleButtonResponder((pressed, s) -> patternFlags[i][8] = pressed ? Pattern.UNICODE_CHARACTER_CLASS : 0)
                .setDisplayText("string.regex.pattern.unicode_character_class.flag")
                .setTooltipText("string.regex.pattern.unicode_character_class")
                .setButtonSupplier(() -> patternFlags[i][8] != 0)
                .setToggleTexture(TOGGLE_BUTTON_BACK)
                .useToggleTexture(true));
        return false;
    }

    private EnumActionResult handleButtonClick(String buttonId) {
        String[] id = buttonId.split(":");
        if (id[0].equals("channel") && this.getEnderProfile().hasPermission(UUID.fromString(id[1]), 1))
            return EnumActionResult.SUCCESS;
        else return EnumActionResult.FAIL;
    }

    private QuadActionResultPredicate<String, String, Widget.ClickData, EntityPlayer> handleDisplayClick(NewTextFieldWidget<?> textFieldWidget) {
        return (componentData, textId, clickData, player) -> {
            String[] components = componentData.split(":");
            switch (components[0]) {
                case "select":
                    if (components[1].equals("channel"))
                        return this.setChannel(components[2], player.getUniqueID().toString()) ? EnumActionResult.PASS : EnumActionResult.FAIL;
                    else return this.setFrequency(components[2], player.getUniqueID().toString()) ? EnumActionResult.PASS : EnumActionResult.FAIL;
                case "remove":
                    if (components[1].equals("channel"))
                        return this.getEnderProfile().removeChannel(components[2], player.getUniqueID().toString()) ? EnumActionResult.PASS : EnumActionResult.FAIL;
                    else return this.removeFrequency(components[2], player.getUniqueID().toString()) ? EnumActionResult.PASS : EnumActionResult.FAIL;
                case "rename":
                    if (this.getEnderProfile().hasPermission(player.getUniqueID(), components[1].equals("channel") ? 1 : 4)) {
                        textFieldWidget.setTextId(components[2] + ":" + player.getUniqueID());
                        return EnumActionResult.SUCCESS;
                    } else return EnumActionResult.FAIL;
                default: return EnumActionResult.PASS;
            }
        };
    }

    private QuadActionResultPredicate<String, String, Widget.ClickData, EntityPlayer> handlePlayerDisplayClick(String[] playerName, long[][] permissions) {
        return (componentData, textId, clickData, player) -> {
            String[] component = componentData.split(":");
            UUID uuid = UUID.fromString(component[1]);
            if (this.getEnderProfile().getOwner() == null || uuid.equals(player.getUniqueID()))
                return EnumActionResult.FAIL;
            switch (component[0]) {
                case "add": return this.getEnderProfile().addUser(uuid, player.getUniqueID()) ? EnumActionResult.PASS : EnumActionResult.FAIL;
                case "remove": return this.getEnderProfile().removeUser(uuid, player.getUniqueID()) ? EnumActionResult.PASS : EnumActionResult.FAIL;
                case "edit":
                    if (this.getEnderProfile().getAllowedUsers().get(uuid) != null && this.getEnderProfile().getAllowedUsers().get(uuid)[4] == 1 & this.getEnderProfile().getAllowedUsers().containsKey(uuid)) {
                        playerName[0] = component[2];
                        permissions[0] = this.getEnderProfile().getAllowedUsers().get(uuid);
                        return EnumActionResult.SUCCESS;
                    } else return EnumActionResult.FAIL;
                default: return EnumActionResult.PASS;
            }
        };
    }

    private int getFlags(int[] flags) {
        int flag = 0;
        for (int i : flags) {
            flag |= i;
        }
        return flag;
    }

    private Consumer<UIDisplayBuilder> addChannelDisplayText(int[] searchResults, int[][] patternFlags, String[] search) {
        return (builder) -> {
            int results = 0;
            builder.addTextComponent(new TextComponentString("§l" + I18n.translateToLocal("tj.multiblock.tab.channels") + "§r(§e" + searchResults[0] + "§r/§e" + this.getEnderProfile().getChannels().size() + "§r)"));
            for (Map.Entry<String, BasicEnergyHandler> entry : this.getEnderProfile().getChannels().entrySet()) {
                String text = entry.getKey();
                if (!search[0].isEmpty() && !Pattern.compile(search[0], this.getFlags(patternFlags[0])).matcher(text).find())
                    continue;

                ITextComponent keyEntry = new TextComponentString(": [§a" + (++results) + "§r] " + text + (text.equals(this.channel) ? " §a<<<" : ""))
                        .appendText("\n")
                        .appendSibling(TJAdvancedTextWidget.withButton(new TextComponentTranslation("machine.universal.linked.select").setStyle(new Style().setColor(text.equals(this.channel) ? GRAY : YELLOW)), "select:channel:" + text))
                        .appendText(" ")
                        .appendSibling(withButton(new TextComponentTranslation("machine.universal.linked.remove"), "remove:channel:" + text))
                        .appendText(" ")
                        .appendSibling(withButton(new TextComponentTranslation("machine.universal.linked.rename"), "rename:channel:" + text));
                builder.addTextComponent(keyEntry);
                this.addChannelText(keyEntry, entry.getKey(), entry.getValue());
            }
            searchResults[0] = results;
        };
    }

    private Consumer<UIDisplayBuilder> addFrequencyDisplayText(int[] searchResults, int[][] patternFlags, String[] search) {
        return (builder) -> {
            int results = 0;
            builder.addTextComponent(new TextComponentString("§l" + I18n.translateToLocal("tj.multiblock.tab.frequencies") + "§r(§e" + searchResults[1] + "§r/§e" + this.getPlayerMap().size() + "§r)"));
            for (Map.Entry<String, EnderCoverProfile<BasicEnergyHandler>> entry : this.getPlayerMap().entrySet()) {
                String text =  entry.getKey() != null ? entry.getKey() : "PUBLIC";
                if (!search[1].isEmpty() && !Pattern.compile(search[1], this.getFlags(patternFlags[1])).matcher(text).find())
                    continue;

                builder.addTextComponent(new TextComponentString(": [§a" + (++results) + "§r] " + text + (text.equals(this.frequency) ? " §a<<<" : ""))
                        .appendText("\n")
                        .appendSibling(TJAdvancedTextWidget.withButton(new TextComponentTranslation("machine.universal.linked.select").setStyle(new Style().setColor(text.equals(this.frequency) ? GRAY : YELLOW)), "select:frequency:" + text))
                        .appendText(" ")
                        .appendSibling(withButton(new TextComponentTranslation("machine.universal.linked.remove"), "remove:frequency:" + text))
                        .appendText(" ")
                        .appendSibling(withButton(new TextComponentTranslation("machine.universal.linked.rename"), "rename:frequency:" + text))
                        .setStyle(new Style().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentTranslation("machine.universal.owner", entry.getValue().getOwner())))));
            }
            searchResults[1] = results;
        };
    }

    private Consumer<List<ITextComponent>> addPlayerDisplayText(int[] searchResults, int[][] patternFlags, String[] search) {
        return (textList) -> {
            int results = 0;
            List<EntityPlayerMP> playerList = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers();
            textList.add(new TextComponentString("§l" + I18n.translateToLocal("tj.multiblock.tab.players") + "§r(§e" + searchResults[2] + "§r/§e" + playerList.size() + "§r)"));
            for (EntityPlayer player : playerList) {
                String text = player.getDisplayNameString();
                if (!search[2].isEmpty() && !Pattern.compile(search[2], this.getFlags(patternFlags[2])).matcher(text).find())
                    continue;
                boolean contains = this.getEnderProfile().getAllowedUsers().containsKey(player.getUniqueID());
                textList.add(new TextComponentString(": [§a" + (++results) + "§r] " + text).setStyle(new Style().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString(text)))).appendText("\n")
                        .appendSibling(TJAdvancedTextWidget.withButton(new TextComponentTranslation("machine.universal.linked.add").setStyle(new Style().setColor(contains ? GRAY : YELLOW)), "add:" + player.getUniqueID()))
                        .appendText(" ")
                        .appendSibling(TJAdvancedTextWidget.withButton(new TextComponentTranslation("machine.universal.linked.remove").setStyle(new Style().setColor(contains ? YELLOW : GRAY)), "remove:" + player.getUniqueID()))
                        .appendText(" ")
                        .appendSibling(TJAdvancedTextWidget.withButton(new TextComponentTranslation("machine.universal.linked.edit").setStyle(new Style().setColor(contains ? GRAY : YELLOW)), "edit:" + player.getUniqueID() + ":" + text)));
            }
            searchResults[2] = results;
        };
    }

    protected void addChannelText(ITextComponent keyEntry, String key, BasicEnergyHandler value) {
        keyEntry.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new TextComponentString(net.minecraft.util.text.translation.I18n.translateToLocalFormatted("machine.universal.energy.stored", value.getEnergyStored(), value.getEnergyCapacity()))));
    }

    @Override
    protected void addDisplayText(UIDisplayBuilder builder) {
        super.addDisplayText(builder);
        if (!this.isStructureFormed()) return;
        builder.energyStoredLine(this.inputEnergyContainer.getEnergyStored(), this.inputEnergyContainer.getEnergyCapacity())
                .customLine(text -> {
                    text.addTextComponent(new TextComponentString(I18n.translateToLocalFormatted("tj.multiblock.ender_battery_tower.energy_inserted", this.workableHandler.getEnergyInserted()))
                            .setStyle(new Style().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentTranslation("tj.multiblock.ender_battery_tower.energy_inserted.tooltip")))));
                    text.addTextComponent(new TextComponentString(I18n.translateToLocalFormatted("tj.multiblock.ender_battery_tower.energy_extracted", this.workableHandler.getEnergyExtracted()))
                            .setStyle(new Style().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentTranslation("tj.multiblock.ender_battery_tower.energy_extracted.tooltip")))));
                    text.addTextComponent(new TextComponentString(I18n.translateToLocalFormatted("tj.multiblock.ender_battery_tower.last_energy_inserted", this.workableHandler.getLastEnergyInserted()))
                            .setStyle(new Style().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentTranslation("tj.multiblock.ender_battery_tower.last_energy_inserted.tooltip")))));
                    text.addTextComponent(new TextComponentString(I18n.translateToLocalFormatted("tj.multiblock.ender_battery_tower.last_energy_extracted", this.workableHandler.getLastEnergyExtracted()))
                            .setStyle(new Style().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentTranslation("tj.multiblock.ender_battery_tower.last_energy_extracted.tooltip")))));
                }).isWorkingLine(this.workableHandler.isWorkingEnabled(), this.workableHandler.isActive(), this.workableHandler.getProgress(), this.workableHandler.getMaxProgress());
    }

    private String[] getTooltipFormat() {
        return ArrayUtils.toArray(getTransferRate());
    }

    private void setTransferRate(String amount, String id) {
        this.transferRate = Math.min(Integer.parseInt(amount), this.maxTransferRate);
        this.markDirty();
    }

    public String getTransferRate() {
        return String.valueOf(this.transferRate);
    }

    private void onIncrement(String id) {
        this.transferRate = (int) MathHelper.clamp(this.transferRate * 2, 1, Math.min(this.maxTransferRate, this.getEnderProfile().maxThroughPut(UUID.fromString(id))));
        this.markDirty();
    }

    private void onDecrement(String id) {
        this.transferRate = (int) MathHelper.clamp(this.transferRate / 2, 1, Math.min(this.maxTransferRate, this.getEnderProfile().maxThroughPut(UUID.fromString(id))));
        this.markDirty();
    }

    private void setPumpMode(CoverPump.PumpMode pumpMode) {
        this.pumpMode = pumpMode;
        this.markDirty();
    }

    private boolean isPublic() {
        return this.getEnderProfile().isPublic();
    }

    private void setPublic(boolean isPublic, String uuid) {
        if (this.getEnderProfile().getOwner() != null && this.getEnderProfile().getOwner().equals(UUID.fromString(uuid))) {
            this.getEnderProfile().setPublic(isPublic);
            this.markDirty();
        }
    }

    private void addFrequency(String key, String uuid) {
        if (this.getEnderProfile().getOwner() == null || this.getEnderProfile().getAllowedUsers().containsKey(UUID.fromString(uuid))) {
            this.getPlayerMap().putIfAbsent(key, new EnderCoverProfile<>(this.ownerId, new Object2ObjectOpenHashMap<>()));
            this.markDirty();
        }
    }

    private void renameFrequency(String key, String id) {
        int index = id.lastIndexOf(":");
        String uuid = id.substring(index + 1);
        String oldKey = id.substring(0, index);
        EnderCoverProfile<BasicEnergyHandler> profile = this.getPlayerMap().get(oldKey);
        if (profile != null && this.getEnderProfile().editFrequency(key, UUID.fromString(uuid))) {
            this.getPlayerMap().put(key, this.getPlayerMap().remove(oldKey));
            this.markDirty();
        }
    }

    private boolean removeFrequency(String key, String uuid) {
        if (this.getPlayerMap().get(key).removeFrequency(uuid)) {
            this.getPlayerMap().remove(key);
            this.markDirty();
            return true;
        } else return false;
    }

    private boolean setFrequency(String key, String id) {
        EnderCoverProfile<?> profile = this.getPlayerMap().getOrDefault(key, this.getPlayerMap().get(null));
        UUID uuid = UUID.fromString(id);
        if (!key.equals(this.frequency) && (profile.isPublic() || profile.getAllowedUsers().get(uuid) != null && profile.getAllowedUsers().get(uuid)[3] == 1)) {
            this.getEnderProfile().removeFromNotifiable(this.channel, this);
            this.setFrequency(key);
            this.getEnderProfile().addToNotifiable(this.channel, this);
            return true;
        } else return false;
    }

    @Override
    public void setFrequency(String frequency) {
        this.frequency = frequency;
        this.markDirty();
    }

    private boolean setChannel(String key, String uuid) {
        if (this.getEnderProfile().setChannel(key, this.channel, uuid, this)) {
            this.handler = this.getEnderProfile().getChannels().get(key);
            this.setChannel(key);
            return true;
        } else return false;
    }

    private void editChannel(String newKey, String id) {
        this.getEnderProfile().editChannel(newKey, id);
    }

    private void addChannel(String key, String uuid) {
        this.getEnderProfile().addChannel(key, uuid, this.createHandler());
        this.markDirty();
    }

    private void onClear(boolean toggle, String uuid) {
        this.getEnderProfile().editChannel(this.channel, uuid, this.createHandler());
        this.markDirty();
    }

    @Override
    public void markToDirty() {
        this.markDirty();
    }

    @Override
    public void setHandler(BasicEnergyHandler handler) {
        this.handler = handler;
    }

    @Override
    public void setChannel(String lastEntry) {
        this.channel = lastEntry;
        this.markDirty();
    }

    @Override
    protected BlockPattern createStructurePattern() {
        FactoryBlockPattern factoryPattern = FactoryBlockPattern.start(RIGHT, FRONT, DOWN);
        factoryPattern.aisle("XXXXX", "XXXXX", "XXXXX", "XXXXX", "XXXXX");
        for (int layer = 0; layer < this.parallelLayer; layer++) {
            factoryPattern.aisle("GGGGG", "GCCCG", "GCCCG", "GCCCG", "GGGGG");
        }
        return factoryPattern.aisle("XXSXX", "XXXXX", "XXXXX", "XXXXX", "XXXXX")
                .setAmountAtLeast('L', 10)
                .where('S', this.selfPredicate())
                .where('L', statePredicate(this.getCasingState()))
                .where('X', statePredicate(this.getCasingState()).or(abilityPartPredicate(ALLOWED_ABILITIES)))
                .where('G', statePredicate(this.getCasingState()).or(glassPredicate()))
                .where('C', cellPredicate())
                .build();
    }

    private IBlockState getCasingState() {
        return GAMetaBlocks.METAL_CASING_1.getState(MetalCasing1.CasingType.HASTELLOY_X78);
    }

    public static Predicate<BlockWorldState> cellPredicate() {
        return blockWorldState -> {
            IBlockState blockState = blockWorldState.getBlockState();
            if (!(blockState.getBlock() instanceof CellCasing))
                return false;
            CellCasing glassCasing = (CellCasing) blockState.getBlock();
            CellCasing.CellType tieredCasingType = glassCasing.getState(blockState);
            List<CellCasing.CellType> cellTypes = blockWorldState.getMatchContext().getOrCreate("Cells", ArrayList::new);
            cellTypes.add(tieredCasingType);
            return cellTypes.get(0).getName().equals(tieredCasingType.getName());
        };
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        this.inputEnergy = this.getAbilities(MultiblockAbility.INPUT_ENERGY);
        this.outputEnergy = this.getAbilities(MultiblockAbility.OUTPUT_ENERGY);
        List<CellCasing.CellType> cellTypes = context.getOrCreate("Cells", ArrayList::new);
        long capacity = cellTypes.get(0).getStorage() * cellTypes.size();
        if (this.energyBuffer == null) {
            this.energyBuffer = new BasicEnergyHandler(capacity);
        } else {
            long stored = this.energyBuffer.getEnergyStored();
            this.energyBuffer = new BasicEnergyHandler(capacity);
            this.energyBuffer.addEnergy(Math.min(stored, capacity));
        }
        this.inputEnergyContainer = this.energyBuffer;
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.inputEnergy = Collections.emptyList();
        this.outputEnergy = Collections.emptyList();
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return ClientHandler.HASTELLOY_X78_CASING;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        Textures.MULTIBLOCK_WORKABLE_OVERLAY.render(renderState, translation, pipeline, this.getFrontFacing(), this.isActive());
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == 2) {
            this.ownerId = buf.readUniqueId();
        }
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(this.ownerId != null);
        if (this.ownerId != null)
            buf.writeUniqueId(this.ownerId);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        if (buf.readBoolean())
            this.ownerId = buf.readUniqueId();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger("mode", this.pumpMode.ordinal());
        if (this.frequency != null)
            data.setString("frequency", this.frequency);
        if (this.ownerId != null)
            data.setUniqueId("ownerId", this.ownerId);
        if (this.channel != null)
            data.setString("channel", this.channel);
        if (this.energyBuffer != null) {
            data.setBoolean("buffer", true);
            this.energyBuffer.writeToNBT(data);
        }
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.pumpMode = CoverPump.PumpMode.values()[data.getInteger("mode")];
        if (data.hasKey("frequency"))
            this.frequency = data.getString("frequency");
        if (data.hasKey("ownerId"))
            this.ownerId = data.getUniqueId("ownerId");
        if (data.hasKey("channel")) {
            this.channel = data.getString("channel");
            this.handler = this.getEnderProfile().getChannels().get(this.channel);
            this.getEnderProfile().addToNotifiable(this.channel, this);
        }
        if (data.hasKey("buffer")) {
            this.energyBuffer = new BasicEnergyHandler(0);
            this.energyBuffer.readFromNBT(data);
        }
    }

    @Override
    public int[][] getBarMatrix() {
        return new int[1][1];
    }

    @Override
    public void getProgressBars(Queue<UnaryOperator<ProgressBar.ProgressBarBuilder>> bars) {
        bars.add(bar -> bar.setProgress(this.workableHandler::getEnergyStored).setMaxProgress(this.workableHandler::getEnergyCapacity)
                .setLocale("tj.multiblock.bars.energy")
                .setColor(0xFFF6FF00));
    }

    @Override
    public int getMaxParallel() {
        return 256;
    }

    @Override
    public boolean isWorkingEnabled() {
        return this.workableHandler.isWorkingEnabled();
    }

    @Override
    public void setWorkingEnabled(boolean isActivationAllowed) {
        this.workableHandler.setWorkingEnabled(isActivationAllowed);
    }

    private long getEnergyStored() {
        return this.handler != null ? this.handler.getEnergyStored() : 0;
    }

    @Override
    public CoverPump.PumpMode getPumpMode() {
        return this.pumpMode;
    }

    @Override
    public BasicEnergyHandler getEnergyHandler() {
        return this.handler != null ? this.handler : TJValues.DUMMY_ENERGY;
    }

    @Override
    public List<IEnergyContainer> getInputEnergy() {
        return this.inputEnergy;
    }

    @Override
    public List<IEnergyContainer> getOutputEnergy() {
        return this.outputEnergy;
    }

    private static class EnderBatteryTowerWorkableHandler extends AbstractWorkableHandler<IBatteryHandler> implements IEnergyContainer {

        private long lastEnergyExtracted;
        private long energyExtracted;
        private long lastEnergyInserted;
        private long energyInserted;

        public EnderBatteryTowerWorkableHandler(MetaTileEntity metaTileEntity) {
            super(metaTileEntity);
            this.maxProgress = 1200;
        }

        @Override
        protected boolean startRecipe() {
            return true;
        }

        @Override
        protected void progressRecipe(int progress) {
            this.energyExtracted = 0;
            this.energyInserted = 0;
            if (!this.isWorking) return;
            for (int i = 0; i < this.handler.getInputEnergy().size(); i++)
                this.energyInserted += this.importEnergy(this.handler.getInputEnergy().get(i));
            for (int i = 0; i < this.handler.getOutputEnergy().size(); i++)
                this.energyExtracted += this.exportEnergy(this.handler.getOutputEnergy().get(i));
            this.progress++;
        }

        @Override
        protected boolean completeRecipe() {
            this.lastEnergyExtracted = 0;
            this.lastEnergyInserted = 0;
            if (this.handler.getPumpMode() == CoverPump.PumpMode.IMPORT)
                this.lastEnergyInserted = this.exportEnergy(this.handler.getEnergyHandler());
            else this.lastEnergyExtracted = this.importEnergy(this.handler.getEnergyHandler());
            return true;
        }

        private long exportEnergy(IEnergyContainer enderEnergyContainer) {
            long energyRemainingToFill = enderEnergyContainer.getEnergyCapacity() - enderEnergyContainer.getEnergyStored();
            if (enderEnergyContainer.getEnergyStored() < 1 || energyRemainingToFill != 0) {
                long energyExtracted = this.handler.getInputEnergyContainer().removeEnergy(energyRemainingToFill);
                return enderEnergyContainer.addEnergy(Math.abs(energyExtracted));
            }
            return 0;
        }

        private long importEnergy(IEnergyContainer enderEnergyContainer) {
            long energyRemainingToFill = this.handler.getInputEnergyContainer().getEnergyCapacity() - this.handler.getInputEnergyContainer().getEnergyStored();
            if (this.handler.getInputEnergyContainer().getEnergyStored() < 1 || energyRemainingToFill != 0) {
                long energyExtracted = Math.abs(enderEnergyContainer.removeEnergy(energyRemainingToFill));
                return this.handler.getInputEnergyContainer().addEnergy(energyExtracted);
            }
            return 0;
        }

        @Override
        public <T> T getCapability(Capability<T> capability) {
            if (capability == GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER)
                return GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER.cast(this);
            return super.getCapability(capability);
        }

        public long getLastEnergyExtracted() {
            return this.lastEnergyExtracted;
        }

        public long getEnergyExtracted() {
            return this.energyExtracted;
        }

        public long getLastEnergyInserted() {
            return this.lastEnergyInserted;
        }

        public long getEnergyInserted() {
            return this.energyInserted;
        }

        @Override
        public long acceptEnergyFromNetwork(EnumFacing enumFacing, long l, long l1) {
            return 0;
        }

        @Override
        public boolean inputsEnergy(EnumFacing enumFacing) {
            return false;
        }

        @Override
        public long changeEnergy(long l) {
            return 0;
        }

        @Override
        public long getEnergyStored() {
            return this.handler.getInputEnergyContainer().getEnergyStored();
        }

        @Override
        public long getEnergyCapacity() {
            return this.handler.getInputEnergyContainer().getEnergyCapacity();
        }

        @Override
        public long getInputAmperage() {
            return 0;
        }

        @Override
        public long getInputVoltage() {
            return 0;
        }
    }
}
