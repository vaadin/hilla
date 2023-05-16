import { Button } from '@hilla/react-components/Button.js';
import { Notification } from '@hilla/react-components/Notification.js';
import {TextField, TextFieldElement} from '@hilla/react-components/TextField.js';
import { HelloReactEndpoint } from 'Frontend/generated/endpoints';
import {useState} from 'react';
import * as React from "react";

export default function HelloReactView() {
  const [name, setName] = useState('');


  function handleNameFieldKeyPress(e: React.KeyboardEvent<TextFieldElement>) {
      if (e.code === 'Enter' || e.code === 'NumpadEnter')
          sayHello();
  }

  function sayHello() {
      HelloReactEndpoint.sayHello(name).then(response => Notification.show(response));
  }

  return (
    <>
      <section className="flex p-m gap-m items-end">
          <TextField label='Your name' value={name}
                     onValueChanged={(e) => setName(e.detail.value)}
                     onKeyPress={handleNameFieldKeyPress}
          />
          <Button onClick = {sayHello}>
              Send
          </Button>
      </section>
    </>
  );
}
