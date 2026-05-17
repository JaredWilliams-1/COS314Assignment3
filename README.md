# COS314Assignment3
Genetic Programming

This is a file detailing how to compile and run this project.

## Run the JAR File
>COS314_GP.jar must be run from the same directory as the .csv file or have the relative path entered when prompted 


1. Run the generated JAR file:
   ```bash
   java -jar COS314_GP.jar
   ```

> To accounts for the two different programs per algorithm, you can select different options when running the file:

2. You will be presented with options 
    1. Search mode
        This is the mode which goes through the 30 runs and finds the best one.
    2. Demo mode
        This mode allows you to enter all the information (seed, filepath, design decision parameters, etc.) and run that particular record

    Type in "1" or "2" (without the quotation marks) and click enter to continue.

3. You will then be presented with another two options:
    1. LogicalGP
    2. SymbolGP
    
    Type in "1" or "2" (without the quotation marks) and click enter to continue.

> Note: if you selected demo mode, you can simply click enter for all the options to default to the best seed and the design decision paramters we used.

Alternatively:
## Compile and Run Normally

1. Compile all Java source files :
   ```bash
   javac *.java
   ```

2. Run the program using the `Main` class:
   ```bash
   java Main
   ```

