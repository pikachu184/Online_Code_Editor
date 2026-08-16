package com.micro.onlinecodeeditor.controller;

import com.micro.onlinecodeeditor.dto.CodeRequest;
import com.micro.onlinecodeeditor.dto.CodeResponse;
import com.micro.onlinecodeeditor.service.CodeExecutionService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/code")
public class CodeController {

    private final CodeExecutionService codeExecutionService;

    public CodeController(CodeExecutionService codeExecutionService) {
        this.codeExecutionService = codeExecutionService;
    }

    @PostMapping("/run")
    public CodeResponse runCode(@RequestBody CodeRequest request) {
        return codeExecutionService.executeCode(request);
    }
}