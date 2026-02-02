package tj.integration.appeng.client.gui.implementations;

import appeng.api.config.LockCraftingMode;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.client.gui.implementations.GuiUpgradeable;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.client.gui.widgets.GuiImgLabel;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.client.gui.widgets.GuiToggleButton;
import appeng.core.localization.GuiText;
import appeng.core.sync.GuiBridge;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketConfigButton;
import appeng.core.sync.packets.PacketSwitchGuis;
import appeng.helpers.IInterfaceHost;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import org.lwjgl.input.Mouse;
import tj.integration.appeng.container.implementations.ContainerSuperInterface;

import java.io.IOException;

public class GuiSuperInterface extends GuiUpgradeable {

    private GuiTabButton priority;
    private GuiImgButton UnlockMode;
    private GuiImgButton BlockMode;
    private GuiToggleButton interfaceMode;
    private GuiImgLabel lockReason;

    public GuiSuperInterface(final InventoryPlayer inventoryPlayer, final IInterfaceHost te) {
        super(new ContainerSuperInterface(inventoryPlayer, te));
        this.ySize = 256;
    }

    @Override
    public void initGui() {
        super.initGui();
        this.addLabel();
    }

    @Override
    protected void addButtons() {
        this.priority = new GuiTabButton(this.guiLeft + 154, this.guiTop, 2 + 4 * 16, GuiText.Priority.getLocal(), this.itemRender);
        this.buttonList.add(this.priority);

        this.BlockMode = new GuiImgButton(this.guiLeft - 18, this.guiTop + 8, Settings.BLOCK, YesNo.NO);
        this.buttonList.add(this.BlockMode);

        this.UnlockMode = new GuiImgButton(this.guiLeft - 18, this.guiTop + 26, Settings.UNLOCK, LockCraftingMode.NONE);
        this.buttonList.add(this.UnlockMode);

        this.interfaceMode = new GuiToggleButton(this.guiLeft - 18, this.guiTop + 44, 84, 85, GuiText.InterfaceTerminal.getLocal(), GuiText.InterfaceTerminalHint.getLocal());
        this.buttonList.add(this.interfaceMode);
    }

    protected void addLabel() {
        if (lockReason != null) {
            labelList.remove(this.lockReason);
        }
        this.lockReason = new GuiImgLabel(this.fontRenderer, guiLeft + 40, guiTop + 12, Settings.UNLOCK, LockCraftingMode.NONE);
        this.lockReason.setVisibility(false);
        labelList.add(lockReason);
    }

    @Override
    public void drawFG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        if (this.BlockMode != null) {
            this.BlockMode.set(((ContainerSuperInterface) this.cvb).getBlockingMode());
        }

        if (this.UnlockMode != null) {
            this.UnlockMode.set(((ContainerSuperInterface) this.cvb).getUnlockMode());

            if (this.lockReason != null) {
                if (this.UnlockMode.getCurrentValue() == LockCraftingMode.NONE) {
                    this.lockReason.setVisibility(false);
                } else {
                    this.lockReason.setVisibility(true);
                    this.lockReason.set(((ContainerSuperInterface) this.cvb).getCraftingLockedReason());
                }
            }
        }

        if (this.interfaceMode != null) {
            this.interfaceMode.setState(((ContainerSuperInterface) this.cvb).getInterfaceTerminalMode() == YesNo.YES);
        }

        this.fontRenderer.drawString(this.getGuiDisplayName(GuiText.Interface.getLocal()), 8, 6, 4210752);

        this.fontRenderer.drawString(GuiText.Config.getLocal(), 8, 6 + 11 + 7, 4210752);
        this.fontRenderer.drawString(GuiText.StoredItems.getLocal(), 8, 6 + 60 + 7, 4210752);
        this.fontRenderer.drawString(GuiText.Patterns.getLocal(), 8, 6 + 73 + 7, 4210752);

    }

    @Override
    protected String getBackground() {
        int upgrades = ((ContainerSuperInterface) this.cvb).getPatternUpgrades();
        if (upgrades == 0) {
            return "guis/super_interface.png";
        } else {
            return "guis/super_interface" + upgrades + ".png";
        }
    }

    @Override
    protected void actionPerformed(final GuiButton btn) throws IOException {
        super.actionPerformed(btn);

        final boolean backwards = Mouse.isButtonDown(1);

        if (btn == this.priority) {
            NetworkHandler.instance().sendToServer(new PacketSwitchGuis(GuiBridge.GUI_PRIORITY));
        }

        if (btn == this.interfaceMode) {
            NetworkHandler.instance().sendToServer(new PacketConfigButton(Settings.INTERFACE_TERMINAL, backwards));
        }

        if (btn == this.BlockMode) {
            NetworkHandler.instance().sendToServer(new PacketConfigButton(this.BlockMode.getSetting(), backwards));
        }

        if (btn == this.UnlockMode) {
            NetworkHandler.instance.sendToServer(new PacketConfigButton(this.UnlockMode.getSetting(), backwards));
        }
    }
}
