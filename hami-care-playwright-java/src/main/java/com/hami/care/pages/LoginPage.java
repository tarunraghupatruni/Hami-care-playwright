package com.hami.care.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.LoadState;

public class LoginPage extends BasePage {
    private final Locator welcomeHeading;
    private final Locator emailInput;
    private final Locator passwordInput;
    private final Locator signInButton;
    private final Locator forgotPasswordLink;
    private final Locator passwordVisibilityToggle;

    public LoginPage(Page page) {
        super(page);
        this.welcomeHeading = heading("Welcome to Hami");
        this.emailInput = page.getByPlaceholder("you@example.com");
        this.passwordInput = page.getByPlaceholder("Enter your password");
        this.signInButton = button("Sign in");
        this.forgotPasswordLink = page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Forgot password?"));
        this.passwordVisibilityToggle = passwordInput.locator("xpath=following-sibling::button[1]");
    }

    public LoginPage open() {
        navigateTo("/auth/login");
        return this;
    }

    public boolean isLoginPageDisplayed() {
        return welcomeHeading.isVisible()
                && emailInput.isVisible()
                && passwordInput.isVisible()
                && signInButton.isVisible()
                && forgotPasswordLink.isVisible();
    }

    public LoginPage enterEmail(String email) {
        emailInput.clear();
        emailInput.fill(email);
        return this;
    }

    public LoginPage enterPassword(String password) {
        passwordInput.clear();
        passwordInput.fill(password);
        return this;
    }

    public void clickSignIn() {
        signInButton.click();
    }

    public void loginAs(String email, String password) {
        enterEmail(email);
        enterPassword(password);
        clickSignIn();
        page.waitForURL("**/admin/**");
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    public void attemptLogin(String email, String password) {
        enterEmail(email);
        enterPassword(password);
        clickSignIn();
        page.waitForTimeout(1000);
    }

    public void togglePasswordVisibility() {
        passwordVisibilityToggle.click();
    }

    public String getPasswordInputType() {
        return passwordInput.getAttribute("type");
    }

    public boolean isEmailValidationInvalid() {
        return Boolean.TRUE.equals(emailInput.evaluate("el => !el.checkValidity()"));
    }
}
