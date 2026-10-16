import m, { StringModel } from "@vaadin/hilla-models";
import type File_1 from "./File.js";
const FileModel = m
  .object<File_1>("File")
  .property("name", m.meta(m.optional(StringModel), { jvmType: "java.lang.String" }))
  .build();
type FileModel = typeof FileModel;
export default FileModel;
