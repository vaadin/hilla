import type EmailNotification_1 from "./EmailNotification.js";
interface HtmlEmailNotification extends Omit<EmailNotification_1, "kind"> {
    html?: string;
    kind: "html-email";
}
export default HtmlEmailNotification;
