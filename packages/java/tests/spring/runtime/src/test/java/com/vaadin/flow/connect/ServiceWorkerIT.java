/*
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.connect;

import com.vaadin.flow.testutil.ChromeDeviceTest;
import com.vaadin.testbench.TestBenchElement;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class ServiceWorkerIT extends ChromeDeviceTest {

    @Test
    public void onlineRoot_serviceWorkerInstalled_serviceWorkerActive() {
        getDriver().get(getRootURL() + "/");
        waitForDevServer();
        waitForServiceWorkerReady();

        boolean serviceWorkerActive = (boolean) ((JavascriptExecutor) getDriver()).executeAsyncScript("const resolve = arguments[arguments.length - 1];" + "navigator.serviceWorker.ready.then( function(reg) { resolve(!!reg.active); });");
        Assert.assertTrue("service worker not installed", serviceWorkerActive);
    }

    @Test
    public void onlineRoot_serviceWorkerInstalled_serviceWorkerResponsive() {
        openPageAndPreCacheWhenDevelopmentMode("/");
        Assert.assertNotNull("Should have outlet when loaded online", findElement(By.id("outlet")));
        Assert.assertNotNull("Should have <test-view> in DOM when loaded online", findElement(By.tagName("test-view")));
        TestBenchElement testView = $("test-view").waitForFirst();

        waitUntil(ExpectedConditions.textToBePresentInElement(
            testView.$(TestBenchElement.class).id("sw-content"),
            "Hey from SW"), 25);
    }

    private void openPageAndPreCacheWhenDevelopmentMode(String targetView) {
        openPageAndPreCacheWhenDevelopmentMode(targetView, () -> {
        });
    }

    private void openPageAndPreCacheWhenDevelopmentMode(String targetView, Runnable activateViews) {
        getDriver().get(getRootURL() + targetView);
        waitForDevServer();
        waitForServiceWorkerReady();

        boolean isDevMode = Boolean.getBoolean("vaadin.test.developmentMode");
        if (isDevMode) {
            // In production mode all views are supposed to be already in the
            // bundle, but in dev mode they are loaded at runtime
            // So, for dev mode, pre cache required views
            activateViews.run();

            // In addition not all external resources are cached when the page
            // opens
            // first time, so we need to reload the page even if there is no
            // navigation to other views
            getDriver().get(getRootURL() + targetView);
            waitForDevServer();
            waitForServiceWorkerReady();
        }
    }

    @Override
    protected String getRootURL() {
        return super.getRootURL();
    }
}
