import EmployeeModel from './EmployeeModel.js';
import PersonModel from './PersonModel.js';

// Compiles only while neither model carries an `ObjectModel<Entity>` annotation,
// which would erase the property map on both sides of the cycle.
export const personName = PersonModel.name;
export const zipCode = PersonModel.zip.code;
export const employeeTitle = EmployeeModel.title;
export const inheritedName = EmployeeModel.name;
