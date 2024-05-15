import ScoredCombinationDto from "@/domain/ScoredCombinationDto";
import {Button, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Typography} from "@mui/material";
import {Fragment} from "react";


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

    function highlightCell(index: number) {
        const highlightedCellColor = "rgba(25, 118, 210, 0.1)"

        return selectedIndex === index ? {backgroundColor: highlightedCellColor} : {};
    }

    const h6 = (text: string) => <Typography variant="h6" component="text">{text}</Typography>;

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
                                    {dto.combination[pairIndex].developers.length === 2 &&
                                        <Fragment>
                                            <TableCell sx={highlightCell(index)}
                                                       align="center">{h6(dto.combination[pairIndex].developers[0].displayName)}</TableCell>
                                            <TableCell sx={highlightCell(index)}
                                                       align="center">{h6(dto.combination[pairIndex].developers[1].displayName)}</TableCell>
                                        </Fragment>
                                    }
                                    {dto.combination[pairIndex].developers.length === 1 &&
                                        <TableCell
                                            align="center" sx={highlightCell(index)}
                                            colSpan={2}>{h6(dto.combination[pairIndex].developers[0].displayName)}</TableCell>
                                    }
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