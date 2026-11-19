package com.hami.care.tests;

import com.hami.care.base.BaseTest;
import com.hami.care.pages.FaqsPage;
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

    @Test(priority = 1, description = "TC_FAQ_01: Verify FAQs Management page, summary cards, and table columns")
    public void verifyFaqsPageOverview() {
        Assert.assertTrue(faqsPage.isLoaded(), "FAQs Management page should load");
        Assert.assertTrue(faqsPage.areSummaryCardsVisible(), "FAQ summary cards should be visible");
        List<String> headers = faqsPage.getTableHeaders();
        Assert.assertTrue(headers.containsAll(List.of("QUESTION", "ANSWER", "CATEGORY", "STATUS", "ORDER", "ACTIONS")),
                "FAQ Directory headers mismatch: " + headers);
    }

    @Test(priority = 2, description = "TC_FAQ_02, TC_FAQ_05, TC_FAQ_06: Create Active FAQ, verify list, Edit all fields, and Delete")
    public void verifyCreateEditDeleteFaqLifecycle() {
        String suffix = uniqueSuffix();
        String question = "How do I book a consultation? #" + suffix;
        String answer = "Open Appointments and choose a doctor slot.";
        String editedQuestion = "Updated FAQ Question #" + suffix;
        String editedAnswer = "Updated answer with detailed booking steps.";

        // 1. Create Active FAQ
        faqsPage.openCreateFaqModal()
                .fillFaqForm(question, answer, null, "Active", "1")
                .submitFaqForm();

        faqsPage.reload();
        Assert.assertTrue(faqsPage.isFaqPresent(question), "Created FAQ should appear in FAQ Directory");
        String createdRow = faqsPage.getFaqRowText(question);
        Assert.assertTrue(createdRow.contains("Active"), "Created FAQ should be Active: " + createdRow);
        Assert.assertTrue(createdRow.contains("1"), "Created FAQ should display Order=1: " + createdRow);

        // 2. Edit FAQ across fields
        faqsPage.openEditModalForFaq(question)
                .fillFaqForm(editedQuestion, editedAnswer, null, "Inactive", "5")
                .submitFaqForm();

        faqsPage.reload();
        Assert.assertTrue(faqsPage.isFaqPresent(editedQuestion), "Edited FAQ should persist after reload");
        String editedRow = faqsPage.getFaqRowText(editedQuestion);
        Assert.assertTrue(editedRow.contains("Inactive"), "Edited FAQ should be Inactive: " + editedRow);
        Assert.assertTrue(editedRow.contains("5"), "Edited FAQ should display Order=5: " + editedRow);

        // 3. Delete FAQ and verify removal
        faqsPage.deleteFaq(editedQuestion, true);
        faqsPage.reload();
        Assert.assertFalse(faqsPage.isFaqPresent(editedQuestion), "Deleted FAQ should be removed from FAQ Directory");
    }

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

    @Test(priority = 4, description = "TC_FAQ_07: Create FAQ fails when required fields are empty")
    public void verifyCreateFaqEmptyRequiredFieldsBlocked() {
        faqsPage.openCreateFaqModal()
                .fillFaqForm("", "", null, "Active", "")
                .attemptSubmitFaqForm();

        Assert.assertTrue(faqsPage.isFaqFormVisible(), "Form should remain open when required fields are empty");
        faqsPage.cancelFaqForm();
    }

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

    @Test(priority = 6, description = "TC_FAQ_10: Create FAQ rejects negative Order")
    public void verifyCreateFaqNegativeOrderRejected() {
        String question = "NegativeOrderFAQ #" + uniqueSuffix();
        faqsPage.openCreateFaqModal()
                .fillFaqForm(question, "Answer text", null, "Active", "-1")
                .attemptSubmitFaqForm();

        Assert.assertFalse(faqsPage.isFaqPresent(question), "FAQ with negative Order should not be created");
    }

    @Test(priority = 7, description = "TC_FAQ_12: Cancel Create and Cancel Delete do not modify list")
    public void verifyCancelCreateAndDelete() {
        String unsavedQuestion = "CancelledFAQ #" + uniqueSuffix();
        faqsPage.openCreateFaqModal()
                .fillFaqForm(unsavedQuestion, "Should not save", null, "Active", "3")
                .cancelFaqForm();
        Assert.assertFalse(faqsPage.isFaqPresent(unsavedQuestion), "Cancelled FAQ should not be created");
    }

    // ==================== EDGE TEST CASES ====================

    @Test(priority = 8, description = "TC_FAQ_15 & TC_FAQ_16: Long text and special characters/XSS escaping")
    public void verifyLongTextAndSpecialCharactersFaq() {
        String xssQuestion = "<script>alert('xss')</script> & "Quotes" — ₹100? #" + uniqueSuffix();
        String longAnswer = "Detailed FAQ answer content for edge validation. ".repeat(15).trim();

        faqsPage.openCreateFaqModal()
                .fillFaqForm(xssQuestion, longAnswer, null, "Active", "9999")
                .submitFaqForm();

        faqsPage.reload();
        Assert.assertTrue(faqsPage.isFaqPresent(xssQuestion),
                "FAQ with special characters and large Order=9999 should be created and escaped safely");
        faqsPage.deleteFaq(xssQuestion, true);
    }
}
