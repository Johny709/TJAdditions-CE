package tj.machines.multi.electric;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregicadditions.Gregicality;
import gregtech.api.GregTechAPI;
import gregtech.api.block.machines.BlockMachine;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.Widget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.render.ICubeRenderer;
import gregtech.api.render.OrientedOverlayRenderer;
import gregtech.api.render.Textures;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import tj.builder.multicontrollers.TJRecipeMapMultiblockController;
import tj.capability.IProcessorProvider;
import tj.capability.OverclockManager;
import tj.gui.widgets.TJSlotWidget;
import tj.items.handlers.FilteredItemStackHandler;
import tj.textures.TJOrientedOverlayRenderer;

import javax.annotation.Nonnull;

import java.util.List;

import static gregicadditions.capabilities.GregicAdditionsCapabilities.MAINTENANCE_HATCH;
import static gregtech.api.metatileentity.multiblock.MultiblockAbility.*;

public class MetaTileEntityProcessingArray extends TJRecipeMapMultiblockController {

    private static final MultiblockAbility<?>[] ALLOWED_ABILITIES = {IMPORT_ITEMS, EXPORT_ITEMS, IMPORT_FLUIDS, EXPORT_FLUIDS, INPUT_ENERGY, MAINTENANCE_HATCH};
    private RecipeMap<?> currentRecipeMap = RecipeMaps.FURNACE_RECIPES;
    private OrientedOverlayRenderer overlayRenderer;
    private int metaId = -1;

    public MetaTileEntityProcessingArray(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, RecipeMaps.FURNACE_RECIPES);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder metaTileEntityHolder) {
        return new MetaTileEntityProcessingArray(this.metaTileEntityId);
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return new FilteredItemStackHandler(this, 1, 16)
                .setItemStackPredicate((slot, itemStack) -> !this.recipeLogic.isActive() && this.getMetaTileEntityFromStack(itemStack) != null)
                .setOnContentsChanged((slot, stack, insert) -> {
                    if (insert) {
                        MetaTileEntity metaTileEntity = this.getMetaTileEntityFromStack(stack);
                        if (!(metaTileEntity instanceof IProcessorProvider)) return;
                        this.recipeLogic.getRecipeLRUCache().clear();
                        this.currentRecipeMap = ((IProcessorProvider) metaTileEntity).getRecipeMap();
                        this.metaId = stack.getMetadata();
                    } else {
                        this.currentRecipeMap = RecipeMaps.FURNACE_RECIPES;
                        this.metaId = -1;
                    }
                    this.writeCustomData(100, buffer -> buffer.writeInt(this.metaId));
                    this.markDirty();
                });
    }

    @Override
    public void postOverclock(OverclockManager<?> overclockManager, Recipe recipe) {
        overclockManager.setEUt(overclockManager.getEUt() * this.getParallel());
    }

    @Override
    protected void mainDisplayTab(List<Widget> widgetGroup) {
        super.mainDisplayTab(widgetGroup);
        widgetGroup.add(new TJSlotWidget<>(this.importItems, 0, 174, 190)
                .setPutItemsPredicate(stack -> !this.recipeLogic.isActive() && this.getMetaTileEntityFromStack(stack) != null)
                .setTakeItemsPredicate(stack -> !this.recipeLogic.isActive())
                .setBackgroundTexture(GuiTextures.SLOT));
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("XXX", "XXX", "XXX")
                .aisle("XXX", "X#X", "XXX")
                .aisle("XXX", "XSX", "XXX")
                .where('S', this.selfPredicate())
                .where('X', statePredicate(MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.TUNGSTENSTEEL_ROBUST)).or(abilityPartPredicate(ALLOWED_ABILITIES)))
                .where('#', isAirPredicate())
                .build();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        this.getFrontOverlay().render(renderState, translation, pipeline, this.getFrontFacing(), this.recipeLogic.isActive());
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == 100) {
            this.metaId = buf.readInt();
            MetaTileEntity metaTileEntity = GregTechAPI.META_TILE_ENTITY_REGISTRY.getObjectById(this.metaId);
            if (metaTileEntity instanceof IProcessorProvider)
                this.overlayRenderer = ((IProcessorProvider) metaTileEntity).getRendererOverlay();
            else this.overlayRenderer = null;
            this.scheduleRenderUpdate();
        }
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeInt(this.metaId);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.metaId = buf.readInt();
        MetaTileEntity metaTileEntity = GregTechAPI.META_TILE_ENTITY_REGISTRY.getObjectById(this.metaId);
        if (metaTileEntity instanceof IProcessorProvider)
            this.overlayRenderer = ((IProcessorProvider) metaTileEntity).getRendererOverlay();
        else this.overlayRenderer = null;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger("metaId", this.metaId);
        data.setString("recipeMap", this.currentRecipeMap.getUnlocalizedName());
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.metaId = data.getInteger("metaId");
        this.currentRecipeMap = RecipeMap.getByName(data.getString("recipeMap"));
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.ROBUST_TUNGSTENSTEEL_CASING;
    }

    @Nonnull
    @Override
    protected OrientedOverlayRenderer getFrontOverlay() {
        return this.overlayRenderer != null ? this.overlayRenderer : Textures.MULTIBLOCK_WORKABLE_OVERLAY;
    }

    @Override
    public TJOrientedOverlayRenderer getFrontalOverlay() {
        return null;
    }

    @Override
    public RecipeMap<?> getRecipeMap() {
        return this.currentRecipeMap;
    }

    @Override
    public String getRecipeUid() {
        return Gregicality.MODID + ":" + this.currentRecipeMap.getUnlocalizedName();
    }

    @Override
    public int getParallel() {
        return this.importItems.getStackInSlot(0).getCount();
    }

    @Override
    public int getTierDifference(long recipeEUt) {
        return 1;
    }

    private MetaTileEntity getMetaTileEntityFromStack(ItemStack stack) {
        Block block = Block.getBlockFromItem(stack.getItem());
        if (!(block instanceof BlockMachine))
            return null;
        return GregTechAPI.META_TILE_ENTITY_REGISTRY.getObjectById(stack.getMetadata());
    }
}
