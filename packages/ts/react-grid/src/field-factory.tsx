import { Item } from '@hilla/react-components/Item.js';
import { ListBox } from '@hilla/react-components/ListBox.js';
import { Select, type SelectElement } from '@hilla/react-components/Select.js';
import { TextField, type TextFieldElement } from '@hilla/react-components/TextField.js';
import { useEffect, useRef, useState } from 'react';
import css from './field-factory.module.css';
import type PropertyStringFilter from './types/dev/hilla/crud/filter/PropertyStringFilter';
import Matcher from './types/dev/hilla/crud/filter/PropertyStringFilter/Matcher';
import type { PropertyInfo } from './utils';

export function useFilterField(
  propertyInfo: PropertyInfo,
  additionalProps: Record<string, any>,
  setPropertyFilter: React.MutableRefObject<(propertyFilter: PropertyStringFilter) => void>,
): JSX.Element | null {
  let field: JSX.Element | null;
  let commonProps = {};
  commonProps = { ...commonProps, ...additionalProps };
  const [matcher, setMatcher] = useState(Matcher.GREATER_THAN);
  const [filterValue, setFilterValue] = useState('');
  const select = useRef<SelectElement>(null);
  useEffect(() => {
    // Workaround for https://github.com/vaadin/react-components/issues/148
    setTimeout(() => {
      if (select.current) {
        select.current.requestContentUpdate();
      }
    }, 1);
  }, []);

  useEffect(() => {
    // Update filter
    const filter = {
      propertyId: propertyInfo.name,
      filterValue,
      matcher,
    };
    // eslint-disable-next-line
    (filter as any).t = 'propertyString';
    setPropertyFilter.current(filter);
  }, [matcher, filterValue]);

  if (propertyInfo.modelType === 'string') {
    field = (
      <TextField
        placeholder="Filter..."
        {...commonProps}
        onInput={(e: any) => {
          const fieldValue = ((e as InputEvent).target as TextFieldElement).value;
          const filterValue = fieldValue;

          setMatcher(Matcher.CONTAINS);
          setFilterValue(filterValue);
        }}
      ></TextField>
    );
  } else if (propertyInfo.modelType === 'number') {
    field = (
      <>
        <Select
          ref={select}
          onValueChanged={(e) => setMatcher(e.detail.value as Matcher)}
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
        <TextField
          placeholder="Filter..."
          onInput={(e) => {
            const fieldValue = ((e as InputEvent).target as TextFieldElement).value;
            setFilterValue(fieldValue);
          }}
        />
      </>
    );
  } else {
    field = null;
  }

  return field;
}
