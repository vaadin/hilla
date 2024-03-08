import { createBrowserRouter } from 'react-router-dom';
import MainLayout from './MainLayout.js';
import { AutoCrudView } from 'Frontend/views/AutoCrudView.js';
import { AutoFormView } from 'Frontend/views/AutoFormView.js';
import { GridWithEntityReferences } from 'Frontend/views/GridWithEntityReferences.js';
import { ReadOnlyGrid } from 'Frontend/views/ReadOnlyGrid.js';
import { ReadOnlyGridOrFilter } from 'Frontend/views/ReadOnlyGridOrFilter.js';
import { ReadOnlyGridSinglePropertyFilter } from 'Frontend/views/ReadOnlyGridSinglePropertyFilter.js';
import { ReadOnlyGridWithHeaderFilters } from 'Frontend/views/ReadOnlyGridWithHeaderFilter.js';
import { GridUseDataProviderHook } from 'Frontend/views/GridUseDataProviderHookView';
import { ReadOnlyGridCustomFilter } from 'Frontend/views/ReadOnlyGridCustomFilter';

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
        path: '/readonly-grid-custom-filter',
        element: <ReadOnlyGridCustomFilter />,
      },
      {
        path: '/grid-entityrefs',
        element: <GridWithEntityReferences />,
      },
      {
        path: '/grid-use-data-provider-hook',
        element: <GridUseDataProviderHook />,
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
