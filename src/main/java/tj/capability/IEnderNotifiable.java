package tj.capability;

public interface IEnderNotifiable<V> {

    V createHandler();

    void markToDirty();

    void setHandler(V handler);

    void setFrequency(String frequency);

    String getFrequency();

    void setChannel(String entry);

    String getChannel();
}
