/*
 * Copyright 2000-2020 Vaadin Ltd.
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
import { assert } from '@open-wc/testing';
import { ConnectionIndicator, ConnectionState, ConnectionStateStore } from '../src';

const $wnd = window as any;

describe('ConnectionIndicator', () => {
  let connectionIndicator: ConnectionIndicator;

  describe('properties', () => {
    beforeEach(async () => {
      connectionIndicator = document.createElement('vaadin-connection-indicator') as ConnectionIndicator;
      document.body.prepend(connectionIndicator);
      await connectionIndicator.updateComplete;
    });

    afterEach(() => {
      connectionIndicator.remove();
    });

    it('should have default property values', () => {
      // Configuration
      assert.isTrue(connectionIndicator.applyDefaultTheme);
      assert.equal(connectionIndicator.firstDelay, 300);
      assert.equal(connectionIndicator.secondDelay, 1500);
      assert.equal(connectionIndicator.thirdDelay, 5000);
      assert.equal(connectionIndicator.expandedDuration, 2000);

      // Strings
      assert.match(connectionIndicator.onlineText, /online/i);
      assert.match(connectionIndicator.offlineText, /connection lost/i);
      assert.match(connectionIndicator.reconnectingText, /reconnect/i);

      // Component state
      assert.isFalse(connectionIndicator.hasAttribute('loading'));
      assert.isFalse(connectionIndicator.hasAttribute('reconnecting'));
      assert.isFalse(connectionIndicator.hasAttribute('offline'));
      assert.isFalse(connectionIndicator.hasAttribute('expanded'));
    });

    it('should apply default theme', () => {
      const style = document.querySelector('#css-loading-indicator');
      assert.isNotNull(style);
    });

    it('should remove css if default theme not applied', () => {
      connectionIndicator.applyDefaultTheme = false;
      const style = document.querySelector('#css-loading-indicator');
      assert.isNull(style);
    });

    it('should remove css when removed from dom', () => {
      connectionIndicator.remove();
      const style = document.querySelector('#css-loading-indicator');
      assert.isNull(style);

      // Add back to prevent errors in afterEach
      document.body.prepend(connectionIndicator);
    });
  });

  describe('with state store', () => {
    let connectionStateStore: ConnectionStateStore;
    let message: HTMLSpanElement;

    beforeEach(() => {
      connectionStateStore = new ConnectionStateStore(ConnectionState.CONNECTED);
      $wnd.Vaadin = { connectionState: connectionStateStore };
    });

    afterEach(() => {
      delete $wnd.Vaadin;
    });

    async function setupIndicator() {
      connectionIndicator = document.createElement('vaadin-connection-indicator') as ConnectionIndicator;
      connectionIndicator.expandedDuration = 10;
      document.body.prepend(connectionIndicator);
      await connectionIndicator.updateComplete;
      message = connectionIndicator.querySelector('.v-status-message > span') as HTMLSpanElement;
    }

    function destroyIndicator() {
      document.body.removeChild(connectionIndicator);
    }

    it('initial state: connected', async () => {
      try {
        await setupIndicator();

        assert.isFalse(connectionIndicator.hasAttribute('offline'));
        assert.isFalse(connectionIndicator.hasAttribute('reconnecting'));
        assert.isFalse(connectionIndicator.hasAttribute('loading'));
        assert.isFalse(connectionIndicator.hasAttribute('expanded'));
        assert.equal(String(message.textContent).trim(), connectionIndicator.onlineText);
      } finally {
        destroyIndicator();
      }
    });

    it('initial state: loading', async () => {
      try {
        connectionStateStore.state = ConnectionState.LOADING;
        await setupIndicator();

        assert.isFalse(connectionIndicator.hasAttribute('offline'));
        assert.isFalse(connectionIndicator.hasAttribute('reconnecting'));
        assert.isTrue(connectionIndicator.hasAttribute('loading'));
        assert.isFalse(connectionIndicator.hasAttribute('expanded'));
        assert.equal(String(message.textContent).trim(), connectionIndicator.onlineText);
      } finally {
        destroyIndicator();
      }
    });

    it('initial state: reconnecting', async () => {
      try {
        connectionStateStore.state = ConnectionState.RECONNECTING;

        await setupIndicator();

        assert.isFalse(connectionIndicator.hasAttribute('offline'));
        assert.isTrue(connectionIndicator.hasAttribute('reconnecting'));
        assert.isFalse(connectionIndicator.hasAttribute('loading'));
        assert.isFalse(connectionIndicator.hasAttribute('expanded'));
        assert.equal(String(message.textContent).trim(), connectionIndicator.reconnectingText);
      } finally {
        destroyIndicator();
      }
    });

    it('initial state: connection lost', async () => {
      try {
        connectionStateStore.state = ConnectionState.CONNECTION_LOST;

        await setupIndicator();

        assert.isTrue(connectionIndicator.hasAttribute('offline'));
        assert.isFalse(connectionIndicator.hasAttribute('reconnecting'));
        assert.isFalse(connectionIndicator.hasAttribute('loading'));
        assert.isFalse(connectionIndicator.hasAttribute('expanded'));
        assert.equal(String(message.textContent).trim(), connectionIndicator.offlineText);
      } finally {
        destroyIndicator();
      }
    });

    it('should react on store state change', async () => {
      try {
        await setupIndicator();

        connectionStateStore.state = ConnectionState.LOADING;
        await connectionIndicator.updateComplete;
        assert.isFalse(connectionIndicator.hasAttribute('offline'));
        assert.isFalse(connectionIndicator.hasAttribute('reconnecting'));
        assert.equal(String(message.textContent).trim(), connectionIndicator.onlineText);
        assert.isTrue(connectionIndicator.hasAttribute('loading'));
        // Loading should not cause expanded message
        assert.isFalse(connectionIndicator.hasAttribute('expanded'));

        connectionStateStore.state = ConnectionState.CONNECTED;
        await connectionIndicator.updateComplete;
        assert.isFalse(connectionIndicator.hasAttribute('offline'));
        assert.isFalse(connectionIndicator.hasAttribute('reconnecting'));
        assert.isFalse(connectionIndicator.hasAttribute('loading'));
        assert.equal(String(message.textContent).trim(), connectionIndicator.onlineText);
        // Message did not change from before loading, should not cause expanded
        assert.isFalse(connectionIndicator.hasAttribute('expanded'));

        connectionStateStore.state = ConnectionState.RECONNECTING;
        await connectionIndicator.updateComplete;
        assert.isFalse(connectionIndicator.hasAttribute('offline'));
        assert.isTrue(connectionIndicator.hasAttribute('reconnecting'));
        assert.isFalse(connectionIndicator.hasAttribute('loading'));
        assert.equal(String(message.textContent).trim(), connectionIndicator.reconnectingText);
        // Message did change, should cause expanded
        assert.isTrue(connectionIndicator.hasAttribute('expanded'));
        await new Promise((resolve) => setTimeout(resolve, 20));
        assert.isFalse(connectionIndicator.hasAttribute('expanded'));

        connectionStateStore.state = ConnectionState.CONNECTION_LOST;
        await connectionIndicator.updateComplete;
        assert.isTrue(connectionIndicator.hasAttribute('offline'));
        assert.isFalse(connectionIndicator.hasAttribute('reconnecting'));
        assert.isFalse(connectionIndicator.hasAttribute('loading'));
        assert.equal(String(message.textContent).trim(), connectionIndicator.offlineText);
        // Message did change, should cause expanded
        assert.isTrue(connectionIndicator.hasAttribute('expanded'));
        await new Promise((resolve) => setTimeout(resolve, 20));
        assert.isFalse(connectionIndicator.hasAttribute('expanded'));

        connectionStateStore.state = ConnectionState.LOADING;
        await connectionIndicator.updateComplete;
        assert.isFalse(connectionIndicator.hasAttribute('offline'));
        assert.isFalse(connectionIndicator.hasAttribute('reconnecting'));
        assert.equal(String(message.textContent).trim(), connectionIndicator.onlineText);
        assert.isTrue(connectionIndicator.hasAttribute('loading'));
        // Loading should not cause expanded message
        assert.isFalse(connectionIndicator.hasAttribute('expanded'));

        connectionStateStore.state = ConnectionState.CONNECTED;
        await connectionIndicator.updateComplete;
        assert.isFalse(connectionIndicator.hasAttribute('offline'));
        assert.isFalse(connectionIndicator.hasAttribute('reconnecting'));
        assert.isFalse(connectionIndicator.hasAttribute('loading'));
        assert.equal(String(message.textContent).trim(), connectionIndicator.onlineText);
        // Message did change from before loading, should cause expanded
        assert.isTrue(connectionIndicator.hasAttribute('expanded'));
        await new Promise((resolve) => setTimeout(resolve, 20));
        assert.isFalse(connectionIndicator.hasAttribute('expanded'));
      } finally {
        destroyIndicator();
      }
    });

    it('should not react on store state change after removed from DOM', async () => {
      await setupIndicator();
      connectionIndicator.remove();

      connectionStateStore.state = ConnectionState.LOADING;
      await connectionIndicator.updateComplete;
      assert.isFalse(connectionIndicator.hasAttribute('offline'));
      assert.isFalse(connectionIndicator.hasAttribute('reconnecting'));
      assert.isFalse(connectionIndicator.hasAttribute('loading'));
      assert.isFalse(connectionIndicator.hasAttribute('expanded'));
      assert.equal(String(message.textContent).trim(), connectionIndicator.onlineText);

      connectionStateStore.state = ConnectionState.RECONNECTING;
      await connectionIndicator.updateComplete;
      assert.isFalse(connectionIndicator.hasAttribute('offline'));
      assert.isFalse(connectionIndicator.hasAttribute('reconnecting'));
      assert.isFalse(connectionIndicator.hasAttribute('loading'));
      assert.isFalse(connectionIndicator.hasAttribute('expanded'));
      assert.equal(String(message.textContent).trim(), connectionIndicator.onlineText);

      connectionStateStore.state = ConnectionState.CONNECTION_LOST;
      await connectionIndicator.updateComplete;
      assert.isFalse(connectionIndicator.hasAttribute('offline'));
      assert.isFalse(connectionIndicator.hasAttribute('reconnecting'));
      assert.isFalse(connectionIndicator.hasAttribute('loading'));
      assert.isFalse(connectionIndicator.hasAttribute('expanded'));
      assert.equal(String(message.textContent).trim(), connectionIndicator.onlineText);

      connectionStateStore.state = ConnectionState.CONNECTED;
      await connectionIndicator.updateComplete;
      assert.isFalse(connectionIndicator.hasAttribute('offline'));
      assert.isFalse(connectionIndicator.hasAttribute('reconnecting'));
      assert.isFalse(connectionIndicator.hasAttribute('loading'));
      assert.isFalse(connectionIndicator.hasAttribute('expanded'));
      assert.equal(String(message.textContent).trim(), connectionIndicator.onlineText);
    });

    it('should show loading using delays', async () => {
      await setupIndicator();
      connectionIndicator.firstDelay = 100;
      connectionIndicator.secondDelay = 200;
      connectionIndicator.thirdDelay = 400;

      const loadingBar = connectionIndicator.querySelector('.v-loading-indicator') as HTMLDivElement;
      assert.equal(loadingBar.getAttribute('style'), 'display: none');

      connectionStateStore.state = ConnectionState.LOADING;
      await connectionIndicator.updateComplete;
      assert.isFalse(loadingBar.classList.contains('first'));
      assert.isFalse(loadingBar.classList.contains('second'));
      assert.isFalse(loadingBar.classList.contains('third'));

      await new Promise((resolve) => setTimeout(resolve, 150));
      assert.isTrue(loadingBar.classList.contains('first'));
      assert.isFalse(loadingBar.classList.contains('second'));
      assert.isFalse(loadingBar.classList.contains('third'));
      assert.equal(loadingBar.getAttribute('style'), 'display: block');

      await new Promise((resolve) => setTimeout(resolve, 150));
      assert.isFalse(loadingBar.classList.contains('first'));
      assert.isTrue(loadingBar.classList.contains('second'));
      assert.isFalse(loadingBar.classList.contains('third'));
      assert.equal(loadingBar.getAttribute('style'), 'display: block');

      await new Promise((resolve) => setTimeout(resolve, 150));
      assert.isFalse(loadingBar.classList.contains('first'));
      assert.isFalse(loadingBar.classList.contains('second'));
      assert.isTrue(loadingBar.classList.contains('third'));
      assert.equal(loadingBar.getAttribute('style'), 'display: block');

      connectionStateStore.state = ConnectionState.CONNECTED;
      await connectionIndicator.updateComplete;
      assert.isFalse(loadingBar.classList.contains('first'));
      assert.isFalse(loadingBar.classList.contains('second'));
      assert.isFalse(loadingBar.classList.contains('third'));
      assert.equal(loadingBar.getAttribute('style'), 'display: none');
    });
  });
});
