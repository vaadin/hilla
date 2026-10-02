import type Direction from "../../../../org/springframework/data/domain/Sort/Direction.js";
import type NullHandling from "../../../../org/springframework/data/domain/Sort/NullHandling.js";
interface Order {
    direction: Direction;
    property: string;
    ignoreCase: boolean;
    nullHandling?: NullHandling;
}
export default Order;
