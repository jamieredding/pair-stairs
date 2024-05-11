"use client"

import {Box, Card, CardContent, Stack, Tab, Tabs, Typography} from "@mui/material";
import CombinationEventDto from "@/domain/CombinationEventDto";
import CombinationTable from "@/components/CombinationTable";
import {formatFriendlyDate} from "@/utils/dateUtils";
import {parseISO} from "date-fns";
import Grid from "@mui/system/Unstable_Grid";
import {ReactNode, useState} from "react";
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


interface TabPanelProps {
    children: ReactNode;
    index: number;
    value: number;
}

function TabPanel(props: TabPanelProps) {
    const {children, value, index, ...other} = props;

    return (
        <div
            role="tabpanel"
            hidden={value !== index}
        >
            {value === index && (
                <Box sx={{padding: 1}}>
                    {children}
                </Box>
            )}
        </div>
    );
}

function NewCombinationCard() {
    const [tabIndex, setTabIndex] = useState(0);

    return (
        <Card>
            <CardContent>
                <Stack gap={1}>
                    <Typography variant="h4">New Combination</Typography>
                    <Box sx={{borderBottom: 1, borderColor: 'divider'}}>
                        <Tabs value={tabIndex} onChange={(_, newValue) => setTabIndex(newValue)}>
                            <Tab label="Calculate"/>
                            <Tab label="Manual"/>
                        </Tabs>
                    </Box>
                    <TabPanel value={tabIndex} index={0}>
                        <DailyCombinationPage/>
                    </TabPanel>
                    <TabPanel value={tabIndex} index={1}>
                        <ManualCombinationPage/>
                    </TabPanel>
                </Stack>
            </CardContent>
        </Card>
    );
}


interface CombinationHistoryCardProps {
    combinationEvents: CombinationEventDto[],
}

function CombinationHistoryCard({combinationEvents}: CombinationHistoryCardProps) {

    return (
        <Card>
            <CardContent>
                <Stack gap={1}>
                    <Typography variant="h4">Combination History</Typography>
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
    return (
        <main>
            <Grid container justifyContent="center" gap={1}>
                <Grid xs={12} sm={4}>
                    <NewCombinationCard/>
                </Grid>
                <Grid xs={12} sm={4}>
                    <CombinationHistoryCard combinationEvents={combinationEvents}/>
                </Grid>
            </Grid>
        </main>
    );
}