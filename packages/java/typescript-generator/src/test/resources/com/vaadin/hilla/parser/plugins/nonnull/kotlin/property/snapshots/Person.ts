import type AbstractEntity from "./AbstractEntity.js";
import type Address from "./Address.js";
interface Person extends AbstractEntity {
    id?: number;
    firstName: string;
    lastName: string;
    email?: string;
    phone: string;
    important: boolean;
    luckyNumber: number;
    addresses: Record<string, Address>;
    profilePicture?: string;
    age?: number;
    fullName: string;
}
export default Person;
