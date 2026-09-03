import type Address_1 from "../data/Address.js";
import type Employee_1 from "../data/Employee.js";
interface NestedPerson {
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
export default NestedPerson;
