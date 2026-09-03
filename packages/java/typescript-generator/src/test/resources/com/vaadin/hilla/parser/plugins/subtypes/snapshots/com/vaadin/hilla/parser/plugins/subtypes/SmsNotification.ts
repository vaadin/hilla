import type Notification_1 from "./Notification.js";
interface SmsNotification extends Notification_1 {
    number?: string;
    kind: "sms";
}
export default SmsNotification;
