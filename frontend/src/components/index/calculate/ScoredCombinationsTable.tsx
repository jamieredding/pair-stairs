import {
    Box,
    Button,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    Tooltip,
    Typography
} from "@mui/material";
import {Fragment} from "react";
import type ScoredCombinationDto from "../../../domain/ScoredCombinationDto";
import useCombinationEvents from "../../../hooks/combinations/useCombinationEvents.ts";


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

    function isStayingInStream(developerId: number, streamId: number): boolean {
        return mostRecentCombinationEvent?.combination.some(pair =>
            pair.stream.id === streamId &&
            pair.developers.some(developer => developer.id === developerId)
        ) || false
    }

    return (
        <TableContainer>
            <Table>
                <caption>* new to stream</caption>
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
                                            {<DeveloperCell
                                                isStayingInStream={isStayingInStream(developer.id, dto.combination[pairIndex].stream.id)}
                                                displayName={developer.displayName}>
                                            </DeveloperCell>}
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
                        {dtos.map((_dto, index) =>
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

const h6 = (text: string) => <Typography variant="h6" component="p">{text}</Typography>;

interface DeveloperCellProps {
    displayName: string,
    isStayingInStream: boolean,
}

function DeveloperCell({
                           displayName,
                           isStayingInStream,
                       }: DeveloperCellProps) {
    const style = isStayingInStream ? {} : {
        '&::after': {content: '"*"'},
        display: "flex", // allow the asterisk to be right of the name
        justifyContent: "center" // push the name back to the center
    }
    const content = <Tooltip title={isStayingInStream ? "Staying in stream" : "New to stream"}>{
        h6(displayName)
    }</Tooltip>

    return (<Box sx={style}>{content}</Box>)
}