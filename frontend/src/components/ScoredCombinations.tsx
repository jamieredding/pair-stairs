import ScoredCombinationDto from "@/domain/ScoredCombinationDto";
import {Button, Table, TableBody, TableCell, TableContainer, TableHead, TableRow} from "@mui/material";
import {Fragment} from "react";


interface ScoredCombinationsProps {
    dtos: ScoredCombinationDto[],
    selectedIndex?: number,
    setSelectedIndex: (columnIndex: number) => void
}

export default function ScoredCombinations({dtos, selectedIndex, setSelectedIndex}: ScoredCombinationsProps) {
    const numberOfPairs = dtos[0].combination.length

    function highlightCell(index: number) {
        const highlightedCellColor = "rgba(25, 118, 210, 0.1)"

        return selectedIndex === index ? {backgroundColor: highlightedCellColor} : {};
    }

    return (
        <TableContainer>
            <Table>
                <TableHead>
                    <TableRow>
                        <TableCell colSpan={1}/>
                        {dtos.map((_, index) =>
                            <TableCell key={index} align="center" colSpan={2} sx={highlightCell(index)}
                            >{index + 1}</TableCell>
                        )}
                    </TableRow>
                </TableHead>
                <TableBody>
                    {Array.from({length: numberOfPairs}, (_, pairIndex) => (
                        <TableRow key={dtos[0].combination[pairIndex].stream.displayName}>
                            <TableCell colSpan={1}>{dtos[0].combination[pairIndex].stream.displayName}</TableCell>
                            {dtos.map((dto, index) =>
                                <Fragment key={index}>
                                    {dto.combination[pairIndex].developers.length === 2 &&
                                        <Fragment>
                                            <TableCell sx={highlightCell(index)}
                                                align="center">{dto.combination[pairIndex].developers[0].displayName}</TableCell>
                                            <TableCell sx={highlightCell(index)}
                                                align="center">{dto.combination[pairIndex].developers[1].displayName}</TableCell>
                                        </Fragment>
                                    }
                                    {dto.combination[pairIndex].developers.length === 1 &&
                                        <TableCell
                                            align="center" sx={highlightCell(index)}
                                            colSpan={2}>{dto.combination[pairIndex].developers[0].displayName}</TableCell>
                                    }
                                </Fragment>
                            )}
                        </TableRow>
                    ))}
                    <TableRow>
                        <TableCell>Score</TableCell>
                        {dtos.map((dto, index) =>
                            <TableCell key={index} align="center" colSpan={2} sx={highlightCell(index)}>{dto.score}</TableCell>
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