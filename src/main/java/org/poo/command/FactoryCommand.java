package org.poo.command;

import org.poo.fileio.CommandInput;

public final class FactoryCommand {

    private FactoryCommand() {
        throw new UnsupportedOperationException("This is a utility class");
    }

    public static Command extractCommand(final CommandInput command) {

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

            case "addFunds" :
                return new AddFunds(command);

            case "deleteCard" :
                return new DeleteCard(command);

            default :
                return null;
        }
    }
}
