package Tests;

import TestUtils.BaseTest;
import com.google.common.collect.ImmutableMap;
import org.openqa.selenium.JavascriptExecutor;
import org.testng.annotations.BeforeMethod;
import pageObjects.CartPage;
import pageObjects.ProductCataloguePage;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Focused native-only test: add two products and verify the cart's displayed
 * total equals the sum of the individual product prices. Complements the
 * full hybrid checkout flow in {@link eCommerce_tc_4_Hybrid}.
 */
public class eCommerce_tc_5_AddToCart extends BaseTest {
@BeforeMethod(alwaysRun = true)
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

    @Test(groups = {"Smoke"})
    public void verifyCartTotalMatchesSumOfPrices() {
        getFP().setNameField("Jane Doe");
        getFP().setGender("Female");
        getFP().setCountryDD("Algeria");

        ProductCataloguePage pcp = getFP().submitForm();
        // The catalogue only exposes one "ADD TO CART" button at a time, so add the
        // first product twice — the General Store creates two separate cart line items.
        pcp.addItemToCartByIndex(0);
        pcp.addItemToCartByIndex(0);
        CartPage cp = pcp.gotoCart();

        Assert.assertEquals(cp.getProductCount(), 2, "Cart should contain exactly two products");
        Assert.assertEquals(
                cp.getDisplayedTotal(),
                cp.getProductPriceSum(),
                "Displayed total should equal the sum of the product prices");
    }
}
