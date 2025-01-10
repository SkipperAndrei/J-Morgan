package org.poo.command.account;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.account.Account;
import org.poo.account.AccountPlans;
import org.poo.command.Command;
import org.poo.command.CommandConstants;
import org.poo.database.ExchangeRateDatabase;
import org.poo.database.UserDatabase;
import org.poo.fileio.CommandInput;
import org.poo.plans.PlanConstants;
import org.poo.user.User;
import org.poo.utils.OutputGenerator;

public class UpgradePlan implements Command {

    private String account;
    private String newPlanType;
    private int timestamp;
    private CommandConstants actionCode = CommandConstants.SUCCESS;
    private String userEmail;

    public UpgradePlan(final CommandInput command) {

        account = command.getAccount();
        newPlanType = command.getNewPlanType();
        timestamp = command.getTimestamp();
    }

    /**
     * This function checks if the account has sufficient funds to pay for the upgrade
     * @param upgradeAcc The account from where the owner will pay
     * @param amount The amount to be paid in RON's
     */
    public void checkBalance(final Account upgradeAcc, final int amount) {

        if (upgradeAcc.getCurrency().equals("RON")) {

            if (upgradeAcc.canPay(amount)) {

                upgradeAcc.decrementFunds(amount);
                actionCode = CommandConstants.SUCCESS;
                return;
            }

            actionCode = CommandConstants.INSUFFICIENT_FUNDS;
            return;
        }

        double exchangeRate = ExchangeRateDatabase.getInstance().
                            getExchangeRate("RON", upgradeAcc.getCurrency());

        double newAmount = amount * exchangeRate;
        if (upgradeAcc.canPay(newAmount)) {

            upgradeAcc.decrementFunds(newAmount);
            actionCode = CommandConstants.SUCCESS;
            return;
        }

        actionCode = CommandConstants.INSUFFICIENT_FUNDS;
    }

    /**
     * This function determines what is the amount that the user has to pay for the new plan
     * @param user The user that requested the upgrade
     * @param upgradeAcc The account from where he will pay for the upgrade
     */
    public void getNewPlan(final User user, final Account upgradeAcc) {

        short diff = (short) (AccountPlans.valueOf(newPlanType.toUpperCase()).
                            getPriority() - upgradeAcc.getPlan().getPriority());

        if (diff == 2) {
            checkBalance(upgradeAcc, PlanConstants.STD_TO_GOLD.getValue());
            return;
        }

        if (newPlanType.equals("silver")) {
            checkBalance(upgradeAcc, PlanConstants.STD_TO_SILVER.getValue());
            return;
        }

        checkBalance(upgradeAcc, PlanConstants.SILVER_TO_GOLD.getValue());
    }

    /**
     * This method checks if the user wants to upgrade to the next plan
     * @param user The user that requested the upgrade
     * @param upgradeAcc The account from where he will pay for the upgrade
     */
    public void checkAccountPlan(final User user, final Account upgradeAcc) {


        if (upgradeAcc.getPlan().getPriority() >= AccountPlans.valueOf(newPlanType.toUpperCase())
                                                .getPriority()) {

            actionCode = CommandConstants.INFERIOR_PLAN;
            return;
        }

        getNewPlan(user, upgradeAcc);
    }

    @Override
    public void executeCommand(final UserDatabase userDatabase) {

        try {

            userEmail = userDatabase.getMailEntry(account);
            User user = userDatabase.getUserEntry(userEmail);
            Account upgradedAcc = user.getUserAccounts().get(account);
            checkAccountPlan(user, upgradedAcc);

            if (actionCode == CommandConstants.SUCCESS) {
                user.upgradeAllPlans(newPlanType);
            }

        } catch (NullPointerException e) {
            actionCode = CommandConstants.NOT_FOUND;
            return;
        }

    }

    @Override
    public void generateOutput(final OutputGenerator outputGenerator) {

        ObjectNode transactionNode = outputGenerator.getMapper().createObjectNode();
        User user = outputGenerator.getUserDatabase().getUserEntry(userEmail);

        switch (actionCode) {

            case SUCCESS:
                transactionNode.put("timestamp", timestamp);
                transactionNode.put("description", "Upgrade plan");
                transactionNode.put("accountIBAN", account);
                transactionNode.put("newPlanType", newPlanType);
                user.addTransaction(transactionNode);
                return;

            case INSUFFICIENT_FUNDS:
                transactionNode.put("description", "Insufficient funds");
                transactionNode.put("timestamp", timestamp);
                user.addTransaction(transactionNode);
                return;

            case NOT_FOUND:
                outputGenerator.errorSetting(timestamp, "Account not found", "upgradePlan");
                return;

            default:
                return;
        }

    }
}
