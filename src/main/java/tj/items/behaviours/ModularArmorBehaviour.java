package tj.items.behaviours;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import gregtech.api.items.armor.ISpecialArmorLogic;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.ISpecialArmor;

import javax.annotation.Nonnull;

public class ModularArmorBehaviour implements ISpecialArmorLogic {

    private final EntityEquipmentSlot equipmentSlot;

    public ModularArmorBehaviour(EntityEquipmentSlot equipmentSlot) {
        this.equipmentSlot = equipmentSlot;
    }

    @Override
    public ISpecialArmor.ArmorProperties getProperties(EntityLivingBase entityLivingBase, @Nonnull ItemStack itemStack, DamageSource damageSource, double v, EntityEquipmentSlot entityEquipmentSlot) {
        return null;
    }

    @Override
    public int getArmorDisplay(EntityPlayer entityPlayer, @Nonnull ItemStack itemStack, int i) {
        return 2;
    }

    @Override
    public EntityEquipmentSlot getEquipmentSlot(ItemStack itemStack) {
        return this.equipmentSlot;
    }

    @Override
    public void damageArmor(EntityLivingBase entityLivingBase, ItemStack itemStack, DamageSource damageSource, int i, EntityEquipmentSlot entityEquipmentSlot) {

    }

    @Override
    public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot slot, ItemStack itemStack) {
        return ImmutableMultimap.of();
    }

    @Override
    public String getArmorTexture(ItemStack itemStack, Entity entity, EntityEquipmentSlot slot, String s) {
        return slot == EntityEquipmentSlot.HEAD || slot == EntityEquipmentSlot.CHEST ? "gregtech:textures/armor/e1_helmet_chest.png" : "gregtech:textures/armor/e1_leggings_boots.png";
    }
}
