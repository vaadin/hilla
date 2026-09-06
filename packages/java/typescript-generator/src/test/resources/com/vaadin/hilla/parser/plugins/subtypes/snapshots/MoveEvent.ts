import type BaseEvent_1 from "./BaseEvent.js";
interface MoveEvent extends BaseEvent_1 {
    item?: string;
    position: number;
    "@type": "move";
}
export default MoveEvent;
