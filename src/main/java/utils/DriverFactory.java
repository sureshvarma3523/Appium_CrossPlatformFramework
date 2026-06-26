package utils;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.options.XCUITestOptions;

import java.net.URL;
import java.time.Duration;
import java.util.Properties;

public class DriverFactory {

    public static AppiumDriver CreateDriver(Properties prop,URL serverUrl){
        String platformName = prop.getProperty("platform").trim().toLowerCase();

        AppiumDriver driver = null;
        switch (platformName){
            case "android":
                driver =CreateAndroidDriver(prop,serverUrl);
                break;
            case "ios":
                driver = CreateIOSDriver(prop,serverUrl);
                break;
            default:
                throw new IllegalArgumentException("Unsupported platform '" + platformName
                        + "'. Use 'android' or 'ios' (set via testng.xml parameter or -Dplatform).");

        }
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        return driver;
    }

    public static AndroidDriver CreateAndroidDriver(Properties prop, URL serverUrl){
        UiAutomator2Options options = new UiAutomator2Options();
        options.setDeviceName(prop.getProperty("AndroidDeviceName"));
//        options.setApp(System.getProperty("user.dir")
//                + "/src/test/java/Resources/"
//                + prop.getProperty("androidApp", "General-Store.apk"));

        //To Run in Docker/Linux(Jenkins)
        options.setApp("C:\\Users\\LENOVO\\Documents\\Workspace\\Learning\\Appium_learning"
                + "\\AppiumFrameworkLearn - Mine\\src\\test\\java\\Resources\\"
                + prop.getProperty("androidApp", "General-Store.apk"));
        String chromedriver = prop.getProperty("chromedriverPath");
        if(chromedriver!=null && !chromedriver.isEmpty()){
            options.setChromedriverExecutable(chromedriver);
        }

        return new AndroidDriver(serverUrl, options);
    }

    public static IOSDriver CreateIOSDriver(Properties prop, URL serverUrl){
        XCUITestOptions options = new XCUITestOptions();
        options.setDeviceName(prop.getProperty("iosDeviceName","iPhone 13 Pro"));
        options.setApp(prop.getProperty("iosApp"));
        options.setPlatformVersion(prop.getProperty("iosPlatformVersion"));
        options.setWdaLaunchTimeout(Duration.ofSeconds(20));
        return new IOSDriver(serverUrl, options);

    }

}
