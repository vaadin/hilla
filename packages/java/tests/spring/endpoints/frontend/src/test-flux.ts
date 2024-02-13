import { Subscription } from '@vaadin/hilla-frontend';
import { LitElement, html } from 'lit';
import { customElement, query } from 'lit/decorators.js';
import { FluxEndpoint } from '../generated/endpoints';

@customElement('test-flux')
export class TestFlux extends LitElement {
  @query('#content')
  private content!: HTMLElement;

  render() {
    return html`
      <div style="display:flex;flex-direction:column">
        <form method="POST" action="login">
          <input id="username" name="username" />
          <input id="password" name="password" />
          <input id="login" type="submit" />
        </form>

        <button id="denied" @click="${this.denied}">denied()</button>
        <button id="hello" @click="${this.hello}">hello()</button>
        <button id="helloAnonymous" @click="${this.helloAnonymous}">helloAnonymous()</button>
        <button id="helloAdmin" @click="${this.helloAdmin}">helloAdmin()</button>
        <button id="checkUser" @click="${this.checkUser}">checkUser()</button>
        <button id="countTo" @click=${() => this.countTo(5)}>countTo(5)</button>
        <button id="countEvenTo" @click="${() => this.countEvenTo(10)}">countEvenTo(10)</button>
        <button id="countThrowError" @click="${() => this.countThrowError(10)}">countThrowError(10)</button>

        <pre id="content"></pre>
      </div>
    `;
  }

  private testEndpoint(sub: Subscription<any>) {
    this.content.textContent = '';
    sub
      .onNext((value) => {
        this.content.textContent += 'Value: ' + value + '\n';
      })
      .onComplete(() => {
        this.content.textContent += 'Completed' + '\n';
      })
      .onError(() => {
        this.content.textContent += 'Error' + '\n';
      });
  }
  private denied() {
    this.testEndpoint(FluxEndpoint.denied());
  }
  private helloAnonymous() {
    this.testEndpoint(FluxEndpoint.helloAnonymous());
  }
  private hello() {
    this.testEndpoint(FluxEndpoint.hello('John', 'Manager'));
  }
  private helloAdmin() {
    this.testEndpoint(FluxEndpoint.helloAdmin());
  }
  private checkUser() {
    this.testEndpoint(FluxEndpoint.checkUser());
  }
  private countTo(to: number) {
    this.testEndpoint(FluxEndpoint.countTo(to));
  }
  private countEvenTo(to: number) {
    this.testEndpoint(FluxEndpoint.countEvenTo(to));
  }
  private countThrowError(to: number) {
    this.testEndpoint(FluxEndpoint.countThrowError(to));
  }
}
