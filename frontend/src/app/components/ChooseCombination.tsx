import {Button, Divider, Stack, Typography} from "@mui/material";
import {Add, ArrowBack, ArrowDownward} from "@mui/icons-material";
import ScoredCombinationDto from "@/app/domain/ScoredCombinationDto";
import ScoredCombinations from "@/app/components/ScoredCombinations";
import {useState} from "react";
import AddNewCombination from "@/app/components/AddNewCombination";
import SaveButton from "@/app/components/SaveButton";
import ButtonRow from "@/app/components/ButtonRow";

interface ChooseCombinationProps {
    developerIds: number[],
    streamIds: number[],
    updateForm: (value: (((prevState: number) => number) | number)) => void
}

const combinations: ScoredCombinationDto[] = [
    {
        "score": 10,
        "combination": [
            {
                "developers": [
                    {
                        "id": 0,
                        "displayName": "dev-0"
                    },
                    {
                        "id": 1,
                        "displayName": "dev-1"
                    }
                ],
                "stream": {
                    "id": 0,
                    "displayName": "stream-a"
                }
            },
            {
                "developers": [
                    {
                        "id": 2,
                        "displayName": "dev-2"
                    }
                ],
                "stream": {
                    "id": 1,
                    "displayName": "stream-b"
                }
            }
        ]
    },
    {
        "score": 20,
        "combination": [
            {
                "developers": [
                    {
                        "id": 0,
                        "displayName": "dev-0"
                    },
                    {
                        "id": 2,
                        "displayName": "dev-2"
                    }
                ],
                "stream": {
                    "id": 0,
                    "displayName": "stream-a"
                }
            },
            {
                "developers": [
                    {
                        "id": 1,
                        "displayName": "dev-1"
                    }
                ],
                "stream": {
                    "id": 1,
                    "displayName": "stream-b"
                }
            }
        ]
    }
]

interface CombinationIndex {
    row: number;
    column: number;
}

export default function ChooseCombination({developerIds, streamIds, updateForm}: ChooseCombinationProps) {
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