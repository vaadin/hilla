import type AbstractEntity_1 from "./AbstractEntity.js";
interface Address extends AbstractEntity_1 {
    id?: number;
    street: string;
    zipCode: string;
    city?: string;
}
export default Address;
