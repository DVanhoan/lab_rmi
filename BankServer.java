import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class BankServer {
    public static void main(String[] args) {
        try {
            System.setProperty("java.rmi.server.hostname", "192.168.1.6");

            int port = 3000;
            String dbFile = "bank.db";
            String peerHost = "192.168.160.129";
            int peerPort = 2021;

            BankImpl impl = new BankImpl(dbFile);
            Registry registry = LocateRegistry.createRegistry(port);
            registry.rebind("BankService", impl);
            System.out.println("BankServer running on port " + port);

            if (peerHost != null)
                impl.setRemotePeer(peerHost, peerPort);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
