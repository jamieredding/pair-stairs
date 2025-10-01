import {Button, Divider, Stack, Typography} from "@mui/material";
import {useEffect, useMemo, useState} from "react";
import {Add} from "@mui/icons-material";
import {format} from "date-fns";
import useDeveloperInfos from "../../../hooks/developers/useDeveloperInfos";
import useStreamInfos from "../../../hooks/streams/useStreamInfos";
import type DeveloperInfoDto from "../../../domain/DeveloperInfoDto";
import type StreamInfoDto from "../../../domain/StreamInfoDto";
import type PairStreamDto from "../../../domain/PairStreamDto";
import useAddCombinationEvent from "../../../hooks/combinations/useAddCombinationEvent";
import useRefreshCombinationEvents from "../../../hooks/combinations/useRefreshCombinationEvents";
import type {PairStreamByIds, SaveCombinationEventDto} from "../../../domain/SaveCombinationEventDto";
import CustomDatePicker from "../../CustomDatePicker";
import Loading from "../../Loading";
import Error from "../../Error";
import IdCheckboxGroup from "../../IdCheckboxGroup.tsx";
import ButtonRow from "../../ButtonRow.tsx";
import SaveButton from "../../SaveButton.tsx";
import CombinationTable from "../../CombinationTable.tsx";

const dateFormat = "yyyy-MM-dd"

export default function ManualCombinationForm() {
    const {allDevelopers, isLoading: loadingDevelopers, isError: erroringDevelopers} = useDeveloperInfos();
    const activeDevelopers = useMemo(
        () => allDevelopers?.filter(d => !d.archived),
        [allDevelopers]
    )

    const {allStreams, isLoading: loadingStreams, isError: erroringStreams} = useStreamInfos()

    const today = format(new Date(), dateFormat)
    const [date, setDate] = useState<string | null>(today)

    const [remainingDevelopers, setRemainingDevelopers] = useState<DeveloperInfoDto[]>();
    useEffect(() => {
        if (activeDevelopers) {
            setRemainingDevelopers(activeDevelopers)
        }
    }, [activeDevelopers]);

    const [remainingStreams, setRemainingStreams] = useState<StreamInfoDto[]>();
    useEffect(() => {
        if (allStreams) {
            setRemainingStreams(allStreams)
        }
    }, [allStreams]);

    const [selectedDeveloperIds, setSelectedDeveloperIds] = useState<number[]>([])
    const [selectedStreamIds, setSelectedStreamIds] = useState<number[]>([])

    const [combination, setCombination] = useState<PairStreamDto[]>([])

    const {trigger} = useAddCombinationEvent()
    const {refresh: refreshCombinationEvents} = useRefreshCombinationEvents();

    const dataLoaded = activeDevelopers && allStreams;
    const validPairStreamSelected: boolean = selectedDeveloperIds.length >= 1 && selectedStreamIds.length === 1
    const somePairsInCombination: boolean = combination.length > 0
    const validForm: boolean = somePairsInCombination && date !== null
    const saveButtonDisabled = !dataLoaded || !validForm;

    function addToCombination() {
        setCombination(prevState => {
            const newPairStream: PairStreamDto = {
                developers: (activeDevelopers as DeveloperInfoDto[]).filter(dev => selectedDeveloperIds.includes(dev.id)),

                stream: (allStreams as StreamInfoDto[]).filter(stream => stream.id === selectedStreamIds[0])[0],
            }

            return [...prevState, newPairStream];
        })
        setSelectedDeveloperIds([])
        setSelectedStreamIds([])

        setRemainingDevelopers(prevState => (prevState as DeveloperInfoDto[]).filter(dev => !selectedDeveloperIds.includes(dev.id)))
        setRemainingStreams(prevState => (prevState as StreamInfoDto[]).filter(stream => !selectedStreamIds.includes(stream.id)))

    }

    function removeFromCombination(toRemove: PairStreamDto) {
        setCombination(prevState => prevState.filter(pair => pair !== toRemove));

        setSelectedDeveloperIds([])
        setSelectedStreamIds([])

        setRemainingDevelopers(prevState => [...(prevState as DeveloperInfoDto[]), ...toRemove.developers])
        setRemainingStreams(prevState => [...(prevState as StreamInfoDto[]), toRemove.stream])
    }

    function resetForm() {
        setCombination([])
        setSelectedDeveloperIds([])
        setSelectedStreamIds([])
        setRemainingDevelopers(activeDevelopers)
        setRemainingStreams(allStreams)
        setDate(today)
    }

    function saveCombination() {
        const pairStreamsByIds: PairStreamByIds[] = combination.map(c => ({
            streamId: c.stream.id,
            developerIds: c.developers.map(d => d.id)
        }))

        const data: SaveCombinationEventDto = {
            date: date as string,
            combination: pairStreamsByIds
        }

        trigger(data)
            .then(async () => {
                await refreshCombinationEvents()
                resetForm()
            })
    }

    return (
        <Stack gap={1}>
            <CustomDatePicker label="Date of combination" value={date} setValue={setDate} dateFormat={dateFormat}/>
            <Divider/>
            <Typography variant="h5">Choose your combination by adding one pair at a time</Typography>
            <Stack gap={4} direction="row" justifyContent="flex-start">
                <Stack gap={1}>
                    <Typography variant="h6">Developers</Typography>
                    {loadingDevelopers && <Loading/>}
                    {erroringDevelopers && <Error/>}
                    {remainingDevelopers && remainingDevelopers.length > 0
                        ? <IdCheckboxGroup allItems={remainingDevelopers} selectedIds={selectedDeveloperIds}
                                           setSelectedIds={setSelectedDeveloperIds} maxSelectable={2}
                                           disabled={remainingStreams !== undefined && remainingStreams.length == 0}/>
                        : <Typography variant="body1">No developers left to pick</Typography>
                    }
                </Stack>
                <Divider orientation="vertical" flexItem/>
                <Stack gap={1}>
                    <Typography variant="h6">Streams</Typography>
                    {loadingStreams && <Loading/>}
                    {erroringStreams && <Error/>}
                    {remainingStreams && remainingStreams.length > 0
                        ? <IdCheckboxGroup allItems={remainingStreams} selectedIds={selectedStreamIds}
                                           setSelectedIds={setSelectedStreamIds} maxSelectable={1}
                                           disabled={remainingDevelopers !== undefined && remainingDevelopers.length == 0}/>
                        : <Typography variant="body1">No streams left to pick</Typography>
                    }
                </Stack>
            </Stack>
            <Divider/>
            <ButtonRow>
                <Button variant="contained" disabled={!dataLoaded || !validPairStreamSelected}
                        onClick={addToCombination}>
                    <Add sx={({marginRight: (theme) => theme.spacing(1)})}/>
                    Add
                </Button>
                <SaveButton disabled={saveButtonDisabled} onClick={saveCombination}/>
                {!saveButtonDisabled &&
                    <Typography variant="h6">Save when you have chosen your combination</Typography>}
            </ButtonRow>
            <CombinationTable combination={combination}
                              removeFromCombination={removeFromCombination}/>
        </Stack>
    )
}