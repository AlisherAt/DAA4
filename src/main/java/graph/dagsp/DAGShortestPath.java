package graph.dagsp;

import graph.Metrics;
import java.util.*;

public class DAGShortestPath {
    private Metrics metrics;

    public DAGShortestPath(Metrics metrics) {
        this.metrics = metrics;
    }

    public static class Result {
        public int[] distances;
        public int[] predecessors;
        public int criticalPathLength;
        public List<Integer> criticalPath;

        public Result(int n) {
            this.distances = new int[n];
            this.predecessors = new int[n];
            Arrays.fill(predecessors, -1);
        }
    }

    public Result shortestPath(List<List<int[]>> graph, List<Integer> topoOrder, int source) {
        metrics.reset();
        long startTime = System.nanoTime();

        int n = graph.size();
        Result result = new Result(n);
        Arrays.fill(result.distances, Integer.MAX_VALUE);
        result.distances[source] = 0;

        // Process nodes in topological order
        for (int u : topoOrder) {
            metrics.incrementOperationCount();
            if (result.distances[u] != Integer.MAX_VALUE) {
                for (int[] edge : graph.get(u)) {
                    metrics.incrementOperationCount();
                    int v = edge[0];
                    int w = edge[1];
                    if (result.distances[u] + w < result.distances[v]) {
                        result.distances[v] = result.distances[u] + w;
                        result.predecessors[v] = u;
                    }
                }
            }
        }

        return result;
    }

    public Result longestPath(List<List<int[]>> graph, List<Integer> topoOrder, int source) {
        metrics.reset();
        long startTime = System.nanoTime();

        int n = graph.size();
        Result result = new Result(n);
        Arrays.fill(result.distances, Integer.MIN_VALUE);
        result.distances[source] = 0;

        // For longest path, we can invert weights and find shortest path
        // or directly compute max distances
        for (int u : topoOrder) {
            metrics.incrementOperationCount();
            if (result.distances[u] != Integer.MIN_VALUE) {
                for (int[] edge : graph.get(u)) {
                    metrics.incrementOperationCount();
                    int v = edge[0];
                    int w = edge[1];
                    if (result.distances[u] + w > result.distances[v]) {
                        result.distances[v] = result.distances[u] + w;
                        result.predecessors[v] = u;
                    }
                }
            }
        }

        // Find critical path (longest path)
        result.criticalPathLength = Integer.MIN_VALUE;
        int endNode = -1;
        for (int i = 0; i < n; i++) {
            if (result.distances[i] > result.criticalPathLength &&
                    result.distances[i] != Integer.MIN_VALUE) {
                result.criticalPathLength = result.distances[i];
                endNode = i;
            }
        }

        // Reconstruct critical path
        if (endNode != -1) {
            result.criticalPath = reconstructPath(result.predecessors, endNode);
        }

        return result;
    }

    private List<Integer> reconstructPath(int[] predecessors, int endNode) {
        List<Integer> path = new ArrayList<>();
        for (int at = endNode; at != -1; at = predecessors[at]) {
            path.add(at);
        }
        Collections.reverse(path);
        return path;
    }
}