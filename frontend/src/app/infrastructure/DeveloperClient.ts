import useSWR from "swr";
import DeveloperInfoDto from "@/app/domain/DeveloperInfoDto";

export function useGetDeveloperInfos() {
    const {data, error, isLoading} = useSWR("/api/v1/developers/info", getDeveloperInfos)

    return {
        allDevelopers: data,
        isLoading,
        isError: error
    }
}

function getDeveloperInfos(url: string): Promise<DeveloperInfoDto[]> {
    return fetch(url)
        .then(res => res.json())
}
