package org.poo.user;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.account.Account;
import org.poo.account.AccountPlans;
import org.poo.account.BusinessAccount;
import org.poo.card.Card;
import org.poo.fileio.UserInput;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Data;

@Data
public final class User {
    private UserInput userData = new UserInput();
    private Map<String, Account> userAccounts = new LinkedHashMap<>();
    private Map<String, Account> userAliasAccounts = new LinkedHashMap<>();
    private int bigPayments = 0;
    private AccountPlans userPlan;
    private ArrayNode userTransactions;

    public User(final UserInput userData) {

        this.userData.setFirstName(userData.getFirstName());
        this.userData.setLastName(userData.getLastName());
        this.userData.setEmail(userData.getEmail());
        this.userData.setBirthDate(userData.getBirthDate());
        this.userData.setOccupation(userData.getOccupation());

        if (userData.getOccupation().equals("student")) {
            userPlan = AccountPlans.STUDENT;
        } else {
            userPlan = AccountPlans.STANDARD;
        }

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
     * This function will append a new transaction, as a JSON node, in the transaction list
     * @param transaction The mapped transaction
     */
    public void addTransaction(final ObjectNode transaction) {
        userTransactions.add(transaction);
    }

    /**
     * This function will add a new transaction, as a JSON node, in the transaction list
     * at a certain timestamp
     * @param timestamp The timestamp when the transaction happened
     * @param transaction The mapped transaction
     */
    public void addTimestampTransaction(final int timestamp, final ObjectNode transaction) {

        Iterator<JsonNode> jsonIterator = userTransactions.elements();
        int position = 0;

        while (jsonIterator.hasNext()) {

            ObjectNode jsonNode = (ObjectNode) jsonIterator.next();

            if (jsonNode.get("timestamp").asInt() < timestamp) {
                position += 1;
            }

            if (jsonNode.get("timestamp").asInt() > timestamp) {
                userTransactions.insert(position, transaction);
                return;
            }

        }

        addTransaction(transaction);
    }

    /**
     * This function generates a transaction for an upgrade plan
     * @param account The account where it would be added
     * @param timestamp The timestamp of the upgrade
     * @param newPlan The new plan of the user
     */
    public void upgradePlanTrans(final String account, final int timestamp,
                                       final String newPlan) {

        ObjectNode upgradeNode = new ObjectMapper().createObjectNode();
        upgradeNode.put("accountIBAN", account);
        upgradeNode.put("description", "Upgrade plan");
        upgradeNode.put("newPlanType", newPlan);
        upgradeNode.put("timestamp", timestamp);

        addTransaction(upgradeNode);
        userAccounts.get(account).addTransaction(upgradeNode);


    }

    /**
     * This functions updates all the accounts to the new service plan
     * @param newPlanType The new plan
     */
    public void upgradeAllPlans(final String newPlanType) {

        userPlan = AccountPlans.valueOf(newPlanType.toUpperCase());

        for (Account account : userAccounts.values()) {

            // This if is to check if the user is the owner of business account
            if (account.getEmail().equals(userData.getEmail())) {
                account.setPlan(AccountPlans.valueOf(newPlanType.toUpperCase()));
            }

        }
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

            if (acc.getAccountType().equals("business")
                        && !((BusinessAccount) acc).checkOwner(userData.getEmail())) {
                continue;
            }

            accounts.add(acc.accountToJson(mapper));
        }

        userNode.set("accounts", accounts);
        return userNode;
    }

}
