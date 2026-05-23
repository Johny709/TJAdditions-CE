package tj.items;

import gregicadditions.GAValues;
import gregtech.api.items.materialitem.MaterialMetaItem;
import gregtech.api.unification.ore.OrePrefix;
import net.minecraft.item.ItemStack;
import tj.TJValues;
import tj.items.behaviours.*;

import static tj.TJValues.CIRCUIT_TIERS;
import static tj.items.TJMetaItems.*;

public class TJMetaItem1 extends MaterialMetaItem {

    @Override
    public void registerSubItems() {
        ELECTRIC_MOTOR_ULV = addItem(983, "electric.motor.ulv");
        ELECTRIC_PUMP_ULV = addItem(984, "electric.pump.ulv");
        CONVEYOR_MODULE_ULV = addItem(985, "conveyor.module.ulv");
        ELECTRIC_PISTON_ULV = addItem(986, "electric.piston.ulv");
        ROBOT_ARM_ULV = addItem(987, "robot.arm.ulv");
        FIELD_GENERATOR_ULV = addItem(988, "field.generator.ulv");
        EMITTER_ULV = addItem(989, "emitter.ulv");
        SENSOR_ULV = addItem(990, "sensor.ulv");
        FLUID_REGULATOR_ULV = addItem(991, "fluid.regulator.ulv");
        GAUGE_DROPPER = addItem(992, "gauge_dropper").addComponents(new GaugeDropperBehavior()).setMaxStackSize(1);
        VOID_ADVANCED_ITEM_COVER = addItem(993, "void_advanced_item_cover").addComponents(new VoidAdvancedItemCoverBehaviour());
        VOID_ADVANCED_FLUID_COVER = addItem(994, "void_advanced_fluid_cover").addComponents(new VoidAdvancedFluidCoverBehaviour());
        VOID_ADVANCED_ENERGY_COVER = addItem(995, "void_advanced_energy_cover").addComponents(new VoidAdvancedEnergyCoverBehaviour());
        VOID_ITEM_COVER = addItem(996, "void_item_cover").addComponents(new VoidItemCoverBehaviour());
        VOID_FLUID_COVER = addItem(997, "void_fluid_cover").addComponents(new VoidFluidCoverBehaviour());
        VOID_ENERGY_COVER = addItem(998, "void_energy_cover").addComponents(new VoidEnergyCoverBehaviour());
        TOOLBOX = addItem(999, "toolbox").addComponents(new ToolboxBehaviour()).setMaxStackSize(1);
        CREATIVE_FLUID_COVER = addItem(1000, "creative.fluid.cover").addComponents(new CreativeFluidCoverBehaviour());
        CREATIVE_ITEM_COVER = addItem(1001, "creative.item.cover").addComponents(new CreativeItemCoverBehaviour());
        CREATIVE_ENERGY_COVER = addItem(1002, "creative.energy.cover").addComponents(new CreativeEnergyCoverBehaviour());
        LINKING_DEVICE = addItem(1003,"item.linking.device").addComponents(new LinkingDeviceBehavior()).setMaxStackSize(1);
        VOID_PLUNGER = addItem(1004, "void_plunger").addComponents(new VoidPlungerBehaviour()).setMaxStackSize(1);
        NBT_READER = addItem(1005, "nbt_reader").addComponents(new NBTReaderBehaviour()).setMaxStackSize(1);
        FLUID_REGULATOR_UHV = addItem(1059, "fluid.regulator.uhv");
        FLUID_REGULATOR_UEV = addItem(1060, "fluid.regulator.uev");
        FLUID_REGULATOR_UIV = addItem(1061, "fluid.regulator.uiv");
        FLUID_REGULATOR_UMV = addItem(1062, "fluid.regulator.umv");
        FLUID_REGULATOR_UXV = addItem(1063, "fluid.regulator.uxv");
        FLUID_REGULATOR_MAX = addItem(1064, "fluid.regulator.max");
        REMOTE_MULTIBLOCK_CONTROLLER = addItem(1065, "remote_multiblock_controller").addComponents(new RemoteMultiblockControllerBehaviour()).setMaxStackSize(1);

        for (int i = 0; i < UNIVERSAL_CIRCUITS.length; i++) { // occupies range 1006 - 1021
            UNIVERSAL_CIRCUITS[i] = addItem(1006 + i, GAValues.VN[i].toLowerCase() + "_universal_circuit").setUnificationData(OrePrefix.circuit, CIRCUIT_TIERS[i]);
        }
        int enderCoverID = 1022; // occupies range 1022 - 1058
        for (int i = 0; i < ENDER_FLUID_COVERS.length; i++) {
            ENDER_FLUID_COVERS[i] = addItem(enderCoverID++, "ender_fluid_cover_" + GAValues.VN[i + 3].toLowerCase()).addComponents(new EnderCoverBehaviour(EnderCoverBehaviour.EnderCoverType.FLUID,i + 3));
            ENDER_ITEM_COVERS[i] = addItem(enderCoverID++, "ender_item_cover_" + GAValues.VN[i + 3].toLowerCase()).addComponents(new EnderCoverBehaviour(EnderCoverBehaviour.EnderCoverType.ITEM, i + 3));
            ENDER_ENERGY_COVERS[i] = addItem(enderCoverID++, "ender_energy_cover_" + GAValues.VN[i + 3].toLowerCase()).addComponents(new EnderCoverBehaviour(EnderCoverBehaviour.EnderCoverType.ENERGY, i + 3));
        }
        for (int i = 0; i < DUAL_COVERS.length; i++) { // occupies range 1170 - 1184
            DUAL_COVERS[i] = addItem(1170 + i, "dual_cover." + GAValues.VN[i].toLowerCase()).addComponents(new DualCoverBehaviour(i));
        }
        for (int i = 0; i < DUAL_COVERS.length; i++) { // occupies range 1185 - 1199
            CONTROLLABLE_DUAL_COVERS[i] = addItem(1185 + i, "controllable_dual_cover." + GAValues.VN[i].toLowerCase()).addComponents(new ControllableDualCoverBehaviour(i));
        }
        for (int i = 0; i < TURBINE_UPGRADES.length; i++) { // occupies range 1200 - 1205
            TURBINE_UPGRADES[i] = addItem(1200 + i, "turbine_upgrade").addComponents(new TurbineUpgradeBehaviour(TJValues.VC[i + GAValues.UHV], 4 + 4 * i)).setMaxStackSize(1);
        }
    }

    @Override
    public ItemStack getContainerItem(ItemStack stack) {
        return stack.copy();
    }
}
