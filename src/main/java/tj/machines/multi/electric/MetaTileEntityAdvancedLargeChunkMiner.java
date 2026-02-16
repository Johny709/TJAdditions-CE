package tj.machines.multi.electric;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregicadditions.GAMaterials;
import gregicadditions.capabilities.GregicAdditionsCapabilities;
import gregicadditions.client.ClientHandler;
import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.metal.MetalCasing2;
import gregtech.api.metatileentity.MTETrait;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.api.multiblock.PatternMatchContext;
import gregtech.api.render.ICubeRenderer;
import gregtech.api.unification.material.Materials;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tj.blocks.BlockSolidCasings;
import tj.blocks.TJMetaBlocks;
import tj.builder.multicontrollers.TJMultiblockControllerBase;
import tj.builder.multicontrollers.UIDisplayBuilder;
import tj.capability.impl.workable.MinerWorkableHandler;
import tj.textures.TJTextures;
import tj.util.EnumFacingHelper;

import javax.annotation.Nullable;
import java.util.List;


public class MetaTileEntityAdvancedLargeChunkMiner extends TJMultiblockControllerBase {

    private static final MultiblockAbility<?>[] ALLOWED_ABILITIES = {MultiblockAbility.IMPORT_ITEMS, MultiblockAbility.EXPORT_ITEMS, MultiblockAbility.IMPORT_FLUIDS, MultiblockAbility.EXPORT_FLUIDS, MultiblockAbility.INPUT_ENERGY, GregicAdditionsCapabilities.MAINTENANCE_HATCH};
    private final MinerWorkableHandler workableHandler = new MinerWorkableHandler(this);
    private final int tier;

    public MetaTileEntityAdvancedLargeChunkMiner(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId);
        this.tier = tier;
        this.reinitializeStructurePattern();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityAdvancedLargeChunkMiner(this.metaTileEntityId, this.tier);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("tj.multiblock.advanced_large_miner.description"));
        tooltip.add(I18n.format("gtadditions.machine.miner.multi.description2", this.getTier(), this.getTier(), 1));
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
    protected void addDisplayText(UIDisplayBuilder builder) {
        super.addDisplayText(builder);
        if (!this.isStructureFormed()) return;
        builder.voltageInLine(this.getInputEnergyContainer())
                .addTranslationLine("tj.multiblock.advanced_large_miner.chunk_index", this.workableHandler.getChunkIndex(), this.workableHandler.getChunkSize())
                .addRecipeOutputLine(this.workableHandler, 1000);
        if (this.workableHandler.isActive())
            builder.addTranslationLine("metaitem.linking.device.x", this.workableHandler.getX())
                    .addTranslationLine("metaitem.linking.device.y", this.workableHandler.getY())
                    .addTranslationLine("metaitem.linking.device.z", this.workableHandler.getZ())
                    .isWorkingLine(this.workableHandler.isWorkingEnabled(), this.workableHandler.isActive(), this.workableHandler.getProgress(), this.workableHandler.getMaxProgress());
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return this.tier == 0 ? null : FactoryBlockPattern.start()
                .aisle("F~~~F", "F~~~F", "CCCCC", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~")
                .aisle("~~~~~", "~~~~~", "CCCCC", "~XXX~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~")
                .aisle("~~~~~", "~~~~~", "CCCCC", "~XXX~", "~FFF~", "~FFF~", "~FFF~", "~~F~~", "~~F~~", "~~F~~")
                .aisle("~~~~~", "~~~~~", "CCCCC", "~XSX~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~")
                .aisle("F~~~F", "F~~~F", "CCCCC", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~")
                .where('S', this.selfPredicate())
                .where('C', statePredicate(this.getCasingState()))
                .where('X', statePredicate(this.getCasingState()).or(abilityPartPredicate(ALLOWED_ABILITIES)))
                .where('F', statePredicate(this.getFrameState()))
                .where('~', tile -> true)
                .build();
    }

    public IBlockState getCasingState() {
        switch (this.tier) {
            case 5: return GAMetaBlocks.METAL_CASING_2.getState(MetalCasing2.CasingType.HSS_G);
            case 6: return GAMetaBlocks.METAL_CASING_2.getState(MetalCasing2.CasingType.HSS_S);
            case 7: return TJMetaBlocks.SOLID_CASING.getState(BlockSolidCasings.SolidCasingType.DURANIUM_CASING);
            case 8: return TJMetaBlocks.SOLID_CASING.getState(BlockSolidCasings.SolidCasingType.SEABORGIUM_CASING);
            default: return GAMetaBlocks.METAL_CASING_2.getState(MetalCasing2.CasingType.BLACK_STEEL);
        }
    }

    public IBlockState getFrameState() {
        switch (this.tier) {
            case 5: return MetaBlocks.FRAMES.get(Materials.HSSG).getDefaultState();
            case 6: return MetaBlocks.FRAMES.get(Materials.HSSS).getDefaultState();
            case 7: return MetaBlocks.FRAMES.get(Materials.Duranium).getDefaultState();
            case 8: return MetaBlocks.FRAMES.get(GAMaterials.Seaborgium).getDefaultState();
            default: return MetaBlocks.FRAMES.get(Materials.BlackSteel).getDefaultState();
        }
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        this.workableHandler.initialize(this.getAbilities(MultiblockAbility.IMPORT_ITEMS).size());
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        switch (this.tier) {
            case 5: return ClientHandler.HSS_G_CASING;
            case 6: return ClientHandler.HSS_S_CASING;
            case 7: return TJTextures.DURANIUM;
            case 8: return TJTextures.SEABORGIUM;
            default: return ClientHandler.BLACK_STEEL_CASING;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        TJTextures.TJ_MULTIBLOCK_WORKABLE_OVERLAY.render(renderState, translation, pipeline, this.getFrontFacing(), this.workableHandler.isActive(), this.workableHandler.hasProblem(), this.workableHandler.isWorkingEnabled());
        ClientHandler.CHUNK_MINER_OVERLAY.renderSided(EnumFacingHelper.getLeftFacingFrom(this.getFrontFacing()), renderState, translation, pipeline);
        ClientHandler.CHUNK_MINER_OVERLAY.renderSided(EnumFacingHelper.getRightFacingFromSpin(this.getFrontFacing()), renderState, translation, pipeline);
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
    public int getTier() {
        return this.tier;
    }
}
