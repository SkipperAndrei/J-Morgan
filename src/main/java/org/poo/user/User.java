package org.poo.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.account.Account;
import org.poo.card.Card;
import org.poo.fileio.UserInput;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Data;

@Data
public class User {
    private UserInput userData = new UserInput();
    private Map<String, Account> userAccounts = new LinkedHashMap<>();
    private Map<String, Account> userAliasAccounts = new LinkedHashMap<>();
    private ArrayNode userTransactions;

    public User(UserInput userData) {
        this.userData.setFirstName(userData.getFirstName());
        this.userData.setLastName(userData.getLastName());
        this.userData.setEmail(userData.getEmail());
        userTransactions = new ObjectMapper().createArrayNode();
    }

    public void addAccount(Account account) {

        if (!account.getEmail().equals(userData.getEmail())) {
            return;
        }

        userAccounts.put(account.getIBAN(), account);
    }

    public void addAccountAlias(Account account, String alias) {
        userAliasAccounts.put(alias, account);
    }

    public void addCard(String IBAN, Card card) {
        userAccounts.get(IBAN).getCards().put(card.getCardNumber(), card);
    }

    public void addTransaction(ObjectNode transaction) {
        userTransactions.add(transaction);
    }

    public ObjectNode userToJson(ObjectMapper mapper) {

        ObjectNode userNode = mapper.createObjectNode();

        userNode.put("firstName", userData.getFirstName());
        userNode.put("lastName", userData.getLastName());
        userNode.put("email", userData.getEmail());

        ArrayNode userAccounts = mapper.createArrayNode();

        for (Account acc : this.userAccounts.values()) {
            userAccounts.add(acc.accountToJson(mapper));
        }

        userNode.set("accounts", userAccounts);
        return userNode;
    }

}
