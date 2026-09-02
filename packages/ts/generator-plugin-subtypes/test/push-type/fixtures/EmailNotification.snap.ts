import type Notification_1 from "./Notification.js";
interface EmailNotification extends Omit<Notification_1, "kind"> {
    address?: string;
    kind: "email";
}
export default EmailNotification;
