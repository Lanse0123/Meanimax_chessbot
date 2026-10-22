package lanse.worldwarchess.shared.utils;

public class LagTester {
    private String label;
    private long startTime;
    private boolean running;

    public LagTester() {}

    public void start(String label) {
        this.label = label;
        this.startTime = System.nanoTime();
        this.running = true;
    }

    public void stop() {
        if (!running) {
            System.out.println(label + " is not running.");
            return;
        }
        long endTime = System.nanoTime();
        long durationNs = endTime - startTime;

        double ms = durationNs / 1000000.0;
        double seconds = ms / 1000.0;

        System.out.println("[" + label + "] Elapsed: " + ms + " (" + seconds + ")");
        running = false;
    }
}
