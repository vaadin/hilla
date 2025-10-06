import { LoginResult } from '@vaadin/hilla-frontend';
import { RouterLocation, WebComponentInterface } from '@vaadin/router';
import '@vaadin/login/vaadin-login-overlay';
import { html } from 'lit';
import { customElement, state } from 'lit/decorators.js';
import { login } from '../auth';
import { View } from './view';

@customElement('login-view')
export class LoginView extends View implements WebComponentInterface {
  @state()
  private error = false;

  // the url to redirect to after a successful login
  private returnUrl?: string;

  private onSuccess = async (result: LoginResult) => {
    // If a login redirect was initiated by opening a protected URL, the server knows where to go (result.redirectUrl).

    // If a login redirect was initiated by the client router, this.returnUrl knows where to go.

    // If login was opened directly, use the default URL provided by the server.

    // As we do not know if the target is a resource or a Fusion view or a Flow view, we cannot just use Router.go

    // Navigation should happen automatically
    // window.location.href = result.redirectUrl || this.returnUrl || '';
  };

  render() {
    return html` <vaadin-login-overlay opened .error="${this.error}" @login="${this.login}"> </vaadin-login-overlay> `;
  }

  async login(event: CustomEvent): Promise<LoginResult> {
    // @ts-ignore
    window.reloadPending = true;
    this.error = false;
    const result = await login(event.detail.username, event.detail.password);
    this.error = result.error;

    if (!result.error) {
      this.onSuccess(result);
    } else {
      // @ts-ignore
      window.reloadPending = false;
    }

    return result;
  }

  onAfterEnter(location: RouterLocation) {
    this.returnUrl = location.redirectFrom;
  }
}
