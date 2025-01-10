package org.poo.utils;

import org.poo.account.Account;
import org.poo.command.CommandConstants;
import org.poo.command.SplitPayment;

import java.util.ArrayList;
import java.util.ListIterator;

import lombok.Getter;
import org.poo.database.UserDatabase;
import org.poo.user.User;

@Getter
public final class SplitTracker {

    private static SplitTracker instance;
    private ArrayList<SplitPayment> listener;

    private SplitTracker() {
        listener = new ArrayList<>();
    }

    public static SplitTracker getInstance() {

        if (instance == null) {
            instance = new SplitTracker();
        }
        return instance;

    }

    public SplitPayment accept(final String email) {

        User user = UserDatabase.getInstance().getUserEntry(email);
        boolean found = false;
        ListIterator<SplitPayment> listenerIterator = listener.listIterator();
        SplitPayment payment = null;

        while (listenerIterator.hasNext()) {

            payment = listenerIterator.next();

            for (Account acc : user.getUserAccounts().values()) {

                if (payment.getAcceptedAccounts().containsKey(acc.getIban())
                        && payment.getAcceptedAccounts().get(acc.getIban()).equals(false)) {

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

    public SplitPayment reject(final String email) {

        User user = UserDatabase.getInstance().getUserEntry(email);
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

        if (!found) {
            return null;
        }

        assert payment != null;
        payment.setActionCode(CommandConstants.REJECTED_SPLIT);
        listener.remove(payment);
        return payment;
    }
}


