import type Address from "../data/Address.js";
import type Employee from "../data/Employee.js";
interface NestedPerson {
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
export default NestedPerson;
