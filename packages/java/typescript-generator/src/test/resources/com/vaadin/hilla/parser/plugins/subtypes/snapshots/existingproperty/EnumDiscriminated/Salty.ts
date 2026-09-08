import type EnumDiscriminated_1 from "../EnumDiscriminated.js";
import type Taste_1 from "./Taste.js";
interface Salty extends EnumDiscriminated_1 {
    taste?: Taste_1;
    level: number;
}
export default Salty;
