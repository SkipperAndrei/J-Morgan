package org.poo.output;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.account.Account;
import org.poo.account.SavingAccount;
import org.poo.command.SplitPayment;
import org.poo.database.UserDatabase;
import org.poo.user.User;
import lombok.Data;

import java.util.Iterator;
import java.util.List;

@Data
public final class OutputGenerator {

    private ObjectMapper mapper;
    private ArrayNode output;
    private UserDatabase userDatabase;

    public OutputGenerator(ObjectMapper mapper, ArrayNode output, UserDatabase userDatabase) {
        this.mapper = mapper;
        this.output = output;
        this.userDatabase = userDatabase;
    }

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

    public void deleteAccount(final int timestamp, final boolean error) {
        ObjectNode deleteNode = mapper.createObjectNode();
        deleteNode.put("command", "deleteAccount");

        ObjectNode infoNode = mapper.createObjectNode();

        if (error) {
            infoNode.put("error", "Account couldn't be deleted - see org.poo.transactions for details");
        } else {
            infoNode.put("success", "Account deleted");
        }

        infoNode.put("timestamp", timestamp);
        deleteNode.set("output", infoNode);
        deleteNode.put("timestamp", timestamp);

        output.add(deleteNode);
    }

    public void successPayment(final int timestamp, final double amount, final String commerciant) {
        ObjectNode paymentNode = mapper.createObjectNode();
        paymentNode.put("timestamp", timestamp);
        paymentNode.put("description", "Card payment");
        paymentNode.put("amount", amount);
        paymentNode.put("commerciant", commerciant);
        output.add(paymentNode);
    }

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

    public void printTransaction(final int timestamp, User user) {
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

    public void tryToAddTransaction(Account acc, ObjectNode transaction) {

        try {
            ((SavingAccount) acc).getInterestRate();
            return;
        } catch (ClassCastException e) {
            userDatabase.getUserEntry(acc.getEmail()).getUserAccounts().
                        get(acc.getIBAN()).addTransaction(transaction);
        }

    }

    public ObjectNode defaultSplitOutput(List<String> args, final int timestamp,
                                          final String currency, final double amount) {

        ObjectNode successNode = mapper.createObjectNode();
        successNode.put("timestamp", timestamp);
        successNode.put("description", "Split payment of " +
                                                String.format("%.2f", amount) + " " + currency);
        successNode.put("currency", currency);
        successNode.put("amount", amount / args.size());

        ArrayNode involvedAccounts = mapper.createArrayNode();
        for (String arg : args) {
            involvedAccounts.add(arg);
        }

        successNode.set("involvedAccounts", involvedAccounts);
        return successNode;
    }

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
}
