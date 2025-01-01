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

---

## Explanation of Commands:

- **Numbering**:
  
    Lines must be numbered starting from 0 and increment sequentially.
- **LET Statement**:
  
    The LET statement declares a variable and allows performing arithmetic operations before assigning the result to the variable.

-   After declaring and initializing a variable, you don’t need to use the LET statement again. You can directly reference the variable by its name in subsequent operations.
- **IF Statement**:

   The IF statement compares two values. If the condition is true, the program jumps to the specified line number, skipping the lines in between.

 -   Syntax:

    IF [condition] THEN [lineNumber]
    
  - GOTO Statement:
    The GOTO statement jumps directly to a specified line number.

   - Syntax:

    GOTO [lineNumber]
    
- WHILE - WEND:

    The WHILE loop allows conditional execution of a block of code until the condition becomes false.

  -  After WHILE, write a condition (similar to IF, but without THEN).
  -  If the condition is true, execution proceeds to the next line. If false, the program skips past the WEND.
  -  Upon reaching the WEND, the program returns to the WHILE statement to recheck the condition.
    
  -  Syntax:

          WHILE [condition]
          ...
          WEND
     
- PRINT Statement:
The PRINT statement outputs either a variable or a string value.

   - Syntax:

          PRINT [variable or "string value"]

---
The End!

