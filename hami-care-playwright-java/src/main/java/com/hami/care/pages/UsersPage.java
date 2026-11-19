package com.hami.care.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;

import java.util.List;

public class UsersPage extends BasePage {
    private final Locator pageHeading;
    private final Locator searchInput;
    private final Locator statusInput;
    private final Locator applyFiltersButton;
    private final Locator clearButton;
    private final Locator tableHeaders;
    private final Locator tableRows;

    // User Profile page (/admin/users/<id>)
    private final Locator profileHeading;
    private final Locator backToUsersButton;
    private final Locator accountStatusSelect;
    private final Locator saveStatusChangesButton;

    public UsersPage(Page page) {
        super(page);
        this.pageHeading = heading("Users");
        this.searchInput = page.locator("input[placeholder*='Name, identifier']");
        this.statusInput = page.getByPlaceholder("Exact status");
        this.applyFiltersButton = button("Apply filters");
        this.clearButton = button("Clear");
        this.tableHeaders = page.locator("table thead th");
        this.tableRows = page.locator("table tbody tr");

        this.profileHeading = heading("User Profile");
        this.backToUsersButton = button("Back to Users");
        this.accountStatusSelect = page.locator("select").first();
        this.saveStatusChangesButton = button("Save Status Changes");
    }

    public UsersPage open() {
        navigateTo("/admin/users");
        return this;
    }

    public boolean isLoaded() {
        return pageHeading.isVisible();
    }

    public List<String> getTableHeaders() {
        return tableHeaders.allInnerTexts().stream().map(String::trim).toList();
    }

    public int getRowCount() {
        return tableRows.count();
    }

    public UsersPage searchUser(String query) {
        searchInput.clear();
        searchInput.fill(query);
        return this;
    }

    public UsersPage filterByStatus(String status) {
        statusInput.clear();
        statusInput.fill(status);
        return this;
    }

    public UsersPage applyFilters() {
        applyFiltersButton.click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
        return this;
    }

    public UsersPage clearFilters() {
        clearButton.click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
        return this;
    }

    public String getSearchInputValue() {
        return searchInput.inputValue();
    }

    public String getFirstRowEmail() {
        return tableRows.first().locator("td").nth(2).innerText().trim();
    }

    public UsersPage openFirstUserDetails() {
        tableRows.first().getByText("View details").click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
        return this;
    }

    public boolean isUserProfileDisplayed() {
        return profileHeading.isVisible() && backToUsersButton.isVisible();
    }

    public boolean hasPersonalInformationSection() {
        return page.getByText("Personal Information").isVisible()
                && page.getByText("Linked Children & Elders").isVisible()
                && page.getByText("Account Management").isVisible();
    }

    public String getSelectedAccountStatus() {
        return accountStatusSelect.inputValue();
    }

    public UsersPage updateAccountStatus(String statusLabel) {
        accountStatusSelect.selectOption(statusLabel);
        saveStatusChangesButton.click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
        return this;
    }

    public UsersPage backToUsers() {
        backToUsersButton.click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
        return this;
    }
}
