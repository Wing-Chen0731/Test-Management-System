package com.test.ui;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType.LaunchOptions;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.test.ui.page.SwaggerPage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.HttpURLConnection;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Swagger UI end-to-end test")
public class SwaggerUITest {
    private static final double SLOW_MOTION_MS = 1200;
    private static final double FINAL_REVIEW_MS = 8000;
    private static final String SWAGGER_UI_URL = "http://localhost:8080/swagger-ui/index.html";

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Playwright playwright;
    private Browser browser;
    private BrowserContext context;
    private Page page;
    private SwaggerPage swaggerPage;

    @BeforeEach
    void setUp() {
        Assumptions.assumeTrue(isSwaggerUiAvailable(), "Swagger UI is not available on localhost:8080");

        playwright = Playwright.create();

        LaunchOptions options = new LaunchOptions();
        boolean headless = isHeadlessMode();
        options.setHeadless(headless);
        options.setSlowMo(headless ? 0 : SLOW_MOTION_MS);

        browser = playwright.chromium().launch(options);
        context = browser.newContext();
        page = context.newPage();
        swaggerPage = new SwaggerPage(page);
    }

    @Test
    @DisplayName("Run CRUD walkthrough from Swagger UI")
    void testCrudWalkthroughByUI() throws Exception {
        swaggerPage.openSwaggerUi();

        swaggerPage.executeList(null);
        assertEquals("200", swaggerPage.getResponseStatus("list"));

        String createdTitle = "UI automation testcase " + System.currentTimeMillis();
        swaggerPage.executeCreate("{\"title\":\"" + createdTitle + "\"}");
        assertEquals("200", swaggerPage.getResponseStatus("create"));

        JsonNode createResponse = objectMapper.readTree(swaggerPage.getResponseBody("create"));
        long testCaseId = createResponse.path("data").path("id").asLong();
        assertTrue(testCaseId > 0);
        assertEquals(createdTitle, createResponse.path("data").path("title").asText());

        swaggerPage.executeGetById(testCaseId);
        assertEquals("200", swaggerPage.getResponseStatus("getById"));
        JsonNode getResponse = objectMapper.readTree(swaggerPage.getResponseBody("getById"));
        assertEquals(testCaseId, getResponse.path("data").path("id").asLong());
        assertEquals(createdTitle, getResponse.path("data").path("title").asText());

        String updatedTitle = createdTitle + " updated";
        swaggerPage.executeUpdate(testCaseId, "{\"title\":\"" + updatedTitle + "\"}");
        assertEquals("200", swaggerPage.getResponseStatus("update"));
        JsonNode updateResponse = objectMapper.readTree(swaggerPage.getResponseBody("update"));
        assertEquals(testCaseId, updateResponse.path("data").path("id").asLong());
        assertEquals(updatedTitle, updateResponse.path("data").path("title").asText());

        swaggerPage.executeDelete(testCaseId);
        assertEquals("200", swaggerPage.getResponseStatus("delete"));
        JsonNode deleteResponse = objectMapper.readTree(swaggerPage.getResponseBody("delete"));
        assertEquals(200, deleteResponse.path("code").asInt());

        swaggerPage.takeScreenshot("swagger-ui-test");
        if (!isHeadlessMode()) {
            swaggerPage.pause(FINAL_REVIEW_MS);
        }
    }

    private boolean isHeadlessMode() {
        String explicitValue = System.getProperty("playwright.headless");
        if (explicitValue != null) {
            return Boolean.parseBoolean(explicitValue);
        }
        return Boolean.parseBoolean(System.getenv().getOrDefault("CI", "false"));
    }

    private boolean isSwaggerUiAvailable() {
        try {
            HttpURLConnection connection = (HttpURLConnection) URI
                    .create(SWAGGER_UI_URL)
                    .toURL()
                    .openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(1000);
            connection.setReadTimeout(1000);
            return connection.getResponseCode() < 500;
        } catch (Exception ignored) {
            return false;
        }
    }

    @AfterEach
    void tearDown() {
        if (page != null) {
            page.close();
        }
        if (context != null) {
            context.close();
        }
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }
}
