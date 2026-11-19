package com.hami.care.tests;

import com.hami.care.base.BaseTest;
import com.hami.care.pages.ChildrenEldersPage;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

public class ChildrenEldersTest extends BaseTest {
    private ChildrenEldersPage childrenEldersPage;

    @BeforeMethod(alwaysRun = true)
    public void navigateToChildrenElders() {
        loginAsAdmin();
        childrenEldersPage = new ChildrenEldersPage(page).open();
    }

    @Test(priority = 1, description = "TC_PAT_01: Verify Patient Management summary cards and table")
    public void verifyPatientManagementOverview() {
        Assert.assertTrue(childrenEldersPage.isLoaded(), "Patient Management page should load");
        Assert.assertTrue(childrenEldersPage.areSummaryCardsVisible(), "Summary cards should be visible");
        List<String> headers = childrenEldersPage.getTableHeaders();
        Assert.assertTrue(headers.containsAll(List.of("CHILD", "GENDER", "PATIENT TYPE", "DATE OF BIRTH", "AGE", "ACTION")),
                "Patient table headers mismatch: " + headers);
    }

    @Test(priority = 2, description = "TC_PAT_03: Search patients and clear filters")
    public void verifySearchAndClearPatients() {
        Assert.assertTrue(childrenEldersPage.getRowCount() > 0, "Expected at least one patient row");
        String firstPatient = childrenEldersPage.getFirstRowPatientName();

        childrenEldersPage.searchPatient(firstPatient).applyFilters();
        Assert.assertTrue(childrenEldersPage.getRowCount() >= 1, "Filtered patient should be listed");

        childrenEldersPage.clearFilters();
        Assert.assertTrue(childrenEldersPage.getRowCount() >= 1, "Patient list should restore after Clear");
    }

    @Test(priority = 3, description = "TC_PAT_04: View patient details")
    public void verifyViewPatientDetails() {
        Assert.assertTrue(childrenEldersPage.getRowCount() > 0, "Expected at least one patient row");
        childrenEldersPage.openFirstPatientDetails();
        Assert.assertTrue(childrenEldersPage.getCurrentUrl().contains("/admin/children/"),
                "Should navigate to patient detail page");
    }
}
