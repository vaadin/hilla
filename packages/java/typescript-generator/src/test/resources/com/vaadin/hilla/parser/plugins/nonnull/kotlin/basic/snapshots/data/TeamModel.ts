import m from "@vaadin/hilla-models";
import AbstractEntityModel from "./AbstractEntityModel.js";
import EmployeeModel from "./EmployeeModel.js";
import ManagerModel from "./ManagerModel.js";
import type Team from "./Team.js";
const TeamModel = m
  .extend(AbstractEntityModel)
  .object<Team>("Team")
  .property("employees", m.meta(m.optional(m.array(EmployeeModel)), { jvmType: "java.util.List" }))
  .property("manager", m.optional(ManagerModel))
  .build();
type TeamModel = typeof TeamModel;
export default TeamModel;
