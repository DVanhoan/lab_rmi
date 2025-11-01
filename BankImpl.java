import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class BankImpl extends UnicastRemoteObject implements Interf {
    private String dbPath;
    private Interf remotePeer;

    public BankImpl(String dbPath) throws RemoteException {
        this.dbPath = dbPath;
        initDatabase();
    }

    public void setRemotePeer(String host, int port) {
        try {
            Registry registry = LocateRegistry.getRegistry(host, port);
            this.remotePeer = (Interf) registry.lookup("BankService");
            System.out.println("Connected to remote peer at " + host + ":" + port);
        } catch (Exception e) {
            System.out.println("Remote peer unavailable now.");
        }
    }

    private boolean tableExists(Connection conn, String table) throws SQLException {
        ResultSet rs = conn.getMetaData().getTables(null, null, table, null);
        return rs.next();
    }

    private void initDatabase() {
        try (Connection conn = getConnection()) {

            if (!tableExists(conn, "users")) {
                Statement st = conn.createStatement();
                st.executeUpdate(
                        "CREATE TABLE users(username TEXT PRIMARY KEY, password TEXT, balance REAL, logged_in INTEGER)");
                st.executeUpdate("INSERT INTO users VALUES('alice','123',1000,0)");
                st.executeUpdate("INSERT INTO users VALUES('bob','123',1500,0)");
                System.out.println("Table created and default users inserted.");
            } else {
                System.out.println("Users table already exists → skip.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + dbPath);
    }

    @Override
    public boolean login(String username, String password) throws RemoteException {
        try (Connection conn = getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE username=? AND password=?");
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                if (rs.getInt("logged_in") == 1)
                    return false;
                PreparedStatement upd = conn.prepareStatement("UPDATE users SET logged_in=1 WHERE username=?");
                upd.setString(1, username);
                upd.executeUpdate();
                syncState(username);
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void logout(String username) throws RemoteException {
        try (Connection conn = getConnection()) {
            PreparedStatement upd = conn.prepareStatement("UPDATE users SET logged_in=0 WHERE username=?");
            upd.setString(1, username);
            upd.executeUpdate();
            syncState(username);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public double getBalance(String username) throws RemoteException {
        try (Connection conn = getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT balance FROM users WHERE username=?");
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getDouble("balance");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public void deposit(String username, double amount) throws RemoteException {
        try (Connection conn = getConnection()) {
            PreparedStatement ps = conn.prepareStatement("UPDATE users SET balance=balance+? WHERE username=?");
            ps.setDouble(1, amount);
            ps.setString(2, username);
            ps.executeUpdate();
            syncState(username);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void withdraw(String username, double amount) throws RemoteException {
        try (Connection conn = getConnection()) {
            PreparedStatement ps = conn.prepareStatement("UPDATE users SET balance=balance-? WHERE username=?");
            ps.setDouble(1, amount);
            ps.setString(2, username);
            ps.executeUpdate();
            syncState(username);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean transfer(String fromUser, String toUser, double amount) throws RemoteException {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            PreparedStatement ps = conn.prepareStatement("SELECT balance FROM users WHERE username=?");
            ps.setString(1, fromUser);
            ResultSet rs = ps.executeQuery();
            if (!rs.next() || rs.getDouble("balance") < amount) {
                conn.rollback();
                return false;
            }

            PreparedStatement ps2 = conn.prepareStatement("SELECT * FROM users WHERE username=?");
            ps2.setString(1, toUser);
            ResultSet rs2 = ps2.executeQuery();
            if (!rs2.next()) {
                conn.rollback();
                return false;
            }

            PreparedStatement pSend = conn.prepareStatement("UPDATE users SET balance = balance - ? WHERE username=?");
            pSend.setDouble(1, amount);
            pSend.setString(2, fromUser);
            pSend.executeUpdate();

            PreparedStatement pRecv = conn.prepareStatement("UPDATE users SET balance = balance + ? WHERE username=?");
            pRecv.setDouble(1, amount);
            pRecv.setString(2, toUser);
            pRecv.executeUpdate();

            conn.commit();

            syncState(fromUser);
            syncState(toUser);

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean changePassword(String username, String newPass) throws RemoteException {
        try (Connection conn = getConnection()) {
            PreparedStatement ps = conn.prepareStatement("UPDATE users SET password=? WHERE username=?");
            ps.setString(1, newPass);
            ps.setString(2, username);
            int changed = ps.executeUpdate();

            syncState(username);

            return changed > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void syncState(String username) {
        if (remotePeer == null)
            return;
        try (Connection conn = getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT balance, logged_in FROM users WHERE username=?");
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                double balance = rs.getDouble("balance");
                boolean loggedIn = rs.getInt("logged_in") == 1;
                remotePeer.syncUpdate(username, balance, loggedIn);
            }
        } catch (Exception e) {
            System.out.println("Sync failed: " + e);
        }
    }

    @Override
    public void syncUpdate(String username, double newBalance, boolean loginState) throws RemoteException {
        try (Connection conn = getConnection()) {
            PreparedStatement ps = conn.prepareStatement("UPDATE users SET balance=?, logged_in=? WHERE username=?");
            ps.setDouble(1, newBalance);
            ps.setInt(2, loginState ? 1 : 0);
            ps.setString(3, username);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
