import type BatchJob_1 from "./BatchJob.js";
import type CronJob_1 from "./CronJob.js";
import type InlineJob_1 from "./Job/InlineJob.js";
import type NightlyJob_1 from "./NightlyJob.js";
type JobUnion = BatchJob_1 | CronJob_1 | NightlyJob_1 | InlineJob_1;
export default JobUnion;
