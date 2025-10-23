package org.foss.apocylberry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import com.foss.apocylberry.StringDiff;


public class StringDiffTest {
    @Test
    public void SD_Test_000_00() {
        StringDiff source = new StringDiff("Hello");
        StringDiff compare = new StringDiff("hello");

        assertEquals(source.getProcessingTime(), "PROCESSING TIME: 0.0000000ms");

        source.findAllDifferences(compare);
        assertTrue(source.getProcessingTime().matches("PROCESSING TIME: ([^n]*)ms"));
    }

    @Test
    public void SD_Test_001_00() {
        StringDiff source = new StringDiff("Hello");

        assertEquals(source.toString(0), "H: 0d0072 0x0048 0b01001000");
        assertEquals(source.toString(0, true), "       0: `H: 0d0072 0x0048 0b01001000`");
        assertEquals(source.toString(), "H: 0d0072 0x0048 0b01001000\ne: 0d0101 0x0065 0b01100101\nl: 0d0108 0x006c 0b01101100\nl: 0d0108 0x006c 0b01101100\no: 0d0111 0x006f 0b01101111");
        assertEquals(source.toString(true),"       0: `H: 0d0072 0x0048 0b01001000`\n       1: `e: 0d0101 0x0065 0b01100101`\n       2: `l: 0d0108 0x006c 0b01101100`\n       3: `l: 0d0108 0x006c 0b01101100`\n       4: `o: 0d0111 0x006f 0b01101111`");
    }

    @Test
    public void SD_Test_002_00() {
        StringDiff source = new StringDiff("Hello");
        StringDiff compare = new StringDiff("Hello world");
        
        assertEquals(source.findFirstDifferenceIndex(compare), -1);
        assertEquals(compare.findFirstDifferenceIndex(source), 5);
        assertEquals(compare.toString(compare.findFirstDifferenceIndex(source)), " : 0d0032 0x0020 0b00100000");
    }

    @Test
    public void SD_Test_003_00() {
        StringDiff source = new StringDiff("Hello world");
        ArrayList<StringDiff> compares = new ArrayList<>();
        compares.add(source);
        compares.add(new StringDiff("Hello world"));
        compares.add(new StringDiff("Hello World"));
        compares.add(new StringDiff("Hello WOrld"));
        compares.add(new StringDiff("hello World"));
        compares.add(new StringDiff("Hello"));
        compares.add(new StringDiff("Hello world !"));
        compares.add(new StringDiff("hello"));
        compares.add(new StringDiff("hello world !"));
        compares.add(new StringDiff("hEllo"));
        compares.add(new StringDiff("Hello 12 world"));
        compares.add(new StringDiff("Hello [wW]orld"));
        compares.add(new StringDiff("Hello [Ww]orld"));
        compares.add(new StringDiff("hello [wW]orlD"));

        int comparesIndex = -1;
        // 1    Source / compare objects the same
        comparesIndex ++;
        testFindAllDifferences(source, compares.get(comparesIndex), String.format("INPUT AND OUTPUT POINT TO SAME OBJECT `0x%08x`", source.hashCode()));

        // ** SAME CONTENT LENGTH **
        // 2    Same string
        comparesIndex ++;
        testFindAllDifferences(source, compares.get(comparesIndex), "");

        // 3    One character different
        comparesIndex ++;
        testFindAllDifferences(source, compares.get(comparesIndex), "SUBSTITUTE S-00000006: `w: 0d0119 0x0077 0b01110111` -> C-00000006: `W: 0d0087 0x0057 0b01010111`");

        // 4    Two characters different, contiguous
        comparesIndex ++;
        testFindAllDifferences(source, compares.get(comparesIndex), "SUBSTITUTE S-00000006: `w: 0d0119 0x0077 0b01110111` -> C-00000006: `W: 0d0087 0x0057 0b01010111`|SUBSTITUTE S-00000007: `o: 0d0111 0x006f 0b01101111` -> C-00000007: `O: 0d0079 0x004f 0b01001111`");

        // 5    Two characters different; different locations
        comparesIndex ++;
        testFindAllDifferences(source, compares.get(comparesIndex), "SUBSTITUTE S-00000000: `H: 0d0072 0x0048 0b01001000` -> C-00000000: `h: 0d0104 0x0068 0b01101000`|SUBSTITUTE S-00000006: `w: 0d0119 0x0077 0b01110111` -> C-00000006: `W: 0d0087 0x0057 0b01010111`");

        // ** DIFFERENT CONTENT LENGTH **
        // 6    Same string, source has more
        comparesIndex ++;
        testFindAllDifferences(source, compares.get(comparesIndex), "DELETE     S-00000005: ` : 0d0032 0x0020 0b00100000`|DELETE     S-00000006: `w: 0d0119 0x0077 0b01110111`|DELETE     S-00000007: `o: 0d0111 0x006f 0b01101111`|DELETE     S-00000008: `r: 0d0114 0x0072 0b01110010`|DELETE     S-00000009: `l: 0d0108 0x006c 0b01101100`|DELETE     S-00000010: `d: 0d0100 0x0064 0b01100100`");

        // 7    Same string, compare has more
        comparesIndex ++;
        testFindAllDifferences(source, compares.get(comparesIndex), "INSERT                                                  C-00000011: ` : 0d0032 0x0020 0b00100000`|INSERT                                                  C-00000012: `!: 0d0033 0x0021 0b00100001`");

        // 8    One character different, source has more
        comparesIndex ++;
        testFindAllDifferences(source, compares.get(comparesIndex), "SUBSTITUTE S-00000000: `H: 0d0072 0x0048 0b01001000` -> C-00000000: `h: 0d0104 0x0068 0b01101000`|DELETE     S-00000005: ` : 0d0032 0x0020 0b00100000`|DELETE     S-00000006: `w: 0d0119 0x0077 0b01110111`|DELETE     S-00000007: `o: 0d0111 0x006f 0b01101111`|DELETE     S-00000008: `r: 0d0114 0x0072 0b01110010`|DELETE     S-00000009: `l: 0d0108 0x006c 0b01101100`|DELETE     S-00000010: `d: 0d0100 0x0064 0b01100100`");

        // 9    One character different, compare has more
        comparesIndex ++;
        testFindAllDifferences(source, compares.get(comparesIndex), "SUBSTITUTE S-00000000: `H: 0d0072 0x0048 0b01001000` -> C-00000000: `h: 0d0104 0x0068 0b01101000`|INSERT                                                  C-00000011: ` : 0d0032 0x0020 0b00100000`|INSERT                                                  C-00000012: `!: 0d0033 0x0021 0b00100001`");

        //10    Two characters different, contiguous; source has more
        comparesIndex ++;
        testFindAllDifferences(source, compares.get(comparesIndex), "SUBSTITUTE S-00000000: `H: 0d0072 0x0048 0b01001000` -> C-00000000: `h: 0d0104 0x0068 0b01101000`|SUBSTITUTE S-00000001: `e: 0d0101 0x0065 0b01100101` -> C-00000001: `E: 0d0069 0x0045 0b01000101`|DELETE     S-00000005: ` : 0d0032 0x0020 0b00100000`|DELETE     S-00000006: `w: 0d0119 0x0077 0b01110111`|DELETE     S-00000007: `o: 0d0111 0x006f 0b01101111`|DELETE     S-00000008: `r: 0d0114 0x0072 0b01110010`|DELETE     S-00000009: `l: 0d0108 0x006c 0b01101100`|DELETE     S-00000010: `d: 0d0100 0x0064 0b01100100`");

        //11    Addded three characters, compare has more but they terminate on the same end
        comparesIndex ++;
        testFindAllDifferences(source, compares.get(comparesIndex), "INSERT                                                  C-00000006: `1: 0d0049 0x0031 0b00110001`|INSERT                                                  C-00000007: `2: 0d0050 0x0032 0b00110010`|INSERT                                                  C-00000008: ` : 0d0032 0x0020 0b00100000`");

        //12    Contains a regex pattern match
        comparesIndex ++;
        testFindAllDifferences(source, compares.get(comparesIndex), "INSERT                                                  C-00000006: `[: 0d0091 0x005b 0b01011011`|INSERT                                                  C-00000008: `W: 0d0087 0x0057 0b01010111`|INSERT                                                  C-00000009: `]: 0d0093 0x005d 0b01011101`");

        //13    Contains a regex pattern match, reversed capitalization from prior regex
        comparesIndex ++;
        testFindAllDifferences(source, compares.get(comparesIndex), "INSERT                                                  C-00000006: `[: 0d0091 0x005b 0b01011011`|INSERT                                                  C-00000007: `W: 0d0087 0x0057 0b01010111`|INSERT                                                  C-00000009: `]: 0d0093 0x005d 0b01011101`");

        //14    Contains original regex pattern match and a later substitution
        comparesIndex ++;
        testFindAllDifferences(source, compares.get(comparesIndex), "SUBSTITUTE S-00000000: `H: 0d0072 0x0048 0b01001000` -> C-00000000: `h: 0d0104 0x0068 0b01101000`|INSERT                                                  C-00000006: `[: 0d0091 0x005b 0b01011011`|INSERT                                                  C-00000008: `W: 0d0087 0x0057 0b01010111`|INSERT                                                  C-00000009: `]: 0d0093 0x005d 0b01011101`|SUBSTITUTE S-00000010: `d: 0d0100 0x0064 0b01100100` -> C-00000013: `D: 0d0068 0x0044 0b01000100`");
    }

    @Test
    public void SD_Test_004_00() {
        // Real-world data
        ArrayList<StringDiff> compares = new ArrayList<>();
        int index = -1;

        // Isolated - unmodified to seek string
        compares.add(new StringDiff("\"connectionTimeMS\":574,\"executionTimeMS\":85,\"retryCount\":0,\"executeContinue\":false"));
        compares.add(new StringDiff("\"connectionTimeMS\":#ANY-COMMA,\"executionTimeMS\":#ANY-COMMA,\"retryCount\":0,\"executeContinue\":false"));
        testFindAllDifferences(compares.get(++index), compares.get(++index), "SUBSTITUTE S-00000019: `5: 0d0053 0x0035 0b00110101` -> C-00000019: `#: 0d0035 0x0023 0b00100011`|SUBSTITUTE S-00000020: `7: 0d0055 0x0037 0b00110111` -> C-00000020: `A: 0d0065 0x0041 0b01000001`|SUBSTITUTE S-00000021: `4: 0d0052 0x0034 0b00110100` -> C-00000021: `N: 0d0078 0x004e 0b01001110`|INSERT                                                  C-00000022: `Y: 0d0089 0x0059 0b01011001`|INSERT                                                  C-00000023: `-: 0d0045 0x002d 0b00101101`|INSERT                                                  C-00000024: `C: 0d0067 0x0043 0b01000011`|INSERT                                                  C-00000025: `O: 0d0079 0x004f 0b01001111`|INSERT                                                  C-00000026: `M: 0d0077 0x004d 0b01001101`|INSERT                                                  C-00000027: `M: 0d0077 0x004d 0b01001101`|INSERT                                                  C-00000028: `A: 0d0065 0x0041 0b01000001`|SUBSTITUTE S-00000041: `8: 0d0056 0x0038 0b00111000` -> C-00000048: `#: 0d0035 0x0023 0b00100011`|SUBSTITUTE S-00000042: `5: 0d0053 0x0035 0b00110101` -> C-00000049: `A: 0d0065 0x0041 0b01000001`|INSERT                                                  C-00000050: `N: 0d0078 0x004e 0b01001110`|INSERT                                                  C-00000051: `Y: 0d0089 0x0059 0b01011001`|INSERT                                                  C-00000052: `-: 0d0045 0x002d 0b00101101`|INSERT                                                  C-00000053: `C: 0d0067 0x0043 0b01000011`|INSERT                                                  C-00000054: `O: 0d0079 0x004f 0b01001111`|INSERT                                                  C-00000055: `M: 0d0077 0x004d 0b01001101`|INSERT                                                  C-00000056: `M: 0d0077 0x004d 0b01001101`|INSERT                                                  C-00000057: `A: 0d0065 0x0041 0b01000001`");
        
        // Full - unmodified to seek string
        compares.add(new StringDiff("{\"service_msg\":\"[jcc][t4][102][10040][4.28.11] Batch failure.  The batch was submitted, but at least one exception occurred on an individual member of the batch.\r\nUse getNextException() to retrieve the exceptions for specific batched elements. ERRORCODE=-4229, SQLSTATE=null\",\"service_status_code\":-1,\"sql_status\":{\"executionInfo\":[{\"name\":\"UPSERT_CLUB_ITEM_INVT\",\"sqlStmt\":\"INSERT INTO TABLE.LANGUAGE (LANGUAGE_CODE, BASE_LANGUAGE_CODE, COUNTRY_CODE)\nVALUES (\n  ?,\n  ?,\n  ?\n)\",\"sqlCode\":-4229,\"sqlState\":\"\",\"sqlMsg\":\"[jcc][t4][102][10040][4.28.11] Batch failure.  The batch was submitted, but at least one exception occurred on an individual member of the batch.\r\nUse getNextException() to retrieve the exceptions for specific batched elements. ERRORCODE=-4229, SQLSTATE=null ('A NON-ATOMIC INSERT STATEMENT ATTEMPTED TO PROCESS MULTIPLE ROWS OF DATA, BUT ERRORS OCCURRED' ERRORCODE=-254, SQLSTATE=22530\r\n'Error for batch element #1: THE VALUE OF INPUT VARIABLE OR PARAMETER NUMBER 2 IS INVALID OR TOO LARGE FOR THE TARGET COLUMN OR THE TARGET VALUE' ERRORCODE=-302, SQLSTATE=22001)\",\"connectionTimeMS\":574,\"executionTimeMS\":85,\"retryCount\":0,\"executeContinue\":false,\"batchErrorRow\":0,\"variables\":{\"request.code\":\"US\",\"request.baseCode\":\"USEN\",\"request.cc\":\"US\"}}],\"UPSERT_CLUB_ITEM_INVT\":{\"excpCode\":-4229,\"batchErrorRow\":0,\"excpMsg\":\"[jcc][t4][102][10040][4.28.11] Batch failure.  The batch was submitted, but at least one exception occurred on an individual member of the batch.\r\nUse getNextException() to retrieve the exceptions for specific batched elements. ERRORCODE=-4229, SQLSTATE=null ('A NON-ATOMIC INSERT STATEMENT ATTEMPTED TO PROCESS MULTIPLE ROWS OF DATA, BUT ERRORS OCCURRED' ERRORCODE=-254, SQLSTATE=22530\r\n'Error for batch element #1: THE VALUE OF INPUT VARIABLE OR PARAMETER NUMBER 2 IS INVALID OR TOO LARGE FOR THE TARGET COLUMN OR THE TARGET VALUE' ERRORCODE=-302, SQLSTATE=22001)\",\"innerException\":{\"excpCode\": -254, \"excpMsg\": \"A NON-ATOMIC INSERT STATEMENT ATTEMPTED TO PROCESS MULTIPLE ROWS OF DATA, BUT ERRORS OCCURRED\", \"excpState\": \"22530\", \"innerException\": {\"excpCode\": -302, \"excpMsg\": \"Error for batch element #1: THE VALUE OF INPUT VARIABLE OR PARAMETER NUMBER 2 IS INVALID OR TOO LARGE FOR THE TARGET COLUMN OR THE TARGET VALUE\", \"excpState\": \"22001\", \"innerException\": null}},\"execution_count\":1,\"rows\":0,\"excpState\":\"\"}}}"));
        compares.add(new StringDiff("{\"service_msg\":\"[jcc][t4][102][10040][4.28.11] Batch failure.  The batch was submitted, but at least one exception occurred on an individual member of the batch.\r\nUse getNextException() to retrieve the exceptions for specific batched elements. ERRORCODE=-4229, SQLSTATE=null\",\"service_status_code\":-1,\"sql_status\":{\"executionInfo\":[{\"name\":\"UPSERT_CLUB_ITEM_INVT\",\"sqlStmt\":\"INSERT INTO TABLE.LANGUAGE (LANGUAGE_CODE, BASE_LANGUAGE_CODE, COUNTRY_CODE)\nVALUES (\n  ?,\n  ?,\n  ?\n)\",\"sqlCode\":-4229,\"sqlState\":\"\",\"sqlMsg\":\"[jcc][t4][102][10040][4.28.11] Batch failure.  The batch was submitted, but at least one exception occurred on an individual member of the batch.\r\nUse getNextException() to retrieve the exceptions for specific batched elements. ERRORCODE=-4229, SQLSTATE=null ('A NON-ATOMIC INSERT STATEMENT ATTEMPTED TO PROCESS MULTIPLE ROWS OF DATA, BUT ERRORS OCCURRED' ERRORCODE=-254, SQLSTATE=22530\r\n'Error for batch element #1: THE VALUE OF INPUT VARIABLE OR PARAMETER NUMBER 2 IS INVALID OR TOO LARGE FOR THE TARGET COLUMN OR THE TARGET VALUE' ERRORCODE=-302, SQLSTATE=22001)\",\"connectionTimeMS\":#ANY-COMMA,\"executionTimeMS\":#ANY-COMMA,\"retryCount\":0,\"executeContinue\":false,\"batchErrorRow\":0,\"variables\":{\"request.code\":\"US\",\"request.baseCode\":\"USEN\",\"request.cc\":\"US\"}}],\"UPSERT_CLUB_ITEM_INVT\":{\"excpCode\":-4229,\"batchErrorRow\":0,\"excpMsg\":\"[jcc][t4][102][10040][4.28.11] Batch failure.  The batch was submitted, but at least one exception occurred on an individual member of the batch.\r\nUse getNextException() to retrieve the exceptions for specific batched elements. ERRORCODE=-4229, SQLSTATE=null ('A NON-ATOMIC INSERT STATEMENT ATTEMPTED TO PROCESS MULTIPLE ROWS OF DATA, BUT ERRORS OCCURRED' ERRORCODE=-254, SQLSTATE=22530\r\n'Error for batch element #1: THE VALUE OF INPUT VARIABLE OR PARAMETER NUMBER 2 IS INVALID OR TOO LARGE FOR THE TARGET COLUMN OR THE TARGET VALUE' ERRORCODE=-302, SQLSTATE=22001)\",\"innerException\":{\"excpCode\": -254, \"excpMsg\": \"A NON-ATOMIC INSERT STATEMENT ATTEMPTED TO PROCESS MULTIPLE ROWS OF DATA, BUT ERRORS OCCURRED\", \"excpState\": \"22530\", \"innerException\": {\"excpCode\": -302, \"excpMsg\": \"Error for batch element #1: THE VALUE OF INPUT VARIABLE OR PARAMETER NUMBER 2 IS INVALID OR TOO LARGE FOR THE TARGET COLUMN OR THE TARGET VALUE\", \"excpState\": \"22001\", \"innerException\": null}},\"execution_count\":1,\"rows\":0,\"excpState\":\"\"}}}"));
        testFindAllDifferences(compares.get(++index), compares.get(++index), "SUBSTITUTE S-00001106: `5: 0d0053 0x0035 0b00110101` -> C-00001106: `#: 0d0035 0x0023 0b00100011`|SUBSTITUTE S-00001107: `7: 0d0055 0x0037 0b00110111` -> C-00001107: `A: 0d0065 0x0041 0b01000001`|SUBSTITUTE S-00001108: `4: 0d0052 0x0034 0b00110100` -> C-00001108: `N: 0d0078 0x004e 0b01001110`|INSERT                                                  C-00001109: `Y: 0d0089 0x0059 0b01011001`|INSERT                                                  C-00001110: `-: 0d0045 0x002d 0b00101101`|INSERT                                                  C-00001111: `C: 0d0067 0x0043 0b01000011`|INSERT                                                  C-00001112: `O: 0d0079 0x004f 0b01001111`|INSERT                                                  C-00001113: `M: 0d0077 0x004d 0b01001101`|INSERT                                                  C-00001114: `M: 0d0077 0x004d 0b01001101`|INSERT                                                  C-00001115: `A: 0d0065 0x0041 0b01000001`|SUBSTITUTE S-00001128: `8: 0d0056 0x0038 0b00111000` -> C-00001135: `#: 0d0035 0x0023 0b00100011`|SUBSTITUTE S-00001129: `5: 0d0053 0x0035 0b00110101` -> C-00001136: `A: 0d0065 0x0041 0b01000001`|INSERT                                                  C-00001137: `N: 0d0078 0x004e 0b01001110`|INSERT                                                  C-00001138: `Y: 0d0089 0x0059 0b01011001`|INSERT                                                  C-00001139: `-: 0d0045 0x002d 0b00101101`|INSERT                                                  C-00001140: `C: 0d0067 0x0043 0b01000011`|INSERT                                                  C-00001141: `O: 0d0079 0x004f 0b01001111`|INSERT                                                  C-00001142: `M: 0d0077 0x004d 0b01001101`|INSERT                                                  C-00001143: `M: 0d0077 0x004d 0b01001101`|INSERT                                                  C-00001144: `A: 0d0065 0x0041 0b01000001`");
    }

    private void testFindAllDifferences(StringDiff source, StringDiff compare, String assertOutput) {
        assertEquals(assertOutput, String.join("|", source.findAllDifferences(compare)));
    }
}
