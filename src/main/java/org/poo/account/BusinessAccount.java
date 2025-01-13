package org.poo.account;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.card.Card;
import org.poo.database.ExchangeRateDatabase;
import org.poo.database.UserDatabase;
import org.poo.user.User;
import org.poo.utils.DiscountTracker;
import lombok.Data;
import lombok.Getter;
import java.util.Map;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.TreeMap;

@Data
public final class BusinessAccount extends Account {

    private double spendingLimit;
    private double depositLimit;

    private Map<String, EmployeeInfo> personnel = new LinkedHashMap<String, EmployeeInfo>();

    public BusinessAccount(final String email, final String currency,
                           final String accountType, final int timestamp,
                           final String occupation) {

        super(email, currency, accountType, timestamp, occupation);

        if (currency.equals("RON")) {

            // assigned it like this to avoid magic number checkstyle error
            spendingLimit = DiscountTracker.FIVE_HUNDRED_THRESHOLD.getValue();
            depositLimit = DiscountTracker.FIVE_HUNDRED_THRESHOLD.getValue();
        } else {

            double rate = ExchangeRateDatabase.getInstance().getExchangeRate("RON", currency);

            // used DiscountTracker enum value to avoid magic number checkstyle error
            double limit = DiscountTracker.FIVE_HUNDRED_THRESHOLD.getValue() * rate;
            spendingLimit = limit;
            depositLimit = limit;
        }

        User user = UserDatabase.getInstance().getUserEntry(email);
        String ownerName = user.getUserData().getLastName() + " "
                            + user.getUserData().getFirstName();

        addAssociate(email, "owner", ownerName);
    }

    @Getter
    private final class EmployeeInfo {

        private String name;
        private String email;
        private String role;
        private double spent = 0;
        private double deposit = 0;
        private ArrayNode spendAndDeposits = new ObjectMapper().createArrayNode();


        private EmployeeInfo(final String email, final String name, final String role) {
            this.email = email;
            this.name = name;
            this.role = role;
        }
    }

    public boolean checkOwner(final String email) {
        EmployeeInfo employee = personnel.get(email);

        return employee.getRole().equals("owner");
    }

    public boolean addAssociate(final String email, final String role, final String name) {

        EmployeeInfo employeeInfo = new EmployeeInfo(email, name, role);

        if (personnel.containsKey(email)) {
            return false;
        }

        personnel.put(employeeInfo.getEmail(), employeeInfo);
        return true;
    }

    public boolean addFundsCheck(final double amount, final String email, final int timestamp) {

        EmployeeInfo empInfo = personnel.get(email);

        if (empInfo.role.equals("employee")) {

            if (amount > depositLimit) {
                return false;
            }
        }

        empInfo.deposit += amount;

        ObjectNode depositNode = new ObjectMapper().createObjectNode();

        depositNode.put("timestamp", timestamp);
        depositNode.put("action", "deposit");
        depositNode.put("amount", amount);

        empInfo.spendAndDeposits.add(depositNode);

        super.incrementFunds(amount);
        return true;
    }

    public boolean changeSpendingLimit(final String email, final double limit) {

        EmployeeInfo empInfo = personnel.get(email);

        if (!empInfo.role.equals("owner")) {
            return false;
        }

        spendingLimit = limit;
        return true;
    }

    public boolean changeDepositLimit(final String email, final double limit) {

        EmployeeInfo empInfo = personnel.get(email);

        if (!empInfo.role.equals("owner")) {
            return false;
        }

        depositLimit = limit;
        return true;
    }

    public boolean deleteCardCheck(final Card card, final String email) {

        EmployeeInfo empInfo = personnel.get(email);
        String cardOwner = card.getCardOwner();

        if (empInfo.getRole().equals("employee") && !cardOwner.equals(empInfo.getEmail())) {
            return false;
        }

        return true;
    }

    public ArrayList<Double> getStatistics(final ArrayNode managers, final ArrayNode employees,
                                           final int startTimestamp, final int endTimestamp) {

        ArrayList<Double> moneyStats = new ArrayList<>(2);
        Double totalSpent = 0.0;
        Double totalDeposit = 0.0;

        ObjectMapper map = new ObjectMapper();
        for (EmployeeInfo employeeInfo : personnel.values()) {

            if (employeeInfo.role.equals("owner")) {
                continue;
            }

            ObjectNode empNode = map.createObjectNode();
            double intervalDeposit = 0.0;
            double intervalSpent = 0.0;

            Iterator<JsonNode> empTransIterator = employeeInfo.spendAndDeposits.elements();

            while (empTransIterator.hasNext()) {

                ObjectNode trans = (ObjectNode) empTransIterator.next();

                if (trans.get("timestamp").asInt() < startTimestamp) {
                    continue;
                }

                if (trans.get("timestamp").asInt() > endTimestamp) {
                    break;
                }

                if (trans.get("action").asText().equals("deposit")) {

                    intervalDeposit += trans.get("amount").asDouble();
                    totalDeposit += trans.get("amount").asDouble();
                } else {

                    intervalSpent += trans.get("amount").asDouble();
                    totalSpent += trans.get("amount").asDouble();
                }

            }

            empNode.put("deposited", intervalDeposit);
            empNode.put("spent", intervalSpent);
            empNode.put("username", employeeInfo.name);

            switch (employeeInfo.role) {

                case "employee":
                    employees.add(empNode);
                    break;

                case "manager":
                    managers.add(empNode);
                    break;

                default:
                    break;

            }

        }

        moneyStats.add(totalSpent);
        moneyStats.add(totalDeposit);
        return moneyStats;

    }

    public boolean checkPayment(final double amount, final String email,
                                final String receiver, final int timestamp) {

        EmployeeInfo empInfo = personnel.get(email);

        if (empInfo.role.equals("employee")) {

            if (amount > spendingLimit) {
                return false;
            }
        }

        empInfo.spent += amount;

        ObjectNode spendNode = new ObjectMapper().createObjectNode();

        spendNode.put("timestamp", timestamp);
        spendNode.put("action", "spend");
        spendNode.put("amount", amount);
        spendNode.put("receiver", receiver);

        empInfo.spendAndDeposits.add(spendNode);
        return true;
    }

    @Getter
    private final class CommerciantInfo {

        private String name;
        private double amountPaid = 0;
        private ArrayList<EmployeeInfo> paid = new ArrayList<EmployeeInfo>();

        private CommerciantInfo(final String name) {
            this.name = name;
        }

        public void parseInformationToJson(final ArrayNode commerciants) {

            ObjectNode commNode = new ObjectMapper().createObjectNode();
            ArrayNode employees = new ObjectMapper().createArrayNode();
            ArrayNode managers = new ObjectMapper().createArrayNode();

            for (EmployeeInfo employeeInfo : paid) {

                if (employeeInfo.role.equals("manager")) {
                    managers.add(employeeInfo.name);
                } else {
                    employees.add(employeeInfo.name);
                }

            }

            commNode.put("commerciant", name);
            commNode.set("employees", employees);
            commNode.set("managers", managers);
            commNode.put("total received", amountPaid);

            commerciants.add(commNode);
        }

    }

    private void handleCommerciantInfo(final TreeMap<String, CommerciantInfo> commMap,
                                       final ObjectNode transaction, final EmployeeInfo empInfo) {

        if (commMap.containsKey(transaction.get("receiver").asText())) {

            CommerciantInfo commInfo = commMap.get(transaction.get("receiver").asText());
            commInfo.amountPaid = commInfo.amountPaid + transaction.get("amount").asDouble();
            commInfo.paid.add(empInfo);
            return;
        }

        CommerciantInfo commInfo = new CommerciantInfo(transaction.get("receiver").asText());
        commInfo.amountPaid = transaction.get("amount").asDouble();
        commInfo.paid.add(empInfo);
        commMap.put(transaction.get("receiver").asText(), commInfo);

    }

    public void generateCommerciantReport(final int startTimestamp, final int endTimestamp,
                                          final ArrayNode commerciants) {

        TreeMap<String, CommerciantInfo> commerciantMap = new TreeMap<>();

        for (EmployeeInfo employeeInfo : personnel.values()) {

            if (employeeInfo.role.equals("owner")) {
                continue;
            }

            Iterator<JsonNode> empTransIterator = employeeInfo.spendAndDeposits.elements();

            while (empTransIterator.hasNext()) {

                ObjectNode trans = (ObjectNode) empTransIterator.next();
                if (trans.get("timestamp").asInt() < startTimestamp) {
                    continue;
                }

                if (trans.get("timestamp").asInt() > endTimestamp) {
                    break;
                }

                if (trans.get("action").asText().equals("spend")) {
                    handleCommerciantInfo(commerciantMap, trans, employeeInfo);
                }
            }

        }

        for (CommerciantInfo commerciantInfo : commerciantMap.values()) {
            commerciantInfo.parseInformationToJson(commerciants);
        }

    }

}
