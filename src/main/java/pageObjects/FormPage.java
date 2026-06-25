package pageObjects;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.AppiumFieldDecorator;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import utils.ReusableMethods;

public class FormPage extends ReusableMethods {

    public FormPage(AppiumDriver driver) {
        super(driver);
        PageFactory.initElements(new AppiumFieldDecorator(driver), this);
    }

    @AndroidFindBy(id = "com.androidsample.generalstore:id/nameField")
    @iOSXCUITFindBy(id = "iosID")
    private WebElement nameField;

    @AndroidFindBy(xpath = "//android.widget.RadioButton[@text='Female']")
    private WebElement femaleOption;

    @AndroidFindBy(xpath = "//android.widget.RadioButton[@text='Male']")
    private WebElement maleOption;

    @AndroidFindBy(id = "android:id/text1")
    private WebElement countryDD;

    @AndroidFindBy(id = "com.androidsample.generalstore:id/btnLetsShop")
    private  WebElement btnLetsShop;

    public void setNameField(String name){
        sendKeysWithWait(nameField, name);
        hideKeyboard();
    }

    public void setGender(String gender){
        if(gender.contains("Male")){
            waitAndClick(femaleOption);
        }else if(gender.contains("Female")){
            waitAndClick(maleOption);

        }
    }

    public void setCountryDD(String countryName){
        countryDD.click();
        scrollToText(countryName);
        driver.findElement(By.xpath("//android.widget.TextView[@text='"+countryName+"']")).click();

    }

    public ProductCataloguePage submitForm(){
        btnLetsShop.click();
        return new ProductCataloguePage(driver);
    }

}
