import type EmailNotification_1 from "./EmailNotification.js";
interface HtmlEmailNotification extends EmailNotification_1 {
    html?: string;
    kind: "html-email";
}
export default HtmlEmailNotification;
