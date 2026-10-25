package lanse.worldwarchess.shared.core.thread;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/** Written by Lanse, taken from Bonk Universe
 * <br><br>
 * This class is the best way to test for lag across the project. Set up which threads to monitor in the ThreadNames class,
 * and this will continuously ask where that thread is. The thread will give a stack trace, which is then recorded. When you
 * use the FlameGraph Show or Stop command, it will display a graph which shows the areas the code is in the most. The bigger
 * the area, the more often the code was there, meaning that part is one of the laggiest parts. **/
public class FlameGraphLagTester {

    private static volatile boolean running = false;
    private static final long SAMPLE_INTERVAL_MS = 5;
    public static final Set<String> INCLUDED_THREADS = new CopyOnWriteArraySet<>();
    private static final Map<String, Integer> flameSamples = new ConcurrentHashMap<>();

    public static void start() {
        if (running) return;
        running = true;

        Thread samplerThread = new Thread(() -> {
            while (running) {
                sampleThreads();

                try {
                    Thread.sleep(SAMPLE_INTERVAL_MS);
                } catch (InterruptedException ignored) {}
            }
        }, ThreadNames.FLAME_GRAPH);

        samplerThread.setDaemon(true);
        samplerThread.start();
    }

    public static void stop() {
        running = false;
        show();
    }

    private static void sampleThreads() {
        Map<Thread, StackTraceElement[]> all = Thread.getAllStackTraces();

        for (Map.Entry<Thread, StackTraceElement[]> entry : all.entrySet()) {
            Thread thread = entry.getKey();

            if (INCLUDED_THREADS.stream().noneMatch(thread.getName()::contains)){
                continue;
            }

            StackTraceElement[] stack = entry.getValue();

            if (stack.length == 0) continue;

            StringBuilder builder = new StringBuilder();
            boolean hasRealWork = false;

            for (int i = stack.length - 1; i >= 0; i--) {
                StackTraceElement e = stack[i];

                String clazz = e.getClassName();
                String method = e.getMethodName();

                boolean isIdle = (clazz.equals("java.lang.Thread") && (method.equals("sleep") || method.equals("sleepNanos")
                        || method.equals("sleepNanos0"))) || (clazz.equals("java.lang.Object") && method.equals("wait")) ||
                        method.equals("park") || clazz.startsWith("java.util.concurrent.locks") ||
                        clazz.startsWith("sun.misc.Unsafe") || clazz.startsWith("jdk.internal.misc.Unsafe") ||
                        clazz.contains("ForkJoinPool") || clazz.startsWith("java.lang.ref.Reference") ||
                        method.equals("getTask") || (clazz.endsWith("DelayedWorkQueue") && method.equals("take") ||
                        clazz.equals("java.awt.EventQueue") || clazz.equals("java.util.concurrent.LinkedBlockingQueue") ||
                        method.contains("dispatchEvent") || clazz.equals("javax.swing.Timer$DoPostEvent") ||
                        clazz.contains("DefaultKeyboardFocusManager") || method.contains("fireActionPerformed"));

                if (!isIdle) {
                    hasRealWork = true;
                }

                if (isIdle) continue;

                builder.append(clazz).append(".").append(method).append(";");
            }

            if (!hasRealWork || builder.isEmpty()) {
                continue;
            }

            builder.setLength(builder.length() - 1);

            String key = builder.toString();
            flameSamples.merge(key, 1, Integer::sum);
        }
    }

    private static Map<String, Integer> getFlameSamples() {
        return new HashMap<>(flameSamples);
    }

    public static void show() {
        Map<String, Integer> data = getFlameSamples();
        FlameGraphPanel.createFlameGraphWindow(data);
    }

    public static void toggle() {
        if (running){
            stop();
        } else {
            flameSamples.clear();
            start();
        }
    }
}