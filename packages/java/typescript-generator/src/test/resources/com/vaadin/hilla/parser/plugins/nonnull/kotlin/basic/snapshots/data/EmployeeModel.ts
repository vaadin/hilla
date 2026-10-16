import m from '@vaadin/hilla-models';
import type Employee from './Employee.js';
import PersonModel from './PersonModel.js';

const EmployeeModel = m
  .extend(PersonModel)
  .object<Employee>('Employee')
  .build();
type EmployeeModel = typeof EmployeeModel;

export default EmployeeModel;
