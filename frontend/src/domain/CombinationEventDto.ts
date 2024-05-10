import PairStreamDto from "@/domain/PairStreamDto";

export default interface CombinationEventDto {
    id: number;
    date: string;
    combination: PairStreamDto[];
}