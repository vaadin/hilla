import { Item } from '@hilla/react-components/Item.js';
import { ListBox } from '@hilla/react-components/ListBox.js';
import { Select, type SelectElement } from '@hilla/react-components/Select.js';
import { TextField, type TextFieldElement } from '@hilla/react-components/TextField.js';
import { createContext, useEffect, useRef, useState, useContext, type ReactElement } from 'react';
import css from './header-filter.module.css';
import type PropertyStringFilter from './types/dev/hilla/crud/filter/PropertyStringFilter';
import Matcher from './types/dev/hilla/crud/filter/PropertyStringFilter/Matcher';
import type { PropertyInfo } from './utils';

export type HeaderFilterContextProps = {
  propertyInfo: PropertyInfo;
  setPropertyFilterRef: React.MutableRefObject<(propertyFilter: PropertyStringFilter) => void>;
};

export const HeaderFilterContext = createContext<HeaderFilterContextProps | null>(null);

export function HeaderFilter(): ReactElement {
  const context = useContext(HeaderFilterContext);
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
      propertyId: context!.propertyInfo.name,
      filterValue,
      matcher,
    };
    // eslint-disable-next-line
    (filter as any).t = 'propertyString';
    context!.setPropertyFilterRef.current(filter);
  }, [matcher, filterValue]);

  if (context!.propertyInfo.modelType === 'string') {
    return (
      <TextField
        placeholder="Filter..."
        onInput={(e: any) => {
          const fieldValue = ((e as InputEvent).target as TextFieldElement).value;

          setMatcher(Matcher.CONTAINS);
          setFilterValue(fieldValue);
        }}
      ></TextField>
    );
  } else if (context!.propertyInfo.modelType === 'number') {
    return (
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
  }
  return <></>;
}
