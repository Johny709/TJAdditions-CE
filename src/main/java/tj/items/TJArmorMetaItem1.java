package tj.items;

import gregtech.api.items.armor.ArmorMetaItem;
import net.minecraft.inventory.EntityEquipmentSlot;
import tj.items.behaviours.ModularArmorBehaviour;

import static tj.items.TJMetaItems.*;

public class TJArmorMetaItem1 extends ArmorMetaItem<ArmorMetaItem<?>.ArmorMetaValueItem> {

    @Override
    public void registerSubItems() {
        MODULAR_ARMOR_HEAD = addItem(0, "modular_armor_head").setArmorLogic(new ModularArmorBehaviour(EntityEquipmentSlot.HEAD));
        MODULAR_ARMOR_CHEST = addItem(1, "modular_armor_chest").setArmorLogic(new ModularArmorBehaviour(EntityEquipmentSlot.CHEST));
        MODULAR_ARMOR_LEGS = addItem(2, "modular_armor_legs").setArmorLogic(new ModularArmorBehaviour(EntityEquipmentSlot.LEGS));
        MODULAR_ARMOR_BOOTS = addItem(3, "modular_armor_boots").setArmorLogic(new ModularArmorBehaviour(EntityEquipmentSlot.FEET));
    }
}
