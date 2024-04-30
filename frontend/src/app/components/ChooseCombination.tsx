import {Button, Divider, Stack, Typography} from "@mui/material";
import {ArrowBack, Save} from "@mui/icons-material";
import ScoredCombinationDto from "@/app/domain/ScoredCombinationDto";
import ScoredCombinations from "@/app/components/ScoredCombinations";

interface ChooseCombinationProps {
    developerIds: number[],
    streamIds: number[],
    updateForm: (value: (((prevState: number) => number) | number)) => void
}

const combinations : ScoredCombinationDto[] = [
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

export default function ChooseCombination({developerIds, streamIds, updateForm}: ChooseCombinationProps) {
    function progressForm(direction: number) {
        updateForm(prevState => prevState + direction)
    }

    return (
        <Stack gap={1}>
            <Typography variant="h4">Possible combinations</Typography>
            <ScoredCombinations dtos={combinations}/>
            <Divider/>
            <Stack direction="row" gap={1}>
                <Button variant="outlined" onClick={() => progressForm(-1)}>
                    <ArrowBack sx={({marginRight: (theme) => theme.spacing(1)})}/>
                    Back
                </Button>

                <Button variant="contained">
                    Save
                    <Save sx={({marginLeft: (theme) => theme.spacing(1)})}/>
                </Button>
            </Stack>
        </Stack>
    );

}