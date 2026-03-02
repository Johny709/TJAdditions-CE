package tj.gui;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.AbstractWidgetGroup;
import gregtech.api.gui.widgets.tab.ITabInfo;
import gregtech.api.gui.widgets.tab.TabListRenderer;
import gregtech.api.util.Position;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import mezz.jei.api.gui.IGhostIngredientHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Tuple;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tj.gui.widgets.PopUpWidgetGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class TJTabGroup extends AbstractWidgetGroup {

    private final List<ITabInfo> tabInfos = new ArrayList<>();
    private final Int2ObjectMap<AbstractWidgetGroup> tabWidgets = new Int2ObjectOpenHashMap<>();
    private int selectedTabIndex = 0;
    private final TabListRenderer tabListRenderer;

    public TJTabGroup(Supplier<TabListRenderer> tabLocation, Position position) {
        super(position);
        this.tabListRenderer = tabLocation.get();
    }

    public void addTab(ITabInfo tabInfo, AbstractWidgetGroup tabWidget) {
        this.tabInfos.add(tabInfo);
        int tabIndex = tabInfos.size() - 1;
        this.tabWidgets.put(tabIndex, tabWidget);
        tabWidget.setVisible(tabIndex == selectedTabIndex);
        addWidget(tabWidget);
    }

    @Override
    public List<Widget> getContainedWidgets(boolean includeHidden) {
        ArrayList<Widget> containedWidgets = new ArrayList<>(this.widgets.size());

        if (includeHidden) {
            for (AbstractWidgetGroup widget : this.tabWidgets.values()) {
                containedWidgets.add(widget);
                if (widget instanceof PopUpWidgetGroup)
                    continue;
                if (widget instanceof AbstractWidgetGroup)
                    containedWidgets.addAll(widget.getContainedWidgets(true));
            }
        } else {
            AbstractWidgetGroup widgetGroup = tabWidgets.get(selectedTabIndex);
            containedWidgets.add(widgetGroup);
            containedWidgets.addAll(widgetGroup.getContainedWidgets(false));
        }

        return containedWidgets;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInBackground(int mouseX, int mouseY, IRenderContext context) {
        this.tabWidgets.get(this.selectedTabIndex).drawInBackground(mouseX, mouseY, context);
        this.tabListRenderer.renderTabs(getPosition(), tabInfos, sizes.getWidth(), sizes.getHeight(), selectedTabIndex);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInForeground(int mouseX, int mouseY) {
        this.tabWidgets.get(this.selectedTabIndex).drawInForeground(mouseX, mouseY);
        Tuple<ITabInfo, int[]> tabOnMouse = getTabOnMouse(mouseX, mouseY);
        if (tabOnMouse != null) {
            int[] tabSizes = tabOnMouse.getSecond();
            ITabInfo tabInfo = tabOnMouse.getFirst();
            boolean isSelected = tabInfos.get(selectedTabIndex) == tabInfo;
            tabInfo.renderHoverText(tabSizes[0], tabSizes[1], tabSizes[2], tabSizes[3], sizes.getWidth(), sizes.getHeight(), isSelected, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        Tuple<ITabInfo, int[]> tabOnMouse = getTabOnMouse(mouseX, mouseY);
        if (tabOnMouse != null) {
            ITabInfo tabInfo = tabOnMouse.getFirst();
            int tabIndex = tabInfos.indexOf(tabInfo);
            if (selectedTabIndex != tabIndex) {
                setSelectedTab(tabIndex);
                playButtonClickSound();
                writeClientAction(2, buf -> buf.writeVarInt(tabIndex));
                return true;
            }
        }
        return this.tabWidgets.get(this.selectedTabIndex).mouseClicked(mouseX, mouseY, button);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseDragged(int mouseX, int mouseY, int button, long timeDragged) {
        return this.tabWidgets.get(this.selectedTabIndex).mouseDragged(mouseX, mouseY, button, timeDragged);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseWheelMove(int mouseX, int mouseY, int wheelDelta) {
        return this.tabWidgets.get(this.selectedTabIndex).mouseWheelMove(mouseX, mouseY, wheelDelta);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseReleased(int mouseX, int mouseY, int button) {
        return this.tabWidgets.get(this.selectedTabIndex).mouseReleased(mouseX, mouseY, button);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateScreen() {
        this.tabWidgets.get(this.selectedTabIndex).updateScreen();
    }

    private void setSelectedTab(int tabIndex) {
        this.tabWidgets.get(selectedTabIndex).setVisible(false);
        this.tabWidgets.get(tabIndex).setVisible(true);
        this.selectedTabIndex = tabIndex;
    }

    @Override
    public void detectAndSendChanges() {
        this.tabWidgets.get(this.selectedTabIndex).detectAndSendChanges();
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        super.handleClientAction(id, buffer);
        if (id == 2) {
            int tabIndex = buffer.readVarInt();
            if (selectedTabIndex != tabIndex) {
                setSelectedTab(tabIndex);
            }
        }
    }

    @Override
    public Object getIngredientOverMouse(int mouseX, int mouseY) {
        return this.tabWidgets.get(this.selectedTabIndex).getIngredientOverMouse(mouseX, mouseY);
    }

    @Override
    public List<IGhostIngredientHandler.Target<?>> getPhantomTargets(Object ingredient) {
        return this.tabWidgets.get(this.selectedTabIndex).getPhantomTargets(ingredient);
    }

    private Tuple<ITabInfo, int[]> getTabOnMouse(int mouseX, int mouseY) {
        for (int tabIndex = 0; tabIndex < tabInfos.size(); tabIndex++) {
            ITabInfo tabInfo = tabInfos.get(tabIndex);
            int[] tabSizes = tabListRenderer.getTabPos(tabIndex, sizes.getWidth(), sizes.getHeight());
            tabSizes[0] += getPosition().x;
            tabSizes[1] += getPosition().y;
            if (isMouseOverTab(mouseX, mouseY, tabSizes)) {
                return new Tuple<>(tabInfo, tabSizes);
            }
        }
        return null;
    }

    private static boolean isMouseOverTab(int mouseX, int mouseY, int[] tabSizes) {
        int minX = tabSizes[0];
        int minY = tabSizes[1];
        int maxX = tabSizes[0] + tabSizes[2];
        int maxY = tabSizes[1] + tabSizes[3];
        return mouseX >= minX && mouseY >= minY && mouseX < maxX && mouseY < maxY;
    }

    @Override
    public boolean isWidgetVisible(Widget widget) {
        return tabWidgets.containsKey(selectedTabIndex) && tabWidgets.get(selectedTabIndex) == widget;
    }
}
