package guru.springframework.spring6restmvc.repositories;

import guru.springframework.spring6restmvc.entities.Customer;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

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
        if (! objects.isEmpty() && parsed.get().length > 0 && Arrays.stream(parsed.get()).anyMatch(CustomerRepositoryTest::doStuff)) {
            ;
        }

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


}