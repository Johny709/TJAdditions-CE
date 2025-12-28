package tj.capability;

public interface IEnderNotifiable<V> {

    void markToDirty();

    void setHandler(V handler);

    void setFrequency(String frequency);

    void setChannel(String entry);
}
