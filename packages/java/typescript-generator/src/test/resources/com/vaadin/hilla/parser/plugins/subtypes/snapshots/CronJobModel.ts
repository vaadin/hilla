import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, StringModel as StringModel_1 } from "@vaadin/hilla-lit-form";
import type CronJob_1 from "./CronJob.js";
import JobModel_1 from "./JobModel.js";
class CronJobModel<T extends CronJob_1 = CronJob_1> extends JobModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(CronJobModel);
    get expression(): StringModel_1 {
        return this[_getPropertyModel_1]("expression", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
}
export default CronJobModel;
