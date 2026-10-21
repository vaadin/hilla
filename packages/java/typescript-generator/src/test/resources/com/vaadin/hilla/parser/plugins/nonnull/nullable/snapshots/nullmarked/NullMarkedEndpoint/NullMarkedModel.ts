interface NullMarkedModel {
  required: string;
  nullable?: string;
  nullableGetter?: string;
  nullableGetterItems: Array<string | undefined>;
  nullableSetter?: string;
  nullableSetterItems: Array<string | undefined>;
}

export default NullMarkedModel;
