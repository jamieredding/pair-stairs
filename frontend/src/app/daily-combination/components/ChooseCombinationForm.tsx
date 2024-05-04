import {Button, Divider, Stack, Typography} from "@mui/material";
import {ArrowBack, ArrowDownward} from "@mui/icons-material";
import ScoredCombinationDto from "@/domain/ScoredCombinationDto";
import ScoredCombinationsTable from "@/app/daily-combination/components/ScoredCombinationsTable";
import {useState} from "react";
import SaveButton from "@/components/SaveButton";
import ButtonRow from "@/components/ButtonRow";
import SaveCombinationEventDto, {PairStreamByIds} from "@/domain/SaveCombinationEventDto";
import {formatISO} from "date-fns";
import {useRouter} from "next/navigation";
import useCalculateCombinations from "@/hooks/combinations/useCalculateCombinations";
import useAddCombinationEvent from "@/hooks/combinations/useAddCombinationEvent";
import Loading from "@/components/Loading";
import Error from "@/components/Error";

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
    const {
        combinationsPages,
        isError,
        isLoading,
        setSize
    } = useCalculateCombinations({developerIds, streamIds});
    const dataLoaded = combinationsPages !== undefined;

    const [selectedCombinationIndex, setSelectedCombinationIndex] = useState<CombinationIndex>()

    const {trigger: addCombinationEvent} = useAddCombinationEvent()
    const router = useRouter()

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
            .then(_ => {
                router.push("/")
            })
    }

    return (
        <Stack gap={1}>
            <Typography variant="h4">Possible combinations</Typography>
            {
                combinationsPages &&
                combinationsPages.map((combinations, rowIndex) =>
                    <ScoredCombinationsTable key={rowIndex} dtos={combinations}
                                             selectedIndex={getSelectedIndexForRow(rowIndex)}
                                             setSelectedIndex={(columnIndex: number) => setSelectedCombinationIndex({
                                                 column: columnIndex,
                                                 row: rowIndex
                                             })}
                    />
                )
            }
            {isLoading && <Loading/>}
            {isError && <Error/>}
            <Divider/>
            <ButtonRow>
                <Button variant="outlined" onClick={() => progressForm(-1)}>
                    <ArrowBack sx={({marginRight: (theme) => theme.spacing(1)})}/>
                    Back
                </Button>
                <Button variant="outlined" onClick={getMoreCombinations} disabled={!dataLoaded}>
                    <ArrowDownward sx={({marginRight: (theme) => theme.spacing(1)})}/>
                    More
                </Button>

                <SaveButton disabled={!dataLoaded || nothingSelected} onClick={saveCombination}/>
            </ButtonRow>
        </Stack>
    );
}