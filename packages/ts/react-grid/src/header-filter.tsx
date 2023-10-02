import { Item } from '@hilla/react-components/Item.js';
import { ListBox } from '@hilla/react-components/ListBox.js';
import { NumberField } from '@hilla/react-components/NumberField.js';
import { Select, type SelectElement } from '@hilla/react-components/Select.js';
import { TextField, type TextFieldElement } from '@hilla/react-components/TextField.js';
import { type ReactElement, type RefObject, useContext, useEffect, useRef, useState } from 'react';
import { ColumnContext } from './autogrid-column-context.js';
import css from './header-filter.module.css';
import Matcher from './types/dev/hilla/crud/filter/PropertyStringFilter/Matcher';

function useFilterState(initialMatcher: Matcher) {
  const context = useContext(ColumnContext)!;
  const [matcher, setMatcher] = useState(initialMatcher);
  const [filterValue, setFilterValue] = useState('');

  function updateFilter(newMatcher: Matcher, newFilterValue: string) {
    setFilterValue(newFilterValue);
    setMatcher(newMatcher);

    const filter = {
      propertyId: context.propertyInfo.name,
      filterValue: newFilterValue,
      matcher: newMatcher,
    };
    // eslint-disable-next-line
    (filter as any).t = 'propertyString';
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

export function StringHeaderFilter(): ReactElement {
  const { updateFilter } = useFilterState(Matcher.CONTAINS);

  return (
    <TextField
      placeholder="Filter..."
      onInput={(e: any) => {
        const fieldValue = ((e as InputEvent).target as TextFieldElement).value;
        updateFilter(Matcher.CONTAINS, fieldValue);
      }}
    ></TextField>
  );
}

export function NumberHeaderFilter(): ReactElement {
  const { matcher, filterValue, updateFilter } = useFilterState(Matcher.GREATER_THAN);
  const select = useRef<SelectElement>(null);

  useSelectInitWorkaround(select);

  return (
    <>
      <Select
        ref={select}
        onValueChanged={(e) => {
          const newMatcher = e.detail.value as Matcher;
          updateFilter(newMatcher, filterValue);
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
        className={css.filterWithLessGreaterEquals}
        value={matcher}
      ></Select>
      <NumberField
        placeholder="Filter..."
        onInput={(e) => {
          const fieldValue = ((e as InputEvent).target as TextFieldElement).value;
          updateFilter(matcher, fieldValue);
        }}
      />
    </>
  );
}

export function BooleanHeaderFilter(): ReactElement {
  const { filterValue, updateFilter } = useFilterState(Matcher.EQUALS);
  const select = useRef<SelectElement>(null);

  useSelectInitWorkaround(select);

  return (
    <Select
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
  );
}
export function NoHeaderFilter(): ReactElement {
  return <></>;
}
