import {Button, Divider, Stack, Typography} from "@mui/material";
import IdToggleButtonGroup from "@/components/IdToggleButtonGroup";
import {useState} from "react";
import DeveloperInfoDto from "@/domain/DeveloperInfoDto";
import StreamInfoDto from "@/domain/StreamInfoDto";
import PairStreamDto from "@/domain/PairStreamDto";
import {Add} from "@mui/icons-material";
import SaveButton from "@/components/SaveButton";
import ButtonRow from "@/components/ButtonRow";
import ManualSelectionTable from "@/components/ManualSelectionTable";
import {DatePicker} from "@mui/x-date-pickers";
import {format, parse} from "date-fns";

// todo remove this
const developers: DeveloperInfoDto[] = [
    {id: 1, displayName: "dev-1"},
    {id: 2, displayName: "dev-2"},
    {id: 3, displayName: "dev-3"},
    {id: 4, displayName: "dev-4"},
    {id: 5, displayName: "dev-5"},
    {id: 6, displayName: "dev-6"},
    {id: 7, displayName: "Jamie with a long long name!"},
]

// todo remove this
const streams: StreamInfoDto[] = [
    {id: 1, displayName: "stream-a"},
    {id: 2, displayName: "stream-b"},
    {id: 3, displayName: "stream-c"},
]

const dateFormat = "yyyy-MM-dd"

interface AddNewCombinationProps {
    developerIds: number[],
    streamIds: number[]
}

export default function AddNewCombination({developerIds, streamIds}: AddNewCombinationProps) {
    const today = format(new Date(), dateFormat)
    const [date, setDate] = useState<string | null>(today)

    const allPossibleDevelopers = developers.filter(dev => developerIds.includes(dev.id));
    const [remainingDevelopers, setRemainingDevelopers] = useState<DeveloperInfoDto[]>(allPossibleDevelopers);
    const [selectedDeveloperIds, setSelectedDeveloperIds] = useState<number[]>([])

    const allPossibleStreams = streams.filter(stream => streamIds.includes(stream.id));
    const [remainingStreams, setRemainingStreams] = useState<StreamInfoDto[]>(allPossibleStreams);
    const [selectedStreamIds, setSelectedStreamIds] = useState<number[]>([])

    const [combination, setCombination] = useState<PairStreamDto[]>([])

    const hasNoPairsToSave: boolean = combination.length === 0
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
            <DatePicker label="Date of combination" format={dateFormat}
                        value={date ? parse(date, dateFormat, new Date()) : null}
                        onChange={(newValue, context) => {
                            if (!context.validationError && newValue !== null) {
                                setDate(format(newValue, dateFormat))
                            } else {
                                setDate(null)
                            }
                        }}
                        slotProps={{
                            textField: {
                                helperText: date ? "" : "You need to enter a date"
                            }
                        }}
            />
            <Divider/>
            <Typography variant="h5">Developers</Typography>
            <IdToggleButtonGroup allItems={remainingDevelopers} selectedIds={selectedDeveloperIds}
                                 setSelectedIds={setSelectedDeveloperIds} maxSelectable={2}/>
            <Divider/>
            <Typography variant="h5">Streams</Typography>
            <IdToggleButtonGroup allItems={remainingStreams} selectedIds={selectedStreamIds}
                                 setSelectedIds={setSelectedStreamIds} maxSelectable={1}/>
            <Divider/>
            <ButtonRow>
                <Button variant="contained" disabled={!validPairStreamSelected}
                        onClick={addToCombination}>
                    <Add sx={({marginRight: (theme) => theme.spacing(1)})}/>
                    Add
                </Button>
                <SaveButton disabled={hasNoPairsToSave}/>
            </ButtonRow>
            <ManualSelectionTable combination={combination}
                                  removeFromCombination={removeFromCombination}/>
        </Stack>
    )
}