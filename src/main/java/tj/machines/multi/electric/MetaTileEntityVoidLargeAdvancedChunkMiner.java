package tj.machines.multi.electric;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregicadditions.GAMaterials;
import gregicadditions.Gregicality;
import gregicadditions.capabilities.GregicAdditionsCapabilities;
import gregicadditions.client.ClientHandler;
import gregicadditions.item.components.MotorCasing;
import gregicadditions.machines.multi.simple.LargeSimpleRecipeMapMultiblockController;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.AdvancedTextWidget;
import gregtech.api.gui.widgets.ToggleButtonWidget;
import gregtech.api.metatileentity.MTETrait;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.api.multiblock.PatternMatchContext;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.render.ICubeRenderer;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.items.MetaItems;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.registries.IForgeRegistryEntry;
import tj.blocks.BlockSolidCasings;
import tj.blocks.TJMetaBlocks;
import tj.builder.WidgetTabBuilder;
import tj.builder.multicontrollers.GUIDisplayBuilder;
import tj.builder.multicontrollers.TJMultiblockControllerBase;
import tj.capability.IProgressBar;
import tj.capability.ProgressBar;
import tj.capability.impl.handler.IMinerHandler;
import tj.capability.impl.workable.MinerWorkableHandler;
import tj.gui.TJGuiTextures;
import tj.gui.widgets.impl.ButtonPopUpWidget;
import tj.gui.widgets.impl.TJPhantomSlotWidget;
import tj.gui.widgets.impl.TJToggleButtonWidget;
import tj.gui.widgets.impl.WindowsWidgetGroup;
import tj.items.handlers.GhostSlotHandler;
import tj.items.handlers.LargeItemStackHandler;
import tj.textures.TJTextures;
import tj.util.EnumFacingHelper;
import tj.util.TJFluidUtils;
import tj.util.pair.IntPair;

import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.function.UnaryOperator;

import static gregtech.api.unification.material.type.Material.MATERIAL_REGISTRY;

public class MetaTileEntityVoidLargeAdvancedChunkMiner extends TJMultiblockControllerBase implements IMinerHandler, IProgressBar {

    private static final MultiblockAbility<?>[] ALLOWED_ABILITIES = {MultiblockAbility.IMPORT_ITEMS, MultiblockAbility.EXPORT_ITEMS, MultiblockAbility.IMPORT_FLUIDS, MultiblockAbility.EXPORT_FLUIDS, MultiblockAbility.INPUT_ENERGY, GregicAdditionsCapabilities.MAINTENANCE_HATCH};
    private final InfiniteMinerWorkableHandler workableHandler = new InfiniteMinerWorkableHandler(this);
    private final GhostSlotHandler ghostSlotHandler = new GhostSlotHandler(this.getImportItems().getSlots());
    private FluidStack drillingFluid = GAMaterials.Taranium.getFluid(1);
    private int fortune;
    private int tier;

    public MetaTileEntityVoidLargeAdvancedChunkMiner(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityVoidLargeAdvancedChunkMiner(this.metaTileEntityId);
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return new LargeItemStackHandler(60, 1);
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
        widgetGroup.add(new ToggleButtonWidget(175, 151, 18, 18, TJGuiTextures.RESET_BUTTON, () -> false, this.workableHandler::setDone)
                .setTooltipText("machine.universal.toggle.reset"));
    }

    @Override
    protected void addDisplayText(GUIDisplayBuilder builder) {
        super.addDisplayText(builder);
        if (!this.isStructureFormed()) return;
        builder.addVoltageInLine(this.getInputEnergyContainer())
                .addEnergyInputLine(this.getInputEnergyContainer(), this.workableHandler.getEnergyPerTick())
                .addTranslationLine("tj.multiblock.advanced_large_miner.chunk_index", this.workableHandler.getChunkIndex() + 1, this.workableHandler.getChunkSize())
                .addTextComponent(AdvancedTextWidget.withButton(new TextComponentTranslation(this.workableHandler.isSilkTouch() ? "tj.multiblock.advanced_large_miner.silktouch_true" : "tj.multiblock.advanced_large_miner.silktouch_false")
                        .setStyle(new Style().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentTranslation("tj.multiblock.advanced_large_miner.silktouch")))), this.workableHandler.isSilkTouch() ? "silkTouch:true" : "silkTouch:false"))
                .addTextComponent(AdvancedTextWidget.withButton(new TextComponentTranslation(this.workableHandler.isReset() ? "tj.multiblock.advanced_large_miner.reset_true" : "tj.multiblock.advanced_large_miner.reset_false")
                        .setStyle(new Style().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentTranslation("tj.multiblock.advanced_large_miner.reset")))), this.workableHandler.isReset() ? "reset:true" : "reset:false"))
                .addIsWorkingLine(this.workableHandler.isWorkingEnabled(), this.workableHandler.isActive(), this.workableHandler.getProgress(), this.workableHandler.getMaxProgress(), 998)
                .addRecipeOutputLine(this.workableHandler, 999);
        if (this.workableHandler.isDone())
            builder.addTextComponent(new TextComponentTranslation("gregtech.multiblock.large_miner.done")
                    .setStyle(new Style().setColor(TextFormatting.GREEN)), 1000);
        if (this.workableHandler.isActive())
            builder.addTranslationLine("metaitem.linking.device.x", this.workableHandler.getX())
                    .addTranslationLine("metaitem.linking.device.y", this.workableHandler.getY())
                    .addTranslationLine("metaitem.linking.device.z", this.workableHandler.getZ())
                    .addFluidInputLine(this.getImportFluidTank(), this.drillingFluid)
                    .addTranslationLine("gtadditions.machine.miner.fluid_usage", this.drillingFluid.amount, this.drillingFluid.getLocalizedName())
                    .addTranslationLine("gregtech.multiblock.large_miner.block_per_tick", this.workableHandler.getMiningSpeed());
    }

    @Override
    protected void handleDisplayClick(String componentData, Widget.ClickData clickData) {
        final String[] data = componentData.split(":");
        if (data[0].equals("silkTouch")) {
            this.workableHandler.setSilkTouch(!data[1].equals("true"));
        } else if (data[0].equals("reset")) {
            this.workableHandler.setReset(!data[1].equals("true"));
        }
    }

    @Override
    protected void addTabs(WidgetTabBuilder tabBuilder, EntityPlayer player) {
        super.addTabs(tabBuilder, player);
        tabBuilder.addTab("tj.multiblock.tab.filter", MetaItems.ITEM_FILTER.getStackForm(), tab -> {
            tab.add(new ButtonPopUpWidget<>()
                    .addPopup(widgetGroup -> {
                        for (int i = 0; i < this.getImportItems().getSlots(); i++) {
                            widgetGroup.addWidget(new TJPhantomSlotWidget(this.getImportItems(), i, 10 + (18 * (i % 10)), 10 + (18 * (i / 10)))
                                    .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.FILTER_SLOT_OVERLAY)
                                    .setTakeItemsPredicate(this.workableHandler::removeItemFromFilter)
                                    .setPutItemsPredicate(this.workableHandler::addItemToFilter)
                                    .setAreGhostItems(this.ghostSlotHandler.getAreGhostItems()));
                        }
                        return false;
                    }).addPopup(40, 40, 0, 0, new TJToggleButtonWidget(175, this.getOffsetY(134), 18, 18)
                            .setBackgroundTextures(TJGuiTextures.ORE_DICTIONARY_FILTER)
                            .setToggleTexture(GuiTextures.TOGGLE_BUTTON_BACK)
                            .useToggleTexture(true), widgetGroup -> {
                        WindowsWidgetGroup windowsWidgetGroup = new WindowsWidgetGroup(0, -3, 135, 45, GuiTextures.BORDERED_BACKGROUND)
                                .addSubWidget(new ToggleButtonWidget(113, 23, 18, 18, GuiTextures.BUTTON_BLACKLIST, this.workableHandler::isBlacklist, this.workableHandler::setBlacklist)
                                        .setTooltipText("tj.multiblock.advanced_large_miner.blacklist"));
                        this.workableHandler.getOreDictFilter().initUI(windowsWidgetGroup::addSubWidget);
                        widgetGroup.addWidget(windowsWidgetGroup);
                        return false;
                    }));
            tab.add(new ToggleButtonWidget(175, 169, 18, 18, GuiTextures.BUTTON_BLACKLIST, this.workableHandler::isBlacklistBlock, this.workableHandler::setBlacklistBlock)
                    .setTooltipText("tj.multiblock.advanced_large_miner.blacklist_block"));
        });
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("F~~~F", "F~~~F", "CCCCC", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~")
                .aisle("~~~~~", "~~~~~", "CCCCC", "~XXX~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~")
                .aisle("~~~~~", "~~~~~", "CCMCC", "~XXX~", "~FFF~", "~FFF~", "~FFF~", "~~F~~", "~~F~~", "~~F~~")
                .aisle("~~~~~", "~~~~~", "CCCCC", "~XSX~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~")
                .aisle("F~~~F", "F~~~F", "CCCCC", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~")
                .where('S', this.selfPredicate())
                .where('C', statePredicate(this.getCasingState()))
                .where('X', statePredicate(this.getCasingState()).or(abilityPartPredicate(ALLOWED_ABILITIES)))
                .where('M', LargeSimpleRecipeMapMultiblockController.motorPredicate())
                .where('F', statePredicate(MetaBlocks.FRAMES.get(MATERIAL_REGISTRY.getObject("chaos")).getDefaultState()))
                .where('~', tile -> true)
                .build();
    }

    private IBlockState getCasingState() {
        return TJMetaBlocks.SOLID_CASING.getState(BlockSolidCasings.SolidCasingType.CHAOS_ALLOY);
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        this.tier = context.getOrDefault("Motor", MotorCasing.CasingType.MOTOR_LV).getTier();
        this.workableHandler.initialize(this.getAbilities(MultiblockAbility.IMPORT_ITEMS).size());
        this.fortune = this.tier + this.tier;
        this.drillingFluid = GAMaterials.Taranium.getFluid(1 << this.getTier() - 1);
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.tier = 0;
        this.fortune = 0;
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return TJTextures.CHAOS_ALLOY;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        TJTextures.TJ_MULTIBLOCK_WORKABLE_OVERLAY.render(renderState, translation, pipeline, this.getFrontFacing(), this.workableHandler.isActive(), this.workableHandler.hasProblem(), this.workableHandler.isWorkingEnabled());
        ClientHandler.CHUNK_MINER_OVERLAY.renderSided(EnumFacingHelper.getLeftFacingFrom(this.getFrontFacing()), renderState, translation, pipeline);
        ClientHandler.CHUNK_MINER_OVERLAY.renderSided(EnumFacingHelper.getRightFacingFrom(this.getFrontFacing()), renderState, translation, pipeline);
    }

    @Override
    public void clearMachineInventory(NonNullList<ItemStack> itemBuffer) {
        this.ghostSlotHandler.clearInventory(this.getImportItems(), itemBuffer);
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        this.ghostSlotHandler.writeInitialSyncData(buf);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.ghostSlotHandler.readInitialSyncData(buf);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        this.ghostSlotHandler.writeToNBT(data);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.ghostSlotHandler.readFromNBT(data);
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
    public int getFortuneLvl() {
        return this.fortune;
    }

    @Override
    public int getDiameter() {
        return this.getTier();
    }

    @Override
    public int getTier() {
        return this.tier;
    }

    @Override
    public FluidStack getDrillingFluid() {
        return this.drillingFluid;
    }

    @Override
    public int[][] getBarMatrix() {
        return new int[1][1];
    }

    @Override
    public void getProgressBars(Queue<UnaryOperator<ProgressBar.ProgressBarBuilder>> bars) {
        bars.add(bar -> bar.setProgress(this::getDrillingFluidAmount).setMaxProgress(this::getDrillingFluidCapacity)
                .setLocale("tj.multiblock.bars.fluid").setParams(() -> new Object[]{this.drillingFluid.getLocalizedName()})
                .setFluidStackSupplier(() -> this.drillingFluid));
    }

    private long getDrillingFluidAmount() {
        return TJFluidUtils.getFluidAmountFromTanks(this.drillingFluid, this.getImportFluidTank());
    }

    private long getDrillingFluidCapacity() {
        return TJFluidUtils.getFluidCapacityFromTanks(this.drillingFluid, this.getImportFluidTank());
    }

    @Override
    public String getRecipeUid() {
        return Gregicality.MODID + ":" + RecipeMaps.MACERATOR_RECIPES.getUnlocalizedName();
    }

    private static class InfiniteMinerWorkableHandler extends MinerWorkableHandler {

        public InfiniteMinerWorkableHandler(MetaTileEntity metaTileEntity) {
            super(metaTileEntity);
        }

        @Override
        protected <T extends IForgeRegistryEntry<T>> boolean addItemDrop(T type, int count, int meta) {
            if (type == null)
                return false;
            final Item item = type instanceof Item ? (Item) type : Item.getItemFromBlock((Block) type);
            final IntPair<ItemStack> stackPair = this.itemType.get(item);
            if (stackPair != null) {
                if (this.blacklist == (this.oreDictFilter.matchItemStack(stackPair.getValue()) != null))
                    return false;
                if (!this.silkTouch && OreDictUnifier.getPrefix(stackPair.getValue()) == OrePrefix.crushed) {
                    count = this.getFortune(stackPair.getKey());
                    stackPair.getValue().grow(count);
                    return false;
                } else stackPair.getValue().grow(count);
            } else {
                ItemStack itemStack = type instanceof Block ? new ItemStack((Block) type, count, meta) : new ItemStack((Item) type, count, meta);
                if (this.blacklist == (this.oreDictFilter.matchItemStack(itemStack) != null))
                    return false;
                if (!this.silkTouch && this.handler.getFortuneLvl() > 1) {
                    final Recipe recipe = RecipeMaps.MACERATOR_RECIPES.findRecipe(Long.MAX_VALUE, Collections.singletonList(itemStack), Collections.emptyList(), 0);
                    if (recipe != null) {
                        itemStack = recipe.getResultItemOutputs(Integer.MAX_VALUE, this.metaTileEntity.getWorld().rand, this.handler.getTier()).get(0).copy();
                        final int originalCount = itemStack.getCount();
                        if (OreDictUnifier.getPrefix(itemStack) == OrePrefix.crushed) {
                            itemStack.setCount(this.getFortune(originalCount));
                            this.itemType.put(item, IntPair.of(originalCount, itemStack));
                            this.itemOutputs.add(itemStack);
                            return false;
                        }
                    }
                }
                this.itemType.put(item, IntPair.of(itemStack.getCount(), itemStack));
                this.itemOutputs.add(itemStack);
            }
            return true;
        }
    }
}
