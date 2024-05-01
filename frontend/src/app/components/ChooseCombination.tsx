import {Button, Divider, Stack, Typography} from "@mui/material";
import {Add, ArrowBack, ArrowDownward} from "@mui/icons-material";
import ScoredCombinationDto from "@/app/domain/ScoredCombinationDto";
import ScoredCombinations from "@/app/components/ScoredCombinations";
import {useEffect, useState} from "react";
import AddNewCombination from "@/app/components/AddNewCombination";
import SaveButton from "@/app/components/SaveButton";
import ButtonRow from "@/app/components/ButtonRow";
import {usePostForCalculateCombinations} from "@/app/infrastructure/CombinationClient";

interface ChooseCombinationProps {
    developerIds: number[],
    streamIds: number[],
    updateForm: (value: ((prevState: number) => number)) => void
}

interface CombinationIndex {
    row: number;
    column: number;
}

export default function ChooseCombination({developerIds, streamIds, updateForm}: ChooseCombinationProps) {
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

interface LoadedModeProps extends ChooseCombinationProps {
    combinations: ScoredCombinationDto[];
}

function LoadedMode({combinations, developerIds, streamIds, updateForm}: LoadedModeProps) {
    const [knownCombinations, setKnownCombinations] = useState<ScoredCombinationDto[][]>([combinations])
    const [selectedCombinationIndex, setSelectedCombinationIndex] = useState<CombinationIndex>()
    const [addingCombination, setAddingCombination] = useState<boolean>(false)

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

    return (
        <Stack gap={1}>
            <Typography variant="h4">Possible combinations</Typography>
            {knownCombinations.map((combinations, rowIndex) =>
                <ScoredCombinations key={rowIndex} dtos={combinations}
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
                <Button variant="outlined" onClick={() => setAddingCombination(!addingCombination)}>
                    <Add sx={({marginRight: (theme) => theme.spacing(1)})}/>
                    Manual combination
                </Button>
                <Button variant="outlined" onClick={getMoreCombinations}>
                    <ArrowDownward sx={({marginRight: (theme) => theme.spacing(1)})}/>
                    More
                </Button>

                <SaveButton disabled={nothingSelected}/>
            </ButtonRow>

            {addingCombination &&
                <Stack gap={1}>
                    <Divider/>
                    <AddNewCombination developerIds={developerIds} streamIds={streamIds}/>
                </Stack>
            }
        </Stack>
    );
}