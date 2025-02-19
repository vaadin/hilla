import { useComboBoxDataProvider } from '@vaadin/hilla-react-crud';
import { Button, ComboBox } from '@vaadin/react-components';
import { useState } from 'react';
import { PersonCustomService } from 'Frontend/generated/endpoints';
import Direction from 'Frontend/generated/org/springframework/data/domain/Sort/Direction';
import NullHandling from 'Frontend/generated/org/springframework/data/domain/Sort/NullHandling';

export default function ComboBoxUseComboBoxDataProviderHook(): React.JSX.Element {
  const [sort] = useState({
    orders: [{ property: 'lastName', direction: Direction.ASC, ignoreCase: true, nullHandling: NullHandling.NATIVE }],
  });
  const dataProvider = useComboBoxDataProvider(PersonCustomService.listPersonsLazyWithFilter);
  const dataProviderLastName = useComboBoxDataProvider(PersonCustomService.listPersonsLazyWithFilter, { sort });

  return (
    <div className="p-m flex flex-col gap-m">
      <ComboBox label="Default sort" dataProvider={dataProvider} itemLabelPath="lastName" />
      <ComboBox label="Sorted using last name" dataProvider={dataProviderLastName} itemLabelPath="lastName" />
      <Button
        onClick={() => {
          dataProvider.refresh();
          dataProviderLastName.refresh();
        }}
      >
        Refresh
      </Button>
    </div>
  );
}
