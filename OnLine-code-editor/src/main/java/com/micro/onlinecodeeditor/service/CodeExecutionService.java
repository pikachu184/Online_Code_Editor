package com.micro.onlinecodeeditor.service;

import com.micro.onlinecodeeditor.dto.CodeRequest;
import com.micro.onlinecodeeditor.dto.CodeResponse;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class CodeExecutionService {

    private static final long EXECUTION_TIMEOUT_SECONDS = 90;

    private static final String MEMORY_MARKER =
            "__MEMORY_PEAK__=";

    public CodeResponse executeCode(CodeRequest request) {

        if (!"java".equalsIgnoreCase(request.getLanguage())) {

            return new CodeResponse(
                    false,
                    "",
                    "Currently only Java is supported.",
                    null,
                    null
            );
        }

        Path tempDirectory = null;

        String containerName =
                "code-runner-" + System.nanoTime();

        try {

            // Create temporary directory
            tempDirectory =
                    Files.createTempDirectory("code-runner-");

            // Create Main.java
            File javaFile =
                    tempDirectory.resolve("Main.java").toFile();

            try (FileWriter writer =
                         new FileWriter(javaFile)) {

                writer.write(request.getCode());
            }

            /*
             * Start measuring the execution time
             * immediately before starting Docker.
             */
            long startTime = System.nanoTime();

            /*
             * Run Docker.
             *
             * Existing security and resource settings
             * remain unchanged.
             */
            Process process = new ProcessBuilder(
                    "docker",
                    "run",
                    "--name", containerName,
                    "--rm",
                    "--network", "none",
                    "--memory", "128m",
                    "--cpus", "0.5",
                    "-v",
                    tempDirectory.toAbsolutePath()
                            + ":/app",
                    "eclipse-temurin:21-jdk",
                    "sh",
                    "-c",
                    "cd /app && " +
                    "(javac Main.java && java Main); " +
                    "exit_code=$?; " +
                    "memory_peak=$(cat /sys/fs/cgroup/memory.peak 2>/dev/null || echo 0); " +
                    "echo '" + MEMORY_MARKER + "'$memory_peak; " +
                    "exit $exit_code"
            )
                    .redirectErrorStream(true)
                    .start();

            /*
             * Read Docker output asynchronously.
             * This prevents output reading from blocking
             * the timeout mechanism.
             */
            CompletableFuture<String> outputFuture =
                    CompletableFuture.supplyAsync(() -> {

                        try {

                            return new String(
                                    process.getInputStream()
                                            .readAllBytes()
                            );

                        } catch (Exception e) {

                            return "";
                        }
                    });

            /*
             * Wait maximum 15 seconds.
             */
            boolean finished =
                    process.waitFor(
                            EXECUTION_TIMEOUT_SECONDS,
                            TimeUnit.SECONDS
                    );

            /*
             * Execution timeout.
             */
            if (!finished) {

                try {

                    Process stopProcess =
                            new ProcessBuilder(
                                    "docker",
                                    "stop",
                                    "--time", "1",
                                    containerName
                            ).start();

                    stopProcess.waitFor(
                            3,
                            TimeUnit.SECONDS
                    );

                } catch (Exception ignored) {
                }

                process.destroyForcibly();

                return new CodeResponse(
                        false,
                        "",
                        "Execution timed out",
                        null,
                        ">90 sec"
                );
            }

            /*
             * Docker finished normally.
             */
            long endTime = System.nanoTime();

            double executionSeconds =
                    (endTime - startTime) / 1_000_000_000.0;

            String executionTime =
                    String.format(
                            "%.3f sec",
                            executionSeconds
                    );

            String rawOutput =
                    outputFuture.get(
                            1,
                            TimeUnit.SECONDS
                    );

            /*
             * Extract memory usage from Docker output.
             */
            String memoryUsed =
                    extractMemoryUsage(rawOutput);

            /*
             * Remove memory marker from actual program output.
             */
            String output =
                    removeMemoryMarker(rawOutput);

            int exitCode =
                    process.exitValue();

            /*
             * Compilation or runtime error.
             */
            if (exitCode != 0) {

                return new CodeResponse(
                        false,
                        "",
                        output,
                        memoryUsed,
                        executionTime
                );
            }

            /*
             * Successful execution.
             */
            return new CodeResponse(
                    true,
                    output,
                    null,
                    memoryUsed,
                    executionTime
            );

        } catch (Exception e) {

            return new CodeResponse(
                    false,
                    "",
                    e.getMessage(),
                    null,
                    null
            );

        } finally {

            /*
             * Delete temporary files.
             */
            if (tempDirectory != null) {

                try {

                    Files.walk(tempDirectory)
                            .sorted(
                                    (a, b) ->
                                            b.compareTo(a)
                            )
                            .forEach(path -> {

                                try {

                                    Files.deleteIfExists(path);

                                } catch (Exception ignored) {
                                }
                            });

                } catch (Exception ignored) {
                }
            }
        }
    }

    /*
     * Extract peak memory from the special marker.
     */
    private String extractMemoryUsage(String output) {

        if (output == null) {
            return null;
        }

        int markerIndex =
                output.lastIndexOf(MEMORY_MARKER);

        if (markerIndex == -1) {
            return null;
        }

        String memoryValue =
                output.substring(
                        markerIndex + MEMORY_MARKER.length()
                ).trim();

        try {

            long memoryBytes =
                    Long.parseLong(memoryValue);

            long memoryMb =
                    (memoryBytes + (1024 * 1024 - 1))
                            / (1024 * 1024);

            return memoryMb + " MB";

        } catch (NumberFormatException e) {

            return null;
        }
    }

    /*
     * Remove the memory marker from the user's
     * actual program output.
     */
    private String removeMemoryMarker(String output) {

        if (output == null) {
            return "";
        }

        int markerIndex =
                output.lastIndexOf(MEMORY_MARKER);

        if (markerIndex == -1) {
            return output;
        }

        return output
                .substring(0, markerIndex)
                .trim();
    }
}
