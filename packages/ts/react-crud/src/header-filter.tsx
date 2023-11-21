import { _enum, type EnumModel } from '@hilla/form';
import { DatePicker } from '@hilla/react-components/DatePicker.js';
import { Item } from '@hilla/react-components/Item.js';
import { ListBox } from '@hilla/react-components/ListBox.js';
import { NumberField } from '@hilla/react-components/NumberField.js';
import { Select, type SelectElement } from '@hilla/react-components/Select.js';
import { TextField, type TextFieldElement } from '@hilla/react-components/TextField.js';
import { TimePicker } from '@hilla/react-components/TimePicker.js';
import { type ReactElement, type RefObject, useContext, useEffect, useRef, useState } from 'react';
import { ColumnContext } from './autogrid-column-context.js';
import { useDatePickerI18n } from './locale.js';
import type FilterUnion from './types/dev/hilla/crud/filter/FilterUnion.js';
import Matcher from './types/dev/hilla/crud/filter/PropertyStringFilter/Matcher.js';
import { convertToTitleCase } from './util';

function useFilterState(initialMatcher: Matcher) {
  const context = useContext(ColumnContext)!;
  const [matcher, setMatcher] = useState(initialMatcher);
  const [filterValue, setFilterValue] = useState('');

  function updateFilter(newMatcher: Matcher, newFilterValue: string) {
    setFilterValue(newFilterValue);
    setMatcher(newMatcher);

    const filter: FilterUnion = {
      '@type': 'propertyString',
      propertyId: context.propertyInfo.name,
      filterValue: newFilterValue,
      matcher: newMatcher,
    };
    context.setPropertyFilter(filter);
  }

  return { matcher, filterValue, updateFilter };
}

// Workaround for https://github.com/vaadin/react-components/issues/148
function useSelectInitWorkaround(selectRef: RefObject<SelectElement>) {
  useEffect(() => {
    setTimeout(() => {
      if (selectRef.current) {
        selectRef.current.requestContentUpdate();
      }
    }, 1);
  }, []);
}

// extracted component (and type) to avoid code duplication
type ComparationSelectionProps = {
  value: Matcher;
  onMatcherChanged(matcher: Matcher): void;
};

function ComparationSelection({ onMatcherChanged, value }: ComparationSelectionProps): ReactElement {
  const select = useRef<SelectElement>(null);

  useSelectInitWorkaround(select);

  return (
    <Select
      theme="small"
      className="auto-grid-comparation-selection"
      ref={select}
      value={value}
      onValueChanged={({ detail }) => {
        onMatcherChanged(detail.value as Matcher);
      }}
      renderer={() => (
        <ListBox>
          <Item value={Matcher.GREATER_THAN} {...{ label: '>' }}>
            &gt; Greater than
          </Item>
          <Item value={Matcher.LESS_THAN} {...{ label: '<' }}>
            &lt; Less than
          </Item>
          <Item value={Matcher.EQUALS} {...{ label: '=' }}>
            = Equals
          </Item>
        </ListBox>
      )}
    ></Select>
  );
}

export function StringHeaderFilter(): ReactElement {
  const { updateFilter } = useFilterState(Matcher.CONTAINS);

  return (
    <div className="auto-grid-string-filter">
      <TextField
        theme="small"
        placeholder="Filter..."
        onInput={(e: any) => {
          const fieldValue = ((e as InputEvent).target as TextFieldElement).value;
          updateFilter(Matcher.CONTAINS, fieldValue);
        }}
      ></TextField>
    </div>
  );
}

export function NumberHeaderFilter(): ReactElement {
  const { matcher, filterValue, updateFilter } = useFilterState(Matcher.GREATER_THAN);
  const select = useRef<SelectElement>(null);

  useSelectInitWorkaround(select);

  return (
    <div className="auto-grid-number-filter">
      <ComparationSelection value={matcher} onMatcherChanged={(m) => updateFilter(m, filterValue)} />
      <NumberField
        theme="small"
        placeholder="Filter..."
        onInput={(e) => {
          const fieldValue = ((e as InputEvent).target as TextFieldElement).value;
          updateFilter(matcher, fieldValue);
        }}
      />
    </div>
  );
}

export function EnumHeaderFilter(): ReactElement {
  const { filterValue, updateFilter } = useFilterState(Matcher.EQUALS);
  const context = useContext(ColumnContext)!;
  const model = context.propertyInfo.model as EnumModel;
  const options = [
    {
      value: '',
      label: '',
    },
    ...Object.keys(model[_enum]).map((value) => ({
      label: convertToTitleCase(value),
      value,
    })),
  ];
  return (
    <div className="auto-grid-enum-filter">
      <Select
        theme="small"
        items={options}
        value={filterValue}
        onValueChanged={(e) => {
          const newFilterValue = e.detail.value;
          updateFilter(Matcher.EQUALS, newFilterValue);
        }}
      />
    </div>
  );
}

export function BooleanHeaderFilter(): ReactElement {
  const { filterValue, updateFilter } = useFilterState(Matcher.EQUALS);
  const select = useRef<SelectElement>(null);

  useSelectInitWorkaround(select);

  return (
    <div className="auto-grid-boolean-filter">
      <Select
        theme="small"
        ref={select}
        onValueChanged={(e) => {
          const newFilterValue = e.detail.value;
          updateFilter(Matcher.EQUALS, newFilterValue);
        }}
        renderer={() => (
          <ListBox>
            <Item value={''} {...{ label: '' }}></Item>
            <Item value={'True'} {...{ label: 'Yes' }}>
              Yes
            </Item>
            <Item value={'False'} {...{ label: 'No' }}>
              No
            </Item>
          </ListBox>
        )}
        value={filterValue}
      ></Select>
    </div>
  );
}

export function DateHeaderFilter(): ReactElement {
  const i18n = useDatePickerI18n();
  const { matcher, filterValue, updateFilter } = useFilterState(Matcher.GREATER_THAN);
  const [invalid, setInvalid] = useState(false);

  return (
    <div className="auto-grid-date-filter">
      <ComparationSelection value={matcher} onMatcherChanged={(m) => updateFilter(m, filterValue)} />
      <DatePicker
        theme="small"
        value={filterValue}
        placeholder="Filter..."
        i18n={i18n}
        onInvalidChanged={({ detail: { value } }) => {
          setInvalid(value);
        }}
        onValueChanged={({ detail: { value } }) => {
          if (!(invalid || value === filterValue)) {
            updateFilter(matcher, value);
          }
        }}
      />
    </div>
  );
}

export function TimeHeaderFilter(): ReactElement {
  const { matcher, filterValue, updateFilter } = useFilterState(Matcher.GREATER_THAN);
  const [invalid, setInvalid] = useState(false);

  return (
    <div className="auto-grid-time-filter">
      <ComparationSelection value={matcher} onMatcherChanged={(m) => updateFilter(m, filterValue)} />
      <TimePicker
        theme="small"
        value={filterValue}
        placeholder="Filter..."
        onInvalidChanged={({ detail: { value } }) => {
          setInvalid(value);
        }}
        onValueChanged={({ detail: { value } }) => {
          if (!(invalid || value === filterValue)) {
            updateFilter(matcher, value);
          }
        }}
      />
    </div>
  );
}

export function NoHeaderFilter(): ReactElement {
  return <></>;
}
