import {
  _getPropertyModel,
  BooleanModel,
  makeObjectEmptyValueCreator,
  NumberModel,
  ObjectModel,
  Required,
  Size,
  StringModel,
} from '@vaadin/hilla-lit-form';

export interface FormUser {
  name: string;
  password: string;
  passwordHint?: string;
}

export interface User extends FormUser {
  id: number;
}

export class FormUserModel<T extends FormUser = FormUser> extends ObjectModel<T> {
  static override createEmptyValue = makeObjectEmptyValueCreator(FormUserModel);

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
      (parent, key) =>
        new StringModel(parent, key, true /* should be optional */, { validators: [new Size({ max: 10 })] }),
    );
  }
}

export class UserModel<T extends User = User> extends FormUserModel<T> {
  static override createEmptyValue = makeObjectEmptyValueCreator(UserModel);

  get id(): NumberModel {
    return this[_getPropertyModel]('id', (parent, key) => new NumberModel(parent, key, false));
  }
}

export interface Login {
  user: User;
  rememberMe: boolean;
}

export class LoginModel<T extends Login = Login> extends ObjectModel<T> {
  static override createEmptyValue = makeObjectEmptyValueCreator(LoginModel);

  get user(): FormUserModel {
    return this[_getPropertyModel]('user', (parent, key) => new FormUserModel(parent, key, false));
  }

  get rememberMe(): BooleanModel {
    return this[_getPropertyModel]('rememberMe', (parent, key) => new BooleanModel(parent, key, true));
  }
}

export interface Entity {
  projectId?: number;
  contractId?: number;
}

export class EntityModel<T extends Entity> extends ObjectModel<T> {
  static override createEmptyValue = makeObjectEmptyValueCreator(EntityModel);

  get projectId(): NumberModel {
    return this[_getPropertyModel](
      'projectId',
      (parent, key) => new NumberModel(parent, key, true, { meta: { javaType: 'java.lang.Long' } }),
    );
  }

  get contractId(): NumberModel {
    return this[_getPropertyModel](
      'contractId',
      (parent, key) => new NumberModel(parent, key, true, { meta: { javaType: 'java.lang.Long' } }),
    );
  }
}

export interface Project {
  id: number;
  name: string;
}

export interface Contract {
  id: number;
  name: string;
}
