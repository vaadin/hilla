import { useForm } from '@vaadin/hilla-react-form';
import {Button, Checkbox, DatePicker, HorizontalLayout, TextField, VerticalLayout} from '@vaadin/react-components';
import {PersonService} from "Frontend/generated/endpoints.js";
import PersonModel from "Frontend/generated/com/vaadin/hilla/gradle/test/data/PersonModel.js";

export default function PersonFormView() {

  const { model, field, reset, submit } = useForm(PersonModel, {
    onSubmit: async (model) => {
      await PersonService.save(model);
    }
  });

  return (
    <VerticalLayout theme='spacing padding'>
      <TextField id='firstname' label="Firstname" {...field(model.firstName)} />
      <TextField id='lastname' label="Lastname" {...field(model.lastName)} />
      <TextField id='email' label="Email" {...field(model.email)} />
      <DatePicker id='dob' label="Date of birth" {...field(model.dateOfBirth)} />
      <Checkbox id='vip' label="VIP?" {...field(model.important)} />
      <HorizontalLayout>
        <Button id='save' onClick={submit}>Save</Button>
        <Button id='reset' onClick={() => reset}>Reset</Button>
      </HorizontalLayout>
    </VerticalLayout>
  );
}
