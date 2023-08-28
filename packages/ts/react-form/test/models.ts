import { _getPropertyModel, BooleanModel, NumberModel, ObjectModel, Required, Size, StringModel } from '@hilla/form';

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
    return this[_getPropertyModel]('name', StringModel, [false, new Required(), new Size({ max: 10 })]);
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

export interface Valued {
  value?: string;
}

export class ValuedModel<T extends Valued = Valued> extends ObjectModel<T> {
  declare static createEmptyValue: () => Valued;

  get value(): StringModel {
    return this[_getPropertyModel]('value', StringModel, [true]);
  }
}
