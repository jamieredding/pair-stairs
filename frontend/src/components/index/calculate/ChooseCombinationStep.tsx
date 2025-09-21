import {Alert, AlertTitle, Button, Divider, Snackbar, Stack, Typography} from "@mui/material";
import {ArrowBack} from "@mui/icons-material";
import {useEffect, useRef, useState} from "react";
import {formatISO} from "date-fns";
import useCalculateCombinations from "../../../hooks/combinations/useCalculateCombinations";
import useAddCombinationEvent from "../../../hooks/combinations/useAddCombinationEvent.ts";
import useRefreshCombinationEvents from "../../../hooks/combinations/useRefreshCombinationEvents.ts";
import type ScoredCombinationDto from "../../../domain/ScoredCombinationDto.ts";
import ScoredCombinationsTable from "./ScoredCombinationsTable.tsx";
import type {SaveCombinationEventDto} from "../../../domain/SaveCombinationEventDto.ts";
import {type PairStreamByIds} from "../../../domain/SaveCombinationEventDto.ts";
import Loading from "../../Loading.tsx";
import Error from "../../Error.tsx";
import ButtonRow from "../../ButtonRow.tsx";
import MoreButton from "../../MoreButton.tsx";
import SaveButton from "../../SaveButton.tsx";

import type {ApiError} from "../../../domain/ApiError.ts";


interface ChooseCombinationStepProps {
    developerIds: number[],
    streamIds: number[],
    updateForm: (value: ((prevState: number) => number)) => void,
    resetForm: () => void
}

interface CombinationIndex {
    row: number;
    column: number;
}

export default function ChooseCombinationStep({
                                                  developerIds,
                                                  streamIds,
                                                  updateForm,
                                                  resetForm
                                              }: ChooseCombinationStepProps) {
    const {
        combinationsPages,
        isError,
        isLoading,
        setSize
    } = useCalculateCombinations({developerIds, streamIds});
    const dataLoaded = combinationsPages !== undefined;

    // Reset the size of the list of combinations when the developer or stream ids change
    // This is necessary because the list of combinations is paginated and otherwise the list would start with too many options
    useEffect(() => {
        setSize(1)
    }, [setSize, developerIds, streamIds]);

    const [selectedCombinationIndex, setSelectedCombinationIndex] = useState<CombinationIndex>()
    const [errorSnackbarOpen, setErrorSnackbarOpen] = useState(false)
    const prevErrorRef = useRef<ApiError | null>(null);

    // Open snackbar when a *new* error appears
    useEffect(() => {
        const prev = prevErrorRef.current;
        if (isError && isError !== prev) {
            setErrorSnackbarOpen(true);
        }
        prevErrorRef.current = (isError as ApiError | null) ?? null;
    }, [isError]);

    const {trigger: addCombinationEvent} = useAddCombinationEvent()
    const {refresh: refreshCombinationEvents} = useRefreshCombinationEvents()

    const nothingSelected = selectedCombinationIndex === undefined;

    function progressForm(direction: number) {
        updateForm(prevState => prevState + direction)
    }

    function getMoreCombinations() {
        setSize(size => size + 1)
    }

    function getSelectedIndexForRow(rowIndex: number): number | undefined {
        if (selectedCombinationIndex) {
            if (selectedCombinationIndex.row === rowIndex) {
                return selectedCombinationIndex.column;
            }
        }
        return undefined;
    }

    function saveCombination() {
        const index = selectedCombinationIndex as CombinationIndex;
        const pages = combinationsPages as ScoredCombinationDto[][]
        const scoredCombination: ScoredCombinationDto = pages[index.row][index.column]
        const pairStreamsByIds: PairStreamByIds[] = scoredCombination.combination.map(c => ({
            streamId: c.stream.id,
            developerIds: c.developers.map(d => d.id)
        }))

        const data: SaveCombinationEventDto = {
            date: formatISO(new Date(), {representation: "date"}),
            combination: pairStreamsByIds
        }

        addCombinationEvent(data)
            .then(async () => {
                await refreshCombinationEvents()
                resetForm()
            })
    }

    return (
        <Stack gap={1}>
            <Typography variant="h5">Possible combinations</Typography>
            {
                combinationsPages &&
                combinationsPages.map((combinations, rowIndex) =>
                    <ScoredCombinationsTable key={rowIndex}
                                             dtos={combinations}
                                             selectedIndex={getSelectedIndexForRow(rowIndex)}
                                             setSelectedIndex={(columnIndex: number) => setSelectedCombinationIndex({
                                                 column: columnIndex,
                                                 row: rowIndex
                                             })}
                    />
                )
            }
            {isLoading && <Loading/>}
            {isError &&
                <>
                    <Error/>
                    <Snackbar
                        open={errorSnackbarOpen}
                        onClose={() => setErrorSnackbarOpen(false)}
                        anchorOrigin={{vertical: "bottom", horizontal: "center"}}
                    >
                        <div>
                            <CustomAlert errorCode={isError?.errorCode ?? "UNKNOWN"}
                                         onClose={() => setErrorSnackbarOpen(false)}/>
                        </div>
                    </Snackbar>
                </>
            }
            <Divider/>
            <ButtonRow>
                <Button variant="outlined" onClick={() => progressForm(-1)}>
                    <ArrowBack sx={({marginRight: (theme) => theme.spacing(1)})}/>
                    Back
                </Button>
                <MoreButton onClick={getMoreCombinations} disabled={!dataLoaded}/>
                <SaveButton disabled={!dataLoaded || nothingSelected} onClick={saveCombination}/>
            </ButtonRow>
        </Stack>
    );
}

interface CustomAlertProps {
    errorCode: string;
    onClose: () => void;
}

function CustomAlert({errorCode, onClose}: CustomAlertProps) {
    switch (errorCode) {
        case "NOT_ENOUGH_DEVELOPERS":
            return <Alert severity="error" onClose={onClose}>
                <AlertTitle>Not enough developers for pairing</AlertTitle>
                Click back to choose another option:
                <ul>
                    <li>Choose fewer streams</li>
                    <li>Choose more developers</li>
                    <li>Manually choose the combination you want</li>
                </ul>
            </Alert>
        case "NOT_ENOUGH_STREAMS":
            return <Alert severity="error" onClose={onClose}>
                <AlertTitle>Not enough streams for the developers</AlertTitle>
                Click back to choose another option:
                <ul>
                    <li>Choose fewer developers</li>
                    <li>Create a new stream</li>
                    <li>Manually choose the combination you want</li>
                </ul>
            </Alert>
        default:
            return <Alert severity="error" onClose={onClose}>
                <AlertTitle>Unexpected error: {errorCode}</AlertTitle>
                Click back to try again or refresh the page.
            </Alert>
    }
}