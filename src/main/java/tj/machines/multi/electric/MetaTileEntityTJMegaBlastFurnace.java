package tj.machines.multi.electric;

import gregicadditions.GAUtility;
import gregicadditions.GAValues;
import gregicadditions.capabilities.GregicAdditionsCapabilities;
import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.GATransparentCasing;
import gregtech.api.GTValues;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.api.multiblock.PatternMatchContext;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.recipeproperties.BlastTemperatureProperty;
import gregtech.api.render.ICubeRenderer;
import gregtech.api.render.Textures;
import gregtech.api.unification.material.Materials;
import gregtech.common.blocks.BlockBoilerCasing;
import gregtech.common.blocks.BlockFireboxCasing;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tj.builder.multicontrollers.GUIDisplayBuilder;
import tj.builder.multicontrollers.TJRecipeMapMultiblockController;
import tj.capability.OverclockManager;

import javax.annotation.Nullable;
import java.util.List;

public class MetaTileEntityTJMegaBlastFurnace extends TJRecipeMapMultiblockController {

    private static final MultiblockAbility<?>[] ALLOWED_ABILITIES = {
            MultiblockAbility.IMPORT_ITEMS, MultiblockAbility.EXPORT_ITEMS,
            MultiblockAbility.IMPORT_FLUIDS, MultiblockAbility.INPUT_ENERGY,
            GregicAdditionsCapabilities.MAINTENANCE_HATCH, MultiblockAbility.EXPORT_FLUIDS
    };

    private int blastFurnaceTemperature;
    private int bonusTemperature;

    public MetaTileEntityTJMegaBlastFurnace(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, RecipeMaps.BLAST_RECIPES);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityTJMegaBlastFurnace(this.metaTileEntityId);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("tj.multiblock.temporary"));
        tooltip.add(I18n.format("tj.multiblock.mega"));
        super.addInformation(stack, player, tooltip, advanced);
    }

    @Override
    public void preOverclock(OverclockManager<?> overclockManager, Recipe recipe) {
        overclockManager.setParallel(1 << overclockManager.getParallel() * 2);
        long recipeEUt = overclockManager.getEUt() * 4;
        int duration = overclockManager.getDuration();
        int heat = this.blastFurnaceTemperature - recipe.getRecipePropertyStorage().getRecipePropertyValue(BlastTemperatureProperty.getInstance(), 0);
        // Apply EUt discount for every 900K above the base recipe temperature
        recipeEUt *= (long) Math.pow(0.95, heat / 900D);
        while (duration > 1 && recipeEUt <= this.maxVoltage) {
            if (heat < 1800) break;
            heat -= 1800;
            duration /= 4;
            recipeEUt *= 4;
        }
        overclockManager.setEUt(recipeEUt / 4);
        overclockManager.setDuration(duration);
    }

    @Override
    protected void addDisplayText(GUIDisplayBuilder builder) {
        super.addDisplayText(builder);
        if (!this.isStructureFormed()) return;
        builder.addTranslationLine("gregtech.multiblock.blast_furnace.max_temperature", this.blastFurnaceTemperature)
                .addTranslationLine("gtadditions.multiblock.blast_furnace.additional_temperature", this.bonusTemperature);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("~~~~~XXXXX~~~~~", "~~~~~XXGXX~~~~~", "~~~~~XXXXX~~~~~", "~~~~~XXPXX~~~~~", "~~~~~~~P~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~")
                .aisle("~~~XXXBBBXXX~~~", "~~~XGCCFCCGX~~~", "~~~XXCCPCCXX~~~", "~~~XPCCPCCPX~~~", "~~~~PPPPPPP~~~~", "~LLLLLLLLLLLLL~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~")
                .aisle("~~XXBBXBXBBXX~~", "~~GXF#C#C#FXG~~", "~~XCP#C#C#PCX~~", "~~PCPCCCCCPCP~~", "~~PPP#####PPP~~", "~LLLLLLLLLLLLL~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~")
                .aisle("~XXXBBXBXBBXXX~", "~XCC##C#C##XXX~", "~XCC##C#C##CCX~", "~XCCCCCCCCCCCX~", "~~P#########P~~", "~LL#########LL~", "~~~~~~RRR~~~~~~", "~~~~~~~R~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~RRR~~~~~~")
                .aisle("~XBBXBBXBBXBBX~", "~XF#C##C##C#FX~", "~XP#C##C##C#PX~", "~PPCCCCCCCCCPP~", "~PP####R####PP~", "~LL####R####LL~", "~~~~RRRRRRR~~~~", "~~~~~~~U~~~~~~~", "~~~~~~~U~~~~~~~", "~~~~~~~U~~~~~~~", "~~~~~~~U~~~~~~~", "~~~~~~~U~~~~~~~", "~~~~~~~U~~~~~~~", "~~~~~~~U~~~~~~~", "~~~~RRRRRRR~~~~")
                .aisle("XXBBBXBXBXBBBXX", "XX###C#C#C###XX", "XC###C#C#C###CX", "XCCCCCCCCCCCCCX", "~P###R###R###P~", "~LL##R###R##LL~", "~~~~RRRRRRR~~~~", "~~~~~ccccc~~~~~", "~~~~~ccccc~~~~~", "~~~~~ccccc~~~~~", "~~~~~ccccc~~~~~", "~~~~~ccccc~~~~~", "~~~~~ccccc~~~~~", "~~~~~ccccc~~~~~", "~~~~RRRRRRR~~~~")
                .aisle("XBXXBBXXXBBXXBX", "X#CC##CCC##CC#X", "X#CC##CCC##CC#X", "XCCCCCCCCCCCCCX", "~P###########P~", "~LL#########CC~", "~~~RRRRRRRRR~~~", "~~~~~c~~~c~~~~~", "~~~~~c~~~c~~~~~", "~~~~~c~~~c~~~~~", "~~~~~c~~~c~~~~~", "~~~~~c~~~c~~~~~", "~~~~~c~~~c~~~~~", "~~~~~c~~~c~~~~~", "~~~RRRRRRRRR~~~")
                .aisle("XBBBXXXfXXXBBBX", "GF##CCCfCCC##FG", "XP##CCCfCCC##PX", "PPCCCCCfCCCCCPP", "PP##R##f##R##PP", "~LL#R##f##R#CP~", "~~~RRRRRRRRR~P~", "~~~RUc~~~cPPPP~", "~~~~Uc~~~cP~~~~", "~~~~Uc~~~cP~~~~", "~~~~Uc~~~cP~~~~", "~~~~Uc~~~cP~~~~", "~~~~Uc~~~cP~~~~", "~~~~Uc~~~cP~~~~", "~~~RRRRmPPPR~~~")
                .aisle("XBXXBBXXXBBXXBX", "X#CC##CCC##CC#X", "X#CC##CCC##CC#X", "XCCCCCCCCCCCCCX", "~P###########P~", "~LL#########CC~", "~~~RRRRRRRRR~~~", "~~~~~c~~~c~~~~~", "~~~~~c~~~c~~~~~", "~~~~~c~~~c~~~~~", "~~~~~c~~~c~~~~~", "~~~~~c~~~c~~~~~", "~~~~~c~~~c~~~~~", "~~~~~c~~~c~~~~~", "~~~RRRRRRRRR~~~")
                .aisle("XXBBBXBXBXBBBXX", "XC###C#C#C###CX", "XC###C#C#C###CX", "XCCCCCCCCCCCCCX", "~P###R###R###P~", "~LL##R###R##LL~", "~~~~RRRRRRR~~~~", "~~~~~ccccc~~~~~", "~~~~~ccccc~~~~~", "~~~~~ccccc~~~~~", "~~~~~ccccc~~~~~", "~~~~~ccccc~~~~~", "~~~~~ccccc~~~~~", "~~~~~ccccc~~~~~", "~~~~RRRRRRR~~~~")
                .aisle("~XBBXBBXBBXBBX~", "~XF#C##C##C#FX~", "~XP#C##C##C#PX~", "~PPCCCCCCCCCPP~", "~PP####R####PP~", "~LL####R####LL~", "~~~~RRRRRRR~~~~", "~~~~~~~U~~~~~~~", "~~~~~~~U~~~~~~~", "~~~~~~~U~~~~~~~", "~~~~~~~U~~~~~~~", "~~~~~~~U~~~~~~~", "~~~~~~~U~~~~~~~", "~~~~~~~U~~~~~~~", "~~~~RRRRRRR~~~~")
                .aisle("~XXXBBXPXBBXXX~", "~XCC##CPC##CCX~", "~XCC##CPC##CCX~", "~XCCCCCPCCCCCX~", "~~P###PP####P~~", "~LL#########LL~", "~~~~~~RRR~~~~~~", "~~~~~~~R~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~RRR~~~~~~")
                .aisle("~~XXBBXGXBBXX~~", "~~GCF#CGC#FCG~~", "~~XCP#CGC#PCX~~", "~~PCPCCGCCPCP~~", "~~PPPPPP#PPPP~~", "~LLLLLLLLLLLLL~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~")
                .aisle("~L~XXXXGXXXX~~~", "~L~XGCCGCCGX~~~", "~L~XXCCGCCXX~~~", "~L~XPCCGCCPX~~~", "~L~~PP~~~PP~~~~", "~LLLLLLLLLLLLL~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~")
                .aisle("~~~~~XXSXX~~~~~", "~~~~~XXGXX~~~~~", "~~~~~XXGXX~~~~~", "~~~~~XXGXX~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~")
                .where('S', this.selfPredicate())
                .where('C', statePredicate(this.getCasingState()))
                .where('c', coilPredicate())
                .where('f', frameworkPredicate())
                .where('X', statePredicate(this.getCasingState()).or(abilityPartPredicate(ALLOWED_ABILITIES)))
                .where('m', abilityPartPredicate(GregicAdditionsCapabilities.MUFFLER_HATCH))
                .where('B', statePredicate(MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.PRIMITIVE_BRICKS)))
                .where('G', statePredicate(GAMetaBlocks.TRANSPARENT_CASING.getState(GATransparentCasing.CasingType.OSMIRIDIUM_GLASS)))
                .where('F', statePredicate(MetaBlocks.BOILER_FIREBOX_CASING.getState(BlockFireboxCasing.FireboxCasingType.TUNGSTENSTEEL_FIREBOX)))
                .where('P', statePredicate(MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.TUNGSTENSTEEL_PIPE)))
                .where('R', statePredicate(MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.TUNGSTENSTEEL_ROBUST)))
                .where('L', statePredicate(MetaBlocks.FRAMES.get(Materials.BlackSteel).getDefaultState()))
                .where('U', statePredicate(MetaBlocks.FRAMES.get(Materials.BlueSteel).getDefaultState()))
                .where('#', isAirPredicate())
                .where('~', tile -> true)
                .build();
    }

    private IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.INVAR_HEATPROOF);
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        int tier = context.getOrDefault("frameworkTier", 0);
        this.blastFurnaceTemperature = context.getOrDefault("coilTemperature", 0);
        int energyTier = GAUtility.getTierByVoltage(this.getInputEnergyContainer().getInputVoltage());
        this.bonusTemperature = Math.max(0, 100 * Math.min(GAUtility.getTierByVoltage(this.maxVoltage), energyTier - 2));
        this.blastFurnaceTemperature += this.bonusTemperature;
        if (tier < GAValues.MAX) {
            this.maxVoltage = 8L << tier * 2;
            this.tier = tier;
        }
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.blastFurnaceTemperature = 0;
        this.bonusTemperature = 0;
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return sourcePart instanceof IMultiblockAbilityPart<?> && ((IMultiblockAbilityPart<?>) sourcePart).getAbility() == GregicAdditionsCapabilities.MUFFLER_HATCH ? Textures.ROBUST_TUNGSTENSTEEL_CASING : Textures.HEAT_PROOF_CASING;
    }

    @Override
    public int getParallel() {
        return Math.max(0, this.tier - GTValues.LuV);
    }

    @Override
    public boolean renderTJLogoOverlay() {
        return true;
    }
}
