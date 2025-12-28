package tj.gui.uifactory;

import gregtech.api.gui.IUIHolder;
import gregtech.api.gui.ModularUI;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 * Similar to {@link gregtech.api.items.gui.PlayerInventoryHolder} but this is UI on player instead of UI on player with item.
 */
public class PlayerHolder implements IUIHolder {

    protected EntityPlayer player;
    protected Object holder;

    public PlayerHolder(EntityPlayer player, Object holder) {
        this.player = player;
        this.holder = holder;
    }

    protected ModularUI createUI(EntityPlayer player) {
        IPlayerUI factory = (IPlayerUI) this.holder;
        return factory.createUI(this, player);
    }

    public void openUI() {
        PlayerUIFactory.INSTANCE.openUI(this, (EntityPlayerMP) this.player);
    }

    @Override
    public boolean isValid() {
        return this.player != null && this.holder != null;
    }

    @Override
    public boolean isRemote() {
        return this.player.getEntityWorld().isRemote;
    }

    @Override
    public void markAsDirty() {
        this.player.inventory.markDirty();
        this.player.inventoryContainer.detectAndSendChanges();
    }
}
