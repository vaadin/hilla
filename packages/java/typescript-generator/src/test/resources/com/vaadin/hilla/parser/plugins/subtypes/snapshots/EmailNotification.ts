import type Notification from "./Notification.js";
interface EmailNotification extends Notification {
    address?: string;
    kind: "email" | "html-email";
}
export default EmailNotification;
