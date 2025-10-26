import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Interf extends Remote {
    boolean login(String username, String password) throws RemoteException;

    void logout(String username) throws RemoteException;

    double getBalance(String username) throws RemoteException;

    void deposit(String username, double amount) throws RemoteException;

    void withdraw(String username, double amount) throws RemoteException;

    void syncUpdate(String username, double newBalance, boolean loginState) throws RemoteException;
}
