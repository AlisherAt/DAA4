package graph;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;
import java.util.*;

public class DatasetGenerator {
    private static final Random random = new Random(42);

    public static void main(String[] args) {
        // Create directories first
        createDirectories();
        generateAllDatasets();
        generateSummary();
    }

    private static void createDirectories() {
        // Create data directory
        File dataDir = new File("data");
        if (!dataDir.exists()) {
            if (dataDir.mkdirs()) {
                System.out.println("Created directory: data/");
            } else {
                System.err.println("Failed to create directory: data/");
            }
        }

        // Create results directories
        File resultsCsvDir = new File("results/csv");
        if (!resultsCsvDir.exists()) {
            if (resultsCsvDir.mkdirs()) {
                System.out.println("Created directory: results/csv/");
            } else {
                System.err.println("Failed to create directory: results/csv/");
            }
        }

        File resultsJsonDir = new File("results/json");
        if (!resultsJsonDir.exists()) {
            if (resultsJsonDir.mkdirs()) {
                System.out.println("Created directory: results/json/");
            } else {
                System.err.println("Failed to create directory: results/json/");
            }
        }
    }

    public static void generateAllDatasets() {
        System.out.println("=== Generating All Datasets ===");

        // Small datasets
        generateDataset("small_cycle", 8, 12, true, 0.8, false, "cyclic");
        generateDataset("small_dag", 10, 15, false, 0.0, false, "acyclic");
        generateDataset("small_mixed", 9, 14, true, 0.4, true, "mixed");

        // Medium datasets
        generateDataset("medium_complex_dag", 15, 25, false, 0.0, false, "acyclic");
        generateDataset("medium_mixed", 18, 30, true, 0.3, true, "mixed");
        generateDataset("medium_multiple_scc", 20, 35, true, 0.6, true, "cyclic");

        // Large datasets
        generateDataset("large_sparse", 30, 45, true, 0.2, true, "sparse");
        generateDataset("large_medium", 40, 80, true, 0.4, true, "medium");
        generateDataset("large_complex_scc", 50, 120, true, 0.7, true, "dense");

        System.out.println("=== All datasets generated successfully ===");
    }

    private static void generateDataset(String name, int nodes, int targetEdges,
                                        boolean allowCycles, double cycleProbability,
                                        boolean multipleSCC, String complexity) {
        GraphData graph = new GraphData();
        graph.n = nodes;
        graph.edges = new ArrayList<>();
        graph.source = 0;
        graph.weightModel = "edge";

        Set<String> edgeSet = new HashSet<>();

        // Create different graph structures based on complexity
        switch (complexity) {
            case "acyclic":
                generateDAG(graph, edgeSet, nodes, targetEdges);
                break;
            case "cyclic":
                generateCyclicGraph(graph, edgeSet, nodes, targetEdges);
                break;
            case "mixed":
                generateMixedGraph(graph, edgeSet, nodes, targetEdges);
                break;
            case "sparse":
                generateSparseGraph(graph, edgeSet, nodes, targetEdges);
                break;
            case "dense":
                generateDenseGraph(graph, edgeSet, nodes, targetEdges);
                break;
            default:
                generateMixedGraph(graph, edgeSet, nodes, targetEdges);
        }

        // Save to file
        File outputFile = new File("data/" + name + ".json");
        try (FileWriter writer = new FileWriter(outputFile)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(graph, writer);
            System.out.println("✓ Generated: " + name + " - " + nodes + " nodes, " +
                    graph.edges.size() + " edges, " + complexity + " complexity");
        } catch (IOException e) {
            System.err.println("✗ Failed to generate " + name + ": " + e.getMessage());
        }
    }

    private static void generateDAG(GraphData graph, Set<String> edgeSet, int nodes, int targetEdges) {
        // Generate a proper DAG (no cycles)
        for (int i = 1; i < nodes; i++) {
            int u = random.nextInt(i);
            int weight = random.nextInt(10) + 1;
            String edgeKey = u + "->" + i;
            edgeSet.add(edgeKey);
            graph.edges.add(new GraphData.Edge(u, i, weight));
        }

        // Add additional edges maintaining DAG property
        while (graph.edges.size() < targetEdges) {
            int u = random.nextInt(nodes - 1);
            int v = random.nextInt(nodes - u - 1) + u + 1;

            String edgeKey = u + "->" + v;
            if (!edgeSet.contains(edgeKey)) {
                edgeSet.add(edgeKey);
                int weight = random.nextInt(10) + 1;
                graph.edges.add(new GraphData.Edge(u, v, weight));
            }

            // Safety break
            if (graph.edges.size() >= targetEdges) break;
        }
    }

    private static void generateCyclicGraph(GraphData graph, Set<String> edgeSet, int nodes, int targetEdges) {
        // Start with a cycle
        for (int i = 0; i < nodes; i++) {
            int u = i;
            int v = (i + 1) % nodes;
            String edgeKey = u + "->" + v;
            if (!edgeSet.contains(edgeKey)) {
                edgeSet.add(edgeKey);
                int weight = random.nextInt(10) + 1;
                graph.edges.add(new GraphData.Edge(u, v, weight));
            }
        }

        // Add random edges
        while (graph.edges.size() < targetEdges) {
            int u = random.nextInt(nodes);
            int v = random.nextInt(nodes);

            String edgeKey = u + "->" + v;
            if (!edgeSet.contains(edgeKey)) {
                edgeSet.add(edgeKey);
                int weight = random.nextInt(10) + 1;
                graph.edges.add(new GraphData.Edge(u, v, weight));
            }

            // Safety break
            if (graph.edges.size() >= targetEdges) break;
        }
    }

    private static void generateMixedGraph(GraphData graph, Set<String> edgeSet, int nodes, int targetEdges) {
        // Mix of DAG and cyclic structures
        generateDAG(graph, edgeSet, nodes, targetEdges / 2);

        // Add some cycles
        int cyclesToAdd = Math.max(1, nodes / 5);
        for (int i = 0; i < cyclesToAdd; i++) {
            int cycleSize = random.nextInt(3) + 3; // 3-5 node cycles
            List<Integer> cycleNodes = new ArrayList<>();
            for (int j = 0; j < cycleSize; j++) {
                cycleNodes.add(random.nextInt(nodes));
            }

            // Create cycle
            for (int j = 0; j < cycleSize; j++) {
                int u = cycleNodes.get(j);
                int v = cycleNodes.get((j + 1) % cycleSize);
                String edgeKey = u + "->" + v;
                if (!edgeSet.contains(edgeKey)) {
                    edgeSet.add(edgeKey);
                    int weight = random.nextInt(10) + 1;
                    graph.edges.add(new GraphData.Edge(u, v, weight));
                }
            }
        }

        // Fill remaining edges
        while (graph.edges.size() < targetEdges) {
            int u = random.nextInt(nodes);
            int v = random.nextInt(nodes);

            String edgeKey = u + "->" + v;
            if (!edgeSet.contains(edgeKey)) {
                edgeSet.add(edgeKey);
                int weight = random.nextInt(10) + 1;
                graph.edges.add(new GraphData.Edge(u, v, weight));
            }

            // Safety break
            if (graph.edges.size() >= targetEdges) break;
        }
    }

    private static void generateSparseGraph(GraphData graph, Set<String> edgeSet, int nodes, int targetEdges) {
        // Sparse graph - mostly tree-like with few extra edges
        generateDAG(graph, edgeSet, nodes, Math.min(targetEdges, nodes * 2));
    }

    private static void generateDenseGraph(GraphData graph, Set<String> edgeSet, int nodes, int targetEdges) {
        // Dense graph - many edges
        for (int u = 0; u < nodes; u++) {
            for (int v = 0; v < nodes; v++) {
                if (u != v && graph.edges.size() < targetEdges) {
                    String edgeKey = u + "->" + v;
                    if (!edgeSet.contains(edgeKey)) {
                        edgeSet.add(edgeKey);
                        int weight = random.nextInt(10) + 1;
                        graph.edges.add(new GraphData.Edge(u, v, weight));
                    }
                }
            }
        }
    }

    private static void generateSummary() {
        System.out.println("=== Generating Summary Files ===");

        List<Map<String, Object>> summary = new ArrayList<>();
        File dataDir = new File("data");

        if (dataDir.exists() && dataDir.isDirectory()) {
            File[] files = dataDir.listFiles((dir, name) -> name.endsWith(".json"));
            if (files != null) {
                for (File file : files) {
                    try {
                        Gson gson = new Gson();
                        GraphData graph = gson.fromJson(new FileReader(file), GraphData.class);

                        Map<String, Object> info = new LinkedHashMap<>();
                        info.put("dataset", file.getName().replace(".json", ""));
                        info.put("nodes", graph.n);
                        info.put("edges", graph.edges.size());
                        double density = graph.n > 1 ?
                                (double) graph.edges.size() / (graph.n * (graph.n - 1)) : 0;
                        info.put("density", String.format("%.3f", density));
                        info.put("source", graph.source);
                        info.put("weight_model", graph.weightModel);

                        summary.add(info);
                        System.out.println("✓ Processed: " + file.getName());
                    } catch (Exception e) {
                        System.err.println("✗ Failed to process " + file.getName() + ": " + e.getMessage());
                    }
                }
            }
        }

        // Export summary to JSON
        try (FileWriter writer = new FileWriter("results/json/summary.json")) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(summary, writer);
            System.out.println("✓ Generated: results/json/summary.json");
        } catch (IOException e) {
            System.err.println("✗ Failed to create summary.json: " + e.getMessage());
        }

        // Export summary to CSV
        try (PrintWriter writer = new PrintWriter(new FileWriter("results/json/summary.csv"))) {
            writer.println("dataset,nodes,edges,density,source,weight_model");
            for (Map<String, Object> info : summary) {
                writer.printf("%s,%d,%d,%s,%d,%s\n",
                        info.get("dataset"), info.get("nodes"), info.get("edges"),
                        info.get("density"), info.get("source"), info.get("weight_model"));
            }
            System.out.println("✓ Generated: results/json/summary.csv");
        } catch (IOException e) {
            System.err.println("✗ Failed to create summary.csv: " + e.getMessage());
        }
    }
}