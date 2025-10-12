import {AlertTitle, Button, Divider, Stack, Typography} from "@mui/material";
import {ArrowBack} from "@mui/icons-material";
import {type ReactElement, useEffect, useState} from "react";
import {formatISO} from "date-fns";
import useCalculateCombinations from "../../../hooks/combinations/useCalculateCombinations";
import useAddCombinationEvent from "../../../hooks/combinations/useAddCombinationEvent.ts";
import useRefreshCombinationEvents from "../../../hooks/combinations/useRefreshCombinationEvents.ts";
import type ScoredCombinationDto from "../../../domain/ScoredCombinationDto.ts";
import ScoredCombinationsTable from "./ScoredCombinationsTable.tsx";
import type {SaveCombinationEventDto} from "../../../domain/SaveCombinationEventDto.ts";
import {type PairStreamByIds} from "../../../domain/SaveCombinationEventDto.ts";
import Loading from "../../Loading.tsx";
import ButtonRow from "../../ButtonRow.tsx";
import MoreButton from "../../MoreButton.tsx";
import SaveButton from "../../SaveButton.tsx";
import ErrorSnackbar from "../../ErrorSnackbar.tsx";


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
        isError: calculateError,
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

    const {trigger: addCombinationEvent, isLoading: loadingAdd, isError: addError} = useAddCombinationEvent()
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
            <ErrorSnackbar error={calculateError} alertContent={alertContent} />
            <ErrorSnackbar error={addError} alertContent={alertContent} />
            <Divider/>
            <ButtonRow>
                <Button variant="outlined" onClick={() => progressForm(-1)}>
                    <ArrowBack sx={({marginRight: (theme) => theme.spacing(1)})}/>
                    Back
                </Button>
                <MoreButton onClick={getMoreCombinations} disabled={!dataLoaded}/>
                <SaveButton disabled={!dataLoaded || nothingSelected} loading={loadingAdd} onClick={saveCombination}/>
            </ButtonRow>
        </Stack>
    );
}

function alertContent(errorCode: string): ReactElement {
    switch (errorCode) {
        case "NOT_ENOUGH_DEVELOPERS":
            return <>
                <AlertTitle>Not enough developers for pairing</AlertTitle>
                Click back to choose another option:
                <ul>
                    <li>Choose fewer streams</li>
                    <li>Choose more developers</li>
                    <li>Manually choose the combination you want</li>
                </ul>
            </>
        case "NOT_ENOUGH_STREAMS":
            return <>
                <AlertTitle>Not enough streams for the developers</AlertTitle>
                Click back to choose another option:
                <ul>
                    <li>Choose fewer developers</li>
                    <li>Create a new stream</li>
                    <li>Manually choose the combination you want</li>
                </ul>
            </>
        default:
            return <>
                <AlertTitle>Unexpected error: {errorCode}</AlertTitle>
                Click back to try again or refresh the page.
            </>
    }
}