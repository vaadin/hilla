import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, NumberModel as NumberModel_1 } from "@vaadin/hilla-lit-form";
import JobModel_1 from "./JobModel.js";
import type RecurringJob_1 from "./RecurringJob.js";
class RecurringJobModel<T extends RecurringJob_1 = RecurringJob_1> extends JobModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(RecurringJobModel);
    get intervalMinutes(): NumberModel_1 {
        return this[_getPropertyModel_1]("intervalMinutes", (parent, key) => new NumberModel_1(parent, key, false, { meta: { javaType: "int" } }));
    }
}
export default RecurringJobModel;
