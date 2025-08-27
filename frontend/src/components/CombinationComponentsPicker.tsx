import {type Dispatch, type SetStateAction, useEffect} from "react";
import useDeveloperInfos from "../hooks/developers/useDeveloperInfos";
import {Divider, Stack, Typography} from "@mui/material";
import Loading from "./Loading";
import Error from "./Error";
import IdCheckboxGroup from "./IdCheckboxGroup.tsx";
import useStreamInfos from "../hooks/streams/useStreamInfos.ts";

interface CombinationComponentsPickerProps {
    savedDeveloperIds: number[],
    setSavedDeveloperIds: Dispatch<SetStateAction<number[]>>,

    savedStreamIds: number[],
    setSavedStreamIds: Dispatch<SetStateAction<number[]>>,
}

export default function CombinationComponentsPicker(
    {
        savedDeveloperIds, setSavedDeveloperIds,
        savedStreamIds, setSavedStreamIds
    }: CombinationComponentsPickerProps
) {
    return <Stack gap={4} direction="row" justifyContent="flex-start">
            <DeveloperPicker savedDeveloperIds={savedDeveloperIds} setSavedDeveloperIds={setSavedDeveloperIds}/>
        <Divider orientation="vertical" flexItem/>
            <StreamPicker savedStreamIds={savedStreamIds} setSavedStreamIds={setSavedStreamIds}/>
    </Stack>
}

interface DeveloperPickerProps {
    savedDeveloperIds: number[],
    setSavedDeveloperIds: Dispatch<SetStateAction<number[]>>,
}

function DeveloperPicker({
                             savedDeveloperIds,
                             setSavedDeveloperIds,
                         }: DeveloperPickerProps) {
    const {allDevelopers, isError, isLoading} = useDeveloperInfos();

    useEffect(() => {
        if (allDevelopers) {
            setSavedDeveloperIds(() => allDevelopers.map(dev => dev.id))
        }
    }, [allDevelopers, setSavedDeveloperIds]);

    return (
        <Stack gap={1}>
            <Typography variant="h6">Developers</Typography>
            {isLoading && <Loading/>}
            {isError && <Error/>}
            {allDevelopers &&
                <IdCheckboxGroup allItems={allDevelopers}
                                 selectedIds={savedDeveloperIds}
                                 setSelectedIds={setSavedDeveloperIds}/>
            }
        </Stack>
    )
}

interface StreamPickerProps {
    savedStreamIds: number[],
    setSavedStreamIds: Dispatch<SetStateAction<number[]>>,
}

function StreamPicker({
                             savedStreamIds,
                             setSavedStreamIds,
                         }: StreamPickerProps) {
    const {allStreams, isError, isLoading} = useStreamInfos();

    useEffect(() => {
        if (allStreams) {
            setSavedStreamIds(() => allStreams.map(dev => dev.id))
        }
    }, [allStreams, setSavedStreamIds]);

    return (
        <Stack gap={1}>
            <Typography variant="h6">Streams</Typography>
            {isLoading && <Loading/>}
            {isError && <Error/>}
            {allStreams &&
                <IdCheckboxGroup allItems={allStreams}
                                 selectedIds={savedStreamIds}
                                 setSelectedIds={setSavedStreamIds}/>
            }
        </Stack>
    )
}