package com.example.es;

import com.alibaba.fastjson.JSON;
import com.example.es.entity.User;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.range.Range;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.*;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

@SpringBootTest
class EsDemoApplicationTests {

    @Test
    void contextLoads() {
    }

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Test
    public void createIndex() throws Exception {

        // 创建 Mapping
        XContentBuilder mapping = XContentFactory.jsonBuilder()
                .startObject()
                .field("dynamic", true)
                .startObject("properties")
                .startObject("name")
                .field("type", "text")
                .startObject("fields")
                .startObject("keyword")
                .field("type", "keyword")
                .endObject()
                .endObject()
                .endObject()
                .startObject("address")
                .field("type", "text")
                .startObject("fields")
                .startObject("keyword")
                .field("type", "keyword")
                .endObject()
                .endObject()
                .endObject()
                .startObject("remark")
                .field("type", "text")
                .startObject("fields")
                .startObject("keyword")
                .field("type", "keyword")
                .endObject()
                .endObject()
                .endObject()
                .startObject("age")
                .field("type", "integer")
                .endObject()
                .startObject("salary")
                .field("type", "float")
                .endObject()
                .startObject("birthDate")
                .field("type", "date")
                .field("format", "yyyy-MM-dd")
                .endObject()
                .startObject("createTime")
                .field("type", "date")
                .endObject()
                .endObject()
                .endObject();

        // 创建索引配置信息，配置
        Settings settings = Settings.builder()
                .put("index.number_of_shards", 3)
                .put("index.number_of_replicas", 0)
                .build();

        // 新建创建索引请求对象，然后设置索引类型（ES 7.0 将不存在索引类型）和 mapping 与 index 配置
        CreateIndexRequest request = new CreateIndexRequest("user", settings);
        request.mapping("doc", mapping);

        // RestHighLevelClient 执行创建索引
        CreateIndexResponse createIndexResponse = restHighLevelClient.indices().create(request, RequestOptions.DEFAULT);

        // 判断是否创建成功
        boolean isCreated = createIndexResponse.isAcknowledged();

        System.out.println(isCreated);
    }

    @Test
    public void deleteIndex() throws Exception {

        // 新建删除索引请求对象
        DeleteIndexRequest request = new DeleteIndexRequest("user");

        // 执行删除索引
        AcknowledgedResponse acknowledgedResponse = restHighLevelClient.indices().delete(request, RequestOptions.DEFAULT);

        // 判断是否删除成功
        boolean isDeleted = acknowledgedResponse.isAcknowledged();

        System.out.println(isDeleted);
    }

    @Test
    public void addDoc() throws IOException {

        // 创建索引请求对象
        IndexRequest indexRequest = new IndexRequest("user", "doc", "1");

        User user = new User()
                .setName("allen")
                .setAge(20)
                .setSalary(100.00f)
                .setAddress("上海市")
                .setRemark("allen handsome")
                .setCreateTime(new Date())
                .setBirthday(new Date());

        // 将对象转换为 byte 数组
        byte[] json = JSON.toJSONBytes(user);

        // 设置文档内容
        indexRequest.source(json, XContentType.JSON);

        // 执行增加文档
        IndexResponse response = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);

        System.out.println(response.status());

    }

    @Test
    public void getDoc() throws IOException {

        GetRequest getRequest = new GetRequest("user", "1");

        GetResponse getResponse = restHighLevelClient.get(getRequest, RequestOptions.DEFAULT);

        if (getResponse.isExists()) {
            User user = JSON.parseObject(getResponse.getSourceAsBytes(), User.class);
            System.out.println(user);
        }

    }

    @Test
    public void updateDoc() throws IOException {

        // 创建索引请求对象
        UpdateRequest updateRequest = new UpdateRequest("user", "1");

        User userInfo = new User();
        userInfo.setSalary(200.00f);
        userInfo.setAddress("上海市-浦东区");

        // 将对象转换为 byte 数组
        byte[] json = JSON.toJSONBytes(userInfo);
        // 设置更新文档内容
        updateRequest.doc(json, XContentType.JSON);

        // 执行更新文档
        UpdateResponse response = restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);

        System.out.println(response.status());
    }

    @Test
    public void delDoc() throws IOException {
        // 创建删除请求对象
        DeleteRequest deleteRequest = new DeleteRequest("user", "1");
        // 执行删除文档
        DeleteResponse response = restHighLevelClient.delete(deleteRequest, RequestOptions.DEFAULT);
        System.out.println(response.status());
    }


    private void print(SearchRequest searchRequest) throws IOException {
        // 执行查询，然后处理响应结果
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        if (RestStatus.OK.equals(searchResponse.status()) && searchResponse.getHits().getTotalHits().value > 0) {
            SearchHits hits = searchResponse.getHits();
            for (SearchHit hit : hits) {
                User user = JSON.parseObject(hit.getSourceAsString(), User.class);
                System.out.println(user);
            }
        }
    }

    /**
     * 精确查询（查询条件不会进行分词，但是查询内容可能会分词，导致查询不到）
     */
    @Test
    public void termQuery() throws Exception {

        // 构建查询条件，termQuery 支持多种格式查询
        // 如 boolean、int、double、string 等，这里使用的是 string 的查询
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //searchSourceBuilder.query(QueryBuilders.termQuery("age", "29"));
        searchSourceBuilder.query(QueryBuilders.termsQuery("address.keyword", "北京市丰台区", "北京市昌平区", "北京市大兴区"));

        // 创建查询请求对象，将查询对象配置到其中
        SearchRequest searchRequest = new SearchRequest("user");
        searchRequest.source(searchSourceBuilder);

        // 执行查询，然后处理响应结果
        print(searchRequest);

    }

    /**
     * 匹配查询符合条件的所有数据，并设置分页
     */
    @Test
    public void matchAllQuery() throws Exception {
        //构建查询条件
        MatchAllQueryBuilder matchAllQueryBuilder = QueryBuilders.matchAllQuery();

        // 创建查询源构造器
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(matchAllQueryBuilder);

        //设置分页,排序
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(3);
        searchSourceBuilder.sort("salary", SortOrder.ASC);

        // 创建查询请求对象，将查询对象配置到其中
        SearchRequest searchRequest = new SearchRequest("user");
        searchRequest.source(searchSourceBuilder);

        // 执行查询，然后处理响应结果
        print(searchRequest);

    }

    /**
     * 匹配查询数据
     */
    @Test
    public void matchQuery() throws IOException {
        // 构建查询条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchQuery("address", "*通州区"));

        // 创建查询请求对象，将查询对象配置到其中
        SearchRequest searchRequest = new SearchRequest("user");
        searchRequest.source(searchSourceBuilder);
        print(searchRequest);

    }


    /**
     * 词语匹配查询
     */
    @Test
    public void matchPhraseQuery() throws IOException {

        // 构建查询条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchPhraseQuery("address", "北京市通州区"));
        // 创建查询请求对象，将查询对象配置到其中
        SearchRequest searchRequest = new SearchRequest("user");
        searchRequest.source(searchSourceBuilder);

        // 执行查询，然后处理响应结果
        print(searchRequest);
    }

    /**
     * 内容在多字段中进行查询
     */
    @Test
    public void matchMultiQuery() throws IOException {

        // 构建查询条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.multiMatchQuery("北京市", "address", "remark"));
        // 创建查询请求对象，将查询对象配置到其中
        SearchRequest searchRequest = new SearchRequest("user");
        searchRequest.source(searchSourceBuilder);

        print(searchRequest);
    }

    /**
     * 模糊查询所有以 “三” 结尾的姓名
     */
    @Test
    public void fuzzyQuery() throws IOException {
        // 构建查询条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.fuzzyQuery("name", "三").fuzziness(Fuzziness.AUTO));
        // 创建查询请求对象，将查询对象配置到其中
        SearchRequest searchRequest = new SearchRequest("user");
        searchRequest.source(searchSourceBuilder);

        print(searchRequest);
    }

    /**
     * 查询岁数 ≥ 30 岁的员工数据
     */
    @Test
    public void rangeQuery() throws IOException {
        // 构建查询条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.rangeQuery("age").gte(30));
        // 创建查询请求对象，将查询对象配置到其中
        SearchRequest searchRequest = new SearchRequest("user");
        searchRequest.source(searchSourceBuilder);

        print(searchRequest);

    }

    /**
     * 查询距离现在 30 年间的员工数据
     * [年(y)、月(M)、星期(w)、天(d)、小时(h)、分钟(m)、秒(s)]
     * 例如：
     * now-1h 查询一小时内范围
     * now-1d 查询一天内时间范围
     * now-1y 查询最近一年内的时间范围
     */
    @Test
    public void dateRangeQuery() throws IOException {
        // 构建查询条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // includeLower（是否包含下边界）、includeUpper（是否包含上边界）
        searchSourceBuilder.query(QueryBuilders.rangeQuery("birthDate")
                .gte("now-30y").includeLower(true).includeUpper(true));
        // 创建查询请求对象，将查询对象配置到其中
        SearchRequest searchRequest = new SearchRequest("user");
        searchRequest.source(searchSourceBuilder);

        print(searchRequest);
    }

    /**
     * 查询所有以 “三” 结尾的姓名
     * <p>
     * *：表示多个字符（0个或多个字符）
     * ?：表示单个字符
     */
    @Test
    public void wildcardQuery() throws IOException {
        // 构建查询条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.wildcardQuery("name.keyword", "*三"));
        // 创建查询请求对象，将查询对象配置到其中
        SearchRequest searchRequest = new SearchRequest("user");
        searchRequest.source(searchSourceBuilder);

        print(searchRequest);
    }

    /**
     * 布尔查询
     * 查询出生在 1990-1995 年期间，且地址在 北京市昌平区、北京市大兴区、北京市房山区 的员工信息
     */
    @Test
    public void boolQuery() throws IOException {
        // 创建 Bool 查询构建器
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        // 构建查询条件
        boolQueryBuilder.must(QueryBuilders.termsQuery("address.keyword", "北京市昌平区", "北京市大兴区", "北京市房山区"))
                .filter().add(QueryBuilders.rangeQuery("birthDate").format("yyyy").gte("1990").lte("1995"));
        // 构建查询源构建器
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(boolQueryBuilder);
        // 创建查询请求对象，将查询对象配置到其中
        SearchRequest searchRequest = new SearchRequest("user");
        searchRequest.source(searchSourceBuilder);

        print(searchRequest);
    }

    /**
     * stats 统计员工总数、员工工资最高值、员工工资最低值、员工平均工资、员工工资总和
     */
    @Test
    public void aggregationStats() throws IOException {

        //设置聚合条件
        StatsAggregationBuilder aggr = AggregationBuilders.stats("salary_stats").field("salary");

        // 查询源构建器
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.aggregation(aggr);

        // 设置查询结果不返回，只返回聚合结果
        searchSourceBuilder.size(0);

        // 创建查询请求对象，将查询条件配置到其中
        SearchRequest request = new SearchRequest("user");
        request.source(searchSourceBuilder);

        // 执行请求
        SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        // 获取响应中的聚合信息
        Aggregations aggregations = response.getAggregations();

        // 输出内容
        if (RestStatus.OK.equals(response.status()) && aggregations != null) {
            ParsedStats aggregation = aggregations.get("salary_stats");
            System.out.println("count：{}" + aggregation.getCount());
            System.out.println("avg：{}" + aggregation.getAvg());
            System.out.println("max：{}" + aggregation.getMax());
            System.out.println("min：{}" + aggregation.getMin());
            System.out.println("sum：{}" + aggregation.getSum());

            /*ParsedPercentiles aggregation = aggregations.get("salary_percentiles");
            for (Percentile percentile : aggregation) {
                log.info("百分位：{}：{}", percentile.getPercent(), percentile.getValue());
            }*/
        }
    }

    /**
     * 按岁数进行聚合分桶
     */
    @Test
    public void aggrBucketTerms() throws IOException {

        AggregationBuilder aggr = AggregationBuilders.terms("age_bucket").field("age");

        // 查询源构建器
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.size(10);
        searchSourceBuilder.aggregation(aggr);

        // 创建查询请求对象，将查询条件配置到其中
        SearchRequest request = new SearchRequest("user");
        request.source(searchSourceBuilder);

        // 执行请求
        SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        // 获取响应中的聚合信息
        Aggregations aggregations = response.getAggregations();

        // 输出内容
        if (RestStatus.OK.equals(response.status())) {
            // 分桶
            Terms byCompanyAggregation = aggregations.get("age_bucket");

            List<? extends Terms.Bucket> buckets = byCompanyAggregation.getBuckets();

            for (Terms.Bucket bucket : buckets) {
                System.out.println("桶名：{" + bucket.getKeyAsString() + "} | 总数：{}" + bucket.getDocCount());
            }

        }

    }

    /**
     * 按工资范围进行聚合分桶
     */
    @Test
    public void aggrBucketRange() throws IOException {

        AggregationBuilder aggr = AggregationBuilders.range("salary_range_bucket")
                .field("salary")
                .addUnboundedTo("低级员工", 3000)
                .addRange("中级员工", 5000, 9000)
                .addUnboundedFrom("高级员工", 9000);

        // 查询源构建器
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.size(0);
        searchSourceBuilder.aggregation(aggr);

        // 创建查询请求对象，将查询条件配置到其中
        SearchRequest request = new SearchRequest("user");
        request.source(searchSourceBuilder);

        // 执行请求
        SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        // 获取响应中的聚合信息
        Aggregations aggregations = response.getAggregations();

        if (RestStatus.OK.equals(response.status())) {
            // 分桶
            Range byCompanyAggregation = aggregations.get("salary_range_bucket");
            List<? extends Range.Bucket> buckets = byCompanyAggregation.getBuckets();

            // 输出各个桶的内容
            for (Range.Bucket bucket : buckets) {
                System.out.println("桶名：{" + bucket.getKeyAsString() + "} | 总数：{}" + bucket.getDocCount());
            }
        }

        //return response.toString();
    }


    /**
     * 按照时间范围进行分桶
     */
    @Test
    public void aggrBucketDateRange() throws IOException {

        AggregationBuilder aggr = AggregationBuilders.dateRange("date_range_bucket")
                .field("birthDate")
                .format("yyyy")
                .addRange("1985-1990", "1985", "1990")
                .addRange("1990-1995", "1990", "1995");

        // 查询源构建器
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.size(0);
        searchSourceBuilder.aggregation(aggr);

        // 创建查询请求对象，将查询条件配置到其中
        SearchRequest request = new SearchRequest("user");
        request.source(searchSourceBuilder);

        // 执行请求
        SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        // 获取响应中的聚合信息
        Aggregations aggregations = response.getAggregations();

        // 输出内容
        if (RestStatus.OK.equals(response.status())) {
            // 分桶
            Range byCompanyAggregation = aggregations.get("date_range_bucket");
            List<? extends Range.Bucket> buckets = byCompanyAggregation.getBuckets();

            // 输出各个桶的内容
            for (Range.Bucket bucket : buckets) {
                System.out.println("桶名：{" + bucket.getKeyAsString() + "} | 总数：{}" + bucket.getDocCount());
            }
        }
    }

    /**
     * 按工资多少进行聚合分桶
     */
    @Test
    public void aggrBucketHistogram() throws IOException {

        AggregationBuilder aggr = AggregationBuilders.histogram("salary_histogram")
                .field("salary")
                .extendedBounds(0, 15000)
                .interval(5000);

        // 查询源构建器
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.size(0);
        searchSourceBuilder.aggregation(aggr);

        // 创建查询请求对象，将查询条件配置到其中
        SearchRequest request = new SearchRequest("user");
        request.source(searchSourceBuilder);

        // 执行请求
        SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        // 获取响应中的聚合信息
        Aggregations aggregations = response.getAggregations();

        // 输出内容
        if (RestStatus.OK.equals(response.status())) {
            // 分桶
            Histogram byCompanyAggregation = aggregations.get("salary_histogram");
            List<? extends Histogram.Bucket> buckets = byCompanyAggregation.getBuckets();
            // 输出各个桶的内容
            for (Histogram.Bucket bucket : buckets) {
                System.out.println("桶名：{" + bucket.getKeyAsString() + "} | 总数：{}" + bucket.getDocCount());
            }
        }

    }

    /**
     * 按出生日期进行分桶
     */
    @Test
    public void aggrBucketDateHistogram() throws IOException {
        AggregationBuilder aggr = AggregationBuilders.dateHistogram("birthday_histogram")
                .field("birthDate")
                .interval(1)
                .dateHistogramInterval(DateHistogramInterval.YEAR)
                .format("yyyy");

        // 查询源构建器
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.size(0);
        searchSourceBuilder.aggregation(aggr);

        // 创建查询请求对象，将查询条件配置到其中
        SearchRequest request = new SearchRequest("user");
        request.source(searchSourceBuilder);
        // 执行请求
        SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        // 获取响应中的聚合信息
        Aggregations aggregations = response.getAggregations();
        // 输出内容
        if (RestStatus.OK.equals(response.status())) {
            // 分桶
            Histogram byCompanyAggregation = aggregations.get("birthday_histogram");

            List<? extends Histogram.Bucket> buckets = byCompanyAggregation.getBuckets();
            // 输出各个桶的内容
            for (Histogram.Bucket bucket : buckets) {
                System.out.println("桶名：{" + bucket.getKeyAsString() + "} | 总数：{}" + bucket.getDocCount());
            }
        }

    }


    /**
     * topHits 按岁数分桶、然后统计每个员工工资最高值
     */
    @Test
    public void aggregationTopHits() throws IOException {

        AggregationBuilder testTop = AggregationBuilders.topHits("salary_max_user")
                .size(1)
                .sort("salary", SortOrder.DESC);

        AggregationBuilder salaryBucket = AggregationBuilders.terms("salary_bucket")
                .field("age")
                .size(10);

        salaryBucket.subAggregation(testTop);


        // 查询源构建器
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.size(0);
        searchSourceBuilder.aggregation(salaryBucket);

        // 创建查询请求对象，将查询条件配置到其中
        SearchRequest request = new SearchRequest("user");
        request.source(searchSourceBuilder);

        // 执行请求
        SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        // 获取响应中的聚合信息
        Aggregations aggregations = response.getAggregations();

        // 输出内容
        if (RestStatus.OK.equals(response.status())) {
            // 分桶
            Terms byCompanyAggregation = aggregations.get("salary_bucket");
            List<? extends Terms.Bucket> buckets = byCompanyAggregation.getBuckets();
            // 输出各个桶的内容
            for (Terms.Bucket bucket : buckets) {
                System.out.println("桶名：{}"+ bucket.getKeyAsString());
                ParsedTopHits topHits = bucket.getAggregations().get("salary_max_user");
                for (SearchHit hit : topHits.getHits()) {
                    System.out.println(hit.getSourceAsString());
                }
            }
        }

    }

    @Test
    public void highLightQuery() throws IOException {

        // 构建查询条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.rangeQuery("age").gte(30));

        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("age");
        highlightBuilder.preTags("<em>").postTags("</em>");

        searchSourceBuilder.highlighter(highlightBuilder);

        // 创建查询请求对象，将查询对象配置到其中
        SearchRequest searchRequest = new SearchRequest("user");
        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        //获取高亮field
        Map<String, HighlightField> highlightFields = searchResponse.getHits().getAt(0).getHighlightFields();

        highlightFields.forEach((k,v) -> {
            System.out.println(k+ " -> " + v);
        });


    }



}
