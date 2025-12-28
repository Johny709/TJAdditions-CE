package tj.capability;

public interface IMultiControllable extends IPageCapable {

    boolean isWorkingEnabled(int i);

    void setWorkingEnabled(boolean isActivationAllowed, int i);
}
