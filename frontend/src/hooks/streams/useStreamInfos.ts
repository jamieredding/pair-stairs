import useSWR, {SWRResponse} from "swr";
import {getStreamInfos, STREAM_INFO_PATH} from "@/infrastructure/StreamClient";
import StreamInfoDto from "@/domain/StreamInfoDto";

export default function useStreamInfos() {
    const {data, error, isLoading}: SWRResponse<StreamInfoDto[], any> =
        useSWR(STREAM_INFO_PATH, getStreamInfos)

    return {
        allStreams: data,
        isLoading,
        isError: error
    }
}