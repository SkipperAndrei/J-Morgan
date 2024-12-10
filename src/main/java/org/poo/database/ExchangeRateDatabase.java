package org.poo.database;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import lombok.Data;

import java.util.HashSet;

@Data
public final class ExchangeRateDatabase {

    private static ExchangeRateDatabase instance;
    private DefaultDirectedWeightedGraph<String, DefaultWeightedEdge> exchangeGraph;

    private ExchangeRateDatabase() {
        exchangeGraph = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
    }

    public static ExchangeRateDatabase getInstance() {
        if (instance == null) {
            instance = new ExchangeRateDatabase();
        }
        return instance;
    }

    public void addNewExchange(final String from, final String to, final double rate) {
        exchangeGraph.addVertex(from);
        exchangeGraph.addVertex(to);
        exchangeGraph.setEdgeWeight(exchangeGraph.addEdge(from, to), rate);
    }


    public void resetExchangeDatabase() {
        exchangeGraph.removeAllVertices(new HashSet<>(exchangeGraph.vertexSet()));
    }

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

    public double getExchangeRate(final String from, final String to) {

        boolean possible = addUnknownExchange(from, to);

        if (!possible) {
            throw new NullPointerException("Can't convert currency");
        }

        DefaultWeightedEdge edge = exchangeGraph.getEdge(from, to);
        return exchangeGraph.getEdgeWeight(edge);
    }

}
