package graph;

import java.io.*;
import java.util.*;

public class BatchProcessor {
    public static void main(String[] args) {
        System.out.println("=== Smart City Scheduling - Batch Processor ===");

        // Create necessary directories
        createDirectories();

        // Generate datasets if they don't exist
        if (!datasetsExist()) {
            System.out.println("Generating datasets...");
            DatasetGenerator.main(args);
        }

        // Process all datasets
        processAllDatasets();

        System.out.println("=== Batch processing completed! ===");
    }

    private static void createDirectories() {
        new File("data").mkdirs();
        new File("results/csv").mkdirs();
        new File("results/json").mkdirs();
    }

    private static boolean datasetsExist() {
        String[] expectedFiles = {
                "small_cycle.json", "small_dag.json", "small_mixed.json",
                "medium_complex_dag.json", "medium_mixed.json", "medium_multiple_scc.json",
                "large_sparse.json", "large_medium.json", "large_complex_scc.json"
        };

        for (String filename : expectedFiles) {
            if (!new File("data/" + filename).exists()) {
                return false;
            }
        }
        return true;
    }

    private static void processAllDatasets() {
        String[] datasets = {
                "small_cycle", "small_dag", "small_mixed",
                "medium_complex_dag", "medium_mixed", "medium_multiple_scc",
                "large_sparse", "large_medium", "large_complex_scc"
        };

        for (String dataset : datasets) {
            System.out.println("Processing: " + dataset);
            try {
                Main.main(new String[]{"data/" + dataset + ".json"});
            } catch (Exception e) {
                System.err.println("Error processing " + dataset + ": " + e.getMessage());
            }
        }
    }
}