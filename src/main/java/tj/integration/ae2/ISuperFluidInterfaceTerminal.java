package tj.integration.ae2;

import tj.mui.widgets.impl.AEGhostFluidListWidget;

import java.util.function.LongUnaryOperator;

public interface ISuperFluidInterfaceTerminal {

    void setFluidStackSize(AEGhostFluidListWidget<?> ghostFluidListWidget, LongUnaryOperator unaryOperator);

    int getFluidStackSize(AEGhostFluidListWidget<?> ghostFluidListWidget);
}
