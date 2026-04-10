package tj.machines.multi.electric;

import gregtech.api.capability.IEnergyContainer;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.render.ICubeRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tj.builder.multicontrollers.TJRecipeMapMultiblockController;
import tj.capability.OverclockManager;
import tj.capability.impl.workable.MegaRecipeLogic;
import tj.textures.TJOrientedOverlayRenderer;
import tj.textures.TJTextures;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static gregicadditions.capabilities.GregicAdditionsCapabilities.MAINTENANCE_HATCH;
import static gregtech.api.metatileentity.multiblock.MultiblockAbility.*;

public class MetaTileEntityMegaFusion extends TJRecipeMapMultiblockController {

    private static final MultiblockAbility<?>[] ALLOWED_ABILITIES = {IMPORT_FLUIDS, EXPORT_FLUIDS, INPUT_ENERGY, MAINTENANCE_HATCH};
    private final Set<BlockPos> activeStates = new HashSet<>();
    private IEnergyContainer energyContainer;

    public MetaTileEntityMegaFusion(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, RecipeMaps.FUSION_RECIPES, true, false);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityMegaFusion(this.metaTileEntityId);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("tj.multiblock.mega_fusion.description"));
        tooltip.add(I18n.format("tj.multiblock.processing_array.eut"));
        super.addInformation(stack, player, tooltip, advanced);
    }

    @Override
    protected MegaRecipeLogic<?> createRecipeLogic() {
        return new MegaRecipeLogic<>(this);
    }

    @Override
    public void preOverclock(OverclockManager<?> overclockManager, Recipe recipe) {
        overclockManager.setParallel((int) (this.energyContainer.getEnergyCapacity() / (long) recipe.getProperty("eu_to_start")));
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return null;
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return TJTextures.FUSION_PORT_UEV;
    }

    @Override
    public TJOrientedOverlayRenderer getFrontalOverlay() {
        return TJTextures.TJ_FUSION_REACTOR_OVERLAY;
    }

    @Override
    public int getParallel() {
        return 0; // don't display parallel overclocking per tier on tooltip
    }
}
