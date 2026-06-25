package TestUtils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import utils.BaseActions;

import java.io.File;

/**
 * TestNG listener that drives the Extent HTML report (./reports/index.html).
 * <p>
 * A node is created per test method; pass/fail/skip status and execution time are
 * logged, and a screenshot is embedded into the report on failure. The driver is
 * obtained via {@link BaseActions#getDriver()} – no reflection on a public field –
 * so it works for any test whose instance extends {@link BaseActions}.
 * <p>
 * The {@link ExtentTest} is held in a {@link ThreadLocal} so parallel tests don't
 * overwrite each other's report node. Register in testng.xml under &lt;listeners&gt;.
 */
public class Listeners implements ITestListener, ISuiteListener {

    private static final String SCREENSHOTS_DIR = "screenshots";

    private final ExtentReports extent = ExtentReporterNG.getReporterObject();
    private final ThreadLocal<ExtentTest> extentTest = new ThreadLocal<>();

    /**
     * Fires once per run (TestNG wraps even a single IDE-gutter test in a suite), before any
     * test executes. Clearing the screenshots folder here means each run starts clean instead
     * of accumulating PNGs from previous runs.
     */
    @Override
    public void onStart(ISuite suite) {
        clearScreenshotsFolder();
    }

    @Override
    public void onStart(ITestContext context) {
        System.out.println("==== Suite started: " + context.getName() + " ====");
    }

    @Override
    public void onTestStart(ITestResult result) {
        ExtentTest test = extent.createTest(result.getMethod().getMethodName());

        String description = result.getMethod().getDescription();
        if (description != null && !description.isEmpty()) {
            test.info(description);
        }
        String[] groups = result.getMethod().getGroups();
        if (groups.length > 0) {
            test.assignCategory(groups);
        }
        extentTest.set(test);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        long durationMs = result.getEndMillis() - result.getStartMillis();
        extentTest.get().log(Status.PASS, "Test Passed (" + durationMs + " ms)");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ExtentTest test = extentTest.get();
        test.fail(result.getThrowable());

        String base64 = captureScreenshot(result);
        if (base64 != null) {
            test.fail("Screenshot on failure",
                    MediaEntityBuilder.createScreenCaptureFromBase64String(base64).build());
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        // Without this, skipped tests (failed dependsOnMethods / SkipException) vanish from the report.
        ExtentTest test = extentTest.get();
        if (test == null) {
            // Skipped before onTestStart ran – e.g. a failed @BeforeClass.
            test = extent.createTest(result.getMethod().getMethodName());
        }
        test.log(Status.SKIP, "Test Skipped");
        if (result.getThrowable() != null) {
            test.skip(result.getThrowable());
        }
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        extentTest.get().log(Status.WARNING,
                "Test failed but within the configured success percentage");
    }

    @Override
    public void onFinish(ITestContext context) {
        System.out.println("==== Suite finished: " + context.getName()
                + " | Passed: " + context.getPassedTests().size()
                + " | Failed: " + context.getFailedTests().size()
                + " | Skipped: " + context.getSkippedTests().size() + " ====");
        // Persists everything buffered in memory to the HTML file. Without this the report is empty.
        extent.flush();
    }

    /**
     * Deletes leftover PNGs from previous runs so the screenshots folder only ever holds
     * the current run's failures. Only top-level .png files are removed; the folder itself
     * is kept (and created if missing). Never throws: a cleanup hiccup must not abort the run.
     */
    private void clearScreenshotsFolder() {
        File dir = new File(SCREENSHOTS_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
            return;
        }
        File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".png"));
        if (files == null) {
            return;
        }
        int deleted = 0;
        for (File file : files) {
            if (file.delete()) {
                deleted++;
            } else {
                System.err.println("Listeners: could not delete old screenshot " + file.getName());
            }
        }
        System.out.println("Listeners: cleared " + deleted + " old screenshot(s) from ./" + SCREENSHOTS_DIR);
    }

    /**
     * Grabs a base64 screenshot from the running test's driver. Embedding base64 into the
     * report (rather than a file path) means the report is self-contained and has no
     * broken-image links. Never throws: a screenshot problem must not mask the real failure.
     */
    private String captureScreenshot(ITestResult result) {
        Object instance = result.getInstance();
        if (!(instance instanceof BaseActions)) {
            System.err.println("Listeners: test instance is not a BaseActions; skipping screenshot.");
            return null;
        }
        AppiumDriver driver = ((BaseActions) instance).getDriver();
        if (driver == null) {
            System.err.println("Listeners: driver is null (failed before setup?); skipping screenshot.");
            return null;
        }
        try {
            return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64);
        } catch (Exception e) {
            System.err.println("Listeners: failed to capture screenshot: " + e.getMessage());
            return null;
        }
    }
}