package com.hami.care.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.SelectOption;

import java.util.List;

public class SubscriptionPlansPage extends BasePage {
    private final Locator pageHeading;
    private final Locator addSubscriptionButton;
    private final Locator searchInput;
    private final Locator statusFilterSelect;
    private final Locator applyFiltersButton;
    private final Locator clearButton;
    private final Locator tableHeaders;
    private final Locator tableRows;

    public SubscriptionPlansPage(Page page) {
        super(page);
        this.pageHeading = heading("Subscription Management");
        this.addSubscriptionButton = page.getByText("+ Add Subscription");
        this.searchInput = page.getByPlaceholder("Search plans...");
        this.statusFilterSelect = page.locator("select").first();
        this.applyFiltersButton = button("Apply filters");
        this.clearButton = button("Clear");
        this.tableHeaders = page.locator("table thead th");
        this.tableRows = page.locator("table tbody tr");
    }

    public SubscriptionPlansPage open() {
        navigateTo("/admin/subscriptions");
        return this;
    }

    public boolean isLoaded() {
        return pageHeading.isVisible() && addSubscriptionButton.isVisible();
    }

    public boolean areSummaryCardsVisible() {
        return page.getByText("Total Plans").isVisible()
                && page.getByText("Active Plans").isVisible()
                && page.getByText("Inactive Plans").isVisible()
                && page.getByText("Popular Plans").isVisible();
    }

    public List<String> getTableHeaders() {
        return tableHeaders.allInnerTexts().stream().map(String::trim).toList();
    }

    public int getRowCount() {
        return tableRows.count();
    }

    public SubscriptionPlansPage searchPlan(String planName) {
        searchInput.clear();
        searchInput.fill(planName);
        return this;
    }

    public SubscriptionPlansPage filterByStatus(String status) {
        statusFilterSelect.selectOption(new SelectOption().setLabel(status));
        return this;
    }

    public SubscriptionPlansPage applyFilters() {
        applyFiltersButton.click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
        return this;
    }

    public SubscriptionPlansPage clearFilters() {
        clearButton.click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
        return this;
    }

    // --- Create Subscription Plan (/admin/subscriptions/add) ---

    public SubscriptionPlansPage clickAddSubscription() {
        addSubscriptionButton.click();
        page.waitForURL("**/admin/subscriptions/add");
        page.waitForLoadState(LoadState.NETWORKIDLE);
        return this;
    }

    public SubscriptionPlansPage fillCreatePlanForm(
            String planName,
            String price,
            String discountPrice,
            String duration,
            String durationType,
            String maxFamilyMembers,
            String displayOrder,
            String status,
            String description,
            boolean markPopular,
            List<String> features
    ) {
        Locator nameInput = page.getByPlaceholder("e.g. Premium");
        nameInput.clear();
        if (planName != null) nameInput.fill(planName);

        Locator priceInput = page.getByLabel("Price (₹)");
        priceInput.clear();
        if (price != null) priceInput.fill(price);

        Locator discountInput = page.getByLabel("Discount Price (₹)");
        discountInput.clear();
        if (discountPrice != null) discountInput.fill(discountPrice);

        Locator durationInput = page.getByLabel("Duration").first();
        durationInput.clear();
        if (duration != null) durationInput.fill(duration);

        if (durationType != null) {
            page.getByLabel("Duration Type").selectOption(new SelectOption().setLabel(durationType));
        }

        Locator maxInput = page.getByLabel("Max Family members");
        maxInput.clear();
        if (maxFamilyMembers != null) maxInput.fill(maxFamilyMembers);

        Locator orderInput = page.getByLabel("Display Order");
        orderInput.clear();
        if (displayOrder != null) orderInput.fill(displayOrder);

        if (status != null) {
            page.getByLabel("Status").selectOption(new SelectOption().setLabel(status));
        }

        Locator descInput = page.getByPlaceholder("Describe the plan");
        descInput.clear();
        if (description != null) descInput.fill(description);

        Locator popularCheckbox = page.getByLabel("Mark as popular plan");
        if (markPopular && !popularCheckbox.isChecked()) {
            popularCheckbox.check();
        } else if (!markPopular && popularCheckbox.isChecked()) {
            popularCheckbox.uncheck();
        }

        for (String feature : features) {
            Locator cb = page.getByLabel(feature, new Page.GetByLabelOptions().setExact(true));
            if (!cb.isChecked()) {
                cb.check();
            }
        }
        return this;
    }

    public SubscriptionPlansPage saveNewSubscription() {
        button("Save Subscription").click();
        page.waitForURL("**/admin/subscriptions");
        page.waitForLoadState(LoadState.NETWORKIDLE);
        return this;
    }

    public SubscriptionPlansPage attemptSaveNewSubscription() {
        button("Save Subscription").click();
        page.waitForTimeout(1000);
        return this;
    }

    public SubscriptionPlansPage cancelCreateSubscription() {
        button("Cancel").click();
        page.waitForURL("**/admin/subscriptions");
        page.waitForLoadState(LoadState.NETWORKIDLE);
        return this;
    }

    public boolean isOnAddSubscriptionPage() {
        return page.url().contains("/admin/subscriptions/add");
    }

    public boolean isCreateFieldInvalid(String labelText) {
        Locator field = page.getByLabel(labelText).first();
        return Boolean.TRUE.equals(field.evaluate("el => !el.checkValidity()"));
    }

    // --- Table Row Verification ---

    public Locator getPlanRow(String planName) {
        return tableRows.filter(new Locator.FilterOptions().setHasText(planName)).first();
    }

    public boolean isPlanPresent(String planName) {
        return tableRows.filter(new Locator.FilterOptions().setHasText(planName)).count() > 0;
    }

    public String getPlanRowText(String planName) {
        return getPlanRow(planName).innerText();
    }

    public String getFirstPlanName() {
        return tableRows.first().innerText().split("\\R")[0].trim();
    }

    // --- Edit Subscription Modal ---

    private Locator editModal() {
        return page.locator("[role='dialog']")
                .or(page.locator("div:has(> div:has-text('Edit Subscription'))"))
                .last();
    }

    public SubscriptionPlansPage openEditModalForPlan(String planName) {
        getPlanRow(planName).getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Edit")).click();
        editModal().waitFor();
        return this;
    }

    public boolean isEditModalVisible() {
        return page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Edit Subscription"))
                .or(page.getByText("Edit Subscription"))
                .first()
                .isVisible();
    }

    public SubscriptionPlansPage updatePlanInModal(
            String newPlanName,
            String newPrice,
            String newDiscountPrice,
            String newDuration,
            String newMaxMembers,
            String newStatus,
            Boolean markPopular,
            List<String> additionalFeatures
    ) {
        Locator modal = editModal();

        Locator nameInput = modal.getByLabel("Plan Name");
        nameInput.clear();
        if (newPlanName != null) nameInput.fill(newPlanName);

        Locator priceInput = modal.getByLabel("Price (₹)");
        priceInput.clear();
        if (newPrice != null) priceInput.fill(newPrice);

        Locator discountInput = modal.getByLabel("Discount Price (₹)");
        discountInput.clear();
        if (newDiscountPrice != null) discountInput.fill(newDiscountPrice);

        Locator durationInput = modal.getByLabel("Duration").first();
        durationInput.clear();
        if (newDuration != null) durationInput.fill(newDuration);

        Locator maxMembersInput = modal.getByLabel("Maximum Family Members");
        maxMembersInput.clear();
        if (newMaxMembers != null) maxMembersInput.fill(newMaxMembers);

        if (newStatus != null) {
            modal.getByLabel("Status").selectOption(new SelectOption().setLabel(newStatus));
        }

        if (markPopular != null) {
            Locator popCb = modal.getByLabel("Mark as Popular");
            if (markPopular && !popCb.isChecked()) {
                popCb.check();
            } else if (!markPopular && popCb.isChecked()) {
                popCb.uncheck();
            }
        }

        for (String feature : additionalFeatures) {
            Locator cb = modal.getByLabel(feature, new Locator.GetByLabelOptions().setExact(true));
            if (!cb.isChecked()) {
                cb.check();
            }
        }
        return this;
    }

    public SubscriptionPlansPage saveEditChanges() {
        editModal().getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Save Changes")).click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
        return this;
    }

    public SubscriptionPlansPage attemptSaveEditChanges() {
        editModal().getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Save Changes")).click();
        page.waitForTimeout(1000);
        return this;
    }

    public SubscriptionPlansPage cancelEditModal() {
        editModal().getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Cancel")).click();
        return this;
    }
}
