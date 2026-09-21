package tests.utils;

import java.nio.file.Path;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.ConfigFactory;

@Config.Sources({ "classpath:config/test.properties" })
public interface TestConfig extends Config {

    public static final TestConfig INSTANCE = ConfigFactory.create(TestConfig.class);

    @Key("servicea.url")
    String urlServiceA();

    @Key("serviceb.url")
    String urlServiceB();

    @Key("gateway.url")
    String urlGateway();

    @Key("servicea.endpoint")
    String serviceAEndpoint();

    @Key("serviceb.endpoint")
    String serviceBEndpoint();

    @Key("timeout")
    int timeout();

    @Key("ms1.filepath")
    String ms1FilePath();

    @Key("ms2.filepath")
    String ms2FilePath();
}
