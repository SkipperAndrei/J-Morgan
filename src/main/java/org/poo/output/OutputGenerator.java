package org.poo.output;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.account.Account;
import org.poo.database.UserDatabase;
import org.poo.user.User;
import lombok.Data;

public final class OutputGenerator {

    private static OutputGenerator instance;
    private ObjectMapper mapper;
    private ArrayNode output;
    private UserDatabase userDatabase;

    private OutputGenerator(ObjectMapper mapper, ArrayNode output, UserDatabase userDatabase) {
        this.mapper = mapper;
        this.output = output;
        this.userDatabase = userDatabase;
    }
    public static OutputGenerator getInstance(ObjectMapper mapper,
                                              ArrayNode output,
                                              UserDatabase userDatabase) {
        if (instance == null) {
            instance = new OutputGenerator(mapper, output, userDatabase);
        }
        return instance;
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

}
