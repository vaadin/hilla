import '@vaadin/button';
import '@vaadin/text-field';
import {BalanceEndpoint, PublicEndpoint} from 'Frontend/generated/endpoints';
import { appStore } from 'Frontend/stores/app-store';
import { html, PropertyValues } from 'lit';
import { customElement, state } from 'lit/decorators.js';
import { View } from '../view';

@customElement('private-view')
export class PrivateTSView extends View {
  @state()
  private balance: number = 0;

  @state()
  private balanceUpdates: ReadonlyArray<number | undefined> = [];

  render() {
    return html`
      <div style="display:flex;flex-direction:column;align-items:flex-start;padding: var(--lumo-space-m);">
        <span id="balanceText">Hello ${appStore.user!.fullName}, your bank account balance is $${this.balance}.</span>

        <vaadin-button @click="${this.applyForLoan}">Apply for a loan</vaadin-button>
      </div>
      <div>
        Latest balance updates:
        <output id="balanceUpdates">${this.balanceUpdates.join(' ')}</output>
      </div>
    `;
  }

  async applyForLoan() {
    await BalanceEndpoint.applyForLoan();
    this.balance = (await BalanceEndpoint.getBalance()) ?? 0;
  }

  async connectedCallback() {
    super.connectedCallback();
    this.subscribeToBalanceUpdates();
    this.balance = (await BalanceEndpoint.getBalance()) ?? 0;
  }
  protected shouldUpdate(_changedProperties: PropertyValues): boolean {
    if (!appStore.user) {
      return false;
    }
    return super.shouldUpdate(_changedProperties);
  }

  public subscribeToBalanceUpdates() {
    BalanceEndpoint.getBalanceUpdates()
      .onNext((balance: number | undefined) =>
        this.balanceUpdates = [
          ...this.balanceUpdates,
          balance,
        ]
      );
  }
}
