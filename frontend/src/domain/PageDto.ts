export default interface PageDto<T> {
    metadata: Metadata;
    data: T[]
}

export interface Metadata {
    nextPageNumber?: number
}