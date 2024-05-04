"use client"

import AddNewCombination from "@/components/AddNewCombination";
import {useGetDeveloperInfos} from "@/infrastructure/DeveloperClient";
import {useGetStreamInfos} from "@/infrastructure/StreamClient";
import Grid from "@mui/system/Unstable_Grid";
import {Card, CardContent} from "@mui/material";

export default function Home() {
    const {allDevelopers, isLoading: loadingDevelopers, isError: erroringDevelopers} = useGetDeveloperInfos();
    const {allStreams, isLoading: loadingStreams, isError: erroringStreams} = useGetStreamInfos()

    return (
        <main>
            <Grid container justifyContent="center">
                <Grid xs={12} sm={4}>
                    <Card>
                        <CardContent>
                            {(loadingDevelopers || loadingStreams) &&
                                <p>retrieving data...</p>
                            }
                            {(erroringDevelopers || erroringStreams) &&
                                <p>error retrieving data...</p>
                            }
                            {allDevelopers && allStreams &&
                                <AddNewCombination
                                    allPossibleDevelopers={allDevelopers}
                                    allPossibleStreams={allStreams}/>
                            }
                        </CardContent>
                    </Card>
                </Grid>
            </Grid>
        </main>
    );
}
