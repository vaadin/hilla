import type BaseEvent_1 from "./BaseEvent.js";
interface UpdateEvent extends BaseEvent_1 {
    oldItem?: string;
    newItem?: string;
    "@type": "update";
}
export default UpdateEvent;
