package com.micro.onlinecodeeditor.dto;

public class CodeResponse {

    private boolean success;
    private String output;
    private String error;

    public CodeResponse() {
    }

    public CodeResponse(boolean success, String output, String error) {
        this.success = success;
        this.output = output;
        this.error = error;
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
}