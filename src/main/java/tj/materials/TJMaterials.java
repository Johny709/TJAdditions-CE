package tj.materials;

import gregtech.api.unification.material.IMaterialHandler;
import gregtech.api.unification.material.MarkerMaterials;
import gregtech.api.unification.material.type.Material;

import static gregicadditions.GAMaterials.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.material.type.DustMaterial.MatFlags.GENERATE_PLATE;
import static gregtech.api.unification.material.type.DustMaterial.MatFlags.SMELT_INTO_FLUID;
import static gregtech.api.unification.material.type.IngotMaterial.MatFlags.*;
import static gregtech.api.unification.material.type.Material.MATERIAL_REGISTRY;
import static gregtech.api.unification.material.type.SolidMaterial.MatFlags.GENERATE_FRAME;

@IMaterialHandler.RegisterMaterialHandler
public class TJMaterials implements IMaterialHandler {

    public static final Material[] MATERIAL_TIER = new Material[]{Lead, Steel, Aluminium, StainlessSteel, Titanium, TungstenSteel, RhodiumPlatedPalladium, MATERIAL_REGISTRY.getObject("star_metal_alloy"), Tritanium, Seaborgium, Bohrium, Adamantium, Vibranium, HeavyQuarkDegenerateMatter, Neutronium};
    public static final Material[] SUPERCONDUCTOR_TIER = new Material[]{null, MATERIAL_REGISTRY.getObject("lv_superconductor"), MVSuperconductor, HVSuperconductor, EVSuperconductor, IVSuperconductor, LuVSuperconductor, ZPMSuperconductor, UVSuperconductor, UHVSuperconductor, UEVSuperconductor, UIVSuperconductor, UMVSuperconductor, UXVSuperconductor, MarkerMaterials.Tier.Superconductor};

    @Override
    public void onMaterialsInit() {
        Periodicium.addFlag(GENERATE_PLATE, GENERATE_BOLT_SCREW, GENERATE_FRAME);
        EnderPearl.addFlag(SMELT_INTO_FLUID);
        EnderEye.addFlag(SMELT_INTO_FLUID);
        Silver.addFlag(GENERATE_DOUBLE_PLATE, GENERATE_DENSE);
        Iron.addFlag(GENERATE_ROTOR);
        Rutherfordium.addFlag(GENERATE_FRAME);
        QCDMatter.addFlag(GENERATE_SMALL_GEAR);
    }
}
