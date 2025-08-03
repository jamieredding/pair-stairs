import type { SWRResponse } from "swr"
import type StreamInfoDto from "../../domain/StreamInfoDto"
import useSWR from "swr"
import {getStreamInfos, STREAM_INFO_PATH} from "../../infrastructure/StreamClient.ts";

export default function useStreamInfos() {
    const {data, error, isLoading}: SWRResponse<StreamInfoDto[]> =
        useSWR(STREAM_INFO_PATH, getStreamInfos)

    return {
        allStreams: data,
        isLoading,
        isError: error
    }
}