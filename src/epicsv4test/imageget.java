package epicsv4test;

import org.epics.pvaClient.PvaClient;
import org.epics.pvaClient.PvaClientChannel;
import org.epics.pvaClient.PvaClientGet;
import org.epics.pvaClient.PvaClientGetData;
import org.epics.pvaClient.PvaClientMonitor;
import org.epics.pvaClient.PvaClientMonitorData;
import org.epics.pvaccess.ClientFactory;
import org.epics.pvaccess.client.ChannelProviderRegistry;
import org.epics.pvdata.pv.PVScalarArray;
import org.epics.pvdata.pv.Status;
import org.epics.pvdata.pv.Structure;
import org.epics.pvdata.pv.LongArrayData;
import org.epics.pvdata.copy.CreateRequest;
import org.epics.pvdata.factory.BasePVUByteArray;
import org.epics.pvdata.pv.ByteArrayData;
import org.epics.pvdata.pv.Convert;
import org.epics.pvdata.pv.PVLongArray;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.PVStructureArray;
import org.epics.pvdata.pv.PVUByteArray;
import org.epics.pvdata.pv.PVUnion;
import org.epics.pvdata.pv.PVByteArray;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVInt;

import java.lang.reflect.Field;

import org.epics.nt.*;

public class imageget {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		System.out.println("Maddens test java get image");
		if (true)
		{
		PvaClient pva=PvaClient.get();
		PvaClientChannel mychannel = pva.channel("13SIM1:Pva1:Image");
		
		
		
		
		
		PVStructure read_request = CreateRequest.create().createRequest("field()");
		PvaClientMonitor pvamon=mychannel.createMonitor(read_request);

	
			
	
		pvamon.start();
		
		
		//This almost cirrectly gets the NTNDArray.
		PvaClientMonitorData easydata = pvamon.getData();
		
		//Structure strct=easydata.getStructure();
	
		while(true)
		{
		pvamon.waitEvent(0);
		 easydata = pvamon.getData();
		 
		//String sc = easydata.showChanged();
		
		//Structure strc = easydata.getStructure();
		PVStructure pvs = easydata.getPVStructure();
		String [] fnames = easydata.getStructure().getFieldNames();
		
		for (int m=0;m<fnames.length;m++)
			System.out.println(fnames[m]);
		
		
		//the problem here is that if I wrap(), it fails and returns null.
		// but only requesting the individual fields, it does not know its an NTNDArray.
		
		
		//!! why does wrap return null? it should work?
		NTNDArray myarray =NTNDArray.wrapUnsafe(pvs);
		//The wrap unsafe leaves out most of the NTNDArray fields. but wrap() feils and returns null.
		//
		
		//So I can get the data. How do I know the Union is holdibng bytes[], floats[] or ints[]?
		// Not sure how to ask NTNDArray the data type
		PVUnion pvu = myarray.getValue();
			
		
		//so how do I know of the data in pvu is short[] int[] or what? That
		//info is stored somewhere...
		
	
		System.out.println("got data " );

		pvamon.releaseEvent();
		
		
		//PvaClientGetData data = pvcg.getData();
		//NTNDArray myarray =NTNDArray.wrap(data.getPVStructure());

		
		
		}
	
		}
	
		
		System.out.println("Maddens test java end");

	}

}
