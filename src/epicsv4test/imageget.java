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
		
		// make an empty ntndarray, so I know the structure.
		NTNDArrayBuilder ntab=NTNDArray.createBuilder();
		NTNDArray nta = ntab.create();
		
		// can't I just ask the pv server what the structure is? why should I even be doing the above?
		// Monitor needs to know what fields to monitor, and it defaults to timestamp, value, and something else,
		// ignoring other fields. So when we monitor, we need tell. it to monituyr a complete NTNDArray.
		// how do we do this? cant we just ask the server what the PV is, then monitor it?
		
		
		
		//
		// Here we get lis of flields in the NTNDArray and then make a String
		// I should not have to do this.....
		
		Structure pvsnt = nta.getPVStructure().getStructure();
		String[] fns =pvsnt.getFieldNames();
		String reqstr = "";
		int slen=fns.length - 1;
		int k;
		for (k=0;k<slen;k++)
		{
			reqstr = reqstr + fns[k];
			reqstr = reqstr +",";
			
				
		}
		reqstr = reqstr + fns[slen];
		
		
						
		//Now we can monitor all the fields. But this seems idiotuic.
		PvaClientMonitor pvamon=mychannel.monitor(reqstr);
		
		//I should be able to do this:
		//PvaClientMonitor pvamon=mychannel.createMonitor(nta.getPVStructure())
		//the probolem is that when I monotir.start, the hosting IOC core dumps.
		
		//pvamon.connect();
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
		
		
		NTNDArray myarray =NTNDArray.wrapUnsafe(pvs);
		//The wrap unsafe leaves out most of the NTNDArray fields. but wrap() feils and returns null.
		//
		
		//So I can get the data. How do I know the Union is holdibng bytes[], floats[] or ints[]?
		// Not sure how to ask NTNDArray the data type
		PVUnion pvu = myarray.getValue();
		
		//PVStructureArray dims = myarray.getDimension();
		
		//org.epics.pvdata.pv.Field a = pvu.getField();
		//PVInt uniqid = myarray.getUniqueId();
		
		
		//PVStructureArray psa =myarray.getAttribute();
	
		System.out.println("got data " );

		pvamon.releaseEvent();
		
		
		//PvaClientGetData data = pvcg.getData();
		//NTNDArray myarray =NTNDArray.wrap(data.getPVStructure());

		
		
		}
	
		}
	
		
		System.out.println("Maddens test java end");

	}

}
