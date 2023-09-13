import { ObjectModel, StringModel, _getPropertyModel } from '@hilla/form';

export interface Person {
  firstName: string;
  lastName: string;
}

export class PersonModel<T extends Person = Person> extends ObjectModel<T> {
  declare static createEmptyValue: () => Person;

  get firstName(): StringModel {
    return this[_getPropertyModel]('firstName', StringModel, [false]);
  }

  get lastName(): StringModel {
    return this[_getPropertyModel]('firstName', StringModel, [false]);
  }
}
