import {Checkbox, FormControl, FormControlLabel, FormGroup, FormLabel} from "@mui/material";
import {type Dispatch, type SetStateAction} from "react";
import type {Displayable} from "../domain/Displayable.tsx";
import {sorted} from "../utils/displayUtils.ts";

interface IdCheckboxGroupProps {
    allItems: Displayable[];
    selectedIds: number[];
    setSelectedIds: Dispatch<SetStateAction<number[]>>;
    maxSelectable?: number;
    disabled?: boolean;
}

export default function IdCheckboxGroup({
                                            allItems,
                                            selectedIds,
                                            setSelectedIds,
                                            maxSelectable,
                                            disabled = false
                                        }: IdCheckboxGroupProps) {
    const error = selectedIds.length == 0
    const allItemsSorted = sorted(allItems)

    function toggleItem(id: number) {
        if (selectedIds.includes(id)) {
            setSelectedIds(prev => prev.filter(other => other !== id))
        } else {
            if (maxSelectable) {
                if (selectedIds.length < maxSelectable) {
                    setSelectedIds(prev => [...prev, id]);
                } else if (maxSelectable === 1) { // usability to allow toggling between single options
                    setSelectedIds(() => [id])
                }
            } else {
                setSelectedIds(prev => [...prev, id]);
            }
        }
    }

    let legendText = ""

    if (maxSelectable) {
        if (maxSelectable == 1) {
            legendText = "Choose exactly 1"
        } else {
            legendText = `Choose 1 - ${maxSelectable}`
        }
    } else {
        legendText = "Choose 1 or more"
    }

    return (
        <FormControl disabled={disabled} error={!disabled && error}>
            <FormLabel component="legend">{legendText}</FormLabel>
            <FormGroup>
                {allItemsSorted.map(item =>
                    <FormControlLabel key={item.id}
                                      control={
                                          <Checkbox checked={selectedIds.includes(item.id)}
                                                    onChange={() => toggleItem(item.id)}/>
                                      }
                                      label={item.displayName}/>
                )}
            </FormGroup>
        </FormControl>
    )
}