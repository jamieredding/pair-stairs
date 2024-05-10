"use client"

import {Button, Card, CardContent, Stack, Typography} from "@mui/material";
import CombinationEventDto from "@/domain/CombinationEventDto";
import CombinationTable from "@/components/CombinationTable";
import {formatFriendlyDate} from "@/utils/dateUtils";
import {parseISO} from "date-fns";
import Grid from "@mui/system/Unstable_Grid";
import {useState} from "react";
import ButtonRow from "@/components/ButtonRow";
import DailyCombinationPage from "@/app/daily-combination/components/DailyCombinationPage";
import ManualCombinationPage from "@/app/manual-combination/components/ManualCombinationPage";

const combinationEvents: CombinationEventDto[] = [
    {
        id: 1,
        date: "2024-05-10",
        combination: [
            {
                developers: [
                    {id: 0, displayName: "dev-0"},
                    {id: 1, displayName: "dev-1"},
                ],
                stream:
                    {id: 1, displayName: "stream-a"}

            },
            {
                developers: [
                    {id: 2, displayName: "dev-2"},
                ],
                stream:
                    {id: 2, displayName: "stream-b"}

            }
        ]
    },

    {
        id: 2,
        date: "2024-05-09",
        combination: [
            {
                developers: [
                    {id: 0, displayName: "dev-0"},
                    {id: 2, displayName: "dev-2"},
                ],
                stream:
                    {id: 1, displayName: "stream-a"}

            },
            {
                developers: [
                    {id: 1, displayName: "dev-1"},
                ],
                stream:
                    {id: 2, displayName: "stream-b"}

            }
        ]
    },

    {
        id: 3,
        date: "2024-05-08",
        combination: [
            {
                developers: [
                    {id: 1, displayName: "dev-1"},
                    {id: 2, displayName: "dev-2"},
                ],
                stream:
                    {id: 1, displayName: "stream-a"}

            },
            {
                developers: [
                    {id: 0, displayName: "dev-0"},
                ],
                stream:
                    {id: 2, displayName: "stream-b"}

            }
        ]
    },
];

interface CombinationEventsCardProps {
    combinationEvents: CombinationEventDto[],
    handleOpenPanel: (chosenPanel: SupportedPanel) => void
}

enum SupportedPanel {
    CalculateCombinations,
    ManualCombinations
}

function CombinationEventsCard({combinationEvents, handleOpenPanel}: CombinationEventsCardProps) {

    return (
        <Card>
            <CardContent>
                <Stack gap={1}>
                    <Typography variant="h4">Combination Events</Typography>
                    <Typography variant="h5">Create a new combination</Typography>
                    <ButtonRow>
                        <Button variant="outlined"
                                onClick={() => handleOpenPanel(SupportedPanel.CalculateCombinations)}>Calculate</Button>
                        <Button variant="outlined"
                                onClick={() => handleOpenPanel(SupportedPanel.ManualCombinations)}>Manual</Button>
                    </ButtonRow>
                    <Stack gap={1}>
                        {combinationEvents.map((combinationEvent) =>
                            <Card key={combinationEvent.id}>
                                <CardContent>
                                    <Stack gap={1}>
                                        <Typography variant="h5">
                                            {formatFriendlyDate(parseISO(combinationEvent.date))}
                                        </Typography>
                                        <CombinationTable combination={combinationEvent.combination}/>
                                    </Stack>
                                </CardContent>
                            </Card>
                        )}
                    </Stack>
                </Stack>
            </CardContent>
        </Card>
    );
}

export default function HomePage() {
    const [selectedPanel, setSelectedPanel] = useState<SupportedPanel>(SupportedPanel.CalculateCombinations);

    return (
        <main>
            <Grid container justifyContent="center" gap={1}>
                <Grid xs={12} sm={4}>
                    <CombinationEventsCard combinationEvents={combinationEvents}
                                           handleOpenPanel={setSelectedPanel}/>
                </Grid>
                <Grid xs={12} sm={4}>
                    <Card>
                        <CardContent>
                            {selectedPanel === SupportedPanel.CalculateCombinations &&
                                <DailyCombinationPage/>
                            }
                            {selectedPanel === SupportedPanel.ManualCombinations &&
                                <ManualCombinationPage/>
                            }
                        </CardContent>
                    </Card>
                </Grid>
            </Grid>
        </main>
    );
}