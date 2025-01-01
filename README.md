# Welcome to our project **"BASIC Interpreter"** for KIU! 🎉

**⚠️ (KEEP IN MIND THE APP IS CONSOLE BASED!)**

---

## 📘 Small Guide to Using the Program:

- **RUN**: Starts executing code.
- **END**: Terminates the application.
- **SAVE [file name]**: Saves code under the specified name.
- **LOAD/delete**: Lists saved files.
- **LOAD [fileName]**: Loads a specified file if saved.
- **DELETE [fileName]**: Deletes a specified file if saved.

---

### 📄 BASIC> Current Program:

```basic
0 LET x = 1
1 WHILE x <= 5
2 PRINT x
3 x = x + 1
4 WEND
5 PRINT
```
"hi, the while loop hopefully worked, and this was printed after the while loop ended."

  has to be numbered from 0 up.
Let statement declares a variable, it can do arithmetic operations before assigning it to the variable too.
after declaring and initializing a variable, you dont have to use the LET statement again, you just just write the variable name.

the IF statement compares 2 values, and then reads the number after it if its true, skipping to the numbered line that was written
IF [condition] THEN [double]

GOTO just expects a line number after it and skips to that number

and then theres WHILE - WEND. it does what you expect, after WHILE you have to write similarly to the IF, but theres no THEN (obviously), its just a condition. if the co dition is true, it'll casually continue thriugh the next line, if its false, it'll skip past the WEND. upon reaching the WEND normally, it then is redirected back to the line where the WHILE statement is, and checks the condition again.

PRINT can just wrint a variable ir a String value.
---
The End!

