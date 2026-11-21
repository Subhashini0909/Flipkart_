package demo.wrappers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class Wrappers {
    private static final Duration TIMEOUT = Duration.ofSeconds(15);

    
    public static void navigateTo(WebDriver driver, String url) {
        driver.get(url);
    }

    
    public static void typeText(WebDriver driver, By locator, String text) {
        WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);
        WebElement input = wait.until(ExpectedConditions.elementToBeClickable(locator));
        input.clear();
        input.sendKeys(text);
    }

    
    public static void pressEnter(WebDriver driver, By locator) {
        WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);
        wait.until(ExpectedConditions.elementToBeClickable(locator)).sendKeys(Keys.ENTER);
    }

    
    public static void clickElement(WebDriver driver, By locator) {
        WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);
        WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        try {
            wait.until(ExpectedConditions.elementToBeClickable(element)).click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        }
    }

    
    public static List<WebElement> findAll(WebDriver driver, By locator) {
        WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);
        return wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(locator));
    }

    
    public static List<WebElement> findAllWithFallback(WebDriver driver, By... locators) {
        WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);
        for (By locator : locators) {
            try {
                return wait.withTimeout(Duration.ofSeconds(6))
                        .until(ExpectedConditions.presenceOfAllElementsLocatedBy(locator));
            } catch (Exception e) {
                // try the next locator
            }
        }
        return new ArrayList<WebElement>();
    }

   
    public static String childText(WebElement parent, By... locators) {
        for (By locator : locators) {
            List<WebElement> found = parent.findElements(locator);
            if (!found.isEmpty() && !found.get(0).getText().trim().isEmpty()) {
                return found.get(0).getText().trim();
            }
        }
        return "";
    }

   
    public static String childAttribute(WebElement parent, String attribute, By... locators) {
        for (By locator : locators) {
            List<WebElement> found = parent.findElements(locator);
            if (!found.isEmpty()) {
                String value = found.get(0).getAttribute(attribute);
                if (value != null && !value.trim().isEmpty()) {
                    return value.trim();
                }
            }
        }
        return "";
    }

    
    public static int parseNumber(String text) {
        String digits = text.replace(",", "").replaceAll("[^0-9 ]", " ").trim().split("\\s+")[0];
        return digits.isEmpty() ? -1 : Integer.parseInt(digits);
    }

   
    public static void waitForUrlContains(WebDriver driver, String fragment) {
        new WebDriverWait(driver, TIMEOUT).until(ExpectedConditions.urlContains(fragment));
    }

  
    public static void pause(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}