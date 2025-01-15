package org.poo.command.payments;

import org.poo.account.Account;
import org.poo.command.CommandConstants;

import java.util.ArrayList;
import java.util.ListIterator;

import lombok.Getter;
import org.poo.database.UserDatabase;
import org.poo.user.User;

/**
 * In this class, all the pending split payment commands will be placed in an ArrayList
 * This class is designed as a singleton
 * A split payment command will be removed from the ArrayList
 * When all accounts accepted the payment, and then the command will be executed
 * When an account rejected the payment, and output will be generated
 */
@Getter
public final class SplitTracker {

    private static SplitTracker instance;
    private ArrayList<SplitPayment> listener;

    private SplitTracker() {
        listener = new ArrayList<>();
    }

    /**
     * This is the instance getter of the SplitTracker class
     * @return The instance of the class
     */
    public static SplitTracker getInstance() {

        if (instance == null) {
            instance = new SplitTracker();
        }
        return instance;

    }

    /**
     * This method implements
     * @param email The email of the user that accepts the payment
     * @param type The type of the split payment
     * @return The split payment instance, in case a payment was fully accepted
     * The function returns null if no payment was fully accepted as a result of this command
     * Returns a split payment with an error signal, if the user doesn't exist
     */
    public SplitPayment accept(final String email, final String type) {

        User user = UserDatabase.getInstance().getUserEntry(email);

        if (user == null) {
            return new SplitPayment(CommandConstants.NOT_FOUND);
        }

        boolean found = false;
        ListIterator<SplitPayment> listenerIterator = listener.listIterator();
        SplitPayment payment = null;

        while (listenerIterator.hasNext()) {

            payment = listenerIterator.next();

            for (Account acc : user.getUserAccounts().values()) {

                // checking if the account is in the payment, and it hasn't already accepted
                // and if the type of payment is correct

                if (payment.getAcceptedAccounts().containsKey(acc.getIban())
                        && payment.getAcceptedAccounts().get(acc.getIban()).equals(false)
                        && payment.getType().equals(type)) {

                    payment.getAcceptedAccounts().put(acc.getIban(), true);
                    found = true;
                    break;
                }

            }

            if (found) {
                break;
            }

        }

        try {

            for (String acc : payment.getAcceptedAccounts().keySet()) {

                if (payment.getAcceptedAccounts().get(acc).equals(false)) {
                    return null;
                }
            }

            payment.finallyExecuteCommand(UserDatabase.getInstance());
            listener.remove(payment);
            return payment;

        } catch (NullPointerException e) {
            return null;
        }
    }

    /**
     * This method implements the reject mechanism of the split payment
     * @param email The email of the user that rejected the payment
     * @return The rejected split payment, or null if the user wasn't found
     */
    public SplitPayment reject(final String email) {

        User user = UserDatabase.getInstance().getUserEntry(email);

        if (user == null) {
            return null;
        }

        boolean found = false;
        ListIterator<SplitPayment> listenerIterator = listener.listIterator();
        SplitPayment payment = null;

        while (listenerIterator.hasNext()) {

            payment = listenerIterator.next();

            for (Account acc : user.getUserAccounts().values()) {

                if (payment.getAcceptedAccounts().containsKey(acc.getIban())) {
                    found = true;
                    break;
                }

            }

            if (found) {
                break;
            }

        }


        assert payment != null;
        payment.setActionCode(CommandConstants.REJECTED_SPLIT);
        listener.remove(payment);
        return payment;
    }
}


