"use client"

import {Button, Divider, Stack, Typography} from "@mui/material";
import IdToggleButtonGroup from "@/components/IdToggleButtonGroup";
import {useEffect, useState} from "react";
import DeveloperInfoDto from "@/domain/DeveloperInfoDto";
import StreamInfoDto from "@/domain/StreamInfoDto";
import PairStreamDto from "@/domain/PairStreamDto";
import {Add} from "@mui/icons-material";
import SaveButton from "@/components/SaveButton";
import ButtonRow from "@/components/ButtonRow";
import CombinationTable from "@/components/CombinationTable";
import {format} from "date-fns";
import CustomDatePicker from "@/components/CustomDatePicker";
import SaveCombinationEventDto, {PairStreamByIds} from "@/domain/SaveCombinationEventDto";
import useAddCombinationEvent from "@/hooks/combinations/useAddCombinationEvent";
import useDeveloperInfos from "@/hooks/developers/useDeveloperInfos";
import useStreamInfos from "@/hooks/streams/useStreamInfos";
import Loading from "@/components/Loading";
import Error from "@/components/Error";
import useRefreshCombinationEvents from "@/hooks/combinations/useRefreshCombinationEvents";

const dateFormat = "yyyy-MM-dd"

export default function ManualCombinationForm() {
    const {allDevelopers, isLoading: loadingDevelopers, isError: erroringDevelopers} = useDeveloperInfos();
    const {allStreams, isLoading: loadingStreams, isError: erroringStreams} = useStreamInfos()

    const today = format(new Date(), dateFormat)
    const [date, setDate] = useState<string | null>(today)

    const [remainingDevelopers, setRemainingDevelopers] = useState<DeveloperInfoDto[]>();
    useEffect(() => {
        if (allDevelopers) {
            setRemainingDevelopers(allDevelopers)
        }
    }, [allDevelopers]);

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

    const dataLoaded = allDevelopers && allStreams;
    const validPairStreamSelected: boolean = selectedDeveloperIds.length >= 1 && selectedStreamIds.length === 1
    const somePairsInCombination: boolean = combination.length > 0
    const validForm: boolean = somePairsInCombination && date !== null

    function addToCombination() {
        setCombination(prevState => {
            const newPairStream: PairStreamDto = {
                developers: (allDevelopers as DeveloperInfoDto[]).filter(dev => selectedDeveloperIds.includes(dev.id)),

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
        setRemainingDevelopers(allDevelopers)
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
            .then(async _ => {
                await refreshCombinationEvents()
                resetForm()
            })
    }

    return (
        <Stack gap={1}>
            <CustomDatePicker label="Date of combination" value={date} setValue={setDate} dateFormat={dateFormat}/>
            <Divider/>
            <Typography variant="h5">Developers</Typography>
            {loadingDevelopers && <Loading/>}
            {erroringDevelopers && <Error/>}
            {remainingDevelopers && remainingDevelopers.length > 0
                ? <IdToggleButtonGroup allItems={remainingDevelopers} selectedIds={selectedDeveloperIds}
                                       setSelectedIds={setSelectedDeveloperIds} maxSelectable={2}/>
                : <Typography variant="body1">No developers left to pick</Typography>
            }
            <Divider/>
            <Typography variant="h5">Streams</Typography>
            {loadingStreams && <Loading/>}
            {erroringStreams && <Error/>}
            {remainingStreams && remainingStreams.length > 0
                ? <IdToggleButtonGroup allItems={remainingStreams} selectedIds={selectedStreamIds}
                                     setSelectedIds={setSelectedStreamIds} maxSelectable={1}/>
                : <Typography variant="body1">No streams left to pick</Typography>
            }
            <Divider/>
            <ButtonRow>
                <Button variant="contained" disabled={!dataLoaded || !validPairStreamSelected}
                        onClick={addToCombination}>
                    <Add sx={({marginRight: (theme) => theme.spacing(1)})}/>
                    Add
                </Button>
                <SaveButton disabled={!dataLoaded || !validForm} onClick={saveCombination}/>
            </ButtonRow>
            <CombinationTable combination={combination}
                              removeFromCombination={removeFromCombination}/>
        </Stack>
    )
}