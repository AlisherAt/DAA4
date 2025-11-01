package graph;

public class BasicMetrics implements Metrics {
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