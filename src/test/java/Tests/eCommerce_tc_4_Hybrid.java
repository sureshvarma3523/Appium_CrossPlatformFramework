package Tests;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import TestUtils.BaseTest;
import com.google.common.collect.ImmutableMap;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.JavascriptExecutor;
import org.testng.annotations.BeforeMethod;
import pageObjects.CartPage;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import pageObjects.ProductCataloguePage;
import org.testng.Assert;
import org.testng.annotations.Test;

import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;

public class eCommerce_tc_4_Hybrid extends BaseTest {

    @BeforeMethod
    public void PreSetUp() {
        String platform = platFormName();
        if (platform.equalsIgnoreCase("android")) {
            ((JavascriptExecutor) driver).executeScript("mobile: startActivity",
                    ImmutableMap.of("intent", "com.androidsample.generalstore/.MainActivity"));
        } else {
            ((JavascriptExecutor) driver).executeScript("mobile: launchApp",
                    ImmutableMap.of("bundleId", "com.androidsample.generalstore"));
        }
    }

    @Test
    public void FillForm() throws InterruptedException
    {
        getFP().setNameField("Rahul Shetty");
        getFP().setGender("Female");
        getFP().setCountryDD("Algeria");
        //If page moves to another page, return the driver and use that with creating object for that page class file.
        // If we want to use the page freshly, return the driver in basetest class using getPageName() method.
        ProductCataloguePage pcp = getFP().submitForm();
        pcp.addItemToCartByIndex(0);
        pcp.addItemToCartByIndex(0);
        CartPage cp = pcp.gotoCart();

        WebDriverWait wait =new WebDriverWait(driver,Duration.ofSeconds(5));
        wait.until(ExpectedConditions.attributeContains(driver.findElement(By.id("com.androidsample.generalstore:id/toolbar_title")),"text" , "Cart"));
        List<WebElement> productPrices =driver.findElements(By.id("com.androidsample.generalstore:id/productPrice"));
        int count = productPrices.size();
        double totalSum =0;
        for(int i =0; i< count; i++)
        {
            String amountString =productPrices.get(i).getText();
            Double price = extractPrice(amountString);
            totalSum = totalSum + price;  //160.97 + 120 =280.97

        }
        String displaySum =driver.findElement(By.id("com.androidsample.generalstore:id/totalAmountLbl")).getText();
        Double displayFormattedSum = extractPrice(displaySum);
        Assert.assertEquals(totalSum, displayFormattedSum);
        WebElement ele = driver.findElement(By.id("com.androidsample.generalstore:id/termsButton"));
        longPress(ele);
        driver.findElement(By.id("android:id/button1")).click();
        cp.clickCheckbox();
        driver.findElement(By.id("com.androidsample.generalstore:id/btnProceed")).click();
        Thread.sleep(6000);
        Set<String> contexts= getAvailableContexts();
        System.out.println("contexts: "+contexts);
        switchContext("WEBVIEW_com.androidsample.generalstore");
        driver.findElement(By.name("q")).sendKeys("rahul shetty academy");
        driver.findElement(By.name("q")).sendKeys(Keys.ENTER);
        ((AndroidDriver) driver).pressKey(new KeyEvent(AndroidKey.BACK));
        switchContext("NATIVE_APP");


    }


}
