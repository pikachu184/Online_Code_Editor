package com.micro.onlinecodeeditor.dto;

public class CodeResponse {

    private boolean success;
    private String output;
    private String error;
    private String memoryUsed;
    private String executionTime;

    public CodeResponse() {
    }

    // Existing constructor - keep it for compatibility
    public CodeResponse(boolean success, String output, String error) {
        this(success, output, error, null, null);
    }

    // Existing constructor with memory usage - keep it for compatibility
    public CodeResponse(
            boolean success,
            String output,
            String error,
            String memoryUsed
    ) {
        this(success, output, error, memoryUsed, null);
    }

    // New constructor with memory usage and execution time
    public CodeResponse(
            boolean success,
            String output,
            String error,
            String memoryUsed,
            String executionTime
    ) {
        this.success = success;
        this.output = output;
        this.error = error;
        this.memoryUsed = memoryUsed;
        this.executionTime = executionTime;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getOutput() {
        return output;
    }

    public String getError() {
        return error;
    }

    public String getMemoryUsed() {
        return memoryUsed;
    }

    public String getExecutionTime() {
        return executionTime;
    }
}
