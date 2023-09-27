import {
  _getPropertyModel,
  BooleanModel,
  makeObjectEmptyValueCreator,
  NumberModel,
  ObjectModel,
  Required,
  Size,
  StringModel,
} from '@hilla/form';

export interface User {
  id: number;
  name: string;
  password: string;
  passwordHint?: string;
}

export class UserModel<T extends User = User> extends ObjectModel<T> {
  static override createEmptyValue = makeObjectEmptyValueCreator(UserModel);

  get id(): NumberModel {
    return this[_getPropertyModel]('id', (parent, key) => new NumberModel(parent, key, false));
  }

  get name(): StringModel {
    return this[_getPropertyModel](
      'name',
      (parent, key) => new StringModel(parent, key, false, { validators: [new Required(), new Size({ max: 10 })] }),
    );
  }

  get password(): StringModel {
    return this[_getPropertyModel](
      'password',
      (parent, key) => new StringModel(parent, key, false, { validators: [new Required(), new Size({ min: 6 })] }),
    );
  }

  get passwordHint(): StringModel {
    return this[_getPropertyModel](
      'passwordHint',
      (parent, key) => new StringModel(parent, key, true /* should be optional */),
    );
  }
}

export interface Login {
  user: User;
  rememberMe: boolean;
}

export class LoginModel<T extends Login = Login> extends ObjectModel<T> {
  static override createEmptyValue = makeObjectEmptyValueCreator(LoginModel);

  get user(): UserModel {
    return this[_getPropertyModel]('user', (parent, key) => new UserModel(parent, key, false));
  }

  get rememberMe(): BooleanModel {
    return this[_getPropertyModel]('rememberMe', (parent, key) => new BooleanModel(parent, key, true));
  }
}
