package com.hami.care.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;

import java.util.List;

public class DoctorApplicationsPage extends BasePage {
    private final Locator pageHeading;
    private final Locator searchInput;
    private final Locator applicationStatusSelect;
    private final Locator applyFiltersButton;
    private final Locator clearButton;
    private final Locator refreshButton;
    private final Locator tableHeaders;
    private final Locator tableRows;

    public DoctorApplicationsPage(Page page) {
        super(page);
        this.pageHeading = heading("Doctor Applications");
        this.searchInput = page.getByPlaceholder("Search doctors...");
        this.applicationStatusSelect = page.locator("select").first();
        this.applyFiltersButton = button("Apply filters");
        this.clearButton = button("Clear");
        this.refreshButton = button("Refresh");
        this.tableHeaders = page.locator("table thead th");
        this.tableRows = page.locator("table tbody tr");
    }

    public DoctorApplicationsPage open() {
        navigateTo("/admin/doctors/applications");
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

    public DoctorApplicationsPage searchDoctor(String name) {
        searchInput.clear();
        searchInput.fill(name);
        return this;
    }

    public DoctorApplicationsPage selectApplicationStatus(String statusLabel) {
        applicationStatusSelect.selectOption(statusLabel);
        return this;
    }

    public DoctorApplicationsPage applyFilters() {
        applyFiltersButton.click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
        return this;
    }

    public DoctorApplicationsPage clearFilters() {
        clearButton.click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
        return this;
    }

    public DoctorApplicationsPage refresh() {
        refreshButton.click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
        return this;
    }

    public String getSearchValue() {
        return searchInput.inputValue();
    }

    public void openFirstApplicationDetails() {
        tableRows.first().getByText("View Details").click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }
}
