import type Direction_1 from "../../../../org/springframework/data/domain/Sort/Direction.js";
import type NullHandling_1 from "../../../../org/springframework/data/domain/Sort/NullHandling.js";
interface Order {
    direction: Direction_1;
    property: string;
    ignoreCase: boolean;
    nullHandling?: NullHandling_1;
}
export default Order;
