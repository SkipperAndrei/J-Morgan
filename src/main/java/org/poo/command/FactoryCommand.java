package org.poo.command;

import org.poo.database.ExchangeRateDatabase;
import org.poo.fileio.CommandInput;

public final class FactoryCommand {

    private FactoryCommand() {
        throw new UnsupportedOperationException("This is a utility class");
    }

    public static Command extractCommand(final CommandInput command,
                                         final ExchangeRateDatabase exchangeRateDatabase) {

        switch(command.getCommand()) {

            case "printUsers" :
                return new PrintUsers(command);

            case "addAccount" :
                return new AddAccount(command);

            case "deleteAccount" :
                return new DeleteAccount(command);

            case "createCard" :
                return new CreateCard(command);

            case "createOneTimeCard" :
                return new CreateOneTimeCard(command);

            case "deleteCard" :
                return new DeleteCard(command);

            case "setMinimumBalance" :
                return new SetMinimumBalance(command);

            case "addFunds" :
                return new AddFunds(command);

            case "payOnline" :
                return new PayOnline(command, exchangeRateDatabase);

            case "sendMoney" :
                return new SendMoney(command, exchangeRateDatabase);

            case "setAlias" :
                return new SetAlias(command);

            case "checkCardStatus" :
                return new CheckCardStatus(command);

            case "printTransactions" :
                return new PrintTransactions(command);

            default :
                return null;
        }
    }
}
