package com.atguigu.gmall.search.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.search.vo.GoodsVO;
import com.atguigu.gmall.search.vo.SearchParamVO;
import com.atguigu.gmall.search.vo.SearchResponse;
import com.atguigu.gmall.search.vo.SearchResponseAttrVO;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.search.aggregation.ChildrenAggregation;
import io.searchbox.core.search.aggregation.GeoBoundsAggregation;
import io.searchbox.core.search.aggregation.MetricAggregation;
import io.searchbox.core.search.aggregation.TermsAggregation;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SearchService {

    @Autowired
    private JestClient jestClient;

    public SearchResponse search(SearchParamVO searchParamVO) {
        try {
            String dsl = buildDSL(searchParamVO);
            //打印dsl语句
             System.out.println(dsl);
            //构建查询库属性
            Search search = new Search.Builder(dsl).addIndex("goods").addType("info").build();
            SearchResult searchResult = this.jestClient.execute(search);

            SearchResponse response =  parseRestlt(searchResult);
            //分页参数直接从请求参数获取
            response.setPageNum(searchParamVO.getPageNum());
            response.setPageSize(searchParamVO.getPageSize());
            response.setTotal(searchResult.getTotal());//获取命中的总记录条数

            return response;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private SearchResponse parseRestlt(SearchResult result) {
        SearchResponse response = new SearchResponse();

        //获取元数据聚合结果集
        MetricAggregation aggregations = result.getAggregations();

        //解析品牌的聚合结果集
        TermsAggregation brandAgg = aggregations.getTermsAggregation("brandAgg");
        List<TermsAggregation.Entry> brandAggBuckets = brandAgg.getBuckets();
        if (!CollectionUtils.isEmpty(brandAggBuckets)){
        SearchResponseAttrVO attrVO = new SearchResponseAttrVO();
        attrVO.setName("品牌");
        List<String> brandValue = brandAggBuckets.stream().map(brandAggBucket -> {
            HashMap<String, Object> map = new HashMap<>();
            map.put("id",brandAggBucket.getKeyAsString());
            TermsAggregation brandName = brandAggBucket.getTermsAggregation("brandName");
            map.put("name",brandName.getBuckets().get(0).getKeyAsString());
            return JSON.toJSONString(map);
        }).collect(Collectors.toList());
        attrVO.setValue(brandValue);
        response.setBrand(attrVO);
        }

        //解析分类的聚合结果集
        TermsAggregation categoryAgg = result.getAggregations().getTermsAggregation("categoryAgg");
        List<TermsAggregation.Entry> categoryAggBuckets = categoryAgg.getBuckets();
        if (!CollectionUtils.isEmpty(categoryAggBuckets)){
            SearchResponseAttrVO categoryVO = new SearchResponseAttrVO();
            categoryVO.setName("分类");
            List<String> categoryValues = categoryAggBuckets.stream().map(categoryAggBucket -> {
                HashMap<String, Object> map = new HashMap<>();
                map.put("id",categoryAggBucket.getKeyAsString());
                map.put("id",categoryAggBucket.getTermsAggregation("categoryName").getBuckets().get(0).getKeyAsString());
                return JSON.toJSONString(map);
            }).collect(Collectors.toList());
            categoryVO.setValue(categoryValues);
            response.setCatelog(categoryVO);
        }

        //解析搜索属性的聚合结果集
        ChildrenAggregation attrAgg = aggregations.getChildrenAggregation("attrAgg");
        TermsAggregation attrIdAgg = attrAgg.getTermsAggregation("attrIdAgg");
        List<TermsAggregation.Entry> attrIdAggBuckets = attrIdAgg.getBuckets();
        List<SearchResponseAttrVO> attrVOS = attrIdAggBuckets.stream().map(attrIdAggBucket -> {
            SearchResponseAttrVO attrVO = new SearchResponseAttrVO();
            attrVO.setProductAttributeId(Long.valueOf(attrIdAggBucket.getKeyAsString()));
            TermsAggregation attrNameAgg = attrIdAggBucket.getTermsAggregation("attrNameAgg");
            TermsAggregation attrValueAgg = attrIdAggBucket.getTermsAggregation("attrValueAgg");
            List<String> values = attrValueAgg.getBuckets().stream().map(bucket -> {
                return bucket.getKeyAsString();
            }).collect(Collectors.toList());
            attrVO.setName(attrNameAgg.getBuckets().get(0).getKeyAsString());
            attrVO.setValue(values);
            return attrVO;
        }).collect(Collectors.toList());
        response.setAttrs(null);

        //解析商品的结果集
        List<GoodsVO> GoodsVOS = result.getSourceAsObjectList(GoodsVO.class, false);
        response.setProducts(GoodsVOS);

        return response;
    }

    private String buildDSL(SearchParamVO searchParamVO) {
        //创建一个用来构建dsl语句的对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //1.构建查询条件和过滤条件
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        //获取检索字段
        String keyword = searchParamVO.getKeyword();
        if (StringUtils.isNotEmpty(keyword)){
            //真实开发中，如果用户没有输入检索字段，那么应该显示默认信息(也就是给钱多的信息)
        boolQuery.must(QueryBuilders.matchQuery("name",keyword).operator(Operator.AND));

        }
        //构建过滤条件
        //品牌
        String[] brands = searchParamVO.getBrand();
        if (ArrayUtils.isNotEmpty(brands)){
        boolQuery.filter(QueryBuilders.termsQuery("brandId",brands));
        }

        //分类
        String[] catelog3 = searchParamVO.getCatelog3();
        if (ArrayUtils.isNotEmpty(catelog3)){
        boolQuery.filter(QueryBuilders.termsQuery("productCategoryId",catelog3));
        }

        //搜索的规格属性的过滤
        String[] props = searchParamVO.getProps();
        if (ArrayUtils.isNotEmpty(props)){
            for (String prop : props) {
                String[] attr = StringUtils.split(prop, ":");
                if (attr != null && attr.length == 2){
                    BoolQueryBuilder attrBoolQuery = QueryBuilders.boolQuery();
                   attrBoolQuery.must(QueryBuilders.termQuery("attrValueList.productAttributeId", attr[0]));
                    String[] attr1 = StringUtils.split(attr[1], "-");
                    attrBoolQuery.must(QueryBuilders.termsQuery("attrValueList.value",attr1));
                    boolQuery.filter(QueryBuilders.nestedQuery("attrValueList",attrBoolQuery, ScoreMode.None));
                }
            }
        }
        searchSourceBuilder.query(boolQuery);


        //2.构建分页条件
        Integer pageNum = searchParamVO.getPageNum();
        Integer pageSize = searchParamVO.getPageSize();

        searchSourceBuilder.from(( pageNum -1 ) * pageSize);//因为当前数据的算法是，当前页的页码减1然后乘以每一页的记录数量
        searchSourceBuilder.size(pageSize);

        //3.构建排序条件
        String order = searchParamVO.getOrder();
        String[] orders = StringUtils.split(order, "-");
        if (ArrayUtils.isNotEmpty(orders) && orders.length == 2){
            SortOrder sortOrder = StringUtils.equals("asc",orders[1]) ? SortOrder.ASC : SortOrder.DESC;
            switch (orders[0]){
                case "0": searchSourceBuilder.sort("_score",sortOrder); break;
                case "1": searchSourceBuilder.sort("sale",sortOrder); break;
                case "2": searchSourceBuilder.sort("price",sortOrder); break;
                default: break;
            }
        }

        //4.构建高亮条件
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("name");
        highlightBuilder.preTags("<font color='red'>");
        highlightBuilder.postTags("</font>");
        searchSourceBuilder.highlighter(highlightBuilder);


        //5.构建聚合条件
        //品牌
        searchSourceBuilder.aggregation(AggregationBuilders.terms("brandAgg").field("brandId")
                .subAggregation(AggregationBuilders.terms("brandName").field("brandName")));

        //分类
        searchSourceBuilder.aggregation(AggregationBuilders.terms("categoryAgg").field("productCategoryId")
                .subAggregation(AggregationBuilders.terms("categoryName").field("productCategoryName")));

        //搜索属性
        searchSourceBuilder.aggregation(AggregationBuilders.nested("attrAgg","attrValueList")
                .subAggregation(AggregationBuilders.terms("attrIdAgg").field("attrValueList.productAttributeId")
                        .subAggregation(AggregationBuilders.terms("attrNameAgg").field("attrValueList.name"))
                        .subAggregation(AggregationBuilders.terms("attrValueAgg").field("ttrValueList.value")))
        );


        return searchSourceBuilder.toString();
    }


}
