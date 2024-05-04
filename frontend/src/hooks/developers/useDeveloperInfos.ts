import useSWR from "swr";
import {DEVELOPER_INFO_PATH, getDeveloperInfos} from "@/infrastructure/DeveloperClient";

export default function useDeveloperInfos() {
    const {data, error, isLoading} = useSWR(DEVELOPER_INFO_PATH, getDeveloperInfos)

    return {
        allDevelopers: data,
        isLoading,
        isError: error
    }
}