package TestUtils;

import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServiceBuilder;
import pageObjects.FormPage;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import utils.DriverFactory;
import utils.ReusableMethods;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Duration;
import java.util.Properties;


// Registering listeners here (not in testng.xml) makes them fire for EVERY run style:
// the IDE gutter (single test/class), a testng.xml suite run, and `mvn test`. TestNG reads
// this annotation off the test class in all cases, so reports + screenshots always generate.
// The annotation is fully qualified to avoid a name clash with TestUtils.Listeners.
@org.testng.annotations.Listeners({ScreenshotListener.class, Listeners.class})
public class BaseTest extends ReusableMethods {

    public AppiumDriverLocalService service;

    public BaseTest() {

        super(null);
    }

    //Here optional platform parameter passing through ConfigureAppium() method will be helpful if we run test from test file directly
    // alwaysRun = true so setup still runs during group-filtered suites (e.g. smoke.xml);
    // otherwise TestNG skips config methods that aren't in the included group.
    @BeforeClass(alwaysRun = true)
    @Parameters({"platform"})
    public void ConfigureAppium(@org.testng.annotations.Optional("android") String platform) throws IOException, URISyntaxException {

        Properties prop = new Properties();

        try (FileInputStream fis = new FileInputStream(System.getProperty("user.dir") + "/src/main/java/Resources/data.properties")) {
            prop.load(fis);
        }


        // Priority: System property (-Dplatform) > TestNG parameter )
        // -Dplatform overrides the TestNG/@Optional value; we then always feed the
        // resolved value into prop so DriverFactory (which reads prop) honours it.
        // Here System.getProperty is getting from -Dplatform command line
        if (System.getProperty("platform") != null) {
            platform = System.getProperty("platform");
        }
        if (platform != null) {
            prop.setProperty("platform", platform);
        }
        System.out.println("Platform set for the test:" + platform);

        String ipAddress = System.getProperty("ipAddress") != null
                ? System.getProperty("ipAddress") : prop.getProperty("ipAddress");
        // Each class spins up its own server; usingAnyFreePort() avoids the bind race
        // that hits a hardcoded port when one class stops its server and the next starts.
        // The driver connects via service.getUrl(), so the chosen port is picked up automatically.
//        service = new AppiumServiceBuilder()
//                .withAppiumJS(new File("C:/Users/LENOVO/AppData/Roaming/npm/node_modules/appium/build/lib/main.js"))
//                .withIPAddress(ipAddress).usingAnyFreePort()
//                .withTimeout(Duration.ofSeconds(60))
//                .build();
//        service.start();

//        driver = DriverFactory.CreateDriver(prop, service.getUrl());
        URL url = new URL("http://host.docker.internal:4723");
        driver = DriverFactory.CreateDriver(prop, url);

    }

    public FormPage getFP() {

        return new FormPage(driver);
    }


    //If page moves to another page in one test, return the driver in last method in that page and use that with creating object for that page class file.
    // If we want to use the page freshly, return the driver in basetest class using getPageName() method.
//    public ProductCataloguePage getPCP() {
//        return new ProductCataloguePage(driver);
//    }
//
//
//    public CartPage getCP() {
//        return new CartPage(driver);
//    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        // Null-safe and independent: if the driver failed to start we must still
        // stop the Appium server, otherwise each failed class leaks a node process.
        if (driver != null) {
            driver.quit();
        }
        if (service != null && service.isRunning()) {
            service.stop();
        }
    }

}

