import type Notification from "./Notification.js";
interface SmsNotification extends Notification {
    number?: string;
}
export default SmsNotification;
