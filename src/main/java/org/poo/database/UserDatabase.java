package org.poo.database;

import org.poo.user.User;
import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Data;

@Data
public final class UserDatabase {

    private static UserDatabase instance;
    private Map<String, User> database;
    private Map<String, String> mailDatabase;

    private UserDatabase() {
        database = new LinkedHashMap<>();
        mailDatabase = new LinkedHashMap<>();
    }

    public static UserDatabase getInstance() {
        if (instance == null) {
            instance = new UserDatabase();
        }
        return instance;
    }

    public void addUserEntry(final String userEmail, final User user) {
        if (!database.containsKey(userEmail)) {
            database.put(userEmail, user);
        }
    }

    public void removeUserEntry(final String userEmail) {
        database.remove(userEmail);
    }

    public User getUserEntry(final String userEmail) {
        return database.get(userEmail);
    }

    public void addMailEntry(final String iban, final String userEmail) {
        mailDatabase.put(iban, userEmail);
    }

    public void removeMailEntry(final String iban) {
        mailDatabase.remove(iban);
    }

    public String getMailEntry(final String iban) {
        return mailDatabase.get(iban);
    }
}
