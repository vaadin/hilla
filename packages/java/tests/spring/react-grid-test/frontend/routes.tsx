import { createBrowserRouter } from 'react-router-dom';
import MainLayout from './MainLayout';
import { AutoCrudView } from './views/AutoCrudView';
import { AutoFormView } from './views/AutoFormView';
import { GridWithEntityReferences } from './views/GridWithEntityReferences';
import { ReadOnlyGrid } from './views/ReadOnlyGrid';
import { ReadOnlyGridOrFilter } from './views/ReadOnlyGridOrFilter';
import { ReadOnlyGridSinglePropertyFilter } from './views/ReadOnlyGridSinglePropertyFilter';
import { ReadOnlyGridWithHeaderFilters } from './views/ReadOnlyGridWithHeaderFilter';

export const routes = [
  {
    path: '',
    element: <MainLayout />,
    children: [
      {
        path: '/readonly-grid',
        element: <ReadOnlyGrid />,
      },
      {
        path: '/readonly-grid-single-property-filter',
        element: <ReadOnlyGridSinglePropertyFilter />,
      },
      {
        path: '/readonly-grid-or-filter',
        element: <ReadOnlyGridOrFilter />,
      },
      {
        path: '/readonly-grid-with-headerfilters',
        element: <ReadOnlyGridWithHeaderFilters />,
      },
      {
        path: '/grid-entityrefs',
        element: <GridWithEntityReferences />,
      },
      {
        path: '/auto-form',
        element: <AutoFormView />,
      },
      {
        path: '/auto-crud',
        element: <AutoCrudView />,
      },
    ],
  },
];

const router = createBrowserRouter([...routes]);
export default router;
