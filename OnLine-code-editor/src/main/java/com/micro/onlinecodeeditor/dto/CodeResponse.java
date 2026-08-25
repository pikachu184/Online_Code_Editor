package com.micro.onlinecodeeditor.dto;

public class CodeResponse {

    private boolean success;
    private String output;
    private String error;
    private String memoryUsed;

    public CodeResponse() {
    }

    // Existing constructor - keep it for compatibility
    public CodeResponse(boolean success, String output, String error) {
        this(success, output, error, null);
    }

    // New constructor with memory usage
    public CodeResponse(
            boolean success,
            String output,
            String error,
            String memoryUsed
    ) {
        this.success = success;
        this.output = output;
        this.error = error;
        this.memoryUsed = memoryUsed;
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
}
