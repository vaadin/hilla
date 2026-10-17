import type AbstractEntity from "./AbstractEntity.js";
import type Employee from "./Employee.js";
import type Manager from "./Manager.js";
interface Team<E = unknown, M = unknown> extends AbstractEntity {
    employees?: Array<Employee>;
    manager?: Manager;
}
export default Team;
