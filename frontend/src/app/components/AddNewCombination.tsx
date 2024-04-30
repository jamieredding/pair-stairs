import {Button, Divider, Stack} from "@mui/material";
import IdToggleButtonGroup from "@/app/components/IdToggleButtonGroup";
import {useState} from "react";
import DeveloperInfoDto from "@/app/domain/DeveloperInfoDto";
import StreamInfoDto from "@/app/domain/StreamInfoDto";
import PairStreamDto from "@/app/domain/PairStreamDto";
import {Add, Close, Save} from "@mui/icons-material";

const developers: DeveloperInfoDto[] = [
    {id: 1, displayName: "dev-1"},
    {id: 2, displayName: "dev-2"},
    {id: 3, displayName: "dev-3"},
    {id: 4, displayName: "dev-4"},
    {id: 5, displayName: "dev-5"},
    {id: 6, displayName: "dev-6"},
    {id: 7, displayName: "Jamie with a long long name!"},
]

const streams: StreamInfoDto[] = [
    {id: 1, displayName: "stream-a"},
    {id: 2, displayName: "stream-b"},
    {id: 3, displayName: "stream-c"},
]

interface AddNewCombinationProps {
    developerIds: number[],
    streamIds: number[]
}

export default function AddNewCombination({developerIds, streamIds}: AddNewCombinationProps) {
    const allPossibleDevelopers = developers.filter(dev => developerIds.includes(dev.id));
    const [remainingDevelopers, setRemainingDevelopers] = useState<DeveloperInfoDto[]>(allPossibleDevelopers);
    const [selectedDeveloperIds, setSelectedDeveloperIds] = useState<number[]>([])

    const allPossibleStreams = streams.filter(stream => streamIds.includes(stream.id));
    const [remainingStreams, setRemainingStreams] = useState<StreamInfoDto[]>(allPossibleStreams);
    const [selectedStreamIds, setSelectedStreamIds] = useState<number[]>([])

    const [combination, setCombination] = useState<PairStreamDto[]>([])

    const outstandingItems: boolean = remainingDevelopers.length !== 0 || remainingStreams.length !== 0
    const validPairStreamSelected: boolean = selectedDeveloperIds.length >= 1 && selectedStreamIds.length === 1

    function addToCombination() {
        setCombination(prevState => {
            const newPairStream: PairStreamDto = {
                developers: allPossibleDevelopers.filter(dev => selectedDeveloperIds.includes(dev.id)),

                stream: allPossibleStreams.filter(stream => stream.id === selectedStreamIds[0])[0],
            }

            return [...prevState, newPairStream];
        })
        setSelectedDeveloperIds([])
        setSelectedStreamIds([])

        setRemainingDevelopers(prevState => prevState.filter(dev => !selectedDeveloperIds.includes(dev.id)))
        setRemainingStreams(prevState => prevState.filter(stream => !selectedStreamIds.includes(stream.id)))

    }

    function removeFromCombination(toRemove: PairStreamDto) {
        setCombination(prevState => prevState.filter(pair => pair !== toRemove));

        setSelectedDeveloperIds([])
        setSelectedStreamIds([])

        setRemainingDevelopers(prevState => [...prevState, ...toRemove.developers])
        setRemainingStreams(prevState => [...prevState, toRemove.stream])
    }

    return (
        <Stack gap={1}>
            <div>Developers</div>
            <IdToggleButtonGroup allItems={remainingDevelopers} selectedIds={selectedDeveloperIds}
                                 setSelectedIds={setSelectedDeveloperIds} maxSelectable={2}/>
            <Divider/>
            <div>Streams</div>
            <IdToggleButtonGroup allItems={remainingStreams} selectedIds={selectedStreamIds}
                                 setSelectedIds={setSelectedStreamIds} maxSelectable={1}/>
            <Divider/>
            <Stack direction="row" gap={1}>

                <Button variant="contained" disabled={!validPairStreamSelected}
                        onClick={addToCombination}>
                    <Add sx={({marginRight: (theme) => theme.spacing(1)})}/>
                    Add
                </Button>

                <Button variant="contained" disabled={outstandingItems}>
                    Save
                    <Save sx={({marginLeft: (theme) => theme.spacing(1)})}/>
                </Button>
            </Stack>
            <div>Selected</div>
            {combination.length === 0 &&
                <p>Make some pairs above</p>
            }
            {combination.map(pair =>
                <div>
                    {pair.stream.displayName}, {pair.developers.map(dev => dev.displayName)}
                    <Button onClick={() => removeFromCombination(pair)}><Close/></Button>
                </div>
            )}
        </Stack>
    )
}