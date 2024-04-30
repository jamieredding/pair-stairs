import PairStreamDto from "@/app/domain/PairStreamDto";

export default interface ScoredCombinationDto {
    score: number;
    combination: PairStreamDto[]
}