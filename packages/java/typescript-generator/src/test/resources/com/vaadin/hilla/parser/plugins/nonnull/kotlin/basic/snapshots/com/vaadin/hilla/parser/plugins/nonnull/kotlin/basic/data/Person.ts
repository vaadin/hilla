import type AbstractEntity_1 from "./AbstractEntity.js";
import type Address_1 from "./Address.js";
import type Employee_1 from "./Employee.js";
interface Person extends AbstractEntity_1 {
    firstName: string;
    lastName: string;
    email?: string;
    phone: string;
    address: Array<Address_1>;
    team: Record<string, Array<Employee_1 | undefined>>;
    dateOfBirth?: string;
    occupation?: string;
    role?: string;
    important: boolean;
}
export default Person;
