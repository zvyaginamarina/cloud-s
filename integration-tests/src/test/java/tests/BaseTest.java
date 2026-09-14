package tests;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import com.microsoft.playwright.APIRequest.NewContextOptions;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.Playwright;

import tests.utils.ApiClient;
import tests.utils.TestConfig;

public class BaseTest {

    private static Playwright playwright;
    private static APIRequestContext requestA;
    private static APIRequestContext requestB;
    private static APIRequestContext requestGateway;

    protected static ApiClient clientA;
    protected static ApiClient clientB;
    protected static ApiClient clientGateway;

    @BeforeAll
    static void setup() {
        playwright = Playwright.create();
        requestA = playwright.request()
                .newContext(new NewContextOptions()
                        .setBaseURL(TestConfig.INSTANCE.urlServiceA())
                        .setTimeout(TestConfig.INSTANCE.timeout()));
        requestB = playwright.request()
                .newContext(new NewContextOptions()
                        .setBaseURL(TestConfig.INSTANCE.urlServiceB())
                        .setTimeout(TestConfig.INSTANCE.timeout()));
        requestGateway = playwright.request()
                .newContext(new NewContextOptions().setBaseURL(TestConfig.INSTANCE.urlGateway()));

        clientA = new ApiClient(requestA);
        clientB = new ApiClient(requestB);
        clientGateway = new ApiClient(requestGateway);
    }

    @AfterAll
    static void teardown() {
        if (requestA != null) {
            requestA.dispose();
        }
        if (requestB != null) {
            requestB.dispose();
        }
        if (requestGateway != null) {
            requestGateway.dispose();
        }
        if (playwright != null) {
            playwright.close();
        }
    }

}
