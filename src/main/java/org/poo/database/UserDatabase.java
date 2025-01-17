package org.poo.database;

import org.poo.user.User;
import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Data;

/**
 * This class represents the place where user information is held.
 * Since all users are held in the same place, this database will be unique
 */
@Data
public final class UserDatabase {

    private static UserDatabase instance;
    private Map<String, User> database;
    private Map<String, String> mailDatabase;

    private UserDatabase() {
        database = new LinkedHashMap<>();
        mailDatabase = new LinkedHashMap<>();
    }

    /**
     * Function that gets the unique instance of the user database.
     * If there isn't an instance previously defined, it creates it.
     * @return The unique instance of the database
     */
    public static UserDatabase getInstance() {

        if (instance == null) {
            instance = new UserDatabase();
        }

        return instance;
    }

    /**
     * This function adds a new pair email-user
     * @param userEmail The user's email
     * @param user The user
     */
    public void addUserEntry(final String userEmail, final User user) {
        if (!database.containsKey(userEmail)) {
            database.put(userEmail, user);
        }
    }

    /**
     * This method removes a pair email-user
     * @param userEmail The email of the user
     */
    public void removeUserEntry(final String userEmail) {
        database.remove(userEmail);
    }

    /**
     * This function gets an user based on it's email
     * @param userEmail The email of the user
     * @return The user
     */
    public User getUserEntry(final String userEmail) {
        return database.get(userEmail);
    }

    /**
     * This method adds a new pair iban-email
     * @param iban of the account
     * @param userEmail of the user that owns the account
     */
    public void addMailEntry(final String iban, final String userEmail) {
        mailDatabase.put(iban, userEmail);
    }

    /**
     * This method removes the pair iban-email associated with the iban got as the parameter
     * @param iban
     */
    public void removeMailEntry(final String iban) {
        mailDatabase.remove(iban);
    }

    /**
     * This method retrieves the email of the owner of an account based on the iban of the account
     * @param iban The iban of the account
     * @return The email of the account with the provided iban, Null if undefined
     */
    public String getMailEntry(final String iban) {
        return mailDatabase.get(iban);
    }

    /**
     * This method will be called when the database needs to be cleared
     */
    public void clearDatabase() {
        database.clear();
    }
}
