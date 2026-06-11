package com.test.ui;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType.LaunchOptions;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.test.ui.page.SwaggerPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("YAML-driven Swagger UI scenarios")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class SwaggerYamlDataDrivenIT {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, String> variables = new HashMap<>();

    @LocalServerPort
    private int port;

    @Test
    @DisplayName("Execute Swagger UI flow from YAML file")
    void shouldExecuteSwaggerFlowFromYaml() throws Exception {
        Yaml yaml = new Yaml();
        InputStream inputStream = getClass().getResourceAsStream("/testdata/swagger_scenarios.yaml");
        List<Map<String, Object>> scenarios = yaml.load(inputStream);

        try (Playwright playwright = Playwright.create()) {
            LaunchOptions options = new LaunchOptions();
            boolean headless = isHeadlessMode();
            options.setHeadless(headless);
            options.setSlowMo(headless ? 0 : 800);

            Browser browser = playwright.chromium().launch(options);
            BrowserContext context = browser.newContext();
            Page page = context.newPage();
            SwaggerPage swaggerPage = new SwaggerPage(page, "http://127.0.0.1:" + port);

            for (Map<String, Object> scenario : scenarios) {
                executeScenario(swaggerPage, scenario);
            }

            context.close();
            browser.close();
        }
    }

    @SuppressWarnings("unchecked")
    private void executeScenario(SwaggerPage swaggerPage, Map<String, Object> scenario) throws Exception {
        List<Map<String, Object>> steps = (List<Map<String, Object>>) scenario.get("steps");
        for (Map<String, Object> step : steps) {
            executeStep(swaggerPage, step);
        }
    }

    private void executeStep(SwaggerPage swaggerPage, Map<String, Object> step) throws Exception {
        String action = (String) step.get("action");
        switch (action) {
            case "openSwaggerUi" -> swaggerPage.openSwaggerUi();
            case "create" -> swaggerPage.executeCreate((String) step.get("body"));
            case "update" -> swaggerPage.executeUpdate(resolveId(step), (String) step.get("body"));
            case "delete" -> swaggerPage.executeDelete(resolveId(step));
            case "assertStatus" -> assertEquals(
                    step.get("expected"),
                    swaggerPage.getResponseStatus((String) step.get("operationId")));
            case "assertBodyContains" -> assertTrue(
                    swaggerPage.getResponseBody((String) step.get("operationId"))
                            .contains((String) step.get("expected")));
            case "captureId" -> captureId(swaggerPage, step);
            default -> throw new IllegalArgumentException("Unsupported YAML action: " + action);
        }
    }

    private void captureId(SwaggerPage swaggerPage, Map<String, Object> step) throws Exception {
        String operationId = (String) step.get("operationId");
        String variable = (String) step.get("variable");
        JsonNode response = objectMapper.readTree(swaggerPage.getResponseBody(operationId));
        variables.put(variable, response.path("data").path("id").asText());
    }

    private long resolveId(Map<String, Object> step) {
        String idVariable = (String) step.get("idVariable");
        if (idVariable != null) {
            return Long.parseLong(variables.get(idVariable));
        }
        return Long.parseLong(String.valueOf(step.get("id")));
    }

    private boolean isHeadlessMode() {
        String explicitValue = System.getProperty("playwright.headless");
        if (explicitValue != null) {
            return Boolean.parseBoolean(explicitValue);
        }
        return Boolean.parseBoolean(System.getenv().getOrDefault("CI", "false"));
    }

}
