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

    private Map<String, EmployeeInfo> personnel = new LinkedHashMap<>();

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

    /**
     * This function checks if the employee is the owner
     * @param email Employee's email
     * @return True if the email is the owner's, False otherwise
     */
    public boolean checkOwner(final String email) {
        EmployeeInfo employee = personnel.get(email);

        return employee.getRole().equals("owner");
    }

    /**
     * This function handles associate adding in the personnel hashmap
     * @param email The email of the new associate
     * @param role The role of the new employee
     * @param name The name of the new employee
     * @return True, if the employee could be added, False if the employee couldn't be added
     */
    public boolean addAssociate(final String email, final String role, final String name) {

        EmployeeInfo employeeInfo = new EmployeeInfo(email, name, role);

        if (personnel.containsKey(email)) {
            return false;
        }

        personnel.put(employeeInfo.getEmail(), employeeInfo);
        return true;
    }

    /**
     * This function checks if the employee can deposit the amount
     * If the employee is not at least a manager,
     * then the amount must be checked to be lower than the deposit limit
     * If the amount can be deposited, then a transaction is created
     * @param amount The amount wanted to be deposited
     * @param email The email of the employee
     * @param timestamp The timestamp of the query
     * @return True, if the amount can be deposited, False otherwise
     */
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

    /**
     * This function checks the permission for an employee to change the spending limit
     * If the employee is at least a manager, then the spending limit is changed
     * @param email The email of the employee
     * @param limit The new spending limit
     * @return True, if successful, False otherwise
     */
    public boolean changeSpendingLimit(final String email, final double limit) {


        if (!checkOwner(email)) {
            return false;
        }

        spendingLimit = limit;
        return true;
    }

    /**
     * This function checks the permission for an employee to change the deposit limit
     * If the employee is at least a manager, then the deposit limit is changed
     * @param email The email of the employee
     * @param limit The new deposit limit
     * @return True, if successful, False otherwise
     */
    public boolean changeDepositLimit(final String email, final double limit) {

        EmployeeInfo empInfo = personnel.get(email);

        if (!empInfo.role.equals("owner")) {
            return false;
        }

        depositLimit = limit;
        return true;
    }

    /**
     * This method checks if the employee can delete the card
     * @param card The card to be deleted
     * @param email The email of the employee
     * @return True, if possible, False otherwise
     */
    public boolean deleteCardCheck(final Card card, final String email) {

        EmployeeInfo empInfo = personnel.get(email);
        String cardOwner = card.getCardOwner();

        return !empInfo.getRole().equals("employee") || cardOwner.equals(empInfo.getEmail());
    }

    /**
     * This function generates the money spent and deposited for every employee, except the owner
     * The interval of time is represented by [startTimestamp, endTimestamp]
     * For this, the function iterates through the employees,
     * and for each employee through it's transactions
     * @param managers ArrayNode of managers
     * @param employees ArrayNode of employees
     * @param startTimestamp The start of the time interval
     * @param endTimestamp The end of the time interval
     * @return An array list where on the first position is the amount spent,
     * and on the second position the amount deposited
     */
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

    /**
     * This function checks if the employee can spend the amount
     * If the employee is not at least a manager,
     * then the amount must be checked to be lower than the spending limit
     * If the amount can be spent, then a transaction is created
     * @param amount The amount to pay
     * @param email The email of the employee
     * @param receiver The receiver of the amount (name/iban of commerciant)
     * @param timestamp The timestamp of the payment
     * @return True, if the employee can spend it, False otherwise
     */
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

        /**
         * This function parses the information for every commerciant
         * that received money from an employee into a JSON node
         * @param commerciants The commerciants array node
         */
        private void parseInformationToJson(final ArrayNode commerciants) {

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

    /**
     * This function adds transaction information to the commerciants map, where information for
     * the business report between two timestamps is held
     * @param commMap The commerciants map
     * @param transaction The current transaction to be analyzed
     * @param empInfo The information of the employee
     */
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

    /**
     * This function iterates through the employee transactions and adds spending transactions
     * to the commerciants array node
     * @param startTimestamp The left end of the timestamp interval
     * @param endTimestamp The right end of the timestamp interval
     * @param commerciants The commerciants array node, where output is held
     */
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

    /**
     * This function maps the contents of the account in JSON format
     * @param mapper The object mapper used to create the node
     * @return The mapped JSON node
     */
    public ObjectNode businessToJson(final ObjectMapper mapper) {

        ObjectNode outputNode = mapper.createObjectNode();

        outputNode.put("IBAN", super.getIban());
        outputNode.put("balance", super.getBalance());
        outputNode.put("currency", super.getCurrency());


        outputNode.put("spending limit", spendingLimit);
        outputNode.put("deposit limit", depositLimit);

        return outputNode;
    }

}
