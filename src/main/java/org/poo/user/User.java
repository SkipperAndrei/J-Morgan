package org.poo.user;

import org.poo.account.Account;
import org.poo.fileio.UserInput;
import java.util.ArrayList;
import lombok.Data;

@Data
public class User {
    private UserInput userData = new UserInput();
    private ArrayList<Account> userAccounts = new ArrayList<>();

    public User(UserInput userData) {
        this.userData.setFirstName(userData.getFirstName());
        this.userData.setLastName(userData.getLastName());
        this.userData.setEmail(userData.getEmail());
    }

    public void addAccount(Account account) {

        if (account.getUserEmail() != userData.getEmail()) {
            // System.out.println("Incompatible user e-mails...\n");
            return;
        }

        userAccounts.add(account);
    }


}
