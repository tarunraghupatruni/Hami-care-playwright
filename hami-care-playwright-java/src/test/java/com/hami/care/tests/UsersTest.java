package com.hami.care.tests;

import com.hami.care.base.BaseTest;
import com.hami.care.pages.UsersPage;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

public class UsersTest extends BaseTest {
    private UsersPage usersPage;

    @BeforeMethod(alwaysRun = true)
    public void navigateToUsers() {
        loginAsAdmin();
        usersPage = new UsersPage(page).open();
    }

    @Test(priority = 1, description = "TC_USERS_01: Verify Users directory page and table columns")
    public void verifyUsersDirectoryAndColumns() {
        Assert.assertTrue(usersPage.isLoaded(), "Users page should be loaded");
        List<String> headers = usersPage.getTableHeaders();
        Assert.assertTrue(headers.containsAll(List.of("S.NO.", "NAME", "EMAIL", "MOBILE", "STATUS", "JOINED", "DETAILS")),
                "Users table headers mismatch: " + headers);
    }

    @Test(priority = 2, description = "TC_USERS_02 & TC_USERS_03: Search user and clear filters")
    public void verifySearchAndClearUsers() {
        Assert.assertTrue(usersPage.getRowCount() > 0, "Expected at least one user row");
        String targetEmail = usersPage.getFirstRowEmail();

        usersPage.searchUser(targetEmail).applyFilters();
        Assert.assertTrue(usersPage.getRowCount() >= 1, "Filtered user should appear");
        Assert.assertEquals(usersPage.getFirstRowEmail(), targetEmail);

        usersPage.clearFilters();
        Assert.assertEquals(usersPage.getSearchInputValue(), "", "Search input should reset after Clear");
    }

    @Test(priority = 3, description = "TC_USERS_04: View User Profile details")
    public void verifyUserProfileDetails() {
        usersPage.openFirstUserDetails();
        Assert.assertTrue(usersPage.isUserProfileDisplayed(), "User Profile page should open");
        Assert.assertTrue(usersPage.hasPersonalInformationSection(),
                "Personal Information, Linked Children & Elders, and Account Management sections should be visible");
    }
}
