package org.poo.database;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import lombok.Data;

import java.util.HashSet;

/**
 * This class is responsible to hold information about currency exchange rates
 * The exchange rates will be held as a weighted oriented graph
 * Since the exchange rates are unique, there will be only one instance of this database
 */
@Data
public final class ExchangeRateDatabase {

    private static ExchangeRateDatabase instance;
    private DefaultDirectedWeightedGraph<String, DefaultWeightedEdge> exchangeGraph;

    private ExchangeRateDatabase() {
        exchangeGraph = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
    }

    /**
     * Function that gets the unique instance of the exchange rate database
     * If there isn't an instance previously defined, it creates it.
     * @return The instance
     */
    public static ExchangeRateDatabase getInstance() {
        if (instance == null) {
            instance = new ExchangeRateDatabase();
        }
        return instance;
    }

    /**
     * Add a new edge between two currencies
     * @param from The source node
     * @param to The destination node
     * @param rate The exchange rate between the two nodes
     */
    public void addNewExchange(final String from, final String to, final double rate) {
        exchangeGraph.addVertex(from);
        exchangeGraph.addVertex(to);
        exchangeGraph.setEdgeWeight(exchangeGraph.addEdge(from, to), rate);
    }

    /**
     * This function will clear all the information from the database
     */
    public void resetExchangeDatabase() {
        exchangeGraph.removeAllVertices(new HashSet<>(exchangeGraph.vertexSet()));
    }

    /**
     * This function will try to add a new exchange rate between 2 nodes
     * The difference between this function and "addExchange" is that in this function
     * the exchange rate is unknown
     * To discover it, we need a path between the two nodes
     * For this, the method will use Dijkstra's algorithm between the two nodes
     * The exchange rates from the edges that make the path will be multiplied after
     * learning the path
     * @param from The source node
     * @param to The destination node
     * @return True, if the conversion is possible, False otherwise
     */
    public boolean addUnknownExchange(final String from, final String to) {

        if (exchangeGraph.containsEdge(from, to)) {
            return true;
        }

        DijkstraShortestPath<String, DefaultWeightedEdge> dijkstra =
                                        new DijkstraShortestPath<>(exchangeGraph);

        GraphPath<String, DefaultWeightedEdge> path = dijkstra.getPath(from, to);

        try {
            if (path.getEdgeList().size() == 1) {
                return true;
            }

            double startingRate = 1;
            for (DefaultWeightedEdge edge : path.getEdgeList()) {
                startingRate *= exchangeGraph.getEdgeWeight(edge);
            }
            addNewExchange(from, to, startingRate);
            return true;
        } catch (NullPointerException e) {
            return false;
        }
    }

    /**
     * This function will extract the currency exchange rate between two nodes if possible
     * @param from The source node
     * @param to The destination node
     * @return The currency exchange rate, if possible to determine it
     */
    public double getExchangeRate(final String from, final String to) {

        boolean possible = addUnknownExchange(from, to);

        if (!possible) {
            throw new NullPointerException("Can't convert currency");
        }

        DefaultWeightedEdge edge = exchangeGraph.getEdge(from, to);
        return exchangeGraph.getEdgeWeight(edge);
    }

}
