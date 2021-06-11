import { Router } from "@vaadin/router";
import "@vaadin/vaadin-app-layout";
import "@vaadin/vaadin-app-layout/vaadin-drawer-toggle";
import "@vaadin/vaadin-avatar/vaadin-avatar";
import "@vaadin/vaadin-tabs";
import "@vaadin/vaadin-tabs/vaadin-tab";
import { logout } from "Frontend/auth";
import { customElement, html } from "lit-element";
import { nothing } from "lit-html";
import { router } from "../index";
import { appStore } from "../stores/app-store";
import { Layout } from "./view";

interface RouteInfo {
  path: string;
  title: string;
  requiresAuthentication?: boolean;
  requiresRole?: string;
  disable?: boolean;
}
@customElement("main-view")
export class MainView extends Layout {
  render() {
    return html`
      <vaadin-app-layout primary-section="drawer">
        <header slot="navbar" theme="dark">
          <vaadin-drawer-toggle></vaadin-drawer-toggle>
          <h1>${appStore.currentViewTitle}</h1>
          ${appStore.user
            ? html`<vaadin-avatar
                img="${appStore.user.imageUrl}"
                name="${appStore.user.username}"
              ></vaadin-avatar>`
            : html`<a router-ignore href="login">Sign in</a>`}
        </header>

        <div slot="drawer">
          <div id="logo">
            <a href="">
              <img
                style="text-align: center"
                src="public/images/logo.jpg"
                alt="${appStore.applicationName} logo"
              />
            </a>
          </div>
          <hr />
          <vaadin-tabs
            orientation="vertical"
            theme="minimal"
            .selected=${this.getSelectedViewRoute()}
          >
            ${this.getMenuRoutes().map(
              (viewRoute) => html`
                <vaadin-tab>
                  <a href="${viewRoute.path}" tabindex="-1"
                    >${viewRoute.title}${viewRoute.disable
                      ? html` (hidden)`
                      : nothing}</a
                  >
                </vaadin-tab>
              `
            )}
            <vaadin-tab
              ><vaadin-button id="logout" @click=${this.logout} tabindex="-1"
                >Logout${!appStore.user
                  ? html` (hidden)`
                  : nothing}</vaadin-button
              ></vaadin-tab
            >
          </vaadin-tabs>
        </div>
        <slot></slot>
      </vaadin-app-layout>
    `;
  }

  private logout() {
    logout();
    Router.go(router.urlForName('public'));
  }
  private getMenuRoutes(): RouteInfo[] {
    const views: RouteInfo[] = [
      {
        path: "",
        title: "Public",
      },
      {
        path: "form",
        title: "Fusion Form",
      },
      {
        path: "private",
        title: "Private",
        requiresAuthentication: true,
      },
      {
        path: "admin",
        title: "Admin",
        requiresRole: "admin",
      },
    ];
    views.forEach((route) => {
      if (route.requiresAuthentication && !appStore.user) {
        route.disable = true;
      } else if (
        route.requiresRole &&
        !appStore.isUserInRole(route.requiresRole)
      ) {
        route.disable = true;
      }
    });
    return views;
  }

  private getSelectedViewRoute(): number {
    return this.getMenuRoutes().findIndex(
      (viewRoute) => viewRoute.path == appStore.location
    );
  }
  connectedCallback() {
    super.connectedCallback();
    this.id = "main-view";
  }
}
