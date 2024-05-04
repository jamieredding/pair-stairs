"use client"

import ManualCombinationPage from "@/app/manual-combination/components/ManualCombinationPage";
import PageCard from "@/components/PageCard";

export default function Home() {

    return (
        <main>
            <PageCard>
                <ManualCombinationPage/>
            </PageCard>
        </main>
    );
}
