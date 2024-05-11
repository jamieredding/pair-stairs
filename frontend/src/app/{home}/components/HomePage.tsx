"use client"

import {Box, Card, CardContent, Divider, Stack, Tab, Tabs, Typography} from "@mui/material";
import CombinationEventDto from "@/domain/CombinationEventDto";
import CombinationTable from "@/components/CombinationTable";
import {formatFriendlyDate} from "@/utils/dateUtils";
import {parseISO} from "date-fns";
import Grid from "@mui/system/Unstable_Grid";
import {ReactNode, useState} from "react";
import CalculateCombinationForm from "@/app/{home}/components/calculate/CalculateCombinationForm";
import ManualCombinationForm from "@/app/{home}/components/manual/ManualCombinationForm";
import useCombinationEvents from "@/hooks/combinations/useCombinationEvents";
import Loading from "@/components/Loading";
import Error from "@/components/Error";
import MoreButton from "@/components/MoreButton";

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
                        <CalculateCombinationForm/>
                    </TabPanel>
                    <TabPanel value={tabIndex} index={1}>
                        <ManualCombinationForm/>
                    </TabPanel>
                </Stack>
            </CardContent>
        </Card>
    );
}


function CombinationHistoryCard() {
    const {
        combinationEvents,
        isError,
        isLoading,
        setSize
    } = useCombinationEvents();
    const dataLoaded = combinationEvents !== undefined;

    function getMoreCombinationEvents() {
        setSize(size => size + 1);
    }

    return (
        <Card>
            <CardContent>
                <Stack gap={1}>
                    <Typography variant="h4">Combination History</Typography>
                    <Stack gap={1}>
                        {combinationEvents &&
                            (combinationEvents as CombinationEventDto[][]).flat().map((combinationEvent) =>
                                <Card key={combinationEvent.id}>
                                    <CardContent>
                                        <Stack gap={1}>
                                            <Typography variant="h5">
                                                {formatFriendlyDate(parseISO(combinationEvent.date))}
                                            </Typography>
                                            <Divider/>
                                            <CombinationTable combination={combinationEvent.combination}/>
                                        </Stack>
                                    </CardContent>
                                </Card>
                            )}
                        {isLoading && <Loading/>}
                        {isError && <Error/>}
                        <MoreButton onClick={getMoreCombinationEvents} disabled={!dataLoaded}/>
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
                    <CombinationHistoryCard/>
                </Grid>
            </Grid>
        </main>
    );
}