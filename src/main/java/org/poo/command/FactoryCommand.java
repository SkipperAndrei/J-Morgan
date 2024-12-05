package org.poo.command;

import org.poo.fileio.CommandInput;

public final class FactoryCommand {

    private FactoryCommand() {
        throw new UnsupportedOperationException("This is a utility class");
    }

    public static Command extractCommand(final CommandInput command) {

        switch(command.getCommand()) {

            case "printUsers": {
                return new PrintUsers();
            }

            case "addAccount": {
                return new AddAccount(command);
            }

            case "createCard" : {
                return new CreateCard(command);
            }

            case "addFunds" : {
                return new AddFunds(command);
            }

            default: {
                return null;
            }
        }
    }
}
