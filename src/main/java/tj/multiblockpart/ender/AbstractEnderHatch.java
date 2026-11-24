package tj.multiblockpart.ender;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregicadditions.machines.multi.multiblockpart.GAMetaTileEntityMultiblockPart;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.*;
import gregtech.api.gui.widgets.tab.VerticalTabListRenderer;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.util.function.BooleanConsumer;
import gregtech.common.covers.CoverPump;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.ArrayUtils;
import tj.TJValues;
import tj.mui.TJGuiUtils;
import tj.mui.widgets.ButtonWidget;
import tj.mui.widgets.TJModularUI;
import tj.mui.widgets.WidgetTabBuilder;
import tj.capability.IEnderNotifiable;
import tj.mui.TJGuiTextures;
import tj.mui.widgets.impl.*;
import tj.items.covers.EnderCoverProfile;
import tj.textures.TJSimpleOverlayRenderer;
import tj.textures.TJTextures;
import tj.util.TJItemUtils;
import tj.util.TextUtils;
import tj.util.references.ObjectReference;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import static gregtech.api.gui.GuiTextures.*;
import static gregtech.api.gui.GuiTextures.TOGGLE_BUTTON_BACK;
import static gregtech.api.gui.widgets.AdvancedTextWidget.withButton;
import static gregtech.api.gui.widgets.tab.VerticalTabListRenderer.HorizontalLocation.LEFT;
import static gregtech.api.gui.widgets.tab.VerticalTabListRenderer.VerticalStartCorner.TOP;
import static net.minecraft.util.text.TextFormatting.GRAY;
import static net.minecraft.util.text.TextFormatting.YELLOW;
import static tj.mui.TJGuiTextures.*;
import static tj.mui.TJGuiTextures.LIST_OVERLAY;

public abstract class AbstractEnderHatch<E, V> extends GAMetaTileEntityMultiblockPart implements IControllable,
        IMultiblockAbilityPart<E>,
        IEnderNotifiable<V> {

    protected String frequency;
    protected String channel;
    protected String displayName;
    protected UUID ownerId;
    protected boolean isWorkingEnabled;
    protected CoverPump.PumpMode pumpMode = CoverPump.PumpMode.IMPORT;
    private MultiblockControllerBase controller;
    protected int maxTransferRate;
    protected int transferRate;
    protected V handler;

    public AbstractEnderHatch(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(net.minecraft.client.resources.I18n.format("metaitem.ender_cover.description"));
    }

    @Override
    public boolean onRightClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        if (this.ownerId == null) {
            this.ownerId = playerIn.getUniqueID();
            this.displayName = playerIn.getDisplayNameString();
        }
        return super.onRightClick(playerIn, hand, facing, hitResult);
    }

    @Override
    public ModularUI createUI(EntityPlayer player) {
        final int[] searchResults = new int[3];
        final int[][] patternFlags = new int[3][9];
        final String[] search = {"", "", ""};
        final ObjectReference<UUID> playerUUID = new ObjectReference<>();
        final WidgetTabBuilder tabBuilder = new WidgetTabBuilder()
                .setTabListRenderer(() -> new VerticalTabListRenderer(TOP, LEFT))
                .addWidget(new TJLabelWidget(7, -19, 162, 19, TJGuiTextures.MACHINE_LABEL_2)
                        .setLocale(this.getMetaFullName()).setItemLabel(this.getStackForm()))
                .addTab(this.getMetaFullName(), this.getStackForm(),
                        tab -> this.createChannelTab(tab, player, searchResults, patternFlags, search))
                .addTab("tj.multiblock.tab.frequencies", TJItemUtils.getItemStackFromName("appliedenergistics2:part", 1, 76),
                        tab -> this.createFrequencyTab(tab, player, searchResults, patternFlags, search, playerUUID));
        return TJModularUI.builder(this.getHolder(), player)
                .setHeight(262)
                .widget(tabBuilder.build())
                .widget(tabBuilder.buildWidgetGroup())
                .build();
    }

    private void createChannelTab(List<Widget> tab, EntityPlayer player, int[] searchResults, int[][] patternFlags, String[] search) {
        final NewTextFieldWidget<?> textFieldRename = new NewTextFieldWidget<>(12, 15, 159, 18, true)
                .setValidator(str -> Pattern.compile(".*").matcher(str).matches())
                .setBackgroundText("machine.universal.toggle.rename.channel")
                .setTooltipText("machine.universal.toggle.rename.channel")
                .setTextResponder(this.getEnderProfile()::editChannel)
                .setMaxStringLength(256);
        final NewTextFieldWidget<?> textFieldAdd = new NewTextFieldWidget<>(12, 15, 159, 18, true)
                .setTextResponder((key, id) -> this.getEnderProfile().addChannel(key, id, this))
                .setValidator(str -> Pattern.compile(".*").matcher(str).matches())
                .setBackgroundText("machine.universal.toggle.add.channel")
                .setTooltipText("machine.universal.toggle.add.channel")
                .setTextId(player.getUniqueID().toString())
                .setMaxStringLength(256);

        final PanelWidgetGroup channelSearchPanel = new PanelWidgetGroup(112, 47, 60, 92, BORDERED_BACKGROUND);
        final PanelWidgetGroup channelRenamePanel = new PanelWidgetGroup(0, 61, 182, 60, BORDERED_BACKGROUND);
        final PanelWidgetGroup channelAddPanel = new PanelWidgetGroup(0, 61, 182, 60, BORDERED_BACKGROUND);
        final PanelWidgetGroup failPanel = TJGuiUtils.createFailPanel(player);
        tab.add(new TJImageWidget(3, 61, 170, 80, MULTIBLOCK_DISPLAY_BASE));
        tab.add(new ScrollingWidgetGroup<>(3, 61, 172, 80)
                .addSubWidget(new TJAdvancedTextWidget(2, 3, this.addChannelDisplayText(searchResults, patternFlags, search),
                        0xFFFFFF).setMaxWidthLimit(1000)
                        .setClickHandler((componentData, clickData) -> {
                            final String[] components = componentData.split(":");
                            if (components[0].equals("select") && components[1].equals("channel")) {
                                if (this.getEnderProfile().hasPermission(player.getUniqueID(), EnderCoverProfile.CAN_USE_CHANNEL)) {
                                    this.getEnderProfile().setChannel(components[2], player.getUniqueID().toString(), this);
                                } else failPanel.openPanel();
                            } else if (components[0].equals("remove") && components[1].equals("channel")) {
                                if (this.getEnderProfile().hasPermission(player.getUniqueID(), EnderCoverProfile.CAN_MODIFY_CHANNELS)) {
                                    this.getEnderProfile().removeChannel(components[2], player.getUniqueID().toString());
                                } else failPanel.openPanel();
                            } else if (components[0].equals("rename") && components[1].equals("channel")) {
                                if (this.getEnderProfile().hasPermission(player.getUniqueID(), 4)) {
                                    textFieldRename.setTextId(components[2] + ":" + player.getUniqueID(), true);
                                    channelRenamePanel.openPanel();
                                } else failPanel.openPanel();
                            }
                        })));
        tab.add(new NewTextFieldWidget<>(30, 43, 116, 18, true)
                .setTextResponder((key, id) -> this.getEnderProfile().setChannel(key, id, this))
                .setValidator(str -> Pattern.compile(".*").matcher(str).matches())
                .setBackgroundText("machine.universal.toggle.current.channel")
                .setTooltipText("machine.universal.toggle.current.channel")
                .setTextId(player.getUniqueID().toString())
                .setTextSupplier(this::getChannel)
                .setMaxStringLength(256)
                .setUpdateOnTyping(true));
        tab.add(new NewTextFieldWidget<>(30, 20, 116, 18, true)
                .setValidator(str -> Pattern.compile("\\*?[0-9_]*\\*?").matcher(str).matches())
                .setBackgroundText("metaitem.ender_cover.transfer")
                .setTooltipText("metaitem.ender_cover.transfer")
                .setTooltipFormat(this::getTooltipFormat)
                .setTextResponder(this::setTransferRate)
                .setTextSupplier(this::getTransferRate)
                .setUpdateOnTyping(true));
        tab.add(new NewTextFieldWidget<>(30, 147, 116, 18, true)
                .setValidator(str -> Pattern.compile(".*").matcher(str).matches())
                .setTextResponder((result, id) -> search[0] = result)
                .setBackgroundText("machine.universal.search")
                .setTextSupplier(() -> search[0])
                .setMaxStringLength(256)
                .setUpdateOnTyping(true));
        tab.add(new TJToggleButtonWidget(151, 15, 18, 18, TJValues::isFalse, this::onIncrement)
                .setHoverTooltipText("machine.universal.toggle.increment.disabled")
                .setButtonId(player.getUniqueID().toString())
                .setToggleTexture(TOGGLE_BUTTON_BACK)
                .useToggleTexture(true)
                .setDisplayText("+"));
        tab.add(new TJToggleButtonWidget(7, 15, 18, 18, TJValues::isFalse, this::onDecrement)
                .setHoverTooltipText("machine.universal.toggle.decrement.disabled")
                .setButtonId(player.getUniqueID().toString())
                .setToggleTexture(TOGGLE_BUTTON_BACK)
                .useToggleTexture(true)
                .setDisplayText("-"));
        tab.add(new TJToggleButtonWidget(7, 143, 18, 18)
                .setButtonResponder(uuid -> this.getEnderProfile().onClear(uuid, this))
                .setHoverTooltipText("machine.universal.toggle.clear")
                .setButtonId(player.getUniqueID().toString())
                .setBackgroundTextures(BUTTON_CLEAR_GRID)
                .setToggleTexture(TOGGLE_BUTTON_BACK)
                .useToggleTexture(true));
        tab.add(new TJCycleButtonWidget<>(30, 161, 116, 18, CoverPump.PumpMode.class, () -> this.pumpMode, this::setPumpMode)
                .setBackgroundTextures(VANILLA_BUTTON));
        tab.add(new TJToggleButtonWidget(7, 161, 18, 18, TOGGLE_POWER_BUTTON, this::isWorkingEnabled, this::setWorkingEnabled)
                .setToggleTitleTooltipHoverText("machine.universal.toggle.run.mode.disabled",
                        "machine.universal.toggle.run.mode.enabled"));
        tab.add(new TJToggleButtonWidget(151, 142, 18, 18, channelSearchPanel::isPanelOpen, (BooleanConsumer) value -> {
            if (!value && channelSearchPanel.isPanelOpen()) {
                channelSearchPanel.closePanel();
            } else if (value) {
                channelSearchPanel.openPanel();
            }
        }).setItemDisplay(TJItemUtils.getItemStackFromName("enderio:item_material", 1, 11))
                .setTitleHoverTooltipText("machine.universal.search.settings")
                .setToggleTexture(TOGGLE_BUTTON_BACK)
                .useToggleTexture(true));
        tab.add(new TJToggleButtonWidget(151, 38, 18, 18, channelAddPanel::isPanelOpen, (BooleanConsumer) value -> {
            if (!value && channelAddPanel.isPanelOpen()) {
                channelAddPanel.closePanel();
            } else if (value) {
                channelAddPanel.openPanel();
            }
        }).setTitleHoverTooltipText("machine.universal.toggle.add.channel")
                .setDisplayText("O")
                .setToggleTexture(TOGGLE_BUTTON_BACK)
                .useToggleTexture(true));
        this.addWidgets(tab::add);
        this.addExtraWidgetsInChannelTab(tab);
        this.addSearchTextWidgets(channelSearchPanel, patternFlags, 0);
        this.createChannelRenamePanel(channelRenamePanel, textFieldRename);
        this.createChannelAddPanel(channelAddPanel, textFieldAdd);
        tab.add(channelSearchPanel);
        tab.add(channelRenamePanel);
        tab.add(channelAddPanel);
        tab.add(failPanel);
    }

    private void createChannelRenamePanel(PanelWidgetGroup channelRenamePanel, NewTextFieldWidget<?> textFieldRename) {
        channelRenamePanel.addWidget(new TJToggleButtonWidget(10, 35, 81, 18, () -> false,
                (BooleanConsumer) b -> channelRenamePanel.closePanel())
                .setDisplayText("machine.universal.cancel")
                .setToggleTexture(TOGGLE_BUTTON_BACK)
                .useToggleTexture(true));
        channelRenamePanel.addWidget(new TJToggleButtonWidget(91, 35, 81, 18, () -> false, (BooleanConsumer) b -> {
            textFieldRename.triggerResponse();
            channelRenamePanel.closePanel();
        }).setDisplayText("machine.universal.ok")
                .setToggleTexture(TOGGLE_BUTTON_BACK)
                .useToggleTexture(true));
        channelRenamePanel.addWidget(new AdvancedTextWidget(45, 4, (textList) -> {
            final int index = textFieldRename.getTextId().lastIndexOf(":");
            final String entry = textFieldRename.getTextId().substring(0, index);
            textList.add(new TextComponentTranslation("machine.universal.renaming", entry));
        }, 0x404040));
        channelRenamePanel.addWidget(textFieldRename);
    }

    private void createChannelAddPanel(PanelWidgetGroup channelAddPanel, NewTextFieldWidget<?> textFieldAdd) {
        channelAddPanel.addWidget(new TJToggleButtonWidget(10, 35, 81, 18, () -> false,
                (BooleanConsumer) b -> channelAddPanel.closePanel())
                .setDisplayText("machine.universal.cancel")
                .setToggleTexture(TOGGLE_BUTTON_BACK)
                .useToggleTexture(true));
        channelAddPanel.addWidget(new TJToggleButtonWidget(91, 35, 81, 18, () -> false, (BooleanConsumer) b -> {
            textFieldAdd.triggerResponse();
            channelAddPanel.closePanel();
        }).setDisplayText("machine.universal.ok")
                .setToggleTexture(TOGGLE_BUTTON_BACK)
                .useToggleTexture(true));
        channelAddPanel.addWidget(new AdvancedTextWidget(55, 4, textList ->
                textList.add(new TextComponentTranslation("machine.universal.toggle.add.channel")), 0x404040));
        channelAddPanel.addWidget(textFieldAdd);
    }

    private void createFrequencyTab(List<Widget> tab,
                                    EntityPlayer player,
                                    int[] searchResults,
                                    int[][] patternFlags,
                                    String[] search,
                                    ObjectReference<UUID> playerUUID) {
        final NewTextFieldWidget<?> textFieldRename = new NewTextFieldWidget<>(12, 15, 159, 18, true)
                .setTextResponder((key, id) -> this.getEnderProfile().renameFrequency(key, id, this))
                .setValidator(str -> Pattern.compile(".*").matcher(str).matches())
                .setBackgroundText("machine.universal.toggle.rename.frequency")
                .setTooltipText("machine.universal.toggle.rename.frequency")
                .setMaxStringLength(256);
        final NewTextFieldWidget<?> textFieldAdd = new NewTextFieldWidget<>(12, 15, 159, 18, true)
                .setTextResponder((key, id) -> this.getEnderProfile().addFrequency(key, id, this))
                .setValidator(str -> Pattern.compile(".*").matcher(str).matches())
                .setBackgroundText("machine.universal.toggle.add.frequency")
                .setTooltipText("machine.universal.toggle.add.frequency")
                .setTextId(player.getUniqueID().toString())
                .setMaxStringLength(256);

        final PanelWidgetGroup frequencySearchPanel = new PanelWidgetGroup(112, 47, 60, 92, BORDERED_BACKGROUND);
        final PanelWidgetGroup frequencyRenamePanel = new PanelWidgetGroup(0, 61, 182, 60, BORDERED_BACKGROUND);
        final PanelWidgetGroup frequencyAddPanel = new PanelWidgetGroup(0, 61, 182, 60, BORDERED_BACKGROUND);
        final PanelWidgetGroup playerSearchPanel = new PanelWidgetGroup(117, 25, 60, 92, BORDERED_BACKGROUND);
        final PanelWidgetGroup allowedPlayersPanel = new PanelWidgetGroup(0, 38, 182, 130, BORDERED_BACKGROUND);
        final PanelWidgetGroup playerPermissionPanel = new PanelWidgetGroup(0, 0, 182, 100, BORDERED_BACKGROUND);
        final PanelWidgetGroup failPanel = TJGuiUtils.createFailPanel(player);
        tab.add(new TJImageWidget(3, 38, 170, 103, MULTIBLOCK_DISPLAY_BASE));
        tab.add(new ScrollingWidgetGroup<>(3, 38, 172, 103)
                .addSubWidget(new TJAdvancedTextWidget(2, 3, this.addFrequencyDisplayText(searchResults, patternFlags, search),
                        0xFFFFFF).setMaxWidthLimit(1000)
                        .setClickHandler((componentData, clickData) -> {
                            final String[] components = componentData.split(":");
                            if (components[0].equals("select") && components[1].equals("frequency")) {
                                if (this.getEnderProfile().hasPermission(player.getUniqueID(), EnderCoverProfile.CAN_USE_ENTRY)) {
                                    this.getEnderProfile().setFrequency(components[2], player.getUniqueID().toString(), this);
                                } else failPanel.openPanel();
                            } else if (components[0].equals("remove") && components[1].equals("frequency")) {
                                if (this.getEnderProfile().hasPermission(player.getUniqueID(), EnderCoverProfile.CAN_MODIFY_ENTRIES)) {
                                    this.getEnderProfile().removeFrequency(components[2], player.getUniqueID().toString(), this);
                                } else failPanel.openPanel();
                            } else if (components[0].equals("rename") && components[1].equals("frequency")) {
                                if (this.getEnderProfile().hasPermission(player.getUniqueID(), EnderCoverProfile.CAN_MODIFY_ENTRIES)) {
                                    textFieldRename.setTextId(components[2] + ":" + player.getUniqueID(), true);
                                    frequencyRenamePanel.openPanel();
                                } else failPanel.openPanel();
                            }
                        })));
        tab.add(new NewTextFieldWidget<>(30, 20, 116, 18, true)
                .setTextResponder((key, id) -> this.getEnderProfile().setFrequency(key, id, this))
                .setValidator(str -> Pattern.compile(".*").matcher(str).matches())
                .setBackgroundText("machine.universal.toggle.current.frequency")
                .setTooltipText("machine.universal.toggle.current.frequency")
                .setTextId(player.getUniqueID().toString())
                .setTextSupplier(this::getFrequency)
                .setMaxStringLength(256)
                .setUpdateOnTyping(true));
        tab.add(new TJToggleButtonWidget(7, 15, 18, 18)
                .setButtonSupplier(this.getEnderProfile()::isPublic)
                .setButtonId(player.getUniqueID().toString())
                .setToggleButtonResponder(this.getEnderProfile()::setPublic)
                .setToggleTexture(UNLOCK_LOCK)
                .useToggleTexture(true));
        tab.add(new NewTextFieldWidget<>(30, 147, 116, 18, true)
                .setValidator(str -> Pattern.compile(".*").matcher(str).matches())
                .setTextResponder((result, id) -> search[1] = result)
                .setBackgroundText("machine.universal.search")
                .setTextSupplier(() -> search[1])
                .setMaxStringLength(256)
                .setUpdateOnTyping(true));
        tab.add(new AdvancedTextWidget(3, 170, textList ->
                textList.add(new TextComponentTranslation("machine.universal.owner", this.displayName)), 0x404040));
        tab.add(new TJToggleButtonWidget(151, 15, 18, 18, frequencyAddPanel::isPanelOpen, (BooleanConsumer) value -> {
            if (!value && frequencyAddPanel.isPanelOpen()) {
                frequencyAddPanel.closePanel();
            } else if (value) {
                frequencyAddPanel.openPanel();
            }
        }).setTitleHoverTooltipText("machine.universal.toggle.add.frequency")
                .setToggleTexture(TOGGLE_BUTTON_BACK)
                .useToggleTexture(true)
                .setDisplayText("O"));
        tab.add(new TJToggleButtonWidget(7, 142, 18, 18, allowedPlayersPanel::isPanelOpen, (BooleanConsumer) value -> {
            if (!value && allowedPlayersPanel.isPanelOpen()) {
                allowedPlayersPanel.closePanel();
            } else if (value) {
                allowedPlayersPanel.openPanel();
            }
        }).setTitleHoverTooltipText("metaitem.ender_cover.allowed_players_list")
                .setToggleTexture(TOGGLE_BUTTON_BACK)
                .setBackgroundTextures(LIST_OVERLAY)
                .useToggleTexture(true));
        tab.add(new TJToggleButtonWidget(151, 142, 18, 18, frequencySearchPanel::isPanelOpen, (BooleanConsumer) value -> {
            if (!value && frequencySearchPanel.isPanelOpen()) {
                frequencySearchPanel.closePanel();
            } else if (value) {
                frequencySearchPanel.openPanel();
            }
        }).setItemDisplay(TJItemUtils.getItemStackFromName("enderio:item_material", 1, 11))
                .setTitleHoverTooltipText("machine.universal.search.settings")
                .setToggleTexture(TOGGLE_BUTTON_BACK)
                .useToggleTexture(true));
        this.addSearchTextWidgets(frequencySearchPanel, patternFlags, 1);
        this.createFrequencyRenamePanel(frequencyRenamePanel, textFieldRename);
        this.createFrequencyAddPanel(frequencyAddPanel, textFieldAdd);
        this.addSearchTextWidgets(playerSearchPanel, patternFlags, 2);
        this.createAllowedPlayersPanel(allowedPlayersPanel, playerPermissionPanel, playerSearchPanel, failPanel,
                player, searchResults, patternFlags, search, playerUUID);
        this.createPlayerPermissionPanel(playerPermissionPanel, playerUUID);
        tab.add(frequencySearchPanel);
        tab.add(frequencyRenamePanel);
        tab.add(frequencyAddPanel);
        tab.add(allowedPlayersPanel);
        tab.add(playerPermissionPanel);
        tab.add(playerSearchPanel);
        tab.add(failPanel);
    }

    private void createFrequencyRenamePanel(PanelWidgetGroup frequencyRenamePanel, NewTextFieldWidget<?> textFieldRename) {
        frequencyRenamePanel.addWidget(new TJToggleButtonWidget(10, 35, 81, 18, () -> false, (BooleanConsumer) b ->
                frequencyRenamePanel.closePanel())
                .setDisplayText("machine.universal.cancel")
                .setToggleTexture(TOGGLE_BUTTON_BACK)
                .useToggleTexture(true));
        frequencyRenamePanel.addWidget(new TJToggleButtonWidget(91, 35, 81, 18, () -> false, (BooleanConsumer) b -> {
            textFieldRename.triggerResponse();
            frequencyRenamePanel.closePanel();
        }).setDisplayText("machine.universal.ok")
                .setToggleTexture(TOGGLE_BUTTON_BACK)
                .useToggleTexture(true));
        frequencyRenamePanel.addWidget(new TJImageWidget(10, 15, 162, 18, DISPLAY));
        frequencyRenamePanel.addWidget(new AdvancedTextWidget(45, 4, (textList) -> {
            final int index = textFieldRename.getTextId().lastIndexOf(":");
            final String entry = textFieldRename.getTextId().substring(0, index);
            textList.add(new TextComponentTranslation("machine.universal.renaming", entry));
        }, 0x404040));
        frequencyRenamePanel.addWidget(textFieldRename);
    }

    private void createFrequencyAddPanel(PanelWidgetGroup frequencyAddPanel, NewTextFieldWidget<?> textFieldAdd) {
        frequencyAddPanel.addWidget(new TJToggleButtonWidget(10, 35, 81, 18, () -> false,
                (BooleanConsumer) b -> frequencyAddPanel.closePanel())
                .setDisplayText("machine.universal.cancel")
                .setToggleTexture(TOGGLE_BUTTON_BACK)
                .useToggleTexture(true));
        frequencyAddPanel.addWidget(new TJToggleButtonWidget(91, 35, 81, 18, () -> false, (BooleanConsumer) b -> {
            textFieldAdd.triggerResponse();
            frequencyAddPanel.closePanel();
        }).setDisplayText("machine.universal.ok")
                .setToggleTexture(TOGGLE_BUTTON_BACK)
                .useToggleTexture(true));
        frequencyAddPanel.addWidget(new TJImageWidget(10, 15, 162, 18, DISPLAY));
        frequencyAddPanel.addWidget(new AdvancedTextWidget(55, 4, textList ->
                textList.add(new TextComponentTranslation("machine.universal.toggle.add.frequency")), 0x404040));
        frequencyAddPanel.addWidget(textFieldAdd);
    }

    private void createAllowedPlayersPanel(PanelWidgetGroup allowedPlayersPanel,
                                           PanelWidgetGroup playerPermissionPanel,
                                           PanelWidgetGroup playerSearchPanel,
                                           PanelWidgetGroup failPanel,
                                           EntityPlayer player,
                                           int[] searchResults,
                                           int[][] patternFlags,
                                           String[] search,
                                           ObjectReference<UUID> playerUUID) {
        allowedPlayersPanel.addWidget(new TJImageWidget(3, 25, 176, 80, MULTIBLOCK_DISPLAY_BASE));
        allowedPlayersPanel.addWidget(new ButtonWidget<>(166, 4, 12, 12, "", clickData -> allowedPlayersPanel.closePanel())
                .setTitleHoverTooltipText("tj.machine.universal.close_panel")
                .setBackgroundTextures(GuiTextures.BUTTON_CLEAR_GRID));
        allowedPlayersPanel.addWidget(new ScrollingWidgetGroup<>(3, 25, 178, 80)
                .addSubWidget(new TJAdvancedTextWidget(2, 3, this.addPlayerDisplayText(searchResults, patternFlags, search),
                        0xFFFFFF).setMaxWidthLimit(1000)
                        .setClickHandler((componentData, clickData) -> {
                            final String[] components = componentData.split(":");
                            if (this.getEnderProfile().getOwner() != null && this.getEnderProfile().getOwner().toString().equals(components[1])) {
                                switch (components[0]) {
                                    case "add":
                                        this.getEnderProfile().addUser(UUID.fromString(components[1]), player.getUniqueID());
                                        break;
                                    case "remove":
                                        this.getEnderProfile().removeUser(UUID.fromString(components[1]), player.getUniqueID());
                                        break;
                                    case "edit":
                                        playerUUID.setValue(UUID.fromString(components[1]));
                                        playerPermissionPanel.openPanel();
                                        break;
                                }
                            } else failPanel.openPanel();
                        })));
        allowedPlayersPanel.addWidget(new AdvancedTextWidget(10, 4, textList ->
                textList.add(new TextComponentTranslation("metaitem.ender_cover.allowed_players", this.frequency)), 0x404040));
        allowedPlayersPanel.addWidget(new NewTextFieldWidget<>(30, 106, 112, 18, true)
                .setValidator(str -> Pattern.compile(".*").matcher(str).matches())
                .setTextResponder((result, id) -> search[2] = result)
                .setBackgroundText("machine.universal.search")
                .setTextSupplier(() -> search[2])
                .setMaxStringLength(256)
                .setUpdateOnTyping(true));
        allowedPlayersPanel.addWidget(new TJToggleButtonWidget(151, 106, 18, 18, playerSearchPanel::isPanelOpen,
                (BooleanConsumer) value -> {
                    if (!value && playerSearchPanel.isPanelOpen()) {
                        playerSearchPanel.closePanel();
                    } else if (value) {
                        playerSearchPanel.openPanel();
                    }
                }).setItemDisplay(TJItemUtils.getItemStackFromName("enderio:item_material", 1, 11))
                .setHoverTooltipText("machine.universal.search.settings")
                .setToggleTexture(TOGGLE_BUTTON_BACK)
                .useToggleTexture(true));
    }

    private void createPlayerPermissionPanel(PanelWidgetGroup playerPermissionPanel, ObjectReference<UUID> playerUUID) {
        playerPermissionPanel.addWidget(new AdvancedTextWidget(10, 4, textList ->
                textList.add(new TextComponentTranslation("metaitem.ender_cover.edit_permission", playerUUID.getValue())), 0x404040));
        playerPermissionPanel.addWidget(new ButtonWidget<>(166, 4, 12, 12, "", clickData ->
                playerPermissionPanel.closePanel())
                .setTitleHoverTooltipText("tj.machine.universal.close_panel")
                .setBackgroundTextures(GuiTextures.BUTTON_CLEAR_GRID));
        playerPermissionPanel.addWidget(new TJToggleButtonWidget(3, 25, 88, 18,
                () -> this.getEnderProfile().getPermission(playerUUID.getValue(), EnderCoverProfile.CAN_SEE_ENTRIES) != 0,
                (toggle, id) -> this.getEnderProfile().setPermission(playerUUID.getValue(), EnderCoverProfile.CAN_SEE_ENTRIES, toggle ? 1 : 0))
                .setToggleDisplayText("machine.universal.false", "machine.universal.true")
                .setHoverTooltipText("metaitem.ender_cover.permission.0")
                .setToggleTexture(TOGGLE_BUTTON_BACK)
                .useToggleTexture(true));
        playerPermissionPanel.addWidget(new TJToggleButtonWidget(91, 25, 88, 18,
                () -> this.getEnderProfile().getPermission(playerUUID.getValue(), EnderCoverProfile.CAN_MODIFY_ENTRIES) != 0,
                (toggle, id) -> this.getEnderProfile().setPermission(playerUUID.getValue(), EnderCoverProfile.CAN_MODIFY_ENTRIES, toggle ? 1 : 0))
                .setToggleDisplayText("machine.universal.false", "machine.universal.true")
                .setHoverTooltipText("metaitem.ender_cover.permission.1")
                .setToggleTexture(TOGGLE_BUTTON_BACK)
                .useToggleTexture(true));
        playerPermissionPanel.addWidget(new TJToggleButtonWidget(3, 43, 88, 18,
                () -> this.getEnderProfile().getPermission(playerUUID.getValue(), EnderCoverProfile.CAN_USE_ENTRY) != 0,
                (toggle, id) -> this.getEnderProfile().setPermission(playerUUID.getValue(), EnderCoverProfile.CAN_USE_ENTRY, toggle ? 1 : 0))
                .setToggleDisplayText("machine.universal.false", "machine.universal.true")
                .setHoverTooltipText("metaitem.ender_cover.permission.2")
                .setToggleTexture(TOGGLE_BUTTON_BACK)
                .useToggleTexture(true));
        playerPermissionPanel.addWidget(new TJToggleButtonWidget(91, 43, 88, 18,
                () -> this.getEnderProfile().getPermission(playerUUID.getValue(), EnderCoverProfile.CAN_SEE_CHANNELS) != 0,
                (toggle, id) -> this.getEnderProfile().setPermission(playerUUID.getValue(), EnderCoverProfile.CAN_SEE_CHANNELS, toggle ? 1 : 0))
                .setToggleDisplayText("machine.universal.false", "machine.universal.true")
                .setHoverTooltipText("metaitem.ender_cover.permission.3")
                .setToggleTexture(TOGGLE_BUTTON_BACK)
                .useToggleTexture(true));
        playerPermissionPanel.addWidget(new TJToggleButtonWidget(3, 61, 88, 18,
                () -> this.getEnderProfile().getPermission(playerUUID.getValue(), EnderCoverProfile.CAN_MODIFY_CHANNELS) != 0,
                (toggle, id) -> this.getEnderProfile().setPermission(playerUUID.getValue(), EnderCoverProfile.CAN_MODIFY_CHANNELS, toggle ? 1 : 0))
                .setToggleDisplayText("machine.universal.false", "machine.universal.true")
                .setHoverTooltipText("metaitem.ender_cover.permission.4")
                .setToggleTexture(TOGGLE_BUTTON_BACK)
                .useToggleTexture(true));
        playerPermissionPanel.addWidget(new TJToggleButtonWidget(91, 61, 88, 18,
                () -> this.getEnderProfile().getPermission(playerUUID.getValue(), EnderCoverProfile.CAN_USE_CHANNEL) != 0,
                (toggle, id) -> this.getEnderProfile().setPermission(playerUUID.getValue(), EnderCoverProfile.CAN_USE_CHANNEL, toggle ? 1 : 0))
                .setToggleDisplayText("machine.universal.false", "machine.universal.true")
                .setHoverTooltipText("metaitem.ender_cover.permission.5")
                .setToggleTexture(TOGGLE_BUTTON_BACK)
                .useToggleTexture(true));
        playerPermissionPanel.addWidget(new NewTextFieldWidget<>(5, 79, 174, 18, true,
                () -> String.valueOf(this.getEnderProfile().getPermission(playerUUID.getValue(), EnderCoverProfile.MAX_THROUGHPUT)),
                (text, id) -> this.getEnderProfile().setPermission(playerUUID.getValue(), EnderCoverProfile.MAX_THROUGHPUT, Long.parseLong(text)))
                .setValidator(str -> Pattern.compile("-*?[0-9_]*\\*?").matcher(str).matches())
                .setTooltipText("metaitem.ender_cover.permission.6")
                .setUpdateOnTyping(true));
    }

    private void addSearchTextWidgets(PanelWidgetGroup widgetGroup, int[][] patternFlags, int i) {
        widgetGroup.addWidget(new TJImageWidget(3, 71, 54, 18, DISPLAY));
        widgetGroup.addWidget(new AdvancedTextWidget(5, 76, textList ->
                textList.add(new TextComponentTranslation("string.regex.flag", this.getFlags(patternFlags[i]))), 0x404040));
        widgetGroup.addWidget(new ButtonWidget<>(44, 4, 12, 12, "", clickData -> widgetGroup.closePanel())
                .setTitleHoverTooltipText("tj.machine.universal.close_panel")
                .setBackgroundTextures(GuiTextures.BUTTON_CLEAR_GRID));
        widgetGroup.addWidget(new TJToggleButtonWidget(3, 20, 18, 18)
                .setToggleButtonResponder((pressed, s) -> patternFlags[i][0] = pressed ? Pattern.UNIX_LINES : 0)
                .setDisplayText("string.regex.pattern.unix_lines.flag")
                .setHoverTooltipText("string.regex.pattern.unix_lines")
                .setButtonSupplier(() -> patternFlags[i][0] != 0)
                .setToggleTexture(TOGGLE_BUTTON_BACK)
                .useToggleTexture(true));
        widgetGroup.addWidget(new TJToggleButtonWidget(21, 20, 18, 18)
                .setToggleButtonResponder((pressed, s) -> patternFlags[i][1] = pressed ? Pattern.CASE_INSENSITIVE : 0)
                .setDisplayText("string.regex.pattern.case_insensitive.flag")
                .setHoverTooltipText("string.regex.pattern.case_insensitive")
                .setButtonSupplier(() -> patternFlags[i][1] != 0)
                .setToggleTexture(TOGGLE_BUTTON_BACK)
                .useToggleTexture(true));
        widgetGroup.addWidget(new TJToggleButtonWidget(39, 20, 18, 18)
                .setToggleButtonResponder((pressed, s) -> patternFlags[i][2] = pressed ? Pattern.COMMENTS : 0)
                .setDisplayText("string.regex.pattern.comments.flag")
                .setHoverTooltipText("string.regex.pattern.comments")
                .setButtonSupplier(() -> patternFlags[i][2] != 0)
                .setToggleTexture(TOGGLE_BUTTON_BACK)
                .useToggleTexture(true));
        widgetGroup.addWidget(new TJToggleButtonWidget(3, 35, 18, 18)
                .setToggleButtonResponder((pressed, s) -> patternFlags[i][3] = pressed ? Pattern.MULTILINE : 0)
                .setDisplayText("string.regex.pattern.multiline.flag")
                .setHoverTooltipText("string.regex.pattern.multiline")
                .setButtonSupplier(() -> patternFlags[i][3] != 0)
                .setToggleTexture(TOGGLE_BUTTON_BACK)
                .useToggleTexture(true));
        widgetGroup.addWidget(new TJToggleButtonWidget(21, 35, 18, 18)
                .setToggleButtonResponder((pressed, s) -> patternFlags[i][4] = pressed ? Pattern.LITERAL : 0)
                .setDisplayText("string.regex.pattern.literal.flag")
                .setHoverTooltipText("string.regex.pattern.literal")
                .setButtonSupplier(() -> patternFlags[i][4] != 0)
                .setToggleTexture(TOGGLE_BUTTON_BACK)
                .useToggleTexture(true));
        widgetGroup.addWidget(new TJToggleButtonWidget(39, 35, 18, 18)
                .setToggleButtonResponder((pressed, s) -> patternFlags[i][5] = pressed ? Pattern.DOTALL : 0)
                .setDisplayText("string.regex.pattern.dotall.flag")
                .setHoverTooltipText("string.regex.pattern.dotall")
                .setButtonSupplier(() -> patternFlags[i][5] != 0)
                .setToggleTexture(TOGGLE_BUTTON_BACK)
                .useToggleTexture(true));
        widgetGroup.addWidget(new TJToggleButtonWidget(3, 53, 18, 18)
                .setToggleButtonResponder((pressed, s) -> patternFlags[i][6] = pressed ? Pattern.UNICODE_CASE : 0)
                .setDisplayText("string.regex.pattern.unicode_case.flag")
                .setHoverTooltipText("string.regex.pattern.unicode_case")
                .setButtonSupplier(() -> patternFlags[i][6] != 0)
                .setToggleTexture(TOGGLE_BUTTON_BACK)
                .useToggleTexture(true));
        widgetGroup.addWidget(new TJToggleButtonWidget(21, 53, 18, 18)
                .setToggleButtonResponder((pressed, s) -> patternFlags[i][7] = pressed ? Pattern.CANON_EQ : 0)
                .setDisplayText("string.regex.pattern.canon_eq.flag")
                .setHoverTooltipText("string.regex.pattern.canon_eq")
                .setButtonSupplier(() -> patternFlags[i][7] != 0)
                .setToggleTexture(TOGGLE_BUTTON_BACK)
                .useToggleTexture(true));
        widgetGroup.addWidget(new TJToggleButtonWidget(39, 53, 18, 18)
                .setToggleButtonResponder((pressed, s) -> patternFlags[i][8] = pressed ? Pattern.UNICODE_CHARACTER_CLASS : 0)
                .setDisplayText("string.regex.pattern.unicode_character_class.flag")
                .setHoverTooltipText("string.regex.pattern.unicode_character_class")
                .setButtonSupplier(() -> patternFlags[i][8] != 0)
                .setToggleTexture(TOGGLE_BUTTON_BACK)
                .useToggleTexture(true));
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
            textList.add(new TextComponentString("§l" + TextUtils.translate("tj.multiblock.tab.channels") + "§r(§e" + searchResults[0]
                    + "§r/§e" + this.getEnderProfile().getChannels().size() + "§r)"));
            for (Map.Entry<String, V> entry : this.getEnderProfile().getChannels().entrySet()) {
                String text = entry.getKey();
                if (!search[0].isEmpty() && !Pattern.compile(search[0], this.getFlags(patternFlags[0])).matcher(text).find())
                    continue;

                ITextComponent keyEntry = new TextComponentString(": [§a" + (++results) + "§r] " + text + (text.equals(this.channel) ? " §a<<<" : ""))
                        .appendText("\n")
                        .appendSibling(TJAdvancedTextWidget.withButton(new TextComponentTranslation("machine.universal.linked.select")
                                .setStyle(new Style().setColor(text.equals(this.channel) ? GRAY : YELLOW)), "select:channel:" + text))
                        .appendText(" ")
                        .appendSibling(withButton(new TextComponentTranslation("machine.universal.linked.remove"),
                                "remove:channel:" + text))
                        .appendText(" ")
                        .appendSibling(withButton(new TextComponentTranslation("machine.universal.linked.rename"),
                                "rename:channel:" + text));
                textList.add(keyEntry);
                this.addChannelText(keyEntry, entry.getKey(), entry.getValue());
            }
            searchResults[0] = results;
        };
    }

    private Consumer<List<ITextComponent>> addFrequencyDisplayText(int[] searchResults, int[][] patternFlags, String[] search) {
        return (textList) -> {
            int results = 0;
            textList.add(new TextComponentString("§l" + TextUtils.translate("tj.multiblock.tab.frequencies") + "§r(§e" + searchResults[1]
                    + "§r/§e" + this.getPlayerMap().size() + "§r)"));
            for (Map.Entry<String, EnderCoverProfile<V>> entry : this.getPlayerMap().entrySet()) {
                String text =  entry.getKey() != null ? entry.getKey() : "PUBLIC";
                if (!search[1].isEmpty() && !Pattern.compile(search[1], this.getFlags(patternFlags[1])).matcher(text).find())
                    continue;

                textList.add(new TextComponentString(": [§a" + (++results) + "§r] " + text + (text.equals(this.frequency) ? " §a<<<" : ""))
                        .appendText("\n")
                        .appendSibling(TJAdvancedTextWidget.withButton(new TextComponentTranslation("machine.universal.linked.select")
                                .setStyle(new Style().setColor(text.equals(this.frequency) ? GRAY : YELLOW)), "select:frequency:" + text))
                        .appendText(" ")
                        .appendSibling(withButton(new TextComponentTranslation("machine.universal.linked.remove"),
                                "remove:frequency:" + text))
                        .appendText(" ")
                        .appendSibling(withButton(new TextComponentTranslation("machine.universal.linked.rename"),
                                "rename:frequency:" + text))
                        .setStyle(new Style().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                new TextComponentTranslation("machine.universal.owner", entry.getValue().getOwner())))));
            }
            searchResults[1] = results;
        };
    }

    private Consumer<List<ITextComponent>> addPlayerDisplayText(int[] searchResults, int[][] patternFlags, String[] search) {
        return (textList) -> {
            int results = 0;
            List<EntityPlayerMP> playerList = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers();
            textList.add(new TextComponentString("§l" + TextUtils.translate("tj.multiblock.tab.players") + "§r(§e" + searchResults[2]
                    + "§r/§e" + playerList.size() + "§r)"));
            for (EntityPlayer player : playerList) {
                String text = player.getDisplayNameString();
                if (!search[2].isEmpty() && !Pattern.compile(search[2], this.getFlags(patternFlags[2])).matcher(text).find())
                    continue;
                boolean contains = this.getEnderProfile().getAllowedUsers().containsKey(player.getUniqueID());
                textList.add(new TextComponentString(": [§a" + (++results) + "§r] " + text)
                        .setStyle(new Style().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString(text)))).appendText("\n")
                        .appendSibling(TJAdvancedTextWidget.withButton(new TextComponentTranslation("machine.universal.linked.add").
                                setStyle(new Style().setColor(contains ? GRAY : YELLOW)), "add:" + player.getUniqueID()))
                        .appendText(" ")
                        .appendSibling(TJAdvancedTextWidget.withButton(new TextComponentTranslation("machine.universal.linked.remove")
                                .setStyle(new Style().setColor(contains ? YELLOW : GRAY)), "remove:" + player.getUniqueID()))
                        .appendText(" ")
                        .appendSibling(TJAdvancedTextWidget.withButton(new TextComponentTranslation("machine.universal.linked.edit")
                                .setStyle(new Style().setColor(contains ? GRAY : YELLOW)), "edit:" + player.getUniqueID() + ":" + text)));
            }
            searchResults[2] = results;
        };
    }

    protected abstract void addChannelText(ITextComponent keyEntry, String key, V value);

    protected abstract TJSimpleOverlayRenderer getOverlay();

    protected abstract Object2ObjectMap<String, EnderCoverProfile<V>> getPlayerMap();

    protected abstract void addWidgets(Consumer<Widget> widget);

    protected void addExtraWidgetsInChannelTab(List<Widget> tab) {}

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        int oldBaseColor = renderState.baseColour;
        int oldAlphaOverride = renderState.alphaOverride;

        renderState.baseColour = this.getPortalColor() << 8;
        renderState.alphaOverride = 0xFF;
        this.getOverlay().renderSided(this.frontFacing, renderState, translation, pipeline);

        renderState.baseColour = TJValues.VC[this.getTier()] << 8;
        TJTextures.INSIDE_OVERLAY_BASE.renderSided(this.frontFacing, renderState, translation, pipeline);

        renderState.baseColour = oldBaseColor;
        renderState.alphaOverride = oldAlphaOverride;
        if (this.getController() == null)
            TJTextures.OUTSIDE_OVERLAY_BASE.renderSided(this.frontFacing, renderState, translation, pipeline);
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == 2) {
            this.ownerId = buf.readUniqueId();
        }
    }

    @Override
    public void writeInitialSyncData(PacketBuffer packetBuffer) {
        super.writeInitialSyncData(packetBuffer);
        packetBuffer.writeBoolean(this.ownerId != null);
        if (this.ownerId != null)
            packetBuffer.writeUniqueId(this.ownerId);
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
        data.setInteger("pumpMode", this.pumpMode.ordinal());
        data.setBoolean("isWorking", this.isWorkingEnabled);
        data.setInteger("transferRate", this.transferRate);
        if (this.frequency != null)
            data.setString("frequency", this.frequency);
        if (this.ownerId != null)
            data.setUniqueId("ownerId", this.ownerId);
        if (this.channel != null)
            data.setString("channel", this.channel);
        return data;
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
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE)
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        return super.getCapability(capability, side);
    }

    @Nonnull
    protected EnderCoverProfile<V> getEnderProfile()  {
        return this.getPlayerMap().getOrDefault(this.frequency, this.getPlayerMap().get(null));
    }

    protected int getPortalColor() {
        return 0xffffff;
    }

    @Override
    public boolean isWorkingEnabled() {
        return this.isWorkingEnabled;
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingEnabled) {
        this.isWorkingEnabled = isWorkingEnabled;
        this.markDirty();
    }

    @Override
    public void markToDirty() {
        this.markDirty();
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
        this.transferRate = (int) MathHelper.clamp(this.transferRate * 2, 1,
                Math.min(this.maxTransferRate, this.getEnderProfile().maxThroughPut(UUID.fromString(id))));
        this.markDirty();
    }

    private void onDecrement(String id) {
        this.transferRate = (int) MathHelper.clamp(this.transferRate / 2, 1,
                Math.min(this.maxTransferRate, this.getEnderProfile().maxThroughPut(UUID.fromString(id))));
        this.markDirty();
    }

    private void setPumpMode(CoverPump.PumpMode pumpMode) {
        this.pumpMode = pumpMode;
        this.markDirty();
    }

    @Override
    public void setFrequency(String frequency) {
        this.frequency = frequency;
        this.markDirty();
    }

    @Override
    public String getFrequency() {
        return this.frequency;
    }

    @Override
    public String getChannel() {
        return this.channel;
    }

    @Override
    public void setHandler(V handler) {
        this.handler = handler;
        this.markDirty();
    }

    @Override
    public void setChannel(String lastEntry) {
        this.channel = lastEntry;
        this.markDirty();
    }

    @Override
    public void addToMultiBlock(MultiblockControllerBase controller) {
        super.addToMultiBlock(controller);
        this.controller = controller;
    }

    @Override
    public void removeFromMultiBlock(MultiblockControllerBase controller) {
        super.removeFromMultiBlock(controller);
        this.controller = null;
    }
}