package org.poo.command.account;

import org.poo.account.Account;
import org.poo.account.BusinessAccount;
import org.poo.command.Command;
import org.poo.command.CommandConstants;
import org.poo.database.UserDatabase;
import org.poo.fileio.CommandInput;
import org.poo.utils.OutputGenerator;
import lombok.Data;
import org.poo.user.User;

@Data
public final class AddFunds implements Command {

    private String account;
    private String email;
    private double amount;
    private int timestamp;
    private CommandConstants actionCode = CommandConstants.SUCCESS;

    public AddFunds(final CommandInput command) {
        account = command.getAccount();
        email = command.getEmail();
        amount = command.getAmount();
        timestamp = command.getTimestamp();
    }

    @Override
    public void executeCommand(final UserDatabase userDatabase) {

        User usr = userDatabase.getUserEntry(email);

        if (usr.getUserAccounts().containsKey(account)) {

            try {
                Account acc = usr.getUserAccounts().get(account);
                ((BusinessAccount) acc).getDepositLimit();
                boolean canAdd = ((BusinessAccount) acc).addFundsCheck(amount, email, timestamp);

                if (!canAdd) {
                    actionCode = CommandConstants.DEPOSIT_LIMIT;
                }
            } catch (ClassCastException e) {
                usr.getUserAccounts().get(account).incrementFunds(amount);
                return;
            }

        }

    }

    @Override
    public void generateOutput(final OutputGenerator outputGenerator) {

//        if (actionCode.equals(CommandConstants.DEPOSIT_LIMIT)) {
//            outputGenerator.errorSetting(timestamp,
//                        "You are not authorised to make this tranzaction", "addFunds");
//        }
        return;
    }
}
