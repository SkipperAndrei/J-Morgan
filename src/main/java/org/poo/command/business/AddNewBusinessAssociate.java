package org.poo.command.business;

import org.poo.account.Account;
import org.poo.account.BusinessAccount;
import org.poo.command.Command;
import org.poo.command.CommandConstants;
import org.poo.database.UserDatabase;
import org.poo.fileio.CommandInput;
import org.poo.user.User;
import org.poo.utils.OutputGenerator;

public final class AddNewBusinessAssociate implements Command {

    private String associateEmail;
    private String role;
    private String account;
    private int timestamp;
    private CommandConstants actionCode = CommandConstants.SUCCESS;

    public AddNewBusinessAssociate(final CommandInput command) {
        associateEmail = command.getEmail();
        role = command.getRole();
        account = command.getAccount();
        timestamp = command.getTimestamp();
    }

    /**
     * This function checks if a user can be added as an employee associate
     * @param acc The business account
     */
    public void addAssociate(final BusinessAccount acc) {

        User user = UserDatabase.getInstance().getUserEntry(associateEmail);
        String name = user.getUserData().getLastName() + " " + user.getUserData().getFirstName();

        boolean canAdd = acc.addAssociate(associateEmail, role, name);

        if (!canAdd) {
            actionCode = CommandConstants.ALREADY_ASSOCIATE;
            return;
        }

        user.getUserAccounts().put(acc.getIban(), acc);
    }

    @Override
    public void executeCommand(final UserDatabase userDatabase) {

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
    public void generateOutput(final OutputGenerator outputGenerator) {
        return;
    }
}
