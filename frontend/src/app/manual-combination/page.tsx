"use client"

import ManualCombinationPage from "@/app/manual-combination/components/ManualCombinationPage";
import PageCard from "@/components/PageCard";
import useDeveloperInfos from "@/hooks/developers/useDeveloperInfos";
import useStreamInfos from "@/hooks/streams/useStreamInfos";

export default function Home() {
    const {allDevelopers, isLoading: loadingDevelopers, isError: erroringDevelopers} = useDeveloperInfos();
    const {allStreams, isLoading: loadingStreams, isError: erroringStreams} = useStreamInfos()

    return (
        <main>
            <PageCard>
                {(loadingDevelopers || loadingStreams) &&
                    <p>retrieving data...</p>
                }
                {(erroringDevelopers || erroringStreams) &&
                    <p>error retrieving data...</p>
                }
                {allDevelopers && allStreams &&
                    <ManualCombinationPage
                        allPossibleDevelopers={allDevelopers}
                        allPossibleStreams={allStreams}/>
                }
            </PageCard>
        </main>
    );
}
