package com.thomsonreuters.ema.articles.webservices.common;

import com.thomsonreuters.ema.access.EmaFactory;
import com.thomsonreuters.ema.access.OmmConsumer;
import com.thomsonreuters.ema.access.OmmConsumerConfig;
import com.thomsonreuters.ema.access.OmmException;

public class ConnectionContext {
	public static final String HOST = "192.168.27.46:14002";
	public static final String USER = "user";
	private static OmmConsumer consumer = null;
	
	static {
		try
		{
			OmmConsumerConfig config = EmaFactory.createOmmConsumerConfig();
			consumer  = EmaFactory.createOmmConsumer(config.host(HOST).username(USER));
		}
		catch (OmmException excp)
		{
			excp.printStackTrace();
		}
	}
	
	private ConnectionContext() {}
	
	public static OmmConsumer getConsumer() {
		return consumer;
	}
	
}
