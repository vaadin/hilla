import type BaseEvent_1 from "./BaseEvent.js";
interface DeleteEvent extends BaseEvent_1 {
    item?: string;
    force: boolean;
    "@type": "delete";
}
export default DeleteEvent;
