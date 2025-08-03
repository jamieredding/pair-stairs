import type DeveloperInfoDto from "../../domain/DeveloperInfoDto.ts";
import {DEVELOPER_INFO_PATH, getDeveloperInfos} from "../../infrastructure/DeveloperClient.ts";
import useSWR, {type SWRResponse} from "swr";

export default function useDeveloperInfos() {
    const {data, error, isLoading}: SWRResponse<DeveloperInfoDto[]> =
        useSWR(DEVELOPER_INFO_PATH, getDeveloperInfos)

    return {
        allDevelopers: data,
        isLoading,
        isError: error
    }
}