import { Item } from '@hilla/react-components/Item.js';
import { ListBox } from '@hilla/react-components/ListBox.js';
import { NumberField } from '@hilla/react-components/NumberField.js';
import { Select, type SelectElement } from '@hilla/react-components/Select.js';
import { TextField, type TextFieldElement } from '@hilla/react-components/TextField.js';
import { useContext, useEffect, useRef, useState, type ReactElement } from 'react';
import { ColumnContext } from './autogrid-column-context.js';
import css from './header-filter.module.css';
import Matcher from './types/dev/hilla/crud/filter/PropertyStringFilter/Matcher';

export function HeaderFilter(): ReactElement {
  const context = useContext(ColumnContext)!;
  const [matcher, setMatcher] = useState(Matcher.GREATER_THAN);
  const [filterValue, setFilterValue] = useState('');
  const select = useRef<SelectElement>(null);

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

  useEffect(() => {
    // Workaround for https://github.com/vaadin/react-components/issues/148
    setTimeout(() => {
      if (select.current) {
        select.current.requestContentUpdate();
      }
    }, 1);
  }, []);

  if (context.propertyInfo.modelType === 'string') {
    return (
      <TextField
        placeholder="Filter..."
        onInput={(e: any) => {
          const fieldValue = ((e as InputEvent).target as TextFieldElement).value;
          updateFilter(Matcher.CONTAINS, fieldValue);
        }}
      ></TextField>
    );
  } else if (context.propertyInfo.modelType === 'number') {
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
  return <></>;
}
