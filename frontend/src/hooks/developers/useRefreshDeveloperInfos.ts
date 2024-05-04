import {useSWRConfig} from "swr";
import DeveloperInfoDto from "@/domain/DeveloperInfoDto";
import {DEVELOPER_INFO_PATH, getDeveloperInfos} from "@/infrastructure/DeveloperClient";

export default function useRefreshDeveloperInfos() {
    const {mutate} = useSWRConfig()

    const configuredMutation = (potentialNewDeveloperInfo: DeveloperInfoDto) =>
        mutate(DEVELOPER_INFO_PATH, () => getDeveloperInfos(DEVELOPER_INFO_PATH), {
            optimisticData: (existingDeveloperInfos) => [...existingDeveloperInfos, potentialNewDeveloperInfo],
            rollbackOnError: true
        })

    return {refresh: configuredMutation};
}