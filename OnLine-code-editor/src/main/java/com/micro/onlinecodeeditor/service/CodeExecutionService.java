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

    private static final long EXECUTION_TIMEOUT_SECONDS = 15;

    public CodeResponse executeCode(CodeRequest request) {

        if (!"java".equalsIgnoreCase(request.getLanguage())) {

            return new CodeResponse(
                    false,
                    "",
                    "Currently only Java is supported."
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

            // Run Docker
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
                    "cd /app && javac Main.java && java Main"
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
                        "Execution timed out"
                );
            }

            /*
             * Docker finished normally.
             */
            String output =
                    outputFuture.get(
                            1,
                            TimeUnit.SECONDS
                    );

            int exitCode =
                    process.exitValue();

            /*
             * Compilation or runtime error.
             */
            if (exitCode != 0) {

                return new CodeResponse(
                        false,
                        "",
                        output
                );
            }

            /*
             * Successful execution.
             */
            return new CodeResponse(
                    true,
                    output,
                    null
            );

        } catch (Exception e) {

            return new CodeResponse(
                    false,
                    "",
                    e.getMessage()
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
}
