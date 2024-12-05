package org.poo.fileio;

import java.util.ArrayList;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public final class ObjectInput {
    private ArrayList<UserInput> users;
    private ArrayList<ExchangeInput> exchangeRates;
    private ArrayList<CommandInput> commands;
    private ArrayList<CommerciantInput> commerciants;
}
