import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class BankClient {
    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 2020);
            Interf bank = (Interf) registry.lookup("BankService");

            Scanner sc = new Scanner(System.in);
            System.out.print("Username: ");
            String user = sc.nextLine();
            System.out.print("Password: ");
            String pass = sc.nextLine();

            if (!bank.login(user, pass)) {
                System.out.println("Login failed or already logged in elsewhere!");
                return;
            }

            System.out.println("Welcome, " + user + "!");
            while (true) {
                System.out.println("1. Balance  2. Deposit  3. Withdraw  4. Logout");
                int c = sc.nextInt();
                if (c == 1) {
                    System.out.println("Balance: " + bank.getBalance(user));
                } else if (c == 2) {
                    System.out.print("Amount: ");
                    double amt = sc.nextDouble();
                    bank.deposit(user, amt);
                } else if (c == 3) {
                    System.out.print("Amount: ");
                    double amt = sc.nextDouble();
                    bank.withdraw(user, amt);
                } else {
                    bank.logout(user);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
