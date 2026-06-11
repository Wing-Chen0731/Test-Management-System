package com.test.service;

import com.test.entity.TestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("TestCase集成测试")
class TestCaseIntegrationTest {

    @Autowired
    private TestCaseService testCaseService;

    private TestCase testCase;

    @BeforeEach
    void setUp() {
        testCaseService.deleteAllTestCases();
        
        testCase = new TestCase();
        testCase.setTitle("用户登录测试");
        testCase.setModule("用户管理");
        testCase.setPriority("高");
        testCase.setPrecondition("用户已注册");
        testCase.setSteps("1. 输入用户名\n2. 输入密码\n3. 点击登录按钮");
        testCase.setExpectedResult("登录成功，跳转到首页");
        testCase.setStatus("待执行");
        testCase.setCreator("测试工程师");
    }

    @Test
    @DisplayName("创建测试用例 - 成功")
    void createTestCase_Success() {
        TestCase created = testCaseService.createTestCase(testCase);
        
        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals("用户登录测试", created.getTitle());
        assertEquals("用户管理", created.getModule());
        assertEquals("高", created.getPriority());
        assertEquals("待执行", created.getStatus());
        assertNotNull(created.getCreateTime());
        assertNotNull(created.getUpdateTime());
    }

    @Test
    @DisplayName("根据ID查询测试用例 - 成功")
    void getTestCaseById_Success() {
        TestCase created = testCaseService.createTestCase(testCase);
        
        assertNotNull(created);
        TestCase found = testCaseService.getTestCaseById(created.getId());
        
        assertNotNull(found);
        assertEquals(created.getId(), found.getId());
        assertEquals(created.getTitle(), found.getTitle());
        assertEquals(created.getModule(), found.getModule());
    }

    @Test
    @DisplayName("根据ID查询测试用例 - 不存在")
    void getTestCaseById_NotFound() {
        TestCase found = testCaseService.getTestCaseById(999999L);
        
        assertNull(found);
    }

    @Test
    @DisplayName("更新测试用例 - 成功")
    void updateTestCase_Success() {
        TestCase created = testCaseService.createTestCase(testCase);
        
        assertNotNull(created);
        
        TestCase updateCase = new TestCase();
        updateCase.setTitle("用户登录测试（已更新）");
        updateCase.setStatus("执行中");
        updateCase.setPriority("中");
        
        TestCase updated = testCaseService.updateTestCase(created.getId(), updateCase);
        
        assertNotNull(updated);
        assertEquals("用户登录测试（已更新）", updated.getTitle());
        assertEquals("执行中", updated.getStatus());
        assertEquals("中", updated.getPriority());
        assertEquals("用户管理", updated.getModule());
        assertEquals("用户已注册", updated.getPrecondition());
    }

    @Test
    @DisplayName("删除测试用例 - 成功")
    void deleteTestCase_Success() {
        TestCase created = testCaseService.createTestCase(testCase);
        
        assertNotNull(created);
        boolean deleted = testCaseService.deleteTestCase(created.getId());
        
        assertTrue(deleted);
        
        TestCase found = testCaseService.getTestCaseById(created.getId());
        assertNull(found);
    }

    @Test
    @DisplayName("查询所有测试用例 - 成功")
    void listAllTestCases_Success() {
        for (int i = 1; i <= 15; i++) {
            TestCase tc = new TestCase();
            tc.setTitle("测试用例" + i);
            tc.setModule("模块" + (i % 3 + 1));
            tc.setPriority(i % 2 == 0 ? "高" : "中");
            tc.setPrecondition("前置条件" + i);
            tc.setSteps("测试步骤" + i);
            tc.setExpectedResult("预期结果" + i);
            tc.setStatus("待执行");
            tc.setCreator("测试工程师");
            testCaseService.createTestCase(tc);
        }

        List<TestCase> result = testCaseService.getAllTestCases();

        assertNotNull(result);
        assertEquals(15, result.size());
    }

    @Test
    @DisplayName("创建多个测试用例并验证")
    void createMultipleTestCases_Success() {
        TestCase tc1 = new TestCase();
        tc1.setTitle("用户注册测试");
        tc1.setModule("用户管理");
        tc1.setPriority("高");
        tc1.setPrecondition("用户未注册");
        tc1.setSteps("注册步骤");
        tc1.setExpectedResult("注册成功");
        tc1.setStatus("待执行");
        tc1.setCreator("测试工程师1");

        TestCase tc2 = new TestCase();
        tc2.setTitle("密码修改测试");
        tc2.setModule("用户管理");
        tc2.setPriority("中");
        tc2.setPrecondition("用户已登录");
        tc2.setSteps("修改密码步骤");
        tc2.setExpectedResult("密码修改成功");
        tc2.setStatus("待执行");
        tc2.setCreator("测试工程师2");

        testCaseService.createTestCase(tc1);
        testCaseService.createTestCase(tc2);

        List<TestCase> result = testCaseService.getAllTestCases();

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(tc -> "用户注册测试".equals(tc.getTitle())));
        assertTrue(result.stream().anyMatch(tc -> "密码修改测试".equals(tc.getTitle())));
    }

    @Test
    @DisplayName("验证自动填充时间字段")
    void verifyAutoFillTimeFields() {
        TestCase created = testCaseService.createTestCase(testCase);
        
        assertNotNull(created);
        assertNotNull(created.getCreateTime());
        assertNotNull(created.getUpdateTime());
        
        LocalDateTime createTime = created.getCreateTime();
        LocalDateTime updateTime = created.getUpdateTime();
        
        assertTrue(createTime.isBefore(LocalDateTime.now().plusMinutes(1)));
        assertTrue(updateTime.isBefore(LocalDateTime.now().plusMinutes(1)));
    }

    @Test
    @DisplayName("更新后验证updateTime字段")
    void verifyUpdateTimeAfterUpdate() {
        TestCase created = testCaseService.createTestCase(testCase);
        
        assertNotNull(created);
        LocalDateTime originalUpdateTime = created.getUpdateTime();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        TestCase updateCase = new TestCase();
        updateCase.setTitle("更新后的标题");
        testCaseService.updateTestCase(created.getId(), updateCase);

        TestCase updated = testCaseService.getTestCaseById(created.getId());
        assertNotNull(updated.getUpdateTime());
        assertTrue(updated.getUpdateTime().isAfter(originalUpdateTime));
    }

    @Test
    @DisplayName("搜索测试用例 - 成功")
    void searchTestCases_Success() {
        testCaseService.createTestCase(testCase);
        
        List<TestCase> result = testCaseService.searchTestCases("登录");
        
        assertNotNull(result);
        assertTrue(result.size() >= 1);
        assertTrue(result.stream().anyMatch(tc -> tc.getTitle().contains("登录")));
    }
}
