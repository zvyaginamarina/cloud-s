package tests.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.options.RequestOptions;

import io.qameta.allure.Allure;
import io.qameta.allure.Step;

public class ApiClient {
    private static final Logger logger = LoggerFactory.getLogger(ApiClient.class);
    private APIRequestContext request;

    public ApiClient(APIRequestContext request) {
        this.request = request;
    }

    @Step
    public APIResponse get(String endpoint) {
        startLogging(endpoint, "GET", " ");
        APIResponse response = request.get(endpoint);
        addLogAllure(response);
        return response;
    }

    @Step
    public APIResponse getWithQueries(String endpoint, String params, String value) {
        startLogging(endpoint, "GET", " with query-params");
        APIResponse response = request.get(endpoint, RequestOptions.create().setQueryParam(params, value));
        addLogAllure(response);
        return response;
    }

    @Step
    public APIResponse getWithHeaders(String endpoint, String header, String value) {
        startLogging(endpoint, "GET", " with headers");
        APIResponse response = request.get(endpoint, RequestOptions.create().setHeader(header, value));
        addLogAllure(response);
        return response;
    }

    @Step
    public APIResponse options(String endpoint, String header1, String value1, String header2, String value2) {
        startLogging(endpoint, "OPTIONS", " ");
        APIResponse response = request.fetch(endpoint,
                RequestOptions.create().setMethod("OPTIONS").setHeader(header1, value1).setHeader(header2, value2));
        addLogAllure(response);
        return response;
    }

    @Step
    public APIResponse post(String endpoint) {
        startLogging(endpoint, "POST", " ");
        APIResponse response = request.post(endpoint);
        addLogAllure(response);
        return response;
    }

    @Step
    private void addLogAllure(APIResponse response) {
        String logInfo = String.format(
                "Status: %d%nHeaders: %s%nBody: %s", response.status(), response.headers(), response.text());
        Allure.addAttachment("log", logInfo);
    }

    @Step
    private void startLogging(String endpoint, String method, String methodParams) {
        String logInfo = method + " " + endpoint + methodParams;
        logger.info(logInfo);
        Allure.step(logInfo);
    }

}
