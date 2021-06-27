package com.example.es.test;

import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ESTest01 {

    String s = "redis-cli --cluster create 192.168.49.133:1001 192.168.49.133:1002 192.168.49.133:1003 192.168.49.133:1004 192.168.49.133:1005 192.168.49.133:1006 --cluster-replicas 1";

}
