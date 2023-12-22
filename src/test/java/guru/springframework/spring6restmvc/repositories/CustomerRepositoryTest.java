package guru.springframework.spring6restmvc.repositories;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import guru.springframework.spring6restmvc.entities.Customer;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.lang.reflect.Array;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class CustomerRepositoryTest {
    @Autowired
    ICustomerRepository customerRepository;

    @Test
    void testSaveCustomer() {
        Customer savedCustomer = customerRepository.save(Customer.builder()
                        .name("NewCust4repo")
                .build());

        Assertions.assertThat(savedCustomer).isNotNull();
        Assertions.assertThat(savedCustomer.getId()).isNotNull();
    }

    // --------------------------------------------------------------------------
    // EXTRA TESTING
    // --------------------------------------------------------------------------

    @Test
    void testConversions() {
        //Object[] obj = new Object[1];
        //Arrays.stream(obj).anyMatch(CustomerRepositoryTest::doStuff);

        byte[] ba1 = new byte[2];
        ba1[0] = 10;
        ba1[1] = 63;
        Object o1 = (Object)ba1;


        Byte[] ba2 = new Byte[2];
        ba2[0] = 10;
        ba2[1] = 63;
        Object o2 = (Object)ba2;
        Optional<Object[]> o2a = ggg(o2);

        Short[] ba3 = new Short[2];
        ba3[0] = 10;
        ba3[1] = 63;
        Object o3 = (Object)ba3;
        Optional<Object[]> o3a = ggg(o3);

        short[] ba4 = new short[2];
        ba4[0] = 10;
        ba4[1] = 63;
        Object o4 = (Object)ba4;
        Optional<Object[]> o4a = ggg(o4);

//                boolean[] tmp = (boolean[]) obj;
//        Object[] ret = new Object[((boolean[]) obj).length];
//        for (int i = 0; i < ret.length; i++) {
//            ret[i] = tmp[i];
//        }
//        return ret;

        List<Object> objects = new ArrayList<Object>(Arrays.asList(o1));
        if (objects.toArray().length > 0 && Arrays.stream(objects.toArray()).anyMatch(CustomerRepositoryTest::doStuff)) {
            ;
        };

        Optional<Object[]> parsed = tryConvertArrayFromObject((Object)objects);
//        if (! objects.isEmpty() && parsed.get().length > 0 && Arrays.stream(parsed.get()).anyMatch(CustomerRepositoryTest::doStuff)) {
//            ;
//        }

    }

    private static Optional<Object[]> tryConvertArrayFromObject(Object obj) {
        if (obj == null) {
            return Optional.empty();
        }
        if (obj instanceof Object[]) {
            return Optional.of((Object[]) obj);
        }
        else if (obj.getClass().isPrimitive()) {
            int length = Array.getLength(obj);
            Object[] ret = new Object[length];
            for (int i = 0; i < length; i++) {
                ret[i] = Array.get(obj, i);
            }
            return Optional.of(ret);
        }
        return Optional.empty();
    }

    private static boolean doStuff(Object o) {
        return true;
    }

    private static <T> Optional<Object[]> ggg(Object obj) {

        if (obj instanceof Object[] && !obj.getClass().isPrimitive()) {
            T[] tmp = (T[]) obj;
            Object[] ret = new Object[((T[]) obj).length];
            for (int i = 0; i < ret.length; i++) {
                ret[i] = tmp[i];
            }
            return Optional.of(ret);
        }
        return Optional.empty();
    }

    // --------------------------------------------------------------------------
    // EXTRA TESTING
    // --------------------------------------------------------------------------

    public static ObjectMapper createYamlMapper() {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory()
                .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
                .disable(YAMLGenerator.Feature.SPLIT_LINES)
                .disable(YAMLGenerator.Feature.MINIMIZE_QUOTES)
                .enable(YAMLGenerator.Feature.LITERAL_BLOCK_STYLE));
        mapper.findAndRegisterModules();
        return mapper;
    }

    public static ObjectMapper createJsonMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // JsonNode node = mapper.createObjectNode();
        // JsonNode node = JsonNodeFactory.instance.objectNode();
        // JsonNode node = mapper.valueToTree(fromValue);
        return mapper;
    }

    @Test
    public void givenANode_whenModifyingIt_thenCorrect() throws IOException {
        String newString1 = "{\"nick\": \"cowtowncoder\"}";
        String newString2 = "{\"kind\": \"ConfigMap\",\"version\": \"1.2.3\"}";
        String newString3 = "{\"services\":{\"s1\":{\"fld\": \"val1\"},\"s2\":{\"fld\": \"val2\"}}}";
        ObjectMapper mapper = createJsonMapper();
        JsonNode newNode1 = mapper.readTree(newString1);
        JsonNode newNode2 = mapper.readTree(newString2);
        JsonNode newNode3 = mapper.readTree(newString3);

        // tree.path("metadata").path("name").isMissingNode()
        ((ObjectNode) newNode2).set("name", newNode1);

        assertFalse(newNode2.path("name").path("nick").isMissingNode());
        assertEquals("cowtowncoder", newNode2.path("name").path("nick").textValue());
    }


}