
import org.junit.Test
import javax.xml.bind.DatatypeConverter



class DisplayTest {


    @Test
    fun displayWML() {
        val string = "3c636f6d2e6578616d706c652e4f726465723e3c69643e313233343c2f69643e3c7374617475733e3c737472696e673e435245415445443c2f737472696e673e3c737472696e673e555044415445443c2f737472696e673e3c737472696e673e43414e43454c45443c2f737472696e673e3c2f7374617475733e3c2f636f6d2e6578616d706c652e4f726465723e"
        val bytes = DatatypeConverter.parseHexBinary(string)
        println(kotlin.text.String(bytes))
    }
}