package utils;

import com.google.common.collect.ImmutableMap;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import io.appium.java_client.ios.IOSDriver;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.DeviceRotation;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.ScreenOrientation;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebElement;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BaseActions {

    protected AppiumDriver driver;

    public BaseActions(AppiumDriver driver){

        this.driver=driver;
    }

    /** Exposes the active driver so TestNG listeners can capture screenshots on failure. */
    public AppiumDriver getDriver(){

        return driver;
    }

    protected boolean  isAndroid(){

        return driver instanceof AndroidDriver;
    }
    public void longPress(WebElement ele) {
        try {
            if (isAndroid()){
                ((JavascriptExecutor)driver).executeScript("mobile: longClickGesture", ImmutableMap.of(
                        "elementId", ((RemoteWebElement)ele).getId(),
                        "duration", 2000
                ));
            }
            else {
                Map<String, Object> params = new HashMap<>();
                params.put("element", ((RemoteWebElement) ele).getId());
                params.put("duration", 5);
                driver.executeScript("mobile:touchAndHold", params);
            }
        } catch (Exception e) {
            System.err.println("Error in longPress: " + e.getMessage());
            throw new RuntimeException("Failed to perform long press action", e);
        }
    }

    /** Android-only: UiAutomator scroll-into-view. Guards against misuse on iOS. */
    public void scrollToText(String text) {
        try {
            if (isAndroid()) {
                driver.findElement(AppiumBy.androidUIAutomator(
                        "new UiScrollable(new UiSelector()).scrollIntoView(text(\"" + text + "\"));"));
            } else {
                throw new UnsupportedOperationException(
                        "scrollToText uses Android UiAutomator; use scrollToWebElement on iOS.");
            }
        } catch (UnsupportedOperationException e) {
            System.err.println("Platform not supported: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("Error in scrollToText: " + e.getMessage());
            throw new RuntimeException("Failed to scroll to text: " + text, e);
        }
    }


    public void ScrollToEnd() {
        try {
            if (isAndroid()) {
                boolean canScrollMore;
                do {

                    canScrollMore = (Boolean) ((JavascriptExecutor) driver).executeScript("mobile: scrollGesture", ImmutableMap.of(
                            "left", 100, "top", 100, "width", 200, "height", 200,
                            "direction", "down",
                            "percent", 3.0
                    ));
                } while (canScrollMore);
            } else {
                // iOS has no scrollGesture/canScrollMore; scroll down until the page source stops
                // changing, capped to avoid an infinite loop on unexpectedly dynamic content.
                final int maxScrolls = 50;
                String previousSource = "";
                String currentSource = driver.getPageSource();
                int attempts = 0;
                while (!currentSource.equals(previousSource) && attempts < maxScrolls) {
                    previousSource = currentSource;
                    Map<String, Object> params = new HashMap<>();
                    params.put("direction", "down");
                    driver.executeScript("mobile:scroll", params);
                    currentSource = driver.getPageSource();
                    attempts++;
                }
            }
        } catch (Exception e) {
            System.err.println("Error in ScrollToEnd: " + e.getMessage());
            throw new RuntimeException("Failed to scroll to end", e);
        }
    }

    /** iOS-only: mobile:scroll towards a given element. */
    public void scrollToWebElement(WebElement ele) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("direction", "down");
            params.put("element", ((RemoteWebElement) ele).getId());
            driver.executeScript("mobile:scroll", params);
        } catch (Exception e) {
            System.err.println("Error in scrollToWebElement: " + e.getMessage());
            throw new RuntimeException("Failed to scroll to web element", e);
        }
    }


    public void SwipeAction(WebElement ele, String direction,double percentage) {
        try {
            if(isAndroid()){
                ((JavascriptExecutor)driver).executeScript("mobile: swipeGesture", ImmutableMap.of(
                        "elementId", ((RemoteWebElement)ele).getId(),
                        "direction", direction,
                        "percent", percentage
                ));
            }
            else {
                Map<String, Object> params = new HashMap<>();
                params.put("direction", direction);
                params.put("element", ((RemoteWebElement) ele).getId());
                driver.executeScript("mobile:swipe", params);
            }
        } catch (Exception e) {
            System.err.println("Error in SwipeAction: " + e.getMessage());
            throw new RuntimeException("Failed to perform swipe action", e);
        }
    }

    public void DragAndDropSourcetoCoordinates(WebElement source, int endX, int endY) {
        try {
            if (isAndroid()) {
                ((JavascriptExecutor)driver).executeScript("mobile: dragGesture", ImmutableMap.of(
                        "elementId", ((RemoteWebElement)source).getId(),
                        "endX", endX,
                        "endY", endY
                ));
            }
            else {
                // iOS has no dragGesture; drag from the source element's centre to the target coordinates.
                Rectangle rect = source.getRect();
                int startX = rect.getX() + rect.getWidth() / 2;
                int startY = rect.getY() + rect.getHeight() / 2;
                Map<String, Object> params = new HashMap<>();
                params.put("fromX", startX);
                params.put("fromY", startY);
                params.put("toX", endX);
                params.put("toY", endY);
                params.put("duration", 1.0);
                driver.executeScript("mobile: dragFromToForDuration", params);
            }
        } catch (Exception e) {
            System.err.println("Error in DragAndDropSourcetoCoordinates: " + e.getMessage());
            throw new RuntimeException("Failed to perform drag and drop action", e);
        }
    }

    /**
     * Rotates the device using precise x/y/z angles, e.g. rotateDevice(0, 0, 90) for landscape.
     * rotate(...) lives on SupportsRotation, implemented by AndroidDriver/IOSDriver (not AppiumDriver).
     */
    public void rotateDevice(int x, int y, int z) {
        try {
            DeviceRotation rotation = new DeviceRotation(x, y, z);
            if (isAndroid()) {
                ((AndroidDriver) driver).rotate(rotation);
            } else {
                ((IOSDriver) driver).rotate(rotation);
            }
        } catch (Exception e) {
            System.err.println("Error in rotateDevice: " + e.getMessage());
            throw new RuntimeException("Failed to rotate device to (" + x + ", " + y + ", " + z + ")", e);
        }
    }

    /** Rotates the device to a coarse orientation (PORTRAIT / LANDSCAPE) on Android and iOS. */
    public void rotateDevice(ScreenOrientation orientation) {
        try {
            if (isAndroid()) {
                ((AndroidDriver) driver).rotate(orientation);
            } else {
                ((IOSDriver) driver).rotate(orientation);
            }
        } catch (Exception e) {
            System.err.println("Error in rotateDevice: " + e.getMessage());
            throw new RuntimeException("Failed to rotate device to " + orientation, e);
        }
    }

    /**
     * Android-only: presses a hardware navigation key. Pass "back" or "home" (case-insensitive).
     * pressKey(...) lives on PressesKey, implemented by AndroidDriver (not AppiumDriver).
     */
    public void pressKey(String key) {
        try {
            if (!isAndroid()) {
                throw new UnsupportedOperationException(
                        "pressKey uses Android key events; not supported on iOS.");
            }
            AndroidKey androidKey;
            switch (key.toLowerCase()) {
                case "back":
                    androidKey = AndroidKey.BACK;
                    break;
                case "home":
                    androidKey = AndroidKey.HOME;
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported key: " + key + ". Use 'back' or 'home'.");
            }
            ((AndroidDriver) driver).pressKey(new KeyEvent(androidKey));
        } catch (UnsupportedOperationException | IllegalArgumentException e) {
            System.err.println("Invalid pressKey request: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("Error in pressKey: " + e.getMessage());
            throw new RuntimeException("Failed to press key: " + key, e);
        }
    }

    /** Cross-platform keyboard dismiss (no-op if no keyboard / unsupported). */
    public void hideKeyboard() {
        try {
            if (isAndroid()) {
                ((AndroidDriver) driver).hideKeyboard();
            } else if (driver instanceof IOSDriver) {
                ((IOSDriver) driver).hideKeyboard();
            }
        } catch (Exception e) {
            System.err.println("Error in hideKeyboard: " + e.getMessage());
            throw new RuntimeException("Failed to hide keyboard", e);
        }
    }


}
