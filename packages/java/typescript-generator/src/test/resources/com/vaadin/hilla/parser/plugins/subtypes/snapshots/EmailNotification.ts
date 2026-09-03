import type Notification_1 from "./Notification.js";
interface EmailNotification extends Notification_1 {
    address?: string;
    kind: "email";
}
export default EmailNotification;
