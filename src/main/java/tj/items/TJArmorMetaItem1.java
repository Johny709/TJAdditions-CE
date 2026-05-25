package tj.items;

import gregicadditions.GAValues;
import gregtech.api.GTValues;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.items.armor.ArmorMetaItem;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.util.GTUtility;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.ItemHandlerHelper;
import tj.TJValues;
import tj.items.behaviours.ModularArmorBehaviour;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

import static tj.items.TJMetaItems.*;

public class TJArmorMetaItem1 extends ArmorMetaItem<ArmorMetaItem<?>.ArmorMetaValueItem> {

    @Override
    public void registerSubItems() {
        MODULAR_ARMOR_HEAD = addItem(0, "modular_armor_head").setArmorLogic(new ModularArmorBehaviour(EntityEquipmentSlot.HEAD));
        MODULAR_ARMOR_CHEST = addItem(1, "modular_armor_chest").setArmorLogic(new ModularArmorBehaviour(EntityEquipmentSlot.CHEST));
        MODULAR_ARMOR_LEGS = addItem(2, "modular_armor_legs").setArmorLogic(new ModularArmorBehaviour(EntityEquipmentSlot.LEGS));
        MODULAR_ARMOR_BOOTS = addItem(3, "modular_armor_boots").setArmorLogic(new ModularArmorBehaviour(EntityEquipmentSlot.FEET));
    }

    @Override
    public void addInformation(ItemStack itemStack, @Nullable World worldIn, List<String> lines, ITooltipFlag tooltipFlag) {
        final ArmorMetaItem<?>.ArmorMetaValueItem item = getItem(itemStack);
        if (item == null) return;
        String unlocalizedTooltip = "metaitem." + item.unlocalizedName + ".tooltip";
        if (I18n.hasKey(unlocalizedTooltip)) {
            lines.addAll(Arrays.asList(I18n.format(unlocalizedTooltip).split("/n")));
        }
        final IElectricItem electricItem = itemStack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if (electricItem != null) {
            lines.add(I18n.format("metaitem.generic.electric_item.tooltip",
                    electricItem.getCharge(),
                    electricItem.getMaxCharge(),
                    TJValues.VCC[electricItem.getTier()],
                    GAValues.VN[electricItem.getTier()]));
        }
        final IFluidHandlerItem fluidHandler = ItemHandlerHelper.copyStackWithSize(itemStack, 1)
                .getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        if (fluidHandler != null) {
            final IFluidTankProperties fluidTankProperties = fluidHandler.getTankProperties()[0];
            final FluidStack fluid = fluidTankProperties.getContents();
            if (fluid != null) {
                lines.add(I18n.format("metaitem.generic.fluid_container.tooltip",
                        fluid.amount,
                        fluidTankProperties.getCapacity(),
                        fluid.getLocalizedName()));
            } else lines.add(I18n.format("metaitem.generic.fluid_container.tooltip_empty"));
        }
        for (IItemBehaviour behaviour : getBehaviours(itemStack)) {
            behaviour.addInformation(itemStack, lines);
        }
    }
}
