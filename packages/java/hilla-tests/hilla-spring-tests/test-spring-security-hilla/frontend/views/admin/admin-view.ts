import "@vaadin/vaadin-button";
import "@vaadin/vaadin-text-field";
import { BalanceEndpoint } from "Frontend/generated/BalanceEndpoint";
import { appStore } from "Frontend/stores/app-store";
import {
  customElement,
  html,
  internalProperty,
  PropertyValues,
} from "lit-element";
import { View } from "../view";

@customElement("admin-view")
export class AdminTSView extends View {
  @internalProperty()
  private balance: number = 0;

  render() {
    return html`
      <h1 id="welcome">
        Welcome to the admin page, ${appStore.user!.fullName}
      </h1>
      <div>This page is full of dangerous controls and secret information</div>
    `;
  }

  protected shouldUpdate(_changedProperties: PropertyValues): boolean {
    if (!appStore.user) {
      return false;
    }
    return super.shouldUpdate(_changedProperties);
  }
}
