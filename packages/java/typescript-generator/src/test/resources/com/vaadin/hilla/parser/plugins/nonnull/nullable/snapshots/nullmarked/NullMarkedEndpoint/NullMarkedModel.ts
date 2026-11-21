interface NullMarkedModel {
  required: string;
  nullable?: string;
  nullableGetter?: string;
  nullableGetterItems: Array<string | undefined>;
  nullableSetter?: string;
  nullableSetterItems: Array<string | undefined>;
  ignoredOnSetterIncludedOnGetter: string;
  ignoredOnGetterIncludedOnField: string;
  ignoredOnGetterIncludedOnSetter: string;
  notIgnoredOnGetter: string;
}

export default NullMarkedModel;
