package com.hami.care.base;

import com.hami.care.config.ConfigReader;
import com.hami.care.pages.LoginPage;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.nio.file.Paths;

public abstract class BaseTest {
    protected Playwright playwright;
    protected Browser browser;
    protected BrowserContext context;
    protected Page page;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        playwright = Playwright.create();
        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                .setHeadless(ConfigReader.getBoolean("headless"));

        String channel = ConfigReader.get("browserChannel");
        if (channel != null && !channel.isBlank()) {
            launchOptions.setChannel(channel.trim());
        }

        browser = playwright.chromium().launch(launchOptions);
        context = browser.newContext(new Browser.NewContextOptions().setViewportSize(1440, 900));
        page = context.newPage();
        page.setDefaultTimeout(ConfigReader.getDouble("timeoutMs"));
    }

    protected void loginAsAdmin() {
        new LoginPage(page)
                .open()
                .loginAs(ConfigReader.get("adminEmail"), ConfigReader.get("adminPassword"));
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        if (page != null && result.getStatus() == ITestResult.FAILURE) {
            page.screenshot(new Page.ScreenshotOptions()
                    .setPath(Paths.get("target/screenshots/" + result.getName() + ".png"))
                    .setFullPage(true));
        }
        if (context != null) {
            context.close();
        }
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }
}
