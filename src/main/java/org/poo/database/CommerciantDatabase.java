package org.poo.database;

import org.poo.fileio.CommerciantInput;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Data;

/**
 * This class represents the place where commerciant information is held.
 * Since all commerciants are held in the same place, this database will be unique
 */
@Data
public final class CommerciantDatabase {

    private static CommerciantDatabase instance;
    private Map<String, Integer> commNameToId;
    private Map<String, Integer> commIbanToId;
    private Map<Integer, CommerciantInput> commerciantDb;

    private CommerciantDatabase() {
        commerciantDb = new LinkedHashMap<>();
        commNameToId = new LinkedHashMap<>();
        commIbanToId = new LinkedHashMap<>();
    }

    /**
     * Since this class is a singleton, it needs a Getter for it's unique instance
     * @return The instance
     */
    public static CommerciantDatabase getInstance() {
        if (instance == null) {
            instance = new CommerciantDatabase();
        }

        return instance;
    }

    /**
     * This function is responsible for adding a new commerciant to the database
     * @param commerciant The commerciant information
     */
    public void addCommerciant(final CommerciantInput commerciant) {
        commerciantDb.put(commerciant.getId(), commerciant);
        commNameToId.put(commerciant.getCommerciant(), commerciant.getId());
        commIbanToId.put(commerciant.getAccount(), commerciant.getId());
    }

    /**
     * This function is a wrapper for the database get method
     * @param id The requested id
     * @return Commerciant information for that id, null if it doesn't exist
     */
    public CommerciantInput getCommerciant(final Integer id) {
        return commerciantDb.get(id);
    }

    /**
     * This function clears the database of it's contents
     */
    public void removeAllCommerciants() {
        commerciantDb.clear();
    }
}
