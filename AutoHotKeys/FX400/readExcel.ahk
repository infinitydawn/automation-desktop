#Persistent
SetBatchLines, -1

excelFilePath := ""
InputBox, excelFilePath, Enter Path To Excel, Please enter a path:, , 300, 150

excelFilePath := StrReplace(excelFilePath, """")

; Check if Excel file exists
if !FileExist(excelFilePath)
{
    MsgBox, The specified Excel file does not exist.
    ExitApp
}

; Create Excel COM object
try
{
    excel := ComObjCreate("Excel.Application")
}
catch e
{
    MsgBox, Failed to create Excel COM object. Error: %e%
    ExitApp
}

; Make Excel visible
excel.Visible := True

; Open the workbook and access the sheet
try
{
    workbook := excel.Workbooks.Open(excelFilePath)
    
    sheet := workbook.Sheets(1)
}
catch e
{
    MsgBox, Failed to open workbook or access sheet. Error: %e%
    excel.Quit()
    ExitApp
}

; Assume columns are to be read
columnLetters := ["A", "B", "C"]
data := []

; Use a more robust method to find the last row by checking each row for data
lastRow := 1
While (sheet.Range(columnLetters[1] . lastRow).Value != "")
{
    lastRow++
}
lastRow--

; Exit if the column is completely empty
If (lastRow = 0)
{
    MsgBox, % "Column" columnLetters[1] " is completely empty."
    workbook.Close(False)
    excel.Quit()
    ExitApp
}

; Read the columns into an array of objects
Loop, % lastRow
{
    rowIndex := A_Index
    RowObject := {}
    for each, col in columnLetters
    {
        cell := sheet.Range(col . rowIndex)  ; Use rowIndex in the Range method
        CellValue := cell.Text
        RowObject[col] := CellValue
    }
    data.Push(RowObject)
}


; Close Excel if you are done with it
workbook.Close(False)
excel.Quit()

; Cleanup COM objects
ObjRelease(workbook)
ObjRelease(sheet)
ObjRelease(excel)



; Bubble sort implementation for an array of objects by key "B"
n := data.MaxIndex()
Loop, % n
{
    swapped := false
    Loop, % (n-1)
    {
        if (data[A_Index]["A"] > data[A_Index+1]["A"])
        {
            temp := data[A_Index]
            data[A_Index] := data[A_Index+1]
            data[A_Index+1] := temp
            swapped := true
        }
    }
    if (!swapped)
        break
}

; Display the sorted data
; for index, obj in data {
;     MsgBox, % "Index: " . index . " - B: " . obj["B"] . ", OtherKey: " . obj["OtherKey"]
; }

; TODO : INSERTING BLANK
; Now, fill in the gaps
lastNumber := 0
sortedData := []
for index, row in data {
    thisNumber := row[columnLetters[1]]
    ; Fill in the gaps
    while (++lastNumber < thisNumber) {
        if (lastNumber < 100) {
            sortedData.Push({columnLetters[1]: lastNumber, columnLetters[2]: "Blank", columnLetters[3]: "Smoke"})
        } else if (lastNumber > 100) {
            sortedData.Push({columnLetters[1]: lastNumber, columnLetters[2]: "Blank", columnLetters[3]: "Duals/clip"})
        }
    }
    ; MsgBox, % "#" row["B"] ", " row[columnLetters[2]] ", " row[columnLetters[3]]
    sortedData.Push(row)  ; Add the current row
}


; Convert the array of objects to a delimited string
delimitedData := ""
for index, rowObject in sortedData {
    delimitedData .= rowObject[columnLetters[1]] "|" rowObject[columnLetters[2]] "|" rowObject[columnLetters[3]] "`n|"
}

; Trim the last newline character
delimitedData := RTrim(delimitedData, "`n")

; Write the delimited string to a temporary file
tempFilePath := "tempArray.txt"
FileDelete, %tempFilePath%
FileAppend, %delimitedData%, %tempFilePath%

; Exit the script
ExitApp