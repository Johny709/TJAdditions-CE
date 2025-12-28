package tj.capability;

public interface IPageCapable {

    default int getPageIndex() {
        return 0;
    }

    default int getPageSize() {
        return 0;
    }
}
