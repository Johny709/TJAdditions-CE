package tj.materials;

import gregtech.api.unification.material.IMaterialHandler;

import static gregicadditions.GAMaterials.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.material.type.DustMaterial.MatFlags.GENERATE_PLATE;
import static gregtech.api.unification.material.type.DustMaterial.MatFlags.SMELT_INTO_FLUID;
import static gregtech.api.unification.material.type.IngotMaterial.MatFlags.*;
import static gregtech.api.unification.material.type.SolidMaterial.MatFlags.GENERATE_FRAME;

@IMaterialHandler.RegisterMaterialHandler
public class TJMaterials implements IMaterialHandler {

    @Override
    public void onMaterialsInit() {
        Periodicium.addFlag(GENERATE_PLATE, GENERATE_BOLT_SCREW, GENERATE_FRAME);
        EnderPearl.addFlag(SMELT_INTO_FLUID);
        EnderEye.addFlag(SMELT_INTO_FLUID);
        Silver.addFlag(GENERATE_DOUBLE_PLATE, GENERATE_DENSE);
        Iron.addFlag(GENERATE_ROTOR);
        Rutherfordium.addFlag(GENERATE_FRAME);
    }
}
