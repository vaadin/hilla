import { _getPropertyModel as _getPropertyModel_1, ArrayModel as ArrayModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1 } from "@vaadin/hilla-lit-form";
import AbstractEntityModel_1 from "./AbstractEntityModel.js";
import EmployeeModel_1 from "./EmployeeModel.js";
import ManagerModel_1 from "./ManagerModel.js";
import type Team_1 from "./Team.js";
class TeamModel<T extends Team_1 = Team_1> extends AbstractEntityModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(TeamModel);
    get employees(): ArrayModel_1<EmployeeModel_1> {
        return this[_getPropertyModel_1]("employees", (parent, key) => new ArrayModel_1(parent, key, true, (parent, key) => new EmployeeModel_1(parent, key, false), { meta: { javaType: "java.util.List" } }));
    }
    get manager(): ManagerModel_1 {
        return this[_getPropertyModel_1]("manager", (parent, key) => new ManagerModel_1(parent, key, true));
    }
}
export default TeamModel;
