import { _getPropertyModel, makeObjectEmptyValueCreator, ObjectModel, StringModel } from "@vaadin/hilla-lit-form";
import type File_1 from "./File.js";
class FileModel<T extends File_1 = File_1> extends ObjectModel<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator(FileModel);
    get name(): StringModel {
        return this[_getPropertyModel]("name", (parent, key) => new StringModel(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
}
export default FileModel;
