package com.anthony.demo;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

@RunWith(SpringRunner.class)
@SpringBootTest
public class WebpagemonitorApplicationTests {

	@Autowired
	WebPageParser webPageParser;


	 @Test
	 public void testHasSignificantChange() throws IOException {
		 File resourcesDirectory = new File("src/test/resources");
		 String file1 = resourcesDirectory.getAbsolutePath() + "/file1.txt";
		 String file2 = resourcesDirectory.getAbsolutePath() + "/file2.txt";
		 String file3 = resourcesDirectory.getAbsolutePath() + "/file3.txt";

		 double changePercentage = webPageParser.evaluateChangePercentage(file1,file2);
		 Assert.assertTrue(round(changePercentage,2)==0.42);

		 //9 characters add to the end of the file
		 changePercentage = webPageParser.evaluateChangePercentage(file3,file1);
		 Assert.assertTrue(round(changePercentage,2)==0.05);
	 }

	public double round(double value, int places) {
		if (places < 0) throw new IllegalArgumentException();

		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}
}
