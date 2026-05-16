package tj.items.behaviours;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import gregicadditions.GAValues;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.capability.impl.ElectricItem;
import gregtech.api.items.armor.ArmorMetaItem;
import gregtech.api.items.armor.ISpecialArmorLogic;
import gregtech.api.items.metaitem.ElectricStats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.ISpecialArmor;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import tj.util.TJItemUtils;

import javax.annotation.Nonnull;

public class ModularArmorBehaviour implements ISpecialArmorLogic {

    private final EntityEquipmentSlot equipmentSlot;

    public ModularArmorBehaviour(EntityEquipmentSlot equipmentSlot) {
        this.equipmentSlot = equipmentSlot;
    }

    @Override
    public ISpecialArmor.ArmorProperties getProperties(EntityLivingBase entityLivingBase, @Nonnull ItemStack itemStack, DamageSource damageSource, double v, EntityEquipmentSlot entityEquipmentSlot) {
        return new ISpecialArmor.ArmorProperties(0, 0, Integer.MAX_VALUE);
    }

    @Override
    public int getArmorDisplay(EntityPlayer entityPlayer, @Nonnull ItemStack itemStack, int i) {
        final IElectricItem electricItem = itemStack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        return electricItem != null && electricItem.getCharge() > 0 ? (6 + TJItemUtils.getCompoundFromStack(itemStack).getInteger("tier")) : 3;
    }

    @Override
    public EntityEquipmentSlot getEquipmentSlot(ItemStack itemStack) {
        return this.equipmentSlot;
    }

    @Override
    public void addToolComponents(ArmorMetaItem.ArmorMetaValueItem metaValueItem) {
        metaValueItem.addStats(new ElectricStats(0, 0, true, false) {
            @Override
            public ICapabilityProvider createProvider(ItemStack itemStack) {
                return new ElectricItem(itemStack, 0, 0, true, false) {
                    @Override
                    public long getTransferLimit() {
                        return GAValues.V[this.getTier()];
                    }

                    @Override
                    public int getTier() {
                        final NBTTagCompound compound = this.itemStack.getTagCompound();
                        return compound != null ? compound.getInteger("tier") : super.getTier();
                    }
                };
            }
        });
    }

    @Override
    public void damageArmor(EntityLivingBase entityLivingBase, ItemStack itemStack, DamageSource damageSource, int damage, EntityEquipmentSlot entityEquipmentSlot) {
        final IElectricItem electricItem = itemStack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if (electricItem == null) return;
        electricItem.discharge(damage, Math.max(32, 32 * TJItemUtils.getCompoundFromStack(itemStack).getInteger("tier")), true, false, false);
    }

    @Override
    public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot slot, ItemStack itemStack) {
        return ImmutableMultimap.of();
    }

    @Override
    public String getArmorTexture(ItemStack itemStack, Entity entity, EntityEquipmentSlot slot, String s) {
        final IElectricItem item = itemStack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if (item != null && item.getCharge() > 0)
            return slot == EntityEquipmentSlot.HEAD || slot == EntityEquipmentSlot.CHEST ? "gregtech:textures/armor/e2_helmet_chest.png" : "gregtech:textures/armor/e2_leggings_boots.png";
        return slot == EntityEquipmentSlot.HEAD || slot == EntityEquipmentSlot.CHEST ? "gregtech:textures/armor/e1_helmet_chest.png" : "gregtech:textures/armor/e1_leggings_boots.png";
    }
}
