import {AutoCrud} from "@vaadin/hilla-react-crud";
import {PersonService} from "Frontend/generated/endpoints.js";
import PersonModel from "Frontend/generated/com/vaadin/hilla/gradle/test/data/PersonModel.js";

export default function PersonCrudView() {
  return <AutoCrud  service={PersonService} model={PersonModel} />
}
