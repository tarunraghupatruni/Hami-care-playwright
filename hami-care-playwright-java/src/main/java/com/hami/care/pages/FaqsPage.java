package com.hami.care.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.SelectOption;
import com.microsoft.playwright.options.WaitForSelectorState;
import java.util.List;

public class FaqsPage extends BasePage {
    private final Locator pageHeading;
    private final Locator createNewFaqButton;
    private final Locator searchInput;
    private final Locator statusFilterSelect;
    private final Locator applyFiltersButton;
    private final Locator clearButton;
    private final Locator tableHeaders;
    private final Locator tableRows;

    public FaqsPage(Page page) {
        super(page);
        this.pageHeading = heading("FAQs Management");
        this.createNewFaqButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Create New FAQ"))
        .or(page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Create New FAQ")))
        .or(page.locator("button:has-text('Create New FAQ'), a:has-text('Create New FAQ')"))
        .first();        this.searchInput = page.getByPlaceholder("Search questions or answers...");
        this.statusFilterSelect = page.locator("select").first();
        this.applyFiltersButton = button("Apply filters");
        this.clearButton = button("Clear");
        this.tableHeaders = page.locator("table thead th");
        this.tableRows = page.locator("table tbody tr");
    }

    public FaqsPage open() {
        navigateTo("/admin/faqs");
        return this;
    }

    public boolean isLoaded() {
        return pageHeading.isVisible() && createNewFaqButton.isVisible();
    }

    public boolean areSummaryCardsVisible() {
        return page.getByText("Total FAQs", new Page.GetByTextOptions().setExact(true)).isVisible()
                && page.getByText("Active FAQs", new Page.GetByTextOptions().setExact(true)).isVisible()
                && page.getByText("Inactive FAQs", new Page.GetByTextOptions().setExact(true)).isVisible();
    }

    public int getSummaryCardCount(String cardTitle) {
        Locator card = page.locator("div").filter(new Locator.FilterOptions().setHasText(cardTitle)).last();
        String digits = card.innerText().replaceAll("[^0-9]", "");
        return digits.isEmpty() ? 0 : Integer.parseInt(digits);
    }

    public List<String> getTableHeaders() {
        return tableHeaders.allInnerTexts().stream().map(String::trim).toList();
    }

    public int getRowCount() {
        return tableRows.count();
    }

    public FaqsPage searchFaq(String query) {
        searchInput.clear();
        searchInput.fill(query);
        return this;
    }

    public FaqsPage filterByStatus(String statusLabel) {
        statusFilterSelect.selectOption(new SelectOption().setLabel(statusLabel));
        return this;
    }

    public FaqsPage applyFilters() {
        applyFiltersButton.click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
        return this;
    }

    public FaqsPage clearFilters() {
        clearButton.click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
        return this;
    }

    // --- Modal / Form Helpers (Create & Edit) ---

    private Locator activeDialog() {
        return page.locator("[role='dialog']")
                .or(page.locator("form").filter(new Locator.FilterOptions().setHasText("Question")))
                .last();
    }

    public FaqsPage openCreateFaqModal() {
        createNewFaqButton.click();
        activeDialog().waitFor();
        return this;
    }

    public boolean isFaqFormVisible() {
        return activeDialog().isVisible();
    }

    public FaqsPage fillFaqForm(String question, String answer, String category, String status, String order) {
        Locator form = activeDialog();

        Locator questionInput = form.getByLabel("Question").or(form.locator("input[name*='question'], textarea[name*='question']")).first();
        questionInput.clear();
        if (question != null) questionInput.fill(question);

        Locator answerInput = form.getByLabel("Answer").or(form.locator("textarea[name*='answer'], input[name*='answer']")).first();
        answerInput.clear();
        if (answer != null) answerInput.fill(answer);

        if (category != null) {
            Locator categoryControl = form.getByLabel("Category").or(form.locator("select[name*='category'], input[name*='category']")).first();
            String tagName = (String) categoryControl.evaluate("el => el.tagName.toLowerCase()");
            if ("select".equals(tagName)) {
                categoryControl.selectOption(new SelectOption().setLabel(category));
            } else {
                categoryControl.clear();
                categoryControl.fill(category);
            }
        }

        if (status != null) {
            Locator statusSelect = form.getByLabel("Status").or(form.locator("select[name*='status']")).first();
            statusSelect.selectOption(new SelectOption().setLabel(status));
        }

        if (order != null) {
            Locator orderInput = form.getByLabel("Order").or(form.locator("input[name*='order']")).first();
            orderInput.clear();
            orderInput.fill(order);
        }
        return this;
    }

    public FaqsPage submitFaqForm() {
        Locator dialog = activeDialog();
        Locator submitBtn = dialog.locator("button[type='submit']")
                .or(dialog.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Save")))
                .or(dialog.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Update")))
                .or(dialog.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Create")))
                .first();
        submitBtn.click();
        dialog.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));
        page.waitForLoadState(LoadState.NETWORKIDLE);
        return this;
    }

    public FaqsPage attemptSubmitFaqForm() {
        Locator submitBtn = activeDialog().locator("button[type='submit']")
                .or(activeDialog().getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Save")))
                .or(activeDialog().getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Create")))
                .first();
        submitBtn.click();
        page.waitForTimeout(800);
        return this;
    }

    public FaqsPage cancelFaqForm() {
        activeDialog().getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Cancel")).click();
        return this;
    }

    // --- Row Actions (Verify, Edit, Delete) ---

    public Locator getFaqRow(String questionText) {
        return tableRows.filter(new Locator.FilterOptions().setHasText(questionText)).first();
    }

    public boolean isFaqPresent(String questionText) {
        try {
            getFaqRow(questionText).waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE)
                    .setTimeout(5000));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getFaqRowText(String questionText) {
        return getFaqRow(questionText).innerText();
    }

    public FaqsPage openEditModalForFaq(String questionText) {
        getFaqRow(questionText).getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Edit")).click();
        activeDialog().waitFor();
        return this;
    }
    public int getFaqCount(String questionText) {
        return tableRows.filter(new Locator.FilterOptions().setHasText(questionText)).count();
    }

    public FaqsPage rapidClickSubmitFaq(int clickCount) {
        Locator dialog = activeDialog();
        Locator submitBtn = dialog.locator("button[type='submit']")
                .or(dialog.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Save")))
                .or(dialog.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Create")))
                .first();

        for (int i = 0; i < clickCount; i++) {
            try {
                if (!dialog.isVisible() || submitBtn.isDisabled()) {
                    break;
                }
                submitBtn.click(new Locator.ClickOptions().setForce(true).setTimeout(1000));
            } catch (Exception ignored) {
                // Expected if the modal closes or button detaches after the first click
                break;
            }
        }

        dialog.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));
        page.waitForLoadState(LoadState.NETWORKIDLE);
        return this;
    }

    public FaqsPage deleteFaq(String questionText, boolean confirm) {
        Locator allDeleteButtons = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Delete"));
        int beforeCount = allDeleteButtons.count();

        Locator row = getFaqRow(questionText);
        row.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Delete"))
                .or(row.locator("button[title*='Delete'], button[aria-label*='Delete']"))
                .first()
                .click();

        page.waitForCondition(() -> allDeleteButtons.count() > beforeCount);

        if (confirm) {
            allDeleteButtons.last().click();
        } else {
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Cancel")).last().click();
        }

        page.waitForCondition(() -> allDeleteButtons.count() <= beforeCount);
        page.waitForLoadState(LoadState.NETWORKIDLE);
        return this;
    }

}
