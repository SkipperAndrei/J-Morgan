package org.poo.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.account.Account;
import org.poo.card.Card;
import org.poo.fileio.UserInput;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Data;

@Data
public class User {
    private UserInput userData = new UserInput();
    private Map<String, Account> userAccounts = new LinkedHashMap<>();
    private Map<String, Account> userAliasAccounts = new LinkedHashMap<>();
    private ArrayNode userTransactions;

    public User(final UserInput userData) {

        this.userData.setFirstName(userData.getFirstName());
        this.userData.setLastName(userData.getLastName());
        this.userData.setEmail(userData.getEmail());
        userTransactions = new ObjectMapper().createArrayNode();
    }

    public void addAccount(final Account account) {

        if (!account.getEmail().equals(userData.getEmail())) {
            return;
        }

        userAccounts.put(account.getIban(), account);
    }

    public void addAccountAlias(final Account account, final String alias) {
        userAliasAccounts.put(alias, account);
    }

    public void addCard(final String iban, final Card card) {
        userAccounts.get(iban).getCards().put(card.getCardNumber(), card);
    }

    public void addTransaction(final ObjectNode transaction) {
        userTransactions.add(transaction);
    }

    public ObjectNode userToJson(final ObjectMapper mapper) {

        ObjectNode userNode = mapper.createObjectNode();

        userNode.put("firstName", userData.getFirstName());
        userNode.put("lastName", userData.getLastName());
        userNode.put("email", userData.getEmail());

        ArrayNode accounts = mapper.createArrayNode();

        for (Account acc : this.userAccounts.values()) {
            accounts.add(acc.accountToJson(mapper));
        }

        userNode.set("accounts", accounts);
        return userNode;
    }

}
