import {useSWRConfig} from "swr";
import type StreamInfoDto from "../../domain/StreamInfoDto.ts";
import {getStreamInfos, STREAM_INFO_PATH} from "../../infrastructure/StreamClient.ts";

export default function useRefreshStreamInfos() {
    const {mutate} = useSWRConfig()

    // todo error handling?
    const configuredMutation = (potentialNewStreamInfo: StreamInfoDto) =>
        mutate(STREAM_INFO_PATH, () => getStreamInfos(STREAM_INFO_PATH), {
            optimisticData: (existingStreamInfos) => [...existingStreamInfos, potentialNewStreamInfo],
            rollbackOnError: true
        })

    return {refresh: configuredMutation};
}