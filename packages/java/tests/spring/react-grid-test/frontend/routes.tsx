import { createBrowserRouter } from 'react-router-dom';
import MainLayout from './MainLayout.js';
import { AutoCrudView } from './views/AutoCrudView.js';
import { AutoFormView } from './views/AutoFormView.js';
import { GridWithEntityReferences } from './views/GridWithEntityReferences.js';
import { ReadOnlyGrid } from './views/ReadOnlyGrid.js';
import { ReadOnlyGridOrFilter } from './views/ReadOnlyGridOrFilter.js';
import { ReadOnlyGridSinglePropertyFilter } from './views/ReadOnlyGridSinglePropertyFilter.js';
import { ReadOnlyGridWithHeaderFilters } from './views/ReadOnlyGridWithHeaderFilter.js';

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
