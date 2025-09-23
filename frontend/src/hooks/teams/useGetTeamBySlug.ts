import useSWR from "swr";
import {getTeamBySlug} from "../../infrastructure/TeamClient.ts";

export default function useGetTeamBySlug(slug: string) {
    const {data, error, isLoading} = useSWR(`/api/v1/teams/${slug}`, getTeamBySlug);

    return {
        teamDto: data,
        isLoading,
        isError: error
    }
}