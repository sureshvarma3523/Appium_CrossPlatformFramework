package pageObjects;

import java.util.List;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.AppiumFieldDecorator;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import utils.ReusableMethods;

public class CartPage extends ReusableMethods {

    public CartPage(AppiumDriver driver) {
        super(driver);
        PageFactory.initElements(new AppiumFieldDecorator(driver), this);
    }

    @AndroidFindBy(className = "android.widget.CheckBox")
    private WebElement checkBox;

    @AndroidFindBy(id = "com.androidsample.generalstore:id/productPrice")
    private List<WebElement> productPrices;

    @AndroidFindBy(id = "com.androidsample.generalstore:id/totalAmountLbl")
    private WebElement totalAmount;

    public  void clickCheckbox() {
        waitAndClick(checkBox);
    }

    /** Number of product rows currently in the cart. */
    public int getProductCount() {
        waitForElementsPresence(productPrices);
        return productPrices.size();
    }

    /** Sum of the individual product prices shown in the cart. */
    public double getProductPriceSum() {
        waitForElementsPresence(productPrices);
        double sum = 0;
        for (WebElement priceRow : productPrices) {
            sum += extractPrice(priceRow.getText());
        }
        return sum;
    }

    /** The total amount the app displays at the bottom of the cart. */
    public double getDisplayedTotal() {
        return extractPrice(getElementText(totalAmount));
    }
}
