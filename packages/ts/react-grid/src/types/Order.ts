import type Direction_1 from './Direction.js';
import type NullHandling_1 from './NullHandling.js';
interface Order {
  direction: Direction_1;
  ignoreCase: boolean;
  nullHandling?: NullHandling_1;
  property: string;
}
export default Order;
