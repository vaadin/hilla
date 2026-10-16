import type BaseEvent from "./BaseEvent.js";
interface AddEvent extends BaseEvent {
    item?: string;
    "@type": "add";
}
export default AddEvent;
