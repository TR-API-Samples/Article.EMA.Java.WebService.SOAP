package com.thomsonreuters.ema.articles.webservices.test;

import com.thomsonreuters.ema.articles.webservices.common.InstrumentData;
import com.thomsonreuters.ema.articles.webservices.soap.SnapshotSOAPService;

public class SnapshotSOAPServiceTest {
	public static void main(String[] args) {
		SnapshotSOAPService obj = new SnapshotSOAPService();
		InstrumentData result = obj.subscribe("TRI.N");
		System.out.println(result);
	}
}
