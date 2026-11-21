package demo;

import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;



import demo.wrappers.Wrappers;

public class TestCases {
    ChromeDriver driver;

    private static final By SEARCH_BOX = By.xpath("//input[@name='q']");
    private static final By PRODUCT_CARD = By.xpath("//div[@data-id]");

    
    private void searchFor(String term) {
        Wrappers.navigateTo(driver, "https://www.flipkart.com");
        Wrappers.typeText(driver, SEARCH_BOX, term);
        Wrappers.pressEnter(driver, SEARCH_BOX);
        Wrappers.waitForUrlContains(driver, "search");
    }

    @Test
    public void testCase01() {
        
        searchFor("Washing Machine");
        Wrappers.clickElement(driver, By.xpath("//div[text()='Popularity']"));
        Wrappers.waitForUrlContains(driver, "popularity");

        
        Wrappers.pause(3000);

        
        int count = 0;
        int rated = 0;
        for (int attempt = 0; attempt < 3; attempt++) {
            try {
                count = 0;
                List<WebElement> ratings = Wrappers.findAllWithFallback(driver,
                        By.xpath("//span[starts-with(@id,'productRating')]/div"),
                        By.xpath("//div[contains(@class,'XQDdHH')]"));
                rated = ratings.size();
                for (WebElement rating : ratings) {
                    String text = rating.getText().trim();
                    if (!text.isEmpty() && Double.parseDouble(text) <= 4.0) {
                        count++;
                    }
                }
                break;
            } catch (StaleElementReferenceException e) {
                System.out.println("Page re-rendered, retrying (attempt " + (attempt + 1) + ")");
                Wrappers.pause(2000);
            }
        }
        System.out.println("Washing Machines with rating <= 4 stars: " + count
                + " (out of " + rated + " rated items)");
    }

    @Test
    public void testCase02() {
        searchFor("iPhone");

        
        By discountLocator = By.xpath("//*[contains(normalize-space(.),'% off') "
                + "and not(*[contains(normalize-space(.),'% off')])]");
        int matches = 0;
        for (int attempt = 0; attempt < 3; attempt++) {
            try {
                matches = 0;
                List<WebElement> labels = Wrappers.findAll(driver, discountLocator);
                System.out.println("Discount labels found on page: " + labels.size());
                for (WebElement label : labels) {
                    int discount = Wrappers.parseNumber(label.getText());
                    if (discount > 17) {
                        // Title lives in the same product card as the discount label
                        WebElement card = label.findElement(By.xpath("(./ancestor::div[@data-id] | ./ancestor::a)[last()]"));
                        String title = Wrappers.childText(card,
                                By.xpath(".//div[contains(@class,'KzDlHZ')]"),
                                By.xpath(".//a[@title]"),
                                By.xpath(".//div[string-length(normalize-space(.))>5 and not(*)][1]"));
                        System.out.println(title + " -> " + discount + "% off");
                        matches++;
                    }
                }
                break;
            } catch (StaleElementReferenceException e) {
                System.out.println("Page re-rendered, retrying (attempt " + (attempt + 1) + ")");
            }
        }
        System.out.println("iPhones with more than 17% discount: " + matches);
    }

    @Test
    public void testCase03() {
        searchFor("Coffee Mug");

        
        Wrappers.clickElement(driver, By.xpath("//div[contains(text(),'4') and contains(text(),'& above')]"));
        Wrappers.waitForUrlContains(driver, "rating");
        Wrappers.pause(3000);

        
        List<String[]> items = new ArrayList<String[]>();
        for (int attempt = 0; attempt < 3; attempt++) {
            try {
                items.clear();
                List<WebElement> cards = Wrappers.findAll(driver, PRODUCT_CARD);
                for (WebElement card : cards) {
                    String reviewText = Wrappers.childText(card,
                            By.xpath(".//span[contains(@class,'Wphh3N')]"),
                            By.xpath(".//span[starts-with(normalize-space(),'(')]"));
                    int reviews = Wrappers.parseNumber(reviewText);
                    if (reviews < 0) {
                        continue;
                    }
                    String title = Wrappers.childAttribute(card, "title", By.xpath(".//a[@title]"));
                    if (title.isEmpty()) {
                        title = Wrappers.childText(card, By.xpath(".//a[contains(@class,'wjcEIp')]"),
                                By.xpath(".//div[contains(@class,'KzDlHZ')]"));
                    }
                    String image = Wrappers.childAttribute(card, "src", By.xpath(".//img"));
                    
                    boolean duplicate = false;
                    for (String[] existing : items) {
                        if (existing[1].equals(title)) {
                            duplicate = true;
                        }
                    }
                    if (!duplicate) {
                        items.add(new String[] {String.valueOf(reviews), title, image});
                    }
                }
                break;
            } catch (StaleElementReferenceException e) {
                System.out.println("Page re-rendered, retrying (attempt " + (attempt + 1) + ")");
                Wrappers.pause(2000);
            }
        }

       
        items.sort((x, y) -> Integer.parseInt(y[0]) - Integer.parseInt(x[0]));
        for (int i = 0; i < Math.min(5, items.size()); i++) {
            System.out.println((i + 1) + ". " + items.get(i)[1] + " | reviews: " + items.get(i)[0]
                    + " | image: " + items.get(i)[2]);
        }
    }

    @BeforeTest
    public void startBrowser()
    {
        System.setProperty("java.util.logging.config.file", "logging.properties");

        ChromeOptions options = new ChromeOptions();
        LoggingPreferences logs = new LoggingPreferences();

        logs.enable(LogType.BROWSER, Level.ALL);
        logs.enable(LogType.DRIVER, Level.ALL);
        options.setCapability("goog:loggingPrefs", logs);
        options.addArguments("--remote-allow-origins=*");

        System.setProperty(ChromeDriverService.CHROME_DRIVER_LOG_PROPERTY, "build/chromedriver.log"); 

        driver = new ChromeDriver(options);

        driver.manage().window().maximize();
    }

    @AfterTest
    public void endTest()
    {
        driver.close();
        driver.quit();

    }
}