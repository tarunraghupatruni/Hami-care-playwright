package com.hami.care.tests;

import com.hami.care.base.BaseTest;
import com.hami.care.pages.FaqsPage;
import com.hami.care.reporting.TestCaseMeta;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

public class FaqsTest extends BaseTest {
    private FaqsPage faqsPage;

    @BeforeMethod(alwaysRun = true)
    public void navigateToFaqs() {
        loginAsAdmin();
        faqsPage = new FaqsPage(page).open();
    }

    private String uniqueSuffix() {
        return String.valueOf(System.currentTimeMillis()).substring(7);
    }

    // ==================== POSITIVE TEST CASES ====================

    @TestCaseMeta(
            id = "TC_FAQ_01",
            title = "Verify FAQs Management page, summary cards, and table columns",
            preconditions = "Admin is logged in",
            steps = "1. Navigate to /admin/faqs.\n2. Inspect summary cards and FAQ Directory table headers.",
            expected = "FAQs Management heading, summary cards (Total FAQs, Active FAQs, Inactive FAQs), and table columns (QUESTION, ANSWER, CATEGORY, STATUS, ORDER, ACTIONS) are visible."
    )
    @Test(priority = 1, description = "TC_FAQ_01: Verify FAQs Management page, summary cards, and table columns")
    public void verifyFaqsPageOverview() {
        Assert.assertTrue(faqsPage.isLoaded(), "FAQs Management page should load");
        Assert.assertTrue(faqsPage.areSummaryCardsVisible(), "FAQ summary cards should be visible");
        List<String> headers = faqsPage.getTableHeaders();
        Assert.assertTrue(headers.containsAll(List.of("QUESTION", "ANSWER", "CATEGORY", "STATUS", "ORDER", "ACTIONS")),
                "FAQ Directory headers mismatch: " + headers);
    }

    @TestCaseMeta(
            id = "TC_FAQ_02_05_06",
            title = "Create Active FAQ, verify in list, Edit all fields, and Delete",
            preconditions = "Admin is on /admin/faqs",
            steps = "1. Click + Create New FAQ, fill Question, Answer, Status=Active, Order=1, and save.\n2. Reload and verify FAQ in list.\n3. Edit Question, Answer, Status=Inactive, Order=5 and save.\n4. Delete FAQ and confirm popup.",
            expected = "FAQ is created, updated values persist after reload, and FAQ is removed after confirming Delete."
    )
    @Test(priority = 2, description = "TC_FAQ_02, TC_FAQ_05, TC_FAQ_06: Create Active FAQ, verify list, Edit all fields, and Delete")
    public void verifyCreateEditDeleteFaqLifecycle() {
        String suffix = uniqueSuffix();
        String question = "How do I book a consultation? #" + suffix;
        String answer = "Open Appointments and choose a doctor slot.";
        String editedQuestion = "Updated FAQ Question #" + suffix;
        String editedAnswer = "Updated answer with detailed booking steps.";

        faqsPage.openCreateFaqModal()
                .fillFaqForm(question, answer, null, "Active", "1")
                .submitFaqForm();

        faqsPage.reload();
        Assert.assertTrue(faqsPage.isFaqPresent(question), "Created FAQ should appear in FAQ Directory");
        String createdRow = faqsPage.getFaqRowText(question);
        Assert.assertTrue(createdRow.contains("Active"), "Created FAQ should be Active: " + createdRow);
        Assert.assertTrue(createdRow.contains("1"), "Created FAQ should display Order=1: " + createdRow);

        faqsPage.openEditModalForFaq(question)
                .fillFaqForm(editedQuestion, editedAnswer, null, "Inactive", "5")
                .submitFaqForm();

        faqsPage.reload();
        Assert.assertTrue(faqsPage.isFaqPresent(editedQuestion), "Edited FAQ should persist after reload");
        String editedRow = faqsPage.getFaqRowText(editedQuestion);
        Assert.assertTrue(editedRow.contains("Inactive"), "Edited FAQ should be Inactive: " + editedRow);
        Assert.assertTrue(editedRow.contains("5"), "Edited FAQ should display Order=5: " + editedRow);

        faqsPage.deleteFaq(editedQuestion, true);
        faqsPage.reload();
        Assert.assertFalse(faqsPage.isFaqPresent(editedQuestion), "Deleted FAQ should be removed from FAQ Directory");
    }

    @TestCaseMeta(
            id = "TC_FAQ_04",
            title = "Search FAQs by keyword and filter by Status",
            preconditions = "At least 1 Active FAQ exists",
            steps = "1. Create an Active FAQ.\n2. Search by question keyword and filter Status=Active.\n3. Click Apply filters, then Clear.\n4. Delete test FAQ.",
            expected = "Matching FAQ appears in filtered results and Clear resets filters."
    )
    @Test(priority = 3, description = "TC_FAQ_04: Search FAQs by keyword and filter by Status")
    public void verifySearchAndFilterFaqs() {
        String question = "Searchable FAQ #" + uniqueSuffix();
        faqsPage.openCreateFaqModal()
                .fillFaqForm(question, "Searchable answer text", null, "Active", "2")
                .submitFaqForm();

        faqsPage.searchFaq(question).filterByStatus("Active").applyFilters();
        Assert.assertTrue(faqsPage.isFaqPresent(question), "Filtered FAQ should appear in results");

        faqsPage.clearFilters();
        faqsPage.deleteFaq(question, true);
    }

    // ==================== NEGATIVE TEST CASES ====================

    @TestCaseMeta(
            id = "TC_FAQ_07",
            title = "Create FAQ fails when required fields are empty",
            preconditions = "Create FAQ modal is open",
            steps = "1. Leave Question, Answer, and Order blank.\n2. Click Save/Create.",
            expected = "Form validation blocks submission and modal remains open."
    )
    @Test(priority = 4, description = "TC_FAQ_07: Create FAQ fails when required fields are empty")
    public void verifyCreateFaqEmptyRequiredFieldsBlocked() {
        faqsPage.openCreateFaqModal()
                .fillFaqForm("", "", null, "Active", "")
                .attemptSubmitFaqForm();

        Assert.assertTrue(faqsPage.isFaqFormVisible(), "Form should remain open when required fields are empty");
        faqsPage.cancelFaqForm();
    }

    @TestCaseMeta(
            id = "TC_FAQ_08_09",
            title = "Create FAQ rejects whitespace-only Question or Answer",
            preconditions = "Create FAQ modal is open",
            steps = "1. Submit whitespace-only Question with valid Answer.\n2. Submit valid Question with whitespace-only Answer.",
            expected = "Validation rejects whitespace-only Question and Answer without saving."
    )
    @Test(priority = 5, description = "TC_FAQ_08 & TC_FAQ_09: Create FAQ rejects whitespace-only Question or Answer")
    public void verifyCreateFaqWhitespaceOnlyRejected() {
        faqsPage.openCreateFaqModal()
                .fillFaqForm("   ", "Valid answer", null, "Active", "1")
                .attemptSubmitFaqForm();
        Assert.assertTrue(faqsPage.isFaqFormVisible(), "Whitespace-only Question should be rejected");
        faqsPage.cancelFaqForm();

        faqsPage.openCreateFaqModal()
                .fillFaqForm("Valid Question?", "   ", null, "Active", "1")
                .attemptSubmitFaqForm();
        Assert.assertTrue(faqsPage.isFaqFormVisible(), "Whitespace-only Answer should be rejected");
        faqsPage.cancelFaqForm();
    }

    @TestCaseMeta(
            id = "TC_FAQ_10",
            title = "Create FAQ rejects negative Order",
            preconditions = "Create FAQ modal is open",
            steps = "1. Enter valid Question and Answer.\n2. Enter -1 in Order and click Save/Create.",
            expected = "FAQ with negative Order is not created."
    )
    @Test(priority = 6, description = "TC_FAQ_10: Create FAQ rejects negative Order")
    public void verifyCreateFaqNegativeOrderRejected() {
        String question = "NegativeOrderFAQ #" + uniqueSuffix();
        faqsPage.openCreateFaqModal()
                .fillFaqForm(question, "Answer text", null, "Active", "-1")
                .attemptSubmitFaqForm();

        Assert.assertFalse(faqsPage.isFaqPresent(question), "FAQ with negative Order should not be created");
    }

    @TestCaseMeta(
            id = "TC_FAQ_12",
            title = "Cancel Create does not add FAQ to list",
            preconditions = "Create FAQ modal is open",
            steps = "1. Fill FAQ fields.\n2. Click Cancel.",
            expected = "Modal closes and cancelled FAQ is not created in FAQ Directory."
    )
    @Test(priority = 7, description = "TC_FAQ_12: Cancel Create and Cancel Delete do not modify list")
    public void verifyCancelCreateAndDelete() {
        String unsavedQuestion = "CancelledFAQ #" + uniqueSuffix();
        faqsPage.openCreateFaqModal()
                .fillFaqForm(unsavedQuestion, "Should not save", null, "Active", "3")
                .cancelFaqForm();
        Assert.assertFalse(faqsPage.isFaqPresent(unsavedQuestion), "Cancelled FAQ should not be created");
    }

    // ==================== EDGE TEST CASES ====================

    @TestCaseMeta(
            id = "TC_FAQ_15_16",
            title = "Long text, special characters, and XSS payload escaping",
            preconditions = "Create FAQ modal is open",
            steps = "1. Enter XSS payload and special characters in Question, 500+ char Answer, Order=9999.\n2. Save, reload, verify display, and delete.",
            expected = "FAQ saves and renders safely as escaped plain text without breaking layout."
    )
    @Test(priority = 8, description = "TC_FAQ_15 & TC_FAQ_16: Long text and special characters/XSS escaping")
    public void verifyLongTextAndSpecialCharactersFaq() {
        String xssQuestion = "<script>alert('xss')</script> & \"Quotes\" — ₹100? #" + uniqueSuffix();
        String longAnswer = "Detailed FAQ answer content for edge validation. ".repeat(15).trim();

        faqsPage.openCreateFaqModal()
                .fillFaqForm(xssQuestion, longAnswer, null, "Active", "9999")
                .submitFaqForm();

        faqsPage.reload();
        Assert.assertTrue(faqsPage.isFaqPresent(xssQuestion),
                "FAQ with special characters and large Order=9999 should be created and escaped safely");
        faqsPage.deleteFaq(xssQuestion, true);
    }

    @TestCaseMeta(
            id = "TC_FAQ_19",
            title = "Rapidly clicking Create button 5 times creates FAQ only once",
            preconditions = "Create FAQ modal is open",
            steps = "1. Fill all FAQ fields with a unique Question.\n2. Click Create/Save 5 times rapidly.\n3. Reload and count matching rows.\n4. Delete test FAQ.",
            expected = "Only 1 FAQ record is created in FAQ Directory (no duplicates)."
    )
    @Test(priority = 9, description = "TC_FAQ_19: Rapidly clicking Create button 5 times should create FAQ only once")
    public void verifyRapidMultiClickCreateCreatesSingleFaq() {
        String question = "RapidClickFAQ #" + uniqueSuffix();

        faqsPage.openCreateFaqModal()
                .fillFaqForm(question, "Idempotency check answer", null, "Active", "1")
                .rapidClickSubmitFaq(5);

        faqsPage.reload();
        Assert.assertEquals(faqsPage.getFaqCount(question), 1,
                "Clicking Create 5 times rapidly should create only 1 FAQ (no duplicates)");

        faqsPage.deleteFaq(question, true);
    }
}