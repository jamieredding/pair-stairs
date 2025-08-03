import type DeveloperInfoDto from "./DeveloperInfoDto.ts";
import type StreamInfoDto from "./StreamInfoDto.ts";

export default interface PairStreamDto {
    developers: DeveloperInfoDto[];
    stream: StreamInfoDto;
}