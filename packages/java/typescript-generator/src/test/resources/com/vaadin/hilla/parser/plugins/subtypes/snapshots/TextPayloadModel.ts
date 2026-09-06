import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, StringModel as StringModel_1 } from "@vaadin/hilla-lit-form";
import PayloadModel_1 from "./PayloadModel.js";
import type TextPayload_1 from "./TextPayload.js";
class TextPayloadModel<T extends TextPayload_1 = TextPayload_1> extends PayloadModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(TextPayloadModel);
    get text(): StringModel_1 {
        return this[_getPropertyModel_1]("text", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
}
export default TextPayloadModel;
