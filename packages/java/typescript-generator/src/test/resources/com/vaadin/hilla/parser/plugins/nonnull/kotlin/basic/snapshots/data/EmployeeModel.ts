import { _getPropertyModel, makeObjectEmptyValueCreator } from "@vaadin/hilla-lit-form";
import type Employee from "./Employee.js";
import PersonModel from "./PersonModel.js";
class EmployeeModel<T extends Employee = Employee> extends PersonModel<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator(EmployeeModel);
}
export default EmployeeModel;
