import {createFileRoute} from "@tanstack/react-router";
import PageCard from "../components/PageCard.tsx";
import StreamsPage from "../components/streams/StreamsPage.tsx";

export const Route = createFileRoute("/streams")({
    component: Streams
})

function Streams() {
    return (
        <main>
            <PageCard>
                <StreamsPage/>
            </PageCard>
        </main>
    );
}
