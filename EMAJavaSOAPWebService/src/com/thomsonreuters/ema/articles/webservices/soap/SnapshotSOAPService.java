package com.thomsonreuters.ema.articles.webservices.soap;

import java.util.Iterator;
import java.util.concurrent.TimeoutException;

import com.thomsonreuters.ema.access.AckMsg;
import com.thomsonreuters.ema.access.DataType;
import com.thomsonreuters.ema.access.EmaFactory;
import com.thomsonreuters.ema.access.FieldEntry;
import com.thomsonreuters.ema.access.GenericMsg;
import com.thomsonreuters.ema.access.Msg;
import com.thomsonreuters.ema.access.OmmConsumer;
import com.thomsonreuters.ema.access.OmmConsumerClient;
import com.thomsonreuters.ema.access.OmmConsumerEvent;
import com.thomsonreuters.ema.access.RefreshMsg;
import com.thomsonreuters.ema.access.ReqMsg;
import com.thomsonreuters.ema.access.StatusMsg;
import com.thomsonreuters.ema.access.UpdateMsg;
import com.thomsonreuters.ema.articles.webservices.common.ConnectionContext;
import com.thomsonreuters.ema.articles.webservices.common.InstrumentData;

public class SnapshotSOAPService {
	public static final int TIMEOUT = 5;
	public static final String SERVICE_NAME = "API_ELEKTRON_EPD_RSSL";
	private InstrumentData data = new InstrumentData();
	
	public synchronized InstrumentData subscribe(String ric) {
		synchronized (data) {
			AppClient appClient = new AppClient();
			OmmConsumer consumer = ConnectionContext.getConsumer();
			ReqMsg reqMsg = EmaFactory.createReqMsg();
			reqMsg.clear();
			
			try {
				if (consumer == null) throw new RuntimeException("Connection: Cannot retrieve OmmConsumer instance. Unable to make a connection to server.");
				consumer.registerClient(reqMsg.serviceName(SERVICE_NAME).name(ric).interestAfterRefresh(false), appClient);
				data.wait(TIMEOUT * 1000);
				if (data == null) throw new TimeoutException("Timeout: Cannot retrieve data within " + TIMEOUT + " seconds");
			} catch (Exception e) {
				data.setRic(ric);
				data.setStreamStatus("SUSPECT");
				data.setStatusText(e.getClass().getName() + ":" + e.getMessage());
			}
			return data;
		}
	}
	
	class AppClient implements OmmConsumerClient
	{
		public static final int FID_BID = 22;
		public static final int FID_ASK = 25;
		public void onRefreshMsg(RefreshMsg refreshMsg, OmmConsumerEvent event)
		{
			synchronized (data) {
				data.setRic(refreshMsg.name());
				data.setStreamStatus(refreshMsg.state().dataStateAsString());
				data.setStatusText(refreshMsg.state().statusText());
				if (DataType.DataTypes.FIELD_LIST == refreshMsg.payload().dataType()) {
					Iterator<FieldEntry> iter = refreshMsg.payload().fieldList().iterator();
					FieldEntry fieldEntry;
					while (iter.hasNext())
					{
						fieldEntry = iter.next();
						if (fieldEntry.fieldId() == FID_BID) {
							data.setBid(fieldEntry.load().toString());
							continue;
						} else if (fieldEntry.fieldId() == FID_ASK) {
							data.setAsk(fieldEntry.load().toString());
							continue;
						} else if (data.getBid() != null && data.getAsk() != null) break;
					}
				}
				data.notify();
			}
		}

		public void onStatusMsg(StatusMsg statusMsg, OmmConsumerEvent event) 
		{
			data.setRic(statusMsg.name());
			data.setStreamStatus(statusMsg.state().dataStateAsString());
			data.setStatusText(statusMsg.state().statusText());
		}
		
		public void onUpdateMsg(UpdateMsg updateMsg, OmmConsumerEvent event) {}
		public void onGenericMsg(GenericMsg genericMsg, OmmConsumerEvent consumerEvent){}
		public void onAckMsg(AckMsg ackMsg, OmmConsumerEvent consumerEvent){}
		public void onAllMsg(Msg msg, OmmConsumerEvent consumerEvent){}
	}
}
