import {DatePicker} from "@mui/x-date-pickers";
import {format, parse} from "date-fns";

interface CustomDatePickerProps {
    label: string,
    value: string | null,
    setValue: (value: (string | null)) => void,
    dateFormat: string
}

export default function CustomDatePicker({value, setValue, dateFormat, label}: CustomDatePickerProps) {
    return (
        <DatePicker label={label} format={dateFormat}
                    value={value ? parse(value, dateFormat, new Date()) : null}
                    onChange={(newValue, context) => {
                        if (!context.validationError && newValue !== null) {
                            setValue(format(newValue, dateFormat))
                        } else {
                            setValue(null)
                        }
                    }}
                    slotProps={{
                        textField: {
                            helperText: value ? "" : "You need to enter a valid date"
                        }
                    }}
        />
    )
}