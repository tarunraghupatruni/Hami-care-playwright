package com.hami.care.pages;

import com.hami.care.config.ConfigReader;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.LoadState;

public abstract class BasePage {
    protected final Page page;
    protected final String baseUrl;

    protected BasePage(Page page) {
        this.page = page;
        this.baseUrl = ConfigReader.get("baseUrl");
    }

    public void navigateTo(String path) {
        page.navigate(baseUrl + path);
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    public String getCurrentUrl() {
        return page.url();
    }

    public void reload() {
        page.reload();
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    protected Locator heading(String name) {
        return page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName(name));
    }

    protected Locator button(String name) {
        return page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(name));
    }

    protected void fillFieldByLabel(String labelText, String value) {
        Locator field = page.getByLabel(labelText);
        field.clear();
        field.fill(value);
    }
}
