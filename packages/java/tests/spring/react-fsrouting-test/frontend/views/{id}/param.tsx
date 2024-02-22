import { useParams } from "react-router-dom"

export default function DirectoryParameter() {
    const params = useParams();
    return <div>Params: {JSON.stringify(params)}</div>
}
