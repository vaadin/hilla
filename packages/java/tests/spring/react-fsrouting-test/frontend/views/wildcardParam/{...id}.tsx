import { useParams } from "react-router-dom"

export default function WildcardParameter() {
    const params = useParams();
    return <div>Params: {JSON.stringify(params)}</div>
}
