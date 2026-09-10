import { _getPropertyModel, ArrayModel, makeObjectEmptyValueCreator } from "@vaadin/hilla-lit-form";
import AbstractEntityModel from "./AbstractEntityModel.js";
import EmployeeModel from "./EmployeeModel.js";
import ManagerModel from "./ManagerModel.js";
import type Team from "./Team.js";
class TeamModel<T extends Team = Team> extends AbstractEntityModel<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator(TeamModel);
    get employees(): ArrayModel<EmployeeModel> {
        return this[_getPropertyModel]("employees", (parent, key) => new ArrayModel(parent, key, true, (parent, key) => new EmployeeModel(parent, key, false), { meta: { javaType: "java.util.List" } }));
    }
    get manager(): ManagerModel {
        return this[_getPropertyModel]("manager", (parent, key) => new ManagerModel(parent, key, true));
    }
}
export default TeamModel;
