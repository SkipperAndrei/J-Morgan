package org.poo.database;


import com.fasterxml.jackson.databind.node.ArrayNode;

import java.util.LinkedHashMap;
import java.util.Map;

public class PaymentDatabase {

    private Map<String, ArrayNode> payments = new LinkedHashMap<>();
}
