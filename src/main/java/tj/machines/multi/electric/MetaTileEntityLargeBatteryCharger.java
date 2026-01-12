package tj.machines.multi.electric;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregicadditions.GAValues;
import gregicadditions.client.ClientHandler;
import gregicadditions.item.CellCasing;
import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.metal.MetalCasing1;
import gregtech.api.capability.*;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.*;
import gregtech.api.metatileentity.MTETrait;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.api.multiblock.PatternMatchContext;
import gregtech.api.render.ICubeRenderer;
import gregtech.api.render.Textures;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.apache.commons.lang3.ArrayUtils;
import tj.builder.WidgetTabBuilder;
import tj.builder.handlers.BatteryChargerWorkableHandler;
import tj.builder.multicontrollers.TJMultiblockDisplayBase;
import tj.builder.multicontrollers.UIDisplayBuilder;
import tj.capability.*;
import tj.gui.TJGuiTextures;
import tj.gui.widgets.AdvancedDisplayWidget;
import tj.gui.widgets.NewTextFieldWidget;
import tj.gui.widgets.impl.ClickPopUpWidget;
import tj.gui.widgets.impl.ScrollableDisplayWidget;
import tj.gui.widgets.impl.TJToggleButtonWidget;
import tj.items.TJMetaItems;
import tj.util.TJFluidUtils;
import tj.util.consumers.QuadConsumer;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

import static gregicadditions.GAMaterials.Talonite;
import static gregicadditions.capabilities.GregicAdditionsCapabilities.MAINTENANCE_HATCH;
import static gregicadditions.machines.multi.MetaTileEntityBatteryTower.cellPredicate;
import static gregtech.api.capability.GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM;
import static gregtech.api.gui.GuiTextures.*;
import static gregtech.api.gui.widgets.AdvancedTextWidget.withButton;
import static gregtech.api.metatileentity.multiblock.MultiblockAbility.*;
import static gregtech.api.unification.material.Materials.Nitrogen;
import static net.minecraftforge.energy.CapabilityEnergy.ENERGY;
import static tj.builder.handlers.BatteryChargerWorkableHandler.TransferMode.INPUT;
import static tj.builder.handlers.BatteryChargerWorkableHandler.TransferMode.OUTPUT;

public class MetaTileEntityLargeBatteryCharger extends TJMultiblockDisplayBase implements LinkEntity, LinkEvent, IParallelController, IProgressBar {

    private static final MultiblockAbility<?>[] ALLOWED_ABILITIES = {IMPORT_ITEMS, EXPORT_ITEMS, INPUT_ENERGY, OUTPUT_ENERGY, IMPORT_FLUIDS, MAINTENANCE_HATCH};
    private final BatteryChargerWorkableHandler workableHandler = new BatteryChargerWorkableHandler(this);
    private int tier;
    private final int pageSize = 4;
    private int pageIndex;
    private IItemHandlerModifiable importItemHandler;
    private IItemHandlerModifiable exportItemHandler;
    private IMultipleTankHandler importFluidHandler;
    private IEnergyContainer inputEnergyContainer;
    private IEnergyContainer outputEnergyContainer;

    public MetaTileEntityLargeBatteryCharger(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
        this.workableHandler.setImportItemsSupplier(this::getImportItemHandler)
                .setExportItemsSupplier(this::getExportItemHandler)
                .setImportFluidsSupplier(this::getImportFluidHandler)
                .setImportEnergySupplier(this::getInputEnergyContainer)
                .setExportEnergySupplier(this::getOutputEnergyContainer)
                .setTierSupplier(this::getTier)
                .setResetEnergy(false);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityLargeBatteryCharger(this.metaTileEntityId);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("tj.multiblock.large_battery_charger.description"));
    }

    @Override
    protected boolean checkStructureComponents(List<IMultiblockPart> parts, Map<MultiblockAbility<Object>, List<Object>> abilities) {
        return abilities.containsKey(INPUT_ENERGY) && abilities.containsKey(OUTPUT_ENERGY) && super.checkStructureComponents(parts, abilities);
    }

    @Override
    protected void addDisplayText(UIDisplayBuilder builder) {
        super.addDisplayText(builder);
        if (this.isStructureFormed())
            builder.voltageInLine(this.inputEnergyContainer)
                    .voltageTierLine(this.tier)
                    .energyStoredLine(this.getEnergyStored(), this.getEnergyCapacity())
                    .energyInputLine(this.inputEnergyContainer, this.workableHandler.getEnergyPerTick(), this.workableHandler.getMaxProgress())
                    .fluidInputLine(this.importFluidHandler, Nitrogen.getPlasma(this.workableHandler.getFluidConsumption()), this.workableHandler.getMaxProgress())
                    .customLine(text -> {
                        text.addTextComponent(new TextComponentTranslation("machine.universal.item.output.transfer")
                                .appendText(" ")
                                .appendSibling(this.workableHandler.isTransferToOutput() ? withButton(new TextComponentTranslation("machine.universal.toggle.run.mode.enabled"), "transferEnabled")
                                        : withButton(new TextComponentTranslation("machine.universal.toggle.run.mode.disabled"), "transferDisabled")));
                        text.addTextComponent(new TextComponentTranslation("machine.universal.mode.transfer")
                                .appendText(" ")
                                .appendSibling(this.workableHandler.getTransferMode() == INPUT ? withButton(new TextComponentTranslation("machine.universal.mode.transfer.input"), "input")
                                        : withButton(new TextComponentTranslation("machine.universal.mode.transfer.output"), "output")));
                    }).isWorkingLine(this.workableHandler.isWorkingEnabled(), this.workableHandler.isActive(), this.workableHandler.getProgress(), this.workableHandler.getMaxProgress());
    }

    @Override
    protected int getExtended() {
        return 18;
    }

    @Override
    protected void addTabs(WidgetTabBuilder tabBuilder, EntityPlayer player) {
        super.addTabs(tabBuilder, player);
        int[] searchResults = new int[1];
        int[] patternFlags = new int[9];
        String[] search = {""};
        tabBuilder.addTab("tj.multiblock.tab.linked_entities_display", TJMetaItems.LINKING_DEVICE.getStackForm(), tab -> {
            NewTextFieldWidget<?> textFieldWidgetRename = new NewTextFieldWidget<>(12, 20, 159, 13)
                    .setValidator(str -> Pattern.compile(".*").matcher(str).matches())
                    .setBackgroundText("machine.universal.toggle.rename.entry")
                    .setTooltipText("machine.universal.toggle.rename.entry")
                    .setTextResponder(this.workableHandler::renameLink)
                    .setMaxStringLength(256);
            AdvancedDisplayWidget displayWidget = new AdvancedDisplayWidget(0, 2, this.addDisplayLinkedPlayersText(searchResults, patternFlags, search), 0xFFFFFF)
                    .addClickHandler(this.handleLinkedPlayersClick(textFieldWidgetRename));
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
                        widgetGroup.addWidget(new AdvancedTextWidget(45, 4, (textList) -> {
                            int index = textFieldWidgetRename.getTextId().lastIndexOf(";");
                            String entry = textFieldWidgetRename.getTextId().substring(0, index);
                            textList.add(new TextComponentTranslation("machine.universal.renaming", entry));
                        }, 0x404040));
                        widgetGroup.addWidget(textFieldWidgetRename);
                        return false;
                    }).addPopup(118, 31, 60, 78, new TJToggleButtonWidget(172, this.getOffsetY(114), 18, 18)
                            .setItemDisplay(new ItemStack(Item.getByNameOrId("enderio:item_material"), 1, 11))
                            .setTooltipText("machine.universal.search.settings")
                            .setToggleTexture(TOGGLE_BUTTON_BACK)
                            .useToggleTexture(true), widgetGroup -> this.addSearchTextWidgets(widgetGroup, patternFlags)));
        });
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

    private Consumer<UIDisplayBuilder> addDisplayLinkedPlayersText(int[] searchResults, int[] flags, String[] search) {
        return (builder) -> {
            builder.addTextComponent(new TextComponentString("§l" + net.minecraft.util.text.translation.I18n.translateToLocal("machine.universal.linked.players") + "§r(§e" + searchResults[0] + "§r/§e" + this.workableHandler.getEntityLinkName().length + "§r)"));
            int results = 0;
            for (int i = 0; i < this.workableHandler.getEntityLinkName().length; i++) {
                String name = this.workableHandler.getEntityLinkName()[i] != null ? this.workableHandler.getEntityLinkName()[i] : net.minecraft.util.text.translation.I18n.translateToLocal("machine.universal.empty");

                if (!search[0].isEmpty() && !Pattern.compile(search[0], this.getFlags(flags)).matcher(name).find())
                    continue;

                EntityPlayer player = this.workableHandler.getLinkedPlayers()[i];
                String dimensionName = player != null ? player.world.provider.getDimensionType().getName() : "";
                int dimensionID = player != null ? this.getDimension(i) : 0;
                int x = player != null ? (int) player.posX : Integer.MIN_VALUE;
                int y = player != null ? (int) player.posY : Integer.MIN_VALUE;
                int z = player != null ? (int) player.posZ : Integer.MIN_VALUE;
                long totalEnergyStored = 0;
                long totalEnergyCapacity = 0;

                if (player != null) {
                    for (int j = 0; j < player.inventory.armorInventory.size(); j++) {
                        ItemStack stack = player.inventory.armorInventory.get(j);
                        if (stack.isEmpty())
                            continue;

                        IEnergyStorage RFContainer = stack.getCapability(ENERGY, null);
                        IElectricItem EUContainer = stack.getCapability(CAPABILITY_ELECTRIC_ITEM, null);
                        if (RFContainer != null) {
                            totalEnergyStored += RFContainer.getEnergyStored() / 4;
                            totalEnergyCapacity += RFContainer.getMaxEnergyStored() / 4;
                        }
                        if (EUContainer != null) {
                            totalEnergyStored += EUContainer.getCharge();
                            totalEnergyCapacity += EUContainer.getMaxCharge();
                        }
                    }

                    for (int j = 0; j < player.inventory.mainInventory.size(); j++) {
                        ItemStack stack = player.inventory.mainInventory.get(j);
                        if (stack.isEmpty())
                            continue;

                        IEnergyStorage RFContainer = stack.getCapability(ENERGY, null);
                        IElectricItem EUContainer = stack.getCapability(CAPABILITY_ELECTRIC_ITEM, null);
                        if (RFContainer != null) {
                            totalEnergyStored += RFContainer.getEnergyStored() / 4;
                            totalEnergyCapacity += RFContainer.getMaxEnergyStored() / 4;
                        }
                        if (EUContainer != null) {
                            totalEnergyStored += EUContainer.getCharge();
                            totalEnergyCapacity += EUContainer.getMaxCharge();
                        }
                    }

                    for (int j = 0; j < player.inventory.offHandInventory.size(); j++) {
                        ItemStack stack = player.inventory.offHandInventory.get(j);
                        if (stack.isEmpty())
                            continue;

                        IEnergyStorage RFContainer = stack.getCapability(ENERGY, null);
                        IElectricItem EUContainer = stack.getCapability(CAPABILITY_ELECTRIC_ITEM, null);
                        if (RFContainer != null) {
                            totalEnergyStored += RFContainer.getEnergyStored() / 4;
                            totalEnergyCapacity += RFContainer.getMaxEnergyStored() / 4;
                        }
                        if (EUContainer != null) {
                            totalEnergyStored += EUContainer.getCharge();
                            totalEnergyCapacity += EUContainer.getMaxCharge();
                        }
                    }
                }
                builder.addTextComponent(new TextComponentString(": [§a" + (++searchResults[0]) + "§r] ")
                        .appendSibling(new TextComponentString(name))
                        .setStyle(new Style()
                                .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString(name)
                                        .appendText("\n")
                                        .appendSibling(new TextComponentString(net.minecraft.util.text.translation.I18n.translateToLocalFormatted("machine.universal.energy.stored", totalEnergyStored, totalEnergyCapacity)))
                                        .appendText("\n")
                                        .appendSibling(new TextComponentString(net.minecraft.util.text.translation.I18n.translateToLocalFormatted("machine.universal.linked.dimension", dimensionName, dimensionID)))
                                        .appendText("\n")
                                        .appendSibling(new TextComponentString(net.minecraft.util.text.translation.I18n.translateToLocalFormatted("machine.universal.linked.pos", x, y, z))))))
                        .appendText("\n")
                        .appendSibling(withButton(new TextComponentTranslation("machine.universal.linked.remove"), "remove:" + i))
                        .appendText(" ")
                        .appendSibling(withButton(new TextComponentTranslation("machine.universal.linked.rename"), "@Popup:" + name + ";" + i)));
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

    private QuadConsumer<String, String, Widget.ClickData, EntityPlayer> handleLinkedPlayersClick(NewTextFieldWidget<?> textFieldWidget) {
        return (componentData, textId, clickData, player) -> {
            String[] components = componentData.split(":");
            switch (components[0]) {
                case "leftPage":
                    if (this.pageIndex > 0)
                        this.pageIndex -= this.pageSize;
                    break;
                case "rightPage":
                    if (this.pageIndex < this.workableHandler.getLinkedPlayers().length - this.pageSize)
                        this.pageIndex += this.pageSize;
                case "remove":
                    int i = Integer.parseInt(components[1]);
                    int index = this.workableHandler.getLinkData().getInteger("I");
                    this.workableHandler.getLinkData().setInteger("I", index + 1);
                    this.workableHandler.getEntityLinkName()[i] = null;
                    this.workableHandler.getLinkedPlayers()[i] = null;
                    this.workableHandler.getLinkedPlayersID()[i] = null;
                    this.workableHandler.getEntityLinkWorld()[i] = Integer.MIN_VALUE;
                    this.workableHandler.updateTotalEnergyPerTick();
                    break;
                case "@Popup": textFieldWidget.setTextId(components[1]);
                    break;
            }
        };
    }

    @Override
    protected void handleDisplayClick(String componentData, Widget.ClickData clickData) {
        switch (componentData) {
            case "transferEnabled":
                this.workableHandler.setTransferToOutput(false);
                break;
            case "transferDisabled":
                this.workableHandler.setTransferToOutput(true);
                break;
            case "input":
                this.workableHandler.setTransferMode(OUTPUT);
                break;
            default:
                this.workableHandler.setTransferMode(INPUT);
        }
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
        this.importItemHandler = new ItemHandlerList(this.getAbilities(IMPORT_ITEMS));
        this.exportItemHandler = new ItemHandlerList(this.getAbilities(EXPORT_ITEMS));
        this.importFluidHandler = new FluidTankList(true, this.getAbilities(IMPORT_FLUIDS));
        this.inputEnergyContainer = new EnergyContainerList(this.getAbilities(INPUT_ENERGY));
        this.outputEnergyContainer = new EnergyContainerList(this.getAbilities(OUTPUT_ENERGY));
        this.tier = context.getOrDefault("CellType", CellCasing.CellType.CELL_EV).getTier();
        this.workableHandler.initialize(0);
    }

    private boolean hasEnoughFluid(int amount) {
        FluidStack fluidStack = this.importFluidHandler.drain(Nitrogen.getPlasma(amount), false);
        return fluidStack != null && fluidStack.amount == amount;
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("HHHHH", "~HHH~", "~C~C~", "~C~C~", "~C~C~", "~C~C~", "~C~C~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~")
                .aisle("HHHHH" ,"HHHHH", "CFBFC", "CFBFC", "CFBFC", "CFBFC", "CFBFC", "~CCC~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~")
                .aisle("HHHHH", "HHHHH", "~BFB~", "~BFB~", "~BFB~", "~BFB~", "~BFB~", "~CFC~", "~~F~~", "~~F~~", "~~F~~", "~~F~~", "~~F~~")
                .aisle("HHHHH", "HHHHH", "CFBFC", "CFBFC", "CFBFC", "CFBFC", "CFBFC", "~CCC~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~")
                .aisle("HHHHH", "~HSH~", "~C~C~", "~C~C~", "~C~C~", "~C~C~", "~C~C~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~")
                .where('S', this.selfPredicate())
                .where('C', statePredicate(this.getCasingState()))
                .where('H', statePredicate(this.getCasingState()).or(abilityPartPredicate(ALLOWED_ABILITIES)))
                .where('B', cellPredicate())
                .where('F', statePredicate(MetaBlocks.FRAMES.get(Talonite).getDefaultState()))
                .where('~', tile -> true)
                .build();
    }

    private IBlockState getCasingState() {
        return GAMetaBlocks.METAL_CASING_1.getState(MetalCasing1.CasingType.TALONITE);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return ClientHandler.TALONITE_CASING;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        Textures.MULTIBLOCK_WORKABLE_OVERLAY.render(renderState, translation, pipeline, this.getFrontFacing(), this.workableHandler.isActive());
    }

    @Override
    public long getEnergyStored() {
        return this.inputEnergyContainer != null && this.workableHandler.getTransferMode() == INPUT ? this.inputEnergyContainer.getEnergyStored()
                : this.outputEnergyContainer != null && this.workableHandler.getTransferMode() == OUTPUT ? this.outputEnergyContainer.getEnergyStored()
                : 0;
    }

    @Override
    public long getEnergyCapacity() {
        return this.inputEnergyContainer != null && this.workableHandler.getTransferMode() == INPUT ? this.inputEnergyContainer.getEnergyCapacity()
                : this.outputEnergyContainer != null && this.workableHandler.getTransferMode() == OUTPUT ? this.outputEnergyContainer.getEnergyCapacity()
                : 0;
    }

    @Override
    public long getMaxEUt() {
        return this.inputEnergyContainer != null && this.workableHandler.getTransferMode() == INPUT ? this.inputEnergyContainer.getInputVoltage()
                : this.outputEnergyContainer != null && this.workableHandler.getTransferMode() == OUTPUT ? this.outputEnergyContainer.getInputVoltage()
                : 0;
    }

    @Override
    public int getEUBonus() {
        return -1;
    }

    @Override
    public long getTotalEnergyConsumption() {
        return this.workableHandler.getTotalEnergyPerTick();
    }

    @Override
    public long getVoltageTier() {
        return GAValues.V[this.tier];
    }

    @Override
    public int dimensionID() {
        return this.getWorld().provider.getDimension();
    }

    @Override
    public boolean isInterDimensional() {
        return true;
    }

    @Override
    public int getDimension(int index) {
        return this.workableHandler.getLinkedPlayers()[index].world.provider.getDimension();
    }

    @Override
    public int getRange() {
        return Integer.MAX_VALUE;
    }

    @Override
    public int getPosSize() {
        return this.workableHandler.getLinkedPlayers().length;
    }

    @Override
    public Entity getEntity(int index) {
        return this.workableHandler.getLinkedPlayers()[index];
    }

    @Override
    public void setPos(String name, BlockPos pos, EntityPlayer player, World world, int index) {
        name = this.checkDuplicateNames(name, 1);
        this.workableHandler.getEntityLinkName()[index] = name;
        this.workableHandler.getEntityLinkWorld()[index] = world.provider.getDimension();
        this.workableHandler.getLinkedPlayers()[index] = player;
        this.workableHandler.getLinkedPlayersID()[index] = player.getUniqueID();
        this.workableHandler.updateTotalEnergyPerTick();
    }

    private String checkDuplicateNames(String name, int count) {
        if (!Arrays.asList(this.workableHandler.getEntityLinkName()).contains(name))
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
    public int getPageIndex() {
        return this.pageIndex;
    }

    @Override
    public int getPageSize() {
        return this.pageSize;
    }

    @Override
    public void onLink(MetaTileEntity tileEntity) {
        this.workableHandler.updateTotalEnergyPerTick();
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == TJCapabilities.CAPABILITY_LINK_ENTITY)
            return TJCapabilities.CAPABILITY_LINK_ENTITY.cast(this);
        if (capability == TJCapabilities.CAPABILITY_PARALLEL_CONTROLLER)
            return TJCapabilities.CAPABILITY_PARALLEL_CONTROLLER.cast(this);
        return super.getCapability(capability, side);
    }

    @Override
    public int[][] getBarMatrix() {
        return new int[2][1];
    }

    @Override
    public void getProgressBars(Queue<UnaryOperator<ProgressBar.ProgressBarBuilder>> bars) {
        bars.add(bar -> bar.setProgress(this::getEnergyStored).setMaxProgress(this::getEnergyCapacity)
                .setLocale("tj.multiblock.bars.energy")
                .setColor(0xFFF6FF00));
        bars.add(bar -> bar.setProgress(this::getNitrogenAmount).setMaxProgress(this::getNitrogenCapacity)
                .setLocale("tj.multiblock.bars.fluid").setParams(() -> new Object[]{MetaTileEntityLargeWirelessEnergyEmitter.NITROGEN_PLASMA.getLocalizedName()})
                .setFluidStackSupplier(() -> MetaTileEntityLargeWirelessEnergyEmitter.NITROGEN_PLASMA));
    }

    private long getNitrogenAmount() {
        return TJFluidUtils.getFluidAmountFromTanks(MetaTileEntityLargeWirelessEnergyEmitter.NITROGEN_PLASMA, this.getImportFluidHandler());
    }

    private long getNitrogenCapacity() {
        return TJFluidUtils.getFluidCapacityFromTanks(MetaTileEntityLargeWirelessEnergyEmitter.NITROGEN_PLASMA, this.getImportFluidHandler());
    }

    @Override
    public void setWorkingEnabled(boolean isActivationAllowed) {
        this.workableHandler.setWorkingEnabled(isActivationAllowed);
    }

    @Override
    public boolean isWorkingEnabled() {
        return this.workableHandler.isWorkingEnabled();
    }

    private IItemHandlerModifiable getImportItemHandler() {
        return this.importItemHandler;
    }

    private IItemHandlerModifiable getExportItemHandler() {
        return this.exportItemHandler;
    }

    private IMultipleTankHandler getImportFluidHandler() {
        return this.importFluidHandler;
    }

    private IEnergyContainer getInputEnergyContainer() {
        return this.inputEnergyContainer;
    }

    private IEnergyContainer getOutputEnergyContainer() {
        return this.outputEnergyContainer;
    }

    public int getTier() {
        return this.tier;
    }
}
