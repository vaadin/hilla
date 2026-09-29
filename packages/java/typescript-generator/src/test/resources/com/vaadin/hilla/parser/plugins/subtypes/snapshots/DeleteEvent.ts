import type BaseEvent from "./BaseEvent.js";
interface DeleteEvent extends BaseEvent {
    item?: string;
    force: boolean;
    "@type": "delete";
}
export default DeleteEvent;
