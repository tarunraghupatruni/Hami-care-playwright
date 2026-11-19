package com.hami.care.tests;

import com.hami.care.base.BaseTest;
import com.hami.care.config.ConfigReader;
import com.hami.care.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class LoginTest extends BaseTest {

    @Test(priority = 1, description = "TC_LOGIN_01: Verify login page UI elements")
    public void verifyLoginPageElements() {
        LoginPage loginPage = new LoginPage(page).open();
        Assert.assertTrue(loginPage.isLoginPageDisplayed(), "Login page elements should be visible");
    }

    @Test(priority = 2, description = "TC_LOGIN_02: Successful login with valid admin credentials")
    public void verifyValidAdminLogin() {
        LoginPage loginPage = new LoginPage(page).open();
        loginPage.loginAs(ConfigReader.get("adminEmail"), ConfigReader.get("adminPassword"));
        Assert.assertTrue(loginPage.getCurrentUrl().contains("/admin/dashboard"),
                "User should be redirected to /admin/dashboard after login");
    }

    @Test(priority = 3, description = "TC_LOGIN_03: Login fails with invalid password")
    public void verifyInvalidPasswordLogin() {
        LoginPage loginPage = new LoginPage(page).open();
        loginPage.attemptLogin(ConfigReader.get("adminEmail"), "WrongPass@123");
        Assert.assertTrue(loginPage.getCurrentUrl().contains("/auth/login"),
                "URL should remain on /auth/login after failed login");
    }

    @Test(priority = 4, description = "TC_LOGIN_04: Validation on empty email and password")
    public void verifyEmptyCredentialsValidation() {
        LoginPage loginPage = new LoginPage(page).open();
        loginPage.clickSignIn();
        Assert.assertTrue(loginPage.isEmailValidationInvalid(), "Email field should fail HTML5 validation");
        Assert.assertTrue(loginPage.getCurrentUrl().contains("/auth/login"),
                "URL should remain on /auth/login when fields are empty");
    }

    @Test(priority = 5, description = "TC_LOGIN_05: Password visibility toggle")
    public void verifyPasswordVisibilityToggle() {
        LoginPage loginPage = new LoginPage(page).open();
        loginPage.enterPassword(ConfigReader.get("adminPassword"));
        Assert.assertEquals(loginPage.getPasswordInputType(), "password");
        loginPage.togglePasswordVisibility();
        Assert.assertEquals(loginPage.getPasswordInputType(), "text");
    }
}
