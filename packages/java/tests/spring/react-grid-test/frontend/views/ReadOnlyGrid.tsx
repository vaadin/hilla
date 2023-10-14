import { AutoGrid } from '@hilla/react-crud';
import type Person from 'Frontend/generated/dev/hilla/test/reactgrid/Person.js';
import PersonModel from 'Frontend/generated/dev/hilla/test/reactgrid/PersonModel.js';
import { PersonListOnlyService } from 'Frontend/generated/endpoints.js';

type GridBodyReactRendererProps<TItem> = {
  item: TItem;
};

function LuckyNumberRenderer({ item }: GridBodyReactRendererProps<Person>): JSX.Element {
  const value = item.luckyNumber;
  return <span style={{ fontWeight: 'bold', color: value % 2 === 0 ? 'green' : 'red' }}>{value}</span>;
}

export function ReadOnlyGrid(): JSX.Element {
  return (
    <AutoGrid
      pageSize={10}
      service={PersonListOnlyService}
      model={PersonModel}
      columnOptions={{ luckyNumber: { renderer: LuckyNumberRenderer } }}
      rowNumbers
    />
  );
  /* page size is defined only to make testing easier */
}
