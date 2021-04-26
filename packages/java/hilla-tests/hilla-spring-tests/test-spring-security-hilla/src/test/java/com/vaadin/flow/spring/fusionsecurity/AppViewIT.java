package com.vaadin.flow.spring.fusionsecurity;

import com.vaadin.flow.component.login.testbench.LoginFormElement;
import com.vaadin.flow.component.login.testbench.LoginOverlayElement;
import com.vaadin.flow.testutil.ChromeBrowserTest;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

public class AppViewIT extends ChromeBrowserTest {

    private static final String ROOT_PAGE_HEADER_TEXT = "Welcome to the TypeScript Bank of Vaadin";
    private static final int SERVER_PORT = 9999;
    private static final String USER_FULLNAME = "John the User";

    @Override
    protected int getDeploymentPort() {
        return SERVER_PORT;
    }

    @After
    public void tearDown() {
        if (getDriver() != null) {
            checkForBrowserErrors();
            logout();
        }
    }

    private void checkForBrowserErrors() {
        checkLogsForErrors(msg -> {
            return msg.contains(
                    "admin-only/secret.txt - Failed to load resource: the server responded with a status of 403");
        });
    }

    private void logout() {
        open("logout");
    }

    private void clickLogout() {
        navigateTo("logout", false);
    }

    private void open(String path) {
        getDriver().get(getRootURL() + "/" + path);
    }

    @Test
    public void root_page_does_not_require_login() {
        open("");
        assertRootPageShown();
    }

    @Test
    public void navigate_to_private_view_prevented() {
        open("");
        navigateTo("private", false);
        assertLoginViewShown();
    }

    @Test
    public void redirect_to_private_view_after_login() {
        open("private");
        assertPathShown("login");
        loginUser();
        assertPrivatePageShown(USER_FULLNAME);
    }

    @Test
    public void private_page_logout_should_redirect_to_root() {
        open("login");
        loginUser();
        navigateTo("private");
        clickLogout();
        assertRootPageShown();
    }

    @Test
    public void redirect_to_resource_after_login() {
        String contents = "Secret document for admin";
        String path = "admin-only/secret.txt";
        open(path);
        loginAdmin();
        assertPathShown(path);
        String result = getDriver().getPageSource();
        Assert.assertTrue(result.contains(contents));
    }

    @Test
    public void refresh_when_logged_in_stays_logged_in() {
        open("private");
        loginUser();
        assertPrivatePageShown(USER_FULLNAME);
        refresh();
        assertPrivatePageShown(USER_FULLNAME);
    }

    @Test
    public void access_restricted_to_logged_in_users() {
        String contents = "Secret document for all logged in users";
        String path = "all-logged-in/secret.txt";

        open(path);
        assertLoginViewShown();
        loginUser();
        assertPageContains(contents);
        logout();
        assertRootPageShown();

        open(path);
        loginAdmin();
        assertPageContains(contents);
        logout();
        assertRootPageShown();

        open(path);
        assertLoginViewShown();
    }

    @Test
    public void access_restricted_to_admin() {
        String contents = "Secret document for admin";
        String path = "admin-only/secret.txt";
        open(path);
        assertLoginViewShown();
        loginUser();
        open(path);
        assertForbiddenPage();
        logout();
        assertRootPageShown();

        open(path);
        loginAdmin();
        String adminResult = getDriver().getPageSource();
        Assert.assertTrue(adminResult.contains(contents));
        logout();
        open(path);
        assertLoginViewShown();
    }

    @Test
    public void static_resources_accessible_without_login() throws Exception {
        open("manifest.webmanifest");
        Assert.assertTrue(getDriver().getPageSource().contains("\"name\":\"Spring Security Helper Test Project\""));
        open("sw.js");
        Assert.assertTrue(getDriver().getPageSource().contains("this._installAndActiveListenersAdded"));
        open("sw-runtime-resources-precache.js");
        Assert.assertTrue(getDriver().getPageSource().contains("self.additionalManifestEntries = ["));
    }

    @Test
    public void public_app_resources_available_for_all() {
        open("public/public.txt");
        String shouldBeTextFile = getDriver().getPageSource();
        Assert.assertTrue(shouldBeTextFile.contains("Public document for all users"));
        open("login");
        loginUser();
        open("public/public.txt");
        shouldBeTextFile = getDriver().getPageSource();
        Assert.assertTrue(shouldBeTextFile.contains("Public document for all users"));
    }

    private void navigateTo(String path) {
        navigateTo(path, true);
    }

    private void navigateTo(String path, boolean assertPathShown) {
        waitUntil(driver -> $("*").attribute("id", "main-view").exists());
        $("*").attribute("id", "main-view").first().$("a").attribute("href", path).first().click();
        if (assertPathShown) {
            assertPathShown(path);
        }
    }

    private void assertLoginViewShown() {
        assertPathShown("login");
        waitUntil(driver -> $(LoginOverlayElement.class).exists());
    }

    private void assertRootPageShown() {
        waitUntil(drive -> $("h1").attribute("id", "header").exists());
        String headerText = $("h1").id("header").getText();
        Assert.assertEquals(ROOT_PAGE_HEADER_TEXT, headerText);
    }

    private void assertPrivatePageShown(String fullName) {
        assertPathShown("private");
        waitUntil(driver -> $("span").attribute("id", "balanceText").exists());
        String balance = $("span").id("balanceText").getText();
        Assert.assertTrue(balance.startsWith("Hello " + fullName + ", your bank account balance is $"));
    }

    private void assertPathShown(String path) {
        waitUntil(driver -> driver.getCurrentUrl().equals(getRootURL() + "/" + path));
    }

    private void loginUser() {
        login("john", "john");
    }

    private void loginAdmin() {
        login("emma", "emma");
    }

    private void login(String username, String password) {
        assertLoginViewShown();

        LoginFormElement form = $(LoginOverlayElement.class).first().getLoginForm();
        form.getUsernameField().setValue(username);
        form.getPasswordField().setValue(password);
        form.submit();
        waitUntilNot(driver -> $(LoginOverlayElement.class).exists());
    }

    private void refresh() {
        getDriver().navigate().refresh();
    }

    private void assertForbiddenPage() {
        assertPageContains("There was an unexpected error (type=Forbidden, status=403).");
    }

    private void assertPageContains(String contents) {
        String pageSource = getDriver().getPageSource();
        Assert.assertTrue(pageSource.contains(contents));
    }

}
