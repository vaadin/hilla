import "@vaadin/vaadin-button";
import "@vaadin/vaadin-text-field";
import { BalanceEndpoint } from "Frontend/generated/endpoints";
import { appStore } from "Frontend/stores/app-store";
import { customElement, html, state, PropertyValues } from "lit-element";
import { View } from "../view";

@customElement("private-view")
export class PrivateTSView extends View {
  @state()
  private balance: number = 0;

  render() {
    return html`
      <div
        style="display:flex;flex-direction:column;align-items:flex-start;padding: var(--lumo-space-m);"
      >
        <span id="balanceText"
          >Hello ${appStore.user!.fullName}, your bank account balance is
          $${this.balance}.</span
        >

        <vaadin-button @click="${this.applyForLoan}"
          >Apply for a loan</vaadin-button
        >
      </div>
    `;
  }

  async applyForLoan() {
    await BalanceEndpoint.applyForLoan();
    this.balance = (await BalanceEndpoint.getBalance()) ?? 0;
  }

  async connectedCallback() {
    super.connectedCallback();
    this.balance = (await BalanceEndpoint.getBalance()) ?? 0;
  }
  protected shouldUpdate(_changedProperties: PropertyValues): boolean {
    if (!appStore.user) {
      return false;
    }
    return super.shouldUpdate(_changedProperties);
  }
}
