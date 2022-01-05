import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ExampleUnitTest {

    @Test
    public void test1() {


        String name = "hello.txt";
        int len = name.length();
        byte[] data = name.getBytes(StandardCharsets.UTF_8);
        System.out.println("len=" + len);
        System.out.println((byte) len);
        System.out.println("data.size="+data.length);
        System.out.println("data="+ Arrays.toString(data));


    }
}
