import type RecurringJob_1 from "./RecurringJob.js";
interface NightlyJob extends RecurringJob_1 {
    hour?: string;
    "@type": "recurring";
}
export default NightlyJob;
