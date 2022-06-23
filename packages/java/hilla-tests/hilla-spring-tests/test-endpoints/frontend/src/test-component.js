import { PolymerElement } from '@polymer/polymer/polymer-element.js';
import { html } from '@polymer/polymer/lib/utils/html-tag.js';
import * as appEndpoint from '../generated/AppEndpoint';
import * as packagePrivateEndpoint from '../generated/PackagePrivateEndpoint';
import { AppEndpoint } from '../generated/endpoints';

class TestComponent extends PolymerElement {
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
      <button id="denied" on-click="denied">endpoint denied</button><br />
      <button id="logout" on-click="logout">logout</button><br />
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

  denied(e) {
    appEndpoint
      .denied()
      .then((response) => (this.$.content.textContent = response))
      .catch((error) => (this.$.content.textContent = 'Error:' + error));
  }
}
customElements.define(TestComponent.is, TestComponent);
