package tj.machines.multi.electric;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.metatileentity.MTETrait;
import net.minecraft.item.Item;
import net.minecraft.util.text.Style;
import net.minecraftforge.fluids.FluidStack;
import tj.builder.WidgetTabBuilder;
import tj.builder.handlers.TeleporterWorkableHandler;
import tj.builder.multicontrollers.TJMultiblockDisplayBase;
import tj.builder.multicontrollers.UIDisplayBuilder;
import tj.capability.*;
import tj.gui.TJGuiTextures;
import tj.gui.widgets.AdvancedDisplayWidget;
import tj.gui.widgets.NewTextFieldWidget;
import tj.gui.widgets.TJAdvancedTextWidget;
import gregicadditions.GAUtility;
import gregicadditions.GAValues;
import gregicadditions.item.GAMultiblockCasing;
import gregicadditions.item.GAMultiblockCasing2;
import gregicadditions.item.components.FieldGenCasing;
import gregicadditions.machines.multi.simple.LargeSimpleRecipeMapMultiblockController;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.*;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.api.multiblock.PatternMatchContext;
import gregtech.api.render.ICubeRenderer;
import gregtech.common.blocks.BlockMultiblockCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.items.MetaItems;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import tj.gui.widgets.impl.ClickPopUpWidget;
import tj.gui.widgets.impl.ScrollableDisplayWidget;
import tj.gui.widgets.impl.TJToggleButtonWidget;
import tj.util.TJFluidUtils;
import tj.util.consumers.QuadConsumer;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import static gregtech.api.unification.material.Materials.EnderPearl;
import static net.minecraft.util.text.TextFormatting.GRAY;
import static net.minecraft.util.text.TextFormatting.YELLOW;
import static tj.textures.TJTextures.FUSION_MK2;
import static tj.textures.TJTextures.TELEPORTER_OVERLAY;
import static gregicadditions.capabilities.GregicAdditionsCapabilities.MAINTENANCE_HATCH;
import static gregicadditions.machines.multi.mega.MegaMultiblockRecipeMapController.frameworkPredicate;
import static gregicadditions.machines.multi.mega.MegaMultiblockRecipeMapController.frameworkPredicate2;
import static gregtech.api.gui.GuiTextures.*;
import static gregtech.api.gui.widgets.AdvancedTextWidget.withButton;
import static gregtech.api.metatileentity.multiblock.MultiblockAbility.IMPORT_FLUIDS;
import static gregtech.api.metatileentity.multiblock.MultiblockAbility.INPUT_ENERGY;


public class MetaTileEntityTeleporter extends TJMultiblockDisplayBase implements IParallelController, LinkPos, IProgressBar {

    private static final MultiblockAbility<?>[] ALLOWED_ABILITIES = {IMPORT_FLUIDS, INPUT_ENERGY, MAINTENANCE_HATCH};
    private static final FluidStack ENDER_PEARL = EnderPearl.getFluid(1);
    private final TeleporterWorkableHandler workableHandler = new TeleporterWorkableHandler(this);
    private IEnergyContainer energyContainer;
    private IMultipleTankHandler inputFluidHandler;
    private int tier;

    public MetaTileEntityTeleporter(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
        this.workableHandler.setImportEnergySupplier(this::getEnergyContainer)
                .setImportFluidsSupplier(this::getInputFluidHandler)
                .setTierSupplier(this::getTier);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityTeleporter(this.metaTileEntityId);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(net.minecraft.client.resources.I18n.format("tj.multiblock.teleporter.description"));
    }

    @Override
    protected boolean checkStructureComponents(List<IMultiblockPart> parts, Map<MultiblockAbility<Object>, List<Object>> abilities) {
        return abilities.containsKey(INPUT_ENERGY) && super.checkStructureComponents(parts, abilities);
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
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        int framework = 0, framework2 = 0;
        if (context.get("framework") instanceof GAMultiblockCasing.CasingType) {
            framework = ((GAMultiblockCasing.CasingType) context.get("framework")).getTier();
        }
        if (context.get("framework2") instanceof GAMultiblockCasing2.CasingType) {
            framework2 = ((GAMultiblockCasing2.CasingType) context.get("framework2")).getTier();
        }
        int fieldGen = context.getOrDefault("FieldGen", FieldGenCasing.CasingType.FIELD_GENERATOR_LV).getTier();
        this.tier = Math.min(fieldGen, Math.max(framework, framework2));
        boolean energyHatchTierMatches = this.getAbilities(INPUT_ENERGY).stream()
                .allMatch(energyContainer -> GAUtility.getTierByVoltage(energyContainer.getInputVoltage()) <= this.tier);
        if (!energyHatchTierMatches) {
            this.invalidateStructure();
            return;
        }
        this.energyContainer = new EnergyContainerList(this.getAbilities(INPUT_ENERGY));
        this.inputFluidHandler = new FluidTankList(true, this.getAbilities(IMPORT_FLUIDS));
        this.workableHandler.initialize(0);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("~CFC~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~")
                .aisle("CHHHC", "~HHH~", "~C#C~", "~C#C~", "~C#C~", "~HHH~", "~~H~~")
                .aisle("FHfHF", "~HSH~", "~###~", "~###~", "~###~", "~HFH~", "~HfH~")
                .aisle("CHHHC", "~HHH~", "~C#C~", "~C#C~", "~C#C~", "~HHH~", "~~H~~")
                .aisle("~CFC~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~")
                .where('S', this.selfPredicate())
                .where('C', statePredicate(this.getCasingState()))
                .where('H', statePredicate(this.getCasingState()).or(abilityPartPredicate(ALLOWED_ABILITIES)))
                .where('F', frameworkPredicate().or(frameworkPredicate2()))
                .where('f', LargeSimpleRecipeMapMultiblockController.fieldGenPredicate())
                .where('#', isAirPredicate())
                .where('~', tile -> true)
                .build();
    }

    private IBlockState getCasingState() {
        return MetaBlocks.MUTLIBLOCK_CASING.getState(BlockMultiblockCasing.MultiblockCasingType.FUSION_CASING_MK2);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return FUSION_MK2;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        TELEPORTER_OVERLAY.render(renderState, translation, pipeline, getFrontFacing(), this.workableHandler.isActive());
    }

    @Override
    protected void addDisplayText(UIDisplayBuilder builder) {
        super.addDisplayText(builder);
        if (!this.isStructureFormed()) return;
        Pair<Integer, BlockPos> selectedPos = this.workableHandler.getPosMap().get(this.workableHandler.getSelectedPosName());
        World world;
        int worldID;
        BlockPos pos;
        long distance;
        long distanceEU;
        if (selectedPos != null) {
            worldID = selectedPos.getLeft();
            pos = selectedPos.getRight();
            world = DimensionManager.getWorld(worldID);
            boolean interdimensional = worldID != this.getWorld().provider.getDimension();
            int x = Math.abs(interdimensional ? pos.getX() : pos.getX() - this.getPos().getX());
            int y = Math.abs(interdimensional ? pos.getY() : pos.getY() - this.getPos().getY());
            int z = Math.abs(interdimensional ? pos.getZ() : pos.getZ() - this.getPos().getZ());
            distance = x + y + z;
            distanceEU = 1000000 + distance * 1000L;
        } else {
            world = null;
            worldID = Integer.MIN_VALUE;
            pos = null;
            distance = 0;
            distanceEU = 0;
        }
        builder.voltageInLine(this.energyContainer)
                .voltageTierLine(this.tier)
                .customLine(text -> {
                    if (selectedPos != null) {
                        text.addTextComponent(new TextComponentString(I18n.translateToLocalFormatted("tj.multiblock.teleporter.selected.world", world != null ? world.provider.getDimensionType().getName() : "Null", worldID)));
                        text.addTextComponent(new TextComponentString(I18n.translateToLocalFormatted("tj.multiblock.teleporter.selected.pos", pos.getX(), pos.getY(), pos.getZ())));
                        text.addTextComponent(new TextComponentString(I18n.translateToLocalFormatted("metaitem.linking.device.range", distance)));
                    }
                }).energyInputLine(this.energyContainer, distanceEU, this.workableHandler.getMaxProgress())
                .isWorkingLine(this.workableHandler.isWorkingEnabled(), this.workableHandler.isActive(), this.workableHandler.getProgress(), this.workableHandler.getMaxProgress());
    }

    @Override
    protected int getExtended() {
        return 18;
    }

    @Override
    protected void addTabs(WidgetTabBuilder tabBuilder, EntityPlayer player) {
        super.addTabs(tabBuilder, player);
        int[][] searchResults = new int[2][1];
        int[][] patternFlags = new int[2][9];
        String[][] search = {{""}, {""}};
        tabBuilder.addTab("tj.multiblock.tab.pos", new ItemStack(Items.COMPASS), blockPosTab -> this.addScrollWidgets(blockPosTab, this.addPosDisplayText(searchResults[0], patternFlags[0], search[0]), patternFlags[0], search[0]));
        tabBuilder.addTab("tj.multiblock.tab.queue", MetaItems.CONVEYOR_MODULE_ZPM.getStackForm(), queueTab -> this.addScrollWidgets(queueTab, this.addQueueDisplayText(searchResults[1], patternFlags[1], search[1]), patternFlags[1], search[1]));
    }

    @Override
    protected void mainDisplayTab(List<Widget> widgetGroup) {
        super.mainDisplayTab(widgetGroup);
        widgetGroup.add(new ImageWidget(28, 114, 141, 18, DISPLAY));
        widgetGroup.add(new NewTextFieldWidget<>(33, 119, 136, 18, () -> String.valueOf(this.workableHandler.getMaxProgress()), (maxProgress, id) -> this.workableHandler.setMaxProgress(maxProgress.isEmpty() ? 1 : Integer.parseInt(maxProgress)))
                .setTooltipFormat(() -> ArrayUtils.toArray(String.valueOf(this.workableHandler.getMaxProgress())))
                .setValidator(str -> Pattern.compile("\\*?[0-9_]*\\*?").matcher(str).matches())
                .setTooltipText("machine.universal.tick.speed"));
        widgetGroup.add(new ClickButtonWidget(7, 114, 18, 18, "+", (click) -> this.workableHandler.setMaxProgress(MathHelper.clamp(this.workableHandler.getMaxProgress() * 2, 1, Integer.MAX_VALUE))));
        widgetGroup.add(new ClickButtonWidget(175, 114, 18, 18, "-", (click) -> this.workableHandler.setMaxProgress(MathHelper.clamp(this.workableHandler.getMaxProgress() / 2, 1, Integer.MAX_VALUE))));
        widgetGroup.add(new ToggleButtonWidget(175, 151, 18, 18, TJGuiTextures.RESET_BUTTON, () -> false, this.workableHandler::setReset)
                .setTooltipText("machine.universal.toggle.reset"));
    }

    private void addScrollWidgets(List<Widget> tab, Consumer<UIDisplayBuilder> displayText2, int[] patternFlags, String[] search) {
        NewTextFieldWidget<?> textFieldWidgetRename = new NewTextFieldWidget<>(12, 20, 159, 13)
                .setValidator(str -> Pattern.compile(".*").matcher(str).matches())
                .setBackgroundText("machine.universal.toggle.rename.entry")
                .setTooltipText("machine.universal.toggle.rename.entry")
                .setTextResponder(this.workableHandler::renameLink)
                .setMaxStringLength(256);
        AdvancedDisplayWidget displayWidget = new AdvancedDisplayWidget(0, 2, displayText2, 0xFFFFFF)
                .addClickHandler(this.handlePosDisplayClick(textFieldWidgetRename));
        displayWidget.setMaxWidthLimit(1024);
        tab.add(new ClickPopUpWidget(0, 0, 0, 0)
                .addPopup(widgetGroup -> {
                    widgetGroup.addWidget(new ScrollableDisplayWidget(10, -15, 183, 142)
                            .addDisplayWidget(displayWidget)
                            .setScrollPanelWidth(3));
                    widgetGroup.addWidget(new ImageWidget(7, this.getOffsetY(114), 162, 18, DISPLAY));
                    widgetGroup.addWidget(new NewTextFieldWidget<>(12, this.getOffsetY(119), 157, 18)
                            .setValidator(str -> Pattern.compile(".*").matcher(str).matches())
                            .setBackgroundText("machine.universal.search")
                            .setTextResponder((s, id) -> search[0] = s)
                            .setTextSupplier(() -> search[0])
                            .setMaxStringLength(256)
                            .setUpdateOnTyping(true));
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
                .addPopup(0, 61, 182, 60, displayWidget, false, widgetGroup -> {
                    widgetGroup.addWidget(new ImageWidget(0, 0, 182, 60, BORDERED_BACKGROUND));
                    widgetGroup.addWidget(new ImageWidget(10, 15, 162, 18, DISPLAY));
                    widgetGroup.addWidget(new AdvancedTextWidget(45, 4, (textList) -> textList.add(new TextComponentTranslation("machine.universal.renaming", textFieldWidgetRename.getTextId())), 0x404040));
                    widgetGroup.addWidget(textFieldWidgetRename);
                    return false;
                }).addPopup(118, 31, 60, 78, new TJToggleButtonWidget(175, this.getOffsetY(114), 18, 18)
                        .setItemDisplay(new ItemStack(Item.getByNameOrId("enderio:item_material"), 1, 11))
                        .setTooltipText("machine.universal.search.settings")
                        .setToggleTexture(TOGGLE_BUTTON_BACK)
                        .useToggleTexture(true), widgetGroup-> this.addSearchTextWidgets(widgetGroup, patternFlags)));
    }

    private boolean addSearchTextWidgets(WidgetGroup widgetGroup, int[] patternFlags) {
        widgetGroup.addWidget(new ImageWidget(0, 0, 60, 78, BORDERED_BACKGROUND));
        widgetGroup.addWidget(new ImageWidget(3, 57, 54, 18, DISPLAY));
        widgetGroup.addWidget(new AdvancedTextWidget(5, 62, textList -> textList.add(new TextComponentTranslation("string.regex.flag", this.getFlags(patternFlags))), 0x404040));
        widgetGroup.addWidget(new TJToggleButtonWidget(3, 3, 18, 18)
                .setToggleButtonResponder((pressed, s) -> patternFlags[0] = pressed ? Pattern.UNIX_LINES : 0)
                .setDisplayText("string.regex.pattern.unix_lines.flag")
                .setTooltipText("string.regex.pattern.unix_lines")
                .setButtonSupplier(() -> patternFlags[0] != 0)
                .setToggleTexture(TOGGLE_BUTTON_BACK)
                .useToggleTexture(true));
        widgetGroup.addWidget(new TJToggleButtonWidget(21, 3, 18, 18)
                .setToggleButtonResponder((pressed, s) -> patternFlags[1] = pressed ? Pattern.CASE_INSENSITIVE : 0)
                .setDisplayText("string.regex.pattern.case_insensitive.flag")
                .setTooltipText("string.regex.pattern.case_insensitive")
                .setButtonSupplier(() -> patternFlags[1] != 0)
                .setToggleTexture(TOGGLE_BUTTON_BACK)
                .useToggleTexture(true));
        widgetGroup.addWidget(new TJToggleButtonWidget(39, 3, 18, 18)
                .setToggleButtonResponder((pressed, s) -> patternFlags[2] = pressed ? Pattern.COMMENTS : 0)
                .setDisplayText("string.regex.pattern.comments.flag")
                .setButtonSupplier(() -> patternFlags[2] != 0)
                .setTooltipText("string.regex.pattern.comments")
                .setToggleTexture(TOGGLE_BUTTON_BACK)
                .useToggleTexture(true));
        widgetGroup.addWidget(new TJToggleButtonWidget(3, 21, 18, 18)
                .setToggleButtonResponder((pressed, s) -> patternFlags[3] = pressed ? Pattern.MULTILINE : 0)
                .setDisplayText("string.regex.pattern.multiline.flag")
                .setTooltipText("string.regex.pattern.multiline")
                .setButtonSupplier(() -> patternFlags[3] != 0)
                .setToggleTexture(TOGGLE_BUTTON_BACK)
                .useToggleTexture(true));
        widgetGroup.addWidget(new TJToggleButtonWidget(21, 21, 18, 18)
                .setToggleButtonResponder((pressed, s) -> patternFlags[4] = pressed ? Pattern.LITERAL : 0)
                .setDisplayText("string.regex.pattern.literal.flag")
                .setButtonSupplier(() -> patternFlags[4] != 0)
                .setTooltipText("string.regex.pattern.literal")
                .setToggleTexture(TOGGLE_BUTTON_BACK)
                .useToggleTexture(true));
        widgetGroup.addWidget(new TJToggleButtonWidget(39, 21, 18, 18)
                .setToggleButtonResponder((pressed, s) -> patternFlags[5] = pressed ? Pattern.DOTALL : 0)
                .setDisplayText("string.regex.pattern.dotall.flag")
                .setButtonSupplier(() -> patternFlags[5] != 0)
                .setTooltipText("string.regex.pattern.dotall")
                .setToggleTexture(TOGGLE_BUTTON_BACK)
                .useToggleTexture(true));
        widgetGroup.addWidget(new TJToggleButtonWidget(3, 39, 18, 18)
                .setToggleButtonResponder((pressed, s) -> patternFlags[6] = pressed ? Pattern.UNICODE_CASE : 0)
                .setDisplayText("string.regex.pattern.unicode_case.flag")
                .setTooltipText("string.regex.pattern.unicode_case")
                .setButtonSupplier(() -> patternFlags[6] != 0)
                .setToggleTexture(TOGGLE_BUTTON_BACK)
                .useToggleTexture(true));
        widgetGroup.addWidget(new TJToggleButtonWidget(21, 39, 18, 18)
                .setToggleButtonResponder((pressed, s) -> patternFlags[7] = pressed ? Pattern.CANON_EQ : 0)
                .setDisplayText("string.regex.pattern.canon_eq.flag")
                .setButtonSupplier(() -> patternFlags[7] != 0)
                .setTooltipText("string.regex.pattern.canon_eq")
                .setToggleTexture(TOGGLE_BUTTON_BACK)
                .useToggleTexture(true));
        widgetGroup.addWidget(new TJToggleButtonWidget(39, 39, 18, 18)
                .setToggleButtonResponder((pressed, s) -> patternFlags[8] = pressed ? Pattern.UNICODE_CHARACTER_CLASS : 0)
                .setDisplayText("string.regex.pattern.unicode_character_class.flag")
                .setTooltipText("string.regex.pattern.unicode_character_class")
                .setButtonSupplier(() -> patternFlags[8] != 0)
                .setToggleTexture(TOGGLE_BUTTON_BACK)
                .useToggleTexture(true));
        return false;
    }

    private Consumer<UIDisplayBuilder> addPosDisplayText(int[] searchResults, int[] flags, String[] search) {
        return (builder) -> {
            builder.addTextComponent(new TextComponentString("§l" + I18n.translateToLocal("tj.multiblock.tab.pos") + "§r(§e" + searchResults[0] + "§r/§e" + this.workableHandler.getPosMap().size() + "§r)"));
            int results = 0;
            for (Map.Entry<String, Pair<Integer, BlockPos>> posEntry : this.workableHandler.getPosMap().entrySet()) {
                String key = posEntry.getKey();

                if (!search[0].isEmpty() && !Pattern.compile(search[0], this.getFlags(flags)).matcher(key).find())
                    continue;

                World world = DimensionManager.getWorld(posEntry.getValue().getLeft());
                String worldName = world != null ? world.provider.getDimensionType().getName() : "Null";
                int worldID = posEntry.getValue().getLeft();

                BlockPos pos = posEntry.getValue().getValue();

                String tp = "tp:" + pos.getX() + ":" + pos.getY() + ":" + pos.getZ() + ":" + worldID;
                String select = "select:" + key;
                String remove = "remove:" + key;
                String rename = "@Popup:" + key;
                ITextComponent keyPos = new TextComponentString(": [§a" + (++results) + "§r] " + key + "§r" + (key.equals(this.workableHandler.getSelectedPosName()) ? " §a<<<" : ""))
                        .appendText("\n")
                        .appendSibling(withButton(new TextComponentString("[TP]"), tp))
                        .appendText(" ")
                        .appendSibling(TJAdvancedTextWidget.withButton(new TextComponentTranslation("machine.universal.linked.select")
                                .setStyle(new Style().setColor(key.equals(this.workableHandler.getSelectedPosName()) ? GRAY : YELLOW)), select))
                        .appendText(" ")
                        .appendSibling(withButton(new TextComponentTranslation("machine.universal.linked.remove"), remove))
                        .appendText(" ")
                        .appendSibling(withButton(new TextComponentTranslation("machine.universal.linked.rename"), rename));

                ITextComponent blockPos = new TextComponentString(key + "\n")
                        .appendSibling(new TextComponentString(I18n.translateToLocalFormatted("machine.universal.linked.dimension", worldName, worldID)))
                        .appendText("\n")
                        .appendSibling(new TextComponentString(I18n.translateToLocalFormatted("machine.universal.linked.pos", pos.getX(), pos.getY(), pos.getZ())));

                keyPos.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, blockPos));
                builder.addTextComponent(keyPos);
            }
            searchResults[0] = results;
        };
    }

    private Consumer<UIDisplayBuilder> addQueueDisplayText(int[] searchResults, int[] flags, String[] search) {
        return (builder) -> {
            builder.addTextComponent(new TextComponentString("§l" + I18n.translateToLocal("tj.multiblock.tab.queue") + "§r(§e" + searchResults[0] + "§r/§e" + this.workableHandler.getQueueTeleport().size() + "§r)"));
            int results = 0;
            for (Triple<Entity, Integer, BlockPos> queueEntry : this.workableHandler.getQueueTeleport()) {
                String key = queueEntry.getLeft().getName();

                if (!search[0].isEmpty() && !Pattern.compile(search[0], this.getFlags(flags)).matcher(key).find())
                    continue;

                BlockPos pos = queueEntry.getRight();
                World world = DimensionManager.getWorld(queueEntry.getMiddle());
                String worldName = world != null ? world.provider.getDimensionType().getName() : "Null";
                int worldID = queueEntry.getMiddle();

                String position = I18n.translateToLocal("machine.universal.linked.pos") + " X: §e" + pos.getX() + "§r Y: §e" + pos.getY() + "§r Z: §e" + pos.getZ();

                ITextComponent keyPos = new TextComponentString(": [§a" + (++results) + "§r] " + key);

                ITextComponent blockPos = new TextComponentString(results + ": " + key + "\n")
                        .appendSibling(new TextComponentString(I18n.translateToLocalFormatted("machine.universal.linked.dimension", worldName, worldID)))
                        .appendText("\n")
                        .appendSibling(new TextComponentString(position));

                keyPos.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, blockPos));
                builder.addTextComponent(keyPos);
            }
            searchResults[0] = results;
        };
    }

    private int getFlags(int[] flags) {
        int flag = 0;
        for (int i : flags) {
            flag |= i;
        }
        return flag;
    }

    private QuadConsumer<String, String, Widget.ClickData, EntityPlayer> handlePosDisplayClick(NewTextFieldWidget<?> textFieldWidget) {
        return (componentData, textId, clickData, player) -> {
            String[] component = componentData.split(":");
            switch (component[0]) {
                case "tp":
                    int posX = Integer.parseInt(component[1]);
                    int posY = Integer.parseInt(component[2]);
                    int posZ = Integer.parseInt(component[3]);
                    int worldID = Integer.parseInt(component[4]);

                    if (DimensionManager.getWorld(worldID) == null) {
                        DimensionManager.initDimension(worldID);
                        DimensionManager.keepDimensionLoaded(worldID, true);
                    }

                    BlockPos blockPos = new BlockPos(posX, posY, posZ);
                    player.sendMessage(new TextComponentString(I18n.translateToLocalFormatted("tj.multiblock.teleporter.queue", player.getName())));
                    this.workableHandler.getQueueTeleport().add(new ImmutableTriple<>(player, worldID, blockPos));
                    break;
                case "select": this.workableHandler.setSelectedPosName(component[1]);
                    break;
                case "remove": this.workableHandler.getPosMap().remove(component[1]);
                    break;
                case "@Popup": textFieldWidget.setTextId(component[1]);
                    break;
            }
        };
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == TJCapabilities.CAPABILITY_PARALLEL_CONTROLLER)
            return TJCapabilities.CAPABILITY_PARALLEL_CONTROLLER.cast(this);
        if (capability == TJCapabilities.CAPABILITY_LINK_POS)
            return TJCapabilities.CAPABILITY_LINK_POS.cast(this);
        return super.getCapability(capability, side);
    }

    @Override
    public int[][] getBarMatrix() {
        return new int[1][1];
    }

    @Override
    public void getProgressBars(Queue<ProgressBar> bars, ProgressBar.ProgressBarBuilder barBuilder) {
        bars.add(barBuilder.setProgress(this::getEnderPearlAmount).setMaxProgress(this::getEnderPearlCapacity)
                .setLocale("tj.multiblock.bars.fluid").setParams(() -> new Object[]{ENDER_PEARL.getLocalizedName()})
                .setFluidStackSupplier(() -> ENDER_PEARL)
                .build());
    }

    private long getEnderPearlAmount() {
        return TJFluidUtils.getFluidAmountFromTanks(ENDER_PEARL, this.getInputFluidHandler());
    }

    private long getEnderPearlCapacity() {
        return TJFluidUtils.getFluidCapacityFromTanks(ENDER_PEARL, this.getInputFluidHandler());
    }

    @Override
    public long getEnergyStored() {
        return this.energyContainer.getEnergyStored();
    }

    @Override
    public long getEnergyCapacity() {
        return this.energyContainer.getEnergyCapacity();
    }

    @Override
    public long getMaxEUt() {
        return this.workableHandler.getEnergyPerTick();
    }

    @Override
    public long getTotalEnergyConsumption() {
        return this.workableHandler.getEnergyPerTick();
    }

    @Override
    public long getVoltageTier() {
        return GAValues.V[this.tier];
    }

    @Override
    public boolean isInterDimensional() {
        return true;
    }

    @Override
    public int dimensionID() {
        return this.getWorld().provider.getDimension();
    }

    @Override
    public int getRange() {
        return Integer.MAX_VALUE;
    }

    @Override
    public int getPosSize() {
        return 1;
    }

    @Override
    public void setPos(String name, BlockPos pos, EntityPlayer player, World world, int index) {
        name = this.checkDuplicateNames(name, 1);
        int worldID = world.provider.getDimension();
        this.workableHandler.getPosMap().put(name, new ImmutablePair<>(worldID, pos));
        this.workableHandler.getLinkData().setInteger("I", 1);
        this.markDirty();
    }

    private String checkDuplicateNames(String name, int count) {
        if (!this.workableHandler.getPosMap().containsKey(name))
            return name;
        if (count > 1) {
            String[] split = name.split(" ");
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < split.length - 1; i++) {
                builder.append(split[i]);
                if (i < split.length - 2)
                    builder.append(" ");
            }
            name = builder.toString();
        }
        name = name + " (" + count + ")";
        return this.checkDuplicateNames(name, ++count);
    }

    @Override
    public World world() {
        return this.getWorld();
    }

    @Override
    public NBTTagCompound getLinkData() {
        return this.workableHandler.getLinkData();
    }

    @Override
    public void setLinkData(NBTTagCompound linkData) {
        this.workableHandler.setLinkData(linkData);
    }

    @Override
    public void setWorkingEnabled(boolean isActivationAllowed) {
        this.workableHandler.setWorkingEnabled(isActivationAllowed);
    }

    @Override
    public boolean isWorkingEnabled() {
        return this.workableHandler.isWorkingEnabled();
    }

    private IEnergyContainer getEnergyContainer() {
        return this.energyContainer;
    }

    private IMultipleTankHandler getInputFluidHandler() {
        return this.inputFluidHandler;
    }

    public int getTier() {
        return this.tier;
    }
}
