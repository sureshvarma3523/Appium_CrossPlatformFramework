package TestUtils;

import io.appium.java_client.AppiumDriver;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.testng.ITestListener;
import org.testng.ITestResult;
import utils.BaseActions;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Captures a screenshot whenever a test fails and saves it under ./screenshots
 * as &lt;testName&gt;_&lt;timestamp&gt;.png. Registered in testng.xml so it applies
 * to every class in the suite. Never throws: a screenshot problem must not mask
 * the real test failure.
 */
public class ScreenshotListener implements ITestListener {

    @Override
    public void onTestFailure(ITestResult result) {
        Object testInstance = result.getInstance();
        if (!(testInstance instanceof BaseActions)) {
            System.err.println("ScreenshotListener: test instance is not a BaseActions; skipping screenshot.");
            return;
        }

        AppiumDriver driver = ((BaseActions) testInstance).getDriver();
        if (driver == null) {
            System.err.println("ScreenshotListener: driver is null (failed before setup?); skipping screenshot.");
            return;
        }

        try {
            File source = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File destination = new File("screenshots", result.getName() + "_" + timestamp + ".png");
            FileUtils.copyFile(source, destination);
            System.out.println("Screenshot saved on failure: " + destination.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("ScreenshotListener: failed to capture screenshot: " + e.getMessage());
        }
    }
}
