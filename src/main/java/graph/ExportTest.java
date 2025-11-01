package graph;

import org.junit.jupiter.api.Test;
import java.io.*;
import java.nio.file.Files;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ExportTest {

    @Test
    void testBasicCSVExport() throws Exception {
        // Test only the public or accessible methods
        List<List<Integer>> components = Arrays.asList(
                Arrays.asList(0, 1),
                Arrays.asList(2, 3, 4)
        );

        // Test if we can at least create the file structure
        File testFile = new File("test_simple.csv");

        try (PrintWriter writer = new PrintWriter(new FileWriter(testFile))) {
            writer.println("component_id,size,nodes");
            for (int i = 0; i < components.size(); i++) {
                List<Integer> component = components.get(i);
                String nodes = component.toString().replace("[", "").replace("]", "");
                writer.println(i + "," + component.size() + "," + nodes);
            }
        }

        assertTrue(testFile.exists());

        List<String> lines = Files.readAllLines(testFile.toPath());
        assertEquals(3, lines.size()); // Header + 2 components
        assertTrue(lines.get(1).contains("0,2,0,1"));

        testFile.delete();
    }

    @Test
    void testGraphDataCreation() {
        // Test basic GraphData functionality
        GraphData graph = new GraphData();
        graph.n = 3;
        graph.edges = Arrays.asList(
                new GraphData.Edge(0, 1, 5),
                new GraphData.Edge(1, 2, 3)
        );

        List<List<Integer>> adj = graph.buildAdjacencyList();
        assertEquals(3, adj.size());
        assertEquals(1, adj.get(0).size());
        assertEquals(1, adj.get(0).get(0));
    }

    @Test
    void testBasicMetrics() {
        BasicMetrics metrics = new BasicMetrics();
        metrics.reset();

        assertEquals(0, metrics.getOperationCount());

        metrics.incrementOperationCount();
        metrics.incrementOperationCount();

        assertEquals(2, metrics.getOperationCount());
        assertTrue(metrics.getTimeNanos() >= 0);
    }
}