package tests.api;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.APIResponse;

import tests.BaseTest;
import tests.utils.ApiClient;
import tests.utils.TestConfig;

public class ApiTests extends BaseTest {

    public static Stream<Arguments> endpointParametersTest1() {
        Arguments arg1 = Arguments.of(clientA, TestConfig.INSTANCE.serviceAEndpoint(), "App-1");
        Arguments arg2 = Arguments.of(clientB, TestConfig.INSTANCE.serviceBEndpoint(), "App-2");
        return Stream.of(arg1, arg2);
    }

    public static Stream<Arguments> endpointParametersTest2() {
        Arguments arg1 = Arguments.of(TestConfig.INSTANCE.serviceAEndpoint(), "App-1");
        Arguments arg2 = Arguments.of(TestConfig.INSTANCE.serviceBEndpoint(), "App-2");
        return Stream.of(arg1, arg2);
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("endpointParametersTest1")
    @DisplayName("Get request to service returns success")
    void getServiceReturnSuccess(ApiClient client, String endpoint, String appId) {
        APIResponse response = client.get(endpoint);

        Map<String, String> headers = response.headers();
        String contentType = headers.get("content-type");
        String body = response.text();

        assertEquals(200, response.status());
        assertTrue(contentType.contains("text/plain"));
        assertTrue(body.contains(appId));
    }

    @ParameterizedTest(name = "{0} to {1}")
    @MethodSource("endpointParametersTest2")
    @DisplayName("Gateway routes to correct service and returns success")
    void getGatewayRoutSuccess(String endpoint, String appId) {
        APIResponse response = clientGateway.get(endpoint);
        String body = response.text();

        assertEquals(200, response.status());
        assertTrue(body.contains(appId));
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("endpointParametersTest1")
    @DisplayName("Gateway response equals to service response")
    void gatewayResponseEqualsToServiceResponse(ApiClient client, String endpoint, String appId) {
        APIResponse responseClient = client.get(endpoint);
        APIResponse responseGateway = clientGateway.get(endpoint);

        Map<String, String> headersClient = responseClient.headers();
        Map<String, String> headersGateway = responseGateway.headers();

        assertEquals(responseGateway.status(), responseClient.status());
        assertEquals(headersGateway.get("content-type"), headersClient.get("content-type"));
        assertEquals(headersGateway.get("content-length"), headersClient.get("content-length"));
        assertEquals(responseGateway.text(), responseClient.text());
    }

    @Test
    @DisplayName("Valid query params returns status 200")
    void serviceACorretParams() {
        APIResponse response = clientA.getWithQueries(TestConfig.INSTANCE.serviceAEndpoint(), "name", "test");

        assertEquals(200, response.status());
        assertTrue(response.text().contains("Приветствую! Вы в приложении: App-1"));
    }

    @ParameterizedTest
    @ValueSource(strings = { ">", "<", "\u0000", "\\", "\'", "\"" })
    @DisplayName("Invalid qurey param's value returns 400")
    void serviceAIncorrectParamsValueValidation(String symbol) {
        APIResponse response = clientA.getWithQueries(TestConfig.INSTANCE.serviceAEndpoint(), "name",
                "test" + symbol);

        assertEquals(400, response.status());
        assertTrue(response.text().contains("Недопустимые параметры запроса"));
    }

    @Disabled("BUG: No validation on parameter's names, should return 400 when invalid symbols in parameter's name, but return 200")
    @ParameterizedTest
    @ValueSource(strings = { ">", "<", "\u0000", "\\", "\'", "\"" })
    @DisplayName("Invalid qurey param's name returns 400")
    void serviceAIncorrectParamsValidation(String symbol) {
        APIResponse response = clientA.getWithQueries(TestConfig.INSTANCE.serviceAEndpoint(),
                "name" + symbol,
                "test");

        assertEquals(400, response.status());
        assertTrue(response.text().contains("Недопустимые параметры запроса"));
    }

    @Test
    @DisplayName("Log file created and contains correct info")
    void logFileCreatedAndContainsInfo() throws IOException {
        Path logPath = Path.of(TestConfig.INSTANCE.ms1FilePath(), "requests.log");

        Files.writeString(logPath, "");

        clientA.get(TestConfig.INSTANCE.serviceAEndpoint());

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            List<String> logData = Files.readAllLines(logPath);
            assertEquals(1, logData.size());
            assertEquals("MS1: User request: /hello", logData.get(0));
        });
    }

    @ParameterizedTest
    @DisplayName("Incorrect gateway route")
    @ValueSource(strings = { "/unknown/hello", "/serviceA/unknown" })
    void unknownGatewayRoute(String route) throws IOException {

        APIResponse responseUnknownRoute = clientGateway.get(route);
        Map<String, String> headers = responseUnknownRoute.headers();
        String ctHeader = headers.get("content-type");

        assertTrue(ctHeader.contains("application/json"));
        assertEquals(404, responseUnknownRoute.status());

        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(responseUnknownRoute.text());

        assertEquals(route, rootNode.get("path").asText());

    }

}
