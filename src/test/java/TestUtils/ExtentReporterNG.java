package TestUtils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

/**
 * Factory for the single, shared {@link ExtentReports} object used across the suite.
 * The HTML report is written to ./reports/index.html. The {@link Listeners} class
 * owns the lifecycle: it creates a node per test and calls flush() when the suite ends.
 */
public class ExtentReporterNG {

    private static ExtentReports extent;

    public static ExtentReports getReporterObject() {
        // One report per JVM run – reused if the listener is created more than once.
        if (extent != null) {
            return extent;
        }

        String reportPath = System.getProperty("user.dir") + "/reports/index.html";
        ExtentSparkReporter reporter = new ExtentSparkReporter(reportPath);
        reporter.config().setReportName("Appium Automation Results");
        reporter.config().setDocumentTitle("Test Report");
        reporter.config().setTheme(Theme.DARK);

        extent = new ExtentReports();
        extent.attachReporter(reporter);
        extent.setSystemInfo("Tester", "Suresh Varma");
        extent.setSystemInfo("Platform", System.getProperty("platform", "android"));
        extent.setSystemInfo("OS", System.getProperty("os.name"));
        return extent;
    }
}