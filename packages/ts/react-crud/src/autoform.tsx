import { type AbstractModel, type DetachedModelConstructor, ValidationError, type Value } from '@hilla/form';
import { Button } from '@hilla/react-components/Button.js';
import { ConfirmDialog } from '@hilla/react-components/ConfirmDialog';
import { FormLayout } from '@hilla/react-components/FormLayout';
import { VerticalLayout } from '@hilla/react-components/VerticalLayout.js';
import { useForm, type UseFormResult } from '@hilla/react-form';
import React, { type ComponentType, type JSX, type ReactElement, useEffect, useState } from 'react';
import { AutoFormField, type AutoFormFieldProps, type FieldOptions } from './autoform-field.js';
import css from './autoform.obj.css';
import type { CrudService } from './crud.js';
import { getIdProperty, getProperties, includeProperty, type PropertyInfo } from './property-info.js';
import type { ComponentStyleProps } from './util';

document.adoptedStyleSheets.unshift(css);

export const emptyItem = Symbol();

type SubmitErrorEvent = {
  error: unknown;
};
type SubmitEvent<TItem> = {
  item: TItem;
};
type DeleteErrorEvent = {
  error: unknown;
};
type DeleteEvent<TItem> = {
  item: TItem;
};

export type AutoFormLayoutRendererProps<M extends AbstractModel> = Readonly<{
  form: UseFormResult<M>;
  children: ReadonlyArray<ReactElement<AutoFormFieldProps>>;
}>;

type FieldColSpan = Readonly<{
  property: string;
  colSpan: number;
}>;

export type AutoFormLayoutProps = Readonly<{
  template: FieldColSpan[][] | string[][];
  responsiveSteps?: Array<{ minWidth: string; columns: number }>;
}>;

export type AutoFormProps<M extends AbstractModel = AbstractModel> = ComponentStyleProps &
  Readonly<{
    service: CrudService<Value<M>>;
    model: DetachedModelConstructor<M>;
    item?: Value<M> | typeof emptyItem | null;
    disabled?: boolean;
    customLayoutRenderer?: AutoFormLayoutProps | ComponentType<AutoFormLayoutRendererProps<M>>;
    fieldOptions?: Record<string, FieldOptions>;
    deleteButtonVisible?: boolean;
    onSubmitError?({ error }: SubmitErrorEvent): void;
    afterSubmit?({ item }: SubmitEvent<Value<M>>): void;
    onDeleteError?({ error }: DeleteErrorEvent): void;
    afterDelete?({ item }: DeleteEvent<Value<M>>): void;
  }>;

type CustomFormLayoutProps = Readonly<{
  customFormLayout: AutoFormLayoutProps;
  children: ReadonlyArray<ReactElement<AutoFormFieldProps>>;
}>;

function findColumnCount(responsiveSteps: Array<{ minWidth: string; columns: number }>): number {
  return responsiveSteps
    .map((step: { minWidth: string; columns: number }) => ({
      minWidth: parseInt(step.minWidth, 10),
      columns: step.columns,
    }))
    .filter(({ minWidth }) => minWidth <= window.innerWidth)
    .reduce(
      (maxStep, step) => {
        if (step.minWidth > maxStep.minWidth) {
          return step;
        }
        return maxStep;
      },
      { minWidth: 0, columns: 1 },
    ).columns;
}

function createGenericResponsiveSteps(
  customFormLayout: AutoFormLayoutProps,
): Array<{ minWidth: string; columns: number }> {
  function gcd(a: number, b: number): number {
    return a > 0 ? gcd(b % a, a) : b;
  }

  const lcm = (a: number, b: number) => (a * b) / gcd(a, b);

  const minNeededColumns = customFormLayout.template.map((row) => row.length).reduce(lcm);

  return [
    { minWidth: '0', columns: 1 },
    { minWidth: '800px', columns: minNeededColumns },
  ];
}

function CustomFormLayout(customFormLayoutProps: CustomFormLayoutProps): JSX.Element {
  const { customFormLayout, children } = customFormLayoutProps;
  const fieldsByPropertyName = new Map<string, AutoFormFieldProps>();
  children.forEach((field) => fieldsByPropertyName.set(field.props.propertyInfo.name, field.props));

  let responsiveSteps: Array<{ minWidth: string; columns: number }>;
  if (customFormLayout.responsiveSteps == null) {
    responsiveSteps = createGenericResponsiveSteps(customFormLayout);
  } else {
    ({ responsiveSteps } = customFormLayout);
  }

  let weightedTemplate: FieldColSpan[][];
  if (typeof customFormLayout.template[0][0] === 'string') {
    const columnCount = findColumnCount(responsiveSteps);
    weightedTemplate = (customFormLayout.template as string[][]).map((row) => {
      const rowSize = row.length;
      const colSpan = Math.ceil(columnCount / rowSize);
      return row.map((property) => ({ property, colSpan }));
    });
  } else {
    weightedTemplate = customFormLayout.template as FieldColSpan[][];
  }

  const spannedFields: JSX.Element[] = [];
  weightedTemplate.forEach((row: FieldColSpan[]) => {
    row.forEach((fieldColSpan: FieldColSpan) => {
      const fieldProps = fieldsByPropertyName.get(fieldColSpan.property)!;
      const spannedField = (
        <AutoFormField
          key={fieldProps.propertyInfo.name}
          propertyInfo={fieldProps.propertyInfo}
          form={fieldProps.form}
          disabled={fieldProps.disabled}
          colSpan={fieldColSpan.colSpan}
        />
      );
      spannedFields.push(spannedField);
    });
  });

  return <FormLayout responsiveSteps={responsiveSteps}>{spannedFields}</FormLayout>;
}

export function ExperimentalAutoForm<M extends AbstractModel>({
  service,
  model,
  item = emptyItem,
  onSubmitError,
  afterSubmit,
  disabled,
  customLayoutRenderer: CustomLayoutRenderer,
  fieldOptions,
  style,
  id,
  className,
  deleteButtonVisible,
  afterDelete,
  onDeleteError,
}: AutoFormProps<M>): JSX.Element {
  const form = useForm(model, {
    onSubmit: async (formItem) => service.save(formItem),
  });
  const [formError, setFormError] = useState('');
  const [showDeleteDialog, setShowDeleteDialog] = useState(false);

  const isEditMode = item !== undefined && item !== null && item !== emptyItem;

  useEffect(() => {
    if (item !== emptyItem) {
      form.read(item);
    } else {
      form.clear();
    }
  }, [item]);

  async function handleSubmit(): Promise<void> {
    try {
      setFormError('');
      const newItem = await form.submit();
      if (newItem === undefined) {
        // If update returns an empty object, then no update was performed
        throw new Error('generic error');
      } else if (afterSubmit) {
        afterSubmit({ item: newItem });
      }
    } catch (error) {
      if (error instanceof ValidationError) {
        // Handled automatically
        return;
      }
      const genericError = 'Something went wrong, please check all your values';
      if (onSubmitError) {
        onSubmitError({ error });
      } else {
        setFormError(genericError);
      }
    }
  }

  function deleteItem() {
    setShowDeleteDialog(true);
  }

  async function confirmDelete() {
    // At this point, item can not be null or emptyItem
    const deletedItem = item as Value<M>;
    try {
      const properties = getProperties(model);
      const idProperty = getIdProperty(properties)!;
      // eslint-disable-next-line
      const id = (item as any)[idProperty.name];
      await service.delete(id);
      if (afterDelete) {
        afterDelete({ item: deletedItem });
      }
    } catch (error) {
      if (onDeleteError) {
        onDeleteError({ error });
      }
    } finally {
      setShowDeleteDialog(false);
    }
  }

  function cancelDelete() {
    setShowDeleteDialog(false);
  }

  function createAutoFormField(propertyInfo: PropertyInfo): JSX.Element {
    const fieldOptionsForProperty = fieldOptions?.[propertyInfo.name];
    return (
      <AutoFormField
        key={propertyInfo.name}
        propertyInfo={propertyInfo}
        form={form}
        disabled={disabled}
        options={fieldOptionsForProperty}
      />
    );
  }

  let layout: JSX.Element;
  if (CustomLayoutRenderer === undefined) {
    const fields = getProperties(model).filter(includeProperty).map(createAutoFormField);
    layout = <FormLayout>{fields}</FormLayout>;
  } else {
    const fields = getProperties(model).map(createAutoFormField);
    if (typeof CustomLayoutRenderer === 'function') {
      layout = <CustomLayoutRenderer form={form}>{fields}</CustomLayoutRenderer>;
    } else {
      layout = <CustomFormLayout customFormLayout={CustomLayoutRenderer}>{fields}</CustomFormLayout>;
    }
  }

  return (
    <div className={`auto-form ${className}`} id={id} style={style} data-testid="auto-form">
      <VerticalLayout className="auto-form-fields">
        {layout}
        {formError ? <div style={{ color: 'var(--lumo-error-color)' }}>{formError}</div> : <></>}
      </VerticalLayout>
      <div className="auto-form-toolbar">
        <Button
          theme="primary"
          disabled={!!disabled || (isEditMode && !form.dirty)}
          // eslint-disable-next-line @typescript-eslint/no-misused-promises
          onClick={handleSubmit}
        >
          Submit
        </Button>
        {form.dirty ? (
          <Button theme="tertiary" onClick={() => form.reset()}>
            Discard
          </Button>
        ) : null}
        {deleteButtonVisible && isEditMode && (
          <Button className="auto-form-delete-button" theme="tertiary error" onClick={deleteItem}>
            Delete...
          </Button>
        )}
      </div>
      {showDeleteDialog && (
        <ConfirmDialog
          opened
          header="Delete item"
          confirmTheme="error"
          cancelButtonVisible
          // eslint-disable-next-line
          onConfirm={confirmDelete}
          onCancel={cancelDelete}
        >
          Are you sure you want to delete the selected item?
        </ConfirmDialog>
      )}
    </div>
  );
}
