package tj.integration.ae2;

import tj.mui.widgets.impl.AEGhostItemListWidget;

import java.util.function.LongUnaryOperator;

public interface ISuperInterfaceTerminal {

    void setItemStackSize(AEGhostItemListWidget<?> ghostItemListWidget, LongUnaryOperator unaryOperator);

    int getItemStackSize(AEGhostItemListWidget<?> ghostItemListWidget);
}
