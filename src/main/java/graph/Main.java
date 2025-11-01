package graph;

import graph.scc.SCC;
import graph.topo.TopologicalSort;
import graph.dagsp.DAGShortestPath;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;
import java.util.*;
import java.nio.file.*;
import java.util.stream.Collectors;

public class Main {
    private static class BasicMetrics implements Metrics {
        private long operationCount = 0;
        private long startTime;

        @Override
        public void reset() {
            operationCount = 0;
            startTime = System.nanoTime();
        }

        @Override
        public long getOperationCount() {
            return operationCount;
        }

        @Override
        public long getTimeNanos() {
            return System.nanoTime() - startTime;
        }

        @Override
        public void incrementOperationCount() {
            operationCount++;
        }
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java graph.Main <input-file>");
            System.out.println("Available datasets:");
            listAvailableDatasets();
            return;
        }

        try {
            String inputFile = args[0];
            File file = new File(inputFile);

            if (!file.exists()) {
                System.err.println("File not found: " + inputFile);
                System.out.println("Available datasets:");
                listAvailableDatasets();
                return;
            }

            String baseName = getBaseName(inputFile);

            // Create results directory structure
            createDirectories();

            // Process the graph
            processGraph(inputFile, baseName);

        } catch (Exception e) {
            System.err.println("Error processing file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void listAvailableDatasets() {
        File dataDir = new File("data");
        if (dataDir.exists() && dataDir.isDirectory()) {
            File[] files = dataDir.listFiles((dir, name) -> name.endsWith(".json"));
            if (files != null && files.length > 0) {
                for (File file : files) {
                    System.out.println("  data/" + file.getName());
                }
            } else {
                System.out.println("  No datasets found. Run DatasetGenerator first.");
            }
        } else {
            System.out.println("  Data directory not found. Run DatasetGenerator first.");
        }
    }

    private static void processGraph(String inputFile, String baseName) throws Exception {
        System.out.println("=== Processing: " + baseName + " ===");

        // Read input file
        Gson gson = new Gson();
        GraphData graphData = gson.fromJson(new FileReader(inputFile), GraphData.class);

        System.out.println("Graph loaded: " + graphData.n + " nodes, " + graphData.edges.size() + " edges");

        BasicMetrics metrics = new BasicMetrics();

        // 1. SCC Detection
        System.out.println("1. Finding Strongly Connected Components...");
        List<List<Integer>> adj = graphData.buildAdjacencyList();
        SCC scc = new SCC(graphData.n, adj, metrics);
        List<List<Integer>> components = scc.findSCCs();

        System.out.println("   Found " + components.size() + " SCCs");

        // Export SCC components to CSV
        exportComponentsToCSV(components, "results/csv/" + baseName + "_components.csv");
        System.out.println("   ✓ Exported components to CSV");

        // 2. Condensation Graph and Topological Sort
        System.out.println("2. Building condensation graph and topological sort...");
        List<List<Integer>> condensation = scc.getCondensationGraph();
        TopologicalSort topo = new TopologicalSort(metrics);
        List<Integer> topoOrder = topo.kahnTopoSort(condensation);

        System.out.println("   Condensation graph: " + condensation.size() + " nodes");
        System.out.println("   Topological order computed");

        // 3. Shortest and Longest Paths in DAG
        System.out.println("3. Computing shortest and longest paths...");
        List<List<int[]>> weightedAdj = graphData.buildWeightedAdjacencyList();
        DAGShortestPath dagSP = new DAGShortestPath(metrics);

        List<List<int[]>> weightedCondensation = createWeightedCondensation(
                condensation, components, graphData.edges);

        DAGShortestPath.Result shortest = dagSP.shortestPath(
                weightedCondensation, topoOrder, 0);

        DAGShortestPath.Result longest = dagSP.longestPath(
                weightedCondensation, topoOrder, 0);

        System.out.println("   Critical path length: " + longest.criticalPathLength);

        // Export metrics to CSV
        exportMetricsToCSV(graphData, components, condensation, topoOrder,
                shortest, longest, metrics, "results/csv/" + baseName + "_metrics.csv");
        System.out.println("   ✓ Exported metrics to CSV");

        // Export paths to CSV
        exportPathsToCSV(shortest, longest, "results/csv/" + baseName + "_paths.csv");
        System.out.println("   ✓ Exported paths to CSV");

        // Export full results to JSON
        exportFullResultsToJSON(graphData, components, condensation, topoOrder,
                shortest, longest, metrics, "results/json/" + baseName + "_full.json");
        System.out.println("   ✓ Exported full results to JSON");

        System.out.println("=== Completed: " + baseName + " ===");
        System.out.println();
    }

    private static void createDirectories() {
        File resultsCsvDir = new File("results/csv");
        if (!resultsCsvDir.exists()) {
            resultsCsvDir.mkdirs();
        }

        File resultsJsonDir = new File("results/json");
        if (!resultsJsonDir.exists()) {
            resultsJsonDir.mkdirs();
        }
    }

    private static String getBaseName(String filePath) {
        File file = new File(filePath);
        String name = file.getName();
        if (name.contains(".")) {
            name = name.substring(0, name.lastIndexOf('.'));
        }
        return name;
    }

    static void exportComponentsToCSV(List<List<Integer>> components, String filePath) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.println("component_id,size,node_list,is_cycle");
            for (int i = 0; i < components.size(); i++) {
                List<Integer> component = components.get(i);
                String nodeList = component.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(", ", "[", "]"));
                boolean isCycle = component.size() > 1; // Components with more than 1 node are cycles
                writer.println(i + "," + component.size() + "," + nodeList + "," + isCycle);
            }
        } catch (IOException e) {
            System.err.println("Error exporting components to CSV: " + e.getMessage());
        }
    }

    static void exportMetricsToCSV(GraphData graphData, List<List<Integer>> components,
                                   List<List<Integer>> condensation, List<Integer> topoOrder,
                                   DAGShortestPath.Result shortest, DAGShortestPath.Result longest,
                                   Metrics metrics, String filePath) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.println("metric,value,description");

            // Graph metrics
            writer.println("nodes," + graphData.n + ",Total number of nodes in graph");
            writer.println("edges," + graphData.edges.size() + ",Total number of edges in graph");
            double density = graphData.n > 1 ?
                    (double) graphData.edges.size() / (graphData.n * (graphData.n - 1)) : 0;
            writer.println("density," + String.format("%.4f", density) + ",Graph density");
            writer.println("source," + graphData.source + ",Source node for path algorithms");
            writer.println("weight_model," + graphData.weightModel + ",Weight model used");

            // SCC metrics
            writer.println("scc_count," + components.size() + ",Number of strongly connected components");
            writer.println("largest_scc," + components.stream().mapToInt(List::size).max().orElse(0) + ",Size of largest SCC");
            writer.println("smallest_scc," + components.stream().mapToInt(List::size).min().orElse(0) + ",Size of smallest SCC");
            writer.println("avg_scc_size," + String.format("%.2f",
                    components.stream().mapToInt(List::size).average().orElse(0)) + ",Average SCC size");

            // Condensation graph metrics
            int condensationEdges = condensation.stream().mapToInt(List::size).sum();
            writer.println("condensation_nodes," + condensation.size() + ",Nodes in condensation graph");
            writer.println("condensation_edges," + condensationEdges + ",Edges in condensation graph");

            // Path metrics
            writer.println("critical_path_length," + longest.criticalPathLength + ",Length of critical path");
            writer.println("operations_count," + metrics.getOperationCount() + ",Total operations performed");
            writer.println("execution_time_ns," + metrics.getTimeNanos() + ",Execution time in nanoseconds");

            // Topological order
            String topoOrderStr = topoOrder.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(" -> "));
            writer.println("topological_order," + topoOrderStr + ",Valid topological order");

        } catch (IOException e) {
            System.err.println("Error exporting metrics to CSV: " + e.getMessage());
        }
    }

    static void exportPathsToCSV(DAGShortestPath.Result shortest,
                                 DAGShortestPath.Result longest,
                                 String filePath) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.println("path_type,target_node,distance,path,is_critical");

            // Critical path (longest path)
            if (longest.criticalPath != null && !longest.criticalPath.isEmpty()) {
                String criticalPathStr = longest.criticalPath.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(" -> "));
                writer.println("critical_path,-1," + longest.criticalPathLength +
                        "," + criticalPathStr + ",true");
            }

            // Shortest paths to all reachable nodes
            for (int i = 0; i < shortest.distances.length; i++) {
                if (shortest.distances[i] != Integer.MAX_VALUE && shortest.distances[i] != Integer.MIN_VALUE) {
                    List<Integer> path = reconstructPath(shortest.predecessors, i);
                    String pathStr = path.stream()
                            .map(String::valueOf)
                            .collect(Collectors.joining(" -> "));
                    boolean isCritical = (longest.criticalPath != null &&
                            longest.criticalPath.contains(i) &&
                            path.equals(longest.criticalPath));
                    writer.println("shortest_path," + i + "," + shortest.distances[i] +
                            "," + pathStr + "," + isCritical);
                }
            }

            // Longest paths to all reachable nodes
            for (int i = 0; i < longest.distances.length; i++) {
                if (longest.distances[i] != Integer.MIN_VALUE && longest.distances[i] != Integer.MAX_VALUE) {
                    List<Integer> path = reconstructPath(longest.predecessors, i);
                    String pathStr = path.stream()
                            .map(String::valueOf)
                            .collect(Collectors.joining(" -> "));
                    boolean isCritical = (longest.criticalPath != null &&
                            longest.criticalPath.contains(i) &&
                            path.equals(longest.criticalPath));
                    writer.println("longest_path," + i + "," + longest.distances[i] +
                            "," + pathStr + "," + isCritical);
                }
            }

        } catch (IOException e) {
            System.err.println("Error exporting paths to CSV: " + e.getMessage());
        }
    }

    static void exportFullResultsToJSON(GraphData graphData, List<List<Integer>> components,
                                        List<List<Integer>> condensation, List<Integer> topoOrder,
                                        DAGShortestPath.Result shortest, DAGShortestPath.Result longest,
                                        Metrics metrics, String filePath) {
        try {
            Map<String, Object> results = new LinkedHashMap<>();

            // Graph info
            results.put("dataset", getBaseName(filePath).replace("_full", ""));
            results.put("nodes", graphData.n);
            results.put("edges", graphData.edges.size());
            results.put("source", graphData.source);
            results.put("weight_model", graphData.weightModel);

            // SCC results
            results.put("scc_count", components.size());
            results.put("components", components);
            results.put("largest_scc_size", components.stream().mapToInt(List::size).max().orElse(0));
            results.put("smallest_scc_size", components.stream().mapToInt(List::size).min().orElse(0));
            results.put("average_scc_size", String.format("%.2f",
                    components.stream().mapToInt(List::size).average().orElse(0)));

            // Condensation graph
            results.put("condensation_nodes", condensation.size());
            results.put("condensation_edges", condensation.stream().mapToInt(List::size).sum());
            results.put("condensation_graph", condensation);

            // Topological order
            results.put("topological_order", topoOrder);

            // Path results
            Map<String, Object> pathResults = new LinkedHashMap<>();
            pathResults.put("critical_path_length", longest.criticalPathLength);
            pathResults.put("critical_path", longest.criticalPath);

            Map<String, Object> shortestPaths = new LinkedHashMap<>();
            for (int i = 0; i < shortest.distances.length; i++) {
                if (shortest.distances[i] != Integer.MAX_VALUE) {
                    Map<String, Object> pathInfo = new LinkedHashMap<>();
                    pathInfo.put("distance", shortest.distances[i]);
                    pathInfo.put("path", reconstructPath(shortest.predecessors, i));
                    shortestPaths.put("node_" + i, pathInfo);
                }
            }
            pathResults.put("shortest_paths", shortestPaths);

            Map<String, Object> longestPaths = new LinkedHashMap<>();
            for (int i = 0; i < longest.distances.length; i++) {
                if (longest.distances[i] != Integer.MIN_VALUE && longest.distances[i] != Integer.MAX_VALUE) {
                    Map<String, Object> pathInfo = new LinkedHashMap<>();
                    pathInfo.put("distance", longest.distances[i]);
                    pathInfo.put("path", reconstructPath(longest.predecessors, i));
                    longestPaths.put("node_" + i, pathInfo);
                }
            }
            pathResults.put("longest_paths", longestPaths);

            results.put("paths", pathResults);

            // Performance metrics
            Map<String, Object> performance = new LinkedHashMap<>();
            performance.put("operations", metrics.getOperationCount());
            performance.put("time_nanos", metrics.getTimeNanos());
            results.put("performance", performance);

            // Write JSON with pretty printing
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            try (FileWriter writer = new FileWriter(filePath)) {
                gson.toJson(results, writer);
            }

        } catch (IOException e) {
            System.err.println("Error exporting full results to JSON: " + e.getMessage());
        }
    }

    static List<Integer> reconstructPath(int[] predecessors, int endNode) {
        List<Integer> path = new ArrayList<>();
        for (int at = endNode; at != -1; at = predecessors[at]) {
            path.add(at);
        }
        Collections.reverse(path);
        return path;
    }

    private static List<List<int[]>> createWeightedCondensation(
            List<List<Integer>> condensation,
            List<List<Integer>> components,
            List<GraphData.Edge> edges) {

        List<List<int[]>> weighted = new ArrayList<>();
        for (int i = 0; i < condensation.size(); i++) {
            weighted.add(new ArrayList<>());
        }

        // Map original nodes to their component IDs
        int n = components.stream().mapToInt(List::size).sum();
        int[] compId = new int[n];
        for (int i = 0; i < components.size(); i++) {
            for (int node : components.get(i)) {
                compId[node] = i;
            }
        }

        // Create weighted edges between components
        // Use maximum weight of any edge between components
        Map<String, Integer> compWeights = new HashMap<>();
        for (GraphData.Edge edge : edges) {
            int compU = compId[edge.u];
            int compV = compId[edge.v];
            if (compU != compV) {
                String key = compU + "->" + compV;
                compWeights.put(key, Math.max(compWeights.getOrDefault(key, 0), edge.w));
            }
        }

        // Build weighted condensation graph
        for (int u = 0; u < condensation.size(); u++) {
            for (int v : condensation.get(u)) {
                String key = u + "->" + v;
                int weight = compWeights.getOrDefault(key, 1);
                weighted.get(u).add(new int[]{v, weight});
            }
        }

        return weighted;
    }
}