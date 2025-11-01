package graph.topo;

import graph.Metrics;
import java.util.*;

public class TopologicalSort {
    private Metrics metrics;

    public TopologicalSort(Metrics metrics) {
        this.metrics = metrics;
    }

    public List<Integer> kahnTopoSort(List<List<Integer>> graph) {
        metrics.reset();
        long startTime = System.nanoTime();

        int n = graph.size();
        int[] inDegree = new int[n];

        // Calculate in-degrees
        for (int u = 0; u < n; u++) {
            for (int v : graph.get(u)) {
                metrics.incrementOperationCount();
                inDegree[v]++;
            }
        }

        // Initialize queue with nodes having 0 in-degree
        Queue<Integer> queue = new LinkedList<>();
        for (int i = 0; i < n; i++) {
            metrics.incrementOperationCount();
            if (inDegree[i] == 0) {
                queue.offer(i);
            }
        }

        List<Integer> topoOrder = new ArrayList<>();
        while (!queue.isEmpty()) {
            metrics.incrementOperationCount();
            int u = queue.poll();
            topoOrder.add(u);

            for (int v : graph.get(u)) {
                metrics.incrementOperationCount();
                inDegree[v]--;
                if (inDegree[v] == 0) {
                    queue.offer(v);
                }
            }
        }

        return topoOrder;
    }
}