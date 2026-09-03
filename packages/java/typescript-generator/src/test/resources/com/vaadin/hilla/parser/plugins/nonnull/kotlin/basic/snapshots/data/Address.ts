import type AbstractEntity_1 from "./AbstractEntity.js";
interface Address extends AbstractEntity_1 {
    street: string;
    zipCode: string;
    city?: string;
}
export default Address;
