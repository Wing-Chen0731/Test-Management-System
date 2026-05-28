package com.test.controller;

import com.test.entity.TestCase;
import com.test.mapper.TestCaseMapper;
import com.test.service.TestCaseService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TestCaseController.class)
@DisplayName("TestCase controller API tests")
public class TestCaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TestCaseService testCaseService;

    @MockBean
    private TestCaseMapper testCaseMapper;

    @Test
    @DisplayName("Create testcase returns success")
    void shouldCreateTestCase() throws Exception {
        TestCase input = new TestCase();
        input.setTitle("login validation");

        when(testCaseService.createTestCase(any(TestCase.class))).thenReturn(input);

        mockMvc.perform(post("/api/testcases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"login validation\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.title").value("login validation"));
    }

    @Test
    @DisplayName("Create testcase validates required title")
    void shouldReturn400WhenTitleMissing() throws Exception {
        mockMvc.perform(post("/api/testcases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("Create testcase returns error when service returns null")
    void shouldReturnErrorWhenCreateServiceReturnsNull() throws Exception {
        when(testCaseService.createTestCase(any(TestCase.class))).thenReturn(null);

        mockMvc.perform(post("/api/testcases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"create failed\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("Update testcase returns success")
    void shouldUpdateTestCase() throws Exception {
        TestCase updated = new TestCase();
        updated.setId(1L);
        updated.setTitle("updated title");

        when(testCaseService.updateTestCase(any(Long.class), any(TestCase.class))).thenReturn(updated);

        mockMvc.perform(put("/api/testcases/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"updated title\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("updated title"));
    }

    @Test
    @DisplayName("Update testcase returns error when service returns null")
    void shouldReturnErrorWhenUpdateReturnsNull() throws Exception {
        when(testCaseService.updateTestCase(any(Long.class), any(TestCase.class))).thenReturn(null);

        mockMvc.perform(put("/api/testcases/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"missing\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("Delete testcase returns success")
    void shouldDeleteTestCase() throws Exception {
        when(testCaseService.deleteTestCase(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/testcases/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("Delete testcase returns error when service fails")
    void shouldReturnErrorWhenDeleteFails() throws Exception {
        when(testCaseService.deleteTestCase(99L)).thenReturn(false);

        mockMvc.perform(delete("/api/testcases/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("Get testcase by id returns success")
    void shouldGetTestCaseById() throws Exception {
        TestCase testCase = new TestCase();
        testCase.setId(1L);
        testCase.setTitle("found title");

        when(testCaseService.getTestCaseById(1L)).thenReturn(testCase);

        mockMvc.perform(get("/api/testcases/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("found title"));
    }

    @Test
    @DisplayName("Get testcase by id returns error when not found")
    void shouldReturnErrorWhenGetByIdReturnsNull() throws Exception {
        when(testCaseService.getTestCaseById(99L)).thenReturn(null);

        mockMvc.perform(get("/api/testcases/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("List testcases without keyword")
    void shouldListAllTestCases() throws Exception {
        TestCase testCase = new TestCase();
        testCase.setId(1L);
        testCase.setTitle("list title");

        when(testCaseService.getAllTestCases()).thenReturn(List.of(testCase));

        mockMvc.perform(get("/api/testcases"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].title").value("list title"));
    }

    @Test
    @DisplayName("List testcases searches when keyword is provided")
    void shouldSearchTestCasesWhenKeywordProvided() throws Exception {
        TestCase testCase = new TestCase();
        testCase.setId(2L);
        testCase.setTitle("keyword title");

        when(testCaseService.searchTestCases("keyword")).thenReturn(List.of(testCase));

        mockMvc.perform(get("/api/testcases").param("keyword", "keyword"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].id").value(2));
    }

    @Test
    @DisplayName("List testcases treats empty keyword as full list")
    void shouldListAllWhenKeywordIsEmpty() throws Exception {
        when(testCaseService.getAllTestCases()).thenReturn(List.of());

        mockMvc.perform(get("/api/testcases").param("keyword", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }
}
