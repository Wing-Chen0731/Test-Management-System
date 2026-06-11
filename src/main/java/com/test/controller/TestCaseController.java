package com.test.controller;

import com.test.common.Result;
import com.test.entity.TestCase;
import com.test.service.TestCaseService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/testcases")
public class TestCaseController {

    @Autowired
    private TestCaseService testCaseService;

    @PostMapping
    public Result<TestCase> create(@Valid @RequestBody TestCase testCase) {
        TestCase created = testCaseService.createTestCase(testCase);
        return created != null ? Result.success(created) : Result.error("创建失败");
    }

    @PutMapping("/{id}")
    public Result<TestCase> update(@PathVariable Long id, @Valid @RequestBody TestCase testCase) {
        TestCase updated = testCaseService.updateTestCase(id, testCase);
        return updated != null ? Result.success(updated) : Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        boolean success = testCaseService.deleteTestCase(id);
        return success ? Result.success() : Result.error("删除失败");
    }

    @GetMapping("/{id}")
    public Result<TestCase> getById(@PathVariable Long id) {
        TestCase testCase = testCaseService.getTestCaseById(id);
        return testCase != null ? Result.success(testCase) : Result.error("未找到数据");
    }

    @GetMapping
    public Result<List<TestCase>> list(@RequestParam(required = false) String keyword) {
        List<TestCase> testCases;
        if (keyword != null && !keyword.isBlank()) {
            testCases = testCaseService.searchTestCases(keyword.trim());
        } else {
            testCases = testCaseService.getAllTestCases();
        }
        return Result.success(testCases);
    }
}
