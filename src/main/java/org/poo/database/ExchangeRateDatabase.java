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
    DefaultDirectedWeightedGraph<String, DefaultWeightedEdge> exchangeGraph;

    private ExchangeRateDatabase() {
        exchangeGraph = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
    }

    public static ExchangeRateDatabase getInstance() {
        if (instance == null) {
            instance = new ExchangeRateDatabase();
        }
        return instance;
    }

    public void addNewExchange(String from, String to, double rate) {
        exchangeGraph.addVertex(from);
        exchangeGraph.addVertex(to);
        exchangeGraph.setEdgeWeight(exchangeGraph.addEdge(from, to), rate);
    }


    public void resetExchangeDatabase() {
        exchangeGraph.removeAllVertices(new HashSet<>(exchangeGraph.vertexSet()));
    }

    public boolean addUnknownExchange(String from, String to) {

        DijkstraShortestPath<String, DefaultWeightedEdge> djikstra = new DijkstraShortestPath<>(exchangeGraph);
        GraphPath<String, DefaultWeightedEdge> path = djikstra.getPath(from, to);

        try {
            if (path.getEdgeList().size() == 1)
                return true;

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



}
