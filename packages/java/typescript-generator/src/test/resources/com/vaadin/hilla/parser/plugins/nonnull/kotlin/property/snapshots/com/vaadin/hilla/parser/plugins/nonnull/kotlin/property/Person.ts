import type AbstractEntity_1 from "./AbstractEntity.js";
import type Address_1 from "./Address.js";
interface Person extends AbstractEntity_1 {
    id?: number;
    firstName: string;
    lastName: string;
    email?: string;
    phone: string;
    important: boolean;
    luckyNumber: number;
    addresses: Record<string, Address_1>;
    profilePicture?: string;
    fullName: string;
    age?: number;
}
export default Person;
