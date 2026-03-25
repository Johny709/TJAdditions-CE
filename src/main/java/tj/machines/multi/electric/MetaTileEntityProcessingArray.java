package tj.machines.multi.electric;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregicadditions.Gregicality;
import gregicadditions.client.ClientHandler;
import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.metal.MetalCasing2;
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
import gregtech.api.multiblock.PatternMatchContext;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.render.ICubeRenderer;
import gregtech.api.render.OrientedOverlayRenderer;
import gregtech.api.render.Textures;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import tj.builder.multicontrollers.GUIDisplayBuilder;
import tj.builder.multicontrollers.TJRecipeMapMultiblockController;
import tj.capability.IProcessorProvider;
import tj.capability.OverclockManager;
import tj.gui.widgets.TJSlotWidget;
import tj.items.handlers.FilteredItemStackHandler;
import tj.textures.TJOrientedOverlayRenderer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.List;

import static gregicadditions.capabilities.GregicAdditionsCapabilities.MAINTENANCE_HATCH;
import static gregtech.api.metatileentity.multiblock.MultiblockAbility.*;

public class MetaTileEntityProcessingArray extends TJRecipeMapMultiblockController {

    private static final MultiblockAbility<?>[] ALLOWED_ABILITIES = {IMPORT_ITEMS, EXPORT_ITEMS, IMPORT_FLUIDS, EXPORT_FLUIDS, INPUT_ENERGY, MAINTENANCE_HATCH};
    private RecipeMap<?> currentRecipeMap = RecipeMaps.FURNACE_RECIPES;
    private OrientedOverlayRenderer overlayRenderer;
    private long machineVoltage;
    private int machineTier;
    private int maxTier;
    private int metaId = -1;

    public MetaTileEntityProcessingArray(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, RecipeMaps.FURNACE_RECIPES);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder metaTileEntityHolder) {
        return new MetaTileEntityProcessingArray(this.metaTileEntityId);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("tj.multiblock.universal.tooltip.2", this.getMaxParallel()));
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return new FilteredItemStackHandler(this, 1, this.getMaxParallel())
                .setItemStackPredicate((slot, itemStack) -> !this.recipeLogic.isActive() && this.getMetaTileEntityFromStack(itemStack) instanceof IProcessorProvider)
                .setOnContentsChangedPre((slot, stack, insert) -> {
                    if (insert) {
                        MetaTileEntity metaTileEntity = this.getMetaTileEntityFromStack(stack);
                        if (!(metaTileEntity instanceof IProcessorProvider)) return;
                        this.currentRecipeMap = ((IProcessorProvider) metaTileEntity).getRecipeMap();
                        this.machineTier = Math.min(this.maxTier, ((IProcessorProvider) metaTileEntity).getMachineTier());
                        this.recipeLogic.getRecipeLRUCache().clear();
                        this.recipeLogic.invalidate();
                        this.machineVoltage = 8L << this.machineTier * 2;
                        this.maxVoltage = this.machineVoltage;
                        this.metaId = stack.getMetadata();
                        this.tier = this.machineTier;
                        this.writeCustomData(100, buffer -> buffer.writeInt(this.metaId));
                        this.markDirty();
                    }
                }).setOnContentsChangedPost((slot, stack) -> {
                    if (stack.isEmpty()) {
                        this.currentRecipeMap = RecipeMaps.FURNACE_RECIPES;
                        this.machineVoltage = 0;
                        this.machineTier = 0;
                        this.maxVoltage = 0;
                        this.metaId = -1;
                        this.tier = 0;
                        this.writeCustomData(100, buffer -> buffer.writeInt(this.metaId));
                    }
                });
    }

    @Override
    public void preOverclock(OverclockManager<?> overclockManager, Recipe recipe) {
        overclockManager.setParallel(this.getParallel());
    }

    @Override
    public void postOverclock(OverclockManager<?> overclockManager, Recipe recipe) {
        overclockManager.setEUt(overclockManager.getEUt() * this.getParallel());
    }

    @Override
    protected void mainDisplayTab(List<Widget> widgetGroup) {
        super.mainDisplayTab(widgetGroup);
        widgetGroup.add(new TJSlotWidget<>(this.importItems, 0, 174, 190)
                .setPutItemsPredicate(stack -> !this.recipeLogic.isActive() && this.getMetaTileEntityFromStack(stack) instanceof IProcessorProvider)
                .setTakeItemsPredicate(stack -> !this.recipeLogic.isActive())
                .setBackgroundTexture(GuiTextures.SLOT));
    }

    @Override
    protected void addDisplayText(GUIDisplayBuilder builder) {
        super.addDisplayText(builder);
        if (!this.isStructureFormed()) return;
        builder.addTranslationLine(1, "gregtech.multiblock.universal.framework", 8L << this.machineTier * 2)
                .addTextComponent(new TextComponentTranslation("gregtech.multiblock.recipe", new TextComponentTranslation("recipemap." + this.getRecipeMap().getUnlocalizedName() + ".name")
                        .setStyle(new Style().setColor(TextFormatting.AQUA))));
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("XXX", "XXX", "XXX")
                .aisle("XXX", "XFX", "XXX")
                .aisle("XXX", "XSX", "XXX")
                .setAmountAtLeast('L', 5)
                .where('S', this.selfPredicate())
                .where('L', statePredicate(this.getCasingState()))
                .where('F', frameworkPredicate())
                .where('X', statePredicate(this.getCasingState()).or(abilityPartPredicate(ALLOWED_ABILITIES)))
                .where('#', isAirPredicate())
                .build();
    }

    public IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.TUNGSTENSTEEL_ROBUST);
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        this.maxTier = context.getOrDefault("frameworkTier", 0);
        this.machineTier = Math.min(this.machineTier, this.maxTier);
        this.machineVoltage = Math.min(this.machineVoltage, 8L << this.machineTier * 2);
        this.maxVoltage = this.machineVoltage;
        this.tier = this.machineTier;
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
        data.setInteger("machineTier", this.machineTier);
        data.setLong("machineVoltage", this.machineVoltage);
        data.setString("recipeMap", this.currentRecipeMap.getUnlocalizedName());
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.metaId = data.getInteger("metaId");
        this.machineTier = data.getInteger("machineTier");
        this.machineVoltage = data.getInteger("machineVoltage");
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

    public ItemStack getCasingItem() {
        return MetaBlocks.METAL_CASING.getItemVariant(BlockMetalCasing.MetalCasingType.TUNGSTENSTEEL_ROBUST);
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

    public int getMaxParallel() {
        return 16;
    }

    @Override
    public int getTier() {
        return this.machineTier;
    }

    @Override
    public long getMaxVoltage() {
        return this.machineVoltage;
    }

    private MetaTileEntity getMetaTileEntityFromStack(ItemStack stack) {
        Block block = Block.getBlockFromItem(stack.getItem());
        if (!(block instanceof BlockMachine))
            return null;
        return GregTechAPI.META_TILE_ENTITY_REGISTRY.getObjectById(stack.getMetadata());
    }

    public static class MetaTileEntityAdvancedProcessingArray extends MetaTileEntityProcessingArray {

        public MetaTileEntityAdvancedProcessingArray(ResourceLocation metaTileEntityId) {
            super(metaTileEntityId);
        }

        @Override
        public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder metaTileEntityHolder) {
            return new MetaTileEntityAdvancedProcessingArray(this.metaTileEntityId);
        }

        @Override
        public IBlockState getCasingState() {
            return GAMetaBlocks.METAL_CASING_2.getState(MetalCasing2.CasingType.HSS_G);
        }

        @Override
        public ItemStack getCasingItem() {
            return GAMetaBlocks.METAL_CASING_2.getItemVariant(MetalCasing2.CasingType.HSS_G);
        }

        @Override
        public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
            return ClientHandler.HSS_G_CASING;
        }

        @Override
        public int getMaxParallel() {
            return 64;
        }
    }

    public static class MetaTileEntitySuperProcessingArray extends MetaTileEntityProcessingArray {

        public MetaTileEntitySuperProcessingArray(ResourceLocation metaTileEntityId) {
            super(metaTileEntityId);
        }

        @Override
        public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder metaTileEntityHolder) {
            return new MetaTileEntitySuperProcessingArray(this.metaTileEntityId);
        }

        @Override
        public IBlockState getCasingState() {
            return GAMetaBlocks.METAL_CASING_2.getState(MetalCasing2.CasingType.HSS_S);
        }

        @Override
        public ItemStack getCasingItem() {
            return GAMetaBlocks.METAL_CASING_2.getItemVariant(MetalCasing2.CasingType.HSS_S);
        }

        @Override
        public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
            return ClientHandler.HSS_S_CASING;
        }

        @Override
        public int getMaxParallel() {
            return 256;
        }
    }
}
