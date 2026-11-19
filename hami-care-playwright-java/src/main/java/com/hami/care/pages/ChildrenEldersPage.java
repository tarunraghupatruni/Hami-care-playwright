package com.hami.care.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;

import java.util.List;

public class ChildrenEldersPage extends BasePage {
    private final Locator pageHeading;
    private final Locator searchInput;
    private final Locator genderSelect;
    private final Locator patientTypeSelect;
    private final Locator applyFiltersButton;
    private final Locator clearButton;
    private final Locator tableHeaders;
    private final Locator tableRows;

    public ChildrenEldersPage(Page page) {
        super(page);
        this.pageHeading = heading("Patient Management");
        this.searchInput = page.getByPlaceholder("Search patients...");
        this.genderSelect = page.locator("select").nth(0);
        this.patientTypeSelect = page.locator("select").nth(1);
        this.applyFiltersButton = button("Apply filters");
        this.clearButton = button("Clear");
        this.tableHeaders = page.locator("table thead th");
        this.tableRows = page.locator("table tbody tr");
    }

    public ChildrenEldersPage open() {
        navigateTo("/admin/children");
        return this;
    }

    public boolean isLoaded() {
        return pageHeading.isVisible();
    }

    public boolean areSummaryCardsVisible() {
        return page.getByText("Total Patients").isVisible()
                && page.getByText("Male Patients").isVisible()
                && page.getByText("Female Patients").isVisible()
                && page.getByText("Other / Unspecified").isVisible();
    }

    public List<String> getTableHeaders() {
        return tableHeaders.allInnerTexts().stream().map(String::trim).toList();
    }

    public int getRowCount() {
        return tableRows.count();
    }

    public ChildrenEldersPage searchPatient(String name) {
        searchInput.clear();
        searchInput.fill(name);
        return this;
    }

    public ChildrenEldersPage selectGender(String genderLabel) {
        genderSelect.selectOption(genderLabel);
        return this;
    }

    public ChildrenEldersPage selectPatientType(String typeLabel) {
        patientTypeSelect.selectOption(typeLabel);
        return this;
    }

    public ChildrenEldersPage applyFilters() {
        applyFiltersButton.click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
        return this;
    }

    public ChildrenEldersPage clearFilters() {
        clearButton.click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
        return this;
    }

    public String getFirstRowPatientName() {
        return tableRows.first().locator("td").first().innerText().trim();
    }

    public void openFirstPatientDetails() {
        tableRows.first().getByText("View Details").click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }
}
