package com.hami.care.tests;

import com.hami.care.base.BaseTest;
import com.hami.care.pages.SubscriptionPlansPage;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;

public class SubscriptionPlansTest extends BaseTest {
    private SubscriptionPlansPage subscriptionsPage;

    @BeforeMethod(alwaysRun = true)
    public void navigateToSubscriptions() {
        loginAsAdmin();
        subscriptionsPage = new SubscriptionPlansPage(page).open();
    }

    private String uniqueSuffix() {
        return String.valueOf(System.currentTimeMillis()).substring(7);
    }

    // ==================== POSITIVE TEST CASES ====================

    @Test(priority = 1, description = "TC_SUB_01: Verify Subscription Management page and columns")
    public void verifySubscriptionManagementPage() {
        Assert.assertTrue(subscriptionsPage.isLoaded(), "Subscription Management page should load");
        Assert.assertTrue(subscriptionsPage.areSummaryCardsVisible(), "Summary cards should be visible");
        List<String> headers = subscriptionsPage.getTableHeaders();
        Assert.assertTrue(headers.containsAll(List.of(
                "#", "PLAN DETAILS", "PRICING", "DURATION", "MAX CHILDREN", "FEATURES", "STATUS", "POPULAR", "ACTIONS"
        )), "Subscription table headers mismatch: " + headers);
    }

    @Test(priority = 2, description = "TC_SUB_02 & TC_SUB_04: Create Active+Popular plan, verify listing, and edit values")
    public void verifyCreateAndEditActivePopularPlan() {
        String suffix = uniqueSuffix();
        String createdPlanName = "AutoPlan-" + suffix;
        String editedPlanName = "AutoPlanEdited-" + suffix;

        subscriptionsPage.clickAddSubscription()
                .fillCreatePlanForm(
                        createdPlanName, "1499", "999", "3", "Months",
                        "4", "10", "Active", "Automated QA subscription plan",
                        true, List.of("Hami Signal", "Milestones", "Health Records")
                )
                .saveNewSubscription();

        subscriptionsPage.reload();
        Assert.assertTrue(subscriptionsPage.isPlanPresent(createdPlanName),
                "Newly created plan should appear in All Subscription Plans");

        String createdRow = subscriptionsPage.getPlanRowText(createdPlanName);
        Assert.assertTrue(createdRow.contains("999.00") && createdRow.contains("1,499.00"),
                "Created plan row should display formatted discount & original pricing: " + createdRow);
        Assert.assertTrue(createdRow.contains("3 Months"), "Expected '3 Months': " + createdRow);
        Assert.assertTrue(createdRow.contains("4"), "Expected Max Children=4: " + createdRow);
        Assert.assertTrue(createdRow.contains("Active"), "Expected Active status: " + createdRow);
        Assert.assertTrue(createdRow.contains("Popular"), "Expected Popular badge: " + createdRow);

        // Edit the created plan
        subscriptionsPage.openEditModalForPlan(createdPlanName);
        Assert.assertTrue(subscriptionsPage.isEditModalVisible(), "Edit Subscription modal should open");

        subscriptionsPage.updatePlanInModal(
                        editedPlanName, "1999", "1299", "6", "5",
                        "Active", true, List.of("Timeline")
                )
                .saveEditChanges();

        subscriptionsPage.reload();
        Assert.assertTrue(subscriptionsPage.isPlanPresent(editedPlanName),
                "Edited plan name should persist after reload");
        String editedRow = subscriptionsPage.getPlanRowText(editedPlanName);
        Assert.assertTrue(editedRow.contains("1,299.00") && editedRow.contains("1,999.00"),
                "Edited row should display updated pricing: " + editedRow);
        Assert.assertTrue(editedRow.contains("6 Months"), "Expected '6 Months': " + editedRow);
        Assert.assertTrue(editedRow.contains("5"), "Expected Max Children=5: " + editedRow);
    }

    @Test(priority = 3, description = "TC_SUB_03: Create Inactive + Non-Popular plan with zero discount")
    public void verifyCreateInactiveNonPopularPlanWithZeroDiscount() {
        String planName = "InactivePlan-" + uniqueSuffix();

        subscriptionsPage.clickAddSubscription()
                .fillCreatePlanForm(
                        planName, "499", "0", "1", "Months",
                        "2", "5", "Inactive", "Inactive zero-discount test plan",
                        false, Collections.emptyList()
                )
                .saveNewSubscription();

        subscriptionsPage.reload();
        Assert.assertTrue(subscriptionsPage.isPlanPresent(planName), "Inactive plan should be listed");
        String rowText = subscriptionsPage.getPlanRowText(planName);
        Assert.assertTrue(rowText.contains("499.00"), "Expected base price 499.00: " + rowText);
        Assert.assertTrue(rowText.contains("Inactive"), "Expected Inactive status: " + rowText);
        Assert.assertFalse(rowText.contains("★ Popular"), "Non-popular plan should not have Popular badge");
    }

    @Test(priority = 4, description = "TC_SUB_05: Search and filter subscription plans by keyword and status")
    public void verifySearchAndFilterPlans() {
        String firstPlan = subscriptionsPage.getFirstPlanName();
        subscriptionsPage.searchPlan(firstPlan)
                .filterByStatus("Active")
                .applyFilters();
        Assert.assertTrue(subscriptionsPage.getRowCount() >= 1, "Filtered plan should appear");
        subscriptionsPage.clearFilters();
        Assert.assertTrue(subscriptionsPage.getRowCount() >= 1, "Full plan list should restore after Clear");
    }

    // ==================== NEGATIVE TEST CASES ====================

    @Test(priority = 5, description = "TC_SUB_06: Create plan fails when required fields are empty")
    public void verifyCreatePlanEmptyRequiredFieldsBlocked() {
        subscriptionsPage.clickAddSubscription()
                .fillCreatePlanForm(
                        "", "", "", "", "Months",
                        "1", "0", "Active", "", false, Collections.emptyList()
                )
                .attemptSaveNewSubscription();

        Assert.assertTrue(subscriptionsPage.isOnAddSubscriptionPage(),
                "Should remain on /admin/subscriptions/add when required fields are empty");
        Assert.assertTrue(subscriptionsPage.isCreateFieldInvalid("Plan Name"),
                "Plan Name should fail required validation");
    }

    @Test(priority = 6, description = "TC_SUB_07: Create plan rejects negative Price")
    public void verifyCreatePlanNegativePriceRejected() {
        String planName = "NegPrice-" + uniqueSuffix();
        subscriptionsPage.clickAddSubscription()
                .fillCreatePlanForm(
                        planName, "-100", "0", "1", "Months",
                        "1", "0", "Active", "Negative price test",
                        false, Collections.emptyList()
                )
                .attemptSaveNewSubscription();

        Assert.assertTrue(subscriptionsPage.isOnAddSubscriptionPage(),
                "Should not save plan with negative price");
    }

    @Test(priority = 7, description = "TC_SUB_08: Create plan rejects Discount Price greater than Price")
    public void verifyCreatePlanDiscountExceedingPriceRejected() {
        String planName = "BadDiscount-" + uniqueSuffix();
        subscriptionsPage.clickAddSubscription()
                .fillCreatePlanForm(
                        planName, "500", "1000", "1", "Months",
                        "1", "0", "Active", "Discount greater than price",
                        false, Collections.emptyList()
                )
                .attemptSaveNewSubscription();

        Assert.assertTrue(subscriptionsPage.isOnAddSubscriptionPage(),
                "Should not save plan when discount price exceeds base price");
    }

    @Test(priority = 8, description = "TC_SUB_09: Create plan rejects zero Duration")
    public void verifyCreatePlanZeroDurationRejected() {
        String planName = "ZeroDur-" + uniqueSuffix();
        subscriptionsPage.clickAddSubscription()
                .fillCreatePlanForm(
                        planName, "500", "0", "0", "Months",
                        "1", "0", "Active", "Zero duration test",
                        false, Collections.emptyList()
                )
                .attemptSaveNewSubscription();

        Assert.assertTrue(subscriptionsPage.isOnAddSubscriptionPage(),
                "Should not save plan with duration = 0");
    }

    @Test(priority = 9, description = "TC_SUB_10: Edit plan rejects empty Plan Name")
    public void verifyEditPlanEmptyNameRejected() {
        String firstPlan = subscriptionsPage.getFirstPlanName();
        subscriptionsPage.openEditModalForPlan(firstPlan)
                .updatePlanInModal("", "999", "0", "1", "1", "Active", null, Collections.emptyList())
                .attemptSaveEditChanges();

        Assert.assertTrue(subscriptionsPage.isEditModalVisible(),
                "Edit modal should remain open when Plan Name is empty");
        subscriptionsPage.cancelEditModal();
        Assert.assertTrue(subscriptionsPage.isPlanPresent(firstPlan),
                "Original plan should remain intact");
    }

    @Test(priority = 10, description = "TC_SUB_11: Cancel Create and Cancel Edit do not persist changes")
    public void verifyCancelCreateAndCancelEdit() {
        String unsavedName = "UnsavedCancel-" + uniqueSuffix();
        subscriptionsPage.clickAddSubscription()
                .fillCreatePlanForm(
                        unsavedName, "799", "0", "2", "Months",
                        "2", "1", "Active", "Should be cancelled",
                        false, Collections.emptyList()
                )
                .cancelCreateSubscription();
        Assert.assertFalse(subscriptionsPage.isPlanPresent(unsavedName),
                "Cancelled plan should not be created");

        String firstPlan = subscriptionsPage.getFirstPlanName();
        subscriptionsPage.openEditModalForPlan(firstPlan)
                .updatePlanInModal("TempEdited-" + uniqueSuffix(), "999", "0", "1", "1", "Active", null, Collections.emptyList())
                .cancelEditModal();
        Assert.assertTrue(subscriptionsPage.isPlanPresent(firstPlan),
                "Original plan should remain unchanged after Cancel");
    }

    // ==================== EDGE TEST CASES ====================

    @Test(priority = 11, description = "TC_SUB_12: Boundary minimum valid values (Price=1, Discount=0, Duration=1, Max=1)")
    public void verifyCreatePlanBoundaryMinimumValues() {
        String planName = "MinBoundary-" + uniqueSuffix();
        subscriptionsPage.clickAddSubscription()
                .fillCreatePlanForm(
                        planName, "1", "0", "1", "Months",
                        "1", "0", "Active", "Minimum boundary test",
                        false, Collections.emptyList()
                )
                .saveNewSubscription();

        subscriptionsPage.reload();
        Assert.assertTrue(subscriptionsPage.isPlanPresent(planName), "Minimum boundary plan should be created");
        String rowText = subscriptionsPage.getPlanRowText(planName);
        Assert.assertTrue(rowText.contains("1.00"), "Expected price 1.00: " + rowText);
    }

    @Test(priority = 12, description = "TC_SUB_13: Special characters and long description handling")
    public void verifySpecialCharactersAndLongDescription() {
        String planName = "QA Plan & Plus (2026) #" + uniqueSuffix();
        String longDesc = "Automated long description validation ".repeat(7).trim();

        subscriptionsPage.clickAddSubscription()
                .fillCreatePlanForm(
                        planName, "2499", "1999", "12", "Months",
                        "6", "20", "Active", longDesc,
                        true, List.of("Hami Signal")
                )
                .saveNewSubscription();

        subscriptionsPage.reload();
        Assert.assertTrue(subscriptionsPage.isPlanPresent(planName),
                "Plan with special characters and long description should be created and displayed");
    }

    @Test(priority = 13, description = "TC_SUB_14: Duplicate Plan Name handling")
    public void verifyDuplicatePlanNameHandling() {
        String existingPlan = subscriptionsPage.getFirstPlanName();
        subscriptionsPage.clickAddSubscription()
                .fillCreatePlanForm(
                        existingPlan, "999", "0", "1", "Months",
                        "1", "1", "Active", "Duplicate plan name test",
                        false, Collections.emptyList()
                )
                .attemptSaveNewSubscription();

        Assert.assertTrue(subscriptionsPage.isOnAddSubscriptionPage() || subscriptionsPage.isPlanPresent(existingPlan),
                "Duplicate plan name should either be rejected on form or handled gracefully");
    }

    @Test(priority = 14, description = "TC_SUB_15: Toggle Status (Active <-> Inactive) and Popular flag during Edit")
    public void verifyToggleStatusAndPopularInEdit() {
        String planName = "TogglePlan-" + uniqueSuffix();
        subscriptionsPage.clickAddSubscription()
                .fillCreatePlanForm(
                        planName, "1200", "900", "3", "Months",
                        "3", "5", "Active", "Toggle test plan",
                        true, List.of("Hami Signal")
                )
                .saveNewSubscription();

        subscriptionsPage.openEditModalForPlan(planName)
                .updatePlanInModal(planName, "1200", "900", "3", "3", "Inactive", false, Collections.emptyList())
                .saveEditChanges();

        subscriptionsPage.reload();
        String updatedRow = subscriptionsPage.getPlanRowText(planName);
        Assert.assertTrue(updatedRow.contains("Inactive"), "Status should update to Inactive: " + updatedRow);
        Assert.assertFalse(updatedRow.contains("★ Popular"), "Popular badge should be removed: " + updatedRow);
    }
}
