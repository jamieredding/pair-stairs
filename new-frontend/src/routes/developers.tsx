import DevelopersPage from "../components/developers/DevelopersPage.tsx";
import PageCard from "../components/PageCard.tsx";
import {createFileRoute} from "@tanstack/react-router";

export const Route = createFileRoute("/developers")({
    component: Developers
})

function Developers() {
    return (
        <main>
            <PageCard>
                <DevelopersPage/>
            </PageCard>
        </main>
    )
}