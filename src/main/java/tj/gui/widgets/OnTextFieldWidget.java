package tj.gui.widgets;

import net.minecraft.network.PacketBuffer;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Similar to {@link TJTextFieldWidget} but text supplier and responder are only initialized and updated by other widgets.
 */
@Deprecated
public class OnTextFieldWidget extends TJTextFieldWidget {

    public OnTextFieldWidget(int xPosition, int yPosition, int width, int height, boolean enableBackground, Supplier<String> textSupplier, Consumer<String> textResponder) {
        super(xPosition, yPosition, width, height, enableBackground, textSupplier, textResponder);
        this.currentString = textSupplier.get();
    }

    public void onResponder(ClickData clickData) {
        this.textResponder.accept(this.currentString);
    }

    @Override
    public void detectAndSendChanges() {
        this.writeUpdateInfo(1, buffer -> buffer.writeString(this.currentString));
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        if (id == 1) {
            String clientText = buffer.readString(Short.MAX_VALUE);
            clientText = clientText.substring(0, Math.min(clientText.length(), this.maxStringLength));
            if (this.textValidator.test(clientText)) {
                this.currentString = clientText;
            }
        }
    }
}
