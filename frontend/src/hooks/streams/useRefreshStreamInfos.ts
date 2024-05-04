import {useSWRConfig} from "swr";
import StreamInfoDto from "@/domain/StreamInfoDto";
import {getStreamInfos, STREAM_INFO_PATH} from "@/infrastructure/StreamClient";

export default function useRefreshStreamInfos() {
    const {mutate} = useSWRConfig()

    const configuredMutation = (potentialNewStreamInfo: StreamInfoDto) =>
        mutate(STREAM_INFO_PATH, () => getStreamInfos(STREAM_INFO_PATH), {
            optimisticData: (existingStreamInfos) => [...existingStreamInfos, potentialNewStreamInfo],
            rollbackOnError: true
        })

    return {refresh: configuredMutation};
}