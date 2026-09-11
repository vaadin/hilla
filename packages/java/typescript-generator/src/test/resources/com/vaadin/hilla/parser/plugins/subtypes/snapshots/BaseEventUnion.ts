import type AddEvent from "./AddEvent.js";
import type DeleteEvent from "./DeleteEvent.js";
import type UpdateEvent from "./UpdateEvent.js";
type BaseEventUnion = AddEvent | UpdateEvent | DeleteEvent;
export default BaseEventUnion;
