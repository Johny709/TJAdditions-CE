package tj.util.consumers;

@FunctionalInterface
public interface BiLongConsumer<U> {

    void accept(long t, U u);
}
