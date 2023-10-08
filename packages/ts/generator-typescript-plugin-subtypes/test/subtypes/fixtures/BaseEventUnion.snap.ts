import type AddEvent_1 from "./AddEvent.js";
import type DeleteEvent_1 from "./DeleteEvent.js";
import type UpdateEvent_1 from "./UpdateEvent.js";
type BaseEventUnion = AddEvent_1 | UpdateEvent_1 | DeleteEvent_1;
export default BaseEventUnion;
