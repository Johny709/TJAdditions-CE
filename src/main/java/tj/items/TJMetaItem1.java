package tj.items;

import gregicadditions.GAValues;
import gregtech.api.items.materialitem.MaterialMetaItem;
import gregtech.api.unification.ore.OrePrefix;
import net.minecraft.item.ItemStack;
import tj.TJValues;
import tj.items.behaviours.*;
import tj.util.TJUtility;

import javax.annotation.Nonnull;

import static gregicadditions.item.GAMetaItems.*;
import static gregicadditions.item.GAMetaItems.ELECTRIC_MOTOR_MAX;
import static gregicadditions.item.GAMetaItems.ELECTRIC_MOTOR_UMV;
import static gregicadditions.item.GAMetaItems.ELECTRIC_MOTOR_UXV;
import static gregtech.common.items.MetaItems.*;
import static gregtech.common.items.MetaItems.ELECTRIC_MOTOR_EV;
import static gregtech.common.items.MetaItems.ELECTRIC_MOTOR_IV;
import static gregtech.common.items.MetaItems.ELECTRIC_MOTOR_LUV;
import static gregtech.common.items.MetaItems.ELECTRIC_MOTOR_UV;
import static gregtech.common.items.MetaItems.ELECTRIC_MOTOR_ZPM;
import static tj.TJValues.CIRCUIT_TIERS;
import static tj.items.TJMetaItems.*;
import static tj.items.TJMetaItems.FLUID_REGULATORS;
import static tj.items.TJMetaItems.PUMPS;

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
        TJUtility.addToArray(MOTORS, ELECTRIC_MOTOR_ULV, ELECTRIC_MOTOR_LV, ELECTRIC_MOTOR_MV, ELECTRIC_MOTOR_HV, ELECTRIC_MOTOR_EV, ELECTRIC_MOTOR_IV, ELECTRIC_MOTOR_LUV, ELECTRIC_MOTOR_ZPM, ELECTRIC_MOTOR_UV, ELECTRIC_MOTOR_UHV, ELECTRIC_MOTOR_UEV, ELECTRIC_MOTOR_UIV, ELECTRIC_MOTOR_UMV, ELECTRIC_MOTOR_UXV, ELECTRIC_MOTOR_MAX);
        TJUtility.addToArray(PUMPS, ELECTRIC_PUMP_ULV, ELECTRIC_PUMP_LV, ELECTRIC_PUMP_MV, ELECTRIC_PUMP_HV, ELECTRIC_PUMP_EV, ELECTRIC_PUMP_IV, ELECTRIC_PUMP_LUV, ELECTRIC_PUMP_ZPM, ELECTRIC_PUMP_UV, ELECTRIC_PUMP_UHV, ELECTRIC_PUMP_UEV, ELECTRIC_PUMP_UIV, ELECTRIC_PUMP_UMV, ELECTRIC_PUMP_UXV, ELECTRIC_PUMP_MAX);
        TJUtility.addToArray(CONVEYORS, CONVEYOR_MODULE_ULV, CONVEYOR_MODULE_LV, CONVEYOR_MODULE_MV, CONVEYOR_MODULE_HV, CONVEYOR_MODULE_EV, CONVEYOR_MODULE_IV, CONVEYOR_MODULE_LUV, CONVEYOR_MODULE_ZPM, CONVEYOR_MODULE_UV, CONVEYOR_MODULE_UHV, CONVEYOR_MODULE_UEV, CONVEYOR_MODULE_UIV, CONVEYOR_MODULE_UMV, CONVEYOR_MODULE_UXV, CONVEYOR_MODULE_MAX);
        TJUtility.addToArray(PISTONS, ELECTRIC_PISTON_ULV, ELECTRIC_PISTON_LV, ELECTRIC_PISTON_MV, ELECTRIC_PISTON_HV, ELECTRIC_PISTON_EV, ELECTRIC_PISTON_IV, ELECTRIC_PISTON_LUV, ELECTRIC_PISTON_ZPM, ELECTRIC_PISTON_UV, ELECTRIC_PISTON_UHV, ELECTRIC_PISTON_UEV, ELECTRIC_PISTON_UIV, ELECTRIC_PISTON_UMV, ELECTRIC_PISTON_UXV, ELECTRIC_PISTON_MAX);
        TJUtility.addToArray(ROBOT_ARMS, ROBOT_ARM_ULV, ROBOT_ARM_LV, ROBOT_ARM_MV, ROBOT_ARM_HV, ROBOT_ARM_EV, ROBOT_ARM_IV, ROBOT_ARM_LUV, ROBOT_ARM_ZPM, ROBOT_ARM_UV, ROBOT_ARM_UHV, ROBOT_ARM_UEV, ROBOT_ARM_UIV, ROBOT_ARM_UMV, ROBOT_ARM_UXV, ROBOT_ARM_MAX);
        TJUtility.addToArray(FIELD_GENERATORS, FIELD_GENERATOR_ULV, FIELD_GENERATOR_LV, FIELD_GENERATOR_MV, FIELD_GENERATOR_HV, FIELD_GENERATOR_EV, FIELD_GENERATOR_IV, FIELD_GENERATOR_LUV, FIELD_GENERATOR_ZPM, FIELD_GENERATOR_UV, FIELD_GENERATOR_UHV, FIELD_GENERATOR_UEV, FIELD_GENERATOR_UIV, FIELD_GENERATOR_UMV, FIELD_GENERATOR_UXV, FIELD_GENERATOR_MAX);
        TJUtility.addToArray(EMITTERS, EMITTER_ULV, EMITTER_LV, EMITTER_MV, EMITTER_HV, EMITTER_EV, EMITTER_IV, EMITTER_LUV, EMITTER_ZPM, EMITTER_UV, EMITTER_UHV, EMITTER_UEV, EMITTER_UIV, EMITTER_UMV, EMITTER_UXV, EMITTER_MAX);
        TJUtility.addToArray(SENSORS, SENSOR_ULV, SENSOR_LV, SENSOR_MV, SENSOR_HV, SENSOR_EV, SENSOR_IV, SENSOR_LUV, SENSOR_ZPM, SENSOR_UV, SENSOR_UHV, SENSOR_UEV, SENSOR_UIV, SENSOR_UMV, SENSOR_UXV, SENSOR_MAX);
        TJUtility.addToArray(FLUID_REGULATORS, FLUID_REGULATOR_ULV, FLUID_REGULATOR_LV, FLUID_REGULATOR_MV, FLUID_REGULATOR_HV, FLUID_REGULATOR_EV, FLUID_REGULATOR_IV, FLUID_REGULATOR_LUV, FLUID_REGULATOR_ZPM, FLUID_REGULATOR_UV, FLUID_REGULATOR_UHV, FLUID_REGULATOR_UEV, FLUID_REGULATOR_UIV, FLUID_REGULATOR_UMV, FLUID_REGULATOR_UXV, FLUID_REGULATOR_MAX);

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

    @Nonnull
    @Override
    public ItemStack getContainerItem(ItemStack stack) {
        return stack.copy();
    }
}
