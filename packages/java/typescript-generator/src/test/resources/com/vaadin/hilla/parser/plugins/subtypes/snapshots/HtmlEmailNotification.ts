import type EmailNotification from "./EmailNotification.js";
interface HtmlEmailNotification extends EmailNotification {
    html?: string;
    kind: "html-email";
}
export default HtmlEmailNotification;
