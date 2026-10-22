import m from "@vaadin/hilla-models";
import EmployeeModel from "./EmployeeModel.js";
import type Manager from "./Manager.js";
const ManagerModel = m
  .extend(EmployeeModel)
  .object<Manager>("Manager")
  .build();
type ManagerModel = typeof ManagerModel;
export default ManagerModel;
