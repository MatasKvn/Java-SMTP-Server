package org.mataskvn.smtp.server;

import java.security.KeyPair;
import java.util.HashMap;
import java.util.Map;

public class DNSLookupUtil {
    private static Map<String, Tuple<String, Integer>> dnsRecords;
    public static Tuple<String, Integer> getIpv4AddressOf(String domainName) {
        try {
            return dnsRecords.get(domainName);
        } catch (Exception e) {
            return null;
        }
    }

    // Add more dns records here
    static  {
        dnsRecords = new HashMap<>();
        dnsRecords.put("example1.com", new Tuple<>("localhost",20000));
        dnsRecords.put("example2.com", new Tuple<>("localhost",20001));
    }
    private DNSLookupUtil() {

    }

    public static class Tuple<T1, T2> {
        public T1 fst;
        public T2 snd;
        public Tuple(T1 fst, T2 snd) {
            this.fst = fst;
            this.snd = snd;
        }
        @Override
        public String toString() {
            return String.format("(%s; %s)", fst, snd);
        }
    }


}
