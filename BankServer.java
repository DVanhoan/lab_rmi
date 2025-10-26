import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class BankServer {
    public static void main(String[] args) {
        try {
            int port = Integer.parseInt(args[0]); // Ví dụ: 2020
            String dbFile = args[1]; // Ví dụ: bank1.db
            String peerHost = args.length > 2 ? args[2] : null; // host server kia
            int peerPort = args.length > 3 ? Integer.parseInt(args[3]) : 0;

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
