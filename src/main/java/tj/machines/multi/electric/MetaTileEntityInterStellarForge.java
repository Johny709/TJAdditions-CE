package tj.machines.multi.electric;

import gregicadditions.GAValues;
import gregicadditions.client.ClientHandler;
import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.components.EmitterCasing;
import gregicadditions.item.metal.MetalCasing2;
import gregicadditions.recipes.GARecipeMaps;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.api.multiblock.PatternMatchContext;
import gregtech.api.render.ICubeRenderer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import tj.TJConfig;
import tj.blocks.BlockFusionCasings;
import tj.blocks.BlockFusionGlass;
import tj.blocks.BlockSolidCasings;
import tj.blocks.TJMetaBlocks;
import tj.builder.multicontrollers.TJRecipeMapMultiblockController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static gregicadditions.capabilities.GregicAdditionsCapabilities.MAINTENANCE_HATCH;
import static gregtech.api.metatileentity.multiblock.MultiblockAbility.*;
import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;

public class MetaTileEntityInterStellarForge extends TJRecipeMapMultiblockController {

    private static final MultiblockAbility<?>[] ALLOWED_ABILITIES = {IMPORT_ITEMS, IMPORT_FLUIDS, EXPORT_ITEMS, INPUT_ENERGY, MAINTENANCE_HATCH};

    public MetaTileEntityInterStellarForge(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, GARecipeMaps.STELLAR_FORGE_RECIPES);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityInterStellarForge(this.metaTileEntityId);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        final List<String[]> pattern = new ArrayList<>();
        final FactoryBlockPattern factoryPattern = FactoryBlockPattern.start(LEFT, FRONT, DOWN);
        pattern.add(new String[]{"~~~~~~~~CCCCCCCCCCC~~~~~~~~", "~~~~~CCCCCCCCCCCCCCCCC~~~~~", "~~~~CCCCCCCCCCCCCCCCCCC~~~~", "~~~CCCCCCC~CC~CC~CCCCCCC~~~", "~~CCCCC~~~~CC~CC~~~~CCCCC~~", "~CCCCC~~~~~~~~~~~~~~~CCCCC~", "~CCCC~~~~~~~~~~~~~~~~~CCCC~", "~CCC~~~~~~~CC~CC~~~~~~~CCC~", "CCCC~~~~~~~CC~CC~~~~~~~CCCC", "CCCC~~~~~~~~~~~~~~~~~~~CCCC", "CCC~~~~~~~~~~~~~~~~~~~~~CCC", "CCCCC~~CC~~~~~~~~~CC~~CCCCC", "CCCCC~~CC~~~~~~~~~CC~~CCCCC", "CCC~~~~~~~~~~~~~~~~~~~~~CCC", "CCCCC~~CC~~~~~~~~~CC~~CCCCC", "CCCCC~~CC~~~~~~~~~CC~~CCCCC", "CCC~~~~~~~~~~~~~~~~~~~~~CCC", "CCCC~~~~~~~~~~~~~~~~~~~CCCC", "CCCC~~~~~~~CC~CC~~~~~~~CCCC", "~CCC~~~~~~~CC~CC~~~~~~~CCC~", "~CCCC~~~~~~~~~~~~~~~~~CCCC~", "~CCCCC~~~~~~~~~~~~~~~CCCCC~", "~~CCCCC~~~~CC~CC~~~~CCCCC~~", "~~~CCCCCCC~CC~CC~CCCCCCC~~~", "~~~~CCCCCCCCCCCCCCCCCCC~~~~", "~~~~~CCCCCCCCCCCCCCCCC~~~~~", "~~~~~~~~CCCCCCCCCCC~~~~~~~~"});
        pattern.add(new String[]{"~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~VVVVVVVVV~~~~~~~~~", "~~~~~~VVV~~~VGV~~~VVV~~~~~~", "~~~~~V~~~~~CVGVC~~~~~V~~~~~", "~~~~V~~~~~~CVGVC~~~~~~V~~~~", "~~~V~~~~~~~~VGV~~~~~~~~V~~~", "~~V~~~~~~~~~VGV~~~~~~~~~V~~", "~~V~~~~~~~~CVGVC~~~~~~~~V~~", "~~V~~~~~~~~CVGVC~~~~~~~~V~~", "~V~~~~~~~~~~VGV~~~~~~~~~~V~", "~V~~~~~~~~~~VGV~~~~~~~~~~V~", "~V~CC~~CC~~~VGV~~~CC~~CC~V~", "~VVVVVVVVVVVVVVVVVVVVVVVVV~", "~VGGGGGGGGGGVGVGGGGGGGGGGV~", "~VVVVVVVVVVVVVVVVVVVVVVVVV~", "~V~CC~~CC~~~VGV~~~CC~~CC~V~", "~V~~~~~~~~~~VGV~~~~~~~~~~V~", "~V~~~~~~~~~~VGV~~~~~~~~~~V~", "~~V~~~~~~~~CVGVC~~~~~~~~V~~", "~~V~~~~~~~~CVGVC~~~~~~~~V~~", "~~V~~~~~~~~~VGV~~~~~~~~~V~~", "~~~V~~~~~~~~VGV~~~~~~~~V~~~", "~~~~V~~~~~~CVGVC~~~~~~V~~~~", "~~~~~V~~~~~CVGVC~~~~~V~~~~~", "~~~~~~VVV~~~VGV~~~VVV~~~~~~", "~~~~~~~~~VVVVVVVVV~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~"});
        pattern.add(new String[]{"~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~VVVVVVVVV~~~~~~~~~", "~~~~~~VVV~~~GcG~~~VVV~~~~~~", "~~~~~V~~~~~CGcGC~~~~~V~~~~~", "~~~~V~~~~~~CGcGC~~~~~~V~~~~", "~~~V~~~~~~~~GcG~~~~~~~~V~~~", "~~V~~~~~~~~~GcG~~~~~~~~~V~~", "~~V~~~~~~~~CGcGC~~~~~~~~V~~", "~~V~~~~~~~~CGcGC~~~~~~~~V~~", "~V~~~~~~~~~~GcG~~~~~~~~~~V~", "~V~~~~~~~~~~GcG~~~~~~~~~~V~", "~V~CC~~CC~~~GcG~~~CC~~CC~V~", "~VGGGGGGGGGGVcVGGGGGGGGGGV~", "~VcccccccccccccccccccccccV~", "~VGGGGGGGGGGVcVGGGGGGGGGGV~", "~V~CC~~CC~~~GcG~~~CC~~CC~V~", "~V~~~~~~~~~~GcG~~~~~~~~~~V~", "~V~~~~~~~~~~GcG~~~~~~~~~~V~", "~~V~~~~~~~~CGcGC~~~~~~~~V~~", "~~V~~~~~~~~CGcGC~~~~~~~~V~~", "~~V~~~~~~~~~GcG~~~~~~~~~V~~", "~~~V~~~~~~~~GcG~~~~~~~~V~~~", "~~~~V~~~~~~CGcGC~~~~~~V~~~~", "~~~~~V~~~~~CGcGC~~~~~V~~~~~", "~~~~~~VVV~~~GcG~~~VVV~~~~~~", "~~~~~~~~~VVVVVVVVV~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~"});
        pattern.add(new String[]{"~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~VVVVVVVVV~~~~~~~~~", "~~~~~~VVV~~~VGV~~~VVV~~~~~~", "~~~~~V~~~~~CVGVC~~~~~V~~~~~", "~~~~V~~~~~~CVGVC~~~~~~V~~~~", "~~~V~~~~~~~~VGV~~~~~~~~V~~~", "~~V~~~~~~~~~VGV~~~~~~~~~V~~", "~~V~~~~~~~~CVGVC~~~~~~~~V~~", "~~V~~~~~~~~CVGVC~~~~~~~~V~~", "~V~~~~~~~~~~VGV~~~~~~~~~~V~", "~V~~~~~~~~~~VGV~~~~~~~~~~V~", "~V~CC~~CC~~~VGV~~~CC~~CC~V~", "~VVVVVVVVVVVVVVVVVVVVVVVVV~", "~VGGGGGGGGGGVGVGGGGGGGGGGV~", "~VVVVVVVVVVVVVVVVVVVVVVVVV~", "~V~CC~~CC~~~VGV~~~CC~~CC~V~", "~V~~~~~~~~~~VGV~~~~~~~~~~V~", "~V~~~~~~~~~~VGV~~~~~~~~~~V~", "~~V~~~~~~~~CVGVC~~~~~~~~V~~", "~~V~~~~~~~~CVGVC~~~~~~~~V~~", "~~V~~~~~~~~~VGV~~~~~~~~~V~~", "~~~V~~~~~~~~VGV~~~~~~~~V~~~", "~~~~V~~~~~~CVGVC~~~~~~V~~~~", "~~~~~V~~~~~CVGVC~~~~~V~~~~~", "~~~~~~VVV~~~VGV~~~VVV~~~~~~", "~~~~~~~~~VVVVVVVVV~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~"});
        pattern.add(new String[]{"~~~~~~~~CCCCCCCCCCC~~~~~~~~", "~~~~~CCCCCCCCCCCCCCCCC~~~~~", "~~~~CCCCCCCCCCCCCCCCCCC~~~~", "~~~CCCCCCC~CC~CC~CCCCCCC~~~", "~~CCCCC~~~~CC~CC~~~~CCCCC~~", "~CCCCC~~~~~~~~~~~~~~~CCCCC~", "~CCCC~~~~~~~~~~~~~~~~~CCCC~", "~CCC~~~~~~~CC~CC~~~~~~~CCC~", "CCCC~~~~~~~CCCCC~~~~~~~CCCC", "CCCC~~~~~~~~~~~~~~~~~~~CCCC", "CCC~~~~~~~~~~~~~~~~~~~~~CCC", "CCCCC~~CC~~~~~~~~~CC~~CCCCC", "CCCCC~~CC~~~~~~~~~CC~~CCCCC", "CCC~~~~~C~~~~~~~~~C~~~~~CCC", "CCCCC~~CC~~~~~~~~~CC~~CCCCC", "CCCCC~~CC~~~~~~~~~CC~~CCCCC", "CCC~~~~~~~~~~~~~~~~~~~~~CCC", "CCCC~~~~~~~~~~~~~~~~~~~CCCC", "CCCC~~~~~~~CCCCC~~~~~~~CCCC", "~CCC~~~~~~~CC~CC~~~~~~~CCC~", "~CCCC~~~~~~~~~~~~~~~~~CCCC~", "~CCCCC~~~~~~~~~~~~~~~CCCCC~", "~~CCCCC~~~~CC~CC~~~~CCCCC~~", "~~~CCCCCCC~CC~CC~CCCCCCC~~~", "~~~~CCCCCCCCCCCCCCCCCCC~~~~", "~~~~~CCCCCCCCCCCCCCCCC~~~~~", "~~~~~~~~CCCCCCCCCCC~~~~~~~~"});
        pattern.add(new String[]{"~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~C~C~~~~~~~~~~~~", "~~~~~~~~~~~~C~C~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~C~C~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~CC~~~C~~~~~~~~~C~~~CC~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~CC~~~C~~~~~~~~~C~~~CC~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~C~C~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~C~C~~~~~~~~~~~~", "~~~~~~~~~~~~C~C~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~"});
        pattern.add(new String[]{"~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~C~C~~~~~~~~~~~~", "~~~~~~~~~~~~C~C~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~C~C~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~CC~~~C~~~~~~~~~C~~~CC~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~CC~~~C~~~~~~~~~C~~~CC~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~C~C~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~C~C~~~~~~~~~~~~", "~~~~~~~~~~~~C~C~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~"});
        pattern.add(new String[]{"~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~XXX~~~~~~~~~~~~", "~~~~~~~~~~~~X~X~~~~~~~~~~~~", "~~~~~~~~~~~~X~X~~~~~~~~~~~~", "~~~~~~~~~~~~X~X~~~~~~~~~~~~", "~~~~~~~~~~~~X~X~~~~~~~~~~~~", "~~~~~~~~~~~~X~X~~~~~~~~~~~~", "~~~~~~~~~~~X~~~X~~~~~~~~~~~", "~~~~~~~~~~XX~~~XX~~~~~~~~~~", "~~~~XXXXXX~~XXX~~XXXXXX~~~~", "~~~~X~~~~~~~XvX~~~~~~~X~~~~", "~~~~XXXXXX~~XXX~~XXXXXX~~~~", "~~~~~~~~~~XX~~~XX~~~~~~~~~~", "~~~~~~~~~~~X~~~X~~~~~~~~~~~", "~~~~~~~~~~~~X~X~~~~~~~~~~~~", "~~~~~~~~~~~~X~X~~~~~~~~~~~~", "~~~~~~~~~~~~X~X~~~~~~~~~~~~", "~~~~~~~~~~~~X~X~~~~~~~~~~~~", "~~~~~~~~~~~~X~X~~~~~~~~~~~~", "~~~~~~~~~~~~XXX~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~"});
        pattern.add(new String[]{"~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~X~X~~~~~~~~~~~~", "~~~~~~~~~~VVVVVVV~~~~~~~~~~", "~~~~~~~~VVVVVVVVVVV~~~~~~~~", "~~~~~~~VVVVVVVVVVVVV~~~~~~~", "~~~~~~VVVVVVVVVVVVVVV~~~~~~", "~~~~~~VVVVVVVVVVVVVVV~~~~~~", "~~~~~VVVVVVVVVVVVVVVVV~~~~~", "~~~~~VVVVVVVVVVVVVVVVV~~~~~", "~~~~XVVVVVVVVVVVVVVVVVX~~~~", "~~~~~VVVVVVVVVVVVVVVVV~~~~~", "~~~~XVVVVVVVVVVVVVVVVVX~~~~", "~~~~~VVVVVVVVVVVVVVVVV~~~~~", "~~~~~VVVVVVVVVVVVVVVVV~~~~~", "~~~~~~VVVVVVVVVVVVVVV~~~~~~", "~~~~~~VVVVVVVVVVVVVVV~~~~~~", "~~~~~~~VVVVVVVVVVVVV~~~~~~~", "~~~~~~~~VVVVVVVVVVV~~~~~~~~", "~~~~~~~~~~VVVVVVV~~~~~~~~~~", "~~~~~~~~~~~~X~X~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~"});
        pattern.add(new String[]{"~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~X~X~~~~~~~~~~~~", "~~~~~~~~~~~~XsX~~~~~~~~~~~~", "~~~~~~~~~~~~ccc~~~~~~~~~~~~", "~~~~~~~~~~ccX~Xcc~~~~~~~~~~", "~~~~~~~~~c~~X~X~~c~~~~~~~~~", "~~~~~~~~c~~~XXX~~~c~~~~~~~~", "~~~~~~~~c~~TTTTT~~c~~~~~~~~", "~~~~~XXcXXXTTTTTXXXcXX~~~~~", "~~~~~~Xc~~XTTTTTX~~cX~~~~~~", "~~~~~XXcXXXTTTTTXXXcXX~~~~~", "~~~~~~~~c~~TTTTT~~c~~~~~~~~", "~~~~~~~~c~~~XXX~~~c~~~~~~~~", "~~~~~~~~~c~~X~X~~c~~~~~~~~~", "~~~~~~~~~~ccX~Xcc~~~~~~~~~~", "~~~~~~~~~~~~ccc~~~~~~~~~~~~", "~~~~~~~~~~~~CCC~~~~~~~~~~~~", "~~~~~~~~~~~~X~X~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~"});
        pattern.add(new String[]{"~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~X~X~~~~~~~~~~~~", "~~~~~~~~~~~~X~X~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~T~~~T~~~~~~~~~~~", "~~~~~~XX~~~~DGD~~~~XX~~~~~~", "~~~~~~~~~~~~G#G~~~~~~~~~~~~", "~~~~~~XX~~~~DGD~~~~XX~~~~~~", "~~~~~~~~~~~T~~~T~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~X~X~~~~~~~~~~~~", "~~~~~~~~~~~~X~X~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~"});
        pattern.add(new String[]{"~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~VVV~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~VVV~~~~~~~~~~~~", "~~~~~~~~~~~~X~X~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~T~~~T~~~~~~~~~~~", "~V~~~VX~~~~~DGD~~~~~XV~~~V~", "~V~~~V~~~~~~G#G~~~~~~V~~~V~", "~V~~~VX~~~~~DGD~~~~~XV~~~V~", "~~~~~~~~~~~T~~~T~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~X~X~~~~~~~~~~~~", "~~~~~~~~~~~~VVV~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~VVV~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~"});
        pattern.add(new String[]{"~~~~~~~~~~~~~V~~~~~~~~~~~~~", "~~~~~~~~~VVVVGVVVV~~~~~~~~~", "~~~~~~VVV~~~~G~~~~VVV~~~~~~", "~~~~~V~~~~~~~G~~~~~~~V~~~~~", "~~~~V~~~~~~~~G~~~~~~~~V~~~~", "~~~V~~~~~~~VVGVV~~~~~~~V~~~", "~~V~~~~~~~~~X~X~~~~~~~~~V~~", "~~V~~~~~~~~~~~~~~~~~~~~~V~~", "~~V~~~~~~~~~~~~~~~~~~~~~V~~", "~V~~~~~~~~~~~~~~~~~~~~~~~V~", "~V~~~~~~~~~~~~~~~~~~~~~~~V~", "~V~~~V~~~~~T~~~T~~~~~V~~~V~", "~V~~~VX~~~~~DGD~~~~~XV~~XV~", "VGGGGG~~~~~~G#G~~~~~~GGGGGV", "~V~~~VX~~~~~DGD~~~~~XV~~XV~", "~V~~~V~~~~~T~~~T~~~~~V~~~V~", "~V~~~~~~~~~~~~~~~~~~~~~~~V~", "~V~~~~~~~~~~~~~~~~~~~~~~~V~", "~~V~~~~~~~~~~~~~~~~~~~~~V~~", "~~V~~~~~~~~~~~~~~~~~~~~~V~~", "~~V~~~~~~~~~X~X~~~~~~~~~V~~", "~~~V~~~~~~~VVGVV~~~~~~~V~~~", "~~~~V~~~~~~~~G~~~~~~~~V~~~~", "~~~~~V~~~~~~~G~~~~~~~V~~~~~", "~~~~~~VVV~~~~G~~~~VVV~~~~~~", "~~~~~~~~~VVVVGVVVV~~~~~~~~~", "~~~~~~~~~~~~~V~~~~~~~~~~~~~"});
        pattern.forEach(factoryPattern::aisle);
        factoryPattern.aisle("~~~~~~~~~~~~VVV~~~~~~~~~~~~", "~~~~~~~~~~~VGcGV~~~~~~~~~~~", "~~~~~~~~~~~~GcG~~~~~~~~~~~~", "~~~~~~~~~~~~GcG~~~~~~~~~~~~", "~~~~~~~~~~~~GcG~~~~~~~~~~~~", "~~~~~~~~~~~VGcGV~~~~~~~~~~~", "~~~~~~~~~~~~XcX~~~~~~~~~~~~", "~~~~~~~~~~~~ccc~~~~~~~~~~~~", "~~~~~~~~~~ccX~Xcc~~~~~~~~~~", "~~~~~~~~~c~~X~X~~c~~~~~~~~~", "~~~~~~~~c~~~X~X~~~c~~~~~~~~", "~V~~~V~~c~~TX~XT~~c~~V~~~V~", "VGGGGGXcXXXXDGDXXXXcXGGGGGV", "Vccccccc~~~~G#G~~~~cccccccV", "VGGGGGXcXXXXDGDXXXXcXGGGGGV", "~V~~~V~~c~~TX~XT~~c~~V~~~V~", "~~~~~~~~c~~~X~X~~~c~~~~~~~~", "~~~~~~~~~c~~X~X~~c~~~~~~~~~", "~~~~~~~~~~ccX~Xcc~~~~~~~~~~", "~~~~~~~~~~~~ccc~~~~~~~~~~~~", "~~~~~~~~~~~~XcX~~~~~~~~~~~~", "~~~~~~~~~~~VGcGV~~~~~~~~~~~", "~~~~~~~~~~~~GcG~~~~~~~~~~~~", "~~~~~~~~~~~~GcG~~~~~~~~~~~~", "~~~~~~~~~~~~GcG~~~~~~~~~~~~", "~~~~~~~~~~~VGcGV~~~~~~~~~~~", "~~~~~~~~~~~~VVV~~~~~~~~~~~~");
        for (int i = 0; i < pattern.size(); i++) {
            final String[] aisle = pattern.get(i);
            final String[] aisle2 = new String[aisle.length];
            for (int j = 0; j < aisle.length; j++) {
                aisle2[j] = aisle[j].replace("s", "S");
            }
            pattern.set(i, aisle2);
        }
        Collections.reverse(pattern);
        pattern.forEach(factoryPattern::aisle);
        return factoryPattern.where('S', this.selfPredicate())
                .where('C', statePredicate(this.getCasingState()))
                .where('s', statePredicate(this.getCasingState()))
                .where('X', statePredicate(this.getCasingState()).or(abilityPartPredicate(ALLOWED_ABILITIES)))
                .where('V', statePredicate(TJMetaBlocks.SOLID_CASING.getState(BlockSolidCasings.SolidCasingType.VIBRANIUM)))
                .where('G', statePredicate(TJMetaBlocks.FUSION_GLASS.getState(BlockFusionGlass.GlassType.FUSION_GLASS_UEV)))
                .where('c', statePredicate(TJMetaBlocks.FUSION_CASING.getState(BlockFusionCasings.FusionType.FUSION_COIL_UEV)))
                .where('v', frameworkPredicate())
                .where('#', isAirPredicate())
                .where('~', tile -> true)
                .build();
    }

    private IBlockState getCasingState() {
        return TJMetaBlocks.SOLID_CASING.getState(BlockSolidCasings.SolidCasingType.CHAOS_ALLOY);
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        final int tier = context.getOrDefault("Emitter", EmitterCasing.CasingType.EMITTER_LV).getTier();
        if (tier < GAValues.MAX) {
            this.maxVoltage = 8L << tier * 2;
            this.tier = tier;
        }
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return ClientHandler.ENRICHED_NAQUADAH_ALLOY_CASING;
    }

    @Override
    public int getEUtMultiplier() {
        return TJConfig.interStellarForge.eutPercentage;
    }

    @Override
    public int getDurationMultiplier() {
        return TJConfig.interStellarForge.durationPercentage;
    }

    @Override
    public int getChanceMultiplier() {
        return TJConfig.interStellarForge.chancePercentage;
    }

    @Override
    public int getParallel() {
        return TJConfig.interStellarForge.stack;
    }
}
