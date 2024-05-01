import useSWR, {useSWRConfig} from "swr";
import useSWRMutation from "swr/mutation";
import StreamDto from "@/app/domain/StreamDto";
import StreamInfoDto from "@/app/domain/StreamInfoDto";

const STREAM_INFO_PATH = "/api/v1/streams/info"

export function useGetStreamInfos() {
    const {data, error, isLoading} = useSWR(STREAM_INFO_PATH, getStreamInfos)

    return {
        allStreams: data,
        isLoading,
        isError: error
    }
}

export function useRefreshGetStreamInfo() {
    const {mutate} = useSWRConfig()

    const configuredMutation = (potentialNewStreamInfo: StreamInfoDto) =>
        mutate(STREAM_INFO_PATH, () => getStreamInfos(STREAM_INFO_PATH), {
            optimisticData: (existingStreamInfos) => [...existingStreamInfos, potentialNewStreamInfo],
            rollbackOnError: true
        })

    return {refresh: configuredMutation};
}

export function useAddStream() {
    const {data, trigger, isMutating} = useSWRMutation("/api/v1/streams", addStream);

    return {trigger}
}

async function getStreamInfos(url: string): Promise<StreamInfoDto[]> {
    const res = await fetch(url);
    return await res.json();
}

function addStream(url: string, {arg}: { arg: StreamDto }) {
    return fetch(url, {method: "POST", body: JSON.stringify(arg)})
    // todo response handling
}
