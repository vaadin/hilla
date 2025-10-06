import { PolymerElement } from '@polymer/polymer/polymer-element.js';
import { html } from '@polymer/polymer/lib/utils/html-tag.js';
import * as appEndpoint from '../generated/AppEndpoint';
import * as packagePrivateEndpoint from '../generated/PackagePrivateEndpoint';
import { AppEndpoint, PagedEndpoint } from '../generated/endpoints';
import Direction from '../generated/org/springframework/data/domain/Sort/Direction';

class TestComponent extends PolymerElement {
  #boundSwMessageListener;

  constructor(props) {
    super(props);
    this.#boundSwMessageListener = this.swMessageListener.bind(this);
  }

  static get template() {
    return html`
      <button id="button">vaadin hello</button><br />
      <button id="hello" on-click="hello">endpoint hello</button><br />
      <button id="helloAnonymous" on-click="helloAnonymous">endpoint helloAnonymous</button><br />
      <button id="helloAnonymousWrapper" on-click="helloAnonymousWrapper">endpoint AppEndpoint.helloAnonymous</button
      ><br />
      <button id="echoWithOptional" on-click="echoWithOptional">endpoint echoWithOptional</button><br />
      <button id="helloAdmin" on-click="helloAdmin">endpoint helloAdmin</button><br />
      <button id="checkUser" on-click="checkUser">endpoint checkUser</button><br />
      <button id="checkUserFromVaadinRequest" on-click="checkUserFromVaadinRequest">
        endpoint checkUser from VaadinRequest</button
      ><br />
      <button id="helloFromPackagePrivate" on-click="hello">package private endpoint hello</button><br />
      <button id="helloAnonymousFromPackagePrivate" on-click="helloAnonymousFromPackagePrivateEndpoint">
        package private endpoint helloAnonymous</button
      ><br />
      <button id="getObjectWithNullValues" on-click="getObjectWithNullValues">
        Get Object With Null Values From Endpoint</button
      ><br />
      <button id="pageOne" on-click="getPageOne">Get page one</button>
      <button id="pageTwo" on-click="getPageTwo">Get page two</button>
      <button id="pageOfEntities" on-click="getPageOfEntities">Get page of entities</button>
      <button id="denied" on-click="denied">endpoint denied</button><br />
      <button id="logout" on-click="logout">logout</button><br />
      <button id="helloAnonymousFromServiceWorker" on-click="helloAnonymousFromServiceWorker">helloAnonymous from serviceWorker</button><br />
      <form method="POST" action="login">
        <input id="username" name="username" />
        <input id="password" name="password" />
        <input id="login" type="submit" />
      </form>
      <div id="content"></div>
    `;
  }

  static get is() {
    return 'test-component';
  }

  async logout() {
    await fetch('logout');
  }

  connectedCallback() {
    super.connectedCallback();
    navigator.serviceWorker?.addEventListener('message', this.#boundSwMessageListener);
  }

  disconnectedCallback() {
    super.disconnectedCallback();
    navigator.serviceWorker?.removeEventListener('message', this.#boundSwMessageListener);
  }

  swMessageListener(event) {
    if (event.data && event.data.type === 'sw-app-message') {
      this.$.content.textContent = event.data.text;
    }
  }

  hello(e) {
    appEndpoint
      .hello('Friend')
      .then((response) => (this.$.content.textContent = response))
      .catch((error) => (this.$.content.textContent = 'Error:' + error));
  }

  helloAnonymous(e) {
    appEndpoint
      .helloAnonymous()
      .then((response) => (this.$.content.textContent = response))
      .catch((error) => (this.$.content.textContent = 'Error:' + error));
  }

  helloAnonymousWrapper(e) {
    AppEndpoint.helloAnonymous()
      .then((response) => (this.$.content.textContent = response))
      .catch((error) => (this.$.content.textContent = 'Error:' + error));
  }

  helloAnonymousFromPackagePrivateEndpoint(e) {
    packagePrivateEndpoint
      .helloAnonymous()
      .then((response) => (this.$.content.textContent = response))
      .catch((error) => (this.$.content.textContent = 'Error:' + error));
  }

  echoWithOptional(e) {
    appEndpoint
      .echoWithOptional('one', undefined, 'three', 'four')
      .then((response) => (this.$.content.textContent = response))
      .catch((error) => (this.$.content.textContent = 'Error:' + error));
  }

  helloAdmin(e) {
    appEndpoint
      .helloAdmin()
      .then((response) => (this.$.content.textContent = response))
      .catch((error) => (this.$.content.textContent = 'Error:' + error));
  }

  checkUser(e) {
    appEndpoint
      .checkUser()
      .then((response) => (this.$.content.textContent = response))
      .catch((error) => (this.$.content.textContent = 'Error:' + error));
  }

  checkUserFromVaadinRequest(e) {
    appEndpoint
      .checkUserFromVaadinRequest()
      .then((response) => (this.$.content.textContent = response))
      .catch((error) => (this.$.content.textContent = 'Error:' + error));
  }

  getObjectWithNullValues(e) {
    appEndpoint
      .getObjectWithNullValues()
      .then((response) => (this.$.content.textContent = '' + response['propWithNullValue']))
      .catch((error) => (this.$.content.textContent = 'Error:' + error));
  }

  getPageOne(e) {
    PagedEndpoint.list(undefined)
      .then((response) => {
        this.$.content.textContent = JSON.stringify(response);
      })
      .catch((error) => {
        this.$.content.textContent = 'Error:' + error;
      });
  }

  getPageTwo() {
    PagedEndpoint.list({
      pageNumber: 1,
      pageSize: 2,
      sort: {
        orders: [
          {
            property: 'qty',
            direction: Direction.DESC,
            ignoreCase: false,
          },
        ],
      },
    })
      .then((response) => {
        this.$.content.textContent = JSON.stringify(response);
      })
      .catch((error) => {
        this.$.content.textContent = 'Error:' + error;
      });
  }

  getPageOfEntities(e) {
    PagedEndpoint.listEntities(undefined)
      .then((response) => {
        this.$.content.textContent = JSON.stringify(response);
      })
      .catch((error) => {
        this.$.content.textContent = 'Error:' + error;
      });
  }

  denied(e) {
    appEndpoint
      .denied()
      .then((response) => (this.$.content.textContent = response))
      .catch((error) => (this.$.content.textContent = 'Error:' + error));
  }

  helloAnonymousFromServiceWorker(e) {
    window.navigator.serviceWorker?.ready.then((registration) => {
      registration.active.postMessage('helloAnonymous');
    });
  }
}
customElements.define(TestComponent.is, TestComponent);
