import {createFileRoute} from '@tanstack/react-router'
import {NewTeamPage} from "../components/new/team/NewTeamPage.tsx";

export const Route = createFileRoute('/new/team')({
    component: RouteComponent,
})

function RouteComponent() {
    return (
        <main>
            <NewTeamPage/>
        </main>
    )
}
