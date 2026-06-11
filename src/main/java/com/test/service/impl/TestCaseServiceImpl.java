package com.test.service.impl;

import com.test.entity.TestCase;
import com.test.mapper.TestCaseMapper;
import com.test.service.TestCaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TestCaseServiceImpl implements TestCaseService {

    private final TestCaseMapper testCaseMapper;

    @Autowired
    public TestCaseServiceImpl(TestCaseMapper testCaseMapper) {
        this.testCaseMapper = testCaseMapper;
    }

    @Override
    public TestCase createTestCase(TestCase testCase) {
        LocalDateTime now = LocalDateTime.now();
        if (testCase.getStatus() == null || testCase.getStatus().isBlank()) {
            testCase.setStatus("DRAFT");
        }
        testCase.setCreateTime(now);
        testCase.setUpdateTime(now);
        return testCaseMapper.insert(testCase) > 0 ? testCase : null;
    }

    @Override
    public TestCase updateTestCase(Long id, TestCase testCase) {
        TestCase existing = testCaseMapper.selectById(id);
        if (existing == null) {
            return null;
        }
        mergeForUpdate(existing, testCase);
        existing.setUpdateTime(LocalDateTime.now());
        testCaseMapper.updateById(existing);
        return testCaseMapper.selectById(id);
    }

    @Override
    public boolean deleteTestCase(Long id) {
        TestCase existing = testCaseMapper.selectById(id);
        if (existing == null) {
            return false;
        }
        return testCaseMapper.deleteById(id) > 0;
    }

    @Override
    public TestCase getTestCaseById(Long id) {
        return testCaseMapper.selectById(id);
    }

    @Override
    public List<TestCase> getAllTestCases() {
        return testCaseMapper.selectList();
    }

    @Override
    public List<TestCase> searchTestCases(String keyword) {
        return testCaseMapper.search(keyword);
    }

    @Override
    public void deleteAllTestCases() {
        testCaseMapper.deleteAll();
    }

    private void mergeForUpdate(TestCase existing, TestCase update) {
        existing.setTitle(update.getTitle());
        if (update.getModule() != null) {
            existing.setModule(update.getModule());
        }
        if (update.getPriority() != null) {
            existing.setPriority(update.getPriority());
        }
        if (update.getPrecondition() != null) {
            existing.setPrecondition(update.getPrecondition());
        }
        if (update.getSteps() != null) {
            existing.setSteps(update.getSteps());
        }
        if (update.getExpectedResult() != null) {
            existing.setExpectedResult(update.getExpectedResult());
        }
        if (update.getStatus() != null) {
            existing.setStatus(update.getStatus());
        }
        if (update.getCreator() != null) {
            existing.setCreator(update.getCreator());
        }
    }
}
