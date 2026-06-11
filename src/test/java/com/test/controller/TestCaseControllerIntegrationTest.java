package com.test.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.entity.TestCase;
import com.test.service.TestCaseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("TestCaseController集成测试")
class TestCaseControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
    @DisplayName("POST /api/testcases - 创建测试用例成功")
    void createTestCase_Success() throws Exception {
        mockMvc.perform(post("/api/testcases")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCase)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("操作成功"))
                .andExpect(jsonPath("$.data.title").value("用户登录测试"))
                .andExpect(jsonPath("$.data.module").value("用户管理"))
                .andExpect(jsonPath("$.data.priority").value("高"))
                .andExpect(jsonPath("$.data.status").value("待执行"))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.createTime").exists())
                .andExpect(jsonPath("$.data.updateTime").exists());
    }

    @Test
    @DisplayName("GET /api/testcases/{id} - 根据ID查询测试用例成功")
    void getTestCaseById_Success() throws Exception {
        String response = mockMvc.perform(post("/api/testcases")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCase)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long id = objectMapper.readTree(response).get("data").get("id").asLong();

        mockMvc.perform(get("/api/testcases/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("操作成功"))
                .andExpect(jsonPath("$.data.id").value(id))
                .andExpect(jsonPath("$.data.title").value("用户登录测试"));
    }

    @Test
    @DisplayName("GET /api/testcases/{id} - 查询不存在的测试用例")
    void getTestCaseById_NotFound() throws Exception {
        mockMvc.perform(get("/api/testcases/999999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("未找到数据"));
    }

    @Test
    @DisplayName("PUT /api/testcases/{id} - 更新测试用例成功")
    void updateTestCase_Success() throws Exception {
        String response = mockMvc.perform(post("/api/testcases")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCase)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long id = objectMapper.readTree(response).get("data").get("id").asLong();

        TestCase updateCase = new TestCase();
        updateCase.setTitle("用户登录测试（已更新）");
        updateCase.setStatus("执行中");
        updateCase.setPriority("中");

        mockMvc.perform(put("/api/testcases/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateCase)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("操作成功"))
                .andExpect(jsonPath("$.data.title").value("用户登录测试（已更新）"))
                .andExpect(jsonPath("$.data.status").value("执行中"))
                .andExpect(jsonPath("$.data.priority").value("中"));
    }

    @Test
    @DisplayName("DELETE /api/testcases/{id} - 删除测试用例成功")
    void deleteTestCase_Success() throws Exception {
        String response = mockMvc.perform(post("/api/testcases")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCase)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long id = objectMapper.readTree(response).get("data").get("id").asLong();

        mockMvc.perform(delete("/api/testcases/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("操作成功"));

        mockMvc.perform(get("/api/testcases/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("GET /api/testcases - 查询所有测试用例成功")
    void listTestCases_Success() throws Exception {
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
            mockMvc.perform(post("/api/testcases")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(tc)));
        }

        mockMvc.perform(get("/api/testcases"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data", hasSize(15)));
    }

    @Test
    @DisplayName("GET /api/testcases - 按关键词搜索")
    void listTestCasesByKeyword_Success() throws Exception {
        mockMvc.perform(post("/api/testcases")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCase)));

        mockMvc.perform(get("/api/testcases")
                .param("keyword", "登录"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data[0].title", containsString("登录")));
    }
}
