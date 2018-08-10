package com.atguigu.gmall.gmalllistservice;

import com.atguigu.gmall.bean.SkuLsParams;
import com.atguigu.gmall.bean.SkuLsResult;
import com.atguigu.gmall.service.ListService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallListServiceApplicationTests {
	@Autowired
	private JestClient jestClient;

	@Autowired
	private ListService listService;
	@Test
	public void contextLoads() {
	}
	/*@Test
	public void testEls() throws IOException {
		String query = "{\n" +
				"  \"query\": {\n" +
				"    \"match\": {\n" +
				"      \"actorList.name\": \"张译\"\n" +
				"    }\n" +
				"  }\n" +
				"}";

		Search search = new Search.Builder(query).addIndex("movie_chn").addType("movie_type_chn").build();

		SearchResult result = jestClient.execute(search);

		List<SearchResult.Hit<HashMap, Void>> hits = result.getHits(HashMap.class);

		for (SearchResult.Hit<HashMap, Void> hit : hits) {
			HashMap source = hit.source;

			System.out.println(source.get("name"));
		}
	}
	@Test
	public void testSearh(){

		SkuLsParams skuLsParams = new SkuLsParams();

		skuLsParams.setCatalog3Id("61");

		skuLsParams.setPageNo(1);

		skuLsParams.setPageSize(1);

		skuLsParams.setValueId(new String[]{"13"});

		skuLsParams.setKeyword("一加");

		SkuLsResult search = listService.getSearch(skuLsParams);

		System.out.println(search.toString());

	}*/
}

