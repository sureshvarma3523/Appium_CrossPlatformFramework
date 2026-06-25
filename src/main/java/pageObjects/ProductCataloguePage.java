package pageObjects;

import java.util.List;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;

import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.AppiumFieldDecorator;
import utils.ReusableMethods;

public class ProductCataloguePage extends ReusableMethods {

    public ProductCataloguePage(AppiumDriver driver) {
        super(driver);
        PageFactory.initElements(new AppiumFieldDecorator(driver), this);
    }

    @AndroidFindBy(xpath="//android.widget.TextView[@text='ADD TO CART']")
    private List<WebElement> addToCart;
    //driver.findElements(By.xpath("//android.widget.TextView[@text='ADD TO CART']"))
    @AndroidFindBy(id="com.androidsample.generalstore:id/appbar_btn_cart")
    private WebElement cart;




    public void addItemToCartByIndex(int index) {
        waitForElementsPresence(addToCart);
        addToCart.get(index).click();
    }

    public CartPage gotoCart() {
        waitAndClick(cart);
        waitForSeconds(2);
        return new CartPage(driver);
    }


}
