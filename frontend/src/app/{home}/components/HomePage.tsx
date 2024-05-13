"use client"

import {
    Box,
    Button,
    Card,
    CardContent,
    Dialog,
    DialogActions,
    DialogContent,
    DialogContentText,
    Divider,
    Stack,
    Tab,
    Tabs,
    Tooltip,
    Typography
} from "@mui/material";
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
import CloseButton from "@/components/CloseButton";
import useDeleteCombinationEvent from "@/hooks/combinations/useDeleteCombinationEvent";
import useRefreshCombinationEvents from "@/hooks/combinations/useRefreshCombinationEvents";

interface TabPanelProps {
    children: ReactNode,
    index: number,
    value: number,
    id: string,
    ariaLabelledBy: string
}

function TabPanel({ariaLabelledBy, children, id, index, value}: TabPanelProps) {
    return (
        <div
            role="tabpanel"
            hidden={value !== index}
            id={id}
            aria-labelledby={ariaLabelledBy}
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
                        <Tabs value={tabIndex} onChange={(_, newValue) => setTabIndex(newValue)}
                              aria-label="new combination tabs">
                            <Tab label="Calculate" id="new-combination-tab-0" aria-controls="calculate-tabpanel-0"/>
                            <Tab label="Manual" id="new-combination-tab-1" aria-controls="manual-tabpanel-1"/>
                        </Tabs>
                    </Box>
                    <TabPanel value={tabIndex} index={0}
                              id="calculate-tabpanel-0" ariaLabelledBy="new-combination-tab-0">
                        <CalculateCombinationForm/>
                    </TabPanel>
                    <TabPanel value={tabIndex} index={1}
                              id="manual-tabpanel-1" ariaLabelledBy="new-combination-tab-1">
                        <ManualCombinationForm/>
                    </TabPanel>
                </Stack>
            </CardContent>
        </Card>
    );
}

interface ConfirmDeleteDialogProps {
    combinationEvent: CombinationEventDto;
    onClose: () => void
}

function ConfirmDeleteDialog({combinationEvent, onClose}: ConfirmDeleteDialogProps) {
    const {trigger: deleteCombinationEvent} = useDeleteCombinationEvent();
    const {refresh: refreshCombinationEvents} = useRefreshCombinationEvents();

    function handleDeleteCombinationEvent() {
        deleteCombinationEvent(combinationEvent.id)
            .then(async _ => {
                await refreshCombinationEvents()
                onClose()
            })
    }

    return (
        <Dialog open={true}>
            <DialogContent>
                <DialogContentText>
                    <p>
                        Are you sure you want to delete the combination for {combinationEvent.date}?
                    </p>
                    <p>
                        This action cannot be undone.
                    </p>
                </DialogContentText>
                <DialogActions>
                    <Button onClick={onClose}>Cancel</Button>
                    <Button onClick={handleDeleteCombinationEvent}>Delete</Button>
                </DialogActions>
            </DialogContent>
        </Dialog>
    )
}


function CombinationHistoryCard() {
    const {
        combinationEvents,
        isError,
        isLoading,
        setSize
    } = useCombinationEvents();
    const dataLoaded = combinationEvents !== undefined;

    const [combinationEventToDelete, setCombinationEventToDelete] = useState<CombinationEventDto>();

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
                                            <Stack direction="row">
                                                <Tooltip title={combinationEvent.date} placement="right">
                                                    <Typography variant="h5">
                                                        {formatFriendlyDate(parseISO(combinationEvent.date))}
                                                    </Typography>
                                                </Tooltip>
                                                <CloseButton
                                                    sx={{marginLeft: "auto"}}
                                                    onClick={() => setCombinationEventToDelete(combinationEvent)}/>
                                            </Stack>
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
                {combinationEventToDelete &&
                    <ConfirmDeleteDialog combinationEvent={combinationEventToDelete}
                                         onClose={() => setCombinationEventToDelete(undefined)}
                    />
                }
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