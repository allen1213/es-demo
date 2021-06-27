package com.example.es.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class ElasticSearchConfig {

    /** 协议 */
    @Value("${elasticsearch.schema:http}")
    private String schema;

    /** 集群地址，如果有多个用“,”隔开 */
    @Value("${elasticsearch.address}")
    private String address;

    /** 连接超时时间 */
    @Value("${elasticsearch.connectTimeout:5000}")
    private int connectTimeout;

    /** Socket 连接超时时间 */
    @Value("${elasticsearch.socketTimeout:10000}")
    private int socketTimeout;

    /** 获取连接的超时时间 */
    @Value("${elasticsearch.connectionRequestTimeout:5000}")
    private int connectionRequestTimeout;

    /** 最大连接数 */
    @Value("${elasticsearch.maxConnectNum:100}")
    private int maxConnectNum;

    /** 最大路由连接数 */
    @Value("${elasticsearch.maxConnectPerRoute:100}")
    private int maxConnectPerRoute;

    @Bean
    public RestHighLevelClient restHighLevelClient() {

        //拆分地址
        List<HttpHost> hostLists = new ArrayList<>();
        String[] hostList = address.split(",");
        for (String addr : hostList) {
            String host = addr.split(":")[0];
            String port = addr.split(":")[1];
            hostLists.add(new HttpHost(host,Integer.parseInt(port),schema));
        }

        //转换成 HttpHost 数组
        HttpHost[] httpHost = hostLists.toArray(new HttpHost[]{});

        //构建连接对象
        RestClientBuilder builder = RestClient.builder(httpHost);

        //异步连接延时设置
        builder.setRequestConfigCallback(c -> {
           c.setConnectTimeout(connectTimeout);
           c.setSocketTimeout(socketTimeout);
           c.setConnectionRequestTimeout(connectionRequestTimeout);
            return c;
        });

        //异步连接数配置
        builder.setHttpClientConfigCallback(h -> {
            h.setMaxConnTotal(maxConnectNum);
            h.setMaxConnPerRoute(maxConnectPerRoute);
            return h;
        });

        return new RestHighLevelClient(builder);
    }

}
