import { AddEvent as AddEvent_1 } from "./AddEvent.js";
import { DeleteEvent as DeleteEvent_1 } from "./DeleteEvent.js";
import { UpdateEvent as UpdateEvent_1 } from "./UpdateEvent.js";
type BaseEvent = AddEvent_1 | UpdateEvent_1 | DeleteEvent_1;
export default BaseEvent;
