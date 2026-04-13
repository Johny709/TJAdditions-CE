package tj.machines.multi.electric;

import gregicadditions.GAMaterials;
import gregicadditions.capabilities.GregicAdditionsCapabilities;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.api.recipes.Recipe;
import gregtech.api.render.ICubeRenderer;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tj.TJRecipeMaps;
import tj.builder.multicontrollers.TJRecipeMapMultiblockController;
import tj.capability.OverclockManager;
import tj.textures.TJTextures;

import javax.annotation.Nullable;
import java.util.List;

import static gregtech.api.unification.material.type.Material.MATERIAL_REGISTRY;


public class MetaTileEntityChaosReplicator extends TJRecipeMapMultiblockController {

    private static final MultiblockAbility<?>[] ALLOWED_ABILITIES = new MultiblockAbility[]{MultiblockAbility.IMPORT_FLUIDS, MultiblockAbility.EXPORT_ITEMS, MultiblockAbility.IMPORT_ITEMS, MultiblockAbility.INPUT_ENERGY, GregicAdditionsCapabilities.MAINTENANCE_HATCH};

    public MetaTileEntityChaosReplicator(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, TJRecipeMaps.CHAOS_REPLICATOR_RECIPES);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityChaosReplicator(this.metaTileEntityId);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("tj.multiblock.temporary"));
        super.addInformation(stack, player, tooltip, advanced);
    }

    @Override
    public void preOverclock(OverclockManager<?> overclockManager, Recipe recipe) {
        overclockManager.setParallel(1);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("CCCCCCC", "CXXXXXC", "CQQCQQC", "CQQCQQC", "CQQCQQC", "CQQQQQC", "CQQQQQC", "CXXXXXC", "CCCCCCC")
                .aisle("CCCCCCC", "XDDDDDX", "QF###FQ", "QF###FQ", "QF#A#FQ", "QF###FQ", "QF###FQ", "XDDDDDX", "CCCCCCC")
                .aisle("CCCCCCC", "XDDDDDX", "Q#DDD#Q", "Q#####Q", "Q#####Q", "Q#####Q", "Q#DDD#Q", "XDDDDDX", "CCCCCCC")
                .aisle("CCCCCCC", "XDDDDDX", "C#DDD#C", "C##D##C", "CA#R#AC", "Q##D##Q", "Q#DDD#Q", "XDDDDDX", "CCCCCCC")
                .aisle("CCCCCCC", "XDDDDDX", "Q#DDD#Q", "Q#####Q", "Q#####Q", "Q#####Q", "Q#DDD#Q", "XDDDDDX", "CCCCCCC")
                .aisle("CCCCCCC", "XDDDDDX", "QF###FQ", "QF###FQ", "QF#A#FQ", "QF###FQ", "QF###FQ", "XDDDDDX", "CCCCCCC")
                .aisle("CCCCCCC", "CXXSXXC", "CQQCQQC", "CQQCQQC", "CQQCQQC", "CQQQQQC", "CQQQQQC", "CXXXXXC", "CCCCCCC")
                .where('S', this.selfPredicate())
                .where('C', blockPredicate(this.getCasingState()))
                .where('X', blockPredicate(this.getCasingState()).or(abilityPartPredicate(ALLOWED_ABILITIES)))
                .where('F', statePredicate(MetaBlocks.FRAMES.get(GAMaterials.EnrichedNaquadahAlloy).getDefaultState()))
                .where('R', statePredicate(MetaBlocks.FRAMES.get(MATERIAL_REGISTRY.getObject("chaos")).getDefaultState()))
                .where('D', blockPredicate(Block.getBlockFromName("draconicevolution:infused_obsidian")))
                .where('Q', blockPredicate(Block.getBlockFromName("enderio:block_fused_quartz")))
                .where('A', blockPredicate(Block.getBlockFromName("draconicevolution:draconic_block")))
                .where('#', isAirPredicate())
                .build();
    }

    private Block getCasingState() {
        return Block.getBlockFromName("contenttweaker:chaoticcasing");
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return TJTextures.CHOATIC;
    }

    @Override
    public boolean renderTJLogoOverlay() {
        return true;
    }

    @Override
    public int getParallel() {
        return 0; // don't display parallel overclocking per tier on tooltip
    }
}
