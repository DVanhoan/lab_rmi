import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class BankClient {
    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry("192.168.160.129", 2021);
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
                System.out.println("1. Balance 2. Deposit 3. Withdraw 4. Transfer 5. Change Password 6. Logout");
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
                } else if (c == 4) {
                    System.out.print("Transfer to (username): ");
                    String toUser = sc.next();
                    System.out.print("Amount: ");
                    double amt = sc.nextDouble();
                    if (bank.transfer(user, toUser, amt)) {
                        System.out.println("Transfer successful!");
                    } else {
                        System.out.println("Transfer failed!");
                    }
                } else if (c == 5) {
                    System.out.print("New Password: ");
                    String newPass = sc.next();
                    if (bank.changePassword(user, newPass)) {
                        System.out.println("Password changed successfully!");
                    } else {
                        System.out.println("Password change failed!");
                    }
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
