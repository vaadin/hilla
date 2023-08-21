import {
  _getPropertyModel,
  ArrayModel,
  BooleanModel,
  NumberModel,
  type ModelConstructor,
  ObjectModel,
  Pattern,
  Positive,
  Required,
  Size,
  StringModel,
} from '@hilla/form';

export interface User {
  id: number;
  name: string;
  password: string;
}

export class UserModel<T extends User = User> extends ObjectModel<T> {
  declare static createEmptyValue: () => User;

  get id(): NumberModel {
    return this[_getPropertyModel]('id', NumberModel, [false]);
  }

  get name(): StringModel {
    return this[_getPropertyModel]('name', StringModel, [false, new Required(), new Size({ min: 4 })]);
  }

  get password(): StringModel {
    return this[_getPropertyModel]('password', StringModel, [false, new Required(), new Size({ min: 6 })]);
  }
}

export interface Login {
  user: User;
  rememberMe: boolean;
}

export class LoginModel<T extends Login = Login> extends ObjectModel<T> {
  declare static createEmptyValue: () => Login;

  get user(): UserModel {
    return this[_getPropertyModel]('user', UserModel, [false]);
  }

  get rememberMe(): BooleanModel {
    return this[_getPropertyModel]('rememberMe', BooleanModel, [true]);
  }
}
