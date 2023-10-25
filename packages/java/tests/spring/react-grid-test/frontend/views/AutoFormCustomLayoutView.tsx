import {ExperimentalAutoForm} from "@hilla/react-crud";
import {PersonService} from "Frontend/generated/endpoints";
import PersonModel from "Frontend/generated/dev/hilla/test/reactgrid/PersonModel";
import {useState} from "react";
import {Button} from "@hilla/react-components/Button.js";


export default function AutoFormCustomLayoutView() {

  const [customFormLayout, setCustomFormLayout] = useState(undefined);
  const [layoutName, setLayoutName] = useState('');

  const layouts = [
    {template: [['firstName', 'lastName', 'birthDate', 'luckyNumber'],
                ['joinedDate', 'emailVerified', 'averageGrade'],
                ['shiftStart', 'appointmentTime']]
    },
    {template: [['firstName', 'lastName', 'birthDate', 'luckyNumber'],
                ['joinedDate', 'emailVerified', 'averageGrade'],
                ['shiftStart', 'appointmentTime']],
      responsiveSteps: [{minWidth: '0', columns: 1},
                        {minWidth: '800px', columns: 6},
                        {minWidth: '1200px', columns: 12}]
    },
    {template: [[{property:'firstName', colSpan: 3}, {property:'lastName', colSpan: 3}, {property:'birthDate', colSpan: 3}, {property:'luckyNumber', colSpan: 3}],
                [{property:'joinedDate', colSpan: 3}, {property:'emailVerified', colSpan: 3}, {property:'averageGrade', colSpan: 6}],
                [{property:'shiftStart', colSpan: 6}, {property:'appointmentTime', colSpan: 6}]]
    },
    {template: [[{property:'firstName', colSpan: 3}, {property:'lastName', colSpan: 3}, {property:'birthDate', colSpan: 3}, {property:'luckyNumber', colSpan: 3}],
                [{property:'joinedDate', colSpan: 3}, {property:'emailVerified', colSpan: 3}, {property:'averageGrade', colSpan: 6}],
                [{property:'shiftStart', colSpan: 6}, {property:'appointmentTime', colSpan: 6}]],
      responsiveSteps: [{minWidth: '0', columns: 1},
                        {minWidth: '800px', columns: 6},
                        {minWidth: '1200px', columns: 12}]
    }
  ]

  function setLayout( index: 0 | 1 | 2 | 3) {
    // @ts-ignore
    setCustomFormLayout(layouts[index]);
    setLayoutName('Layout ' + (index + 1));
  }

  return (
    <>
      <section className="flex flex-row p-m gap-m">
        <Button theme="primary" onClick={() => setLayout(0)}>Layout 1</Button>
        <Button theme="primary" onClick={() => setLayout(1)}>Layout 2</Button>
        <Button theme="primary" onClick={() => setLayout(2)}>Layout 3</Button>
        <Button theme="primary" onClick={() => setLayout(3)}>Layout 4</Button>
        <label>Selected layout: {!!layoutName ? layoutName : 'none'}</label>
      </section>
      <ExperimentalAutoForm service={PersonService}
                            model={PersonModel}
                            customFormLayout={customFormLayout} />
    </>
  );
}
