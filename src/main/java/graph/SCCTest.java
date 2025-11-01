package graph;

import graph.scc.SCC;
import org.testng.annotations.Test;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class SCCTest {

    @Test
    void testSCCSimpleGraph() {
        // Create a simple graph with 2 SCCs
        int n = 4;
        List<List<Integer>> adj = new ArrayList<>();
        for (int i = 0; i < n; i++) adj.add(new ArrayList<>());

        // SCC 1: 0->1->2->0
        adj.get(0).add(1);
        adj.get(1).add(2);
        adj.get(2).add(0);

        // SCC 2: 3 (single node)
        adj.get(3).add(3);

        BasicMetrics metrics = new BasicMetrics();
        SCC scc = new SCC(n, adj, metrics);
        List<List<Integer>> components = scc.findSCCs();

        assertEquals(2, components.size());
        assertTrue(components.stream().anyMatch(comp -> comp.size() == 3));
        assertTrue(components.stream().anyMatch(comp -> comp.size() == 1));
    }
}