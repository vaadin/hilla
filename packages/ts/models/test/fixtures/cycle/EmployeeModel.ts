import m, { StringModel } from '../../../src/index.js';
import type Employee from './Employee.js';
import PersonModel from './PersonModel.js';

// The base is read through a function: this module is evaluated before
// `PersonModel.ts` whenever that module is the entry, and the binding is not
// initialized yet at that point.
const EmployeeModel = m
  .extend(() => PersonModel)
  .object<Employee>('Employee')
  .property('title', StringModel)
  .build();
type EmployeeModel = typeof EmployeeModel;
export default EmployeeModel;
