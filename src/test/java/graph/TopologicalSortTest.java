package graph;

import graph.topo.TopologicalSort;
import org.junit.jupiter.api.Test;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class TopologicalSortTest {

    @Test
    void testTopologicalSortDAG() {
        // Create a simple DAG: 0->1->2, 0->3
        int n = 4;
        List<List<Integer>> graph = new ArrayList<>();
        for (int i = 0; i < n; i++) graph.add(new ArrayList<>());

        graph.get(0).add(1);
        graph.get(0).add(3);
        graph.get(1).add(2);

        BasicMetrics metrics = new BasicMetrics();
        TopologicalSort topo = new TopologicalSort(metrics);
        List<Integer> order = topo.kahnTopoSort(graph);

        assertEquals(n, order.size());
        // Verify topological order property
        int index0 = order.indexOf(0);
        int index1 = order.indexOf(1);
        int index2 = order.indexOf(2);
        int index3 = order.indexOf(3);

        assertTrue(index0 < index1);
        assertTrue(index1 < index2);
        assertTrue(index0 < index3);
    }
}