import type DeveloperInfoDto from "../../domain/DeveloperInfoDto.ts";
import {DEVELOPER_INFO_PATH, getDeveloperInfos} from "../../infrastructure/DeveloperClient.ts";
import useSWR, {type SWRResponse} from "swr";
import type {ApiError} from "../../domain/ApiError.ts";

export default function useDeveloperInfos() {
    const {data, error, isLoading}: SWRResponse<DeveloperInfoDto[], ApiError> =
        useSWR(DEVELOPER_INFO_PATH, getDeveloperInfos)

    return {
        allDevelopers: data,
        isLoading,
        isError: error
    }
}