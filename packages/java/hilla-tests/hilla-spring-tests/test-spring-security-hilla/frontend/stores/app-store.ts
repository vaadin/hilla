import { RouterLocation } from '@vaadin/router';
import UserInfo from 'Frontend/generated/com/vaadin/flow/spring/fusionsecurity/data/UserInfo';
import { UserInfoEndpoint } from 'Frontend/generated/endpoints';
import { makeAutoObservable } from 'mobx';
export class AppStore {
  applicationName = 'Bank of Vaadin';

  // The location, relative to the base path, e.g. "hello" when viewing "/hello"
  location = '';

  currentViewTitle = '';

  user: UserInfo | undefined = undefined;

  constructor() {
    makeAutoObservable(this);
  }

  setLocation(location: RouterLocation) {
    if (location.route) {
      this.location = location.route.path;
    } else if (location.pathname.startsWith(location.baseUrl)) {
      this.location = location.pathname.substr(location.baseUrl.length);
    } else {
      this.location = location.pathname;
    }
    const route = location?.route as any;

    this.currentViewTitle = route?.title || '';
  }

  async fetchUserInfo() {
    this.user = await UserInfoEndpoint.getUserInfo();
  }
  clearUserInfo() {
    this.user = undefined;
  }

  get loggedIn() {
    return !!this.user;
  }
  isUserInRole(role: string) {
    return this.user?.roles?.includes(role);
  }
}
export const appStore = new AppStore();
