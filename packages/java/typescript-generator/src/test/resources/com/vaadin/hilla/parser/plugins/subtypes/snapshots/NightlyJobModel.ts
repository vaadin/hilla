import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, StringModel as StringModel_1 } from "@vaadin/hilla-lit-form";
import type NightlyJob_1 from "./NightlyJob.js";
import RecurringJobModel_1 from "./RecurringJobModel.js";
class NightlyJobModel<T extends NightlyJob_1 = NightlyJob_1> extends RecurringJobModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(NightlyJobModel);
    get hour(): StringModel_1 {
        return this[_getPropertyModel_1]("hour", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
}
export default NightlyJobModel;
