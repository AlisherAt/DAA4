package graph;

public interface Metrics {
    void reset();
    long getOperationCount();
    long getTimeNanos();
    void incrementOperationCount();
}