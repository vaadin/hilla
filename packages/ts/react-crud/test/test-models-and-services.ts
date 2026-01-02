import m, { BooleanModel, NumberModel, StringModel } from '@vaadin/hilla-models';
import type { CountService, CrudService, ListService } from '../src/crud.js';
import type FilterUnion from '../src/types/com/vaadin/hilla/crud/filter/FilterUnion.js';
import Matcher from '../src/types/com/vaadin/hilla/crud/filter/PropertyStringFilter/Matcher.js';
import type Pageable from '../src/types/com/vaadin/hilla/mappedtypes/Pageable.js';
import type Sort from '../src/types/com/vaadin/hilla/mappedtypes/Sort.js';
import Direction from '../src/types/org/springframework/data/domain/Sort/Direction.js';

const intModel = m.meta(NumberModel, { jvmType: 'int' });
const floatModel = m.meta(NumberModel, { jvmType: 'float' });

const LocalDateModel = m.meta(StringModel, { jvmType: 'java.time.LocalDate' });
const LocalTimeModel = m.meta(StringModel, { jvmType: 'java.time.LocalTime' });
const LocalDateTimeModel = m.meta(StringModel, { jvmType: 'java.time.LocalDateTime' });

type HasIdVersion = {
  id?: number;
  version?: number;
};
const idNumberModel = m.meta(m.optional(NumberModel), { annotations: [{ jvmType: 'jakarta.persistence.Id' }] });
const versionNumberModel = m.meta(m.optional(NumberModel), {
  annotations: [{ jvmType: 'jakarta.persistence.Version' }],
});

export interface Company extends HasIdVersion {
  name: string;
  foundedDate: string;
}
export const CompanyModel = m
  .object<Company>('Company')
  .property('id', idNumberModel)
  .property('version', versionNumberModel)
  .property('name', StringModel)
  .property('foundedDate', StringModel)
  .build();
export type CompanyModel = typeof CompanyModel;

export enum Gender {
  MALE = 'MALE',
  FEMALE = 'FEMALE',
  NON_BINARY = 'NON_BINARY',
}
export const GenderModel = m.enum(Gender, 'Gender');
export type GenderModel = typeof GenderModel;

export interface Named {
  firstName: string;
  lastName: string;
}
export const NamedModel = m
  .object<Named>('Named')
  .property('firstName', StringModel)
  .property('lastName', StringModel)
  .build();
export type NamedModel = typeof NamedModel;

export interface Address {
  street: string;
  city: string;
  country: string;
}
export const AddressModel = m
  .object<Address>('Address')
  .property('street', StringModel)
  .property('city', StringModel)
  .property('country', StringModel)
  .build();
export type AddressModel = typeof AddressModel;

export interface Department extends HasIdVersion {
  name?: string;
}
export const DepartmentModel = m
  .object<Department>('Department')
  .property('id', idNumberModel)
  .property('version', versionNumberModel)
  .property('name', StringModel)
  .build();
export type DepartmentModel = typeof DepartmentModel;

export interface Person extends HasIdVersion, Named {
  gender: Gender;
  email: string;
  someInteger: number;
  someDecimal: number;
  vip: boolean;
  birthDate: string;
  shiftStart: string;
  appointmentTime: string;
  address?: Address;
  department?: Department;
}
export const PersonModel = m
  .extend(NamedModel)
  .object<Person>('Person')
  .property('id', idNumberModel)
  .property('version', versionNumberModel)
  .property('gender', GenderModel)
  .property('email', StringModel)
  .property('someInteger', intModel)
  .property('someDecimal', floatModel)
  .property('vip', BooleanModel)
  .property('birthDate', LocalDateModel)
  .property('shiftStart', LocalTimeModel)
  .property('appointmentTime', LocalDateTimeModel)
  .property('address', m.meta(AddressModel, { annotations: [{ jvmType: 'jakarta.persistence.OneToOne' }] }))
  .property('department', m.meta(DepartmentModel, { annotations: [{ jvmType: 'jakarta.persistence.ManyToOne' }] }))
  .build();
export type PersonModel = typeof PersonModel;

export interface NestedTestValues {
  nestedString: string;
  nestedNumber: number;
  nestedBoolean: boolean;
  nestedDate?: string;
}
export const NestedTestModel = m
  .object<NestedTestValues>('NestedTest')
  .property('nestedString', StringModel)
  .property('nestedNumber', intModel)
  .property('nestedBoolean', BooleanModel)
  .property('nestedDate', LocalDateModel)
  .build();
export type NestedTestModel = typeof NestedTestModel;

export interface ColumnRendererTestValues extends HasIdVersion {
  string: string;
  integer: number;
  decimal: number;
  boolean: boolean;
  enum?: Gender;
  localDate?: string;
  localTime?: string;
  localDateTime?: string;
  nested?: NestedTestValues;
}
export const ColumnRendererTestModel = m
  .object<ColumnRendererTestValues>('ColumnRendererTest')
  .property('id', idNumberModel)
  .property('version', versionNumberModel)
  .property('string', StringModel)
  .property('integer', intModel)
  .property('decimal', floatModel)
  .property('boolean', BooleanModel)
  .property('enum', m.optional(GenderModel))
  .property('localDate', m.optional(LocalDateModel))
  .property('localTime', m.optional(LocalTimeModel))
  .property('localDateTime', m.optional(LocalDateTimeModel))
  .property(
    'nested',
    m.meta(m.optional(NestedTestModel), { annotations: [{ jvmType: 'jakarta.persistence.OneToOne' }] }),
  )
  .build();
export type ColumnRendererTestModel = typeof ColumnRendererTestModel;

export interface UserData extends HasIdVersion {
  name?: string;
}
export const UserDataModel = m
  .object<UserData>('UserData')
  .property('id', idNumberModel)
  .property('version', versionNumberModel)
  .property('name', m.optional(StringModel))
  .build();
export type UserDataModel = typeof UserDataModel;

export const PersonWithSimpleIdPropertyModel = m
  .extend(PersonModel)
  .object<Person>('PersonWithSimpleIdProperty')
  .property('id', NumberModel)
  .build();
export type PersonWithSimpleIdPropertyModel = typeof PersonWithSimpleIdPropertyModel;

export const createService = <T extends HasIdVersion>(
  initialData: T[],
): CountService<T> & CrudService<T> & HasTestInfo => {
  let _lastSort: Sort | undefined;
  let _lastFilter: FilterUnion | undefined;
  let data = initialData;
  let _callCount = 0;

  function filterData(filter: FilterUnion | undefined): T[] {
    if (filter && filter['@type'] === 'propertyString') {
      return data.filter((item) => {
        const propertyValue = (item as Record<string, any>)[filter.propertyId];
        if (filter.matcher === Matcher.CONTAINS && typeof propertyValue === 'string') {
          return propertyValue.includes(filter.filterValue);
        }
        return propertyValue === filter.filterValue;
      });
    }
    return data;
  }

  return {
    // eslint-disable-next-line @typescript-eslint/require-await
    async list(request: Pageable, filter: FilterUnion | undefined): Promise<T[]> {
      _lastSort = request.sort;
      _lastFilter = filter;
      _callCount += 1;

      let filteredData: T[] = [];
      if (request.pageNumber === 0) {
        filteredData = filterData(filter);
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
        newValue.version = currentValue.version ? currentValue.version + 1 : 0;
        data = data.map((item) => (item.id === newValue.id ? newValue : item));
      } else {
        newValue.id = data.map((item) => item.id ?? 0).reduce((prev, curr) => Math.max(prev, curr)) + 1;
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
    async count(filter: FilterUnion | undefined): Promise<number> {
      return Promise.resolve(filterData(filter).length);
    },
  };
};

export const createListService = <T extends HasIdVersion>(initialData: T[]): HasTestInfo & ListService<T> => {
  const service = createService(initialData);
  return {
    callCount: service.callCount,
    lastFilter: service.lastFilter,
    lastSort: service.lastSort,
    list: async (request: Pageable, filter: FilterUnion | undefined): Promise<T[]> => service.list(request, filter),
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
    appointmentTime: '2021-05-13T08:45',
    address: {
      street: '122 North Street',
      city: 'North Town',
      country: 'US',
    },
    department: {
      id: 1,
      version: 1,
      name: 'Sales',
    },
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
    appointmentTime: '2025-08-21T14:30',
    address: {
      street: '122 South Street',
      city: 'South Town',
      country: 'US',
    },
    department: {
      id: 2,
      version: 1,
      name: 'IT',
    },
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
    localDate: '2021-05-13',
    localTime: '08:45:00',
    localDateTime: '2021-05-13T08:45:00',
    nested: {
      nestedString: 'Nested string 1',
      nestedNumber: 123456,
      nestedBoolean: true,
      nestedDate: '2021-05-13',
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
    localDate: 'foo',
    localTime: 'foo',
    localDateTime: 'foo',
  },
  {
    id: 5,
    version: 1,
    string: 'Hello World 5',
    integer: 0,
    decimal: 0,
    boolean: false,
  },
];

export type HasTestInfo = {
  lastSort: Sort | undefined;
  lastFilter: FilterUnion | undefined;
  callCount: number;
};

export const personService = (): CountService<Person> & CrudService<Person> & HasTestInfo => createService(personData);
export const personListService = (): ListService<Person> => createListService(personData);
export const companyService = (): CountService<Company> & CrudService<Company> & HasTestInfo =>
  createService(companyData);
export const columnRendererTestService = (): CountService<ColumnRendererTestValues> &
  CrudService<ColumnRendererTestValues> &
  HasTestInfo => createService(columnRendererTestData);

const noSort: Sort = { orders: [] };

export async function getItem<T extends HasIdVersion>(
  service: CrudService<T> & HasTestInfo,
  id: number,
): Promise<T | undefined> {
  return (await service.list({ pageNumber: 0, pageSize: 1000, sort: noSort }, undefined)).find((p) => p.id === id);
}
