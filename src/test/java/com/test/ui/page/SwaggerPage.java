package com.test.ui.page;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

import java.nio.file.Paths;

public class SwaggerPage {
    private static final double STEP_PAUSE_MS = 1500;
    private static final double RESPONSE_PAUSE_MS = 3000;
    private static final String OPERATION_PREFIX = "#/test-case-controller/";

    private final Page page;
    private final String swaggerUiUrl;

    public SwaggerPage(Page page, String baseUrl) {
        this.page = page;
        this.swaggerUiUrl = baseUrl + "/swagger-ui/index.html";
    }

    public void openSwaggerUi() {
        page.navigate(swaggerUiUrl);
        page.waitForTimeout(STEP_PAUSE_MS);
    }

    public void executeList(String keyword) {
        Locator block = prepareOperation("list");
        Locator queryInput = block.locator("input[type='text']");
        if (keyword != null) {
            queryInput.fill(keyword);
            page.waitForTimeout(STEP_PAUSE_MS);
        }
        clickExecute(block);
    }

    public void executeCreate(String jsonBody) {
        Locator block = prepareOperation("create");
        block.locator("textarea").fill(jsonBody);
        page.waitForTimeout(STEP_PAUSE_MS);
        clickExecute(block);
    }

    public void executeGetById(long id) {
        Locator block = prepareOperation("getById");
        block.locator("input[type='text']").fill(String.valueOf(id));
        page.waitForTimeout(STEP_PAUSE_MS);
        clickExecute(block);
    }

    public void executeUpdate(long id, String jsonBody) {
        Locator block = prepareOperation("update");
        block.locator("input[type='text']").fill(String.valueOf(id));
        page.waitForTimeout(STEP_PAUSE_MS);
        block.locator("textarea").fill(jsonBody);
        page.waitForTimeout(STEP_PAUSE_MS);
        clickExecute(block);
    }

    public void executeDelete(long id) {
        Locator block = prepareOperation("delete");
        block.locator("input[type='text']").fill(String.valueOf(id));
        page.waitForTimeout(STEP_PAUSE_MS);
        clickExecute(block);
    }

    public String getResponseStatus(String operationId) {
        Locator statusCell = operationBlock(operationId)
                .locator("table.live-responses-table tbody tr td.response-col_status");
        statusCell.waitFor();
        return statusCell.textContent();
    }

    public String getResponseBody(String operationId) {
        Locator responseBody = operationBlock(operationId)
                .locator("div.responses-inner h5 + div pre");
        responseBody.waitFor();
        return responseBody.innerText();
    }

    public void takeScreenshot(String fileName) {
        page.screenshot(new Page.ScreenshotOptions()
                .setPath(Paths.get(fileName + ".png"))
                .setFullPage(true));
    }

    public void pause(double timeoutMs) {
        page.waitForTimeout(timeoutMs);
    }

    private Locator prepareOperation(String operationId) {
        Locator block = operationBlock(operationId);
        expandOperation(operationId);
        Locator tryItOutButton = block.getByRole(
                AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Try it out"));
        if (tryItOutButton.isVisible()) {
            tryItOutButton.click();
            page.waitForTimeout(STEP_PAUSE_MS);
        }
        return block;
    }

    private void expandOperation(String operationId) {
        operationSummary(operationId).click();
        page.waitForTimeout(STEP_PAUSE_MS);
    }

    private void clickExecute(Locator block) {
        block.getByRole(
                AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Execute"))
                .click();
        page.waitForTimeout(RESPONSE_PAUSE_MS);
    }

    private Locator operationSummary(String operationId) {
        return page.locator("div.opblock-summary:has(a[href='" + OPERATION_PREFIX + operationId + "'])");
    }

    private Locator operationBlock(String operationId) {
        return page.locator(".opblock:has(a[href='" + OPERATION_PREFIX + operationId + "'])");
    }
}
