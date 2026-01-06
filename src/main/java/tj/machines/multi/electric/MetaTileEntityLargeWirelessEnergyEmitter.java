package tj.machines.multi.electric;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.metatileentity.MTETrait;
import net.minecraft.item.Item;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tj.TJValues;
import tj.builder.WidgetTabBuilder;
import tj.builder.handlers.LargeWirelessEnergyWorkableHandler;
import tj.builder.multicontrollers.MultiblockDisplayBuilder;
import tj.builder.multicontrollers.TJMultiblockDisplayBase;
import tj.capability.IParallelController;
import tj.capability.LinkEvent;
import tj.capability.LinkPos;
import tj.capability.TJCapabilities;
import tj.gui.TJGuiTextures;
import tj.gui.widgets.NewTextFieldWidget;
import tj.gui.widgets.TJAdvancedTextWidget;
import tj.gui.widgets.TJTextFieldWidget;
import tj.gui.widgets.impl.ClickPopUpWidget;
import tj.gui.widgets.impl.ScrollableTextWidget;
import tj.gui.widgets.impl.TJToggleButtonWidget;
import tj.items.TJMetaItems;
import gregicadditions.GAValues;
import gregicadditions.client.ClientHandler;
import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.GAMultiblockCasing;
import gregicadditions.item.GAMultiblockCasing2;
import gregicadditions.item.metal.MetalCasing1;
import gregicadditions.item.metal.MetalCasing2;
import gregtech.api.block.machines.BlockMachine;
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
import gregtech.api.render.Textures;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.ArrayUtils;
import tj.util.consumers.QuadConsumer;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import static gregicadditions.GAMaterials.Talonite;
import static gregicadditions.capabilities.GregicAdditionsCapabilities.MAINTENANCE_HATCH;
import static gregicadditions.machines.multi.mega.MegaMultiblockRecipeMapController.frameworkPredicate;
import static gregicadditions.machines.multi.mega.MegaMultiblockRecipeMapController.frameworkPredicate2;
import static gregtech.api.capability.GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER;
import static gregtech.api.gui.GuiTextures.*;
import static gregtech.api.gui.widgets.AdvancedTextWidget.withButton;
import static gregtech.api.metatileentity.multiblock.MultiblockAbility.*;
import static gregtech.api.unification.material.Materials.Nitrogen;
import static gregtech.api.unification.material.Materials.RedSteel;
import static net.minecraftforge.energy.CapabilityEnergy.ENERGY;

public class MetaTileEntityLargeWirelessEnergyEmitter extends TJMultiblockDisplayBase implements LinkPos, LinkEvent, IParallelController {

    private static final MultiblockAbility<?>[] ALLOWED_ABILITIES = {IMPORT_FLUIDS, INPUT_ENERGY, OUTPUT_ENERGY, MAINTENANCE_HATCH};
    protected final LargeWirelessEnergyWorkableHandler workableHandler = new LargeWirelessEnergyWorkableHandler(this);
    private final int pageSize = 4;

    protected TransferType transferType;
    private IMultipleTankHandler importFluidHandler;
    private IEnergyContainer inputEnergyContainer;
    private int tier;
    private int pageIndex;

    public MetaTileEntityLargeWirelessEnergyEmitter(ResourceLocation metaTileEntityId, TransferType transferType) {
        super(metaTileEntityId);
        this.transferType = transferType;
        this.workableHandler.setImportFluidsSupplier(this::getImportFluidHandler)
                .setImportEnergySupplier(this::getInputEnergyContainer)
                .setTierSupplier(this::getTier)
                .setResetEnergy(false);
        this.reinitializeStructurePattern();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityLargeWirelessEnergyEmitter(this.metaTileEntityId, this.transferType);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("tj.multiblock.large_wireless_energy_emitter.description"));
    }

    @Override
    protected boolean checkStructureComponents(List<IMultiblockPart> parts, Map<MultiblockAbility<Object>, List<Object>> abilities) {
        return (this.transferType != TransferType.INPUT || abilities.containsKey(INPUT_ENERGY)) && super.checkStructureComponents(parts, abilities);
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        super.addDisplayText(textList);
        if (isStructureFormed())
            MultiblockDisplayBuilder.start(textList)
                    .voltageIn(this.inputEnergyContainer)
                    .voltageTier(this.tier)
                    .energyStored(this.getEnergyStored(), this.getEnergyCapacity())
                    .energyInput(hasEnoughEnergy(this.workableHandler.getEnergyPerTick()), this.workableHandler.getEnergyPerTick(), this.workableHandler.getMaxProgress())
                    .fluidInput(hasEnoughFluid(this.workableHandler.getFluidConsumption()), Nitrogen.getPlasma(this.workableHandler.getFluidConsumption()), this.workableHandler.getMaxProgress())
                    .isWorking(this.workableHandler.isWorkingEnabled(), this.workableHandler.isActive(), this.workableHandler.getProgress(), this.workableHandler.getMaxProgress());

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
            TJAdvancedTextWidget textWidget = new TJAdvancedTextWidget(0, 0, this.addDisplayLinkedEntitiesText(searchResults, patternFlags, search), 0xFFFFFF)
                    .addClickHandler(this.handleLinkedDisplayClick(textFieldWidgetRename));
            textWidget.setMaxWidthLimit(1024);
            tab.add(new ClickPopUpWidget(0, 0, 0, 0)
                    .addPopup(widgetGroup -> {
                        widgetGroup.addWidget(new AdvancedTextWidget(10, -20, (textList) -> textList.add(new TextComponentString("§l" + net.minecraft.util.text.translation.I18n.translateToLocal("tj.multiblock.large_world_accelerator.linked") + "§r(§e" + searchResults[0] + "§r/§e" + this.workableHandler.getEntityLinkName().length + "§r)")), 0xFFFFFF));
                        widgetGroup.addWidget(new ScrollableTextWidget(10, -8, 178, 117)
                                .addTextWidget(textWidget));
                        widgetGroup.addWidget(new ImageWidget(7, 112, 162, 18, DISPLAY));
                        widgetGroup.addWidget(new NewTextFieldWidget<>(12, 117, 157, 18)
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
                    .addPopup(0, 61, 182, 60, textWidget, false, widgetGroup -> {
                        widgetGroup.addWidget(new ImageWidget(0, 0, 182, 60, BORDERED_BACKGROUND));
                        widgetGroup.addWidget(new ImageWidget(10, 15, 162, 18, DISPLAY));
                        widgetGroup.addWidget(new AdvancedTextWidget(45, 4, (textList) -> {
                            int index = textFieldWidgetRename.getTextId().lastIndexOf(";");
                            String entry = textFieldWidgetRename.getTextId().substring(0, index);
                            textList.add(new TextComponentTranslation("machine.universal.renaming", entry));
                        }, 0x404040));
                        widgetGroup.addWidget(textFieldWidgetRename);
                        return false;
                    }).addPopup(118, 31, 60, 78, new TJToggleButtonWidget(172, 112, 18, 18)
                            .setItemDisplay(new ItemStack(Item.getByNameOrId("enderio:item_material"), 1, 11))
                            .setTooltipText("machine.universal.search.settings")
                            .setToggleTexture(TOGGLE_BUTTON_BACK)
                            .useToggleTexture(true), widgetGroup -> this.addSearchTextWidgets(widgetGroup, patternFlags)));
        });
    }

    @Override
    protected void mainDisplayTab(List<Widget> widgetGroup) {
        super.mainDisplayTab(widgetGroup);
        widgetGroup.add(new ImageWidget(28, 112, 141, 18, DISPLAY));
        widgetGroup.add(new TJTextFieldWidget(33, 117, 136, 18, false, () -> String.valueOf(this.workableHandler.getMaxProgress()), maxProgress -> this.workableHandler.setMaxProgress(maxProgress.isEmpty() ? 1 : Integer.parseInt(maxProgress)))
                .setTooltipText("machine.universal.tick.speed")
                .setTooltipFormat(() -> ArrayUtils.toArray(String.valueOf(this.workableHandler.getMaxProgress())))
                .setValidator(str -> Pattern.compile("\\*?[0-9_]*\\*?").matcher(str).matches()));
        widgetGroup.add(new ClickButtonWidget(7, 112, 18, 18, "+", click -> this.workableHandler.setMaxProgress(MathHelper.clamp(this.workableHandler.getMaxProgress() * 2, 1, Integer.MAX_VALUE))));
        widgetGroup.add(new ClickButtonWidget(172, 112, 18, 18, "-", click -> this.workableHandler.setMaxProgress(MathHelper.clamp(this.workableHandler.getMaxProgress() / 2, 1, Integer.MAX_VALUE))));
        widgetGroup.add(new ToggleButtonWidget(172, 151, 18, 18, TJGuiTextures.RESET_BUTTON, () -> false, this.workableHandler::setReset)
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

    private int getFlags(int[] flags) {
        int flag = 0;
        for (int i : flags) {
            flag |= i;
        }
        return flag;
    }

    private Consumer<List<ITextComponent>> addDisplayLinkedEntitiesText(int[] searchResults, int[] flags, String[] search) {
        return (textList) -> {
            int results = 0;
            for (int i = 0; i < this.workableHandler.getEntityLinkName().length; i++) {
                String name = this.workableHandler.getEntityLinkName()[i] != null ? this.workableHandler.getEntityLinkName()[i] : net.minecraft.util.text.translation.I18n.translateToLocal("machine.universal.empty");

                if (!search[0].isEmpty() && !Pattern.compile(search[0], this.getFlags(flags)).matcher(name).find())
                    continue;

                BlockPos pos = this.workableHandler.getEntityLinkBlockPos()[i] != null ? this.workableHandler.getEntityLinkBlockPos()[i] : TJValues.DUMMY_POS;
                WorldServer world = DimensionManager.getWorld(this.workableHandler.getEntityLinkWorld()[i]);
                TileEntity getTileEntity = world != null ? world.getTileEntity(pos) : null;
                MetaTileEntity getMetaTileEntity = world != null ? BlockMachine.getMetaTileEntity(world, pos) : null;
                boolean isTileEntity = getTileEntity != null;
                boolean isMetaTileEntity = getMetaTileEntity != null;
                IEnergyStorage RFContainer = isTileEntity ? getTileEntity.getCapability(ENERGY, null) : null;
                long RFStored = RFContainer != null ? RFContainer.getEnergyStored() : 0;
                long RFCapacity = RFContainer != null ? RFContainer.getMaxEnergyStored() : 0;
                IEnergyContainer EUContainer = isMetaTileEntity ? getMetaTileEntity.getCapability(CAPABILITY_ENERGY_CONTAINER, null) : null;
                long EUStored = EUContainer != null ? EUContainer.getEnergyStored() : 0;
                long EUCapacity = EUContainer != null ? EUContainer.getEnergyCapacity() : 0;

                textList.add(new TextComponentString(": [§a" + (++searchResults[0]) + "§r] ")
                        .appendSibling(new TextComponentString(name)).setStyle(new Style()
                                .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString(name)
                                        .appendText("\n")
                                        .appendSibling(new TextComponentString(net.minecraft.util.text.translation.I18n.translateToLocalFormatted("machine.universal.energy.stored", isMetaTileEntity ? EUStored : RFStored, isMetaTileEntity ? EUCapacity : RFCapacity)))
                                        .appendText("\n")
                                        .appendSibling(new TextComponentString(net.minecraft.util.text.translation.I18n.translateToLocalFormatted("machine.universal.linked.dimension", world != null ? world.provider.getDimensionType().getName() : "N/A", world != null ? world.provider.getDimension() : 0)))
                                        .appendText("\n")
                                        .appendSibling(new TextComponentString(net.minecraft.util.text.translation.I18n.translateToLocalFormatted("machine.universal.linked.pos", pos.getX(), pos.getY(), pos.getZ()))))))
                        .appendText("\n")
                        .appendSibling(new TextComponentTranslation("machine.universal.energy.amps", this.workableHandler.getEntityEnergyAmps()[i])
                                .appendText(" ")
                                .appendSibling(withButton(new TextComponentString("[+]"), "increment:" + i))
                                .appendText(" ")
                                .appendSibling(withButton(new TextComponentString("[-]"), "decrement:" + i)))
                        .appendText(" ")
                        .appendSibling(withButton(new TextComponentTranslation("machine.universal.linked.remove"), "remove:" + i))
                        .appendText(" ")
                        .appendSibling(withButton(new TextComponentTranslation("machine.universal.linked.rename"), "@Popup:" + name + ";" + i)));

            }
            searchResults[0] = results;
        };
    }

    private QuadConsumer<String, String, Widget.ClickData, EntityPlayer> handleLinkedDisplayClick(NewTextFieldWidget<?> textFieldWidget) {
        return (componentData, textId, clickData, player) -> {
            String[] component = componentData.split(":");
            switch (component[0]) {
                case "leftPage":
                    if (this.pageIndex > 0)
                        this.pageIndex -= this.pageSize;
                    break;
                case "rightPage":
                    if (this.pageIndex < this.workableHandler.getEntityLinkBlockPos().length - this.pageSize)
                        this.pageIndex += this.pageSize;
                    break;
                case "increment":
                    int i = Integer.parseInt(component[1]);
                    this.workableHandler.getEntityEnergyAmps()[i] = MathHelper.clamp(this.workableHandler.getEntityEnergyAmps()[i] + 1, 0, 256);
                    this.workableHandler.updateTotalEnergyPerTick();
                    break;
                case "decrement":
                    int i1 = Integer.parseInt(component[1]);
                    this.workableHandler.getEntityEnergyAmps()[i1] = MathHelper.clamp(this.workableHandler.getEntityEnergyAmps()[i1] - 1, 0, 256);
                    this.workableHandler.updateTotalEnergyPerTick();
                    break;
                case "remove":
                    int i2 = Integer.parseInt(component[1]);
                    int j = this.workableHandler.getLinkData().getInteger("I");
                    this.workableHandler.getLinkData().setInteger("I", j + 1);
                    this.workableHandler.getEntityLinkName()[i2] = null;
                    this.workableHandler.getEntityLinkBlockPos()[i2] = null;
                    this.workableHandler.getEntityLinkWorld()[i2] = Integer.MIN_VALUE;
                    this.workableHandler.getEntityEnergyAmps()[i2] = 0;
                    this.workableHandler.updateTotalEnergyPerTick();
                    break;
                case "@Popup":
                    textFieldWidget.setTextId(component[1]);
                    break;
            }
        };
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

    protected boolean hasEnoughEnergy(long amount) {
        return this.inputEnergyContainer.getEnergyStored() >= amount;
    }

    private boolean hasEnoughFluid(int amount) {
        FluidStack fluidStack = this.importFluidHandler.drain(Nitrogen.getPlasma(amount), false);
        return fluidStack != null && fluidStack.amount == amount;
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return this.transferType == null ? null : FactoryBlockPattern.start()
                .aisle("~HHH~", "~HHH~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~")
                .aisle("HHHHH", "HHFHH", "~CFC~", "~CFC~", "~CFC~", "~CFC~", "~CFC~", "~~F~~", "~~F~~", "~~F~~", "~~F~~", "~~F~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~")
                .aisle("HHHHH", "HFIFH", "~FIF~", "~FIF~", "~FIF~", "~FIF~", "~FIF~", "~FIF~", "~FIF~", "~FIF~", "~FIF~", "~FIF~", "~~F~~", "~~F~~", "~~F~~", "~~F~~", "~~F~~")
                .aisle("HHHHH", "HHFHH", "~CFC~", "~CFC~", "~CFC~", "~CFC~", "~CFC~", "~~F~~", "~~F~~", "~~F~~", "~~F~~", "~~F~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~")
                .aisle("~HHH~", "~HSH~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~")
                .where('S', this.selfPredicate())
                .where('C', statePredicate(this.getCasingState(this.transferType)))
                .where('H', statePredicate(this.getCasingState(this.transferType)).or(abilityPartPredicate(ALLOWED_ABILITIES)))
                .where('F', statePredicate(this.getFrameState(this.transferType)))
                .where('I', frameworkPredicate().or(frameworkPredicate2()))
                .where('~', tile -> true)
                .build();
    }

    public IBlockState getCasingState(TransferType transferType) {
        if (transferType == TransferType.INPUT)
            return GAMetaBlocks.METAL_CASING_1.getState(MetalCasing1.CasingType.TALONITE);
        else return GAMetaBlocks.METAL_CASING_2.getState(MetalCasing2.CasingType.RED_STEEL);
    }

    public IBlockState getFrameState(TransferType transferType) {
        if (transferType == TransferType.INPUT)
            return MetaBlocks.FRAMES.get(Talonite).getDefaultState();
        else return MetaBlocks.FRAMES.get(RedSteel).getDefaultState();
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
        this.tier = Math.max(framework, framework2);
        this.inputEnergyContainer = new EnergyContainerList(getAbilities(MultiblockAbility.INPUT_ENERGY));
        this.importFluidHandler = new FluidTankList(true, getAbilities(MultiblockAbility.IMPORT_FLUIDS));
        this.workableHandler.initialize(this.transferType.ordinal());
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return ClientHandler.TALONITE_CASING;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        Textures.MULTIBLOCK_WORKABLE_OVERLAY.render(renderState, translation, pipeline, getFrontFacing(), this.workableHandler.isActive());
    }

    @Override
    public long getEnergyStored() {
        return this.inputEnergyContainer.getEnergyStored();
    }

    @Override
    public long getEnergyCapacity() {
        return this.inputEnergyContainer.getEnergyCapacity();
    }

    @Override
    public long getMaxEUt() {
        return this.inputEnergyContainer.getInputVoltage();
    }

    @Override
    public int getEUBonus() {
        return -1;
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
    public void onLink(MetaTileEntity tileEntity) {
        this.workableHandler.onLink(tileEntity);
    }

    @Override
    public boolean isInterDimensional() {
        return true;
    }

    @Override
    public int dimensionID() {
        return getWorld().provider.getDimension();
    }

    @Override
    public int getDimension(int index) {
        return this.workableHandler.getEntityLinkWorld()[index];
    }

    @Override
    public int getRange() {
        return Integer.MAX_VALUE;
    }

    @Override
    public int getPosSize() {
        return this.workableHandler.getEntityLinkBlockPos().length;
    }

    @Override
    public BlockPos getPos(int i) {
        return this.workableHandler.getEntityLinkBlockPos()[i];
    }

    @Override
    public void setPos(String name, BlockPos pos, EntityPlayer player, World world, int index) {
        name = this.checkDuplicateNames(name, 1);
        this.workableHandler.getEntityLinkName()[index] = name;
        this.workableHandler.getEntityLinkWorld()[index] = world.provider.getDimension();
        this.workableHandler.getEntityEnergyAmps()[index] = 1;
        this.workableHandler.getEntityLinkBlockPos()[index] = pos;
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
    public int getPageIndex() {
        return this.pageIndex;
    }

    @Override
    public int getPageSize() {
        return this.pageSize;
    }

    @Override
    public void setLinkData(NBTTagCompound linkData) {
        this.workableHandler.setLinkData(linkData);
    }

    @Override
    public NBTTagCompound getLinkData() {
        return this.workableHandler.getLinkData();
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == TJCapabilities.CAPABILITY_LINK_POS)
            return TJCapabilities.CAPABILITY_LINK_POS.cast(this);
        if (capability == TJCapabilities.CAPABILITY_PARALLEL_CONTROLLER)
            return TJCapabilities.CAPABILITY_PARALLEL_CONTROLLER.cast(this);
        return super.getCapability(capability, side);
    }

    @Override
    public void setWorkingEnabled(boolean isActivationAllowed) {
        this.workableHandler.setWorkingEnabled(isActivationAllowed);
    }

    @Override
    public boolean isWorkingEnabled() {
        return this.workableHandler.isWorkingEnabled();
    }

    private IEnergyContainer getInputEnergyContainer() {
        return this.inputEnergyContainer;
    }

    private IMultipleTankHandler getImportFluidHandler() {
        return this.importFluidHandler;
    }

    public int getTier() {
        return this.tier;
    }

    public enum TransferType {
        INPUT,
        OUTPUT
    }
}
