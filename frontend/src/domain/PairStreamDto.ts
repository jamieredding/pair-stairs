import DeveloperInfoDto from "@/domain/DeveloperInfoDto";
import StreamInfoDto from "@/domain/StreamInfoDto";

export default interface PairStreamDto {
    developers: DeveloperInfoDto[];
    stream: StreamInfoDto;
}