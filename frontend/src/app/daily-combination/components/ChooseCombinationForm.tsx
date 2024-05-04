import {Button, Divider, Stack, Typography} from "@mui/material";
import {ArrowBack, ArrowDownward} from "@mui/icons-material";
import ScoredCombinationDto from "@/domain/ScoredCombinationDto";
import ScoredCombinationsTable from "@/app/daily-combination/components/ScoredCombinationsTable";
import {useEffect, useState} from "react";
import SaveButton from "@/components/SaveButton";
import ButtonRow from "@/components/ButtonRow";
import {usePostForCalculateCombinations, usePostForSaveCombinationEvent} from "@/infrastructure/CombinationClient";
import SaveCombinationEventDto, {PairStreamByIds} from "@/domain/SaveCombinationEventDto";
import {formatISO} from "date-fns";
import {useRouter} from "next/navigation";

interface ChooseCombinationFormProps {
    developerIds: number[],
    streamIds: number[],
    updateForm: (value: ((prevState: number) => number)) => void
}

interface CombinationIndex {
    row: number;
    column: number;
}

export default function ChooseCombinationForm({developerIds, streamIds, updateForm}: ChooseCombinationFormProps) {
    const {combinations, trigger, isError, isLoading} = usePostForCalculateCombinations();

    useEffect(() => {
        trigger({
            developerIds,
            streamIds
        })
    }, [developerIds, streamIds, trigger])

    return (
        <>
            {isError &&
                <p>failed to load combinations...</p>
            }
            {isLoading &&
                <p>calculating combinations...</p>
            }
            {combinations && <LoadedMode combinations={combinations} developerIds={developerIds} streamIds={streamIds}
                                         updateForm={updateForm}/>}
        </>
    )
}

interface LoadedModeProps extends ChooseCombinationFormProps {
    combinations: ScoredCombinationDto[];
}

function LoadedMode({combinations, developerIds, streamIds, updateForm}: LoadedModeProps) {
    const [knownCombinations, setKnownCombinations] = useState<ScoredCombinationDto[][]>([combinations])
    const [selectedCombinationIndex, setSelectedCombinationIndex] = useState<CombinationIndex>()

    const {trigger} = usePostForSaveCombinationEvent()
    const router = useRouter()

    const nothingSelected = selectedCombinationIndex === undefined;

    function progressForm(direction: number) {
        updateForm(prevState => prevState + direction)
    }

    function getMoreCombinations() {
        setKnownCombinations(prevState => [...prevState, combinations]);
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
        const scoredCombination: ScoredCombinationDto = knownCombinations[index.row][index.column]
        const pairStreamsByIds: PairStreamByIds[] = scoredCombination.combination.map(c => ({
            streamId: c.stream.id,
            developerIds: c.developers.map(d => d.id)
        }))

        const data: SaveCombinationEventDto = {
            date: formatISO(new Date(), {representation: "date"}),
            combination: pairStreamsByIds
        }

        trigger(data)
            .then(_ => {
                router.push("/")
            })
    }

    return (
        <Stack gap={1}>
            <Typography variant="h4">Possible combinations</Typography>
            {knownCombinations.map((combinations, rowIndex) =>
                <ScoredCombinationsTable key={rowIndex} dtos={combinations}
                                         selectedIndex={getSelectedIndexForRow(rowIndex)}
                                         setSelectedIndex={(columnIndex: number) => setSelectedCombinationIndex({
                                        column: columnIndex,
                                        row: rowIndex
                                    })}
                />
            )}
            <Divider/>
            <ButtonRow>
                <Button variant="outlined" onClick={() => progressForm(-1)}>
                    <ArrowBack sx={({marginRight: (theme) => theme.spacing(1)})}/>
                    Back
                </Button>
                <Button variant="outlined" onClick={getMoreCombinations}>
                    <ArrowDownward sx={({marginRight: (theme) => theme.spacing(1)})}/>
                    More
                </Button>

                <SaveButton disabled={nothingSelected} onClick={saveCombination}/>
            </ButtonRow>
        </Stack>
    );
}