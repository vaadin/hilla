import type AbstractEntity from "./AbstractEntity.js";
import type Address from "./Address.js";
import type Employee from "./Employee.js";
interface Person extends AbstractEntity {
    firstName: string;
    lastName: string;
    email?: string;
    phone: string;
    address: Array<Address>;
    team: Record<string, Array<Employee | undefined>>;
    dateOfBirth?: string;
    occupation?: string;
    role?: string;
    important: boolean;
}
export default Person;
