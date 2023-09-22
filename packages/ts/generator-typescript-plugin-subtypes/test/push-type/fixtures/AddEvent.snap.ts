import type BaseEvent_1 from "./BaseEvent.js";
interface AddEvent extends BaseEvent_1 {
    item?: string;
    "@type": "add";
}
export default AddEvent;
