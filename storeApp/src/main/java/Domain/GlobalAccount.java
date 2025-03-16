package Domain;

public class GlobalAccount {
    private static Object account;

    public static Object getAccount() {
        return account;
    }

    public static void setAccount(Object newAccount) {
        if (newAccount instanceof User || newAccount instanceof Admin || newAccount == null) {
            account = newAccount;
        } else {
            throw new IllegalArgumentException("Account type must be 'User' or 'Admin'.");
        }
    }
}

