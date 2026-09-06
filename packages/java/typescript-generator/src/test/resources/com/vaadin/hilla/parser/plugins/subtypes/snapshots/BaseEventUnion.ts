import type AddEvent_1 from "./AddEvent.js";
import type NestedEvent_1 from "./BaseEvent/NestedEvent.js";
import type DeleteEvent_1 from "./DeleteEvent.js";
import type MoveEvent_1 from "./MoveEvent.js";
import type UpdateEvent_1 from "./UpdateEvent.js";
type BaseEventUnion = AddEvent_1 | UpdateEvent_1 | DeleteEvent_1 | MoveEvent_1 | NestedEvent_1;
export default BaseEventUnion;
