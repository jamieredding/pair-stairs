import DeveloperInfoDto from "@/app/domain/DeveloperInfoDto";
import StreamInfoDto from "@/app/domain/StreamInfoDto";

export default interface PairStreamDto {
    developers: DeveloperInfoDto[];
    stream: StreamInfoDto;
}