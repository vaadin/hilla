import type BaseEvent from "./BaseEvent.js";
interface UpdateEvent extends BaseEvent {
    oldItem?: string;
    newItem?: string;
    "@type": "update";
}
export default UpdateEvent;
