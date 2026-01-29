package tj.multiblockpart.utility;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregicadditions.machines.multi.multiblockpart.GAMetaTileEntityMultiblockPart;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.*;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.recipes.CountableIngredient;
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
import net.minecraftforge.items.ItemStackHandler;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import tj.capability.impl.handler.IRecipeMapProvider;
import tj.builder.RecipeUtility;
import tj.gui.widgets.impl.SlotScrollableWidgetGroup;
import tj.gui.widgets.impl.CraftingRecipeTransferWidget;
import tj.gui.widgets.impl.SlotDisplayWidget;
import tj.multiblockpart.TJMultiblockAbility;
import tj.textures.TJTextures;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

import static gregtech.api.gui.GuiTextures.*;
import static tj.gui.TJGuiTextures.DARKENED_SLOT;


public class MetaTileEntityCrafterHatch extends GAMetaTileEntityMultiblockPart implements IMultiblockAbilityPart<IRecipeMapProvider>, IRecipeMapProvider {

    private final InventoryCrafting inventoryCrafting = new InventoryCrafting(new DummyContainer(), 3, 3);
    private final ItemStackHandler craftingInventory = new ItemStackHandler(9);
    private final ItemStackHandler encodingInventory;
    private final ItemStackHandler resultInventory = new ItemStackHandler(1);
    private final Int2ObjectMap<Triple<IRecipe, NonNullList<CountableIngredient>, NonNullList<ItemStack>>> recipeMap = new Int2ObjectOpenHashMap<>();
    private final int encodingSlots;
    private Runnable clearRecipeCache;
    private IRecipe currentRecipe;

    public MetaTileEntityCrafterHatch(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
        this.encodingSlots = 6 + (tier * 3);
        this.encodingInventory = new ItemStackHandler(this.encodingSlots);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityCrafterHatch(this.metaTileEntityId, this.getTier());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("tj.machine.crafter_hatch.description"));
        tooltip.add(I18n.format("tj.multiblock.large_crafter.slots", this.encodingSlots));
        tooltip.add(I18n.format("tj.machine.crafter.tooltip"));
    }

    @Override
    protected ModularUI createUI(EntityPlayer player) {
        WidgetGroup craftingSlotGroup = new WidgetGroup(new Position(7, 14));
        SlotScrollableWidgetGroup scrollableWidgetGroup = new SlotScrollableWidgetGroup(109, 14, 64, 54, 3);
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
                            if (this.clearRecipeCache != null)
                                this.clearRecipeCache.run();
                            this.markDirty();
                        }
                    }));
        }
        return ModularUI.builder(BACKGROUND, 176, 156)
                .widget(new LabelWidget(7, 5, this.getMetaFullName()))
                .widget(new ImageWidget(72, 28, 26, 26, SLOT))
                .widget(new ImageWidget(109, 14, 54, 54, DARKENED_SLOT))
                .widget(new SlotDisplayWidget(this.resultInventory, 0, 76, 32)
                        .onPressedConsumer((button, slot, stack) -> this.addRecipe(this.currentRecipe)))
                .widget(new ClickButtonWidget(62, 14, 8, 8, "", (clickData) -> {
                    this.clearCraftingResult();
                    this.setCraftingResult(0, ItemStack.EMPTY);
                }).setButtonTexture(BUTTON_CLEAR_GRID))
                .widget(new CraftingRecipeTransferWidget(this::setCraftingResult))
                .widget(craftingSlotGroup)
                .widget(scrollableWidgetGroup)
                .bindPlayerInventory(player.inventory, 74)
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
        TJTextures.CRAFTER.renderSided(this.getFrontFacing(), renderState, translation, pipeline);
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
    public MultiblockAbility<IRecipeMapProvider> getAbility() {
        return TJMultiblockAbility.CRAFTER;
    }

    @Override
    public void registerAbilities(List<IRecipeMapProvider> list) {
        list.add(this);
    }

    @Override
    public void addToMultiBlock(MultiblockControllerBase controller) {
        super.addToMultiBlock(controller);
        if (controller instanceof IRecipeMapProvider)
            this.clearRecipeCache = ((IRecipeMapProvider) controller)::clearRecipeCache;
    }

    @Override
    public void removeFromMultiBlock(MultiblockControllerBase controller) {
        super.removeFromMultiBlock(controller);
        this.clearRecipeCache = null;
    }
}
