import { Button } from '@vaadin/react-components';
import { Grid } from '@vaadin/react-components/Grid';
import { GridSortColumn } from '@vaadin/react-components/GridSortColumn';
import { useGridDataProvider } from '../../../../../../ts/react-crud/data-provider';
import { PersonCustomService } from 'Frontend/generated/endpoints';

export default function GridUseGridDataProviderHook(): React.JSX.Element {
  const dataProvider = useGridDataProvider(PersonCustomService.listPersonsLazy);

  return (
    <div className="p-m flex flex-col gap-m">
      <Grid pageSize={10} dataProvider={dataProvider}>
        <GridSortColumn path="firstName" />
        <GridSortColumn path="lastName" />
        <GridSortColumn path="gender" />
      </Grid>
      <Button onClick={dataProvider.refresh}>Refresh</Button>
    </div>
  );
}
