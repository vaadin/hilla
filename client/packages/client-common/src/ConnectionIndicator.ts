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

import { html, LitElement } from 'lit';
import { property } from 'lit/decorators.js';
import { classMap } from 'lit/directives/class-map.js';
import { ConnectionState, ConnectionStateStore } from './ConnectionState.js';

const DEFAULT_STYLE_ID = 'css-loading-indicator';

/**
 * The loading indicator states
 */
export const enum LoadingBarState {
  IDLE = '',
  FIRST = 'first',
  SECOND = 'second',
  THIRD = 'third',
}

/**
 * Component showing loading and connection indicator. When added to DOM,
 * listens for changes on `window.Vaadin.connectionState` ConnectionStateStore.
 */
export class ConnectionIndicator extends LitElement {
  /**
   * Initialize global connection indicator instance at
   * window.Vaadin.connectionIndicator and add instance to the document body.
   */
  static create(): ConnectionIndicator {
    const $wnd = window as any;
    if (!$wnd.Vaadin?.connectionIndicator) {
      $wnd.Vaadin = $wnd.Vaadin || {};
      $wnd.Vaadin.connectionIndicator = document.createElement('vaadin-connection-indicator');
      document.body.appendChild($wnd.Vaadin.connectionIndicator);
    }
    return $wnd.Vaadin?.connectionIndicator as ConnectionIndicator;
  }

  /**
   * The delay before showing the loading indicator, in ms.
   */
  @property({ type: Number })
  firstDelay = 300;

  /**
   * The delay before the loading indicator goes into "second" state, in ms.
   */
  @property({ type: Number })
  secondDelay = 1500;

  /**
   * The delay before the loading indicator goes into "third" state, in ms.
   */
  @property({ type: Number })
  thirdDelay = 5000;

  /**
   * The duration for which the connection state change message is visible,
   * in ms.
   */
  @property({ type: Number })
  expandedDuration = 2000;

  /**
   * The message shown when the connection goes to connected state.
   */
  @property({ type: String })
  onlineText = 'Online';

  /**
   * The message shown when the connection goes to lost state.
   */
  @property({ type: String })
  offlineText = 'Connection lost';

  /**
   * The message shown when the connection goes to reconnecting state.
   */
  @property({ type: String })
  reconnectingText = 'Connection lost, trying to reconnect...';

  @property({ type: Boolean, reflect: true })
  private offline = false;

  @property({ type: Boolean, reflect: true })
  private reconnecting = false;

  @property({ type: Boolean, reflect: true })
  private expanded = false;

  @property({ type: Boolean, reflect: true })
  private loading = false;

  @property({ type: String })
  private loadingBarState: LoadingBarState = LoadingBarState.IDLE;

  private applyDefaultThemeState = true;

  private firstTimeout = 0;

  private secondTimeout = 0;

  private thirdTimeout = 0;

  private expandedTimeout = 0;

  private connectionStateStore?: ConnectionStateStore;

  private readonly connectionStateListener: () => void;

  private lastMessageState: ConnectionState = ConnectionState.CONNECTED;

  constructor() {
    super();

    this.connectionStateListener = () => {
      this.expanded = this.updateConnectionState();
      this.expandedTimeout = this.timeoutFor(
        this.expandedTimeout,
        this.expanded,
        () => {
          this.expanded = false;
        },
        this.expandedDuration
      );
    };
  }

  render() {
    return html`
      <div class="v-loading-indicator ${this.loadingBarState}" style="${this.getLoadingBarStyle()}"></div>

      <div
        class="v-status-message ${classMap({
          active: this.reconnecting,
        })}"
      >
        <span class="text"> ${this.renderMessage()} </span>
      </div>
    `;
  }

  connectedCallback() {
    super.connectedCallback();

    const $wnd = window as any;
    if ($wnd.Vaadin?.connectionState) {
      this.connectionStateStore = $wnd.Vaadin.connectionState as ConnectionStateStore;
      this.connectionStateStore.addStateChangeListener(this.connectionStateListener);
      this.updateConnectionState();
    }

    this.updateTheme();
  }

  disconnectedCallback() {
    super.disconnectedCallback();

    if (this.connectionStateStore) {
      this.connectionStateStore.removeStateChangeListener(this.connectionStateListener);
    }

    this.updateTheme();
  }

  @property({ type: Boolean })
  get applyDefaultTheme() {
    return this.applyDefaultThemeState;
  }

  set applyDefaultTheme(applyDefaultTheme: boolean) {
    if (applyDefaultTheme !== this.applyDefaultThemeState) {
      this.applyDefaultThemeState = applyDefaultTheme;
      this.updateTheme();
    }
  }

  protected createRenderRoot() {
    return this;
  }

  /**
   * Update state flags.
   *
   * @return true if the connection message changes, and therefore a new
   * message should be shown
   */
  private updateConnectionState(): boolean {
    const state = this.connectionStateStore?.state;
    this.offline = state === ConnectionState.CONNECTION_LOST;
    this.reconnecting = state === ConnectionState.RECONNECTING;
    this.updateLoading(state === ConnectionState.LOADING);
    if (this.loading) {
      // Entering loading state, do not show message
      return false;
    }

    if (state !== this.lastMessageState) {
      this.lastMessageState = state!;
      // Message changes, show new message
      return true;
    }

    // Message did not change
    return false;
  }

  private updateLoading(loading: boolean) {
    this.loading = loading;
    this.loadingBarState = LoadingBarState.IDLE;

    this.firstTimeout = this.timeoutFor(
      this.firstTimeout,
      loading,
      () => {
        this.loadingBarState = LoadingBarState.FIRST;
      },
      this.firstDelay
    );

    this.secondTimeout = this.timeoutFor(
      this.secondTimeout,
      loading,
      () => {
        this.loadingBarState = LoadingBarState.SECOND;
      },
      this.secondDelay
    );

    this.thirdTimeout = this.timeoutFor(
      this.thirdTimeout,
      loading,
      () => {
        this.loadingBarState = LoadingBarState.THIRD;
      },
      this.thirdDelay
    );
  }

  private renderMessage() {
    if (this.reconnecting) {
      return this.reconnectingText;
    }

    if (this.offline) {
      return this.offlineText;
    }

    return this.onlineText;
  }

  private updateTheme() {
    if (this.applyDefaultThemeState && this.isConnected) {
      if (!document.getElementById(DEFAULT_STYLE_ID)) {
        const style = document.createElement('style');
        style.id = DEFAULT_STYLE_ID;
        style.textContent = this.getDefaultStyle();
        document.head.appendChild(style);
      }
    } else {
      const style = document.getElementById(DEFAULT_STYLE_ID);
      if (style) {
        document.head.removeChild(style);
      }
    }
  }

  private getDefaultStyle(): string {
    return `
      @keyframes v-progress-start {
        0% {
          width: 0%;
        }
        100% {
          width: 50%;
        }
      }
      @keyframes v-progress-delay {
        0% {
          width: 50%;
        }
        100% {
          width: 90%;
        }
      }
      @keyframes v-progress-wait {
        0% {
          width: 90%;
          height: 4px;
        }
        3% {
          width: 91%;
          height: 7px;
        }
        100% {
          width: 96%;
          height: 7px;
        }
      }
      @keyframes v-progress-wait-pulse {
        0% {
          opacity: 1;
        }
        50% {
          opacity: 0.1;
        }
        100% {
          opacity: 1;
        }
      }
      .v-loading-indicator,
      .v-status-message {
        position: fixed;
        z-index: 251;
        left: 0;
        right: auto;
        top: 0;
        background-color: var(--lumo-primary-color, var(--material-primary-color, blue));
        transition: none;
      }
      .v-loading-indicator {
        width: 50%;
        height: 4px;
        opacity: 1;
        pointer-events: none;
        animation: v-progress-start 1000ms 200ms both;
      }
      .v-loading-indicator[style*='none'] {
        display: block !important;
        width: 100%;
        opacity: 0;
        animation: none;
        transition: opacity 500ms 300ms, width 300ms;
      }
      .v-loading-indicator.second {
        width: 90%;
        animation: v-progress-delay 3.8s forwards;
      }
      .v-loading-indicator.third {
        width: 96%;
        animation: v-progress-wait 5s forwards, v-progress-wait-pulse 1s 4s infinite backwards;
      }

      vaadin-connection-indicator[offline] .v-loading-indicator,
      vaadin-connection-indicator[reconnecting] .v-loading-indicator {
        display: none;
      }

      .v-status-message {
        opacity: 0;
        width: 100%;
        max-height: var(--status-height-collapsed, 8px);
        overflow: hidden;
        background-color: var(--status-bg-color-online, var(--lumo-primary-color, var(--material-primary-color, blue)));
        color: var(
          --status-text-color-online,
          var(--lumo-primary-contrast-color, var(--material-primary-contrast-color, #fff))
        );
        font-size: 0.75rem;
        font-weight: 600;
        line-height: 1;
        transition: all 0.5s;
        padding: 0 0.5em;
      }

      vaadin-connection-indicator[offline] .v-status-message,
      vaadin-connection-indicator[reconnecting] .v-status-message {
        opacity: 1;
        background-color: var(--status-bg-color-offline, var(--lumo-shade, #333));
        color: var(
          --status-text-color-offline,
          var(--lumo-primary-contrast-color, var(--material-primary-contrast-color, #fff))
        );
        background-image: repeating-linear-gradient(
          45deg,
          rgba(255, 255, 255, 0),
          rgba(255, 255, 255, 0) 10px,
          rgba(255, 255, 255, 0.1) 10px,
          rgba(255, 255, 255, 0.1) 20px
        );
      }

      vaadin-connection-indicator[reconnecting] .v-status-message {
        animation: show-reconnecting-status 2s;
      }

      vaadin-connection-indicator[offline] .v-status-message:hover,
      vaadin-connection-indicator[reconnecting] .v-status-message:hover,
      vaadin-connection-indicator[expanded] .v-status-message {
        max-height: var(--status-height, 1.75rem);
      }

      vaadin-connection-indicator[expanded] .v-status-message {
        opacity: 1;
      }

      .v-status-message span {
        display: flex;
        align-items: center;
        justify-content: center;
        height: var(--status-height, 1.75rem);
      }

      vaadin-connection-indicator[reconnecting] .v-status-message span::before {
        content: '';
        width: 1em;
        height: 1em;
        border-top: 2px solid
          var(--status-spinner-color, var(--lumo-primary-color, var(--material-primary-color, blue)));
        border-left: 2px solid
          var(--status-spinner-color, var(--lumo-primary-color, var(--material-primary-color, blue)));
        border-right: 2px solid transparent;
        border-bottom: 2px solid transparent;
        border-radius: 50%;
        box-sizing: border-box;
        animation: v-spin 0.4s linear infinite;
        margin: 0 0.5em;
      }

      @keyframes v-spin {
        100% {
          transform: rotate(360deg);
        }
      }
    `;
  }

  private getLoadingBarStyle(): string {
    switch (this.loadingBarState) {
      case LoadingBarState.IDLE:
        return 'display: none';
      case LoadingBarState.FIRST:
      case LoadingBarState.SECOND:
      case LoadingBarState.THIRD:
        return 'display: block';
      default:
        return '';
    }
  }

  private timeoutFor(timeoutId: number, enabled: boolean, handler: () => void, delay: number): number {
    if (timeoutId !== 0) {
      window.clearTimeout(timeoutId);
    }

    return enabled ? window.setTimeout(handler, delay) : 0;
  }

  static get instance(): ConnectionIndicator {
    return ConnectionIndicator.create();
  }
}

if (customElements.get('vaadin-connection-indicator') === undefined) {
  customElements.define('vaadin-connection-indicator', ConnectionIndicator);
}

/**
 * The global connection indicator object. Its appearance and behavior can be
 * configured via properties:
 *
 * connectionIndicator.firstDelay = 0;
 * connectionIndicator.onlineText = 'The application is online';
 *
 * To avoid altering the appearance while the indicator is active, apply the
 * configuration in your application 'frontend/index.ts' file.
 */
export const connectionIndicator = ConnectionIndicator.instance;
