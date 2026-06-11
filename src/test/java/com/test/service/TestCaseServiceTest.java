package com.test.service;

import com.test.entity.TestCase;
import com.test.mapper.TestCaseMapper;
import com.test.service.impl.TestCaseServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TestCaseService单元测试")
class TestCaseServiceTest {

    @Mock
    private TestCaseMapper testCaseMapper;

    @InjectMocks
    private TestCaseServiceImpl testCaseService;

    private TestCase testCase;

    @BeforeEach
    void setUp() {
        testCase = new TestCase();
        testCase.setId(1L);
        testCase.setTitle("用户登录测试");
        testCase.setModule("用户管理");
        testCase.setPriority("高");
        testCase.setPrecondition("用户已注册");
        testCase.setSteps("1. 输入用户名\n2. 输入密码\n3. 点击登录按钮");
        testCase.setExpectedResult("登录成功，跳转到首页");
        testCase.setStatus("待执行");
        testCase.setCreator("测试工程师");
        testCase.setCreateTime(LocalDateTime.now());
        testCase.setUpdateTime(LocalDateTime.now());
    }

    @Test
    @DisplayName("正常场景 - 创建测试用例成功")
    void createTestCase_Success() {
        when(testCaseMapper.insert(any(TestCase.class))).thenReturn(1);

        TestCase result = testCaseService.createTestCase(testCase);

        assertNotNull(result);
        verify(testCaseMapper, times(1)).insert(any(TestCase.class));
    }

    @Test
    @DisplayName("正常场景 - 根据ID查询测试用例成功")
    void getTestCaseById_Success() {
        when(testCaseMapper.selectById(1L)).thenReturn(testCase);

        TestCase result = testCaseService.getTestCaseById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("用户登录测试", result.getTitle());
        verify(testCaseMapper, times(1)).selectById(1L);
    }

    @Test
    @DisplayName("正常场景 - 根据ID查询测试用例不存在")
    void getTestCaseById_NotFound() {
        when(testCaseMapper.selectById(999L)).thenReturn(null);

        TestCase result = testCaseService.getTestCaseById(999L);

        assertNull(result);
        verify(testCaseMapper, times(1)).selectById(999L);
    }

    @Test
    @DisplayName("正常场景 - 更新测试用例成功")
    void updateTestCase_Success() {
        when(testCaseMapper.selectById(1L)).thenReturn(testCase);
        when(testCaseMapper.updateById(any(TestCase.class))).thenReturn(1);
        when(testCaseMapper.selectById(1L)).thenReturn(testCase);

        TestCase updatedCase = new TestCase();
        updatedCase.setTitle("用户登录测试（已更新）");
        
        TestCase result = testCaseService.updateTestCase(1L, updatedCase);

        assertNotNull(result);
        ArgumentCaptor<TestCase> captor = ArgumentCaptor.forClass(TestCase.class);
        verify(testCaseMapper).updateById(captor.capture());
        assertEquals("用户登录测试（已更新）", captor.getValue().getTitle());
        assertEquals("用户管理", captor.getValue().getModule());
        assertEquals("待执行", captor.getValue().getStatus());
    }

    @Test
    @DisplayName("正常场景 - 更新测试用例不存在")
    void updateTestCase_NotFound() {
        when(testCaseMapper.selectById(999L)).thenReturn(null);

        TestCase updatedCase = new TestCase();
        updatedCase.setTitle("用户登录测试（已更新）");
        
        TestCase result = testCaseService.updateTestCase(999L, updatedCase);

        assertNull(result);
        verify(testCaseMapper, never()).updateById(any(TestCase.class));
    }

    @Test
    @DisplayName("正常场景 - 删除测试用例成功")
    void deleteTestCase_Success() {
        when(testCaseMapper.selectById(1L)).thenReturn(testCase);
        when(testCaseMapper.deleteById(1L)).thenReturn(1);

        boolean result = testCaseService.deleteTestCase(1L);

        assertTrue(result);
        verify(testCaseMapper, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("正常场景 - 删除测试用例不存在")
    void deleteTestCase_NotFound() {
        when(testCaseMapper.selectById(999L)).thenReturn(null);

        boolean result = testCaseService.deleteTestCase(999L);

        assertFalse(result);
        verify(testCaseMapper, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("正常场景 - 查询所有测试用例成功")
    void listAllTestCases_Success() {
        List<TestCase> testCases = Arrays.asList(testCase);
        when(testCaseMapper.selectList()).thenReturn(testCases);

        List<TestCase> result = testCaseService.getAllTestCases();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("用户登录测试", result.get(0).getTitle());
        verify(testCaseMapper, times(1)).selectList();
    }

    @Test
    @DisplayName("正常场景 - 搜索测试用例成功")
    void searchTestCases_Success() {
        List<TestCase> testCases = Arrays.asList(testCase);
        when(testCaseMapper.search("登录")).thenReturn(testCases);

        List<TestCase> result = testCaseService.searchTestCases("登录");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(testCaseMapper, times(1)).search("登录");
    }

    @Test
    @DisplayName("异常场景 - 创建测试用例失败")
    void createTestCase_Failure() {
        when(testCaseMapper.insert(any(TestCase.class))).thenReturn(0);

        TestCase result = testCaseService.createTestCase(testCase);

        assertNull(result);
        verify(testCaseMapper, times(1)).insert(any(TestCase.class));
    }

    @Test
    @DisplayName("异常场景 - 查询测试用例时数据库异常")
    void getTestCaseById_DatabaseException() {
        when(testCaseMapper.selectById(1L)).thenThrow(new RuntimeException("数据库连接异常"));

        assertThrows(RuntimeException.class, () -> {
            testCaseService.getTestCaseById(1L);
        });

        verify(testCaseMapper, times(1)).selectById(1L);
    }

    @Test
    @DisplayName("异常场景 - 创建测试用例时数据库异常")
    void createTestCase_DatabaseException() {
        when(testCaseMapper.insert(any(TestCase.class))).thenThrow(new RuntimeException("数据库连接异常"));

        assertThrows(RuntimeException.class, () -> {
            testCaseService.createTestCase(testCase);
        });

        verify(testCaseMapper, times(1)).insert(any(TestCase.class));
    }

    @Test
    @DisplayName("边界场景 - 查询不存在的ID")
    void getTestCaseByNonExistentId() {
        when(testCaseMapper.selectById(-1L)).thenReturn(null);

        TestCase result = testCaseService.getTestCaseById(-1L);

        assertNull(result);
        verify(testCaseMapper, times(1)).selectById(-1L);
    }

    @Test
    @DisplayName("边界场景 - 查询空列表")
    void listTestCases_EmptyResult() {
        when(testCaseMapper.selectList()).thenReturn(Arrays.asList());

        List<TestCase> result = testCaseService.getAllTestCases();

        assertNotNull(result);
        assertEquals(0, result.size());
        verify(testCaseMapper, times(1)).selectList();
    }
}
