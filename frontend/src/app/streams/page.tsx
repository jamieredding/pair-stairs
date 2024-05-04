import PageCard from "@/components/PageCard";
import StreamsPage from "@/app/streams/components/StreamsPage";

export default function Home() {
    return (
        <main>
            <PageCard>
                <StreamsPage/>
            </PageCard>
        </main>
    );
}
