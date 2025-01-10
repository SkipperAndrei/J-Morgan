package org.poo.command;

import org.poo.account.Account;
import org.poo.account.BusinessAccount;
import org.poo.database.UserDatabase;
import org.poo.fileio.CommandInput;
import org.poo.user.User;
import org.poo.utils.OutputGenerator;

public class AddNewBusinessAssociate implements Command {

    private String associateEmail;
    private String role;
    private String account;
    private int timestamp;

    public AddNewBusinessAssociate(CommandInput command) {
        associateEmail = command.getEmail();
        role = command.getRole();
        account = command.getAccount();
        timestamp = command.getTimestamp();
    }

    public void addAssociate(BusinessAccount acc) {

        User user = UserDatabase.getInstance().getUserEntry(associateEmail);
        String name = user.getUserData().getLastName() + " " + user.getUserData().getFirstName();

        acc.addAssociate(associateEmail, role, name);
        user.getUserAccounts().put(acc.getIban(), acc);
    }

    @Override
    public void executeCommand(UserDatabase userDatabase) {

        try {

            String userEmail = userDatabase.getMailEntry(account);
            User owner = userDatabase.getUserEntry(userEmail);
            Account acc = owner.getUserAccounts().get(account);
            ((BusinessAccount) acc).getDepositLimit();
            addAssociate((BusinessAccount) acc);

        } catch (ClassCastException e) {
            return;
        }
    }

    @Override
    public void generateOutput(OutputGenerator outputGenerator) {
        return;
    }
}
