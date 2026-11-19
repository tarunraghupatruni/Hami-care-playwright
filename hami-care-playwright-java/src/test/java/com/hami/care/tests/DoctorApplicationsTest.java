package com.hami.care.tests;

import com.hami.care.base.BaseTest;
import com.hami.care.pages.DoctorApplicationsPage;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

public class DoctorApplicationsTest extends BaseTest {
    private DoctorApplicationsPage doctorApplicationsPage;

    @BeforeMethod(alwaysRun = true)
    public void navigateToDoctorApplications() {
        loginAsAdmin();
        doctorApplicationsPage = new DoctorApplicationsPage(page).open();
    }

    @Test(priority = 1, description = "TC_DOC_01: Verify Doctor Applications page and table columns")
    public void verifyDoctorApplicationsColumns() {
        Assert.assertTrue(doctorApplicationsPage.isLoaded(), "Doctor Applications page should load");
        List<String> headers = doctorApplicationsPage.getTableHeaders();
        Assert.assertTrue(headers.containsAll(List.of("#", "DOCTOR", "GENDER", "SUBMITTED", "VERIFICATION", "ACTION")),
                "Doctor Applications table headers mismatch: " + headers);
    }

    @Test(priority = 2, description = "TC_DOC_03: Search doctor applications and clear filters")
    public void verifySearchAndClearDoctorApplications() {
        doctorApplicationsPage.searchDoctor("Dr").applyFilters();
        doctorApplicationsPage.clearFilters();
        Assert.assertEquals(doctorApplicationsPage.getSearchValue(), "", "Search input should clear");
    }

    @Test(priority = 3, description = "TC_DOC_04: Refresh doctor applications list")
    public void verifyRefreshDoctorApplications() {
        doctorApplicationsPage.refresh();
        Assert.assertTrue(doctorApplicationsPage.isLoaded(), "Page should remain loaded after Refresh");
    }

    @Test(priority = 4, description = "TC_DOC_05: Open Doctor Application details")
    public void verifyViewDoctorApplicationDetails() {
        if (doctorApplicationsPage.getRowCount() > 0) {
            doctorApplicationsPage.openFirstApplicationDetails();
            Assert.assertFalse(doctorApplicationsPage.getCurrentUrl().endsWith("/applications"),
                    "Should navigate or open details for the selected doctor application");
        }
    }
}
