package tj.machines.multi.electric;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.google.common.collect.Lists;
import gregicadditions.GAUtility;
import gregicadditions.GAValues;
import gregicadditions.capabilities.GregicAdditionsCapabilities;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.gui.GuiTextures;
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
import gregtech.api.util.function.BooleanConsumer;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.covers.filter.OreDictionaryItemFilter;
import gregtech.common.items.MetaItems;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import tj.blocks.BlockSolidCasings;
import tj.blocks.TJMetaBlocks;
import tj.builder.WidgetTabBuilder;
import tj.builder.multicontrollers.TJMultiblockDisplayBase;
import tj.gui.TJGuiTextures;
import tj.gui.widgets.PopUpWidgetGroup;
import tj.machines.ExtendedItemFilter;
import tj.machines.TJMiner;
import tj.textures.TJTextures;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static gregicadditions.GAMaterials.Taranium;
import static gregtech.api.gui.widgets.AdvancedTextWidget.withButton;
import static gregtech.api.unification.material.Materials.DrillingFluid;
import static gregtech.api.unification.material.Materials.Duranium;


public class MetaTileEntityEliteLargeMiner extends TJMultiblockDisplayBase implements TJMiner {

    protected static final MultiblockAbility<?>[] ALLOWED_ABILITIES = {MultiblockAbility.EXPORT_ITEMS, MultiblockAbility.IMPORT_FLUIDS, MultiblockAbility.INPUT_ENERGY, GregicAdditionsCapabilities.MAINTENANCE_HATCH};

    public final Type type;
    protected AtomicLong x = new AtomicLong(Long.MAX_VALUE), y = new AtomicLong(Long.MAX_VALUE), z = new AtomicLong(Long.MAX_VALUE);
    protected AtomicInteger currentChunk = new AtomicInteger(0), minY = new AtomicInteger(0), maxY = new AtomicInteger(Integer.MAX_VALUE);
    protected IEnergyContainer energyContainer;
    protected IMultipleTankHandler importFluidHandler;
    protected IItemHandlerModifiable outputInventory;
    protected List<Chunk> chunks = new ArrayList<>();
    protected boolean isActive = false;
    protected boolean done = false;
    protected boolean silktouch = false;
    protected boolean canRestart = false;
    protected final ExtendedItemFilter blockFilter;
    protected BooleanConsumer enableBlockPopUp;
    protected final OreDictionaryItemFilter oreDictFilter;
    protected BooleanConsumer enableOreDictPopUp;
    protected boolean enableFilter;
    protected boolean blackListFilter;
    protected boolean oreDict;

    public MetaTileEntityEliteLargeMiner(ResourceLocation metaTileEntityId, Type type) {
        super(metaTileEntityId);
        this.type = type;
        this.blockFilter = new ExtendedItemFilter();
        this.oreDictFilter = new OreDictionaryItemFilter();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityEliteLargeMiner(this.metaTileEntityId, this.getType());
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        if (this.type != Type.CREATIVE) {
            tooltip.add(I18n.format("gtadditions.machine.miner.multi.description", this.type.chunk, this.type.chunk, this.type.fortuneString));

            tooltip.add(I18n.format("gtadditions.machine.miner.fluid_usage", this.type.drillingFluidConsumePerTick, I18n.format(DrillingFluid.getFluid(0).getUnlocalizedName())));
        } else {
            tooltip.add(I18n.format("gtadditions.machine.miner.multi.description2", this.type.chunk, this.type.chunk, this.type.fortuneString));

            tooltip.add(I18n.format("gtadditions.machine.miner.fluid_usage", this.type.drillingFluidConsumePerTick, I18n.format(Taranium.getFluid(0).getUnlocalizedName())));
        }
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        resetTileAbilities();
        if (this.isActive)
            setActive(false);
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        initializeAbilities();
    }

    private void initializeAbilities() {
        this.importFluidHandler = new FluidTankList(true, getAbilities(MultiblockAbility.IMPORT_FLUIDS));
        this.outputInventory = new ItemHandlerList(getAbilities(MultiblockAbility.EXPORT_ITEMS));
        this.energyContainer = new EnergyContainerList(getAbilities(MultiblockAbility.INPUT_ENERGY));
    }

    private void resetTileAbilities() {
        this.importFluidHandler = new FluidTankList(true);
        this.outputInventory = new ItemStackHandler(0);
        this.energyContainer = new EnergyContainerList(Lists.newArrayList());
    }

    public boolean drainEnergy() {
        long energyDrain = GAValues.V[Math.max(GAValues.EV, GAUtility.getTierByVoltage(this.energyContainer.getInputVoltage()))];

        FluidStack drillingFluid;
        if (this.type != Type.CREATIVE) {
            drillingFluid = DrillingFluid.getFluid(type.drillingFluidConsumePerTick);
        } else {
            drillingFluid = Taranium.getFluid(type.drillingFluidConsumePerTick);
        }
        FluidStack canDrain = this.importFluidHandler.drain(drillingFluid, false);
        if (this.energyContainer.getEnergyStored() >= energyDrain && canDrain != null && canDrain.amount == type.drillingFluidConsumePerTick) {
            this.energyContainer.removeEnergy(this.energyContainer.getInputVoltage());
            this.importFluidHandler.drain(drillingFluid, true);
            return true;
        }
        return false;
    }

    @Override
    public Type getType() {
        return this.type;
    }

    @Override
    public long getNbBlock() {
        int tierDifference = GAUtility.getTierByVoltage(this.energyContainer.getInputVoltage()) - GAValues.MV;
        return (long) Math.floor(Math.pow(2, tierDifference));
    }

    @Override
    protected void updateFormedValid() {
        if (this.isWorkingEnabled) {
            if (this.getNumProblems() < 6) {
                if (this.getOffsetTimer() % (1 + this.getNumProblems()) == 0) {
                    if (this.done || !this.drainEnergy()) {
                        if (this.isActive)
                            this.setActive(false);
                        return;
                    }

                    if (!this.isActive)
                        this.setActive(true);

                    this.calculateMaintenance(1 + this.getNumProblems());
                    WorldServer world = (WorldServer) this.getWorld();
                    Chunk chunkMiner = world.getChunk(getPos());
                    Chunk origin;
                    if (this.chunks.isEmpty() && this.type.chunk / 2.0 > 1.0) {
                        int tmp = Math.floorDiv(this.type.chunk, 2);
                        origin = world.getChunk(chunkMiner.x - tmp, chunkMiner.z - tmp);
                        for (int i = 0; i < this.type.chunk; i++) {
                            for (int j = 0; j < this.type.chunk; j++) {
                                this.chunks.add(world.getChunk(origin.x + i, origin.z + j));
                            }
                        }
                    } else if (this.chunks.isEmpty() && this.type.chunk == 1) {
                        origin = world.getChunk(chunkMiner.x, chunkMiner.z);
                        this.chunks.add(origin);
                    }

                    if (this.currentChunk.intValue() == this.chunks.size()) {
                        this.setActive(false);
                        return;
                    }

                    Chunk chunk = this.chunks.get(this.currentChunk.intValue());

                    if (this.maxY.get() == Integer.MAX_VALUE) {
                        this.maxY.set(this.getPos().getY());
                    }
                    if (this.x.get() == Long.MAX_VALUE) {
                        this.x.set(chunk.getPos().getXStart());
                    }
                    if (this.z.get() == Long.MAX_VALUE) {
                        this.z.set(chunk.getPos().getZStart());
                    }
                    if (this.y.get() == Long.MAX_VALUE) {
                        this.y.set(this.maxY.get());
                    }
                    List<BlockPos> blockPos = TJMiner.getBlockToMinePerChunk(this, x, y, z, chunk.getPos());
                    blockPos.forEach(blockPos1 -> {
                        NonNullList<ItemStack> itemStacks = NonNullList.create();
                        IBlockState blockState = this.getWorld().getBlockState(blockPos1);
                        int meta = blockState.getBlock().getMetaFromState(blockState);
                        ItemStack stack = new ItemStack(blockState.getBlock(), 1, meta);
                        if (this.isEnableFilter()) {
                            boolean isFilterStack = this.oreDictFilter.matchItemStack(stack) != null || this.blockFilter.matchItemStack(stack) != null;
                            if (this.isBlackListFilter() == isFilterStack)
                                return;
                        }

                        if (!this.silktouch) {
                            if (getType() != Type.DESTROYER) {
                                GAUtility.applyHammerDrops(world.rand, blockState, itemStacks, this.type.fortune, null, this.energyContainer.getInputVoltage());
                            } else {
                                itemStacks.add(new ItemStack(blockState.getBlock().getItemDropped(blockState, world.rand, this.type.fortune), 1, meta));
                            }
                        } else {
                            itemStacks.add(stack);
                        }
                        if (addItemsToItemHandler(this.outputInventory, true, itemStacks)) {
                            addItemsToItemHandler(this.outputInventory, false, itemStacks);
                            if (this.getType() != Type.DESTROYER) {
                                world.setBlockState(blockPos1, Blocks.COBBLESTONE.getDefaultState());
                            } else {
                                world.setBlockState(blockPos1, Blocks.AIR.getDefaultState());
                            }
                        }
                    });

                    if (this.y.get() < this.minY.get()) {
                        if (this.type != Type.CREATIVE) {
                            this.currentChunk.incrementAndGet();
                            if (this.currentChunk.get() >= this.chunks.size()) {
                                if (this.canRestart) {
                                    this.currentChunk.set(0);
                                    this.chunks.clear();
                                    this.x.set(this.chunks.get(this.currentChunk.intValue()).getPos().getXStart());
                                    this.z.set(this.chunks.get(this.currentChunk.intValue()).getPos().getZStart());
                                    this.y.set(this.maxY.get());
                                } else
                                    this.done = true;
                            } else {
                                this.x.set(this.chunks.get(this.currentChunk.intValue()).getPos().getXStart());
                                this.z.set(this.chunks.get(this.currentChunk.intValue()).getPos().getZStart());
                                this.y.set(this.maxY.get());
                            }
                        } else {
                            this.x.set(this.chunks.get(this.currentChunk.intValue()).getPos().getXStart());
                            this.z.set(this.chunks.get(this.currentChunk.intValue()).getPos().getZStart());
                            this.y.set(maxY.get());
                        }
                    }

                    if (!this.getWorld().isRemote && this.getOffsetTimer() % 5 == 0) {
                        this.pushItemsIntoNearbyHandlers(getFrontFacing());
                    }
                }
            }
        } else if (this.isActive)
            this.setActive(false);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("F###F", "F###F", "PQQQP", "#####", "#####", "#####", "#####", "#####", "#####", "#####")
                .aisle("#####", "#####", "QPPPQ", "#CCC#", "#####", "#####", "#####", "#####", "#####", "#####")
                .aisle("#####", "#####", "QPPPQ", "#CPC#", "#FFF#", "#FFF#", "#FFF#", "##F##", "##F##", "##F##")
                .aisle("#####", "#####", "CPPPQ", "#CSC#", "#####", "#####", "#####", "#####", "#####", "#####")
                .aisle("F###F", "F###F", "PQQQP", "#####", "#####", "#####", "#####", "#####", "#####", "#####")
                .setAmountAtLeast('L', 3)
                .where('S', this.selfPredicate())
                .where('L', statePredicate(getCasingState()))
                .where('C', statePredicate(getCasingState()).or(abilityPartPredicate(ALLOWED_ABILITIES)))
                .where('P', statePredicate(getCasingState()))
                .where('Q', statePredicate(getCasingState()).or(abilityPartPredicate(MultiblockAbility.EXPORT_ITEMS)))
                .where('F', statePredicate(getFrameState()))
                .where('#', blockWorldState -> true)
                .build();
    }

    public IBlockState getFrameState() {
        return MetaBlocks.FRAMES.get(Duranium).getDefaultState();
    }

    public IBlockState getCasingState() {
        return TJMetaBlocks.SOLID_CASING.getState(BlockSolidCasings.SolidCasingType.DURANIUM_CASING);
    }

    @Override
    protected boolean checkStructureComponents(List<IMultiblockPart> parts, Map<MultiblockAbility<Object>, List<Object>> abilities) {
        //basically check minimal requirements for inputs count
        int itemOutputsCount = abilities.getOrDefault(MultiblockAbility.EXPORT_ITEMS, Collections.emptyList())
                .stream().map(it -> (IItemHandler) it).mapToInt(IItemHandler::getSlots).sum();
        int fluidInputsCount = abilities.getOrDefault(MultiblockAbility.IMPORT_FLUIDS, Collections.emptyList()).size();
        return itemOutputsCount >= 1 && fluidInputsCount >= 1 && abilities.containsKey(MultiblockAbility.INPUT_ENERGY) && super.checkStructureComponents(parts, abilities);
    }

    @Override
    protected void addTabs(WidgetTabBuilder tabBuilder, EntityPlayer player) {
        super.addTabs(tabBuilder, player);
        tabBuilder.addTab("tj.multiblock.tab.filter", MetaItems.ITEM_FILTER.getStackForm(), filterTab -> {
            PopUpWidgetGroup slotsPopUp = new PopUpWidgetGroup(0, 7, 180, 90, null);
            PopUpWidgetGroup oreDictPopUp = new PopUpWidgetGroup(9, 37, 179, 90, GuiTextures.BACKGROUND);
            this.blockFilter.initUI(slotsPopUp::addWidget);
            this.enableBlockPopUp = slotsPopUp::setEnabled;
            this.enableBlockPopUp.apply(!this.oreDict);
            this.oreDictFilter.initUI(oreDictPopUp::addWidget);
            this.enableOreDictPopUp = oreDictPopUp::setEnabled;
            this.enableOreDictPopUp.apply(this.oreDict);
            filterTab.addWidget(new ToggleButtonWidget(172, 133, 18, 18, GuiTextures.TOGGLE_BUTTON_BACK, this::isEnableFilter, this::setEnableFilter)
                    .setTooltipText("machine.universal.toggle.filter"));
            filterTab.addWidget(new ImageWidget(172, 133, 18, 18, TJGuiTextures.ITEM_FILTER));
            filterTab.addWidget(new ToggleButtonWidget(172, 151, 18, 18, GuiTextures.BUTTON_BLACKLIST, this::isBlackListFilter, this::setBlackListFilter)
                    .setTooltipText("cover.filter.blacklist"));
            filterTab.addWidget(new ToggleButtonWidget(172, 169, 18, 18, GuiTextures.BUTTON_FILTER_DAMAGE, this::isOreDict, this::setOreDict)
                    .setTooltipText("cover.filter.ore_dictionary.open"));
            filterTab.addWidget(slotsPopUp);
            filterTab.addWidget(oreDictPopUp);
        });
        tabBuilder.addTab("tj.multiblock.tab.settings", MetaItems.WRENCH.getStackForm(), settingsTab -> settingsTab.addWidget(new AdvancedTextWidget(10,-2, this::addSettingsDisplayText, 0xFFFFFF)
                .setMaxWidthLimit(180)
                .setClickHandler(this::handleSettingDisplayText)));
    }

    @Override
    protected void mainDisplayTab(WidgetGroup widgetGroup) {
        super.mainDisplayTab(widgetGroup);
        widgetGroup.addWidget(new ToggleButtonWidget(172, 151, 18, 18, TJGuiTextures.RESET_BUTTON, () -> false, this::setDone)
                .setTooltipText("machine.universal.toggle.reset"));
    }

    private void setDone(boolean reset) {
        this.done = false;
        this.isWorkingEnabled = false;
        if (this.isActive)
            setActive(false);
        if (!this.chunks.isEmpty()) {
            this.currentChunk.set(0);
            this.x.set(chunks.get(this.currentChunk.intValue()).getPos().getXStart());
            this.z.set(chunks.get(this.currentChunk.intValue()).getPos().getZStart());
            this.y.set(maxY.get());
            this.chunks.clear();
        }
    }

    protected void addSettingsDisplayText(List<ITextComponent> textList) {
        if (this.isActive)
            textList.add(new TextComponentTranslation("tj.multiblock.elite_large_miner.running"));
        textList.add(new TextComponentTranslation("tj.multiblock.elite_large_miner.mining.level")
                .appendText(" ")
                .appendSibling(withButton(new TextComponentTranslation("tj.multiblock.elite_large_miner.reset.y"), "reset")));
        textList.add(new TextComponentString(net.minecraft.util.text.translation.I18n.translateToLocalFormatted("tj.multiblock.elite_large_miner.maximum.y", maxY.get()))
                .appendSibling(withButton(new TextComponentString(" [+]"), "maxYIncrement"))
                .appendSibling(withButton(new TextComponentString(" [-]"), "maxYDecrement")));
        textList.add(new TextComponentString(net.minecraft.util.text.translation.I18n.translateToLocalFormatted("tj.multiblock.elite_large_miner.minimum.y", minY.get()))
                .appendSibling(withButton(new TextComponentString(" [+]"), "minYIncrement"))
                .appendSibling(withButton(new TextComponentString(" [-]"), "minYDecrement")));
    }

    private void handleSettingDisplayText(String componentData, Widget.ClickData clickData) {
        if (this.isActive) return;
        int value = clickData.isCtrlClick ? 100
                : clickData.isShiftClick ? 10
                : 1;
        switch (componentData) {
            case "maxYIncrement":
                this.maxY.set(MathHelper.clamp(this.maxY.get() + value, this.minY.get(), this.getPos().getY()));
                return;
            case "maxYDecrement":
                this.maxY.set(MathHelper.clamp(this.maxY.get() - value, this.minY.get(), this.getPos().getY()));
                return;
            case "minYIncrement":
                this.minY.set(MathHelper.clamp(this.minY.get() + value, 0, this.maxY.get()));
                return;
            case "minYDecrement":
                this.minY.set(MathHelper.clamp(this.minY.get() - value, 0, this.maxY.get()));
                return;
            default:
                this.maxY.set(getPos().getY());
                this.minY.set(0);
        }
    }

    public void setEnableFilter(boolean enableFilter) {
        this.enableFilter = enableFilter;
    }

    public boolean isEnableFilter() {
        return this.enableFilter;
    }

    public void setBlackListFilter(boolean blackListFilter) {
        this.blackListFilter = blackListFilter;
    }

    public boolean isBlackListFilter() {
        return this.blackListFilter;
    }

    private void setOreDict(boolean oreDict) {
        this.oreDict = oreDict;
        this.enableBlockPopUp.apply(!oreDict);
        this.enableOreDictPopUp.apply(oreDict);
    }

    private boolean isOreDict() {
        return this.oreDict;
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        super.addDisplayText(textList);
        if (this.isStructureFormed()) {
            if(this.x.get() == Long.MAX_VALUE) {
                textList.add(new TextComponentString("X: Not Active"));
                textList.add(new TextComponentString("Y: Not Active"));
                textList.add(new TextComponentString("Z: Not Active"));
            } else {
                textList.add(new TextComponentString(String.format("X: %d", this.x.get())));
                textList.add(new TextComponentString(String.format("Y: %d", this.y.get())));
                textList.add(new TextComponentString(String.format("Z: %d", this.z.get())));
            }
            textList.add(new TextComponentTranslation("gregtech.multiblock.large_miner.chunk", this.currentChunk.get()));
            textList.add(new TextComponentTranslation("gregtech.multiblock.large_miner.nb_chunk", this.chunks.size()));
            textList.add(new TextComponentTranslation("gregtech.multiblock.large_miner.block_per_tick", getNbBlock()));
            if (this.type != Type.CREATIVE) {
                textList.add(new TextComponentTranslation("gregtech.multiblock.large_miner.silktouch", this.silktouch));
                textList.add(new TextComponentTranslation("gregtech.multiblock.large_miner.mode"));
            }
            ITextComponent toggleContinous = new TextComponentTranslation("tj.multiblock.elite_large_miner.restart");
            toggleContinous.appendText(" ");

            if (this.canRestart)
                toggleContinous.appendSibling(withButton(new TextComponentTranslation("tj.multiblock.elite_large_miner.restart.true"), "true"));
            else toggleContinous.appendSibling(withButton(new TextComponentTranslation("tj.multiblock.elite_large_miner.restart.false"), "false"));

            textList.add(toggleContinous);

            if (this.done)
                textList.add(new TextComponentTranslation("gregtech.multiblock.large_miner.done", getNbBlock()).setStyle(new Style().setColor(TextFormatting.GREEN)));
        }
    }

    @Override
    protected void handleDisplayClick(String componentData, Widget.ClickData clickData) {
        this.canRestart = !componentData.equals("true");
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return TJTextures.DURANIUM;
    }

    @Override
    public boolean onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        if (this.type != Type.CREATIVE) {
            this.silktouch = !this.silktouch;
            return true;
        }
        return false;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        NBTTagList blockList = new NBTTagList();
        NBTTagList indexList = new NBTTagList();
        this.blockFilter.writeToNBT(data);
        data.setTag("BlockList", blockList);
        data.setTag("IndexList", indexList);
        data.setBoolean("EnableFilter", this.enableFilter);
        data.setBoolean("BlackListFilter", this.blackListFilter);
        data.setTag("xPos", new NBTTagLong(this.x.get()));
        data.setTag("yPos", new NBTTagLong(this.y.get()));
        data.setTag("zPos", new NBTTagLong(this.z.get()));
        data.setTag("chunk", new NBTTagInt(this.currentChunk.get()));
        data.setTag("done", new NBTTagInt(this.done ? 1 : 0));
        data.setTag("silktouch", new NBTTagInt(this.silktouch ? 1 : 0));
        data.setTag("restart", new NBTTagInt(this.canRestart ? 1 : 0));
        data.setTag("working", new NBTTagInt(this.isWorkingEnabled ? 1 : 0));
        data.setTag("minY", new NBTTagInt(this.minY.get()));
        data.setTag("maxY", new NBTTagInt(this.maxY.get()));
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.blockFilter.readFromNBT(data);
        this.blackListFilter = data.getBoolean("BlackListFilter");
        this.enableFilter = data.getBoolean("EnableFilter");
        this.x.set(data.getLong("xPos"));
        this.y.set(data.getLong("yPos"));
        this.z.set(data.getLong("zPos"));
        this.currentChunk.set(data.getInteger("chunk"));
        this.done = data.getInteger("done") != 0;
        this.silktouch = data.getInteger("silktouch") != 0;
        this.canRestart = data.getInteger("restart") != 0;
        this.isWorkingEnabled = data.getInteger("working") != 0;
        this.minY.set(data.getInteger("minY"));
        this.maxY.set(data.getInteger("maxY"));
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(this.isActive);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.isActive = buf.readBoolean();
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        Textures.MULTIBLOCK_WORKABLE_OVERLAY.render(renderState, translation, pipeline, getFrontFacing(), this.isActive);
    }

    protected void setActive(boolean active) {
        this.isActive = active;
        if (!getWorld().isRemote) {
            this.writeCustomData(1, buf -> buf.writeBoolean(active));
            this.markDirty();
        }
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == 1) {
            this.isActive = buf.readBoolean();
            this.getHolder().scheduleChunkForRenderUpdate();
        }
    }
}
