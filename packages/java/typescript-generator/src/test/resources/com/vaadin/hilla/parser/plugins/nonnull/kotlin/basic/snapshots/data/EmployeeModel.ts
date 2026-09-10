import m, { ObjectModel } from "@vaadin/hilla-models";
import type Employee from "./Employee.js";
import PersonModel from "./PersonModel.js";
const EmployeeModel: ObjectModel<Employee> = m
  .extend(PersonModel)
  .object<Employee>("Employee")
  .build();
type EmployeeModel = typeof EmployeeModel;
export default EmployeeModel;
