package utils;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ReusableMethods extends BaseActions {

    public ReusableMethods(AppiumDriver driver) {
        super(driver);
    }

    /**
     * Single source of truth for the platform: derived from the live driver instance,
     * which was built from the resolved platform (-Dplatform > TestNG param > data.properties).
     * This guarantees the launch branch can never disagree with the driver that exists.
     */
    public String platFormName() {

        return isAndroid() ? "android" : "ios";
    }

    public void waitAndClick(WebElement element) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.elementToBeClickable(element));
            element.click();
        } catch (TimeoutException e) {
            System.err.println("Element not clickable: " + e.getMessage());
            throw new RuntimeException("Failed to click element", e);
        }
    }

    public void waitForElementPresence(By locator) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        } catch (TimeoutException e) {
            System.err.println("Element not found: " + e.getMessage());
            throw new RuntimeException("Element not present in DOM", e);
        }
    }

    public void waitForElementVisibility(WebElement element) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.visibilityOf(element));
        } catch (TimeoutException e) {
            System.err.println("Element not visible: " + e.getMessage());
            throw new RuntimeException("Element not visible", e);
        }
    }

    public void waitForElementsPresence(List<WebElement> elements) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.visibilityOfAllElements(elements));
        } catch (TimeoutException e) {
            System.err.println("Elements not found: " + e.getMessage());
            throw new RuntimeException("Elements not present", e);
        }
    }

    public void sendKeysWithWait(WebElement element, String text) {
        // Retry on staleness: a freshly (re)launched activity can recreate the view
        // between locating the element and typing into it. The PageFactory proxy
        // re-locates the element on each attempt, so a retry resolves the race.
        int attempts = 0;
        while (true) {
            try {
                waitForElementVisibility(element);
                element.clear();
                element.sendKeys(text);
                return;
            } catch (StaleElementReferenceException e) {
                if (++attempts >= 3) {
                    System.err.println("Error sending keys: element stale after " + attempts + " attempts");
                    throw new RuntimeException("Failed to send keys (stale element)", e);
                }
            } catch (Exception e) {
                System.err.println("Error sending keys: " + e.getMessage());
                throw new RuntimeException("Failed to send keys", e);
            }
        }
    }

    public String getElementText(WebElement element) {
        try {
            waitForElementVisibility(element);
            return element.getText();
        } catch (Exception e) {
            System.err.println("Error getting text: " + e.getMessage());
            throw new RuntimeException("Failed to get element text", e);
        }
    }

    public String getElementAttribute(WebElement element, String attribute) {
        try {
            waitForElementVisibility(element);
            return element.getAttribute(attribute);
        } catch (Exception e) {
            System.err.println("Error getting attribute: " + e.getMessage());
            throw new RuntimeException("Failed to get element attribute", e);
        }
    }

    public boolean isElementDisplayed(WebElement element) {
        try {
            return element.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isElementEnabled(WebElement element) {
        try {
            return element.isEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    public void scrollToElement(WebElement element) {
        try {
            ((org.openqa.selenium.JavascriptExecutor) driver)
                    .executeScript("arguments[0].scrollIntoView(true);", element);
            Thread.sleep(500);
        } catch (Exception e) {
            System.err.println("Error scrolling to element: " + e.getMessage());
            throw new RuntimeException("Failed to scroll to element", e);
        }
    }

    public int getListSize(List<WebElement> elements) {
        try {
            return elements.size();
        } catch (Exception e) {
            System.err.println("Error getting list size: " + e.getMessage());
            return 0;
        }
    }

    public Double extractPrice(String priceText) {
        try {
            return Double.parseDouble(priceText.replaceAll("[^0-9.]", ""));
        } catch (Exception e) {
            System.err.println("Error extracting price: " + e.getMessage());
            throw new RuntimeException("Failed to extract price", e);
        }
    }

    public void waitForSeconds(long seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            System.err.println("Wait interrupted: " + e.getMessage());
        }
    }

    public boolean waitForCondition(java.util.function.BooleanSupplier condition, long timeoutSeconds) {
        long startTime = System.currentTimeMillis();
        long timeoutMillis = timeoutSeconds * 1000;

        while (System.currentTimeMillis() - startTime < timeoutMillis) {
            if (condition.getAsBoolean()) {
                return true;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }

    public void clickByCoordinates(int x, int y) {
        try {
            ((org.openqa.selenium.JavascriptExecutor) driver)
                    .executeScript("document.elementFromPoint(" + x + ", " + y + ").click();");
        } catch (Exception e) {
            System.err.println("Error clicking by coordinates: " + e.getMessage());
            throw new RuntimeException("Failed to click by coordinates", e);
        }
    }

    public String getCurrentPageTitle() {
        try {
            return driver.getTitle();
        } catch (Exception e) {
            System.err.println("Error getting page title: " + e.getMessage());
            return "";
        }
    }

    public String getCurrentPageUrl() {
        try {
            return driver.getCurrentUrl();
        } catch (Exception e) {
            System.err.println("Error getting page URL: " + e.getMessage());
            return "";
        }
    }

    public void ScreenShot() throws IOException {
        try {
            String picture ="";
            File source = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
            File destination = new File("screenshots/"+ picture + ".png");
            FileUtils.copyFile(source, destination);
        } catch (IOException e) {
            System.err.println("Error in ScreenShot: " + e.getMessage());
            throw new IOException("Failed to take screenshot", e);
        } catch (Exception e) {
            System.err.println("Unexpected error in ScreenShot: " + e.getMessage());
            throw new IOException("Failed to take screenshot due to unexpected error", e);
        }
    }



    public Set<String> getAvailableContexts() {
        try {
            if (isAndroid()) {
                return ((AndroidDriver) driver).getContextHandles();
            } else if (driver instanceof IOSDriver) {
                return ((IOSDriver) driver).getContextHandles();
            }
        } catch (Exception e) {
            System.err.println("Error getting contexts: " + e.getMessage());
        }
        return new HashSet<>();
    }

    public void switchContext(String contextName) {
        try {
            if (isAndroid()) {
                ((AndroidDriver) driver).context(contextName);
            } else if (driver instanceof IOSDriver) {
                ((IOSDriver) driver).context(contextName);
            }
        } catch (Exception e) {
            System.err.println("Error switching context to " + contextName + ": " + e.getMessage());
            throw new RuntimeException("Failed to switch context", e);
        }
    }

    public String getCurrentContext() {
        try {
            if (isAndroid()) {
                return ((AndroidDriver) driver).getContext();
            } else if (driver instanceof IOSDriver) {
                return ((IOSDriver) driver).getContext();
            }
        } catch (Exception e) {
            System.err.println("Error getting current context: " + e.getMessage());
        }
        return null;
    }
}