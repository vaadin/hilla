import { LitElement, type TemplateResult, html } from 'lit';
import { loginUsingUrl, waitForConsole } from './h2-util';
import type {
  DevToolsInterface,
  DevToolsPlugin,
  MessageHandler,
  ServerMessage,
} from 'Frontend/generated/jar-resources/vaadin-dev-tools/vaadin-dev-tools';

let devTools: DevToolsInterface;

type H2Data = {
  path: string;
  jdbcUrl: string;
};
export class DevToolsDatabase extends LitElement implements MessageHandler {
  declare h2path?: string;

  declare h2jdbcUrl?: string;

  static properties = { h2path: { type: String }, h2jdbcUrl: { type: String } };

  connectedCallback(): void {
    super.connectedCallback();
    this.style.width = '100%';
  }

  handleMessage(message: ServerMessage): boolean {
    if (message.command === 'devtools-database-init') {
      // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access
      const data = message.data.h2 as H2Data | undefined;
      if (data) {
        this.h2path = data.path;
        this.h2jdbcUrl = data.jdbcUrl;
      }
      return true;
    }
    return false;
  }

  render(): TemplateResult {
    return html`<div style="padding: 1em;display:inline-flex;flex-direction:column;">
      <button ?disabled=${!this.h2path} @click=${this.openH2Console}>Open H2 console</button>${!this.h2path
        ? 'H2 is not in use'
        : ''}
    </div>`;
  }

  async openH2Console(): Promise<void> {
    const h2Window = window.open(this.h2path)!;
    await waitForConsole(h2Window);
    loginUsingUrl(h2Window, this.h2jdbcUrl);
  }
}

const plugin: DevToolsPlugin = {
  init(devToolsInterface: DevToolsInterface): void {
    devTools = devToolsInterface;
    devTools.addTab('Database', 'devtools-database');
  },
};

// eslint-disable-next-line @typescript-eslint/no-unsafe-call, @typescript-eslint/no-unsafe-member-access
(window as any).Vaadin.devToolsPlugins.push(plugin);

customElements.define('devtools-database', DevToolsDatabase);
