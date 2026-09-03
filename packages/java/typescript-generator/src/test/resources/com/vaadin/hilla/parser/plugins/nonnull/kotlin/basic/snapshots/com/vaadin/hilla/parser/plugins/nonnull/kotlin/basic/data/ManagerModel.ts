import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1 } from "@vaadin/hilla-lit-form";
import EmployeeModel_1 from "./EmployeeModel.js";
import type Manager_1 from "./Manager.js";
class ManagerModel<T extends Manager_1 = Manager_1> extends EmployeeModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(ManagerModel);
}
export default ManagerModel;
