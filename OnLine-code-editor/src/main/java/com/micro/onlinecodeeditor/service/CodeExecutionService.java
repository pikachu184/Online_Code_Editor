package com.micro.onlinecodeeditor.service;

import com.micro.onlinecodeeditor.dto.CodeRequest;
import com.micro.onlinecodeeditor.dto.CodeResponse;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class CodeExecutionService {

    public CodeResponse executeCode(CodeRequest request) {

        if (!"java".equalsIgnoreCase(request.getLanguage())) {

            return new CodeResponse(
                    false,
                    "",
                    "Currently only Java is supported."
            );
        }

        Path tempDirectory = null;

        try {

            // Create temporary directory
            tempDirectory = Files.createTempDirectory("code-runner-");

            // Create Main.java
            File javaFile =
                    tempDirectory.resolve("Main.java").toFile();

            try (FileWriter writer = new FileWriter(javaFile)) {
                writer.write(request.getCode());
            }

            // Run Docker
            Process process = new ProcessBuilder(
                    "docker",
                    "run",
                    "--rm",
                    "--network", "none",
                    "--memory", "128m",
                    "--cpus", "0.5",
                    "-v",
                    tempDirectory.toAbsolutePath() + ":/app",
                    "eclipse-temurin:21-jdk",
                    "sh",
                    "-c",
                    "cd /app && javac Main.java && java Main"
            )
                    .redirectErrorStream(true)
                    .start();

            String output =
                    new String(
                            process.getInputStream().readAllBytes()
                    );

            int exitCode = process.waitFor();

            if (exitCode != 0) {

                return new CodeResponse(
                        false,
                        "",
                        output
                );
            }

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

            // Delete temporary files
            if (tempDirectory != null) {

                try {

                    Files.walk(tempDirectory)
                            .sorted((a, b) ->
                                    b.compareTo(a))
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