package org.foss.apocylberry;

import java.util.ArrayList;

/**
 * Utility class for validating and comparing strings at the character level.
 * Provides methods for printing string contents, finding differences between strings,
 * and disposing of sensitive string data from memory.
 */
public class StringDiff {
    private final String    SAME_INSTANCE_TEXT = String.format("INPUT AND OUTPUT POINT TO SAME OBJECT `0x%08x`", this.hashCode());
    private final String    EMPTY_INPUT_SOURCE = "SOURCE STRING IS EMPTY";
    private final String    EMPTY_INPUT_COMPARE = "COMPARE STRING IS EMPTY";

    private char[]          contents;
    private boolean         _isDisposed = false;
    private long            startTime = 0;
    private long            endTime = 0;
    public enum TimeUnits {
        seconds,
        milliseconds,
        nanoseconds
    }

    public TimeUnits defaultTimeUnit = TimeUnits.milliseconds;
    public boolean printDecimal = true;
    public boolean printHex = false;
    public boolean printBinary = false;
    
    /**
     * Maximum number of characters to peek ahead when attempting to distinguish
     * insertions/deletions from substitutions during diffing.
     *
     * The diff algorithm will scan up to this many characters forward in both
     * the source and compare buffers when a mismatch is found, in order to
     * determine whether the difference is an insertion, a deletion, or a
     * substitution. Increasing this value can improve detection of multi-
     * character insert/delete sequences but increases work per mismatch.
     *
     * Set to 0 to disable lookahead (only direct substitutions are detected).
     *
     * Default: 10. Must be non-negative.
     */
    public int maxSubstitutionPeek = 10;


    public StringDiff(String forString)                                        {this(forString, false);}
    public StringDiff(String forString, boolean printOutput) {
        if ( forString == null ) forString = "";
        
        contents = new char[forString.length()];
        for (int iPOS = 0; iPOS < forString.length(); iPOS ++) {
            contents[iPOS] = forString.charAt(iPOS);
            if (printOutput) print(iPOS, true);
        }
    }

    // .NET-esque disposal pattern
    public boolean isDisposed() {return _isDisposed;}
    public void dispose() {
        // Wipe the memory clean
        for (int iPOS = 0; iPOS < contents.length; iPOS++) {
            contents[iPOS] = 0;
        }

        // Remove memory allocation
        contents = new char[0];
        _isDisposed = true;
    }
    

    @Override
    public String toString()                                                        {return toString(false);}
    public String toString(boolean printIndexNumber)                                {return toString(0, contents.length, printIndexNumber);}
    public String toString(int index)                                               {return toString(index, false);}
    public String toString(int index, boolean printIndexNumber)                     {return toString(index, index + 1, printIndexNumber);}
    public String toString(int startIndex, int endIndex)                            {return toString(startIndex, endIndex, false);}
    public String toString(int startIndex, int endIndex, boolean printIndexNumber) {
        StringBuilder Response = new StringBuilder("");

        for ( int iPOS = startIndex; iPOS < endIndex; iPOS ++ ) {
            if (iPOS >= contents.length) break;

            if (printIndexNumber) Response.append(String.format("%8d: `", iPOS));
                                  Response.append(printableCharacter(contents[iPOS]));

                                  // Print the encoding values
                                  if ( printDecimal || printHex || printBinary ) {
                                                      Response.append(":");
                                  }

                                  if ( printDecimal ) Response.append( String.format(" 0d%04d", (int)contents[iPOS] ));
                                  if ( printHex )     Response.append( String.format(" 0x%04x", (int)contents[iPOS] ));
                                  if ( printBinary )  Response.append(" 0b").append(String.format("%8s", Integer.toBinaryString(contents[iPOS])).replace(' ', '0'));
            if (printIndexNumber) Response.append("`");
                                  Response.append("\n");
        }

        // Remove final newline
        if (Response.length() > 0) Response.setLength(Response.length() - 1);

        return Response.toString();
    }

    public int length()                                                             {return contents.length;}
    public int lastIndex()                                                          {return this.length() - 1;}

    public void printFrom(int startIndex)                                           {printFrom(startIndex, false);}
    public void printFrom(int startIndex, boolean printIndexNumber)                 {print(startIndex, contents.length, printIndexNumber);}
    public void print()                                                             {print(false);}
    public void print(boolean printIndexNumber)                                     {print(0, contents.length, printIndexNumber);}
    public void print(int index)                                                    {print(index, false);}
    public void print(int index, boolean printIndexNumber)                          {print(index, index + 1, printIndexNumber);}
    public void print(int startIndex, int endIndex)                                 {print(startIndex, endIndex, false);}
    public void print(int startIndex, int endIndex, boolean printIndexNumber) {
        for (int iPOS = startIndex; iPOS < endIndex; iPOS ++) {
            if (iPOS >= contents.length ) break;
            System.out.println(toString(iPOS, printIndexNumber));
        }
    }

    /**
     * Provides the processing time for the most recent call through {@code .findAllDifferences()}.
     * Set {@code .defaultTimeUnit} to alter output time unit
     * @return {@code String} PROCESSING TIME: n
     */
    public String getProcessingTime() {
        String timeUnitName = switch(defaultTimeUnit) {
            case nanoseconds -> "0fns";
            case milliseconds -> "7fms";
            case seconds -> "4fs";
        };

        float processingTime = switch(defaultTimeUnit) {
            case nanoseconds -> (endTime - startTime);
            case milliseconds -> (endTime - startTime) / 1000000000.0f * 1000;
            case seconds -> ((endTime - startTime) / 1000000000.0f) / 1000.0f;
        };

        return String.format("PROCESSING TIME: %1." + timeUnitName, processingTime);
    }

    // String-native comparisons
    public int findFirstDifferenceIndex(String compare)                             {return findFirstDifferenceIndex(compare, 0, 0);}
    public int findFirstDifferenceIndex(String compare, int fromIndex)              {return findFirstDifferenceIndex(compare, fromIndex, fromIndex);}
    public int findFirstDifferenceIndex(String compare, int fromSourceIndex, int fromCompareIndex) {return findFirstDifferenceIndex(new StringDiff(compare), fromSourceIndex, fromCompareIndex);}
    
    // Cross-validator comparisons
    public int findFirstDifferenceIndex(StringDiff compare)                         {return findFirstDifferenceIndex(compare, 0, 0);}
    public int findFirstDifferenceIndex(StringDiff compare, int fromIndex)          {return findFirstDifferenceIndex(compare, fromIndex, fromIndex);}
    public int findFirstDifferenceIndex(StringDiff compare, int fromSourceIndex, int fromCompareIndex) {
        int Response = -1;

        if (this == compare) {
            System.out.println(SAME_INSTANCE_TEXT);
        }
        if (contents.length == 0 && compare.contents.length > 0) {
            System.out.println(EMPTY_INPUT_SOURCE);
        }
        if (contents.length > 0 && compare.contents.length == 0) {
            System.out.println(EMPTY_INPUT_COMPARE);
        }
        else {
            if ( fromSourceIndex < contents.length && fromCompareIndex < compare.contents.length ) {
                int iterations = 0;
                for (int iPOS = fromSourceIndex; iPOS < contents.length; iPOS ++ ){
                    int kPOS = fromCompareIndex + iterations;

                    if (kPOS >= compare.contents.length || contents[iPOS] != compare.contents[kPOS]) {
                        if (kPOS >= compare.contents.length) {
                            System.out.println(String.format(" OVERRUN INDEX %s || [no more members]`", toString(iPOS, true)));
                        }
                        else {
                            System.out.println(String.format("MISMATCH INDEX %s || `%s`", toString(iPOS, true), compare.toString(kPOS, false)));
                        }
                        Response = iPOS;
                        break;
                    }
                    iterations ++;
                }
                if (Response == -1) System.out.println("NO DIFFERENCES");
            }
        }
        return Response;
    }

    // String-native comparisons
    public void printAllDifferences(String compare)                        {printAllDifferences(compare, 0, 0);}
    public void printAllDifferences(String compare, int fromSourceIndex, int fromCompareIndex) {printAllDifferences(new StringDiff(compare), fromSourceIndex, fromCompareIndex);}

    // Cross-validator comparisons
    public void printAllDifferences(StringDiff compare)                        {printAllDifferences(compare, 0, 0);}
    public void printAllDifferences(StringDiff compare, int fromSourceIndex, int fromCompareIndex) {
        String[] Response = findAllDifferences(compare, fromSourceIndex, fromCompareIndex);
        if (Response.length == 0) {
            System.out.println("NO DIFFERENCES");
        }
        else {
            for (String line : Response) {
                System.out.println(line);
            }
        }
        System.out.println(getProcessingTime());
    }

    // String-native comparisons
    public String[] findAllDifferences(String compare)                              {return findAllDifferences(compare, 0, 0);}
    public String[] findAllDifferences(String compare, int fromSourceIndex, int fromCompareIndex) {return findAllDifferences(new StringDiff(compare), fromSourceIndex, fromCompareIndex);}

    // Cross-validator comparisons
    public String[] findAllDifferences(StringDiff compare)                     {return findAllDifferences(compare, 0, 0);}
    public String[] findAllDifferences(StringDiff compare, int fromSourceIndex, int fromCompareIndex) {
        ArrayList<String> Response = new ArrayList<>();
        startTime = System.nanoTime();
        endTime = startTime;

        boolean originalDecimal = compare.printDecimal;
        boolean originalHex = compare.printHex;
        boolean originalBinary = compare.printBinary;
        try {
            // Make compare output match source output
            compare.printDecimal = this.printDecimal;
            compare.printHex = this.printHex;
            compare.printBinary = this.printBinary;

            if (this == compare) {
                Response.add(SAME_INSTANCE_TEXT);
                System.out.println(Response.get(0));
            }
            if (contents.length == 0 && compare.contents.length > 0) {
                Response.add(EMPTY_INPUT_SOURCE);
                System.out.println(Response.get(0));
            }
            if (contents.length > 0 && compare.contents.length == 0) {
                Response.add(EMPTY_INPUT_COMPARE);
                System.out.println(Response.get(0));
            }
            else {
                int iPOS = fromSourceIndex;
                int jPOS = fromCompareIndex;
                while (iPOS < contents.length && jPOS < compare.contents.length) {
                    if (contents[iPOS] == compare.contents[jPOS]) {
                        iPOS++;
                        jPOS++;
                    } else {
                        // Look ahead for insert or delete
                        int iPEEK = iPOS + 1;
                        int jPEEK = jPOS + 1;
                        boolean foundInsert = false;
                        boolean foundDelete = false;

                        // Check for insert in compare
                        while (jPEEK < compare.contents.length && jPEEK - jPOS <= maxSubstitutionPeek) {
                            if (contents[iPOS] == compare.contents[jPEEK]) {
                                foundInsert = true;
                                break;
                            }
                            jPEEK++;
                        }

                        // Check for delete in source
                        while (iPEEK < contents.length && iPEEK - iPOS <= maxSubstitutionPeek) {
                            if (contents[iPEEK] == compare.contents[jPOS]) {
                                foundDelete = true;
                                break;
                            }
                            iPEEK++;
                        }

                        if (foundInsert && (!foundDelete || jPEEK - jPOS <= iPEEK - iPOS)) {
                            // Insert detected in compare
                            for (int k = jPOS; k < jPEEK; k++) {
                                Response.add(String.format("INSERT                                                  C-%08d: `%s`", k, compare.toString(k)));
                            }
                            jPOS = jPEEK;
                        } else if (foundDelete) {
                            // Delete detected in source
                            for (int k = iPOS; k < iPEEK; k++) {
                                Response.add(String.format("DELETE     S-%08d: `%s`", k, toString(k)));
                            }
                            iPOS = iPEEK;
                        } else {
                            // Substitution
                            Response.add(String.format("SUBSTITUTE S-%08d: `%s` -> C-%08d: `%s`", iPOS, toString(iPOS), jPOS, compare.toString(jPOS)));
                            iPOS++;
                            jPOS++;
                        }
                    }
                }

                // Handle remaining deletions
                while (iPOS < contents.length) {
                    Response.add(String.format("DELETE     S-%08d: `%s`", iPOS, toString(iPOS)));
                    iPOS++;
                }

                // Handle remaining insertions
                while (jPOS < compare.contents.length) {
                    Response.add(String.format("INSERT                                                  C-%08d: `%s`", jPOS, compare.toString(jPOS)));
                    jPOS++;
                }
            }
        }
        catch (Exception ex) {
            throw ex;
        }
        finally {
            // Return compare output to original state
            compare.printDecimal = originalDecimal;
            compare.printHex = originalHex;
            compare.printBinary = originalBinary;
        }
        
        endTime = System.nanoTime();
        return Response.toArray(new String[0]);
    }
    
    private char printableCharacter(char input) {
        if (input >= 32 && input <= 127) return input;
        return '\uFFFD';
    }
}
