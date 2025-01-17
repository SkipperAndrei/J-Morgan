package org.poo.command.account;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.account.Account;
import org.poo.account.SavingAccount;
import org.poo.command.Command;
import org.poo.command.CommandConstants;
import org.poo.database.UserDatabase;
import org.poo.fileio.CommandInput;
import org.poo.utils.OutputGenerator;
import org.poo.user.User;

public final class AddInterest implements Command {

    private String email;
    private String account;
    private int timestamp;
    private double amountAdded;
    private CommandConstants actionCode = CommandConstants.NOT_FOUND;

    public AddInterest(final CommandInput command) {
        account = command.getAccount();
        timestamp = command.getTimestamp();
    }

    /**
     * This function will check if the account is a savings or a classic acc
     * If the account is a savings account, it will increase the balance with the interest rate
     * Also, if it's a savings accont, it will send a success signal
     * If it's a saving account it will send an error signal
     * @param acc The account queried
     */
    public void checkAccount(final Account acc) {

        try {

            amountAdded = acc.getBalance() * ((SavingAccount) acc).getInterestRate();
            acc.incrementFunds(amountAdded);
            actionCode = CommandConstants.SUCCESS;
        } catch (ClassCastException e) {

            actionCode = CommandConstants.CLASSIC_ACC;
        }
    }

    @Override
    public void executeCommand(final UserDatabase userDatabase) {

        for (User user : userDatabase.getDatabase().values()) {

            if (user.getUserAccounts().containsKey(account)) {

                email = user.getUserData().getEmail();
                checkAccount(user.getUserAccounts().get(account));
                return;
            }
        }

    }

    @Override
    public void generateOutput(final OutputGenerator outputGenerator) {

        switch (actionCode) {

            case SUCCESS:

                ObjectNode successNode = outputGenerator.getMapper().createObjectNode();
                successNode.put("amount", amountAdded);
                successNode.put("currency", outputGenerator.getUserDatabase().getUserEntry(email).
                                            getUserAccounts().get(account).getCurrency());
                successNode.put("description", "Interest rate income");
                successNode.put("timestamp", timestamp);


                outputGenerator.getUserDatabase().getUserEntry(email).addTransaction(successNode);
                outputGenerator.getUserDatabase().getUserEntry(email).getUserAccounts().
                        get(account).addTransaction(successNode);
                break;

            case CLASSIC_ACC:

                outputGenerator.errorSetting(timestamp, "This is not a savings account",
                                    "addInterest");
                break;

            case NOT_FOUND:

                outputGenerator.errorSetting(timestamp, "Account not found", "addInterest");
                break;

            default:
                break;
        }
    }
}
