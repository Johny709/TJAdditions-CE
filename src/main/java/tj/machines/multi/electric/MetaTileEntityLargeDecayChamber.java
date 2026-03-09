package tj.machines.multi.electric;

import gregicadditions.capabilities.GregicAdditionsCapabilities;
import gregicadditions.client.ClientHandler;
import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.components.FieldGenCasing;
import gregicadditions.item.metal.MetalCasing2;
import gregicadditions.machines.multi.nuclear.MetaTileEntityNuclearReactor;
import gregicadditions.machines.multi.simple.LargeSimpleRecipeMapMultiblockController;
import gregicadditions.recipes.GARecipeMaps;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.api.multiblock.PatternMatchContext;
import gregtech.api.render.ICubeRenderer;
import gregtech.api.render.OrientedOverlayRenderer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tj.TJConfig;
import tj.builder.multicontrollers.TJRecipeMapMultiblockController;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class MetaTileEntityLargeDecayChamber extends TJRecipeMapMultiblockController {

    private static final MultiblockAbility<?>[] ALLOWED_ABILITIES = {MultiblockAbility.IMPORT_ITEMS, MultiblockAbility.EXPORT_ITEMS, MultiblockAbility.IMPORT_FLUIDS, MultiblockAbility.EXPORT_FLUIDS, MultiblockAbility.INPUT_ENERGY, GregicAdditionsCapabilities.MAINTENANCE_HATCH};

    public MetaTileEntityLargeDecayChamber(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, GARecipeMaps.DECAY_CHAMBERS_RECIPES);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityLargeDecayChamber(this.metaTileEntityId);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("tj.multiblock.large_decay_chamber.description"));
        super.addInformation(stack, player, tooltip, advanced);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("~~C~~", "~HHH~", "CHHHC", "~HHH~", "~~C~~")
                .aisle("~HHH~", "C###C", "C#F#C", "C###C", "~HHH~")
                .aisle("CHHHC", "C#F#C", "CFRFC", "C#F#C", "CHHHC")
                .aisle("~HHH~", "C###C", "C#F#C", "C###C", "~HHH~")
                .aisle("~~C~~", "~HHH~", "CHSHC", "~HHH~", "~~C~~")
                .setAmountAtLeast('L', 24)
                .where('S', selfPredicate())
                .where('L', statePredicate(this.getCasingState()))
                .where('C', statePredicate(this.getCasingState()))
                .where('H', statePredicate(this.getCasingState()).or(abilityPartPredicate(ALLOWED_ABILITIES)))
                .where('R', MetaTileEntityNuclearReactor.heatingCoilPredicate())
                .where('F', LargeSimpleRecipeMapMultiblockController.fieldGenPredicate())
                .where('#', isAirPredicate())
                .where('~', (tile) -> true)
                .build();
    }

    private IBlockState getCasingState() {
        return GAMetaBlocks.METAL_CASING_2.getState(MetalCasing2.CasingType.LEAD);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return ClientHandler.LEAD_CASING;
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        this.tier = context.getOrDefault("FieldGen", FieldGenCasing.CasingType.FIELD_GENERATOR_LV).getTier();
        this.maxVoltage = 8L << this.tier * 2;
    }

    @Nonnull
    @Override
    protected OrientedOverlayRenderer getFrontOverlay() {
        return ClientHandler.REPLICATOR_OVERLAY;
    }

    @Override
    public int getEUtMultiplier() {
        return TJConfig.largeDecayChamber.eutPercentage;
    }

    @Override
    public int getDurationMultiplier() {
        return TJConfig.largeDecayChamber.durationPercentage;
    }

    @Override
    public int getChanceMultiplier() {
        return TJConfig.largeDecayChamber.chancePercentage;
    }

    @Override
    public int getParallel() {
        return TJConfig.largeDecayChamber.stack;
    }

}
