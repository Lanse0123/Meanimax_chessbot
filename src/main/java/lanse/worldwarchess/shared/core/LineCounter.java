package lanse.worldwarchess.shared.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class LineCounter {

    public static void main() {
        countAllLines();
    }

    public static void countAllLines() {
        String path = ".";
        File root = new File(path);

        if (!root.exists() || !root.isDirectory()) {
            System.out.println("Invalid directory.");
            return;
        }

        long javaLines = countLinesByExtension(root, ".java");
        long jsonLines = countLinesByExtension(root, ".json");

        System.out.printf("Total Java lines: %,d%n", javaLines);
        System.out.printf("Total JSON lines: %,d%n", jsonLines);
        System.out.printf("Combined total: %,d%n", javaLines + jsonLines);
    }

    public static long countLinesByExtension(File dir, String extension) {
        long lines = 0;

        File[] files = dir.listFiles();
        if (files == null) return 0;

        for (File file : files) {
            if (file.isDirectory()) {
                lines += countLinesByExtension(file, extension);
            } else if (file.getName().endsWith(extension)) {
                lines += countLinesInFile(file);
            }
        }
        return lines;
    }

    public static long countLinesInFile(File file) {
        long count = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            while (reader.readLine() != null) {
                count++;
            }
        } catch (IOException e) {
            System.out.println("Failed to read: " + file.getAbsolutePath());
        }

        return count;
    }
}