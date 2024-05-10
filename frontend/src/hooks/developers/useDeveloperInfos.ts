import useSWR, {SWRResponse} from "swr";
import {DEVELOPER_INFO_PATH, getDeveloperInfos} from "@/infrastructure/DeveloperClient";
import DeveloperInfoDto from "@/domain/DeveloperInfoDto";

export default function useDeveloperInfos() {
    const {data, error, isLoading}: SWRResponse<DeveloperInfoDto[], any> =
        useSWR(DEVELOPER_INFO_PATH, getDeveloperInfos)

    return {
        allDevelopers: data,
        isLoading,
        isError: error
    }
}