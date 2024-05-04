"use client"

import ManualCombinationPage from "@/app/manual-combination/components/ManualCombinationPage";
import {useGetDeveloperInfos} from "@/infrastructure/DeveloperClient";
import {useGetStreamInfos} from "@/infrastructure/StreamClient";
import PageCard from "@/components/PageCard";

export default function Home() {
    const {allDevelopers, isLoading: loadingDevelopers, isError: erroringDevelopers} = useGetDeveloperInfos();
    const {allStreams, isLoading: loadingStreams, isError: erroringStreams} = useGetStreamInfos()

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
