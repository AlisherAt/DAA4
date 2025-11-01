package graph.scc;

import graph.Metrics;
import java.util.*;

public class SCC {
    private int n;
    private List<List<Integer>> adj;
    private int[] low, ids;
    private boolean[] onStack;
    private Deque<Integer> stack;
    private int id, sccCount;
    private List<List<Integer>> components;
    private Metrics metrics;

    public SCC(int n, List<List<Integer>> adj, Metrics metrics) {
        this.n = n;
        this.adj = adj;
        this.metrics = metrics;
        this.low = new int[n];
        this.ids = new int[n];
        this.onStack = new boolean[n];
        this.stack = new ArrayDeque<>();
        this.components = new ArrayList<>();
        Arrays.fill(ids, -1);
    }

    public List<List<Integer>> findSCCs() {
        metrics.reset();
        long startTime = System.nanoTime();

        for (int i = 0; i < n; i++) {
            if (ids[i] == -1) {
                dfs(i);
            }
        }

        metrics.incrementOperationCount(); // For timing measurement
        return components;
    }

    private void dfs(int at) {
        metrics.incrementOperationCount();
        stack.push(at);
        onStack[at] = true;
        ids[at] = low[at] = id++;

        for (int to : adj.get(at)) {
            metrics.incrementOperationCount(); // Edge visit
            if (ids[to] == -1) {
                dfs(to);
            }
            if (onStack[to]) {
                low[at] = Math.min(low[at], low[to]);
            }
        }

        if (ids[at] == low[at]) {
            List<Integer> component = new ArrayList<>();
            while (!stack.isEmpty()) {
                int node = stack.pop();
                onStack[node] = false;
                low[node] = ids[at];
                component.add(node);
                if (node == at) break;
            }
            components.add(component);
            sccCount++;
        }
    }

    public List<List<Integer>> getCondensationGraph() {
        List<List<Integer>> condensation = new ArrayList<>();
        for (int i = 0; i < sccCount; i++) {
            condensation.add(new ArrayList<>());
        }

        // Map each node to its component ID
        int[] compId = new int[n];
        for (int i = 0; i < components.size(); i++) {
            for (int node : components.get(i)) {
                compId[node] = i;
            }
        }

        // Build condensation graph edges
        Set<String> edges = new HashSet<>();
        for (int u = 0; u < n; u++) {
            for (int v : adj.get(u)) {
                int compU = compId[u];
                int compV = compId[v];
                if (compU != compV) {
                    String edge = compU + "->" + compV;
                    if (!edges.contains(edge)) {
                        edges.add(edge);
                        condensation.get(compU).add(compV);
                    }
                }
            }
        }

        return condensation;
    }
}