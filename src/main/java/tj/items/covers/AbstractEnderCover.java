package tj.items.covers;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.IControllable;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.CoverWithUI;
import gregtech.api.cover.ICoverable;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.*;
import gregtech.api.gui.widgets.tab.HorizontalTabListRenderer;
import gregtech.common.covers.CoverPump;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.*;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.apache.commons.lang3.ArrayUtils;
import tj.TJValues;
import tj.builder.WidgetTabBuilder;
import tj.capability.IEnderNotifiable;
import tj.gui.widgets.*;
import tj.gui.widgets.impl.ClickPopUpWidget;
import tj.gui.widgets.impl.ScrollableTextWidget;
import tj.gui.widgets.impl.TJToggleButtonWidget;
import tj.textures.TJSimpleOverlayRenderer;
import tj.textures.TJTextures;
import tj.util.predicates.QuadActionResultPredicate;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import static gregtech.api.gui.GuiTextures.*;
import static gregtech.api.gui.widgets.AdvancedTextWidget.withButton;
import static gregtech.api.gui.widgets.tab.HorizontalTabListRenderer.HorizontalStartCorner.LEFT;
import static gregtech.api.gui.widgets.tab.HorizontalTabListRenderer.VerticalLocation.TOP;
import static net.minecraft.util.text.TextFormatting.GRAY;
import static net.minecraft.util.text.TextFormatting.YELLOW;
import static tj.gui.TJGuiTextures.*;

public abstract class AbstractEnderCover<V> extends CoverBehavior implements CoverWithUI, ITickable, IControllable, IEnderNotifiable<V> {

    protected String frequency;
    protected String channel;
    protected String displayName;
    protected UUID ownerId;
    protected boolean isWorkingEnabled;
    protected CoverPump.PumpMode pumpMode = CoverPump.PumpMode.IMPORT;
    protected int maxTransferRate;
    protected int transferRate;
    protected V handler;

    public AbstractEnderCover(ICoverable coverHolder, EnumFacing attachedSide) {
        super(coverHolder, attachedSide);
    }

    @Override
    public void renderCover(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, Cuboid6 cuboid6, BlockRenderLayer blockRenderLayer) {
        int oldBaseColor = renderState.baseColour;
        int oldAlphaOverride = renderState.alphaOverride;

        renderState.baseColour = getPortalColor() << 8;
        renderState.alphaOverride = 0xFF;
        this.getOverlay().renderSided(attachedSide, renderState, translation, pipeline);

        renderState.baseColour = TJValues.VC[getTier()] << 8;
        TJTextures.INSIDE_OVERLAY_BASE.renderSided(attachedSide, renderState, translation, pipeline);

        renderState.baseColour = oldBaseColor;
        renderState.alphaOverride = oldAlphaOverride;
        TJTextures.OUTSIDE_OVERLAY_BASE.renderSided(attachedSide, renderState, translation, pipeline);
    }

    @Override
    public EnumActionResult onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, CuboidRayTraceResult hitResult) {
        if (this.ownerId == null) {
            this.ownerId = playerIn.getUniqueID();
            this.displayName = playerIn.getDisplayNameString();
        }
        if (!playerIn.world.isRemote) {
            this.openUI((EntityPlayerMP) playerIn);
        }
        return EnumActionResult.SUCCESS;
    }

    protected int getPortalColor() {
        return 0xffffff;
    }

    protected int getTier() {
        return 0;
    }

    protected String getName() {
        return "Null";
    }

    @Nonnull
    protected EnderCoverProfile<V> getEnderProfile()  {
        return this.getPlayerMap().getOrDefault(this.frequency, this.getPlayerMap().get(null));
    }

    protected abstract TJSimpleOverlayRenderer getOverlay();

    protected abstract Map<String, EnderCoverProfile<V>> getPlayerMap();

    protected abstract void addWidgets(Consumer<Widget> widget);

    protected void addToPopUpWidget(PopUpWidget<?> buttonPopUpWidget) {}

    protected abstract V createHandler();

    @Override
    public ModularUI createUI(EntityPlayer player) {
        int[] searchResults = new int[3];
        int[][] patternFlags = new int[3][9];
        long[][] permissions = new long[1][7];
        String[] search = {"", "", ""};
        String[] playerName = {""};
        WidgetTabBuilder tabBuilder = new WidgetTabBuilder()
                .setTabListRenderer(() -> new HorizontalTabListRenderer(LEFT, TOP))
                .addWidget(new LabelWidget(30, 4, this.getName()))
                .addTab(this.getName(), this.getPickItem(), tab -> {
                    NewTextFieldWidget<?> textFieldWidgetRename = new NewTextFieldWidget<>(12, 20, 159, 13)
                            .setValidator(str -> Pattern.compile(".*").matcher(str).matches())
                            .setBackgroundText("machine.universal.toggle.rename.channel")
                            .setTooltipText("machine.universal.toggle.rename.channel")
                            .setTextResponder(this.getEnderProfile()::editChannel)
                            .setMaxStringLength(256);
                    NewTextFieldWidget<?> textFieldWidgetEntry = new NewTextFieldWidget<>(12, 20, 159, 13)
                            .setValidator(str -> Pattern.compile(".*").matcher(str).matches())
                            .setBackgroundText("machine.universal.toggle.add.channel")
                            .setTooltipText("machine.universal.toggle.add.channel")
                            .setTextId(player.getUniqueID().toString())
                            .setTextResponder(this::addChannel)
                            .setMaxStringLength(256);
                    TJAdvancedTextWidget textWidget = new TJAdvancedTextWidget(2, 3, this.addChannelDisplayText(searchResults, patternFlags, search), 0xFFFFFF);
                    textWidget.setMaxWidthLimit(1000);
                    tab.addWidget(new ClickPopUpWidget(0, 0, 0, 0)
                            .addPopup(widgetGroup -> {
                                widgetGroup.addWidget(new ImageWidget(30, 15, 115, 18, DISPLAY));
                                widgetGroup.addWidget(new ImageWidget(30, 38, 115, 18, DISPLAY));
                                widgetGroup.addWidget(new ImageWidget(3, 61, 170, 80, DISPLAY));
                                widgetGroup.addWidget(new ImageWidget(30, 142, 115, 18, DISPLAY));
                                widgetGroup.addWidget(new ImageWidget(-25, 33, 28, 28, BORDERED_BACKGROUND_RIGHT));
                                widgetGroup.addWidget(new ScrollableTextWidget(3, 61, 182, 80)
                                        .addTextWidget(textWidget));
                                widgetGroup.addWidget(new NewTextFieldWidget<>(32, 43, 112, 13, false)
                                        .setValidator(str -> Pattern.compile(".*").matcher(str).matches())
                                        .setBackgroundText("machine.universal.toggle.current.channel")
                                        .setTooltipText("machine.universal.toggle.current.channel")
                                        .setTextId(player.getUniqueID().toString())
                                        .setTextSupplier(() -> this.channel)
                                        .setTextResponder(this::setChannel)
                                        .setMaxStringLength(256)
                                        .setUpdateOnTyping(true));
                                widgetGroup.addWidget(new NewTextFieldWidget<>(32, 20, 112, 13, false)
                                        .setValidator(str -> Pattern.compile("\\*?[0-9_]*\\*?").matcher(str).matches())
                                        .setBackgroundText("metaitem.ender_cover.transfer")
                                        .setTooltipText("metaitem.ender_cover.transfer")
                                        .setTooltipFormat(this::getTooltipFormat)
                                        .setTextResponder(this::setTransferRate)
                                        .setTextSupplier(this::getTransferRate)
                                        .setUpdateOnTyping(true));
                                widgetGroup.addWidget(new NewTextFieldWidget<>(32, 147, 112, 13, false)
                                        .setValidator(str -> Pattern.compile(".*").matcher(str).matches())
                                        .setTextResponder((result, id) -> search[0] = result)
                                        .setBackgroundText("machine.universal.search")
                                        .setTextSupplier(() -> search[0])
                                        .setMaxStringLength(256)
                                        .setUpdateOnTyping(true));
                                widgetGroup.addWidget(new TJToggleButtonWidget(151, 15, 18, 18)
                                        .setTooltipText("machine.universal.toggle.increment.disabled")
                                        .setButtonId(player.getUniqueID().toString())
                                        .setButtonResponder(this::onIncrement)
                                        .setToggleTexture(TOGGLE_BUTTON_BACK)
                                        .setButtonSupplier(() -> false)
                                        .useToggleTexture(true)
                                        .setDisplayText("+"));
                                widgetGroup.addWidget(new TJToggleButtonWidget(7, 15, 18, 18)
                                        .setTooltipText("machine.universal.toggle.decrement.disabled")
                                        .setButtonId(player.getUniqueID().toString())
                                        .setButtonResponder(this::onDecrement)
                                        .setToggleTexture(TOGGLE_BUTTON_BACK)
                                        .setButtonSupplier(() -> false)
                                        .useToggleTexture(true)
                                        .setDisplayText("-"));
                                widgetGroup.addWidget(new TJToggleButtonWidget(-20, 38, 18, 18)
                                        .setTooltipText("machine.universal.toggle.clear")
                                        .setButtonId(player.getUniqueID().toString())
                                        .setBackgroundTextures(BUTTON_CLEAR_GRID)
                                        .setToggleButtonResponder(this::onClear)
                                        .setToggleTexture(TOGGLE_BUTTON_BACK)
                                        .useToggleTexture(true));
                                widgetGroup.addWidget(new CycleButtonWidget(30, 161, 115, 18, CoverPump.PumpMode.class, () -> this.pumpMode, this::setPumpMode));
                                widgetGroup.addWidget(new ToggleButtonWidget(7, 161, 18, 18, POWER_BUTTON, this::isWorkingEnabled, this::setWorkingEnabled)
                                        .setTooltipText("machine.universal.toggle.run.mode"));
                                this.addWidgets(widgetGroup::addWidget);
                                return true;
                            }).addPopup(112, 61, 60, 78, new TJToggleButtonWidget(151, 142, 18, 18)
                                    .setItemDisplay(new ItemStack(Item.getByNameOrId("enderio:item_material"), 1, 11))
                                    .setTooltipText("machine.universal.search.settings")
                                    .setToggleTexture(TOGGLE_BUTTON_BACK)
                                    .useToggleTexture(true), widgetGroup -> this.addSearchTextWidgets(widgetGroup, patternFlags, 0))
                            .addClosingButton(new TJToggleButtonWidget(10, 35, 81, 18)
                                    .setDisplayText("machine.universal.cancel")
                                    .setToggleTexture(TOGGLE_BUTTON_BACK)
                                    .setButtonSupplier(() -> false)
                                    .useToggleTexture(true))
                            .addClosingButton(new TJToggleButtonWidget(91, 35, 81, 18)
                                    .setButtonResponderWithMouse(textFieldWidgetRename::triggerResponse)
                                    .setDisplayText("machine.universal.ok")
                                    .setToggleTexture(TOGGLE_BUTTON_BACK)
                                    .setButtonSupplier(() -> false)
                                    .useToggleTexture(true))
                            .addPopup(0, 61, 182, 60, textWidget, false, widgetGroup -> {
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
                                    .setButtonSupplier(() -> false)
                                    .useToggleTexture(true))
                            .addClosingButton(new TJToggleButtonWidget(91, 35, 81, 18)
                                    .setButtonResponderWithMouse(textFieldWidgetEntry::triggerResponse)
                                    .setDisplayText("machine.universal.ok")
                                    .setToggleTexture(TOGGLE_BUTTON_BACK)
                                    .setButtonSupplier(() -> false)
                                    .useToggleTexture(true))
                            .addPopup(0, 61, 182, 60, new TJToggleButtonWidget(151, 38, 18, 18)
                                    .setTooltipText("machine.universal.toggle.add.channel")
                                    .setButtonId("channel:" + player.getUniqueID())
                                    .setToggleTexture(TOGGLE_BUTTON_BACK)
                                    .useToggleTexture(true)
                                    .setDisplayText("O"), widgetGroup -> {
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
                            }).passPopup(this::addToPopUpWidget));
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
                    TJAdvancedTextWidget textWidget = new TJAdvancedTextWidget(2, 3, this.addFrequencyDisplayText(searchResults, patternFlags, search), 0xFFFFFF);
                    textWidget.setMaxWidthLimit(1000);
                    tab.addWidget(new ClickPopUpWidget(0, 0, 0, 0)
                            .addPopup(widgetGroup -> {
                                widgetGroup.addWidget(new ImageWidget(30, 15, 115, 18, DISPLAY));
                                widgetGroup.addWidget(new ImageWidget(3, 38, 170, 103, DISPLAY));
                                widgetGroup.addWidget(new ImageWidget(30, 142, 115, 18, DISPLAY));
                                widgetGroup.addWidget(new ScrollableTextWidget(3, 38, 182, 103)
                                        .addTextWidget(textWidget));
                                widgetGroup.addWidget(new NewTextFieldWidget<>(32, 20, 112, 18)
                                        .setValidator(str -> Pattern.compile(".*").matcher(str).matches())
                                        .setBackgroundText("machine.universal.toggle.current.frequency")
                                        .setTooltipText("machine.universal.toggle.current.frequency")
                                        .setTextId(player.getUniqueID().toString())
                                        .setTextSupplier(() -> this.frequency)
                                        .setTextResponder(this::setFrequency)
                                        .setMaxStringLength(256)
                                        .setUpdateOnTyping(true));
                                widgetGroup.addWidget(new TJToggleButtonWidget(7, 15, 18, 18)
                                        .setButtonSupplier(() -> this.getEnderProfile().isPublic())
                                        .setButtonId(player.getUniqueID().toString())
                                        .setToggleButtonResponder(this::setPublic)
                                        .setToggleTexture(UNLOCK_LOCK)
                                        .useToggleTexture(true));
                                widgetGroup.addWidget(new NewTextFieldWidget<>(32, 147, 112, 13, false)
                                        .setValidator(str -> Pattern.compile(".*").matcher(str).matches())
                                        .setTextResponder((result, id) -> search[1] = result)
                                        .setBackgroundText("machine.universal.search")
                                        .setTextSupplier(() -> search[1])
                                        .setMaxStringLength(256)
                                        .setUpdateOnTyping(true));
                                widgetGroup.addWidget(new LabelWidget(3, 170, "machine.universal.owner", this.displayName));
                                return true;
                            }).addClosingButton(new TJToggleButtonWidget(10, 35, 81, 18)
                                    .setDisplayText("machine.universal.cancel")
                                    .setToggleTexture(TOGGLE_BUTTON_BACK)
                                    .setButtonSupplier(() -> false)
                                    .useToggleTexture(true))
                            .addClosingButton(new TJToggleButtonWidget(91, 35, 81, 18)
                                    .setButtonResponderWithMouse(textFieldWidgetRename::triggerResponse)
                                    .setDisplayText("machine.universal.ok")
                                    .setToggleTexture(TOGGLE_BUTTON_BACK)
                                    .setButtonSupplier(() -> false)
                                    .useToggleTexture(true))
                            .addPopup(0, 61, 182, 60, textWidget, false, widgetGroup -> {
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
                                    .setButtonSupplier(() -> false)
                                    .useToggleTexture(true))
                            .addClosingButton(new TJToggleButtonWidget(91, 35, 81, 18)
                                    .setButtonResponderWithMouse(textFieldWidgetChannel::triggerResponse)
                                    .setDisplayText("machine.universal.ok")
                                    .setToggleTexture(TOGGLE_BUTTON_BACK)
                                    .setButtonSupplier(() -> false)
                                    .useToggleTexture(true))
                            .addPopup(0, 61, 182, 60, new TJToggleButtonWidget(151, 15, 18, 18)
                                    .setTooltipText("machine.universal.toggle.add.frequency")
                                    .setToggleTexture(TOGGLE_BUTTON_BACK)
                                    .useToggleTexture(true)
                                    .setDisplayText("O"), widgetGroup -> {
                                widgetGroup.addWidget(new ImageWidget(0, 0, 182, 60, BORDERED_BACKGROUND));
                                widgetGroup.addWidget(new ImageWidget(10, 15, 162, 18, DISPLAY));
                                widgetGroup.addWidget(new AdvancedTextWidget(55, 4, textList -> textList.add(new TextComponentTranslation("machine.universal.toggle.add.frequency")), 0x404040));
                                widgetGroup.addWidget(textFieldWidgetChannel);
                                return false;
                            }).addPopup(0, 38, 182, 130, new TJToggleButtonWidget(7, 142, 18, 18)
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
                                                        widgetGroup2.addWidget(new ScrollableTextWidget(3, 25, 185, 80)
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
                                        }).addPopup(117, 25, 60, 78, new TJToggleButtonWidget(151, 106, 18, 18)
                                                .setItemDisplay(new ItemStack(Item.getByNameOrId("enderio:item_material"), 1, 11))
                                                .setTooltipText("machine.universal.search.settings")
                                                .setToggleTexture(TOGGLE_BUTTON_BACK)
                                                .useToggleTexture(true), innerWidgetGroup -> this.addSearchTextWidgets(innerWidgetGroup, patternFlags, 2)));
                                return false;
                            }).addPopup(112, 61, 60, 78, new TJToggleButtonWidget(151, 142, 18, 18)
                                    .setItemDisplay(new ItemStack(Item.getByNameOrId("enderio:item_material"), 1, 11))
                                    .setTooltipText("machine.universal.search.settings")
                                    .setToggleTexture(TOGGLE_BUTTON_BACK)
                                    .useToggleTexture(true), widgetGroup -> this.addSearchTextWidgets(widgetGroup, patternFlags, 1)));
                });
        return ModularUI.builder(BORDERED_BACKGROUND, 176, 262)
                .bindPlayerInventory(player.inventory, 181)
                .widget(tabBuilder.build())
                .widget(tabBuilder.buildWidgetGroup())
                .build(this, player);
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

    private Consumer<List<ITextComponent>> addChannelDisplayText(int[] searchResults, int[][] patternFlags, String[] search) {
        return (textList) -> {
            int results = 0;
            textList.add(new TextComponentString("§l" + I18n.translateToLocal("tj.multiblock.tab.channels") + "§r(§e" + searchResults[0] + "§r/§e" + this.getEnderProfile().getChannels().size() + "§r)"));
            for (Map.Entry<String, V> entry : this.getEnderProfile().getChannels().entrySet()) {
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
                textList.add(keyEntry);
                this.addChannelText(keyEntry, entry.getKey(), entry.getValue());
            }
            searchResults[0] = results;
        };
    }

    private Consumer<List<ITextComponent>> addFrequencyDisplayText(int[] searchResults, int[][] patternFlags, String[] search) {
        return (textList) -> {
            int results = 0;
            textList.add(new TextComponentString("§l" + I18n.translateToLocal("tj.multiblock.tab.frequencies") + "§r(§e" + searchResults[1] + "§r/§e" + this.getPlayerMap().size() + "§r)"));
            for (Map.Entry<String, EnderCoverProfile<V>> entry : this.getPlayerMap().entrySet()) {
                String text =  entry.getKey() != null ? entry.getKey() : "PUBLIC";
                if (!search[1].isEmpty() && !Pattern.compile(search[1], this.getFlags(patternFlags[1])).matcher(text).find())
                    continue;

                textList.add(new TextComponentString(": [§a" + (++results) + "§r] " + text + (text.equals(this.frequency) ? " §a<<<" : ""))
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

    protected abstract void addChannelText(ITextComponent keyEntry, String key, V value);

    private String[] getTooltipFormat() {
        return ArrayUtils.toArray(getTransferRate());
    }

    private void setTransferRate(String amount, String id) {
        this.transferRate = Math.min(Integer.parseInt(amount), this.maxTransferRate);
        this.markAsDirty();
    }

    public String getTransferRate() {
        return String.valueOf(this.transferRate);
    }

    private void onIncrement(String id) {
        this.transferRate = (int) MathHelper.clamp(this.transferRate * 2, 1, Math.min(this.maxTransferRate, this.getEnderProfile().maxThroughPut(UUID.fromString(id))));
        this.markAsDirty();
    }

    private void onDecrement(String id) {
        this.transferRate = (int) MathHelper.clamp(this.transferRate / 2, 1, Math.min(this.maxTransferRate, this.getEnderProfile().maxThroughPut(UUID.fromString(id))));
        this.markAsDirty();
    }

    private void setPumpMode(CoverPump.PumpMode pumpMode) {
        this.pumpMode = pumpMode;
        this.markAsDirty();
    }

    private void setPublic(boolean isPublic, String uuid) {
        if (this.getEnderProfile().getOwner() != null && this.getEnderProfile().getOwner().equals(UUID.fromString(uuid))) {
            this.getEnderProfile().setPublic(isPublic);
            this.markAsDirty();
        }
    }

    private void addFrequency(String key, String uuid) {
        if (this.getEnderProfile().getOwner() == null || this.getEnderProfile().getAllowedUsers().containsKey(UUID.fromString(uuid))) {
            this.getPlayerMap().putIfAbsent(key, new EnderCoverProfile<>(this.ownerId, new Object2ObjectOpenHashMap<>()));
            this.markAsDirty();
        }
    }

    private void renameFrequency(String key, String id) {
        int index = id.lastIndexOf(":");
        String uuid = id.substring(index + 1);
        String oldKey = id.substring(0, index);
        EnderCoverProfile<V> profile = this.getPlayerMap().get(oldKey);
        if (profile != null && this.getEnderProfile().editFrequency(key, UUID.fromString(uuid))) {
            this.getPlayerMap().put(key, this.getPlayerMap().remove(oldKey));
            this.markAsDirty();
        }
    }

    private boolean removeFrequency(String key, String uuid) {
        if (this.getPlayerMap().get(key).removeFrequency(uuid)) {
            this.getPlayerMap().remove(key);
            this.markAsDirty();
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
        this.markAsDirty();
    }

    private boolean setChannel(String key, String uuid) {
        if (this.getEnderProfile().setChannel(key, this.channel, uuid, this)) {
            this.handler = this.getEnderProfile().getChannels().get(key);
            this.setChannel(key);
            return true;
        } else return false;
    }

    private void addChannel(String key, String uuid) {
        this.getEnderProfile().addChannel(key, uuid, this.createHandler());
        this.markAsDirty();
    }

    private void onClear(boolean toggle, String uuid) {
        this.getEnderProfile().editChannel(this.channel, uuid, this.createHandler());
        this.markAsDirty();
    }

    @Override
    public void setHandler(V handler) {
        this.handler = handler;
    }

    @Override
    public void setChannel(String lastEntry) {
        this.channel = lastEntry;
        this.markAsDirty();
    }

    @Override
    public void readUpdateData(int id, PacketBuffer packetBuffer) {
       if (id == 2) {
           this.ownerId = packetBuffer.readUniqueId();
        }
    }

    @Override
    public void writeInitialSyncData(PacketBuffer packetBuffer) {
        packetBuffer.writeBoolean(this.ownerId != null);
        if (this.ownerId != null)
            packetBuffer.writeUniqueId(this.ownerId);
    }

    @Override
    public void readInitialSyncData(PacketBuffer packetBuffer) {
        if (packetBuffer.readBoolean())
            this.ownerId = packetBuffer.readUniqueId();
    }

    @Override
    public void writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger("pumpMode", this.pumpMode.ordinal());
        data.setBoolean("isWorking", this.isWorkingEnabled);
        data.setInteger("transferRate", this.transferRate);
        if (this.frequency != null)
            data.setString("frequency", this.frequency);
        if (this.ownerId != null)
            data.setUniqueId("ownerId", this.ownerId);
        if (this.channel != null)
            data.setString("channel", this.channel);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.pumpMode = CoverPump.PumpMode.values()[data.getInteger("pumpMode")];
        this.isWorkingEnabled = data.getBoolean("isWorking");
        this.transferRate = data.getInteger("transferRate");
        if (data.hasKey("frequency"))
            this.frequency = data.getString("frequency");
        if (data.hasKey("ownerId"))
            this.ownerId = data.getUniqueId("ownerId");
        if (data.hasKey("channel")) {
            this.channel = data.getString("channel");
            this.handler = this.getEnderProfile().getChannels().get(this.channel);
            this.getEnderProfile().addToNotifiable(this.channel, this);
        }
    }

    @Override
    public void markToDirty() {
        this.markAsDirty();
    }

    @Override
    public boolean isWorkingEnabled() {
        return this.isWorkingEnabled;
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingEnabled) {
        this.isWorkingEnabled = isWorkingEnabled;
        this.markAsDirty();
    }
}
