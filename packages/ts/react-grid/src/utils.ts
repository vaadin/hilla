import {
  createDetachedModel,
  StringModel,
  type AbstractModel,
  type DetachedModelConstructor,
  NumberModel,
} from '@hilla/form';
import type { GridItemModel } from '@hilla/react-components/Grid.js';
import type { GridColumnElement, GridColumnProps } from '@hilla/react-components/GridColumn.js';
import type { ComponentType } from 'react';
import { AutoGridNumberRenderer } from './autogrid-number-renderer';

type ColumnOptions = Omit<GridColumnProps<any>, 'dangerouslySetInnerHTML'>;
export interface PropertyInfo {
  name: string;
  humanReadableName: string;
  modelType: 'number' | 'string' | undefined;
  gridRenderer:
    | ComponentType<Readonly<{ item: any; model: GridItemModel<any>; original: GridColumnElement }>>
    | null
    | undefined;
  gridColumnOptions: ColumnOptions;
}

// This is from vaadin-grid-column.js, should be used from there maybe. At least we must be 100% sure to match grid and fields
export function _generateHeader(path: string): string {
  return path
    .substring(path.lastIndexOf('.') + 1)
    .replace(/([A-Z])/gu, '-$1')
    .toLowerCase()
    .replace(/-/gu, ' ')
    .replace(/^./u, (match) => match.toUpperCase());
}

export const getProperties = (model: DetachedModelConstructor<AbstractModel>): PropertyInfo[] => {
  const properties = Object.keys(Object.getOwnPropertyDescriptors(model.prototype)).filter((p) => p !== 'constructor');
  // eslint-disable-next-line @typescript-eslint/no-unsafe-call
  const modelInstance: any = createDetachedModel(model);
  return properties.map((name) => {
    // eslint-disable-next-line
    const propertyModel = modelInstance[name];
    const humanReadableName = _generateHeader(name);
    const { constructor } = propertyModel;
    const modelType = constructor === StringModel ? 'string' : constructor === NumberModel ? 'number' : undefined;
    let gridRenderer;

    const gridColumnOptions: ColumnOptions = { autoWidth: true };
    if (modelType === 'number') {
      gridRenderer = AutoGridNumberRenderer;
      gridColumnOptions.textAlign = 'end';
      gridColumnOptions.flexGrow = 0;
    }
    return {
      name,
      humanReadableName,
      modelType,
      gridRenderer,
      gridColumnOptions,
    };
  });
};
