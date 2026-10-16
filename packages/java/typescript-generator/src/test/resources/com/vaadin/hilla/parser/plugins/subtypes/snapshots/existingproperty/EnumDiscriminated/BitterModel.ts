import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, StringModel as StringModel_1 } from "@vaadin/hilla-lit-form";
import EnumDiscriminatedModel_1 from "../EnumDiscriminatedModel.js";
import type Bitter_1 from "./Bitter.js";
import TasteModel_1 from "./TasteModel.js";
class BitterModel<T extends Bitter_1 = Bitter_1> extends EnumDiscriminatedModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(BitterModel);
    get note(): StringModel_1 {
        return this[_getPropertyModel_1]("note", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
    get taste(): TasteModel_1 {
        return this[_getPropertyModel_1]("taste", (parent, key) => new TasteModel_1(parent, key, true));
    }
}
export default BitterModel;
