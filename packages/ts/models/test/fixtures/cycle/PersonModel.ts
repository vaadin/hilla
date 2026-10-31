import m, { type ObjectModel, StringModel } from '../../../src/index.js';
import type Employee from './Employee.js';
import EmployeeModel from './EmployeeModel.js';
import type Person from './Person.js';
import ZipCodeModel from './ZipCodeModel.js';

// Imports are in plain sorted order, so `EmployeeModel` evaluates before
// `ZipCodeModel`. Nothing here depends on that order.
const PersonModel = m
  .object<Person>('Person')
  .property('name', StringModel)
  .property('zip', ZipCodeModel)
  // The deferred reference is typed at the thunk, which keeps this model's own
  // property map inferable rather than erasing it to `ObjectModel<Person>`. The
  // array is also what breaks the default value cycle: it defaults to `[]`
  // without reading the model it holds.
  .property('team', m.array(m.optional(m.lazy((): ObjectModel<Employee> => EmployeeModel))))
  .build();
type PersonModel = typeof PersonModel;
export default PersonModel;
