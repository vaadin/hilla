interface NullMarkedModel {
  required: string;
  nullable?: string;
  nullableGetter?: string;
  nullableGetterItems: Array<string | undefined>;
}

export default NullMarkedModel;
