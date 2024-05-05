import StreamDto from "@/domain/StreamDto";
import StreamInfoDto from "@/domain/StreamInfoDto";

export const STREAM_INFO_PATH = "/api/v1/streams/info"

export async function getStreamInfos(url: string): Promise<StreamInfoDto[]> {
    const res = await fetch(url);
    return await res.json();
}

export function addStream(url: string, {arg}: { arg: StreamDto }) {
    return fetch(url, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(arg)
    })
    // todo response handling
}
