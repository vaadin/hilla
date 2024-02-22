import { useParams } from "react-router-dom"

export default function BasicParameter() {
    const params = useParams();
    return <div>Params: {JSON.stringify(params)}</div>
}
