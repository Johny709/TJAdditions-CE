package tj.machines.multi.electric;

import gregicadditions.client.ClientHandler;
import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.components.EmitterCasing;
import gregicadditions.item.components.FieldGenCasing;
import gregicadditions.item.components.PumpCasing;
import gregicadditions.item.components.SensorCasing;
import gregicadditions.item.metal.MetalCasing2;
import gregicadditions.recipes.GARecipeMaps;
import gregtech.api.GTValues;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.api.multiblock.PatternMatchContext;
import gregtech.api.render.ICubeRenderer;
import net.minecraft.util.ResourceLocation;
import tj.builder.multicontrollers.TJRecipeMapMultiblockController;

public class MetaTileEntityLargeBioReactor extends TJRecipeMapMultiblockController {

    public MetaTileEntityLargeBioReactor(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, GARecipeMaps.BIO_REACTOR_RECIPES);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityLargeBioReactor(this.metaTileEntityId);
    }

    // TODO WIP
    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("XXX", "XXX", "XXX")
                .aisle("XXX", "X#X", "XXX")
                .aisle("XXX", "XSX", "XXX")
                .where('S', this.selfPredicate())
                .where('X', statePredicate(GAMetaBlocks.METAL_CASING_2.getState(MetalCasing2.CasingType.ENRICHED_NAQUADAH_ALLOY)))
                .where('#', isAirPredicate())
                .build();
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        final int fieldGen = context.getOrDefault("FieldGen", FieldGenCasing.CasingType.FIELD_GENERATOR_LV).getTier();
        final int emitter = context.getOrDefault("Emitter", EmitterCasing.CasingType.EMITTER_LV).getTier();
        final int sensor = context.getOrDefault("Sensor", SensorCasing.CasingType.SENSOR_LV).getTier();
        final int pump = context.getOrDefault("Pump", PumpCasing.CasingType.PUMP_LV).getTier();
        final int tier = Math.min(fieldGen, Math.min(emitter, Math.min(sensor, pump)));
        if (tier < GTValues.MAX) {
            this.maxVoltage = 8L << tier * 2;
            this.tier = tier;
        }
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return ClientHandler.BIO_REACTOR;
    }
}
