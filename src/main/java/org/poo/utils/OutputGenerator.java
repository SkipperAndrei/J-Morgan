package org.poo.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.account.Account;
import org.poo.account.SavingAccount;
import org.poo.database.UserDatabase;
import org.poo.user.User;
import lombok.Data;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * This class will handle JSON output generation
 */
@Data
public final class OutputGenerator {

    private ObjectMapper mapper;
    private ArrayNode output;
    private UserDatabase userDatabase;

    public OutputGenerator(final ObjectMapper mapper, final ArrayNode output,
                           final UserDatabase userDatabase) {

        this.mapper = mapper;
        this.output = output;
        this.userDatabase = userDatabase;
    }

    /**
     * This function will add all the users mapped as JSON nodes to output file.
     * @param timestamp The moment the command was queried
     */
    public void addUsers(final int timestamp) {

        ObjectNode usersNode = mapper.createObjectNode();
        usersNode.put("command", "printUsers");
        ArrayNode users = mapper.createArrayNode();

        for (User user : userDatabase.getDatabase().values()) {
            users.add(user.userToJson(mapper));
        }

        usersNode.set("output", users);
        usersNode.put("timestamp", timestamp);
        output.add(usersNode);
    }

    /**
     * This function generates the output for the "delete account" query.
     * @param timestamp The timestamp of the query
     * @param error If an error happened
     * @param user The user whose account needs to be deleted
     */
    public void deleteAccount(final int timestamp, final boolean error, final User user) {

        ObjectNode deleteNode = mapper.createObjectNode();
        deleteNode.put("command", "deleteAccount");

        ObjectNode infoNode = mapper.createObjectNode();

        if (error) {
            infoNode.put("error", "Account couldn't be deleted - "
                        + "see org.poo.transactions for details");
        } else {
            infoNode.put("success", "Account deleted");
        }

        infoNode.put("timestamp", timestamp);
        deleteNode.set("output", infoNode);
        deleteNode.put("timestamp", timestamp);

        ObjectNode transactionInfoNode = infoNode.deepCopy();

        if (transactionInfoNode.has("error")) {
            transactionInfoNode.remove("error");
            transactionInfoNode.put("description", "Account couldn't be deleted - "
                                    + "there are funds remaining");
        }

        user.addTransaction(transactionInfoNode);

        output.add(deleteNode);
    }

    /**
     * This function generates output for queries that encountered errors.
     * @param timestamp The timestamp of the query
     * @param description Description of the error
     * @param command The query type
     */
    public void errorSetting(final int timestamp, final String description, final String command) {

        ObjectNode errorNode = mapper.createObjectNode();
        errorNode.put("command", command);

        ObjectNode infoNode = mapper.createObjectNode();
        infoNode.put("timestamp", timestamp);
        infoNode.put("description", description);

        errorNode.set("output", infoNode);
        errorNode.put("timestamp", timestamp);

        output.add(errorNode);
    }

    /**
     * This function generates output for the "print transactions" command.
     * @param timestamp The timestamp of the query
     * @param user The user whose transactions will be printed
     */
    public void printTransaction(final int timestamp, final User user) {
        ObjectNode transactionNode = mapper.createObjectNode();

        transactionNode.put("command", "printTransactions");
        ArrayNode transactions = mapper.createArrayNode();

        for (JsonNode transaction : user.getUserTransactions()) {
            transactions.add(transaction);
        }

        transactionNode.set("output", transactions);
        transactionNode.put("timestamp", timestamp);
        output.add(transactionNode);
    }

    /**
     * This function will try to add the transaction to an account.
     * If the account is a savings account it won't add it.
     * @param acc The account
     * @param transaction The transaction
     * @return True, if it was added, False otherwise
     */
    public boolean tryToAddTransaction(final Account acc, final ObjectNode transaction) {

        try {
            ((SavingAccount) acc).getInterestRate();
            return false;
        } catch (ClassCastException e) {
            userDatabase.getUserEntry(acc.getEmail()).getUserAccounts().
                        get(acc.getIban()).addTransaction(transaction);
            return true;
        }

    }

    /**
     * This function generates the output for the "Split payment" query.
     * @param args The list of Ibans
     * @param timestamp The timestamp of the query
     * @param currency The currency used in the transaction
     * @param amount The amount to be payed
     * @return The mapped JSON node
     */
    public ObjectNode defaultSplitOutput(final List<String> args, final int timestamp,
                                         final String currency, final double amount) {

        ObjectNode successNode = mapper.createObjectNode();

        successNode.put("timestamp", timestamp);
        successNode.put("description", "Split payment of "
                        + String.format("%.2f", amount) + " " + currency);

        successNode.put("currency", currency);
        successNode.put("amount", amount / args.size());

        ArrayNode involvedAccounts = mapper.createArrayNode();
        for (String arg : args) {
            involvedAccounts.add(arg);
        }

        successNode.set("involvedAccounts", involvedAccounts);
        return successNode;
    }

    /**
     * This function generates the output for the "report" query.
     * The function iterates through all transaction associated to the account.
     * It maps in the output only the ones that are in the queried interval.
     * @param startTimestamp The start of the timestamp interval
     * @param endTimestamp The end of the timestamp interval
     * @param email The email of the user
     * @param account The Iban of the account
     * @param timestamp The timestamp of the "report" query
     */
    public void generateReport(final int startTimestamp, final int endTimestamp,
                               final String email, final String account, final int timestamp) {

        ObjectNode reportNode = mapper.createObjectNode();

        reportNode.put("command", "report");
        ObjectNode outputNode = mapper.createObjectNode();

        Account acc = userDatabase.getUserEntry(email).getUserAccounts().get(account);
        outputNode.put("IBAN", account);
        outputNode.put("balance", acc.getBalance());
        outputNode.put("currency", acc.getCurrency());

        ArrayNode transactions = mapper.createArrayNode();

        Iterator<JsonNode> jsonIterator = acc.getAccountTransactions().elements();

        while (jsonIterator.hasNext()) {

            ObjectNode transactionNode = (ObjectNode) jsonIterator.next();

            if (transactionNode.get("timestamp").asInt() > endTimestamp) {
                break;
            }

            if (transactionNode.get("timestamp").asInt() < startTimestamp) {
                continue;
            }

            transactions.add(transactionNode);
        }

        outputNode.set("transactions", transactions);

        reportNode.set("output", outputNode);
        reportNode.put("timestamp", timestamp);
        output.add(reportNode);
    }

    /**
     * This function generates the output for the "spendings report" query.
     * If the query was issued on a savings iban it will generate an error message.
     * Same as "report" query, the function iterates through the transactions.
     * It keeps the ones that are the result of "pay online".
     * It also keeps track of the commerciants and the amount payed to them.
     * @param startTimestamp The beginning of the interval
     * @param endTimestamp The end of the interval
     * @param email The email of the user
     * @param iban The iban of the account
     * @param timestamp The timestamp of the query
     */
    public void generateSpendingReport(final int startTimestamp, final int endTimestamp,
                                       final String email, final String iban, final int timestamp) {

        ObjectNode reportNode = mapper.createObjectNode();
        reportNode.put("command", "spendingsReport");

        Account acc = userDatabase.getUserEntry(email).getUserAccounts().get(iban);

        ObjectNode outputNode = mapper.createObjectNode();
        outputNode.put("IBAN", iban);
        outputNode.put("balance", acc.getBalance());
        outputNode.put("currency", acc.getCurrency());

        ArrayNode payments = mapper.createArrayNode();

        Iterator<JsonNode> jsonIterator = acc.getAccountTransactions().elements();
        Map<String, Double> commerciants = new TreeMap<>();

        while (jsonIterator.hasNext()) {

            ObjectNode transactionNode = (ObjectNode) jsonIterator.next();

            if (transactionNode.get("timestamp").asInt() > endTimestamp) {
                break;
            }

            if (transactionNode.get("timestamp").asInt() < startTimestamp) {
                continue;
            }

            if (transactionNode.has("commerciant")) {

                payments.add(transactionNode);
                Double amount = transactionNode.get("amount").asDouble();
                Double prevMoney = commerciants.get(transactionNode.get("commerciant").asText());

                if (prevMoney == null) {
                    commerciants.put(transactionNode.get("commerciant").asText(), amount);
                } else {
                    commerciants.put(transactionNode.get("commerciant").asText(),
                                    prevMoney + amount);
                }

            }
        }

        outputNode.set("transactions", payments);
        ArrayNode commerciantArrayNode = mapper.createArrayNode();

        for (Map.Entry<String, Double> entry : commerciants.entrySet()) {
            ObjectNode commerciantNode = mapper.createObjectNode();
            commerciantNode.put("commerciant", entry.getKey());
            commerciantNode.put("total", entry.getValue());
            commerciantArrayNode.add(commerciantNode);
        }

        outputNode.set("commerciants", commerciantArrayNode);
        reportNode.set("output", outputNode);
        reportNode.put("timestamp", timestamp);
        output.add(reportNode);

    }

}

