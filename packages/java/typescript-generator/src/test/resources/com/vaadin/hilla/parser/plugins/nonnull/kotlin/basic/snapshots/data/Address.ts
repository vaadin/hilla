import type AbstractEntity from "./AbstractEntity.js";
interface Address extends AbstractEntity {
    street: string;
    zipCode: string;
    city?: string;
}
export default Address;
