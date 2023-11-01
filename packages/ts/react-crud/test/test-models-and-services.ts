import {
  BooleanModel,
  EnumModel,
  NumberModel,
  ObjectModel,
  StringModel,
  _enum,
  _getPropertyModel,
  makeEnumEmptyValueCreator,
  makeObjectEmptyValueCreator,
} from '@hilla/form';
import type { CrudService } from '../src/crud.js';
import type FilterUnion from '../src/types/dev/hilla/crud/filter/FilterUnion.js';
import Matcher from '../src/types/dev/hilla/crud/filter/PropertyStringFilter/Matcher.js';
import type Pageable from '../src/types/dev/hilla/mappedtypes/Pageable.js';
import type Sort from '../src/types/dev/hilla/mappedtypes/Sort.js';
import Direction from '../src/types/org/springframework/data/domain/Sort/Direction.js';

export interface Company extends HasIdVersion {
  name: string;
  foundedDate: string;
}

export enum Gender {
  MALE = 'MALE',
  FEMALE = 'FEMALE',
  NON_BINARY = 'NON_BINARY',
}

export interface Named {
  firstName: string;
  lastName: string;
}

export interface Person extends HasIdVersion, Named {
  gender: Gender;
  email: string;
  someInteger: number;
  someDecimal: number;
  vip: boolean;
  birthDate: string;
  shiftStart: string;
}

export interface NestedTestValues {
  nestedString: string;
  nestedNumber: number;
  nestedBoolean: boolean;
  nestedDate?: string;
}

export interface ColumnRendererTestValues extends HasIdVersion {
  string: string;
  integer: number;
  decimal: number;
  boolean: boolean;
  enum?: Gender;
  date?: string;
  localDate?: string;
  localTime?: string;
  localDateTime?: string;
  nested?: NestedTestValues;
}

class GenderModel extends EnumModel<typeof Gender> {
  static override createEmptyValue = makeEnumEmptyValueCreator(GenderModel);
  readonly [_enum] = Gender;
}

export class NamedModel<T extends Named = Named> extends ObjectModel<T> {
  static override createEmptyValue = makeObjectEmptyValueCreator(NamedModel);

  get firstName(): StringModel {
    return this[_getPropertyModel]('firstName', (parent, key) => new StringModel(parent, key, false));
  }

  get lastName(): StringModel {
    return this[_getPropertyModel]('lastName', (parent, key) => new StringModel(parent, key, false));
  }
}

export class PersonModel<T extends Person = Person> extends NamedModel<T> {
  static override createEmptyValue = makeObjectEmptyValueCreator(PersonModel);

  get id(): NumberModel {
    return this[_getPropertyModel](
      'id',
      (parent, key) =>
        new NumberModel(parent, key, false, { meta: { annotations: [{ name: 'jakarta.persistence.Id' }] } }),
    );
  }

  get version(): NumberModel {
    return this[_getPropertyModel](
      'version',
      (parent, key) =>
        new NumberModel(parent, key, false, { meta: { annotations: [{ name: 'jakarta.persistence.Version' }] } }),
    );
  }

  get gender(): GenderModel {
    return this[_getPropertyModel]('gender', (parent, key) => new GenderModel(parent, key, false));
  }

  get email(): StringModel {
    return this[_getPropertyModel]('email', (parent, key) => new StringModel(parent, key, false));
  }

  get someInteger(): NumberModel {
    return this[_getPropertyModel](
      'someInteger',
      (parent, key) => new NumberModel(parent, key, false, { meta: { javaType: 'int' } }),
    );
  }

  get someDecimal(): NumberModel {
    return this[_getPropertyModel](
      'someDecimal',
      (parent, key) => new NumberModel(parent, key, false, { meta: { javaType: 'float' } }),
    );
  }

  get vip(): BooleanModel {
    return this[_getPropertyModel]('vip', (parent, key) => new BooleanModel(parent, key, false));
  }

  get birthDate(): StringModel {
    return this[_getPropertyModel](
      'birthDate',
      (parent, key) => new StringModel(parent, key, false, { meta: { javaType: 'java.time.LocalDate' } }),
    );
  }

  get shiftStart(): StringModel {
    return this[_getPropertyModel](
      'shiftStart',
      (parent, key) => new StringModel(parent, key, false, { meta: { javaType: 'java.time.LocalTime' } }),
    );
  }
}

export class CompanyModel<T extends Company = Company> extends ObjectModel<T> {
  declare static createEmptyValue: () => Company;

  get id(): NumberModel {
    return this[_getPropertyModel](
      'id',
      (parent, key) =>
        new NumberModel(parent, key, false, { meta: { annotations: [{ name: 'jakarta.persistence.Id' }] } }),
    );
  }

  get version(): NumberModel {
    return this[_getPropertyModel](
      'version',
      (parent, key) =>
        new NumberModel(parent, key, false, { meta: { annotations: [{ name: 'jakarta.persistence.Version' }] } }),
    );
  }

  get name(): StringModel {
    return this[_getPropertyModel]('name', (parent, key) => new StringModel(parent, key, false));
  }

  get foundedDate(): StringModel {
    return this[_getPropertyModel]('foundedDate', (parent, key) => new StringModel(parent, key, false));
  }
}

export class NestedTestModel<T extends NestedTestValues = NestedTestValues> extends ObjectModel<T> {
  declare static createEmptyValue: () => Company;

  get nestedString(): StringModel {
    return this[_getPropertyModel]('nestedString', (parent, key) => new StringModel(parent, key, false));
  }

  get nestedNumber(): NumberModel {
    return this[_getPropertyModel](
      'nestedNumber',
      (parent, key) => new NumberModel(parent, key, false, { meta: { javaType: 'int' } }),
    );
  }

  get nestedBoolean(): BooleanModel {
    return this[_getPropertyModel]('nestedBoolean', (parent, key) => new BooleanModel(parent, key, false));
  }

  get nestedDate(): StringModel {
    return this[_getPropertyModel](
      'nestedDate',
      (parent, key) => new StringModel(parent, key, false, { meta: { javaType: 'java.util.Date' } }),
    );
  }
}

export class ColumnRendererTestModel<
  T extends ColumnRendererTestValues = ColumnRendererTestValues,
> extends ObjectModel<T> {
  declare static createEmptyValue: () => Company;

  get id(): NumberModel {
    return this[_getPropertyModel](
      'id',
      (parent, key) =>
        new NumberModel(parent, key, false, { meta: { annotations: [{ name: 'jakarta.persistence.Id' }] } }),
    );
  }

  get string(): StringModel {
    return this[_getPropertyModel]('string', (parent, key) => new StringModel(parent, key, false));
  }

  get integer(): NumberModel {
    return this[_getPropertyModel](
      'integer',
      (parent, key) => new NumberModel(parent, key, false, { meta: { javaType: 'int' } }),
    );
  }

  get decimal(): NumberModel {
    return this[_getPropertyModel](
      'decimal',
      (parent, key) => new NumberModel(parent, key, false, { meta: { javaType: 'float' } }),
    );
  }

  get boolean(): BooleanModel {
    return this[_getPropertyModel]('boolean', (parent, key) => new BooleanModel(parent, key, false));
  }

  get enum(): GenderModel {
    return this[_getPropertyModel]('enum', (parent, key) => new GenderModel(parent, key, false));
  }

  get date(): StringModel {
    return this[_getPropertyModel](
      'date',
      (parent, key) => new StringModel(parent, key, false, { meta: { javaType: 'java.util.Date' } }),
    );
  }

  get localDate(): StringModel {
    return this[_getPropertyModel](
      'localDate',
      (parent, key) => new StringModel(parent, key, false, { meta: { javaType: 'java.time.LocalDate' } }),
    );
  }

  get localTime(): StringModel {
    return this[_getPropertyModel](
      'localTime',
      (parent, key) => new StringModel(parent, key, false, { meta: { javaType: 'java.time.LocalTime' } }),
    );
  }

  get localDateTime(): StringModel {
    return this[_getPropertyModel](
      'localDateTime',
      (parent, key) => new StringModel(parent, key, false, { meta: { javaType: 'java.time.LocalDateTime' } }),
    );
  }

  get nested(): NestedTestModel {
    return this[_getPropertyModel](
      'nested',
      (parent, key) =>
        new NestedTestModel(parent, key, false, { meta: { annotations: [{ name: 'jakarta.persistence.OneToOne' }] } }),
    );
  }
}

type HasIdVersion = {
  id: number;
  version: number;
};

export const createService = <T extends HasIdVersion>(initialData: T[]): CrudService<T> & HasTestInfo => {
  let _lastSort: Sort | undefined;
  let _lastFilter: FilterUnion | undefined;
  let data = initialData;
  let _callCount = 0;

  return {
    // eslint-disable-next-line @typescript-eslint/require-await
    async list(request: Pageable, filter: FilterUnion | undefined): Promise<T[]> {
      _lastSort = request.sort;
      _lastFilter = filter;
      _callCount += 1;

      let filteredData: T[] = [];
      if (request.pageNumber === 0) {
        if (filter && filter['@type'] === 'propertyString') {
          filteredData = data.filter((item) => {
            const propertyValue = (item as Record<string, any>)[filter.propertyId];
            if (filter.matcher === Matcher.CONTAINS && typeof propertyValue === 'string') {
              return propertyValue.includes(filter.filterValue);
            }
            return propertyValue === filter.filterValue;
          });
        } else {
          filteredData = data;
        }
      }

      if (request.sort.orders.length === 1) {
        const sortPropertyId = request.sort.orders[0]!.property;
        const directionMod = request.sort.orders[0]!.direction === Direction.ASC ? 1 : -1;
        filteredData.sort((a, b) =>
          // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access
          (a as any)[sortPropertyId] > (b as any)[sortPropertyId] ? Number(directionMod) : -1 * directionMod,
        );
      }
      return filteredData;
    },
    // eslint-disable-next-line @typescript-eslint/require-await
    async save(value: T): Promise<T | undefined> {
      const currentValue = data.find((item) => item.id === value.id);
      if (currentValue) {
        if (currentValue.version !== value.version) {
          // Trying to update an old value
          throw new Error('Trying to update an old value');
        }
      }
      const newValue = { ...value };
      if (currentValue) {
        newValue.version = currentValue.version + 1;
        data = data.map((item) => (item.id === newValue.id ? newValue : item));
      } else {
        newValue.id = data.map((item) => item.id).reduce((prev, curr) => Math.max(prev, curr)) + 1;
        newValue.version = 1;
        data = [...data, newValue];
      }
      return data.find((item) => item.id === newValue.id);
    },
    // eslint-disable-next-line
    async delete(id: any): Promise<void> {
      data = data.filter((item) => item.id !== id);
    },
    get lastSort() {
      return _lastSort;
    },
    get lastFilter() {
      return _lastFilter;
    },
    get callCount() {
      return _callCount;
    },
  };
};

export const personData: Person[] = [
  {
    id: 1,
    version: 1,
    firstName: 'John',
    lastName: 'Dove',
    gender: Gender.MALE,
    email: 'john@example.com',
    someInteger: -12,
    someDecimal: 0.12,
    vip: true,
    birthDate: '1999-12-31',
    shiftStart: '08:30',
  },
  {
    id: 2,
    version: 1,
    firstName: 'Jane',
    lastName: 'Love',
    gender: Gender.FEMALE,
    email: 'jane@example.com',
    someInteger: 123456,
    someDecimal: 123.456,
    vip: false,
    birthDate: '1999-12-31',
    shiftStart: '08:30',
  },
];

export const companyData: Company[] = [
  { id: 1, version: 1, name: 'Vaadin Ltd', foundedDate: '2000-05-06' },
  { id: 2, version: 1, name: 'Google', foundedDate: '1998-09-04' },
];

export const columnRendererTestData: ColumnRendererTestValues[] = [
  {
    id: 1,
    version: 1,
    string: 'Hello World 1',
    integer: 123456,
    decimal: 123.456,
    boolean: true,
    enum: Gender.MALE,
    date: '2021-05-13T00:00:00',
    localDate: '2021-05-13',
    localTime: '08:45:00',
    localDateTime: '2021-05-13T08:45:00',
    nested: {
      nestedString: 'Nested string 1',
      nestedNumber: 123456,
      nestedBoolean: true,
      nestedDate: '2021-05-13T00:00:00',
    },
  },
  {
    id: 2,
    version: 1,
    string: 'Hello World 2',
    integer: -12,
    decimal: -0.12,
    boolean: false,
    enum: Gender.FEMALE,
    date: '2021-05-14T00:00:00',
    localDate: '2021-05-14',
    localTime: '20:45:00',
    localDateTime: '2021-05-14T20:45:00',
  },
  {
    id: 3,
    version: 1,
    string: 'Hello World 3',
    enum: Gender.NON_BINARY,
    integer: 123456,
    decimal: 123.4,
    boolean: false,
  },
  {
    id: 4,
    version: 1,
    string: 'Hello World 4',
    integer: -12,
    decimal: -12,
    boolean: false,
    date: 'foo',
    localDate: 'foo',
    localTime: 'foo',
    localDateTime: 'foo',
  },
];

export type HasTestInfo = {
  lastSort: Sort | undefined;
  lastFilter: FilterUnion | undefined;
  callCount: number;
};

export const personService = (): CrudService<Person> & HasTestInfo => createService(personData);
export const companyService = (): CrudService<Company> & HasTestInfo => createService(companyData);
export const columnRendererTestService = (): CrudService<ColumnRendererTestValues> & HasTestInfo =>
  createService(columnRendererTestData);

const noSort: Sort = { orders: [] };

export async function getItem<T extends HasIdVersion>(
  service: CrudService<T> & HasTestInfo,
  id: number,
): Promise<T | undefined> {
  return (await service.list({ pageNumber: 0, pageSize: 1000, sort: noSort }, undefined)).find((p) => p.id === id);
}
