import type AbstractEntity_1 from "./AbstractEntity.js";
import type Employee_1 from "./Employee.js";
import type Manager_1 from "./Manager.js";
interface Team<E = unknown, M = unknown> extends AbstractEntity_1 {
    employees?: Array<Employee_1>;
    manager?: Manager_1;
}
export default Team;
