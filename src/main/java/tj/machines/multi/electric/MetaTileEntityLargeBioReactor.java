package tj.machines.multi.electric;

import gregicadditions.GAValues;
import gregicadditions.client.ClientHandler;
import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.GAMultiblockCasing2;
import gregicadditions.item.GATransparentCasing;
import gregicadditions.item.components.EmitterCasing;
import gregicadditions.item.components.FieldGenCasing;
import gregicadditions.item.components.PumpCasing;
import gregicadditions.item.components.SensorCasing;
import gregicadditions.recipes.GARecipeMaps;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.api.multiblock.PatternMatchContext;
import gregtech.api.recipes.Recipe;
import gregtech.api.render.ICubeRenderer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import tj.TJConfig;
import tj.builder.multicontrollers.GUIDisplayBuilder;
import tj.builder.multicontrollers.TJRecipeMapMultiblockController;
import tj.capability.OverclockManager;

import static gregicadditions.capabilities.GregicAdditionsCapabilities.MAINTENANCE_HATCH;
import static gregicadditions.machines.multi.simple.LargeSimpleRecipeMapMultiblockController.*;
import static gregtech.api.metatileentity.multiblock.MultiblockAbility.*;

public class MetaTileEntityLargeBioReactor extends TJRecipeMapMultiblockController {

    private static final MultiblockAbility<?>[] ALLOWED_ABILITIES = {IMPORT_ITEMS, EXPORT_ITEMS, IMPORT_FLUIDS, EXPORT_FLUIDS, INPUT_ENERGY, MAINTENANCE_HATCH};
    private int energyBonus;

    public MetaTileEntityLargeBioReactor(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, GARecipeMaps.BIO_REACTOR_RECIPES);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityLargeBioReactor(this.metaTileEntityId);
    }

    @Override
    public void postOverclock(OverclockManager<?> overclockManager, Recipe recipe) {
        overclockManager.setEUt(overclockManager.getEUt() * (100 - this.energyBonus) / 100);
    }

    @Override
    protected void addDisplayText(GUIDisplayBuilder builder) {
        super.addDisplayText(builder);
        builder.addEnergyBonusLine(this.energyBonus, this.isStructureFormed() && this.energyBonus >= 0);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~c~c~~~~~~", "~~~~~CcXcC~~~~~", "~~~~~~c~c~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~")
                .aisle("~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~GCG~~~~~~", "~~~~~CcCcC~~~~~", "~~~CC#####CC~~~", "~~~~~CcCcC~~~~~", "~~~~~~GCG~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~")
                .aisle("~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~GGCGG~~~~~", "~~~~~GGCGG~~~~~", "~~~~GG###GG~~~~", "~~~CC#c#c#CC~~~", "~~C###c#c###C~~", "~~~CC#c#c#CC~~~", "~~~~GG###GG~~~~", "~~~~~GGCGG~~~~~", "~~~~~GGCGG~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~")
                .aisle("~~~~~~~~~~~~~~~", "~~~~~~GCG~~~~~~", "~~~~GG###GG~~~~", "~~~~G#####G~~~~", "~~~G#######G~~~", "~~C#########C~~", "~C###########C~", "~~C#########C~~", "~~~G#######G~~~", "~~~~G#####G~~~~", "~~~~GG###GG~~~~", "~~~~~~GCG~~~~~~", "~~~~~~~~~~~~~~~")
                .aisle("~~~~~~~~~~~~~~~", "~~~~~GGCGG~~~~~", "~~~GG#####GG~~~", "~~~G#######G~~~", "~~G#########G~~", "~~C#########C~~", "~C###########C~", "~~C#########C~~", "~~G#########G~~", "~~~G#######G~~~", "~~~GG#####GG~~~", "~~~~~GGCGG~~~~~", "~~~~~~~~~~~~~~~")
                .aisle("~~~~~~CCC~~~~~~", "~~~~GG###GG~~~~", "~~GG#######GG~~", "~~G#########G~~", "~~G#########G~~", "~C###########C~", "C#############C", "~C###########C~", "~~G#########G~~", "~~G#########G~~", "~~GG#######GG~~", "~~~~GG###GG~~~~", "~~~~~~CCC~~~~~~")
                .aisle("~~~~~CXXXC~~~~~", "~~~GG#####GG~~~", "~~G#########G~~", "~~G#########G~~", "~G###########G~", "ccc#########ccc", "c#c####E####c#c", "ccc#########ccc", "~G###########G~", "~~G#########G~~", "~~G#########G~~", "~~~GG#####GG~~~", "~~~~~CXXXC~~~~~")
                .aisle("~~~~~CXXXC~~~~~", "~~~CC#####CC~~~", "~~C#########C~~", "~~C#########C~~", "~C###########C~", "~C#####P#####C~", "X#####sFs#####X", "~C#####P#####C~", "~C###########C~", "~~C#########C~~", "~~C#########C~~", "~~~CC#####CC~~~", "~~~~~CXXXC~~~~~")
                .aisle("~~~~~CXXXC~~~~~", "~~~GG#####GG~~~", "~~G#########G~~", "~~G#########G~~", "~G###########G~", "ccc#########ccc", "c#c####E####c#c", "ccc#########ccc", "~G###########G~", "~~G#########G~~", "~~G#########G~~", "~~~GG#####GG~~~", "~~~~~CXXXC~~~~~")
                .aisle("~~~~~~CCC~~~~~~", "~~~~GG###GG~~~~", "~~GG#######GG~~", "~~G#########G~~", "~~G#########G~~", "~C###########C~", "C#############C", "~C###########C~", "~~G#########G~~", "~~G#########G~~", "~~GG#######GG~~", "~~~~GG###GG~~~~", "~~~~~~CCC~~~~~~")
                .aisle("~~~~~~~~~~~~~~~", "~~~~~GGCGG~~~~~", "~~~GG#####GG~~~", "~~~G#######G~~~", "~~G#########G~~", "~~C#########C~~", "~C###########C~", "~~C#########C~~", "~~G#########G~~", "~~~G#######G~~~", "~~~GG#####GG~~~", "~~~~~GGCGG~~~~~", "~~~~~~~~~~~~~~~")
                .aisle("~~~~~~~~~~~~~~~", "~~~~~~GCG~~~~~~", "~~~~GG###GG~~~~", "~~~~G#####G~~~~", "~~~G#######G~~~", "~~C#########C~~", "~C###########C~", "~~C#########C~~", "~~~G#######G~~~", "~~~~G#####G~~~~", "~~~~GG###GG~~~~", "~~~~~~GCG~~~~~~", "~~~~~~~~~~~~~~~")
                .aisle("~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~GGCGG~~~~~", "~~~~~GGCGG~~~~~", "~~~~GG###GG~~~~", "~~~CC#c#c#CC~~~", "~~C###c#c###C~~", "~~~CC#c#c#CC~~~", "~~~~GG###GG~~~~", "~~~~~GGCGG~~~~~", "~~~~~GGCGG~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~")
                .aisle("~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~GCG~~~~~~", "~~~~~CcCcC~~~~~", "~~~CC#####CC~~~", "~~~~~CcCcC~~~~~", "~~~~~~GCG~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~")
                .aisle("~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~c~c~~~~~~", "~~~~~CcScC~~~~~", "~~~~~~c~c~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~")
                .where('S', this.selfPredicate())
                .where('C', statePredicate(this.getCasingState()))
                .where('X', statePredicate(this.getCasingState()).or(abilityPartPredicate(ALLOWED_ABILITIES)))
                .where('G', statePredicate(GAMetaBlocks.TRANSPARENT_CASING.getState(GATransparentCasing.CasingType.OSMIRIDIUM_GLASS)))
                .where('c', coilPredicate())
                .where('P', pumpPredicate())
                .where('F', fieldGenPredicate())
                .where('E', emitterPredicate())
                .where('s', sensorPredicate())
                .where('#', isAirPredicate())
                .where('~', tile -> true)
                .build();
    }

    private IBlockState getCasingState() {
        return GAMetaBlocks.MUTLIBLOCK_CASING2.getState(GAMultiblockCasing2.CasingType.BIO_REACTOR);
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        final int fieldGen = context.getOrDefault("FieldGen", FieldGenCasing.CasingType.FIELD_GENERATOR_LV).getTier();
        final int emitter = context.getOrDefault("Emitter", EmitterCasing.CasingType.EMITTER_LV).getTier();
        final int sensor = context.getOrDefault("Sensor", SensorCasing.CasingType.SENSOR_LV).getTier();
        final int pump = context.getOrDefault("Pump", PumpCasing.CasingType.PUMP_LV).getTier();
        final int tier = Math.min(fieldGen, Math.min(emitter, Math.min(sensor, pump)));
        this.energyBonus = context.getOrDefault("coilIndex", 0) * 5;
        if (tier < GAValues.MAX) {
            this.maxVoltage = 8L << tier * 2;
            this.tier = tier;
        }
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.energyBonus = 0;
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return ClientHandler.BIO_REACTOR;
    }

    @Override
    public int getEUtMultiplier() {
        return TJConfig.largeBioReactor.eutPercentage;
    }

    @Override
    public int getDurationMultiplier() {
        return TJConfig.largeBioReactor.durationPercentage;
    }

    @Override
    public int getChanceMultiplier() {
        return TJConfig.largeBioReactor.chancePercentage;
    }

    @Override
    public int getParallel() {
        return TJConfig.largeBioReactor.stack;
    }
}
