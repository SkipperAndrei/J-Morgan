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
public final class User {
    private UserInput userData = new UserInput();
    private Map<String, Account> userAccounts = new LinkedHashMap<>();
    private Map<String, Account> userAliasAccounts = new LinkedHashMap<>();
    private ArrayNode userTransactions;

    public User(final UserInput userData) {

        this.userData.setFirstName(userData.getFirstName());
        this.userData.setLastName(userData.getLastName());
        this.userData.setEmail(userData.getEmail());
        this.userData.setBirthDate(userData.getBirthDate());
        this.userData.setOccupation(userData.getOccupation());
        userTransactions = new ObjectMapper().createArrayNode();
    }

    /**
     * This function adds a new account by iban if
     * the account email corresponds with the user email
     * @param account The new account
     */
    public void addAccount(final Account account) {

        if (!account.getEmail().equals(userData.getEmail())) {
            return;
        }

        userAccounts.put(account.getIban(), account);
    }

    /**
     * This function adds a new account by alias if
     * the account email corresponds with the user email
     * @param account The account that will be associated with the alias
     * @param alias The alias of the account, used when receiving money
     */
    public void addAccountAlias(final Account account, final String alias) {

        if (!account.getEmail().equals(userData.getEmail())) {
            return;
        }

        userAliasAccounts.put(alias, account);
    }

    /**
     * This function will add a new card on the user's account
     * @param iban The iban of the account
     * @param card The new card
     */
    public void addCard(final String iban, final Card card) {
        userAccounts.get(iban).getCards().put(card.getCardNumber(), card);
    }

    /**
     * This function will add a new transaction, as a JSON node, in the transaction list
     * @param transaction The mapped transaction
     */
    public void addTransaction(final ObjectNode transaction) {
        userTransactions.add(transaction);
    }

    /**
     * This function maps the contents of a user object to a JSON node
     * @param mapper Mapper used to create the new node
     * @return The mapped JSON node
     */
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
