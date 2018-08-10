package com.atguigu.gmall.item;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Collections;


@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallItemWebApplicationTests {

	@Test
	public void contextLoads() {
		Collections.synchronizedList(new ArrayList<>());

	}

}
