package graph;

import java.util.*;

public class GraphData {
    public int n;
    public List<Edge> edges;
    public int source;
    public String weightModel;

    public static class Edge {
        public int u, v, w;

        public Edge(int u, int v, int w) {
            this.u = u;
            this.v = v;
            this.w = w;
        }
    }

    public List<List<Integer>> buildAdjacencyList() {
        List<List<Integer>> adj = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            adj.add(new ArrayList<>());
        }
        for (Edge edge : edges) {
            adj.get(edge.u).add(edge.v);
        }
        return adj;
    }

    public List<List<int[]>> buildWeightedAdjacencyList() {
        List<List<int[]>> adj = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            adj.add(new ArrayList<>());
        }
        for (Edge edge : edges) {
            adj.get(edge.u).add(new int[]{edge.v, edge.w});
        }
        return adj;
    }
}