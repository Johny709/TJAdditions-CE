package tj.items.behaviours;

import gregtech.api.GTValues;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ToggleButtonWidget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.items.gui.ItemUIFactory;
import gregtech.api.items.gui.PlayerInventoryHolder;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.items.metaitem.stats.IItemCapabilityProvider;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;
import tj.gui.TJGuiUtils;
import tj.gui.widgets.NewTextFieldWidget;
import tj.gui.widgets.impl.TJToggleButtonWidget;

import java.util.List;
import java.util.regex.Pattern;

import static tj.gui.TJGuiTextures.*;

public class GaugeDropperBehavior implements IItemBehaviour, ItemUIFactory, IItemCapabilityProvider {

    private static final ResourceLocation OVERRIDE_KEY_LOCATION = new ResourceLocation(GTValues.MODID, "gauge_dropper_void");

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        if (!world.isRemote)
            PlayerInventoryHolder.openHandItemUI(player, hand);
        return ActionResult.newResult(EnumActionResult.PASS, player.getHeldItemMainhand());
    }

    @Override
    public void onAddedToItem(MetaItem.MetaValueItem metaValueItem) {
        metaValueItem.getMetaItem().addPropertyOverride(OVERRIDE_KEY_LOCATION, (stack, world, entity) -> this.isVoiding(stack) ? 1.0F : 0.0F);
    }

    @Override
    public ModularUI createUI(PlayerInventoryHolder holder, EntityPlayer player) {
        final ItemStack playerStack = player.getHeldItemMainhand();
        final NBTTagCompound compound = playerStack.getOrCreateSubCompound("settings");
        return ModularUI.defaultBuilder()
                .widget(new NewTextFieldWidget<>(27, 30, 119, 18, true, () -> String.valueOf(compound.getInteger("capacity")), (text, id) -> compound.setInteger("capacity", (int) Math.min(Integer.MAX_VALUE, Long.parseLong(text))))
                        .setValidator(str -> Pattern.compile("\\*?[0-9_]*\\*?").matcher(str).matches())
                        .setUpdateOnTyping(true)
                        .setMaxStringLength(10))
                .widget(new ToggleButtonWidget(7, 30, 18, 18, PLUS_BUTTON, () -> false, plus -> compound.setInteger("capacity", Math.max(1, Math.min(1000, compound.getInteger("capacity") * 2)))))
                .widget(new ToggleButtonWidget(151, 30, 18, 18, MINUS_BUTTON, () -> false, minus -> compound.setInteger("capacity", Math.max(1, compound.getInteger("capacity") / 2))))
                .widget(new ToggleButtonWidget(151, 48, 18, 18, RESET_BUTTON, () -> false, reset -> compound.setInteger("capacity", 1000)))
                .widget(new TJToggleButtonWidget(27, 48, 119, 18, () -> compound.getBoolean("voiding"), (bool, str) -> compound.setBoolean("voiding", bool))
                        .setToggleDisplayText("machine.universal.toggle.fluid_voiding.disabled", "machine.universal.toggle.fluid_voiding.enabled")
                        .setToggleTexture(GuiTextures.TOGGLE_BUTTON_BACK)
                        .useToggleTexture(true))
                .widget(TJGuiUtils.bindPlayerInventory(new WidgetGroup(), player.inventory, 7, 84, playerStack))
                .bindCloseListener(() -> playerStack.getOrCreateSubCompound("settings").merge(compound))
                .label(50, 4, "metaitem.gauge_dropper.name")
                .build(holder, player);
    }

    @Override
    public ICapabilityProvider createProvider(ItemStack itemStack) {
        return new FluidHandlerItemStack(itemStack, 1000) {
            @Override
            public int fill(FluidStack resource, boolean doFill) {
                if (this.container.getCount() != 1 || resource == null || resource.amount <= 0 || !this.canFillFluidType(resource)) {
                    return 0;
                }
                final FluidStack contained = this.getFluid();
                final int capacity = this.container.getOrCreateSubCompound("settings").getInteger("capacity");
                final boolean voiding = this.container.getOrCreateSubCompound("settings").getBoolean("voiding");
                if (contained == null || voiding) {
                    final int fillAmount = Math.min(resource.amount, Math.min(voiding ? Integer.MAX_VALUE : this.capacity, capacity));
                    if (doFill && !voiding) {
                        final FluidStack filled = resource.copy();
                        filled.amount = fillAmount;
                        this.setFluid(filled);
                    }
                    return fillAmount;
                } else {
                    if (contained.isFluidEqual(resource)) {
                        final int fillAmount = Math.min(resource.amount, Math.min(this.capacity, capacity) - contained.amount);
                        if (doFill && fillAmount > 0) {
                            contained.amount += fillAmount;
                            this.setFluid(contained);
                        }
                        return fillAmount;
                    }
                    return 0;
                }
            }
        };
    }

    private boolean isVoiding(ItemStack stack) {
        return stack.getOrCreateSubCompound("settings").getBoolean("voiding");
    }

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
        lines.add(I18n.format("metaitem.void_plunger.mode", this.isVoiding(itemStack)));
    }
}
