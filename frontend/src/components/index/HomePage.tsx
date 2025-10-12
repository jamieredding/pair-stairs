import {
    AlertTitle,
    Box,
    Button,
    Card,
    CardContent,
    CircularProgress,
    Dialog,
    DialogActions,
    DialogContent,
    Divider,
    Stack,
    Tab,
    Tabs,
    Tooltip,
    Typography
} from "@mui/material";
import {parseISO} from "date-fns";
import {Grid} from "@mui/system";
import {type ReactNode, useState} from "react";
import type CombinationEventDto from "../../domain/CombinationEventDto.ts";
import CalculateCombinationForm from "./calculate/CalculateCombinationForm.tsx";
import ManualCombinationForm from "./manual/ManualCombinationForm.tsx";
import useDeleteCombinationEvent from "../../hooks/combinations/useDeleteCombinationEvent.ts";
import useRefreshCombinationEvents from "../../hooks/combinations/useRefreshCombinationEvents.ts";
import useCombinationEvents from "../../hooks/combinations/useCombinationEvents.ts";
import {formatFriendlyDate} from "../../utils/dateUtils.ts";
import CloseButton from "../CloseButton.tsx";
import CombinationTable from "../CombinationTable.tsx";
import Loading from "../Loading.tsx";
import Error from "../Error.tsx";
import MoreButton from "../MoreButton.tsx";
import {useNavigate, useSearch} from "@tanstack/react-router";
import {CustomLink} from "../CustomLink.tsx";
import ErrorSnackbar from "../ErrorSnackbar.tsx";

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

type TabKey = 'calculate' | 'manual';
const indexByKey: Record<TabKey, number> = {calculate: 0, manual: 1};
const keyByIndex = (i: number): TabKey => (i === 1 ? 'manual' : 'calculate');

export interface Props {
    route: "/" | "/team/$teamName";
}

function NewCombinationCard({route} : Props) {
    const navigate = useNavigate();
    const search = useSearch({from: route})
    const tabKey: TabKey = search.newCombinationTab;
    const tabIndex = indexByKey[tabKey]

    return (
        <Card>
            <CardContent>
                <Stack gap={1}>
                    <Typography variant="h4">New Combination</Typography>
                    <Box sx={{borderBottom: 1, borderColor: 'divider'}}>
                        <Tabs
                            value={tabIndex}
                            onChange={(_, nextIndex: number) => {
                                navigate({
                                    to: '.',
                                    search: (prev) => ({
                                        ...prev,
                                        newCombinationTab: keyByIndex(nextIndex)
                                    }),
                                });
                            }}
                            aria-label="new combination tabs"
                        >
                            <Tab
                                label="Calculate"
                                id="new-combination-tab-0"
                                aria-controls="calculate-tabpanel-0"
                                value={0}
                                component={CustomLink as React.ElementType}
                                to="."
                                search={(prev: Record<string, unknown>) => ({
                                    ...prev,
                                    newCombinationTab: 'calculate'
                                })}
                            />
                            <Tab
                                label="Manual"
                                id="new-combination-tab-1"
                                aria-controls="manual-tabpanel-1"
                                value={1}
                                component={CustomLink as React.ElementType}
                                to="."
                                search={(prev: Record<string, unknown>) => ({
                                    ...prev,
                                    newCombinationTab: 'manual'
                                })}
                            />
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
    const {trigger: deleteCombinationEvent, isLoading: loadingDelete, isError: deleteError} = useDeleteCombinationEvent();
    const {refresh: refreshCombinationEvents} = useRefreshCombinationEvents();

    function handleDeleteCombinationEvent() {
        deleteCombinationEvent(combinationEvent.id)
            .then(async () => {
                await refreshCombinationEvents()
                onClose()
            })
    }

    return (
        <Dialog open={true}>
            <DialogContent>
                <p>
                    Are you sure you want to delete the combination for {combinationEvent.date}?
                </p>
                <p>
                    This action cannot be undone.
                </p>
                <DialogActions>
                    <Button onClick={onClose}>Cancel</Button>
                    <Button disabled={loadingDelete} onClick={handleDeleteCombinationEvent}>
                        Delete
                        {loadingDelete &&
                            <CircularProgress size={20} sx={({marginLeft: (theme) => theme.spacing(1)})} />
                        }
                    </Button>
                </DialogActions>
                <ErrorSnackbar error={deleteError} alertContent={(errorCode: string) =>
                    <>
                        <AlertTitle>Unexpected error: {errorCode}</AlertTitle>
                        Error while trying to delete, try again.
                    </>
                } />
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

export default function HomePage({route} : Props) {
    return (
        <main>
            <Grid container justifyContent="center" spacing={1}>
                <Grid size={{xs: 12, md: 12, lg: 6}}>
                    <NewCombinationCard route={route} />
                </Grid>
                <Grid size={{xs: 12, md: 12, lg: 6}}>
                    <CombinationHistoryCard/>
                </Grid>
            </Grid>
        </main>
    );
}