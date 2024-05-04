import useSWR from "swr";
import {getStreamInfos, STREAM_INFO_PATH} from "@/infrastructure/StreamClient";

export default function useStreamInfos() {
    const {data, error, isLoading} = useSWR(STREAM_INFO_PATH, getStreamInfos)

    return {
        allStreams: data,
        isLoading,
        isError: error
    }
}