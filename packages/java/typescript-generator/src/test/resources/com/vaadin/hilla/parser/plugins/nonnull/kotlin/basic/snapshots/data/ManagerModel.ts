import { _getPropertyModel, makeObjectEmptyValueCreator } from "@vaadin/hilla-lit-form";
import EmployeeModel from "./EmployeeModel.js";
import type Manager from "./Manager.js";
class ManagerModel<T extends Manager = Manager> extends EmployeeModel<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator(ManagerModel);
}
export default ManagerModel;
