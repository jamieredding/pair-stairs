import ScoredCombinationDto from "@/domain/ScoredCombinationDto";
import {
    Box,
    Button,
    SxProps,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    Typography
} from "@mui/material";
import {Fragment} from "react";
import useCombinationEvents from "@/hooks/combinations/useCombinationEvents";


interface ScoredCombinationsTableProps {
    key: number
    dtos: ScoredCombinationDto[],
    selectedIndex?: number,
    setSelectedIndex: (columnIndex: number) => void,
}

export default function ScoredCombinationsTable({
                                                    dtos,
                                                    selectedIndex,
                                                    setSelectedIndex
                                                }: ScoredCombinationsTableProps) {
    const numberOfPairs = dtos[0].combination.length
    const {combinationEvents} = useCombinationEvents()
    const mostRecentCombinationEvent = combinationEvents && combinationEvents.length > 0
        ? combinationEvents[0][0]
        : null

    function highlightCell(index: number) {
        const highlightedCellColor = "rgba(25, 118, 210, 0.1)"

        return selectedIndex === index ? {backgroundColor: highlightedCellColor} : {};
    }

    const h6 = (text: string) => <Typography variant="h6" component="p">{text}</Typography>;

    function isStayingInStream(developerId: number, streamId: number) {
        return mostRecentCombinationEvent?.combination.some(pair =>
            pair.stream.id === streamId &&
            pair.developers.some(developer => developer.id === developerId)
        )
    }

    function maybeMarkNew(developerId: number, streamId: number): SxProps {
        return isStayingInStream(developerId, streamId) ? {} : {
            '&::after': {content: '"*"'},
            display: "flex", // allow the asterisk to be right of the name
            justifyContent: "center" // push the name back to the center
        }
    }

    return (
        <TableContainer>
            <Table>
                <TableHead>
                    <TableRow>
                        <TableCell colSpan={1}/>
                        {dtos.map((_, index) =>
                            <TableCell key={index} align="center" colSpan={2} sx={highlightCell(index)}
                            >{h6(String(index + 1))}</TableCell>
                        )}
                    </TableRow>
                </TableHead>
                <TableBody>
                    {Array.from({length: numberOfPairs}, (_, pairIndex) => (
                        <TableRow key={dtos[0].combination[pairIndex].stream.displayName}>
                            <TableCell colSpan={1}>{h6(dtos[0].combination[pairIndex].stream.displayName)}</TableCell>
                            {dtos.map((dto, index) =>
                                <Fragment key={index}>
                                    {dto.combination[pairIndex].developers.map(developer =>
                                        <TableCell key={developer.id} align="center"
                                                   colSpan={2 / dto.combination[pairIndex].developers.length} // either 1 or 2
                                                   sx={highlightCell(index)}>
                                            {<Box
                                                sx={{...maybeMarkNew(developer.id, dto.combination[pairIndex].stream.id)}}>
                                                {h6(developer.displayName)}
                                            </Box>}
                                        </TableCell>
                                    )}
                                </Fragment>
                            )}
                        </TableRow>
                    ))}
                    <TableRow>
                        <TableCell>{h6("Score")}</TableCell>
                        {dtos.map((dto, index) =>
                            <TableCell key={index} align="center" colSpan={2}
                                       sx={highlightCell(index)}>{h6(String(dto.score))}</TableCell>
                        )}
                    </TableRow>
                    <TableRow>
                        <TableCell></TableCell>
                        {dtos.map((dto, index) =>
                            <TableCell key={index} align="center" colSpan={2} sx={highlightCell(index)}>
                                <Button variant="outlined" onClick={() => setSelectedIndex(index)}>Choose</Button>
                            </TableCell>
                        )}
                    </TableRow>
                </TableBody>
            </Table>
        </TableContainer>
    )
}