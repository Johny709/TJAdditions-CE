package tj.machines.singleblock;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.*;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.recipes.CountableIngredient;
import gregtech.api.render.Textures;
import gregtech.api.util.DummyContainer;
import gregtech.api.util.Position;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import tj.capability.impl.workable.CrafterRecipeLogic;
import tj.capability.impl.handler.IRecipeMapProvider;
import tj.builder.RecipeUtility;
import tj.gui.TJGuiTextures;
import tj.gui.widgets.impl.*;
import tj.gui.widgets.TJLabelWidget;
import tj.textures.TJTextures;
import tj.util.Color;
import tj.util.EnumFacingHelper;
import tj.util.TooltipHelper;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

import static gregtech.api.gui.GuiTextures.*;
import static gregtech.api.gui.GuiTextures.INDICATOR_NO_ENERGY;
import static tj.gui.TJGuiTextures.*;


public class MetaTileEntityCrafter extends TJTieredWorkableMetaTileEntity implements IRecipeMapProvider {

    private final CrafterRecipeLogic recipeLogic = new CrafterRecipeLogic(this);
    private final InventoryCrafting inventoryCrafting = new InventoryCrafting(new DummyContainer(), 3, 3);
    private final ItemStackHandler craftingInventory = new ItemStackHandler(9);
    private final ItemStackHandler encodingInventory;
    private final ItemStackHandler resultInventory = new ItemStackHandler(1);
    private final Int2ObjectMap<Triple<IRecipe, NonNullList<CountableIngredient>, NonNullList<ItemStack>>> recipeMap = new Int2ObjectOpenHashMap<>();
    private final int encodingSlots;
    private final int parallel;
    private IRecipe currentRecipe;

    public MetaTileEntityCrafter(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
        this.encodingSlots = 6 + (tier * 3);
        this.parallel = 1 << this.getTier() - 1;
        this.encodingInventory = new ItemStackHandler(this.encodingSlots);
        this.recipeLogic.initialize(1);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder metaTileEntityHolder) {
        return new MetaTileEntityCrafter(this.metaTileEntityId, this.getTier());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("tj.multiblock.large_crafter.description"));
        tooltip.add(I18n.format("tj.multiblock.large_crafter.slots", this.encodingSlots));
        tooltip.add(I18n.format("tj.multiblock.parallel", this.parallel));
        tooltip.add(I18n.format("tj.machine.crafter.tooltip"));
        tooltip.add(TooltipHelper.blinkingText(Color.YELLOW, 20, "tj.multiblock.large_crafter.warning"));
    }

    @Override
    public void update() {
        super.update();
        if (!this.getWorld().isRemote)
            this.recipeLogic.update();
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return new ItemStackHandler(18);
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return new ItemStackHandler(1);
    }

    @Override
    protected ModularUI createUI(EntityPlayer player) {
        WidgetGroup inventorySlotGroup = new WidgetGroup(new Position(7, 72)), craftingSlotGroup = new WidgetGroup(new Position(7, 14));
        SlotScrollableWidgetGroup scrollableWidgetGroup = new SlotScrollableWidgetGroup(109, 14, 64, 54, 3);
        for (int i = 0; i < this.importItems.getSlots(); i++) {
            inventorySlotGroup.addWidget(new SlotWidget(this.importItems, i, 18 * (i % 9), 18 * (i / 9), true, true)
                    .setBackgroundTexture(SLOT));
        }
        for (int i = 0; i < this.craftingInventory.getSlots(); i++) {
            int finalI = i;
            craftingSlotGroup.addWidget(new PhantomSlotWidget(this.craftingInventory, i, 18 * (i % 3), 18 * (i / 3))
                    .setBackgroundTexture(SLOT)
                    .setChangeListener(() -> this.setCraftingResult(finalI, this.craftingInventory.getStackInSlot(finalI))));
        }
        for (int i = 0; i < this.encodingInventory.getSlots(); i++) {
            scrollableWidgetGroup.addWidget(new SlotDisplayWidget(this.encodingInventory, i, 18 * (i % 3), 18 * (i / 3))
                    .onPressedConsumer((button, slot, stack) -> {
                        if (button == 0) {
                            this.clearCraftingResult();
                            NonNullList<ItemStack> itemStacks = this.recipeMap.get(slot).getRight();
                            for (int j = 0; j < itemStacks.size(); j++) {
                                this.setCraftingResult(j, itemStacks.get(j));
                            }
                        } else if (button == 1) {
                            this.removeRecipe(slot);
                        }
                    }));
        }
        RecipeOutputDisplayWidget displayWidget = new RecipeOutputDisplayWidget(77, 21, 21, 20)
                .setFluidOutputSupplier(this.recipeLogic::getFluidOutputs)
                .setItemOutputSupplier(this.recipeLogic::getItemOutputs)
                .setItemOutputInventorySupplier(this::getExportItems)
                .setFluidOutputTankSupplier(this::getExportFluids);
        return ModularUI.builder(BACKGROUND, 176, 216)
                .widget(new TJLabelWidget(7, -18, 162, 18, TJGuiTextures.MACHINE_LABEL)
                        .setItemLabel(this.getStackForm()).setLocale(this.getMetaFullName()))
                .widget(new ProgressWidget(this.recipeLogic::getProgressPercent, 55, 111, 21, 20, PROGRESS_BAR_ARROW, ProgressWidget.MoveType.HORIZONTAL))
                .widget(new ImageWidget(72, 28, 26, 26, SLOT))
                .widget(new ImageWidget(109, 14, 54, 54, DARKENED_SLOT))
                .widget(new SlotDisplayWidget(this.resultInventory, 0, 76, 32)
                        .onPressedConsumer((button, slot, stack) -> this.addRecipe(this.currentRecipe)))
                .widget(new DischargerSlotWidget(this.chargerInventory, 0, 25, 112)
                        .setBackgroundTexture(SLOT, CHARGER_OVERLAY))
                .widget(new SlotWidget(this.exportItems, 0, 79, 112, true, false)
                        .setBackgroundTexture(SLOT))
                .widget(new RecipeOutputSlotWidget(0, 79, 112, 18, 18, displayWidget::getItemOutputAt, null))
                .widget(new ToggleButtonWidget(133, 112, 18, 18, ITEM_VOID_BUTTON, this.recipeLogic::isVoidOutputs, this.recipeLogic::setVoidOutputs)
                        .setTooltipText("machine.universal.toggle.item_voiding"))
                .widget(new ToggleButtonWidget(151, 112, 18, 18, POWER_BUTTON, this.recipeLogic::isWorkingEnabled, this.recipeLogic::setWorkingEnabled)
                        .setTooltipText("machine.universal.toggle.run.mode"))
                .widget(new ToggleButtonWidget(7, 112, 18, 18, BUTTON_ITEM_OUTPUT, this::isAutoOutputItems, this::setItemAutoOutput)
                        .setTooltipText("gregtech.gui.item_auto_output.tooltip"))
                .widget(new ImageWidget(79, 62, 18, 18, INDICATOR_NO_ENERGY)
                        .setPredicate(this.recipeLogic::hasNotEnoughEnergy))
                .widget(new ClickButtonWidget(62, 14, 8, 8, "", (clickData) -> {
                    this.clearCraftingResult();
                    this.setCraftingResult(0, ItemStack.EMPTY);
                }).setButtonTexture(BUTTON_CLEAR_GRID))
                .widget(new CraftingRecipeTransferWidget(this::setCraftingResult))
                .widget(craftingSlotGroup)
                .widget(inventorySlotGroup)
                .widget(scrollableWidgetGroup)
                .bindPlayerInventory(player.inventory, 134)
                .widget(displayWidget)
                .build(this.getHolder(), player);
    }

    private void addRecipe(IRecipe recipe) {
        if (recipe != null) {
            for (int i = 0; i < this.encodingSlots; i++) {
                if (!this.recipeMap.containsKey(i)) {
                    this.encodingInventory.setStackInSlot(i, recipe.getRecipeOutput());
                    NonNullList<ItemStack> itemStacks = NonNullList.create();
                    for (int j = 0; j < this.craftingInventory.getSlots(); j++) {
                        itemStacks.add(this.craftingInventory.getStackInSlot(j));
                    }
                    this.recipeMap.put(i, new ImmutableTriple<>(recipe, RecipeUtility.mergeIngredients(recipe.getIngredients()), itemStacks));
                    this.markDirty();
                    return;
                }
            }
        }
    }

    private void removeRecipe(int slot) {
        this.recipeMap.remove(slot);
        Int2ObjectMap<Triple<IRecipe, NonNullList<CountableIngredient>, NonNullList<ItemStack>>> recipeMap = new Int2ObjectArrayMap<>();
        int i = 0;
        for (int j = 0; j < this.encodingInventory.getSlots(); j++)
            this.encodingInventory.setStackInSlot(j, ItemStack.EMPTY);
        for (Map.Entry<Integer, Triple<IRecipe, NonNullList<CountableIngredient>, NonNullList<ItemStack>>> recipeEntry : this.recipeMap.entrySet()) {
            recipeMap.put(i, recipeEntry.getValue());
            this.encodingInventory.setStackInSlot(i++, recipeEntry.getValue().getLeft().getRecipeOutput());
        }
        this.recipeMap.clear();
        this.recipeMap.putAll(recipeMap);
        this.markDirty();
    }

    private void clearCraftingResult() {
        for (int i = 0; i < this.craftingInventory.getSlots(); i++) {
            this.craftingInventory.setStackInSlot(i, ItemStack.EMPTY);
            this.inventoryCrafting.setInventorySlotContents(i, ItemStack.EMPTY);
        }
    }

    private void setCraftingResult(int index, ItemStack stack) {
        this.craftingInventory.setStackInSlot(index, stack);
        this.inventoryCrafting.setInventorySlotContents(index, stack);
        this.currentRecipe = CraftingManager.findMatchingRecipe(this.inventoryCrafting, this.getWorld());
        this.resultInventory.setStackInSlot(0, this.currentRecipe != null ? this.currentRecipe.getRecipeOutput() : ItemStack.EMPTY);
        this.markDirty();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        TJTextures.TJ_MULTIBLOCK_WORKABLE_OVERLAY.render(renderState, translation, pipeline, this.getFrontFacing(), this.recipeLogic.isActive(), this.recipeLogic.hasProblem(), this.recipeLogic.isWorkingEnabled());
        TJTextures.CRAFTER.renderSided(EnumFacingHelper.getTopFacingFrom(this.getFrontFacing()), renderState, translation, pipeline);
        Textures.PIPE_OUT_OVERLAY.renderSided(this.getOutputFacing(), renderState, translation, pipeline);
        if (this.isAutoOutputItems())
            Textures.ITEM_OUTPUT_OVERLAY.renderSided(this.getOutputFacing(), renderState, translation, pipeline);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        NBTTagList recipeList = new NBTTagList();
        for (Map.Entry<Integer, Triple<IRecipe, NonNullList<CountableIngredient>, NonNullList<ItemStack>>> recipeEntry : this.recipeMap.entrySet()) {
            NBTTagCompound recipeNBT = new NBTTagCompound();
            NBTTagList patternNBT = new NBTTagList();
            NonNullList<ItemStack> itemStacks = recipeEntry.getValue().getRight();
            for (int i = 0; i < itemStacks.size(); i++) {
                patternNBT.appendTag(itemStacks.get(i).serializeNBT());
            }
            recipeNBT.setInteger("index", recipeEntry.getKey());
            recipeNBT.setString("id", recipeEntry.getValue().getLeft().getRegistryName().toString());
            recipeNBT.setTag("craftingPattern", patternNBT);
            recipeList.appendTag(recipeNBT);
        }
        data.setTag("recipeList", recipeList);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        NBTTagList recipeList = data.getTagList("recipeList", 10);
        for (int i = 0; i < recipeList.tagCount(); i++) {
            int index = recipeList.getCompoundTagAt(i).getInteger("index");
            IRecipe recipe = CraftingManager.getRecipe(new ResourceLocation(recipeList.getCompoundTagAt(i).getString("id")));
            if (recipe != null) {
                NonNullList<ItemStack> itemStacks = NonNullList.create();
                NBTTagList patternNBT = recipeList.getCompoundTagAt(i).getTagList("craftingPattern", 10);
                for (int j = 0; j < patternNBT.tagCount(); j++) {
                    itemStacks.add(new ItemStack(patternNBT.getCompoundTagAt(j)));
                }
                this.recipeMap.put(index, new ImmutableTriple<>(recipe, RecipeUtility.mergeIngredients(recipe.getIngredients()), itemStacks));
                this.encodingInventory.setStackInSlot(index, recipe.getRecipeOutput());
            }
        }
    }

    @Override
    public Int2ObjectMap<Triple<IRecipe, NonNullList<CountableIngredient>, NonNullList<ItemStack>>> getRecipeMap() {
        return this.recipeMap;
    }

    @Override
    public int getParallel() {
        return this.parallel;
    }
}
