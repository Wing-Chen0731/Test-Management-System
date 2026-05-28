package com.test.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Result response wrapper tests")
class ResultTest {

    @Test
    @DisplayName("success without data uses default success code and message")
    void successWithoutData() {
        Result<Void> result = Result.success();

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getMessage()).isEqualTo("操作成功");
        assertThat(result.getData()).isNull();
    }

    @Test
    @DisplayName("success with data stores payload")
    void successWithData() {
        Result<String> result = Result.success("payload");

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getMessage()).isEqualTo("操作成功");
        assertThat(result.getData()).isEqualTo("payload");
    }

    @Test
    @DisplayName("success with custom message stores message and payload")
    void successWithMessageAndData() {
        Result<Integer> result = Result.success("created", 1);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getMessage()).isEqualTo("created");
        assertThat(result.getData()).isEqualTo(1);
    }

    @Test
    @DisplayName("error without arguments uses default error code and message")
    void errorWithoutArguments() {
        Result<Void> result = Result.error();

        assertThat(result.getCode()).isEqualTo(500);
        assertThat(result.getMessage()).isEqualTo("操作失败");
        assertThat(result.getData()).isNull();
    }

    @Test
    @DisplayName("error with message uses default error code")
    void errorWithMessage() {
        Result<Void> result = Result.error("failed");

        assertThat(result.getCode()).isEqualTo(500);
        assertThat(result.getMessage()).isEqualTo("failed");
        assertThat(result.getData()).isNull();
    }

    @Test
    @DisplayName("error with code and message stores both")
    void errorWithCodeAndMessage() {
        Result<Void> result = Result.error(400, "bad request");

        assertThat(result.getCode()).isEqualTo(400);
        assertThat(result.getMessage()).isEqualTo("bad request");
        assertThat(result.getData()).isNull();
    }

    @Test
    @DisplayName("error with code message and data stores all fields")
    void errorWithCodeMessageAndData() {
        Map<String, String> data = Map.of("title", "must not be blank");

        Result<Map<String, String>> result = Result.error(400, "validation failed", data);

        assertThat(result.getCode()).isEqualTo(400);
        assertThat(result.getMessage()).isEqualTo("validation failed");
        assertThat(result.getData()).isEqualTo(data);
    }
}
