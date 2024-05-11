import {Button, Divider, Stack, Typography} from "@mui/material";
import IdToggleButtonGroup from "@/components/IdToggleButtonGroup";
import {useEffect, useState} from "react";
import {ArrowBack, List} from "@mui/icons-material";
import ButtonRow from "@/components/ButtonRow";

import useStreamInfos from "@/hooks/streams/useStreamInfos";
import Loading from "@/components/Loading";
import Error from "@/components/Error";

interface ChooseStreamsStepProps {
    savedStreamIds?: number[],
    setSavedStreamIds: (newStreamIds: number[]) => any,
    updateForm: (value: ((prevState: number) => number)) => void
}

export default function ChooseStreamsStep({savedStreamIds, setSavedStreamIds, updateForm}: ChooseStreamsStepProps) {
    const {allStreams, isError, isLoading} = useStreamInfos();
    const [selectedStreamIds, setSelectedStreamIds] = useState<number[]>(() => savedStreamIds || []);
    const dataLoaded = allStreams !== undefined;

    useEffect(() => {
        if (allStreams) {
            setSelectedStreamIds(allStreams.map(dev => dev.id))
        }
    }, [allStreams]);

    function progressForm(direction: number) {
        setSavedStreamIds(selectedStreamIds);
        updateForm(prevState => prevState + direction)
    }

    return (
        <Stack gap={1}>
            <Typography variant="h5">What streams will be worked on?</Typography>
            {isLoading && <Loading/>}
            {isError && <Error/>}
            {allStreams &&
                <IdToggleButtonGroup allItems={allStreams} selectedIds={selectedStreamIds}
                                     setSelectedIds={setSelectedStreamIds}/>
            }
            <Divider/>
            <ButtonRow>
                <Button variant="outlined" onClick={() => progressForm(-1)}>
                    <ArrowBack sx={({marginRight: (theme) => theme.spacing(1)})}/>
                    Back
                </Button>

                <Button variant="contained" onClick={() => progressForm(1)}
                        disabled={!dataLoaded}>
                    See combinations
                    <List sx={({marginLeft: (theme) => theme.spacing(1)})}/>
                </Button>
            </ButtonRow>
        </Stack>
    );
}