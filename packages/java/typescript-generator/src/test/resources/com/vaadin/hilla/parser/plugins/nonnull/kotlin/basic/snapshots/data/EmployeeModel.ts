import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1 } from "@vaadin/hilla-lit-form";
import type Employee_1 from "./Employee.js";
import PersonModel_1 from "./PersonModel.js";
class EmployeeModel<T extends Employee_1 = Employee_1> extends PersonModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(EmployeeModel);
}
export default EmployeeModel;
